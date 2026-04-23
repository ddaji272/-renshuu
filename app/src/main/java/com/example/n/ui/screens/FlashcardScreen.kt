package com.example.n.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.n.ui.components.FlashcardItem
import com.example.n.viewmodel.FlashcardViewModel

@Composable
fun FlashcardScreen(viewModel: FlashcardViewModel = viewModel()) {
    // Quan sát dữ liệu từ ViewModel
    val flashcards by viewModel.flashcards.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()

    // Trạng thái lật thẻ (giữ tại UI vì BE không cần quan tâm thẻ đang lật hay không)
    var isFlipped by remember { mutableStateOf(false) }

    // Tự động lật thẻ về mặt trước mỗi khi chuyển sang thẻ mới
    LaunchedEffect(currentIndex) {
        isFlipped = false
    }

    // Trường hợp danh sách rỗng
    if (flashcards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Danh sách thẻ trống!")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hiển thị tiến độ học
        Text(
            text = "Thẻ ${currentIndex + 1} / ${flashcards.size}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Hiển thị thẻ Flashcard
        FlashcardItem(
            card = flashcards[currentIndex],
            isFlipped = isFlipped,
            onCardClick = { isFlipped = !isFlipped }
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Hàng chứa các nút điều hướng
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { viewModel.previousCard() },
                enabled = currentIndex > 0,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Trước")
            }

            Button(
                onClick = { viewModel.nextCard() },
                enabled = currentIndex < flashcards.size - 1,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("Tiếp")
            }
        }
    }
}