package com.example.n.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit // Hàm gọi khi bấm nút Đăng xuất
) {
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
                    // Nút Đăng xuất ở góc phải trên cùng
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
            // Nút dấu + nổi bật để tạo Flashcard mới
            FloatingActionButton(
                onClick = { /* TODO: Chuyển sang màn hình tạo Deck */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tạo bộ bài mới", tint = Color.White)
            }
        }
    ) { paddingValues ->
        // Nội dung chính của Trang chủ
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
                // Một cái Card hiển thị tiến độ học tập (Mockup tạm)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
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
                        Text("📚 Bộ bài của bạn: 0", fontSize = 16.sp)
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

            // Giao diện giữ chỗ (Placeholder) cho các bộ bài sau này
            items(3) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🗂️", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Bộ bài mẫu ${index + 1}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("0 từ vựng", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}