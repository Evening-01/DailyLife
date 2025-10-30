package com.evening.dailylife.feature.me.settings.quickusage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.core.data.preferences.PreferencesManager
import com.evening.dailylife.core.data.preferences.QuickUsageReminderDefaults
import com.evening.dailylife.feature.me.settings.quickusage.scheduler.QuickUsageReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuickUsageUiState(
    val reminderEnabled: Boolean = false,
    val reminderTimeMinutes: Int = QuickUsageReminderDefaults.DEFAULT_TIME_MINUTES,
)

@HiltViewModel
class QuickUsageViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val reminderScheduler: QuickUsageReminderScheduler,
) : ViewModel() {

    private val _events = MutableSharedFlow<QuickUsageEvent>()
    val events = _events.asSharedFlow()

    val uiState: StateFlow<QuickUsageUiState> = combine(
        preferencesManager.quickUsageReminderEnabled,
        preferencesManager.quickUsageReminderTimeMinutes,
    ) { enabled, minutes ->
        QuickUsageUiState(
            reminderEnabled = enabled,
            reminderTimeMinutes = minutes,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = QuickUsageUiState(
            reminderEnabled = preferencesManager.quickUsageReminderEnabled.value,
            reminderTimeMinutes = preferencesManager.quickUsageReminderTimeMinutes.value,
        ),
    )

    fun enableReminder() {
        viewModelScope.launch {
            preferencesManager.setQuickUsageReminderEnabled(true)
            val scheduled = reminderScheduler.scheduleReminder(
                preferencesManager.quickUsageReminderTimeMinutes.value,
            )
            if (!scheduled) {
                preferencesManager.setQuickUsageReminderEnabled(false)
                _events.emit(QuickUsageEvent.ExactAlarmPermissionRequired)
            }
        }
    }

    fun disableReminder() {
        viewModelScope.launch {
            preferencesManager.setQuickUsageReminderEnabled(false)
            reminderScheduler.cancelReminder()
        }
    }

    fun updateReminderTime(minutes: Int) {
        viewModelScope.launch {
            val normalized = minutes.coerceIn(0, QuickUsageReminderDefaults.MINUTES_PER_DAY - 1)
            preferencesManager.setQuickUsageReminderTimeMinutes(normalized)
            if (preferencesManager.quickUsageReminderEnabled.value) {
                val scheduled = reminderScheduler.scheduleReminder(normalized)
                if (!scheduled) {
                    preferencesManager.setQuickUsageReminderEnabled(false)
                    _events.emit(QuickUsageEvent.ExactAlarmPermissionRequired)
                }
            }
        }
    }

    fun resetReminderTime() {
        viewModelScope.launch {
            preferencesManager.setQuickUsageReminderTimeMinutes(QuickUsageReminderDefaults.DEFAULT_TIME_MINUTES)
            if (preferencesManager.quickUsageReminderEnabled.value) {
                val scheduled = reminderScheduler.scheduleReminder(QuickUsageReminderDefaults.DEFAULT_TIME_MINUTES)
                if (!scheduled) {
                    preferencesManager.setQuickUsageReminderEnabled(false)
                    _events.emit(QuickUsageEvent.ExactAlarmPermissionRequired)
                }
            }
        }
    }
}

sealed class QuickUsageEvent {
    data object ExactAlarmPermissionRequired : QuickUsageEvent()
}
