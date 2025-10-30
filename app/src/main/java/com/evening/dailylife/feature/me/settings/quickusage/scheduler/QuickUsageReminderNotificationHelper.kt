package com.evening.dailylife.feature.me.settings.quickusage.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.evening.dailylife.R
import com.evening.dailylife.app.main.MainActivity
import java.util.Locale

object QuickUsageReminderNotificationHelper {

    private const val CHANNEL_ID = "quick_usage_reminder"
    private const val NOTIFICATION_ID = 2005
    private const val CONTENT_REQUEST_CODE = 2010

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.quick_usage_reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.quick_usage_reminder_channel_description)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    fun showReminder(context: Context, reminderMinutes: Int) {
        ensureChannel(context)

        val formattedTime = formatReminderTime(reminderMinutes)
        val contentIntent = buildContentIntent(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.quick_usage_reminder_notification_title))
            .setContentText(
                context.getString(
                    R.string.quick_usage_reminder_notification_text,
                    formattedTime,
                )
            )
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun buildContentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(intent)
            .getPendingIntent(
                CONTENT_REQUEST_CODE,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )!!
    }

    private fun formatReminderTime(minutes: Int): String {
        val hour = minutes / 60
        val minute = minutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }
}
