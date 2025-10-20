package com.evening.dailylife.feature.me

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
data class MeHeatMapUiState(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val contributions: Map<LocalDate, MeHeatMapEntry> = emptyMap(),
    val isLoading: Boolean = true,
)

@RequiresApi(Build.VERSION_CODES.O)
data class MeHeatMapEntry(
    val transactionCount: Int,
    val totalIncome: Double,
    val totalExpense: Double,
    val moodScoreSum: Int,
    val moodCount: Int,
)
