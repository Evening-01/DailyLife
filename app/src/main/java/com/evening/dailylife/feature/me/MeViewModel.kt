package com.evening.dailylife.feature.me

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.core.data.preferences.PreferencesManager
import com.evening.dailylife.core.data.preferences.ThemeMode
import com.evening.dailylife.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class MeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    val themeMode = preferencesManager.themeMode
    val dynamicColor = preferencesManager.dynamicColor
    val fingerprintLockEnabled = preferencesManager.fingerprintLockEnabled

    private val zoneId = ZoneId.systemDefault()

    val profileStatsState = transactionRepository
        .observeAllTransactions()
        .mapLatest { transactions ->
            if (transactions == null) {
                MeProfileStatsUiState()
            } else {
                val transactionDates = transactions
                    .map { Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() }

                val distinctDayCount = transactionDates
                    .asSequence()
                    .distinct()
                    .count()

                val totalTransactionCount = transactions.size

                val transactionDateSet = transactionDates.toHashSet()
                var consecutiveDays = 0
                var currentDate = LocalDate.now(zoneId)
                while (transactionDateSet.contains(currentDate)) {
                    consecutiveDays += 1
                    currentDate = currentDate.minusDays(1)
                }

                MeProfileStatsUiState(
                    consecutiveCheckInDays = consecutiveDays,
                    totalActiveDays = distinctDayCount,
                    totalTransactions = totalTransactionCount,
                    isLoading = false,
                )
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MeProfileStatsUiState(),
        )

    fun setThemeMode(themeMode: ThemeMode) {
        preferencesManager.setThemeMode(themeMode)
    }

    fun setDynamicColor(enabled: Boolean) {
        preferencesManager.setDynamicColor(enabled)
    }

    fun setFingerprintLockEnabled(enabled: Boolean) {
        preferencesManager.setFingerprintLockEnabled(enabled)
    }
}

data class MeProfileStatsUiState(
    val consecutiveCheckInDays: Int = 0,
    val totalActiveDays: Int = 0,
    val totalTransactions: Int = 0,
    val isLoading: Boolean = true,
)
