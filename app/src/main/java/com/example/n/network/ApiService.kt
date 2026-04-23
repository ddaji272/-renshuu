package com.example.n.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Header
import java.util.concurrent.TimeUnit

// ==========================================
// 1. CÁC LỚP DỮ LIỆU (DATA CLASSES)
// ==========================================

// THÊM: Lớp chứa thông tin User (Vì Backend có trả về cái này khi Login)
data class UserInfo(val id: String, val email: String)

// Đăng nhập
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(
    val accessToken: String, // ĐÃ SỬA: Đổi từ 'token' thành 'accessToken' cho khớp BE
    val message: String,
    val user: UserInfo?      // THÊM: Hứng thêm thông tin user để app biết ai đang đăng nhập
)

// Đăng ký
data class RegisterRequest(val email: String, val password: String)
data class RegisterResponse(val message: String)

// THÊM: Quên mật khẩu
data class ForgotPasswordRequest(val email: String)
data class ForgotPasswordResponse(val message: String)

// THÊM: Đăng xuất (Dùng chung Response vì nó chỉ trả về mỗi chữ message)
data class GeneralResponse(val message: String)


// ==========================================
// 2. KHAI BÁO CÁC ĐƯỜNG DẪN API (ROUTES)
// ==========================================
interface ApiService {

    @POST("/api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): LoginResponse

    @POST("/api/auth/signup") // ĐÃ SỬA: Đổi từ '/register' thành '/signup'
    suspend fun registerUser(@Body request: RegisterRequest): RegisterResponse

    // --- CÁC TÍNH NĂNG MỚI ĐỂ "CÂN" VỚI BACKEND ---

    @POST("/api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("/api/auth/logout")
    suspend fun logoutUser(): GeneralResponse
}


// ==========================================
// 3. KHỞI TẠO CẤU HÌNH MẠNG (RETROFIT)
// ==========================================
object RetrofitClient {
    private const val BASE_URL = "https://renshuu-backend.onrender.com/"

    // Giữ nguyên cấu hình kiên nhẫn đợi Render khởi động (60 giây)
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}