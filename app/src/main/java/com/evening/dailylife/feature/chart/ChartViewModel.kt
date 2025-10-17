package com.evening.dailylife.feature.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.R
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.core.util.StringProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val stringProvider: StringProvider
) : ViewModel() {

    companion object {
        private const val DEFAULT_TOP_RANK_LIMIT = 5
        private const val MILLIS_IN_DAY = 24L * 60L * 60L * 1000L
        private const val DAYS_IN_WEEK = 7
        private const val MONTHS_IN_YEAR = 12
    }

    private val _uiState = MutableStateFlow(ChartUiState())
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    private var cachedTransactions: List<TransactionEntity> = emptyList()
    private val cachedTransactionsByType: MutableMap<ChartType, List<TransactionEntity>> = mutableMapOf(
        ChartType.Expense to emptyList(),
        ChartType.Income to emptyList(),
    )
    private val rangeOptionsCache = mutableMapOf<RangeCacheKey, List<ChartRangeOption>>()
    private val summaryCache = mutableMapOf<SummaryCacheKey, CachedSummary>()
    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collectLatest { transactions ->
                cachedTransactions = transactions.sortedBy(TransactionEntity::date)
                cachedTransactionsByType[ChartType.Expense] = cachedTransactions.filter { it.amount < 0 }
                cachedTransactionsByType[ChartType.Income] = cachedTransactions.filter { it.amount > 0 }
                invalidateCaches()
                refreshState(preferredOptionId = _uiState.value.selectedRangeOption?.id)
            }
        }
    }

    fun onTypeSelected(type: ChartType) {
        if (type == _uiState.value.selectedType) return
        _uiState.value = _uiState.value.copy(selectedType = type)
        refreshState(resetRangeSelection = true)
    }

    fun onPeriodSelected(period: ChartPeriod) {
        if (period == _uiState.value.selectedPeriod) return
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        refreshState(resetRangeSelection = true)
    }

    fun onRangeOptionSelected(optionId: String) {
        if (optionId == _uiState.value.selectedRangeOption?.id) return
        refreshState(preferredOptionId = optionId)
    }

    private fun refreshState(
        resetRangeSelection: Boolean = false,
        preferredOptionId: String? = null
    ) {
        val currentState = _uiState.value
        val transactionsSnapshot = cachedTransactions

        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val newState = withContext(Dispatchers.Default) {
                buildState(
                    baseState = currentState,
                    transactions = transactionsSnapshot,
                    resetRangeSelection = resetRangeSelection,
                    preferredOptionId = preferredOptionId
                )
            }
            _uiState.value = newState
        }
    }

    private fun buildState(
        baseState: ChartUiState,
        transactions: List<TransactionEntity>,
        resetRangeSelection: Boolean,
        preferredOptionId: String?
    ): ChartUiState {
        val type = baseState.selectedType
        val period = baseState.selectedPeriod
        val filtered = filterTransactionsByType(type)
        val rangeTabs = buildRangeOptions(type, period, filtered)

        val selectedOption = when {
            rangeTabs.isEmpty() -> null
            preferredOptionId != null -> rangeTabs.firstOrNull { it.id == preferredOptionId }
                ?: rangeTabs.first()
            resetRangeSelection -> rangeTabs.first()
            else -> {
                val currentId = baseState.selectedRangeOption?.id
                rangeTabs.firstOrNull { it.id == currentId } ?: rangeTabs.first()
            }
        }

        val range = selectedOption?.let {
            ChartDataCalculator.buildRange(
                period = period,
                startMillis = it.startInclusive,
                endMillis = it.endInclusive,
                stringProvider = stringProvider
            )
        }

        val (summary, moodEntries) = if (range != null) {
            val cacheKey = SummaryCacheKey(
                type = type,
                period = period,
                start = range.start,
                end = range.end
            )
            summaryCache[cacheKey]?.let { cached ->
                cached.summary to cached.moodEntries
            } ?: run {
                val rangeTransactions = transactionsInRange(filtered, range.start, range.end)
                val computedSummary = ChartDataCalculator.summarize(
                    transactions = rangeTransactions,
                    type = type,
                    range = range,
                    topLimit = DEFAULT_TOP_RANK_LIMIT
                )
                val computedMood = ChartDataCalculator.buildMoodEntries(
                    transactions = transactions,
                    range = range
                )
                summaryCache[cacheKey] = CachedSummary(
                    summary = computedSummary,
                    moodEntries = computedMood
                )
                computedSummary to computedMood
            }
        } else {
            ChartDataCalculator.Summary(
                entries = emptyList(),
                total = 0.0,
                average = 0.0,
                categoryRanks = emptyList()
            ) to emptyList()
        }

        return baseState.copy(
            rangeTabs = rangeTabs,
            selectedRangeOption = selectedOption,
            entries = summary.entries,
            categoryRanks = summary.categoryRanks,
            totalAmount = summary.total,
            averageAmount = summary.average,
            moodEntries = moodEntries,
            isLoading = false,
            hasLoadedContent = true
        )
    }

    private fun filterTransactionsByType(type: ChartType): List<TransactionEntity> {
        return cachedTransactionsByType[type].orEmpty()
    }

    private fun buildRangeOptions(
        type: ChartType,
        period: ChartPeriod,
        transactions: List<TransactionEntity>
    ): List<ChartRangeOption> {
        val cacheKey = RangeCacheKey(type = type, period = period)
        rangeOptionsCache[cacheKey]?.let { return it }

        val result = when (period) {
            ChartPeriod.Week -> buildWeekOptions(transactions)
            ChartPeriod.Month -> buildMonthOptions(transactions)
            ChartPeriod.Year -> buildYearOptions(transactions)
        }
        rangeOptionsCache[cacheKey] = result
        return result
    }

    private fun transactionsInRange(
        transactions: List<TransactionEntity>,
        start: Long,
        end: Long
    ): List<TransactionEntity> {
        if (transactions.isEmpty()) return emptyList()
        val result = ArrayList<TransactionEntity>()
        for (entity in transactions) {
            if (entity.date < start) continue
            if (entity.date > end) break
            result.add(entity)
        }
        return result
    }

    private fun invalidateCaches() {
        rangeOptionsCache.clear()
        summaryCache.clear()
    }

    private fun buildWeekOptions(
        transactions: List<TransactionEntity>
    ): List<ChartRangeOption> {
        if (transactions.isEmpty()) return emptyList()

        val now = isoCalendar().apply { timeInMillis = System.currentTimeMillis() }
        val nowWeekStart = now.weekStartMillis()

        val grouped = transactions.groupBy { transaction ->
            val cal = isoCalendar().apply {
                timeInMillis = transaction.date
            }
            cal.weekStartMillis()
        }

        return grouped.map { (startMillis, _) ->
            val startCal = isoCalendar().apply { timeInMillis = startMillis }
            val weekOfYear = startCal.get(Calendar.WEEK_OF_YEAR)
            val weekYear = startCal.isoWeekYear()
            val endMillis = (startCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, DAYS_IN_WEEK - 1)
                setToEndOfDay()
            }.timeInMillis

            val diffWeeks = max(0, ((nowWeekStart - startMillis) / MILLIS_IN_DAY / DAYS_IN_WEEK).toInt())
            val label = when {
                diffWeeks == 0 -> stringProvider.getString(R.string.chart_tab_week_this)
                diffWeeks == 1 -> stringProvider.getString(R.string.chart_tab_week_last)
                weekYear == now.isoWeekYear() -> stringProvider.getString(
                    R.string.chart_tab_week_number,
                    weekOfYear
                )
                else -> stringProvider.getString(
                    R.string.chart_tab_week_with_year,
                    weekYear,
                    weekOfYear
                )
            }

            ChartRangeOption(
                id = "week-$weekYear-$weekOfYear",
                period = ChartPeriod.Week,
                label = label,
                startInclusive = startMillis,
                endInclusive = endMillis
            )
        }.sortedByDescending { it.startInclusive }
    }

    private fun buildMonthOptions(
        transactions: List<TransactionEntity>
    ): List<ChartRangeOption> {
        if (transactions.isEmpty()) return emptyList()

        val grouped = transactions.groupBy { transaction ->
            val cal = Calendar.getInstance(Locale.getDefault()).apply {
                timeInMillis = transaction.date
            }
            MonthKey(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH)
            )
        }
        val now = Calendar.getInstance(Locale.getDefault())

        return grouped.map { (key, _) ->
            val startCal = Calendar.getInstance(Locale.getDefault()).apply {
                set(Calendar.YEAR, key.year)
                set(Calendar.MONTH, key.month)
                set(Calendar.DAY_OF_MONTH, 1)
                setToStartOfDay()
            }
            val endCal = (startCal.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                setToEndOfDay()
            }

            val diffMonths =
                (now.get(Calendar.YEAR) - key.year) * MONTHS_IN_YEAR + (now.get(Calendar.MONTH) - key.month)
            val label = when {
                diffMonths == 0 -> stringProvider.getString(R.string.chart_tab_month_this)
                diffMonths == 1 -> stringProvider.getString(R.string.chart_tab_month_last)
                key.year == now.get(Calendar.YEAR) -> stringProvider.getString(
                    R.string.chart_tab_month_number,
                    key.month + 1
                )
                else -> stringProvider.getString(
                    R.string.chart_tab_month_with_year,
                    key.year,
                    key.month + 1
                )
            }

            ChartRangeOption(
                id = "month-${key.year}-${key.month}",
                period = ChartPeriod.Month,
                label = label,
                startInclusive = startCal.timeInMillis,
                endInclusive = endCal.timeInMillis
            )
        }.sortedByDescending { it.startInclusive }
    }

    private fun buildYearOptions(
        transactions: List<TransactionEntity>
    ): List<ChartRangeOption> {
        if (transactions.isEmpty()) return emptyList()

        val grouped = transactions.groupBy { transaction ->
            val cal = Calendar.getInstance(Locale.getDefault()).apply {
                timeInMillis = transaction.date
            }
            cal.get(Calendar.YEAR)
        }
        val nowYear = Calendar.getInstance(Locale.getDefault()).get(Calendar.YEAR)

        return grouped.map { (year, _) ->
            val startCal = Calendar.getInstance(Locale.getDefault()).apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DAY_OF_MONTH, 1)
                setToStartOfDay()
            }
            val endCal = (startCal.clone() as Calendar).apply {
                set(Calendar.MONTH, Calendar.DECEMBER)
                set(Calendar.DAY_OF_MONTH, 31)
                setToEndOfDay()
            }

            val label = when (nowYear - year) {
                0 -> stringProvider.getString(R.string.chart_tab_year_this)
                1 -> stringProvider.getString(R.string.chart_tab_year_last)
                else -> stringProvider.getString(R.string.chart_tab_year_value, year)
            }

            ChartRangeOption(
                id = "year-$year",
                period = ChartPeriod.Year,
                label = label,
                startInclusive = startCal.timeInMillis,
                endInclusive = endCal.timeInMillis
            )
        }.sortedByDescending { it.startInclusive }
    }

    private fun isoCalendar(): Calendar {
        return GregorianCalendar.getInstance(Locale.getDefault()).apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
        }
    }

    private fun Calendar.setToStartOfDay() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun Calendar.setToEndOfDay() {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    private fun Calendar.weekStartMillis(): Long {
        val copy = (clone() as Calendar)
        val diff = (copy.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek + DAYS_IN_WEEK) % DAYS_IN_WEEK
        copy.add(Calendar.DAY_OF_YEAR, -diff)
        copy.setToStartOfDay()
        return copy.timeInMillis
    }

    private fun Calendar.isoWeekYear(): Int {
        val weekOfYear = get(Calendar.WEEK_OF_YEAR)
        val year = get(Calendar.YEAR)
        val month = get(Calendar.MONTH)
        return when {
            weekOfYear == 1 && month == Calendar.DECEMBER -> year + 1
            weekOfYear >= 52 && month == Calendar.JANUARY -> year - 1
            else -> year
        }
    }

    private data class MonthKey(
        val year: Int,
        val month: Int
    )

    private data class RangeCacheKey(
        val type: ChartType,
        val period: ChartPeriod
    )

    private data class SummaryCacheKey(
        val type: ChartType,
        val period: ChartPeriod,
        val start: Long,
        val end: Long
    )

    private data class CachedSummary(
        val summary: ChartDataCalculator.Summary,
        val moodEntries: List<MoodChartEntry>
    )

}
