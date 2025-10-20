package com.evening.dailylife.feature.me

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.core.data.local.model.DailyTransactionSummary
import com.evening.dailylife.core.data.preferences.PreferencesManager
import com.evening.dailylife.core.data.preferences.ThemeMode
import com.evening.dailylife.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.LinkedHashMap
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class MeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    transactionRepository: TransactionRepository,
) : ViewModel() {

    companion object {
        private const val HEAT_MAP_MONTH_SPAN = 12L
    }

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val heatMapRange = HeatMapRange.create(zoneId = zoneId)

    private val initialHeatMapState = MeHeatMapUiState(
        startDate = heatMapRange.startDate,
        endDate = heatMapRange.endDate,
        isLoading = true,
    )

    val heatMapUiState: StateFlow<MeHeatMapUiState> = transactionRepository
        .getDailySummaries(
            startDate = heatMapRange.startMillis,
            endDate = heatMapRange.endMillis,
        )
        .map { summaries -> buildHeatMapState(summaries) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = initialHeatMapState,
        )

    // 直接暴露状态
    val themeMode = preferencesManager.themeMode
    val dynamicColor = preferencesManager.dynamicColor

    // setter 直接调用 PreferencesManager 的方法
    fun setThemeMode(themeMode: ThemeMode) {
        preferencesManager.setThemeMode(themeMode)
    }

    fun setDynamicColor(enabled: Boolean) {
        preferencesManager.setDynamicColor(enabled)
    }

    private fun buildHeatMapState(
        summaries: List<DailyTransactionSummary>,
    ): MeHeatMapUiState {
        val contributions = LinkedHashMap<LocalDate, MeHeatMapEntry>()
        val summaryByDate = summaries.associateBy { summary ->
            Instant.ofEpochMilli(summary.dayStartMillis)
                .atZone(zoneId)
                .toLocalDate()
        }

        var currentDate = heatMapRange.startDate
        while (!currentDate.isAfter(heatMapRange.endDate)) {
            val summary = summaryByDate[currentDate]
            contributions[currentDate] = summary?.let {
                MeHeatMapEntry(
                    transactionCount = it.transactionCount,
                    totalIncome = it.totalIncome,
                    totalExpense = it.totalExpense,
                    moodScoreSum = it.moodScoreSum,
                    moodCount = it.moodCount,
                )
            } ?: MeHeatMapEntry(
                transactionCount = 0,
                totalIncome = 0.0,
                totalExpense = 0.0,
                moodScoreSum = 0,
                moodCount = 0,
            )
            currentDate = currentDate.plusDays(1)
        }

        return MeHeatMapUiState(
            startDate = heatMapRange.startDate,
            endDate = heatMapRange.endDate,
            contributions = contributions,
            isLoading = false,
        )
    }

    private data class HeatMapRange(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val startMillis: Long,
        val endMillis: Long,
    ) {
        companion object {
            fun create(now: LocalDate = LocalDate.now(), zoneId: ZoneId): HeatMapRange {
                val endDate = now
                val startDate = endDate.minusMonths(HEAT_MAP_MONTH_SPAN)
                val startMillis = startDate.startOfDayMillis(zoneId)
                val endMillis = endDate.endOfDayMillis(zoneId)
                return HeatMapRange(startDate, endDate, startMillis, endMillis)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun LocalDate.startOfDayMillis(zoneId: ZoneId): Long =
    atStartOfDay(zoneId).toInstant().toEpochMilli()

@RequiresApi(Build.VERSION_CODES.O)
private fun LocalDate.endOfDayMillis(zoneId: ZoneId): Long =
    atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
