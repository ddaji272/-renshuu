package com.example.n.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.n.network.ForgotPasswordRequest
import com.example.n.network.LoginRequest
import com.example.n.network.RegisterRequest
import com.example.n.network.RetrofitClient
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

    // 1. THÊM HÀM NÀY ĐỂ GIAO DIỆN CÓ THỂ CHỦ ĐỘNG BÁO LỖI
    fun setError(message: String) {
        _errorMessage.value = message
    }

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

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

                _successMessage.value = "🎉 Đăng ký thành công! Vui lòng đăng nhập để tiếp tục."

            } catch (e: HttpException) {
                when (e.code()) {
                    409, 400 -> _errorMessage.value = "Tài khoản này đã tồn tại!"
                    else -> _errorMessage.value = "Lỗi server khi đăng ký: ${e.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi kết nối mạng hoặc server!"
            } finally {
                _isLoading.value = false
            }
        }
    }

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

    fun logout() {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.logoutUser()
            } catch (e: Exception) {
                println("Lỗi logout server, tiến hành logout local")
            } finally {
                _accessToken.value = null
                _loginSuccess.value = false
            }
        }
    }

    // 2. SỬA LẠI HÀM NÀY ĐỂ NHẬN CHUỖI ID TOKEN (THAY VÌ CONTEXT NHƯ BẢN CŨ)
    fun loginWithGoogle(idToken: String) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Nếu sau này bạn làm API ở Node.js để xác thực tài khoản Google, hãy thay thế bằng đoạn này:
                // val response = RetrofitClient.apiService.verifyGoogleToken(idToken)
                // _accessToken.value = response.accessToken

                _accessToken.value = idToken
                println("Lấy Token Google thành công: $idToken")
                _loginSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Đăng nhập Google thất bại trên Server."
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