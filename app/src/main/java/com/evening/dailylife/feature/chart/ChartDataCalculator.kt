package com.evening.dailylife.feature.chart

import com.evening.dailylife.R
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.util.StringProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

internal object ChartDataCalculator {

    data class Range(
        val start: Long,
        val end: Long,
        val buckets: List<Bucket>
    )

    data class Bucket(
        val label: String,
        val start: Long,
        val end: Long
    )

    data class Summary(
        val entries: List<ChartEntry>,
        val total: Double,
        val average: Double,
        val categoryRanks: List<ChartCategoryRank>
    )

    fun buildRange(
        period: ChartPeriod,
        startMillis: Long,
        endMillis: Long,
        stringProvider: StringProvider
    ): Range {
        val rangeStart = Calendar.getInstance().apply {
            timeInMillis = startMillis
            setToStartOfDay()
        }
        val rangeEnd = Calendar.getInstance().apply {
            timeInMillis = endMillis
            setToEndOfDay()
        }
        return when (period) {
            ChartPeriod.Week -> buildWeekRange(rangeStart, rangeEnd)
            ChartPeriod.Month -> buildMonthRange(rangeStart, rangeEnd, stringProvider)
            ChartPeriod.Year -> buildYearRange(rangeStart, rangeEnd, stringProvider)
        }
    }

    fun summarize(
        transactions: List<TransactionEntity>,
        type: ChartType,
        range: Range,
        topLimit: Int
    ): Summary {
        val filtered = transactions.filter {
            when (type) {
                ChartType.Expense -> it.amount < 0
                ChartType.Income -> it.amount > 0
            }
        }

        val normalised = filtered.map { transaction ->
            if (type == ChartType.Expense) abs(transaction.amount) else transaction.amount
        }

        val entries = range.buckets.map { bucket ->
            val bucketTotal = filtered
                .filter { it.date in bucket.start..bucket.end }
                .sumOf { transaction ->
                    if (type == ChartType.Expense) abs(transaction.amount) else transaction.amount
                }

            ChartEntry(
                label = bucket.label,
                value = bucketTotal.toFloat()
            )
        }

        val total = normalised.sumOf { it }
        val average = if (range.buckets.isEmpty()) 0.0 else total / range.buckets.size
        val categoryRanks = buildCategoryRanks(
            transactions = filtered,
            type = type,
            total = total,
            topLimit = topLimit
        )

        return Summary(
            entries = entries,
            total = total,
            average = average,
            categoryRanks = categoryRanks
        )
    }

    fun buildMoodEntries(
        transactions: List<TransactionEntity>,
        range: Range
    ): List<MoodChartEntry> {
        if (range.buckets.isEmpty()) return emptyList()

        val moodTransactions = transactions.filter { entity ->
            entity.mood != null && entity.date in range.start..range.end
        }

        if (moodTransactions.isEmpty()) {
            return range.buckets.map { bucket ->
                MoodChartEntry(label = bucket.label, value = null)
            }
        }

        val bucketMoodValues = range.buckets.associate { bucket ->
            bucket.start to mutableListOf<Int>()
        }

        moodTransactions.forEach { entity ->
            val bucket = range.buckets.firstOrNull { bucket ->
                entity.date in bucket.start..bucket.end
            }
            if (bucket != null) {
                bucketMoodValues[bucket.start]?.add(entity.mood!!)
            }
        }

        return range.buckets.map { bucket ->
            val moods = bucketMoodValues[bucket.start].orEmpty()
            val average = if (moods.isEmpty()) {
                null
            } else {
                moods.sum().toFloat() / moods.size
            }
            MoodChartEntry(
                label = bucket.label,
                value = average
            )
        }
    }

    private fun buildCategoryRanks(
        transactions: List<TransactionEntity>,
        type: ChartType,
        total: Double,
        topLimit: Int
    ): List<ChartCategoryRank> {
        if (transactions.isEmpty() || total <= 0.0 || topLimit <= 0) {
            return emptyList()
        }

        val amounts = transactions.groupBy { it.category }
            .mapValues { (_, list) ->
                list.sumOf { entity ->
                    when (type) {
                        ChartType.Expense -> abs(entity.amount)
                        ChartType.Income -> entity.amount
                    }
                }
            }
            .filterValues { it > 0.0 }

        return amounts.entries
            .sortedByDescending { it.value }
            .take(topLimit)
            .map { (category, amount) ->
                val ratio = if (total > 0) (amount / total).toFloat() else 0f
                ChartCategoryRank(
                    category = category,
                    amount = amount,
                    ratio = ratio.coerceIn(0f, 1f)
                )
            }
    }

