package com.evening.dailylife.feature.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.core.data.analytics.TransactionAnalyticsRepository
import com.evening.dailylife.core.data.analytics.TransactionAnalyticsRepository.ChartAnalyticsCache
import com.evening.dailylife.core.data.analytics.TransactionAnalyticsRepository.ChartSummaryKey
import com.evening.dailylife.core.data.analytics.TransactionAnalyticsRepository.ChartSummarySnapshot
import com.evening.dailylife.feature.chart.model.ChartContentStatus
import com.evening.dailylife.feature.chart.model.ChartPeriod
import com.evening.dailylife.feature.chart.model.ChartRangeOption
import com.evening.dailylife.feature.chart.model.ChartType
import com.evening.dailylife.feature.chart.model.ChartUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ChartViewModel @Inject constructor(
    analyticsRepository: TransactionAnalyticsRepository
) : ViewModel() {

    private val chartCacheFlow = analyticsRepository.chartCache()

    private val selectedType = MutableStateFlow(ChartType.Expense)
    private val selectedPeriod = MutableStateFlow(ChartPeriod.Week)
    private val selectedRangeId = MutableStateFlow<String?>(null)

    private val initialUiState: ChartUiState = buildUiState(
        cache = chartCacheFlow.value,
        type = selectedType.value,
        period = selectedPeriod.value,
        preferredRangeId = selectedRangeId.value
    )

    private val selectionState = combine(
        chartCacheFlow,
        selectedType,
        selectedPeriod,
        selectedRangeId
    ) { cache, type, period, preferredRangeId ->
        SelectionState(
            cache = cache,
            type = type,
            period = period,
            preferredRangeId = preferredRangeId
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ChartUiState> = selectionState
        .mapLatest { selection ->
            buildUiState(
                cache = selection.cache,
                type = selection.type,
                period = selection.period,
                preferredRangeId = selection.preferredRangeId
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = initialUiState
        )

    fun onTypeSelected(type: ChartType) {
        if (selectedType.value == type) return
        selectedType.value = type
        selectedRangeId.value = null
    }

    fun onPeriodSelected(period: ChartPeriod) {
        if (selectedPeriod.value == period) return
        selectedPeriod.value = period
        selectedRangeId.value = null
    }

    fun onRangeOptionSelected(optionId: String) {
        if (selectedRangeId.value == optionId) return
        selectedRangeId.value = optionId
    }

    private fun buildUiState(
        cache: ChartAnalyticsCache,
        type: ChartType,
        period: ChartPeriod,
        preferredRangeId: String?
    ): ChartUiState {
        val options = cache.optionsByPeriod[period].orEmpty()
        val selectedOption = selectRangeOption(options, preferredRangeId)

        val summary = selectedOption
            ?.let { cache.summariesByKey[ChartSummaryKey(type, it.id)] }
            ?: DEFAULT_SUMMARY

        val moodEntries = selectedOption
            ?.let { cache.moodEntriesByRangeId[it.id] }
            .orEmpty()

        val contentStatus = if (summary.entries.isEmpty()) {
            ChartContentStatus.Empty
        } else {
            ChartContentStatus.Content
        }

        return ChartUiState(
            selectedType = type,
            selectedPeriod = period,
            rangeTabs = options,
            selectedRangeOption = selectedOption,
            entries = summary.entries,
            categoryRanks = summary.categoryRanks,
            totalAmount = summary.total,
            averageAmount = summary.average,
            moodEntries = moodEntries,
            contentStatus = contentStatus
        )
    }

    private fun selectRangeOption(
        options: List<ChartRangeOption>,
        preferredId: String?
    ): ChartRangeOption? {
        if (options.isEmpty()) return null
        if (preferredId == null) return options.first()
        return options.firstOrNull { it.id == preferredId } ?: options.first()
    }

    private data class SelectionState(
        val cache: ChartAnalyticsCache,
        val type: ChartType,
        val period: ChartPeriod,
        val preferredRangeId: String?
    )

    companion object {
        private val DEFAULT_SUMMARY = ChartSummarySnapshot(
            entries = emptyList(),
            total = 0.0,
            average = 0.0,
            categoryRanks = emptyList()
        )
    }
}
