package com.example.n.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n.network.DeckResponse
import com.example.n.viewmodel.FlashcardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    token: String,
    deck: DeckResponse,
    viewModel: FlashcardViewModel,
    onBack: () -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // THÊM: Các biến cho thẻ Premium
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf("TEXT") } // TEXT, IMAGE, TOUCH
    var expandedDropdown by remember { mutableStateOf(false) }

    // THÊM: Biến cho Ảnh
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    // THÊM: Biến cho Touch Recall (Canvas vẽ tay)
    var drawPath by remember { mutableStateOf(Path()) }

    LaunchedEffect(deck._id) {
        viewModel.fetchCards(token, deck._id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deck.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Add, contentDescription = "Thêm thẻ", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            item { Text("Thư viện thẻ: ${cards.size}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)) }

            if (cards.isEmpty()) {
                item { Text("Bạn chưa có thẻ nào. Bấm dấu + để tạo tuyệt tác đầu tiên!", color = Color.Gray) }
            } else {
                items(cards) { card ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(card.front, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("💡 Đáp án: ${card.back}", color = Color.DarkGray)

                                // Hiển thị loại thẻ mờ mờ ở dưới
                                Text("Loại: ${card.type ?: "TEXT"}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                            IconButton(onClick = { viewModel.deleteCard(token, card._id) }) {
                                Icon(Icons.Filled.Delete, "Xóa thẻ", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOG TẠO THẺ PREMIUM
    // ==========================================
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Tạo Thẻ Mới", fontWeight = FontWeight.ExtraBold) },
            text = {
                // Thêm scroll để không bị tràn màn hình khi mở tính năng Vẽ
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                    // 1. CHỌN LOẠI THẺ
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = when(cardType) {
                                "TEXT" -> "Nhập phím (Truyền thống)"
                                "IMAGE" -> "Nhìn ảnh đoán chữ"
                                "TOUCH" -> "Touch Recall (Vẽ tay)"
                                else -> "Nhập phím"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Chế độ học") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                            DropdownMenuItem(text = { Text("Nhập phím (Truyền thống)") }, onClick = { cardType = "TEXT"; expandedDropdown = false })
                            DropdownMenuItem(text = { Text("Nhìn ảnh đoán chữ") }, onClick = { cardType = "IMAGE"; expandedDropdown = false })
                            DropdownMenuItem(text = { Text("Touch Recall (Vẽ tay)") }, onClick = { cardType = "TOUCH"; expandedDropdown = false })
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. MẶT TRƯỚC (CÂU HỎI)
                    OutlinedTextField(
                        value = frontText,
                        onValueChange = { frontText = it },
                        label = { Text("Câu hỏi / Từ vựng (Mặt trước)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. MẶT SAU (TÙY THEO LOẠI THẺ MÀ HIỂN THỊ KHÁC NHAU)
                    when (cardType) {
                        "TEXT" -> {
                            OutlinedTextField(
                                value = backText,
                                onValueChange = { backText = it },
                                label = { Text("Đáp án (Mặt sau)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        "IMAGE" -> {
                            Button(
                                onClick = { photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Filled.Image, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (selectedImageUri == null) "Chọn ảnh từ Thư viện" else "Đã chọn 1 ảnh")
                            }
                            OutlinedTextField(
                                value = backText,
                                onValueChange = { backText = it },
                                label = { Text("Đáp án (Gõ chữ)") },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                        }
                        "TOUCH" -> {
                            Text("Touch Recall (Bút Tích)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Dùng tay viết đáp án vào ô dưới đây:", fontSize = 12.sp, color = Color.Gray)

                            // VÙNG VẼ TAY CẢM ỨNG XỊN XÒ
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(top = 8.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                            ) {
                                Canvas(
                                    modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { offset -> drawPath.moveTo(offset.x, offset.y) },
                                            onDrag = { change, _ -> drawPath.lineTo(change.position.x, change.position.y) }
                                        )
                                    }
                                ) {
                                    drawPath(path = drawPath, color = Color.Black, style = Stroke(width = 8f))
                                }
                            }
                            // Nút xóa nét vẽ
                            TextButton(onClick = { drawPath = Path() }) { Text("Xóa nháp") }

                            // Vẫn cần chỗ lưu kết quả chuẩn để hệ thống chấm điểm
                            OutlinedTextField(
                                value = backText,
                                onValueChange = { backText = it },
                                label = { Text("Đáp án gốc (Để máy chấm)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (frontText.isNotBlank() && backText.isNotBlank()) {
                        val imgUrl = selectedImageUri?.toString()

                        // ĐÃ SỬA: Gọi đúng hàm addCard với đầy đủ tham số của bản Premium
                        viewModel.addCard(token, deck._id, frontText, backText, cardType, imgUrl, "CanvasData") {
                            showDialog = false
                            frontText = ""; backText = ""; selectedImageUri = null; drawPath = Path() // Reset
                        }
                    }
                }) { Text("Lưu tuyệt tác") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Hủy") }
            }
        )
    }
}