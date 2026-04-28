package com.example.n

import android.content.Context
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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.n.ui.screens.AuthScreen
import com.example.n.ui.screens.DeckDetailScreen
import com.example.n.ui.screens.HomeScreen
import com.example.n.ui.screens.StudyScreen
import com.example.n.ui.screens.OnboardingScreen
import com.example.n.utils.TokenManager
import com.example.n.viewmodel.AuthViewModel
import com.example.n.viewmodel.FlashcardViewModel
import com.example.n.worker.ReminderWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // KÍCH HOẠT HỆ THỐNG THÔNG BÁO TEST (Chạy ngay lập tức)
        val testRequest = OneTimeWorkRequestBuilder<ReminderWorker>().build()
        WorkManager.getInstance(this).enqueue(testRequest)


        // BƯỚC 1: ĐỌC BỘ NHỚ LOCAL XEM USER ĐÃ XEM GIỚI THIỆU CHƯA
        val sharedPref = getSharedPreferences("RenshuuPrefs", Context.MODE_PRIVATE)
        val initialHasSeenOnboarding = sharedPref.getBoolean("hasSeenOnboarding", false)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showOnboarding by remember { mutableStateOf(!initialHasSeenOnboarding) }

                    val context = LocalContext.current
                    val tokenManager = remember { TokenManager(context) }

                    val authViewModel: AuthViewModel = viewModel()
                    val flashcardViewModel: FlashcardViewModel = viewModel()

                    var currentToken by remember { mutableStateOf(tokenManager.getToken()) }

                    val loginSuccess by authViewModel.loginSuccess.collectAsState()
                    val newToken by authViewModel.accessToken.collectAsState()

                    var selectedDeck by remember { mutableStateOf<com.example.n.network.DeckResponse?>(null) }
                    var isStudying by remember { mutableStateOf(false) }

                    LaunchedEffect(newToken) {
                        if (newToken != null) {
                            tokenManager.saveToken(newToken!!)
                            currentToken = newToken
                        }
                    }

                    // BỘ ĐỊNH TUYẾN (ROUTER) CẤP CAO NHẤT
                    if (showOnboarding) {
                        OnboardingScreen(
                            onFinish = {
                                sharedPref.edit().putBoolean("hasSeenOnboarding", true).apply()
                                showOnboarding = false
                            }
                        )
                    } else {
                        if (currentToken == null && !loginSuccess) {
                            AuthScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = { /* Tự động xử lý */ }
                            )
                        } else {
                            if (isStudying && selectedDeck != null) {
                                StudyScreen(
                                    token = currentToken ?: "",
                                    viewModel = flashcardViewModel,
                                    onFinish = {
                                        isStudying = false
                                        selectedDeck = null
                                    }
                                )
                            } else if (selectedDeck != null) {
                                DeckDetailScreen(
                                    token = currentToken ?: "",
                                    deck = selectedDeck!!,
                                    viewModel = flashcardViewModel,
                                    onBack = { selectedDeck = null }
                                )
                            } else {
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
                                        selectedDeck = deck
                                    },
                                    onStudyClick = { deck ->
                                        selectedDeck = deck
                                        isStudying = true
                                        flashcardViewModel.fetchStudyCards(currentToken ?: "", deck._id)
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