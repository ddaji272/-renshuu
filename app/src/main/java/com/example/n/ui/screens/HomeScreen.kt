package com.example.n.ui.screens

import androidx.compose.foundation.clickable // THÊM IMPORT ĐỂ BẤM ĐƯỢC
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    // THÊM: Lệnh chuyển trang khi bấm vào bộ bài
    onDeckClick: (com.example.n.network.DeckResponse) -> Unit
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
                    Text("Renshuu", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
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
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tạo bộ bài mới", tint = Color.White)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Chào mừng bạn trở lại! 👋",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Tiến độ hôm nay", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🔥 Đã ôn tập: 0/20 từ vựng", fontSize = 16.sp)
                        Text("📚 Bộ bài của bạn: ${decks.size}", fontSize = 16.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Bộ bài gần đây",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (isLoading && decks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                }
            } else if (decks.isEmpty()) {
                item {
                    Text(
                        "Bạn chưa có bộ bài nào. Hãy bấm nút + để tạo nhé!",
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(decks) { deck ->
                    Card(
                        // SỬA: Thêm modifier .clickable để bấm được vào từng bộ bài
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onDeckClick(deck) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                                Text("🗂️", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(deck.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                // Sửa lại dòng text cho đúng trải nghiệm
                                Text("Click để xem và thêm thẻ", color = Color.Gray, fontSize = 12.sp)
                            }

                            IconButton(
                                onClick = { flashcardViewModel.deleteDeck(token, deck._id) }
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Xóa", tint = Color.Red)
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
            title = { Text("Tạo bộ bài mới", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = deckName,
                    onValueChange = { deckName = it },
                    label = { Text("Tên bộ bài (VD: N5 Kanji)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
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
                    }
                ) {
                    Text("Tạo mới")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Hủy", color = Color.Gray)
                }
            }
        )
    }
}