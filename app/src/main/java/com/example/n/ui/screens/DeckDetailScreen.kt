package com.example.n.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.n.network.DeckResponse
import com.example.n.viewmodel.FlashcardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    token: String,
    deck: DeckResponse, // Nhận thông tin bộ bài đang chọn
    viewModel: FlashcardViewModel,
    onBack: () -> Unit // Nút quay lại Trang chủ
) {
    val cards by viewModel.cards.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }

    // Tải thẻ khi vừa mở màn hình
    LaunchedEffect(deck._id) {
        viewModel.fetchCards(token, deck._id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deck.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
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
            item {
                Text("Số thẻ hiện tại: ${cards.size}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            }

            if (cards.isEmpty()) {
                item { Text("Chưa có thẻ nào. Bấm dấu + để thêm nhé!", color = Color.Gray) }
            } else {
                items(cards) { card ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Mặt trước: ${card.front}", fontWeight = FontWeight.Bold)
                                Text("Mặt sau: ${card.back}", color = Color.DarkGray)
                            }
                            IconButton(onClick = { viewModel.deleteCard(token, card._id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Xóa thẻ", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Thêm Thẻ
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Thêm thẻ mới") },
            text = {
                Column {
                    OutlinedTextField(value = frontText, onValueChange = { frontText = it }, label = { Text("Mặt trước (VD: Apple)") })
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = backText, onValueChange = { backText = it }, label = { Text("Mặt sau (VD: Quả táo)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (frontText.isNotBlank() && backText.isNotBlank()) {
                        viewModel.addCard(token, deck._id, frontText, backText) {
                            showDialog = false
                            frontText = ""
                            backText = ""
                        }
                    }
                }) { Text("Lưu") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Hủy") }
            }
        )
    }
}