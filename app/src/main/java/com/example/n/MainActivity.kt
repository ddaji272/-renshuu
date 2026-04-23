package com.example.n

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.n.ui.screens.AuthScreen
import com.example.n.ui.screens.DeckDetailScreen // THÊM: Import màn hình Thẻ
import com.example.n.ui.screens.HomeScreen
import com.example.n.utils.TokenManager
import com.example.n.viewmodel.AuthViewModel
import com.example.n.viewmodel.FlashcardViewModel // THÊM: Import Bộ não Flashcard

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val tokenManager = remember { TokenManager(context) }

                    // Khởi tạo 2 bộ não cho app
                    val authViewModel: AuthViewModel = viewModel()
                    val flashcardViewModel: FlashcardViewModel = viewModel()

                    // 1. Kiểm tra xem trong két có Token không
                    var currentToken by remember { mutableStateOf(tokenManager.getToken()) }

                    // Lắng nghe trạng thái đăng nhập
                    val loginSuccess by authViewModel.loginSuccess.collectAsState()
                    val newToken by authViewModel.accessToken.collectAsState()

                    // =======================================================
                    // THÊM: Biến theo dõi xem user đang bấm vào Bộ bài nào
                    // =======================================================
                    var selectedDeck by remember { mutableStateOf<com.example.n.network.DeckResponse?>(null) }

                    // 2. Khi có chìa khóa mới -> Cất ngay vào két
                    LaunchedEffect(newToken) {
                        if (newToken != null) {
                            tokenManager.saveToken(newToken!!)
                            currentToken = newToken
                        }
                    }

                    // =======================================================
                    // BỘ ĐỊNH TUYẾN (ROUTER) ĐÃ ĐƯỢC NÂNG CẤP
                    // =======================================================
                    if (currentToken == null && !loginSuccess) {
                        // CHƯA ĐĂNG NHẬP -> Hiện màn hình Đăng Nhập
                        AuthScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = { /* Tự động xử lý */ }
                        )
                    } else {
                        // ĐÃ ĐĂNG NHẬP -> Kiểm tra xem user muốn đi đâu
                        if (selectedDeck != null) {
                            // NẾU ĐANG CHỌN BỘ BÀI -> Hiện màn hình Chi tiết Thẻ
                            DeckDetailScreen(
                                token = currentToken ?: "",
                                deck = selectedDeck!!,
                                viewModel = flashcardViewModel,
                                onBack = { selectedDeck = null } // Bấm mũi tên Back thì set về null để về Home
                            )
                        } else {
                            // NẾU CHƯA CHỌN BỘ BÀI -> Hiện Trang Chủ
                            HomeScreen(
                                token = currentToken ?: "",
                                flashcardViewModel = flashcardViewModel,
                                onLogout = {
                                    authViewModel.logout()
                                    tokenManager.clearToken()
                                    currentToken = null
                                    selectedDeck = null // Reset trạng thái chọn bài
                                },
                                onDeckClick = { deck ->
                                    selectedDeck = deck // Bấm vào bộ bài nào thì gán vào đây để mở màn hình Thẻ
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}