    fun roundUpToNiceNumber(value: Float): Float {
        if (value <= 0f) return 0f
        val magnitude = 10.0.pow(floor(log10(value.toDouble()))).toFloat()
        val normalized = value / magnitude
        val niceNormalized = when {
            normalized <= 1f -> 1f
            normalized <= 2f -> 2f
            normalized <= 5f -> 5f
            else -> 10f
        }
        return niceNormalized * magnitude
    }

    private fun buildWeekRange(
        rangeStart: Calendar,
        rangeEnd: Calendar
    ): Range {
        val startDay = (rangeStart.clone() as Calendar).apply { setToStartOfDay() }
        val dateFormat = SimpleDateFormat("MM-dd", Locale.CHINA)
        val buckets = (0 until DAYS_IN_WEEK).map { offset ->
            val dayStart = (startDay.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
                setToStartOfDay()
            }
            val dayEnd = (dayStart.clone() as Calendar).apply { setToEndOfDay() }
            Bucket(
                label = dateFormat.format(dayStart.time),
                start = dayStart.timeInMillis,
                end = minOf(dayEnd.timeInMillis, rangeEnd.timeInMillis)
            )
        }
        val endMillis = minOf(
            (startDay.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, DAYS_IN_WEEK - 1)
                setToEndOfDay()
            }.timeInMillis,
            rangeEnd.timeInMillis
        )
        return Range(
            start = startDay.timeInMillis,
            end = endMillis,
            buckets = buckets
        )
    }

    private fun buildMonthRange(
        rangeStart: Calendar,
        rangeEnd: Calendar,
        stringProvider: StringProvider
    ): Range {
        val monthStart = (rangeStart.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            setToStartOfDay()
        }
        val monthEnd = (rangeEnd.clone() as Calendar).apply { setToEndOfDay() }
        val buckets = mutableListOf<Bucket>()
        val cursor = (monthStart.clone() as Calendar)
        while (cursor.timeInMillis <= monthEnd.timeInMillis) {
            val dayStart = (cursor.clone() as Calendar).apply { setToStartOfDay() }
            val dayEnd = (cursor.clone() as Calendar).apply { setToEndOfDay() }
            buckets += Bucket(
                label = stringProvider.getString(
                    R.string.chart_label_day,
                    dayStart.get(Calendar.DAY_OF_MONTH)
                ),
                start = dayStart.timeInMillis,
                end = minOf(dayEnd.timeInMillis, monthEnd.timeInMillis)
            )
            cursor.add(Calendar.DAY_OF_MONTH, 1)
        }
        return Range(
            start = monthStart.timeInMillis,
            end = monthEnd.timeInMillis,
            buckets = buckets
        )
    }

    private fun buildYearRange(
        rangeStart: Calendar,
        rangeEnd: Calendar,
        stringProvider: StringProvider
    ): Range {
        val yearStart = (rangeStart.clone() as Calendar).apply {
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            setToStartOfDay()
        }
        val yearEnd = (rangeEnd.clone() as Calendar).apply { setToEndOfDay() }
        val buckets = mutableListOf<Bucket>()
        val cursor = (yearStart.clone() as Calendar)
        while (cursor.timeInMillis <= yearEnd.timeInMillis) {
            val monthStart = (cursor.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, 1)
                setToStartOfDay()
            }
            val monthEnd = (monthStart.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                setToEndOfDay()
            }
            buckets += Bucket(
                label = stringProvider.getString(
                    R.string.chart_label_month,
                    monthStart.get(Calendar.MONTH) + 1
                ),
                start = monthStart.timeInMillis,
                end = minOf(monthEnd.timeInMillis, yearEnd.timeInMillis)
            )
            cursor.add(Calendar.MONTH, 1)
        }
        return Range(
            start = yearStart.timeInMillis,
            end = yearEnd.timeInMillis,
            buckets = buckets
        )
    }
}

private const val DAYS_IN_WEEK = 7

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
