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
import androidx.compose.material.icons.filled.Audiotrack // <-- IMPORT ICON LOA
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
    val isLoading by viewModel.isLoading.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }

    // CÔNG CỤ CHỌN ẢNH
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    // CÔNG CỤ CHỌN FILE ÂM THANH
    var selectedSoundUri by remember { mutableStateOf<Uri?>(null) }
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> selectedSoundUri = uri }
    )

    // BIẾN CHO VẼ TAY
    val drawPath = remember { Path() }
    var pathTrigger by remember { mutableIntStateOf(0) }

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

                                Row(modifier = Modifier.padding(top = 6.dp)) {
                                    if (!card.imageUrl.isNullOrEmpty()) {
                                        Text("🖼️ Có ảnh", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
                                    }
                                    if (!card.sound.isNullOrEmpty()) {
                                        Text("🎵 Có âm thanh", color = Color(0xFF9C27B0), fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp))
                                    }
                                    if (!card.drawData.isNullOrEmpty() && card.drawData != "null") {
                                        Text("✍️ Có nét vẽ", color = MaterialTheme.colorScheme.tertiary, fontSize = 12.sp)
                                    }
                                }
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

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Tạo Thẻ Mới", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = frontText,
                        onValueChange = { frontText = it },
                        label = { Text("Câu hỏi / Từ vựng (Mặt trước)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = backText,
                        onValueChange = { backText = it },
                        label = { Text("Đáp án (Mặt sau)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Đính kèm (Tùy chọn)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    // NÚT CHỌN ẢNH
                    Button(
                        onClick = { photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedImageUri == null) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (selectedImageUri == null) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedImageUri == null) "Chọn ảnh từ Thư viện" else "Đã đính kèm 1 ảnh")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // NÚT CHỌN ÂM THANH
                    Button(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedSoundUri == null) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFF3E5F5),
                            contentColor = if (selectedSoundUri == null) MaterialTheme.colorScheme.onSecondaryContainer else Color(0xFF6A1B9A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Audiotrack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedSoundUri == null) "Chọn File Âm thanh (MP3)" else "Đã đính kèm 1 file Audio")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Ghi chú nét vẽ (Tùy chọn)", fontSize = 13.sp, color = Color.DarkGray)
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(top = 4.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                    ) {
                        Canvas(
                            modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        drawPath.moveTo(offset.x, offset.y)
                                        pathTrigger++ // Ép vẽ lại
                                    },
                                    onDrag = { change, _ ->
                                        drawPath.lineTo(change.position.x, change.position.y)
                                        pathTrigger++ // Ép vẽ lại
                                    }
                                )
                            }
                        ) {
                            val trigger = pathTrigger // Lắng nghe cò súng
                            drawPath(path = drawPath, color = Color.Black, style = Stroke(width = 8f))
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = {
                            drawPath.reset()
                            pathTrigger++
                        }) {
                            Text("Xóa nét vẽ", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (frontText.isNotBlank() && backText.isNotBlank()) {
                            val imgUrl = selectedImageUri?.toString()
                            val soundUrl = selectedSoundUri?.toString() // Gán link âm thanh

                            viewModel.addCard(
                                token = token,
                                deckId = deck._id,
                                front = frontText,
                                back = backText,
                                type = "TEXT",
                                imageUrl = imgUrl,
                                drawData = "CanvasData",
                                sound = soundUrl // Đẩy link vào ViewModel
                            ) {
                                showDialog = false
                                // Reset hết sạch sành sanh sau khi lưu thành công
                                frontText = ""; backText = ""; selectedImageUri = null; selectedSoundUri = null; drawPath.reset(); pathTrigger++
                            }
                        }
                    },
                    enabled = !isLoading && frontText.isNotBlank() && backText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isLoading) "Đang lưu..." else "Lưu thẻ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Hủy", color = Color.Gray) }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}