package com.example.n.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.example.n.BuildConfig // Đảm bảo import đúng BuildConfig chứa API Key
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Lớp đại diện cho một tin nhắn trong đoạn chat
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean // Phân biệt tin nhắn của người dùng và của AI
)

class ChatbotViewModel : ViewModel() {

    // Danh sách các tin nhắn để hiển thị trên màn hình
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Trạng thái chờ AI trả lời (để hiện loading)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Khởi tạo mô hình Gemini
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro", // Dùng bản pro cho ổn định
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    // Khởi tạo phiên chat (để AI nhớ được bối cảnh cuộc trò chuyện)
    private val chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text("Xin chào. Bạn là ai?") },
            content(role = "model") { text("Tôi là Trợ lý AI của Renshuu. Tôi có thể giúp gì cho bạn trong việc học tập hôm nay?") }
        )
    )

    init {
        // Thêm câu chào mặc định vào màn hình khi vừa mở
        _messages.value = listOf(
            ChatMessage("Tôi là Trợ lý AI của Renshuu. Tôi có thể giúp gì cho bạn trong việc học tập hôm nay?", isFromUser = false)
        )
    }

    // Hàm gửi tin nhắn
    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // 1. Thêm tin nhắn của người dùng vào màn hình ngay lập tức
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(ChatMessage(userMessage, isFromUser = true))
        _messages.value = currentMessages

        // 2. Bật trạng thái loading
        _isLoading.value = true

        // 3. Gửi câu hỏi lên AI
        viewModelScope.launch {
            try {
                val response = chat.sendMessage(userMessage)
                val aiReply = response.text ?: "Xin lỗi, tôi không thể trả lời câu hỏi này."

                // 4. Cập nhật câu trả lời của AI lên màn hình
                _messages.value = _messages.value + ChatMessage(aiReply, isFromUser = false)
            } catch (e: Exception) {
                e.printStackTrace()
                _messages.value = _messages.value + ChatMessage("Lỗi kết nối. Vui lòng thử lại sau.", isFromUser = false)
            } finally {
                // 5. Tắt trạng thái loading
                _isLoading.value = false
            }
        }
    }
}