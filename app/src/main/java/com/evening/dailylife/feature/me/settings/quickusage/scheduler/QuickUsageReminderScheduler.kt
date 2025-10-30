package com.evening.dailylife.feature.me.settings.quickusage.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.evening.dailylife.core.data.preferences.QuickUsageReminderDefaults
import com.evening.dailylife.feature.me.settings.quickusage.scheduler.QuickUsageReminderNotificationHelper
import com.evening.dailylife.feature.me.settings.quickusage.scheduler.receiver.QuickUsageReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuickUsageReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(minutesOfDay: Int): Boolean {
        val normalized = minutesOfDay.coerceIn(0, QuickUsageReminderDefaults.MINUTES_PER_DAY - 1)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return false
            }
        }
        val triggerAtMillis = calculateTriggerTimeMillis(normalized)
        QuickUsageReminderNotificationHelper.ensureChannel(context)
        val pendingIntent = reminderPendingIntent()
        alarmManager.cancel(pendingIntent)

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }
            else -> {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }
        }
        return true
    }

    fun cancelReminder() {
        alarmManager.cancel(reminderPendingIntent())
    }

    private fun reminderPendingIntent(): PendingIntent {
        val intent = Intent(context, QuickUsageReminderReceiver::class.java).apply {
            action = ACTION_QUICK_USAGE_REMINDER
        }
        val baseFlags = PendingIntent.FLAG_UPDATE_CURRENT
        val mutabilityFlag = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PendingIntent.FLAG_MUTABLE
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> PendingIntent.FLAG_IMMUTABLE
            else -> 0
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            baseFlags or mutabilityFlag,
        )
    }

    private fun calculateTriggerTimeMillis(minutesOfDay: Int): Long {
        val now = System.currentTimeMillis()
        val hour = minutesOfDay / 60
        val minute = minutesOfDay % 60
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }

    companion object {
        private const val REQUEST_CODE = 1003
        const val ACTION_QUICK_USAGE_REMINDER = "com.evening.dailylife.ACTION_QUICK_USAGE_REMINDER"
    }
}
