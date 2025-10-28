package com.evening.dailylife.feature.me

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.core.data.analytics.TransactionAnalyticsRepository
import com.evening.dailylife.core.data.analytics.TransactionAnalyticsRepository.MeProfileStatsData
import com.evening.dailylife.core.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class MeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    analyticsRepository: TransactionAnalyticsRepository,
) : ViewModel() {

    val fingerprintLockEnabled = preferencesManager.fingerprintLockEnabled

    private val profileStatsFlow = analyticsRepository.profileStats()

    val profileStatsState = profileStatsFlow
        .map { data -> data.toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = profileStatsFlow.value.toUiState(isLoading = true),
        )

    fun setFingerprintLockEnabled(enabled: Boolean) {
        preferencesManager.setFingerprintLockEnabled(enabled)
    }
}

private fun MeProfileStatsData.toUiState(
    isLoading: Boolean = false
): MeProfileStatsUiState {
    return MeProfileStatsUiState(
        consecutiveCheckInDays = consecutiveCheckInDays,
        totalActiveDays = totalActiveDays,
        totalTransactions = totalTransactions,
        isLoading = isLoading || this.isLoading,
    )
}

data class MeProfileStatsUiState(
    val consecutiveCheckInDays: Int = 0,
    val totalActiveDays: Int = 0,
    val totalTransactions: Int = 0,
    val isLoading: Boolean = true,
)
