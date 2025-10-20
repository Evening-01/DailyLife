package com.evening.dailylife.feature.chart

import com.evening.dailylife.R
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.util.StringProvider
import com.evening.dailylife.feature.chart.model.ChartCategoryRank
import com.evening.dailylife.feature.chart.model.ChartEntry
import com.evening.dailylife.feature.chart.model.ChartPeriod
import com.evening.dailylife.feature.chart.model.ChartType
import com.evening.dailylife.feature.chart.model.MoodChartEntry
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
        val buckets = range.buckets
        if (buckets.isEmpty()) {
            return Summary(
                entries = emptyList(),
                total = 0.0,
                average = 0.0,
                categoryRanks = emptyList()
            )
        }

        if (transactions.isEmpty()) {
            return Summary(
                entries = buckets.map { bucket ->
                    ChartEntry(label = bucket.label, value = 0f)
                },
                total = 0.0,
                average = 0.0,
                categoryRanks = emptyList()
            )
        }

        val bucketTotals = DoubleArray(buckets.size)
        val categoryTotals = mutableMapOf<String, Double>()
        var bucketIndex = 0
        var total = 0.0

        for (transaction in transactions) {
            val normalized = when (type) {
                ChartType.Expense -> if (transaction.amount < 0) abs(transaction.amount) else continue
                ChartType.Income -> if (transaction.amount > 0) transaction.amount else continue
            }

            val date = transaction.date
            if (date < range.start) {
                continue
            }
            if (date > range.end) {
                break
            }

            while (bucketIndex < buckets.lastIndex && date > buckets[bucketIndex].end) {
                bucketIndex++
            }
            if (bucketIndex >= buckets.size) {
                break
            }

            if (date in buckets[bucketIndex].start..buckets[bucketIndex].end) {
                bucketTotals[bucketIndex] += normalized
            }

            categoryTotals[transaction.category] =
                (categoryTotals[transaction.category] ?: 0.0) + normalized
            total += normalized
        }

        val entries = buckets.mapIndexed { index, bucket ->
            ChartEntry(
                label = bucket.label,
                value = bucketTotals[index].toFloat()
            )
        }

        val average = if (buckets.isEmpty()) 0.0 else total / buckets.size
        val categoryRanks = if (total <= 0.0 || categoryTotals.isEmpty() || topLimit <= 0) {
            emptyList()
        } else {
            categoryTotals.entries
                .sortedByDescending { it.value }
                .take(topLimit)
                .map { (category, amount) ->
                    val ratio = (amount / total).toFloat().coerceIn(0f, 1f)
                    ChartCategoryRank(
                        category = category,
                        amount = amount,
                        ratio = ratio
                    )
                }
        }

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

        val buckets = range.buckets
        val moodSums = FloatArray(buckets.size)
        val moodCounts = IntArray(buckets.size)

        val sortedMoods = moodTransactions.sortedBy(TransactionEntity::date)
        var bucketIndex = 0

        sortedMoods.forEach { entity ->
            val date = entity.date
            while (bucketIndex < buckets.lastIndex && date > buckets[bucketIndex].end) {
                bucketIndex++
            }
            val bucket = buckets.getOrNull(bucketIndex)
            val moodValue = entity.mood
            if (bucket != null && moodValue != null && date in bucket.start..bucket.end) {
                moodSums[bucketIndex] += moodValue
                moodCounts[bucketIndex] += 1
            }
        }

        return buckets.mapIndexed { index, bucket ->
            val count = moodCounts[index]
            val average = if (count == 0) null else moodSums[index] / count
            MoodChartEntry(
                label = bucket.label,
                value = average
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
        val locale = Locale.getDefault()
        val dateFormat = SimpleDateFormat("MM-dd", locale)
        val weekDayFormat = SimpleDateFormat("E", locale)
        val buckets = (0 until DAYS_IN_WEEK).map { offset ->
            val dayStart = (startDay.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
                setToStartOfDay()
            }
            val dayEnd = (dayStart.clone() as Calendar).apply { setToEndOfDay() }
            val dateLabel = dateFormat.format(dayStart.time)
            val weekLabel = weekDayFormat.format(dayStart.time)
            Bucket(
                label = "$weekLabel $dateLabel",
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
