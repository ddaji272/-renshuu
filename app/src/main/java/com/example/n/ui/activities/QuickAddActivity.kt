package com.example.n.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
class QuickAddActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. CHỘP LẤY ĐOẠN TEXT NGƯỜI DÙNG VỪA BÔI ĐEN
        val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: ""

        setContent {
            MaterialTheme {
                val sheetState = rememberModalBottomSheetState()
                var showBottomSheet by remember { mutableStateOf(true) }

                // Biến lưu trữ dữ liệu tạo thẻ
                var frontText by remember { mutableStateOf(selectedText) } // Gán luôn text bôi đen vào mặt trước
                var backText by remember { mutableStateOf("") }

                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                            finish() // Đóng Activity khi vuốt bảng xuống
                        },
                        sheetState = sheetState,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✨ Thêm nhanh vào Renshuu",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Ô điền mặt trước (Đã có sẵn chữ bôi đen)
                            OutlinedTextField(
                                value = frontText,
                                onValueChange = { frontText = it },
                                label = { Text("Câu hỏi / Từ vựng (Mặt trước)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Ô điền mặt sau
                            OutlinedTextField(
                                value = backText,
                                onValueChange = { backText = it },
                                label = { Text("Đáp án (Mặt sau)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { finish() },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Hủy", color = Color.Gray)
                                }

                                Button(
                                    onClick = {
                                        // TODO: Gọi ViewModel để đẩy thẻ này lên Server
                                        // Tạm thời đóng Bottom Sheet sau khi bấm
                                        showBottomSheet = false
                                        finish()
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    enabled = frontText.isNotBlank() && backText.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Lưu thẻ")
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp)) // Đệm dưới cùng cho đỡ sát viền
                        }
                    }
                }
            }
        }
    }
}