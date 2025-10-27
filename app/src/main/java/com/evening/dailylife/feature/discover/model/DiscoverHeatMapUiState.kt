package com.evening.dailylife.feature.discover.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
data class DiscoverHeatMapUiState(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val contributions: Map<LocalDate, DiscoverHeatMapEntry> = emptyMap(),
    val isLoading: Boolean = true,
)

@RequiresApi(Build.VERSION_CODES.O)
data class DiscoverHeatMapEntry(
    val transactionCount: Int,
    val totalIncome: Double,
    val totalExpense: Double,
    val moodScoreSum: Int,
    val moodCount: Int,
    val intensity: Int = 0,
)
