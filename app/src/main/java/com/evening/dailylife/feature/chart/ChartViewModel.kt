package com.evening.dailylife.feature.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.R
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.core.util.StringProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.math.max
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ChartViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    private val stringProvider: StringProvider
) : ViewModel() {

    companion object {
        private const val DEFAULT_TOP_RANK_LIMIT = 5
        private const val MILLIS_IN_DAY = 24L * 60L * 60L * 1000L
        private const val DAYS_IN_WEEK = 7
        private const val MONTHS_IN_YEAR = 12
    }

    private val selectedType = MutableStateFlow(ChartType.Expense)
    private val selectedPeriod = MutableStateFlow(ChartPeriod.Week)
    private val selectedRangeId = MutableStateFlow<String?>(null)

    private val transactionsState: StateFlow<TransactionsState> =
        transactionRepository.getAllTransactions()
            .map<List<TransactionEntity>, TransactionsState> { entities ->
                val sorted = entities.sortedBy(TransactionEntity::date)
                TransactionsState.Loaded(sorted)
            }
            .onStart { emit(TransactionsState.Loading) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TransactionsState.Loading
            )

    val uiState: StateFlow<ChartUiState> = combine(
        transactionsState,
        selectedType,
        selectedPeriod,
        selectedRangeId
    ) { transactions, type, period, preferredRangeId ->
        when (transactions) {
            TransactionsState.Loading -> ChartUiState(
                selectedType = type,
                selectedPeriod = period,
                contentStatus = ChartContentStatus.Loading
            )

            is TransactionsState.Loaded -> buildUiState(
                type = type,
                period = period,
                preferredRangeId = preferredRangeId,
                allTransactions = transactions.items
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChartUiState()
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
        type: ChartType,
        period: ChartPeriod,
        preferredRangeId: String?,
        allTransactions: List<TransactionEntity>
    ): ChartUiState {
        val typedTransactions = filterTransactionsByType(allTransactions, type)
        val rangeOptions = buildRangeOptions(
            period = period,
            transactions = typedTransactions
        )
        val selectedOption = selectRangeOption(rangeOptions, preferredRangeId)
        val range = selectedOption?.let {
            ChartDataCalculator.buildRange(
                period = period,
                startMillis = it.startInclusive,
                endMillis = it.endInclusive,
                stringProvider = stringProvider
            )
        }

        val summary = if (range != null) {
            val rangeTransactions = transactionsInRange(
                transactions = typedTransactions,
                start = range.start,
                end = range.end
            )
            ChartDataCalculator.summarize(
                transactions = rangeTransactions,
                type = type,
                range = range,
                topLimit = DEFAULT_TOP_RANK_LIMIT
            )
        } else {
            ChartDataCalculator.Summary(
                entries = emptyList(),
                total = 0.0,
                average = 0.0,
                categoryRanks = emptyList()
            )
        }

        val moodEntries = if (range != null) {
            ChartDataCalculator.buildMoodEntries(
                transactions = allTransactions,
                range = range
            )
        } else {
            emptyList()
        }

        val contentStatus = when {
            selectedOption == null -> ChartContentStatus.Empty
            else -> ChartContentStatus.Content
        }

        return ChartUiState(
            selectedType = type,
            selectedPeriod = period,
            rangeTabs = rangeOptions,
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

    private fun filterTransactionsByType(
        transactions: List<TransactionEntity>,
        type: ChartType
    ): List<TransactionEntity> {
        return when (type) {
            ChartType.Expense -> transactions.filter { it.amount < 0 }
            ChartType.Income -> transactions.filter { it.amount > 0 }
        }
    }

    private fun buildRangeOptions(
        period: ChartPeriod,
        transactions: List<TransactionEntity>
    ): List<ChartRangeOption> {
        return when (period) {
            ChartPeriod.Week -> buildWeekOptions(transactions)
            ChartPeriod.Month -> buildMonthOptions(transactions)
            ChartPeriod.Year -> buildYearOptions(transactions)
        }
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

    private sealed interface TransactionsState {
        data object Loading : TransactionsState
        data class Loaded(val items: List<TransactionEntity>) : TransactionsState
    }
}
