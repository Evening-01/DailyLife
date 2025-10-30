package com.evening.dailylife.feature.me.settings.quickusage.scheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.evening.dailylife.core.data.preferences.PreferencesManager
import com.evening.dailylife.feature.me.settings.quickusage.scheduler.QuickUsageReminderNotificationHelper
import com.evening.dailylife.feature.me.settings.quickusage.scheduler.QuickUsageReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QuickUsageReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var reminderScheduler: QuickUsageReminderScheduler

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != QuickUsageReminderScheduler.ACTION_QUICK_USAGE_REMINDER) {
            return
        }

        if (!preferencesManager.quickUsageReminderEnabled.value) {
            reminderScheduler.cancelReminder()
            return
        }

        val reminderMinutes = preferencesManager.quickUsageReminderTimeMinutes.value
        QuickUsageReminderNotificationHelper.showReminder(context, reminderMinutes)
        reminderScheduler.scheduleReminder(reminderMinutes)
    }
}
