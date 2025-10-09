package com.evening.dailylife.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
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
    val color: Color,
    val score: Int
)

object MoodRepository {
    val moods = listOf(
        Mood("开心", Icons.Outlined.SentimentVerySatisfied, Color(0xFF4CAF50), 2),
        Mood("不错", Icons.Outlined.SentimentSatisfied, Color(0xFF8BC34A), 1),
        Mood("一般", Icons.Outlined.SentimentNeutral, Color(0xFFFFC107), 0),
        Mood("不爽", Icons.Outlined.SentimentDissatisfied, Color(0xFFFF9800), -1),
        Mood("超糟", Icons.Outlined.SentimentVeryDissatisfied, Color(0xFFF44336), -2),
    )

    fun getIcon(moodName: String): ImageVector {
        return moods.find { it.name == moodName }?.icon ?: Icons.AutoMirrored.Outlined.HelpOutline
    }

    fun getColor(moodName: String): Color {
        return moods.find { it.name == moodName }?.color ?: Color.Gray
    }

    // 根据分数获取心情对象
    fun getMoodByScore(totalScore: Int): Mood? {
        // 根据总分返回最匹配的心情
        return when {
            totalScore >= 2 -> moods.find { it.name == "开心" }
            totalScore == 1 -> moods.find { it.name == "不错" }
            totalScore == 0 -> moods.find { it.name == "一般" }
            totalScore == -1 -> moods.find { it.name == "不爽" }
            else -> moods.find { it.name == "超糟" }
        }
    }
}