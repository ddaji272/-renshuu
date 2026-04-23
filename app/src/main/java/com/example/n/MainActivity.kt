package com.example.n

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.n.ui.screens.AuthScreen
import com.example.n.ui.screens.HomeScreen
import com.example.n.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Dùng chung 1 ViewModel cho cả MainActivity để quản lý trạng thái
                    val authViewModel: AuthViewModel = viewModel()

                    // Lắng nghe biến loginSuccess từ ViewModel
                    val isLoggedIn by authViewModel.loginSuccess.collectAsState()

                    // Bộ định tuyến (Router)
                    if (!isLoggedIn) {
                        // Nếu chưa đăng nhập -> Hiện màn hình Đăng Nhập
                        AuthScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                // Không cần gán biến gì ở đây vì ViewModel đã tự set loginSuccess = true rồi
                            }
                        )
                    } else {
                        // Nếu đã đăng nhập -> Hiện Trang Chủ
                        HomeScreen(
                            onLogout = {
                                // Gọi hàm logout bên ViewModel để đẩy user ra ngoài
                                authViewModel.logout()
                            }
                        )
                    }
                }
            }
        }
    }
}