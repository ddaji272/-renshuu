package com.example.n.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.n.model.Flashcard // Import model Flashcard cũ của bạn
import com.example.n.network.*
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

    // Tải danh sách thẻ khi bấm vào 1 bộ bài
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

    // Thêm thẻ mới
    fun addCard(
        token: String,
        deckId: String,
        front: String,
        back: String,
        type: String,         // Thêm loại
        imageUrl: String?,    // Thêm ảnh
        drawData: String?,    // Thêm nét vẽ
        sound: String? = null,// <-- THÊM BIẾN SOUND VÀO ĐÂY ĐỂ HỨNG DỮ LIỆU TỪ UI
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Nhét biến sound vào chung gói Request để đẩy lên Backend
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

    // Xóa thẻ
    fun deleteCard(token: String, cardId: String) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.deleteCard("Bearer $token", cardId)
                _cards.value = _cards.value.filter { it._id != cardId } // Rút thẻ vừa xóa khỏi UI
                println("Xóa thẻ thành công!")
            } catch (e: Exception) {
                println("Lỗi xóa thẻ: ${e.message}")
            }
        }
    }

    // Danh sách thẻ dành riêng cho việc ôn tập
    private val _studyCards = MutableStateFlow<List<CardResponse>>(emptyList())
    val studyCards: StateFlow<List<CardResponse>> = _studyCards.asStateFlow()

    private val _studyIndex = MutableStateFlow(0)
    val studyIndex: StateFlow<Int> = _studyIndex.asStateFlow()

    // Lấy danh sách thẻ cần học hôm nay
    fun fetchStudyCards(token: String, deckId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitClient.apiService.getCardsByDeck("Bearer $token", deckId)
                // Sau này sẽ lọc theo ngày ôn tập từ Backend, tạm thời lấy hết để test
                _studyCards.value = response
                _studyIndex.value = 0
            } catch (e: Exception) {
                println("Lỗi tải thẻ học: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Gửi kết quả (0-Again, 1-Hard, 2-Good, 3-Easy)
    fun submitReview(token: String, cardId: String, rating: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.reviewCard("Bearer $token", cardId, ReviewRequest(rating))
                // Chuyển sang thẻ tiếp theo
                if (_studyIndex.value < _studyCards.value.size - 1) {
                    _studyIndex.value += 1
                } else {
                    _studyCards.value = emptyList() // Đã hoàn thành bộ bài
                }
            } catch (e: Exception) {
                println("Lỗi gửi review: ${e.message}")
            }
        }
    }
}