package com.example.n.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n.viewmodel.FlashcardViewModel

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

    // Hiệu ứng lật thẻ mượt mà
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "CardRotation"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Đang ôn tập", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onFinish) { Icon(Icons.Default.Close, "Đóng") }
                }
            )
        }
    ) { padding ->
        if (cards.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", fontSize = 60.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Bạn đã hoàn thành bộ bài này!", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Hẹn gặp lại vào lần tới nhé!", color = Color.Gray)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onFinish) { Text("Quay về trang chủ") }
                }
            }
        } else {
            val currentCard = cards[currentIndex]

            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Thanh tiến trình
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / cards.size },
                    modifier = Modifier.fillMaxWidth().height(8.dp).padding(bottom = 32.dp),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // Khối thẻ lật 3D
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
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
                        Box(
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rotation <= 90f) {
                                // MẶT TRƯỚC (Câu hỏi)
                                Text(
                                    text = currentCard.front,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 40.sp
                                )
                            } else {
                                // MẶT SAU (Đáp án)
                                Column(
                                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = currentCard.back,
                                        fontSize = 28.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 36.sp
                                    )

                                    // Kiểm tra xem Backend có trả về ảnh không
                                    if (!currentCard.imageUrl.isNullOrEmpty()) {
                                        Spacer(Modifier.height(16.dp))
                                        Text("🖼️ (Thẻ có hình ảnh)", fontSize = 14.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Dòng nút bấm FSRS (Khớp điểm số với Backend)
                if (isFlipped) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // SỬA: Gửi đúng thang điểm 0, 1, 2, 3 cho Backend
                        FSRSButton("Lặp lại", Color(0xFFE57373), Modifier.weight(1f)) {
                            viewModel.submitReview(token, currentCard._id, 0) // Điểm 0
                            isFlipped = false
                        }
                        FSRSButton("Khó", Color(0xFFFFB74D), Modifier.weight(1f)) {
                            viewModel.submitReview(token, currentCard._id, 1) // Điểm 1
                            isFlipped = false
                        }
                        FSRSButton("Tốt", Color(0xFF81C784), Modifier.weight(1f)) {
                            viewModel.submitReview(token, currentCard._id, 2) // Điểm 2
                            isFlipped = false
                        }
                        FSRSButton("Dễ", Color(0xFF64B5F6), Modifier.weight(1f)) {
                            viewModel.submitReview(token, currentCard._id, 3) // Điểm 3
                            isFlipped = false
                        }
                    }
                } else {
                    Text(
                        "Chạm vào thẻ để xem đáp án",
                        color = Color.Gray,
                        fontSize = 16.sp,
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