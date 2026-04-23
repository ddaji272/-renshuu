package com.example.n.viewmodel

import androidx.lifecycle.ViewModel
import com.example.n.model.Flashcard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlashcardViewModel : ViewModel() {

    // Mock data - Dữ liệu giả lập
    private val mockData = listOf(
        Flashcard("1", "Kotlin", "Ngôn ngữ lập trình hiện đại cho Android"),
        Flashcard("2", "Jetpack Compose", "Toolkit xây dựng UI khai báo"),
        Flashcard("3", "ViewModel", "Lưu trữ và quản lý dữ liệu liên quan đến UI"),
        Flashcard("4", "StateFlow", "Giữ trạng thái và phát ra luồng dữ liệu")
    )

    private val _flashcards = MutableStateFlow(mockData)
    val flashcards: StateFlow<List<Flashcard>> = _flashcards.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    fun nextCard() {
        if (_currentIndex.value < _flashcards.value.size - 1) {
            _currentIndex.value++
        }
    }

    fun previousCard() {
        if (_currentIndex.value > 0) {
            _currentIndex.value--
        }
    }
}