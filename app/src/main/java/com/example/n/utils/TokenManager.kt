package com.example.n.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    // Tạo một vùng nhớ kín có tên là "renshuu_prefs"
    private val prefs: SharedPreferences = context.getSharedPreferences("renshuu_prefs", Context.MODE_PRIVATE)

    // 1. Cất Token vào két (Gọi lúc Đăng nhập thành công)
    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    // 2. Lấy Token ra xài (Gọi lúc mở app hoặc lúc cần tạo Deck/Card)
    fun getToken(): String? {
        return prefs.getString("jwt_token", null)
    }

    // 3. Xóa Token (Gọi lúc Đăng xuất)
    fun clearToken() {
        prefs.edit().remove("jwt_token").apply()
    }
}