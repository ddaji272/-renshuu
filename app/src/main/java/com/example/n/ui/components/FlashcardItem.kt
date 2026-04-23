package com.example.n.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.n.model.Flashcard

@Composable
fun FlashcardItem(card: Flashcard, isFlipped: Boolean, onCardClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flipAnimation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clickable { onCardClick() }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // Mặt trước
                Text(
                    text = card.word,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // Mặt sau (Xoay ngược chữ lại)
                Text(
                    text = card.meaning,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(16.dp)
                        .graphicsLayer { rotationY = 180f }
                )
            }
        }
    }
}