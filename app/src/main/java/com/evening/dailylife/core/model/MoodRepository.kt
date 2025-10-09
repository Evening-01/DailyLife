package com.evening.dailylife.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Mood(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

object MoodRepository {
    val moods = listOf(
        Mood("开心", Icons.Outlined.SentimentVerySatisfied, Color(0xFF4CAF50)),
        Mood("不错", Icons.Outlined.SentimentSatisfied, Color(0xFF8BC34A)),
        Mood("一般", Icons.Outlined.SentimentNeutral, Color(0xFFFFC107)),
        Mood("不爽", Icons.Outlined.SentimentDissatisfied, Color(0xFFFF9800)),
        Mood("超糟", Icons.Outlined.SentimentVeryDissatisfied, Color(0xFFF44336)),
    )

    fun getIcon(moodName: String): ImageVector {
        return moods.find { it.name == moodName }?.icon ?: Icons.Outlined.HelpOutline
    }

    fun getColor(moodName: String): Color {
        return moods.find { it.name == moodName }?.color ?: Color.Gray
    }
}