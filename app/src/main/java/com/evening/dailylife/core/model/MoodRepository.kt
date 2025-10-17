package com.evening.dailylife.core.model

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.evening.dailylife.R
import com.evening.dailylife.app.ui.theme.MoodBadColor
import com.evening.dailylife.app.ui.theme.MoodGoodColor
import com.evening.dailylife.app.ui.theme.MoodHappyColor
import com.evening.dailylife.app.ui.theme.MoodNeutralColor
import com.evening.dailylife.app.ui.theme.MoodTerribleColor
import com.evening.dailylife.core.util.StringProvider

data class Mood(
    @StringRes val nameRes: Int,
    val icon: ImageVector,
    val color: Color,
    val score: Int
)

object MoodRepository {
    val moods = listOf(
        Mood(R.string.mood_happy, Icons.Outlined.SentimentVerySatisfied, MoodHappyColor, 2),
        Mood(R.string.mood_good, Icons.Outlined.SentimentSatisfied, MoodGoodColor, 1),
        Mood(R.string.mood_normal, Icons.Outlined.SentimentNeutral, MoodNeutralColor, 0),
        Mood(R.string.mood_bad, Icons.Outlined.SentimentDissatisfied, MoodBadColor, -1),
        Mood(R.string.mood_terrible, Icons.Outlined.SentimentVeryDissatisfied, MoodTerribleColor, -2),
    )

    private fun findMoodByName(context: Context, moodName: String): Mood? =
        moods.firstOrNull { context.getString(it.nameRes) == moodName }

    fun getIcon(context: Context, moodName: String): ImageVector =
        findMoodByName(context, moodName)?.icon ?: Icons.AutoMirrored.Outlined.HelpOutline

    fun getColor(context: Context, moodName: String): Color =
        findMoodByName(context, moodName)?.color ?: Color.Gray

    fun getMoodByScore(totalScore: Int): Mood? = when {
        totalScore >= 2 -> moods.firstOrNull { it.score == 2 }
        totalScore == 1 -> moods.firstOrNull { it.score == 1 }
        totalScore == 0 -> moods.firstOrNull { it.score == 0 }
        totalScore == -1 -> moods.firstOrNull { it.score == -1 }
        else -> moods.firstOrNull { it.score == -2 }
    }

    fun getMoodNameByScore(stringProvider: StringProvider, totalScore: Int): String? =
        getMoodByScore(totalScore)?.let { stringProvider.getString(it.nameRes) }

    fun getMoodScoreByName(stringProvider: StringProvider, moodName: String): Int? =
        moods.firstOrNull { stringProvider.getString(it.nameRes) == moodName }?.score
}
