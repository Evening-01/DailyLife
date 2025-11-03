package com.evening.dailylife.core.data.analytics

import android.os.Build
import androidx.annotation.RequiresApi
import com.evening.dailylife.R
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.core.di.ApplicationScope
import com.evening.dailylife.core.util.StringProvider
import com.evening.dailylife.feature.chart.ChartDataCalculator
import com.evening.dailylife.feature.chart.model.ChartCategoryRank
import com.evening.dailylife.feature.chart.model.ChartEntry
import com.evening.dailylife.feature.chart.model.ChartPeriod
import com.evening.dailylife.feature.chart.model.ChartRangeOption
import com.evening.dailylife.feature.chart.model.ChartType
import com.evening.dailylife.feature.chart.model.MoodChartEntry
import com.evening.dailylife.feature.discover.model.TypeProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max

/**
 * 集中构建交易相关的统计快照，避免各个页面重复执行昂贵计算。
 */
@Singleton
class TransactionAnalyticsRepository @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val stringProvider: StringProvider,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    private val sortedTransactions: StateFlow<List<TransactionEntity>> =
        transactionRepository.observeAllTransactions()
            .map { transactions ->
                transactions
                    .asSequence()
                    .filter { !it.isDeleted }
                    .sortedBy(TransactionEntity::date)
                    .toList()
            }
            .stateIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    private val monthlySnapshotsFlow: StateFlow<Map<YearMonthKey, MonthlySnapshot>> =
        sortedTransactions
            .map { transactions -> buildMonthlySnapshots(transactions) }
            .stateIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyMap()
            )

    private val chartCacheFlow: StateFlow<ChartAnalyticsCache> =
        sortedTransactions
            .map { transactions -> buildChartCache(transactions) }
            .stateIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                initialValue = ChartAnalyticsCache.EMPTY
            )

    @RequiresApi(Build.VERSION_CODES.O)
    private val discoverCacheFlow: StateFlow<DiscoverAnalyticsCache> =
        sortedTransactions
            .map { transactions -> buildDiscoverCache(transactions) }
            .stateIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                initialValue = DiscoverAnalyticsCache.EMPTY
            )

    @RequiresApi(Build.VERSION_CODES.O)
    private val profileStatsFlow: StateFlow<MeProfileStatsData> =
        sortedTransactions
            .map { transactions -> buildProfileStats(transactions) }
            .stateIn(
                scope = applicationScope,
                started = SharingStarted.Eagerly,
                initialValue = MeProfileStatsData()
            )

    fun observeMonthlySnapshot(year: Int, zeroBasedMonth: Int): Flow<MonthlySnapshot> {
        val key = YearMonthKey(year, zeroBasedMonth)
        return monthlySnapshotsFlow
            .map { map -> map[key] ?: MonthlySnapshot.empty(year, zeroBasedMonth) }
            .distinctUntilChanged()
    }

    fun observeLatestSnapshotUpTo(year: Int, zeroBasedMonth: Int): Flow<MonthlySnapshot> {
        val targetKey = YearMonthKey(year, zeroBasedMonth)
        return monthlySnapshotsFlow
            .map { map ->
                val fallbackKey = map.keys
                    .filter { it <= targetKey }
                    .maxOrNull()
                fallbackKey?.let { map[it] } ?: MonthlySnapshot.empty(year, zeroBasedMonth)
            }
            .distinctUntilChanged()
    }

    private fun ChartDataCalculator.Summary.toSnapshot(): ChartSummarySnapshot {
        return ChartSummarySnapshot(
            entries = entries,
            total = total,
            average = average,
            categoryRanks = categoryRanks
        )
    }

    fun chartCache(): StateFlow<ChartAnalyticsCache> = chartCacheFlow

    @RequiresApi(Build.VERSION_CODES.O)
    fun discoverCache(): StateFlow<DiscoverAnalyticsCache> = discoverCacheFlow

    @RequiresApi(Build.VERSION_CODES.O)
    fun profileStats(): StateFlow<MeProfileStatsData> = profileStatsFlow

    private fun buildMonthlySnapshots(
        transactions: List<TransactionEntity>
    ): Map<YearMonthKey, MonthlySnapshot> {
        if (transactions.isEmpty()) return emptyMap()

        val calendar = Calendar.getInstance(Locale.getDefault())
        val groupedByMonth = transactions.groupBy { entity ->
            calendar.timeInMillis = entity.date
            YearMonthKey(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH)
            )
        }

        return groupedByMonth.mapValues { (key, monthTransactions) ->
            val groupedByDay = monthTransactions.groupBy { entity ->
                dayStartMillis(entity.date)
            }

            val daySnapshots = groupedByDay.map { (dayStart, dayTransactions) ->
                val income = dayTransactions
                    .asSequence()
                    .filter { it.amount > 0 }
                    .sumOf { it.amount }
                val expense = dayTransactions
                    .asSequence()
                    .filter { it.amount < 0 }
                    .sumOf { it.amount }
                val moodScoreSum = dayTransactions.sumOf { it.mood ?: 0 }
                val moodCount = dayTransactions.count { it.mood != null }
                DailySnapshot(
                    dayStartMillis = dayStart,
                    transactions = dayTransactions.sortedByDescending(TransactionEntity::date),
                    income = income,
                    expense = expense,
                    moodScoreSum = moodScoreSum,
                    moodCount = moodCount
                )
            }.sortedByDescending(DailySnapshot::dayStartMillis)

            val totalIncome = monthTransactions
                .asSequence()
                .filter { it.amount > 0 }
                .sumOf { it.amount }
            val totalExpense = monthTransactions
                .asSequence()
                .filter { it.amount < 0 }
                .sumOf { it.amount }
            val moodScoreSum = monthTransactions.sumOf { it.mood ?: 0 }
            val moodCount = monthTransactions.count { it.mood != null }

            MonthlySnapshot(
                year = key.year,
                month = key.month,
                days = daySnapshots,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                moodScoreSum = moodScoreSum,
                moodCount = moodCount
            )
        }
    }

    private fun buildChartCache(
        transactions: List<TransactionEntity>
    ): ChartAnalyticsCache {
        if (transactions.isEmpty()) return ChartAnalyticsCache.EMPTY

        val expenseTransactions = transactions.filter { it.amount < 0 }
        val incomeTransactions = transactions.filter { it.amount > 0 }

        val optionsByPeriod = ChartPeriod.entries.associateWith { period ->
            val options = when (period) {
                ChartPeriod.Week -> buildWeekOptions(transactions)
                ChartPeriod.Month -> buildMonthOptions(transactions)
                ChartPeriod.Year -> buildYearOptions(transactions)
            }
            options.ifEmpty { listOf(buildFallbackOption(period)) }
        }

        val summaries = mutableMapOf<ChartSummaryKey, ChartSummarySnapshot>()
        val moodEntriesByRangeId = mutableMapOf<String, List<MoodChartEntry>>()

        optionsByPeriod.values.flatten().forEach { option ->
            val period = option.period
            val range = ChartDataCalculator.buildRange(
                period = period,
                startMillis = option.startInclusive,
                endMillis = option.endInclusive,
                stringProvider = stringProvider
            )

            val rangeExpense = transactionsInRange(expenseTransactions, range.start, range.end)
            val rangeIncome = transactionsInRange(incomeTransactions, range.start, range.end)

            val expenseSummary = ChartDataCalculator.summarize(
                transactions = rangeExpense,
                type = ChartType.Expense,
                range = range,
                topLimit = DEFAULT_TOP_RANK_LIMIT
            )
            val incomeSummary = ChartDataCalculator.summarize(
                transactions = rangeIncome,
                type = ChartType.Income,
                range = range,
                topLimit = DEFAULT_TOP_RANK_LIMIT
            )

            summaries[ChartSummaryKey(ChartType.Expense, option.id)] = expenseSummary.toSnapshot()
            summaries[ChartSummaryKey(ChartType.Income, option.id)] = incomeSummary.toSnapshot()

            val moodEntries = ChartDataCalculator.buildMoodEntries(transactions, range)
            moodEntriesByRangeId[option.id] = moodEntries
        }

        return ChartAnalyticsCache(
            optionsByPeriod = optionsByPeriod,
            summariesByKey = summaries,
            moodEntriesByRangeId = moodEntriesByRangeId
        )
    }

    private fun buildFallbackOption(period: ChartPeriod): ChartRangeOption {
        return when (period) {
            ChartPeriod.Week -> buildCurrentWeekOption()
            ChartPeriod.Month -> buildCurrentMonthOption()
            ChartPeriod.Year -> buildCurrentYearOption()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildDiscoverCache(
        transactions: List<TransactionEntity>
    ): DiscoverAnalyticsCache {
        if (transactions.isEmpty()) return DiscoverAnalyticsCache.EMPTY

        val now = Calendar.getInstance(Locale.getDefault())
        val monthStart = (now.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            setToStartOfDay()
        }
        val monthEnd = (monthStart.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            setToEndOfDay()
        }
        val monthTransactions = transactions.filter { entity ->
            entity.date in monthStart.timeInMillis..monthEnd.timeInMillis
        }
        val typeProfile = buildTypeProfile(monthTransactions)

        return DiscoverAnalyticsCache(
            typeProfile = TypeProfileSnapshot(
                year = monthStart.get(Calendar.YEAR),
                month = monthStart.get(Calendar.MONTH) + 1,
                profile = typeProfile
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildProfileStats(
        transactions: List<TransactionEntity>
    ): MeProfileStatsData {
        if (transactions.isEmpty()) {
            return MeProfileStatsData(isLoading = false)
        }

        val zoneId = ZoneId.systemDefault()
        val transactionDates = transactions
            .map { Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() }
        val distinctDays = transactionDates.toSet().size
        val totalTransactions = transactions.size

        val dateSet = transactionDates.toHashSet()
        var consecutiveDays = 0
        var current = LocalDate.now(zoneId)
        while (dateSet.contains(current)) {
            consecutiveDays++
            current = current.minusDays(1)
        }

        return MeProfileStatsData(
            consecutiveCheckInDays = consecutiveDays,
            totalActiveDays = distinctDays,
            totalTransactions = totalTransactions,
            isLoading = false
        )
    }

    private fun buildTypeProfile(
        transactions: List<TransactionEntity>
    ): TypeProfile {
        if (transactions.isEmpty()) return TypeProfile()

        var expenseTotal = 0.0
        var incomeTotal = 0.0
        var expenseCount = 0
        var incomeCount = 0

        transactions.forEach { entity ->
            when {
                entity.amount < 0 -> {
                    expenseTotal += abs(entity.amount)
                    expenseCount++
                }
                entity.amount > 0 -> {
                    incomeTotal += entity.amount
                    incomeCount++
                }
            }
        }

        return TypeProfile(
            expenseTotal = expenseTotal,
            incomeTotal = incomeTotal,
            expenseCount = expenseCount,
            incomeCount = incomeCount
        )
    }

    private fun buildWeekOptions(
        transactions: List<TransactionEntity>
    ): List<ChartRangeOption> {
        if (transactions.isEmpty()) return emptyList()

        val grouped = transactions.groupBy { entity ->
            val calendar = isoCalendar().apply { timeInMillis = entity.date }
            calendar.weekStartMillis()
        }

        val nowCalendar = isoCalendar().apply { timeInMillis = System.currentTimeMillis() }
        val nowWeekStart = nowCalendar.weekStartMillis()

        return grouped.map { (startMillis, _) ->
            val startCal = isoCalendar().apply { timeInMillis = startMillis }
            val weekOfYear = startCal.get(Calendar.WEEK_OF_YEAR)
            val weekYear = startCal.isoWeekYear()
            val endMillis = (startCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, DAYS_IN_WEEK - 1)
                setToEndOfDay()
            }.timeInMillis

            val diffWeeks = max(
                0,
                ((nowWeekStart - startMillis) / MILLIS_IN_DAY / DAYS_IN_WEEK).toInt()
            )
            val label = when {
                diffWeeks == 0 -> stringProvider.getString(
                    R.string.chart_tab_week_this
                )
                diffWeeks == 1 -> stringProvider.getString(
                    R.string.chart_tab_week_last
                )
                weekYear == nowCalendar.isoWeekYear() -> stringProvider.getString(
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
        }.sortedByDescending(ChartRangeOption::startInclusive)
    }

    private fun buildMonthOptions(
        transactions: List<TransactionEntity>
    ): List<ChartRangeOption> {
        if (transactions.isEmpty()) return emptyList()

        val grouped = transactions.groupBy { entity ->
            val calendar = Calendar.getInstance(Locale.getDefault()).apply {
                timeInMillis = entity.date
            }
            YearMonthKey(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH)
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
                (now.get(Calendar.YEAR) - key.year) * MONTHS_IN_YEAR +
                    (now.get(Calendar.MONTH) - key.month)
            val label = when {
                diffMonths == 0 -> stringProvider.getString(
                    R.string.chart_tab_month_this
                )
                diffMonths == 1 -> stringProvider.getString(
                    R.string.chart_tab_month_last
                )
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
        }.sortedByDescending(ChartRangeOption::startInclusive)
    }

    private fun buildCurrentWeekOption(): ChartRangeOption {
        val calendar = isoCalendar().apply {
            timeInMillis = System.currentTimeMillis()
        }
        val startMillis = calendar.weekStartMillis()
        val endMillis = (calendar.clone() as Calendar).apply {
            timeInMillis = startMillis
            add(Calendar.DAY_OF_YEAR, DAYS_IN_WEEK - 1)
            setToEndOfDay()
        }.timeInMillis

        return ChartRangeOption(
            id = "week-current",
            period = ChartPeriod.Week,
            label = stringProvider.getString(R.string.chart_tab_week_this),
            startInclusive = startMillis,
            endInclusive = endMillis
        )
    }

    private fun buildCurrentMonthOption(): ChartRangeOption {
        val now = Calendar.getInstance(Locale.getDefault())
        val startCal = (now.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            setToStartOfDay()
        }
        val endCal = (startCal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            setToEndOfDay()
        }

        return ChartRangeOption(
            id = "month-current",
            period = ChartPeriod.Month,
            label = stringProvider.getString(R.string.chart_tab_month_this),
            startInclusive = startCal.timeInMillis,
            endInclusive = endCal.timeInMillis
        )
    }

    private fun buildCurrentYearOption(): ChartRangeOption {
        val now = Calendar.getInstance(Locale.getDefault())
        val startCal = (now.clone() as Calendar).apply {
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            setToStartOfDay()
        }
        val endCal = (startCal.clone() as Calendar).apply {
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 31)
            setToEndOfDay()
        }

        return ChartRangeOption(
            id = "year-current",
            period = ChartPeriod.Year,
            label = stringProvider.getString(R.string.chart_tab_year_this),
            startInclusive = startCal.timeInMillis,
            endInclusive = endCal.timeInMillis
        )
    }

    private fun buildYearOptions(
        transactions: List<TransactionEntity>
    ): List<ChartRangeOption> {
        if (transactions.isEmpty()) return emptyList()

        val grouped = transactions.groupBy { entity ->
            val calendar = Calendar.getInstance(Locale.getDefault()).apply {
                timeInMillis = entity.date
            }
            calendar.get(Calendar.YEAR)
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

            val label = when (year) {
                nowYear -> stringProvider.getString(
                    R.string.chart_tab_year_this
                )
                nowYear - 1 -> stringProvider.getString(
                    R.string.chart_tab_year_last
                )
                else -> stringProvider.getString(
                    R.string.chart_tab_year_value,
                    year
                )
            }

            ChartRangeOption(
                id = "year-$year",
                period = ChartPeriod.Year,
                label = label,
                startInclusive = startCal.timeInMillis,
                endInclusive = endCal.timeInMillis
            )
        }.sortedByDescending(ChartRangeOption::startInclusive)
    }

    private fun transactionsInRange(
        transactions: List<TransactionEntity>,
        start: Long,
        end: Long
    ): List<TransactionEntity> {
        if (transactions.isEmpty()) return emptyList()
        val result = ArrayList<TransactionEntity>()
        transactions.forEach { entity ->
            if (entity.date in start..end) {
                result.add(entity)
            }
        }
        return result
    }

    private fun dayStartMillis(timestamp: Long): Long {
        return (timestamp / MILLIS_IN_DAY) * MILLIS_IN_DAY
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

    private fun isoCalendar(): Calendar {
        return GregorianCalendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
        }
    }

    private fun Calendar.weekStartMillis(): Long {
        val calendar = (clone() as Calendar).apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            setToStartOfDay()
        }
        return calendar.timeInMillis
    }

    private fun Calendar.isoWeekYear(): Int {
        return get(Calendar.YEAR) + when {
            get(Calendar.MONTH) == Calendar.JANUARY && get(Calendar.WEEK_OF_YEAR) >= 52 -> -1
            get(Calendar.MONTH) == Calendar.DECEMBER && get(Calendar.WEEK_OF_YEAR) == 1 -> 1
            else -> 0
        }
    }

    data class YearMonthKey(
        val year: Int,
        val month: Int
    ) : Comparable<YearMonthKey> {
        override fun compareTo(other: YearMonthKey): Int {
            return when {
                year != other.year -> year - other.year
                else -> month - other.month
            }
        }
    }

    data class DailySnapshot(
        val dayStartMillis: Long,
        val transactions: List<TransactionEntity>,
        val income: Double,
        val expense: Double,
        val moodScoreSum: Int,
        val moodCount: Int
    )

    data class MonthlySnapshot(
        val year: Int,
        val month: Int,
        val days: List<DailySnapshot>,
        val totalIncome: Double,
        val totalExpense: Double,
        val moodScoreSum: Int,
        val moodCount: Int
    ) {
        val averageMood: Int?
            get() = if (moodCount > 0) moodScoreSum / moodCount else null

        companion object {
            fun empty(year: Int, zeroBasedMonth: Int): MonthlySnapshot {
                return MonthlySnapshot(
                    year = year,
                    month = zeroBasedMonth,
                    days = emptyList(),
                    totalIncome = 0.0,
                    totalExpense = 0.0,
                    moodScoreSum = 0,
                    moodCount = 0
                )
            }
        }
    }

    data class ChartSummaryKey(
        val type: ChartType,
        val rangeId: String
    )

    data class ChartSummarySnapshot(
        val entries: List<ChartEntry>,
        val total: Double,
        val average: Double,
        val categoryRanks: List<ChartCategoryRank>
    )

    data class ChartAnalyticsCache(
        val optionsByPeriod: Map<ChartPeriod, List<ChartRangeOption>>,
        val summariesByKey: Map<ChartSummaryKey, ChartSummarySnapshot>,
        val moodEntriesByRangeId: Map<String, List<MoodChartEntry>>
    ) {
        companion object {
            val EMPTY = ChartAnalyticsCache(
                optionsByPeriod = emptyMap(),
                summariesByKey = emptyMap(),
                moodEntriesByRangeId = emptyMap()
            )
        }
    }

    data class TypeProfileSnapshot(
        val year: Int,
        val month: Int,
        val profile: TypeProfile
    )

    data class DiscoverAnalyticsCache(
        val typeProfile: TypeProfileSnapshot
    ) {
        companion object {
            val EMPTY = DiscoverAnalyticsCache(
                typeProfile = TypeProfileSnapshot(
                    year = Calendar.getInstance().get(Calendar.YEAR),
                    month = Calendar.getInstance().get(Calendar.MONTH) + 1,
                    profile = TypeProfile()
                )
            )
        }
    }

    data class MeProfileStatsData(
        val consecutiveCheckInDays: Int = 0,
        val totalActiveDays: Int = 0,
        val totalTransactions: Int = 0,
        val isLoading: Boolean = true
    )

    companion object {
        private const val DEFAULT_TOP_RANK_LIMIT = 5
        private const val MILLIS_IN_DAY = 86_400_000L
        private const val DAYS_IN_WEEK = 7
        private const val MONTHS_IN_YEAR = 12

    }
}
