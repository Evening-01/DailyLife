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
import kotlin.math.max
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
        val average: Double
    )

    fun buildRange(
        period: ChartPeriod,
        stringProvider: StringProvider,
        reference: Calendar = Calendar.getInstance()
    ): Range {
        val todayEnd = (reference.clone() as Calendar).apply { setToEndOfDay() }
        return when (period) {
            ChartPeriod.Week -> buildWeekRange(todayEnd)
            ChartPeriod.Month -> buildMonthRange(todayEnd, stringProvider)
            ChartPeriod.Year -> buildYearRange(todayEnd, stringProvider)
        }
    }

    fun summarize(
        transactions: List<TransactionEntity>,
        type: ChartType,
        range: Range
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

        return Summary(
            entries = entries,
            total = total,
            average = average
        )
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

    private fun buildWeekRange(todayEnd: Calendar): Range {
        val startDay = (todayEnd.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -6)
            setToStartOfDay()
        }
        val dateFormat = SimpleDateFormat("E", Locale.CHINA)
        val buckets = (0 until 7).map { offset ->
            val dayStart = (startDay.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
            }
            val dayEnd = (dayStart.clone() as Calendar).apply { setToEndOfDay() }
            Bucket(
                label = dateFormat.format(dayStart.time),
                start = dayStart.timeInMillis,
                end = dayEnd.timeInMillis
            )
        }
        return Range(
            start = buckets.first().start,
            end = todayEnd.timeInMillis,
            buckets = buckets
        )
    }

    private fun buildMonthRange(todayEnd: Calendar, stringProvider: StringProvider): Range {
        val monthStart = (todayEnd.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            setToStartOfDay()
        }
        val totalDays = max(1, todayEnd.get(Calendar.DAY_OF_MONTH))
        val buckets = (0 until totalDays).map { offset ->
            val dayStart = (monthStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, offset) }
            val dayEnd = (dayStart.clone() as Calendar).apply { setToEndOfDay() }
            Bucket(
                label = stringProvider.getString(
                    R.string.chart_label_day,
                    dayStart.get(Calendar.DAY_OF_MONTH)
                ),
                start = dayStart.timeInMillis,
                end = dayEnd.timeInMillis
            )
        }
        return Range(
            start = monthStart.timeInMillis,
            end = todayEnd.timeInMillis,
            buckets = buckets
        )
    }

    private fun buildYearRange(todayEnd: Calendar, stringProvider: StringProvider): Range {
        val yearStart = (todayEnd.clone() as Calendar).apply {
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            setToStartOfDay()
        }
        val currentMonthIndex = todayEnd.get(Calendar.MONTH)
        val buckets = (0..currentMonthIndex).map { monthIndex ->
            val monthStart = (yearStart.clone() as Calendar).apply {
                set(Calendar.MONTH, monthIndex)
            }
            val monthEnd = (monthStart.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                setToEndOfDay()
            }
            Bucket(
                label = stringProvider.getString(R.string.chart_label_month, monthIndex + 1),
                start = monthStart.timeInMillis,
                end = minOf(monthEnd.timeInMillis, todayEnd.timeInMillis)
            )
        }
        return Range(
            start = yearStart.timeInMillis,
            end = todayEnd.timeInMillis,
            buckets = buckets
        )
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
