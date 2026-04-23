package com.example.n.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.n.network.ForgotPasswordRequest
import com.example.n.network.LoginRequest
import com.example.n.network.RegisterRequest
import com.example.n.network.RetrofitClient
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException

class AuthViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Biến chứa thông báo thành công (ví dụ: gửi email quên pass thành công)
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    // ================================================================
    // THÊM MỚI QUAN TRỌNG: Biến lưu Token để giao cho MainActivity cất vào Két
    // ================================================================
    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() && pass.isBlank()) {
            _errorMessage.value = "Vui lòng nhập tài khoản và mật khẩu!"
            return
        }
        if (email.isBlank()) {
            _errorMessage.value = "Vui lòng nhập email/tài khoản!"
            return
        }
        if (pass.isBlank()) {
            _errorMessage.value = "Vui lòng nhập mật khẩu!"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            try {
                val request = LoginRequest(email, pass)
                val response = RetrofitClient.apiService.loginUser(request)

                // Gán Token lấy được từ Server vào biến
                _accessToken.value = response.accessToken

                println("Đăng nhập thành công! Token: ${response.accessToken}")
                _loginSuccess.value = true
            } catch (e: HttpException) {
                when (e.code()) {
                    404 -> _errorMessage.value = "Tài khoản không tồn tại!"
                    401 -> _errorMessage.value = "Mật khẩu không đúng!"
                    400 -> _errorMessage.value = "Thông tin không hợp lệ!"
                    else -> _errorMessage.value = "Lỗi server: ${e.code()}"
                }
            } catch (e: SocketTimeoutException) {
                _errorMessage.value = "Server đang khởi động, vui lòng thử lại sau vài giây!"
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi: Kiểm tra lại kết nối mạng!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Vui lòng điền đầy đủ email và mật khẩu!"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            try {
                val request = RegisterRequest(email, pass)
                val response = RetrofitClient.apiService.registerUser(request)

                // Báo thành công và KHÔNG tự động đăng nhập nữa
                _successMessage.value = "🎉 Đăng ký thành công! Vui lòng đăng nhập để tiếp tục."

            } catch (e: HttpException) {
                when (e.code()) {
                    409, 400 -> _errorMessage.value = "Tài khoản này đã tồn tại!"
                    else -> _errorMessage.value = "Lỗi server khi đăng ký: ${e.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi kết nối mạng hoặc server!"
            } finally {
                // Tắt vòng xoay loading sau khi hoàn tất
                _isLoading.value = false
            }
        }
    }

    // Tính năng Quên mật khẩu
    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _errorMessage.value = "Vui lòng nhập email của bạn vào ô trên để reset mật khẩu!"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {
            try {
                val request = ForgotPasswordRequest(email)
                val response = RetrofitClient.apiService.forgotPassword(request)
                _successMessage.value = "Đã gửi link khôi phục. Vui lòng kiểm tra Email!"
            } catch (e: HttpException) {
                _errorMessage.value = "Không tìm thấy tài khoản với email này."
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi mạng khi gửi yêu cầu quên mật khẩu."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Tính năng Đăng xuất
    fun logout() {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.logoutUser()
            } catch (e: Exception) {
                println("Lỗi logout server, tiến hành logout local")
            } finally {
                _accessToken.value = null // Xóa Token nội bộ
                _loginSuccess.value = false // Đẩy user ra khỏi màn hình chính
            }
        }
    }

    fun loginWithGoogle(context: Context) {
        _isLoading.value = true
        _errorMessage.value = null

        val credentialManager = CredentialManager.create(context)
        val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    _accessToken.value = idToken // Cất Token của Google
                    println("Lấy Token Google thành công: $idToken")
                    _loginSuccess.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = "Đăng nhập Google bị hủy hoặc thất bại."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _errorMessage.value = null
        _successMessage.value = null
        _loginSuccess.value = false
    }
}