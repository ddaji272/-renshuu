package com.example.n

import android.content.Context // THÊM IMPORT NÀY ĐỂ DÙNG SHAREDPREFERENCES
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
import com.example.n.ui.screens.DeckDetailScreen
import com.example.n.ui.screens.HomeScreen
import com.example.n.ui.screens.StudyScreen
import com.example.n.ui.screens.OnboardingScreen // THÊM IMPORT MÀN HÌNH ONBOARDING
import com.example.n.utils.TokenManager
import com.example.n.viewmodel.AuthViewModel
import com.example.n.viewmodel.FlashcardViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // =======================================================
        // BƯỚC 1: ĐỌC BỘ NHỚ LOCAL XEM USER ĐÃ XEM GIỚI THIỆU CHƯA
        // =======================================================
        val sharedPref = getSharedPreferences("RenshuuPrefs", Context.MODE_PRIVATE)
        val initialHasSeenOnboarding = sharedPref.getBoolean("hasSeenOnboarding", false)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Biến quản lý việc bật/tắt Onboarding
                    var showOnboarding by remember { mutableStateOf(!initialHasSeenOnboarding) }

                    val context = LocalContext.current
                    val tokenManager = remember { TokenManager(context) }

                    // Khởi tạo 2 bộ não cho app
                    val authViewModel: AuthViewModel = viewModel()
                    val flashcardViewModel: FlashcardViewModel = viewModel()

                    // Kiểm tra xem trong két có Token không
                    var currentToken by remember { mutableStateOf(tokenManager.getToken()) }

                    // Lắng nghe trạng thái đăng nhập
                    val loginSuccess by authViewModel.loginSuccess.collectAsState()
                    val newToken by authViewModel.accessToken.collectAsState()

                    // Biến theo dõi trạng thái điều hướng
                    var selectedDeck by remember { mutableStateOf<com.example.n.network.DeckResponse?>(null) }
                    var isStudying by remember { mutableStateOf(false) }

                    // Khi có chìa khóa mới -> Cất ngay vào két
                    LaunchedEffect(newToken) {
                        if (newToken != null) {
                            tokenManager.saveToken(newToken!!)
                            currentToken = newToken
                        }
                    }

                    // =======================================================
                    // BỘ ĐỊNH TUYẾN (ROUTER) CẤP CAO NHẤT
                    // =======================================================
                    if (showOnboarding) {
                        // NẾU CHƯA XEM GIỚI THIỆU -> KHÓA MỌI THỨ, BẮT XEM ONBOARDING
                        OnboardingScreen(
                            onFinish = {
                                // Lưu cờ 'true' vào bộ nhớ: Đã xem rồi, lần sau đừng hiện nữa!
                                sharedPref.edit().putBoolean("hasSeenOnboarding", true).apply()
                                // Tắt Onboarding, tự động thả user rơi xuống logic Login bên dưới
                                showOnboarding = false
                            }
                        )
                    } else {
                        // NẾU ĐÃ XEM GIỚI THIỆU (Hoặc tải app từ lâu) -> CHẠY ROUTER CŨ CỦA BẠN
                        if (currentToken == null && !loginSuccess) {
                            // CHƯA ĐĂNG NHẬP -> Hiện màn hình Đăng Nhập
                            AuthScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = { /* Tự động xử lý */ }
                            )
                        } else {
                            // ĐÃ ĐĂNG NHẬP -> Phân luồng giao thông
                            if (isStudying && selectedDeck != null) {
                                // 1. NẾU BẬT CÔNG TẮC HỌC -> Vào phòng tập (StudyScreen)
                                StudyScreen(
                                    token = currentToken ?: "",
                                    viewModel = flashcardViewModel,
                                    onFinish = {
                                        isStudying = false // Tắt công tắc học
                                        selectedDeck = null // Trả về màn hình chính
                                    }
                                )
                            } else if (selectedDeck != null) {
                                // 2. NẾU CHỈ BẤM VÀO THÂN BỘ BÀI -> Vào phòng quản lý thẻ (DeckDetailScreen)
                                DeckDetailScreen(
                                    token = currentToken ?: "",
                                    deck = selectedDeck!!,
                                    viewModel = flashcardViewModel,
                                    onBack = { selectedDeck = null } // Bấm mũi tên Back thì về Home
                                )
                            } else {
                                // 3. NẾU CHƯA CHỌN GÌ -> Đứng ở sảnh chính (HomeScreen)
                                HomeScreen(
                                    token = currentToken ?: "",
                                    flashcardViewModel = flashcardViewModel,
                                    onLogout = {
                                        authViewModel.logout()
                                        tokenManager.clearToken()
                                        currentToken = null
                                        selectedDeck = null
                                        isStudying = false
                                    },
                                    onDeckClick = { deck ->
                                        selectedDeck = deck // Bấm vào thân bộ bài -> Mở quản lý thẻ
                                    },
                                    onStudyClick = { deck ->
                                        selectedDeck = deck
                                        isStudying = true // Bấm nút Play -> Bật công tắc học
                                        flashcardViewModel.fetchStudyCards(currentToken ?: "", deck._id) // Ra lệnh tải bài về học
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}