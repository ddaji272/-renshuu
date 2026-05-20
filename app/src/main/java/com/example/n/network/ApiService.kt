package com.example.n.network

import com.example.n.BuildConfig
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// 1. CÁC LỚP DỮ LIỆU (DATA CLASSES)
// Auth
data class UserInfo(val id: String, val email: String)
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val accessToken: String, val message: String, val user: UserInfo?)
data class RegisterRequest(val email: String, val password: String)
data class RegisterResponse(val message: String)
data class ForgotPasswordRequest(val email: String)
data class ForgotPasswordResponse(val message: String)
data class GeneralResponse(val message: String)

// Deck & Card
data class DeckRequest(val name: String)

data class FsrsParams(
    val weights: List<Double> = emptyList(),
    val request_retention: Double = 0.9,
    val maximum_interval: Int = 36500
)

data class DeckResponse(
    val _id: String,
    val name: String,
    val algorithm: String = "SM2",
    val fsrs_params: FsrsParams? = null
)

data class UpdateDeckRequest(
    val name: String? = null,
    val algorithm: String? = null
)

data class OptimizeResponse(
    val message: String,
    val new_weights: List<Double>
)

data class RetentionRequest(
    val request_retention: Double
)

// CardRequest: Chỉ dùng cho AI Quick Add (không có file đính kèm)
data class CardRequest(
    @SerializedName("deckID") val deckId: String,
    val front: String,
    val back: String,
    val type: String = "TEXT",
    @SerializedName("image") val imageUrl: String? = null,
    val drawData: String? = null,
    val sound: String? = null
)

// CardResponse: Nhận dữ liệu thẻ từ Backend
data class CardResponse(
    val _id: String,
    val front: String,
    val back: String,
    val type: String? = "TEXT",
    @SerializedName("image") val imageUrl: String? = null,
    val drawData: String? = null,
    val sound: String? = null
)

data class ReviewRequest(val rating: Int)


// 2. KHAI BÁO CÁC ĐƯỜNG DẪN API (ROUTES)
interface ApiService {

    // AUTH ROUTES
    @POST("/api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): LoginResponse

    @POST("/api/auth/signup")
    suspend fun registerUser(@Body request: RegisterRequest): RegisterResponse

    @POST("/api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("/api/auth/logout")
    suspend fun logoutUser(): GeneralResponse

    // DECK ROUTES
    @POST("/api/deck")
    suspend fun createDeck(
        @Header("Authorization") token: String,
        @Body request: DeckRequest
    ): DeckResponse

    @GET("/api/deck")
    suspend fun getDecks(
        @Header("Authorization") token: String
    ): List<DeckResponse>

    @PUT("/api/deck/{id}")
    suspend fun updateDeck(
        @Header("Authorization") token: String,
        @Path("id") deckId: String,
        @Body request: UpdateDeckRequest
    ): DeckResponse

    @DELETE("/api/deck/{id}")
    suspend fun deleteDeck(
        @Header("Authorization") token: String,
        @Path("id") deckId: String
    ): GeneralResponse

    // CARD ROUTES
    // Dùng cho tạo thẻ CÓ đính kèm ảnh/âm thanh từ máy người dùng (Multipart)
    @Multipart
    @POST("/api/card")
    suspend fun createCard(
        @Header("Authorization") token: String,
        @Part("deckID") deckId: RequestBody,
        @Part("front") front: RequestBody,
        @Part("back") back: RequestBody,
        @Part("type") type: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part sound: MultipartBody.Part?
    ): CardResponse

    // Dùng cho tạo thẻ KHÔNG có file đính kèm (AI Quick Add)
    @POST("/api/card")
    suspend fun createCardJson(
        @Header("Authorization") token: String,
        @Body request: CardRequest
    ): CardResponse

    // Dùng cho cập nhật thẻ CÓ đính kèm ảnh/âm thanh từ máy người dùng (Multipart)
    @Multipart
    @PUT("/api/card/{cardId}")
    suspend fun updateCard(
        @Header("Authorization") token: String,
        @Path("cardId") cardId: String,
        @Part("deckID") deckId: RequestBody,
        @Part("front") front: RequestBody,
        @Part("back") back: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part sound: MultipartBody.Part?
    ): CardResponse

    @POST("/api/optimizer/optimize/{deckId}")
    suspend fun optimizeDeck(
        @Header("Authorization") token: String,
        @Path("deckId") deckId: String
    ): OptimizeResponse

    @DELETE("/api/card/{id}")
    suspend fun deleteCard(
        @Header("Authorization") token: String,
        @Path("id") cardId: String
    ): GeneralResponse

    @GET("/api/card/{deckId}")
    suspend fun getCardsByDeck(
        @Header("Authorization") token: String,
        @Path("deckId") deckId: String
    ): List<CardResponse>

    // STUDY ROUTES (FSRS / SM-2)
    @POST("/api/study/review/{id}")
    suspend fun reviewCard(
        @Header("Authorization") token: String,
        @Path("id") cardId: String,
        @Body request: ReviewRequest
    ): GeneralResponse
}

// 3. KHỞI TẠO CẤU HÌNH MẠNG (RETROFIT)
object RetrofitClient {
    private val BASE_URL = if (BuildConfig.DEBUG)
        BuildConfig.BASE_URL_DEBUG
    else
        BuildConfig.BASE_URL_RELEASE

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.MINUTES)
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