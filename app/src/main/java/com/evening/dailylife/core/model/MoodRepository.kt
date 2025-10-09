package com.evening.dailylife.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.ui.graphics.vector.ImageVector

data class Mood(
    val name: String,
    val icon: ImageVector
)

object MoodRepository {
    val moods = listOf(
        Mood("开心", Icons.Outlined.SentimentVerySatisfied),
        Mood("不错", Icons.Outlined.SentimentSatisfied),
        Mood("一般", Icons.Outlined.SentimentNeutral),
        Mood("不爽", Icons.Outlined.SentimentDissatisfied),
        Mood("超糟", Icons.Outlined.SentimentVeryDissatisfied),
    )

    fun getIcon(moodName: String): ImageVector {
        return moods.find { it.name == moodName }?.icon ?: Icons.Outlined.SentimentNeutral
    }
}