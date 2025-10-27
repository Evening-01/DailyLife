package com.evening.dailylife.feature.discover

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.feature.discover.model.DiscoverHeatMapEntry
import com.evening.dailylife.feature.discover.model.DiscoverHeatMapUiState
import com.evening.dailylife.feature.discover.model.DiscoverTypeProfileUiState
import com.evening.dailylife.feature.discover.model.TypeProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    companion object {
        private const val HEAT_MAP_MONTH_SPAN = 12L
    }

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val heatMapRange = HeatMapRange.create(zoneId = zoneId)

    private val initialHeatMapState = DiscoverHeatMapUiState(
        startDate = heatMapRange.startDate,
        endDate = heatMapRange.endDate,
        isLoading = true,
    )

    private val _heatMapState = MutableStateFlow(initialHeatMapState)
    val heatMapUiState: StateFlow<DiscoverHeatMapUiState> = _heatMapState.asStateFlow()

    private val _typeProfileState = MutableStateFlow(DiscoverTypeProfileUiState())
    val typeProfileState: StateFlow<DiscoverTypeProfileUiState> = _typeProfileState.asStateFlow()
    private val transactionsState = transactionRepository.observeAllTransactions()

    init {
        val cachedTransactions = transactionsState.value
        if (cachedTransactions != null) {
            _typeProfileState.value = buildTypeProfileState(cachedTransactions)
            _heatMapState.value = buildHeatMapState(cachedTransactions)
        } else {
            _typeProfileState.value = _typeProfileState.value.copy(isLoading = true)
            _heatMapState.value = _heatMapState.value.copy(isLoading = true)
        }

        viewModelScope.launch {
            transactionsState
                .filterNotNull()
                .mapLatest { transactions ->
                    withContext(Dispatchers.Default) {
                        buildTypeProfileState(transactions) to buildHeatMapState(transactions)
                    }
                }
                .collectLatest { (typeProfile, heatMap) ->
                    _typeProfileState.value = typeProfile
                    _heatMapState.value = heatMap
                }
        }
    }

    private fun buildTypeProfileState(
        transactions: List<TransactionEntity>
    ): DiscoverTypeProfileUiState {
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

        return DiscoverTypeProfileUiState(
            typeProfile = buildTypeProfile(monthTransactions),
            isLoading = false,
            year = monthStart.get(Calendar.YEAR),
            month = monthStart.get(Calendar.MONTH) + 1
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildHeatMapState(
        transactions: List<TransactionEntity>,
    ): DiscoverHeatMapUiState {
        val transactionsByDate = transactions
            .asSequence()
            .filter { entity ->
                entity.date in heatMapRange.startMillis..heatMapRange.endMillis
            }
            .groupBy { entity ->
                Instant.ofEpochMilli(entity.date)
                    .atZone(zoneId)
                    .toLocalDate()
            }

        val contributions = LinkedHashMap<LocalDate, DiscoverHeatMapEntry>()
        var currentDate = heatMapRange.startDate
        while (!currentDate.isAfter(heatMapRange.endDate)) {
            val dayTransactions = transactionsByDate[currentDate]
            val entry = if (dayTransactions != null && dayTransactions.isNotEmpty()) {
                var totalIncome = 0.0
                var totalExpense = 0.0
                var moodScoreSum = 0
                var moodCount = 0

                dayTransactions.forEach { entity ->
                    val amount = entity.amount
                    if (amount > 0) {
                        totalIncome += amount
                    } else if (amount < 0) {
                        totalExpense += amount
                    }
                    entity.mood?.let { mood ->
                        moodScoreSum += mood
                        moodCount++
                    }
                }

                DiscoverHeatMapEntry(
                    transactionCount = dayTransactions.size,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    moodScoreSum = moodScoreSum,
                    moodCount = moodCount,
                )
            } else {
                DiscoverHeatMapEntry(
                    transactionCount = 0,
                    totalIncome = 0.0,
                    totalExpense = 0.0,
                    moodScoreSum = 0,
                    moodCount = 0,
                )
            }
            contributions[currentDate] = entry
            currentDate = currentDate.plusDays(1)
        }

        return DiscoverHeatMapUiState(
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

@RequiresApi(Build.VERSION_CODES.O)
private fun LocalDate.startOfDayMillis(zoneId: ZoneId): Long =
    atStartOfDay(zoneId).toInstant().toEpochMilli()

@RequiresApi(Build.VERSION_CODES.O)
private fun LocalDate.endOfDayMillis(zoneId: ZoneId): Long =
    atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
