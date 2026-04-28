package com.example.n.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.n.utils.TokenManager
import com.example.n.viewmodel.FlashcardViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
class QuickAddActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. CHỘP LẤY TEXT TỪ APP KHÁC (Bôi đen)
        val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()?.trim() ?: ""

        // Nếu người dùng lỡ bôi đen khoảng trắng thì tắt luôn
        if (selectedText.isEmpty()) {
            Toast.makeText(this, "Không có văn bản nào được chọn", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            MaterialTheme {
                val context = LocalContext.current
                val tokenManager = remember { TokenManager(context) }
                val token = tokenManager.getToken()

                // 2. KHỞI TẠO BỘ NÃO (VIEWMODEL)
                val viewModel: FlashcardViewModel = viewModel()

                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                var showBottomSheet by remember { mutableStateOf(true) }

                // Quản lý trạng thái UI: 0 = Đang chạy AI, 1 = Thành công, 2 = Lỗi
                var processState by remember { mutableIntStateOf(0) }

                // 3. LUỒNG XỬ LÝ NGẦM GỌI AI VÀ LƯU HỘP NHÁP
                LaunchedEffect(selectedText) {
                    if (token != null) {
                        try {
                            // Đợi một chút cho hiệu ứng bảng trượt lên mượt mà
                            delay(500)

                            // GỌI HÀM XỊN: Vừa gọi AI giải nghĩa, vừa lưu vào Deck "Hộp Nháp"
                            val isSuccess = viewModel.createCardWithAI(token, selectedText)

                            if (isSuccess) {
                                processState = 1 // Hiện dấu tích xanh
                                delay(1200)      // Để user kịp nhìn thấy thành quả
                            } else {
                                processState = 2 // Hiện cảnh báo lỗi
                                delay(2000)
                            }
                        } catch (e: Exception) {
                            processState = 2
                            delay(2000)
                        } finally {
                            showBottomSheet = false
                            finish() // Xong việc thì đóng Activity ngay
                        }
                    } else {
                        Toast.makeText(context, "Vui lòng đăng nhập trước!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                // 4. GIAO DIỆN BẢNG TRƯỢT (BOTTOM SHEET) THÔNG BÁO TRẠNG THÁI
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            showBottomSheet = false
                            finish()
                        },
                        sheetState = sheetState,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        AnimatedContent(targetState = processState, label = "AI_Process") { state ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                when (state) {
                                    0 -> { // ĐANG XỬ LÝ
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(48.dp),
                                            strokeWidth = 4.dp
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Text(
                                            text = "Đang lưu \"$selectedText\"...",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "AI đang tự động phân tích ngôn ngữ & tạo ví dụ ⚡",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(top = 8.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    1 -> { // THÀNH CÔNG
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            contentDescription = "Thành công",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Đã cất vào Hộp Nháp!",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                    2 -> { // LỖI
                                        Icon(
                                            Icons.Filled.Warning,
                                            contentDescription = "Lỗi",
                                            tint = Color(0xFFE53935),
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Lỗi kết nối AI hoặc Server!",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE53935)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}