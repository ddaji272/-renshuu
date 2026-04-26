package com.example.n.ui.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    onStudyClick: (com.example.n.network.DeckResponse) -> Unit
) {
    val decks by flashcardViewModel.decks.collectAsState()
    val isLoading by flashcardViewModel.isLoading.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var deckName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            flashcardViewModel.fetchDecks(token)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Renshuu", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
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
                .padding(horizontal = 20.dp, vertical = 8.dp) // Căn lề thoáng hơn
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
                // THẺ TỔNG QUAN (Đã làm lại xịn xò)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Dùng màu chìm thay vì bóng
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
                    // UI TỪNG BỘ BÀI
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
                            // Icon thư mục có nền mờ
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

                            // NÚT HỌC NGAY (Có nền mờ xanh lá)
                            IconButton(
                                onClick = { onStudyClick(deck) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFFE8F5E9), CircleShape)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Học ngay", tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // NÚT XÓA (Có nền mờ đỏ)
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
                    Spacer(modifier = Modifier.height(80.dp)) // Tránh bị nút FAB che mất item cuối
                }
            }
        }
    }

    // DIALOG TẠO BỘ BÀI MỚI (Làm mượt hơn)
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
}