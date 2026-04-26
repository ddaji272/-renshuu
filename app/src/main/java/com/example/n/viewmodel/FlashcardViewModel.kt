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

    // ==========================================
    // 1. TÍNH NĂNG LẬT THẺ (CŨ - ĐỂ SCREEN CHẠY ĐƯỢC)
    // ==========================================

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

    // ==========================================
    // 2. QUẢN LÝ BỘ BÀI (DECK)
    // ==========================================

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

    // ==========================================
    // 3. QUẢN LÝ THẺ (CARD) BÊN TRONG BỘ BÀI
    // ==========================================

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
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Gói toàn bộ dữ liệu xịn xò này gửi lên Backend
                val request = CardRequest(deckId, front, back, type, imageUrl, drawData)
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
}