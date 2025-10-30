package com.evening.dailylife.feature.me.settings.quickusage.scheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.evening.dailylife.core.data.preferences.PreferencesManager
import com.evening.dailylife.feature.me.settings.quickusage.scheduler.QuickUsageReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QuickUsageReminderBootReceiver : BroadcastReceiver() {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var reminderScheduler: QuickUsageReminderScheduler

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                if (preferencesManager.quickUsageReminderEnabled.value) {
                    reminderScheduler.scheduleReminder(
                        preferencesManager.quickUsageReminderTimeMinutes.value,
                    )
                }
            }
        }
    }
}
