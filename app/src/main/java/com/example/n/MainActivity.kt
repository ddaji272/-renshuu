package com.example.n

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.n.ui.screens.AuthScreen
import com.example.n.ui.screens.FlashcardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Biến trạng thái: Mặc định lúc mới mở app là false (chưa đăng nhập)
                    var isLoggedIn by remember { mutableStateOf(false) }

                    // Bộ định tuyến (Router) đơn giản
                    if (!isLoggedIn) {
                        // Nếu chưa đăng nhập -> Hiện màn hình Đăng Nhập
                        AuthScreen(
                            onLoginSuccess = {
                                // Khi hàm login() trong ViewModel chạy thành công,
                                // nó sẽ báo ra đây và ta đổi biến này thành true
                                isLoggedIn = true
                            }
                        )
                    } else {
                        // Nếu isLoggedIn == true -> Hiện màn hình Flashcard
                        FlashcardScreen()
                    }
                }
            }
        }
    }
}