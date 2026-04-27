package com.example.n.ui.screens

import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.n.network.CardResponse
import com.example.n.viewmodel.FlashcardViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    token: String,
    viewModel: FlashcardViewModel,
    onFinish: () -> Unit
) {
    val cards by viewModel.studyCards.collectAsState()
    val currentIndex by viewModel.studyIndex.collectAsState()
    var isFlipped by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    // KHỞI TẠO BẢNG NHÁP CHO MẶT TRƯỚC
    val scratchPath = remember { Path() }
    var scratchTrigger by remember { mutableIntStateOf(0) }

    // Tự động xóa bảng nháp khi chuyển sang thẻ mới
    LaunchedEffect(currentIndex) {
        scratchPath.reset()
        scratchTrigger++
    }

    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) { }
        }
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    // HÀM XỬ LÝ ÂM THANH THÔNG MINH
    fun handleSpeech(text: String, soundUrl: String?, isTtsReady: Boolean) {
        if (!soundUrl.isNullOrEmpty()) {
            try {
                MediaPlayer().apply {
                    setDataSource(soundUrl)
                    prepareAsync()
                    setOnPreparedListener { start() }
                    setOnCompletionListener { release() }
                }
            } catch (e: Exception) {
                if (isTtsReady) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        } else {
            if (isTtsReady) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "CardRotation"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Đang ôn tập", fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = onFinish) { Icon(Icons.Default.Close, "Đóng") } }
            )
        }
    ) { padding ->
        if (cards.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 60.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Bạn đã hoàn thành bộ bài này!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Button(onClick = onFinish, modifier = Modifier.padding(top = 24.dp)) { Text("Quay về trang chủ") }
                }
            }
        } else {
            val currentCard = cards[currentIndex]

            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / cards.size },
                    modifier = Modifier.fillMaxWidth().height(8.dp).padding(bottom = 32.dp),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp) // Tăng chiều cao lên chút để chứa bảng nháp
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        // Vẫn cho phép bấm lật thẻ, nhưng sẽ không ảnh hưởng đến vùng vẽ nháp
                        .clickable { isFlipped = !isFlipped },
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFlipped) MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            if (rotation <= 90f) {
                                // ================== MẶT TRƯỚC ==================
                                IconButton(
                                    // SỬ DỤNG HÀM ÂM THANH THÔNG MINH
                                    onClick = { handleSpeech(currentCard.front, currentCard.sound, tts != null) },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(Icons.Filled.VolumeUp, contentDescription = "Nghe", tint = MaterialTheme.colorScheme.primary)
                                }

                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = currentCard.front,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 24.dp)
                                    )

                                    // BẢNG NHÁP (SCRATCHPAD) DÀNH CHO CHỮ TƯỢNG HÌNH
                                    Text("✍️ Bảng nháp viết chữ:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp) // Khu vực nháp
                                            .padding(top = 4.dp)
                                            .border(2.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                                    ) {
                                        Canvas(
                                            modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                                                detectDragGestures(
                                                    onDragStart = { offset ->
                                                        scratchPath.moveTo(offset.x, offset.y)
                                                        scratchTrigger++
                                                    },
                                                    onDrag = { change, _ ->
                                                        scratchPath.lineTo(change.position.x, change.position.y)
                                                        scratchTrigger++
                                                    }
                                                )
                                            }
                                        ) {
                                            val trigger = scratchTrigger
                                            drawPath(path = scratchPath, color = Color.Blue, style = Stroke(width = 8f))
                                        }
                                    }
                                }
                            } else {
                                // ================== MẶT SAU ==================
                                Box(modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                                    IconButton(
                                        // MẶT SAU: Ưu tiên file âm thanh nếu có, không thì đọc phần backText
                                        onClick = { handleSpeech(currentCard.back, currentCard.sound, tts != null) },
                                        modifier = Modifier.align(Alignment.TopStart)
                                    ) {
                                        Icon(Icons.Filled.VolumeUp, contentDescription = "Nghe", tint = MaterialTheme.colorScheme.primary)
                                    }

                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = currentCard.back,
                                            fontSize = 32.sp, // Chữ to rõ để so nét
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )

                                        if (!currentCard.imageUrl.isNullOrEmpty()) {
                                            Spacer(Modifier.height(16.dp))
                                            AsyncImage(
                                                model = currentCard.imageUrl,
                                                contentDescription = "Hình ảnh minh họa",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(160.dp)
                                                    .clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isFlipped) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FSRSButton("Lặp lại", Color(0xFFE57373), Modifier.weight(1f)) {
                            viewModel.submitReview(token, currentCard._id, 0)
                            isFlipped = false
                        }
                        FSRSButton("Khó", Color(0xFFFFB74D), Modifier.weight(1f)) {
                            viewModel.submitReview(token, currentCard._id, 1)
                            isFlipped = false
                        }
                        FSRSButton("Tốt", Color(0xFF81C784), Modifier.weight(1f)) {
                            viewModel.submitReview(token, currentCard._id, 2)
                            isFlipped = false
                        }
                        FSRSButton("Dễ", Color(0xFF64B5F6), Modifier.weight(1f)) {
                            viewModel.submitReview(token, currentCard._id, 3)
                            isFlipped = false
                        }
                    }
                } else {
                    Text(
                        "Chạm vào ngoài viền bảng nháp để lật thẻ",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun FSRSButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
    }
}