package com.example.n.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.n.BuildConfig
import com.example.n.model.Flashcard
import com.example.n.network.*
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlashcardViewModel : ViewModel() {

    // 1. TÍNH NĂNG LẬT THẺ (ĐỂ SCREEN CHẠY ĐƯỢC)
    private val _flashcards = MutableStateFlow<List<Flashcard>>(emptyList())
    val flashcards: StateFlow<List<Flashcard>> = _flashcards.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    fun nextCard() {
        if (_currentIndex.value < _flashcards.value.size - 1) {
            _currentIndex.value += 1
        }
    }

    fun previousCard() {
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
        }
    }

    // 2. QUẢN LÝ BỘ BÀI (DECK)
    private val _decks = MutableStateFlow<List<DeckResponse>>(emptyList())
    val decks: StateFlow<List<DeckResponse>> = _decks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchDecks(token: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitClient.apiService.getDecks("Bearer $token")
                _decks.value = response
            } catch (e: Exception) {
                println("Lỗi lấy danh sách Deck: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createDeck(token: String, name: String, onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitClient.apiService.createDeck("Bearer $token", DeckRequest(name))
                _decks.value = _decks.value + response
                onSuccess(response._id)
            } catch (e: Exception) {
                println("Lỗi tạo Deck: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteDeck(token: String, deckId: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.deleteDeck("Bearer $token", deckId)
                _decks.value = _decks.value.filter { it._id != deckId }
                println("Xóa Deck thành công!")
            } catch (e: Exception) {
                println("Lỗi xóa Deck: ${e.message}")
            }
        }
    }

    // 3. QUẢN LÝ THẺ (CARD) BÊN TRONG BỘ BÀI
    private val _cards = MutableStateFlow<List<CardResponse>>(emptyList())
    val cards: StateFlow<List<CardResponse>> = _cards.asStateFlow()

    fun fetchCards(token: String, deckId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitClient.apiService.getCardsByDeck("Bearer $token", deckId)
                _cards.value = response
            } catch (e: Exception) {
                println("Lỗi tải danh sách thẻ: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCard(
        token: String,
        deckId: String,
        front: String,
        back: String,
        type: String,
        imageUrl: String?,
        drawData: String?,
        sound: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val request = CardRequest(deckId, front, back, type, imageUrl, drawData, sound)
                val response = RetrofitClient.apiService.createCard("Bearer $token", request)
                _cards.value = _cards.value + response
                onSuccess()
            } catch (e: Exception) {
                println("Lỗi thêm thẻ pro: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCard(token: String, cardId: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.deleteCard("Bearer $token", cardId)
                _cards.value = _cards.value.filter { it._id != cardId }
                println("Xóa thẻ thành công!")
            } catch (e: Exception) {
                println("Lỗi xóa thẻ: ${e.message}")
            }
        }
    }

    private val _studyCards = MutableStateFlow<List<CardResponse>>(emptyList())
    val studyCards: StateFlow<List<CardResponse>> = _studyCards.asStateFlow()

    private val _studyIndex = MutableStateFlow(0)
    val studyIndex: StateFlow<Int> = _studyIndex.asStateFlow()

    fun fetchStudyCards(token: String, deckId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitClient.apiService.getCardsByDeck("Bearer $token", deckId)
                _studyCards.value = response
                _studyIndex.value = 0
            } catch (e: Exception) {
                println("Lỗi tải thẻ học: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitReview(token: String, cardId: String, rating: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.reviewCard("Bearer $token", cardId, ReviewRequest(rating))
                if (_studyIndex.value < _studyCards.value.size - 1) {
                    _studyIndex.value += 1
                } else {
                    _studyCards.value = emptyList()
                }
            } catch (e: Exception) {
                println("Lỗi gửi review: ${e.message}")
            }
        }
    }

    // 4. TÍNH NĂNG TẠO THẺ THÔNG MINH BẰNG AI (QUICK ADD) - ĐA NGÔN NGỮ
    suspend fun createCardWithAI(token: String, frontText: String): Boolean {
        return try {
            _isLoading.value = true

            // Lấy danh sách Deck nếu đang rỗng
            if (_decks.value.isEmpty()) {
                val decksResponse = RetrofitClient.apiService.getDecks("Bearer $token")
                _decks.value = decksResponse
            }

            // Gọi GEMINI AI với API Key đã được bảo mật từ BuildConfig
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = BuildConfig.GEMINI_API_KEY
            )

            // Prompt ép AI TỰ ĐỘNG NHẬN DIỆN ngôn ngữ
            val prompt = """
                Bạn là một chuyên gia ngôn ngữ đa quốc gia. Hãy phân tích từ hoặc cụm từ sau: '$frontText'.
                Hãy tự động nhận diện ngôn ngữ gốc của từ này (Anh, Nhật, Trung, Hàn, Pháp...) và trả về kết quả tuân thủ NGHIÊM NGẶT định dạng sau, tuyệt đối không thêm lời chào hay giải thích thừa:
                
                [Phiên âm theo chuẩn của ngôn ngữ gốc]
                Nghĩa tiếng Việt (Từ loại)
                Ví dụ: <1 câu ví dụ bằng CHÍNH NGÔN NGỮ ĐÓ chứa từ trên> (Nghĩa tiếng Việt của câu ví dụ)
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val backText = response.text?.trim() ?: "Không tìm thấy nghĩa"

            // Tìm hoặc tạo Deck "Hộp Nháp"
            var inboxDeck = _decks.value.find { it.name.equals("Hộp Nháp", ignoreCase = true) }

            if (inboxDeck == null) {
                val newDeck = RetrofitClient.apiService.createDeck("Bearer $token", DeckRequest("Hộp Nháp"))
                _decks.value = _decks.value + newDeck
                inboxDeck = newDeck
            }

            val deckId = inboxDeck._id

            // Tạo thẻ lưu lên server
            val request = CardRequest(
                deckId = deckId,
                front = frontText,
                back = backText,
                type = "text",
                imageUrl = null,
                drawData = null,
                sound = null
            )
            RetrofitClient.apiService.createCard("Bearer $token", request)

            // Cập nhật giao diện Hộp Nháp
            fetchCards(token, deckId)

            true
        } catch (e: Exception) {
            println("Lỗi AI tạo thẻ: ${e.message}")
            false
        } finally {
            _isLoading.value = false
        }
    }
}