package com.example.n.ui.screens

import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    var currentAlgorithm by remember { mutableStateOf(deck.algorithm ?: "SM2") }
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

            // HEADER: Số lượng thẻ
            item {
                Text(
                    "Thư viện thẻ: ${cards.size}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // THUẬT TOÁN LÊN LỊCH ÔN TẬP
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Thuật toán lên lịch ôn tập",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    0.5.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            listOf("SM2", "FSRS").forEach { alg ->
                                val isSelected = currentAlgorithm == alg
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            currentAlgorithm = alg
                                            viewModel.updateDeckAlgorithm(token, deck._id, alg)
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (alg == "FSRS") "FSRS ✦" else "SM-2",
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (currentAlgorithm == "FSRS")
                                "Thuật toán hiện đại — lên lịch chính xác hơn sau 500+ lần ôn tập."
                            else
                                "Thuật toán cổ điển — đơn giản và ổn định cho mọi kích thước bộ bài.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // FSRS OPTIMIZER UI — chỉ hiện khi đang dùng FSRS
            if (currentAlgorithm == "FSRS") {
                item {
                    val isOptimizing by viewModel.isOptimizing.collectAsState()
                    val optimizeResult by viewModel.optimizeResult.collectAsState()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // TIÊU ĐỀ
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "⚙️ Tối ưu hóa FSRS",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "Chạy tối ưu để tính lại trọng số dựa trên lịch sử ôn tập thực tế của bạn. Cần ít nhất 500 lần ôn tập.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // NÚT CHẠY TỐI ƯU
                            Button(
                                onClick = {
                                    viewModel.clearOptimizeResult()
                                    viewModel.optimizeDeck(token, deck._id)
                                },
                                enabled = !isOptimizing,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isOptimizing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Đang tối ưu hóa...", color = Color.White, fontSize = 14.sp)
                                } else {
                                    Text("🚀 Chạy tối ưu hóa", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // KẾT QUẢ
                            optimizeResult?.let { result ->
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(12.dp))

                                if (result.success) {
                                    // THÀNH CÔNG
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Tối ưu thành công!",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // BẢNG TRỌNG SỐ
                                    Text(
                                        "Trọng số mới (17 tham số):",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    // HIỂN THỊ WEIGHTS THEO LƯỚI 3 CỘT
                                    val chunked = result.weights.chunked(3)
                                    chunked.forEachIndexed { rowIndex, rowWeights ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            rowWeights.forEachIndexed { colIndex, weight ->
                                                val paramIndex = rowIndex * 3 + colIndex + 1
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .background(
                                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            "w$paramIndex",
                                                            fontSize = 10.sp,
                                                            color = Color.Gray
                                                        )
                                                        Text(
                                                            "%.3f".format(weight),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                            // Nếu hàng cuối không đủ 3 ô thì fill khoảng trống
                                            repeat(3 - rowWeights.size) {
                                                Box(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                } else {
                                    // THẤT BẠI
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFFE53935),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            result.message,
                                            fontSize = 13.sp,
                                            color = Color(0xFFE53935),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // DANH SÁCH THẺ TRỐNG
            if (cards.isEmpty()) {
                item {
                    Text(
                        "Bạn chưa có thẻ nào. Bấm dấu + để tạo tuyệt tác đầu tiên!",
                        color = Color.Gray
                    )
                }
            } else {
                // DANH SÁCH THẺ
                items(cards) { card ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    card.front,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "💡 Đáp án: ${card.back}",
                                    color = Color.DarkGray
                                )
                                Row(modifier = Modifier.padding(top = 6.dp)) {
                                    if (!card.imageUrl.isNullOrEmpty()) {
                                        Text(
                                            "🖼️ Có ảnh",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                    if (!card.sound.isNullOrEmpty()) {
                                        Text(
                                            "🎵 Có âm thanh",
                                            color = Color(0xFF9C27B0),
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                    }
                                    if (!card.drawData.isNullOrEmpty() && card.drawData != "null") {
                                        Text(
                                            "✍️ Có nét vẽ",
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.deleteCard(token, card._id) }) {
                                Icon(Icons.Filled.Delete, "Xóa thẻ", tint = Color.Red)
                            }
                        }
                    }
                }

                // KHOẢNG CÁCH CUỐI ĐỂ FAB KHÔNG CHE THẺ CUỐI
                item {
                    Spacer(modifier = Modifier.height(80.dp))
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