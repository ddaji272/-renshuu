package com.example.n.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close // <-- Đã import icon nút X
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.n.viewmodel.FlashcardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    token: String,
    flashcardViewModel: FlashcardViewModel = viewModel(),
    onLogout: () -> Unit,
    onDeckClick: (com.example.n.network.DeckResponse) -> Unit,
    onStudyClick: (com.example.n.network.DeckResponse) -> Unit,
    onChatbotClick: () -> Unit
) {
    val decks by flashcardViewModel.decks.collectAsState()
    val isLoading by flashcardViewModel.isLoading.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var deckName by remember { mutableStateOf("") }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("renshuu_prefs", Context.MODE_PRIVATE)
    var showAIGuide by remember { mutableStateOf(prefs.getBoolean("show_ai_guide", true)) }

    // ==========================================
    // BIẾN CHO THẺ "MẸO HAY MỖI NGÀY"
    // ==========================================
    val tips = listOf(
        "Bí kíp: Chạm vào chữ 'Renshuu' màu tím ở góc trái trên cùng để gọi Trợ lý AI giải đáp thắc mắc nhé! 🤖",
        "Mẹo nhanh: Khi đọc báo gặp từ khó, hãy bôi đen và chọn 'Thêm vào Renshuu' để tạo thẻ siêu tốc! ⚡",
        "Bạn có biết? Thuật toán FSRS của Renshuu sẽ tính toán chính xác ngày bạn sắp quên từ vựng để nhắc nhở. 🧠",
        "Thói quen tốt: Dành ra 5 phút lướt qua 'Hộp Nháp' mỗi ngày để chuyển từ vựng vào trí nhớ dài hạn nhé! 📦"
    )
    val currentTip = remember { tips.random() } // Random 1 câu mỗi lần mở app
    var showTipCard by remember { mutableStateOf(true) } // Công tắc bật/tắt thẻ
    // ==========================================

    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            flashcardViewModel.fetchDecks(token)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Renshuu",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onChatbotClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Đăng xuất",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tạo bộ bài mới", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            item {
                Text(
                    text = "Chào mừng bạn trở lại! 👋",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 20.dp, top = 8.dp)
                )
            }

            item {
                // THẺ TỔNG QUAN
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Tổng quan",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "📚 Đang có: ${decks.size} bộ bài",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text("✨", fontSize = 44.sp)
                    }
                }
            }

            // ==========================================
            // THẺ MẸO HAY MỖI NGÀY (IN-APP MESSAGE)
            // ==========================================
            if (showTipCard) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡", fontSize = 28.sp)

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = currentTip,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f),
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Medium
                            )

                            IconButton(
                                onClick = { showTipCard = false }, // Bấm X là giấu thẻ đi
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Đóng",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
            // ==========================================

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Bộ bài của bạn",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (isLoading && decks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (decks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📭", fontSize = 60.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Bạn chưa có bộ bài nào",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Hãy bấm nút + để tạo tuyệt tác đầu tiên nhé!",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                items(decks) { deck ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onDeckClick(deck) },
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🗂️", fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(deck.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Chạm để chỉnh sửa", color = Color.Gray, fontSize = 13.sp)
                            }

                            IconButton(
                                onClick = { onStudyClick(deck) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFFE8F5E9), CircleShape)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Học ngay", tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { flashcardViewModel.deleteDeck(token, deck._id) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFFFFEBEE), CircleShape)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Xóa", tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // DIALOG TẠO BỘ BÀI MỚI
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Tạo bộ bài mới", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
            text = {
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text("Tên bộ bài (VD: N5 Kanji)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deckName.isNotBlank()) {
                            flashcardViewModel.createDeck(token, deckName)
                            showDialog = false
                            deckName = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tạo ngay", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Hủy", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // DIALOG HƯỚNG DẪN TÍNH NĂNG AI CHATBOT LẦN ĐẦU
    if (showAIGuide) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { /* Để trống */ },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 44.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Khám phá tính năng ẩn!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Trợ lý AI Renshuu đã sẵn sàng! Bất cứ khi nào bạn có thắc mắc, hãy chạm vào chữ 'Renshuu' màu tím ở góc trái trên cùng để gọi AI nhé.",
                        fontSize = 15.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            showAIGuide = false
                            prefs.edit().putBoolean("show_ai_guide", false).apply()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Tuyệt vời, tôi đã hiểu!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}