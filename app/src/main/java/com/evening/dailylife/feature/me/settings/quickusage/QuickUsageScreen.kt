package com.evening.dailylife.feature.me.settings.quickusage

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.evening.dailylife.R
import com.evening.dailylife.core.navigation.safePopBackStack
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.ext.safeMainPadding
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableSaltApi::class)
@Composable
fun QuickUsageScreen(
    navController: NavHostController,
    viewModel: QuickUsageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showTimePicker by remember { mutableStateOf(false) }
    var pendingEnableAfterPermission by remember { mutableStateOf(false) }

    val formattedTime = formatReminderTime(uiState.reminderTimeMinutes)
    val latestFormattedTime by rememberUpdatedState(formattedTime)

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                QuickUsageEvent.ExactAlarmPermissionRequired -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = "package:${context.packageName}".toUri()
                            if (context !is Activity) {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        }
                        runCatching { context.startActivity(intent) }
                    }
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.quick_usage_reminder_exact_alarm_required),
                            Toast.LENGTH_LONG,
                        )
                        .show()
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (pendingEnableAfterPermission) {
                pendingEnableAfterPermission = false
                if (granted) {
                    viewModel.enableReminder()
                    Toast
                        .makeText(
                            context,
                            context.getString(
                                R.string.quick_usage_reminder_toast_enabled,
                                latestFormattedTime
                            ),
                            Toast.LENGTH_SHORT,
                        )
                        .show()
                } else {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.quick_usage_reminder_permission_denied),
                            Toast.LENGTH_SHORT,
                        )
                        .show()
                }
            }
        },
    )

    if (showTimePicker) {
        LaunchedEffect(showTimePicker) {
            val minutes = uiState.reminderTimeMinutes
            val hour = minutes / 60
            val minute = minutes % 60
            val dialog = TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    val totalMinutes = selectedHour * 60 + selectedMinute
                    viewModel.updateReminderTime(totalMinutes)
                    Toast
                        .makeText(
                            context,
                            context.getString(
                                R.string.quick_usage_reminder_time_updated,
                                formatReminderTime(totalMinutes),
                            ),
                            Toast.LENGTH_SHORT,
                        )
                        .show()
                },
                hour,
                minute,
                DateFormat.is24HourFormat(context),
            )
            dialog.setTitle(context.getString(R.string.quick_usage_reminder_time_dialog_title))
            dialog.setButton(
                DialogInterface.BUTTON_NEUTRAL,
                context.getString(R.string.action_reset),
            ) { _, _ ->
                viewModel.resetReminderTime()
                dialog.dismiss()
            }
            dialog.setOnDismissListener {
                showTimePicker = false
            }
            dialog.show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
            .safeMainPadding(),
    ) {
        TitleBar(
            onBack = { navController.safePopBackStack() },
            text = stringResource(id = R.string.accounting_preferences),
        )

        RoundedColumn(modifier = Modifier.fillMaxWidth()) {
            ItemTitle(text = stringResource(id = R.string.quick_usage_section_shortcuts))
            Item(
                text = stringResource(R.string.quick_usage_widget_title),
                sub = stringResource(R.string.quick_usage_widget_sub),
                iconPainter = rememberVectorPainter(image = Icons.Outlined.Widgets),
                iconColor = SaltTheme.colors.text,
                iconPaddingValues = PaddingValues(all = 1.8.dp),
                onClick = {},
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        RoundedColumn(modifier = Modifier.fillMaxWidth()) {
            ItemTitle(text = stringResource(id = R.string.quick_usage_section_reminder))

            ItemSwitcher(
                state = uiState.reminderEnabled,
                onChange = { checked ->
                    if (checked) {
                        val notificationsEnabled = NotificationManagerCompat
                            .from(context)
                            .areNotificationsEnabled()
                        val hasPermission = hasNotificationPermission(context)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
                            pendingEnableAfterPermission = true
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            return@ItemSwitcher
                        }
                        if (!notificationsEnabled) {
                            Toast
                                .makeText(
                                    context,
                                    context.getString(R.string.quick_usage_reminder_notifications_disabled),
                                    Toast.LENGTH_SHORT,
                                )
                                .show()
                            return@ItemSwitcher
                        }
                        viewModel.enableReminder()
                        Toast
                            .makeText(
                                context,
                                context.getString(
                                    R.string.quick_usage_reminder_toast_enabled,
                                    formattedTime,
                                ),
                                Toast.LENGTH_SHORT,
                            )
                            .show()
                    } else {
                        viewModel.disableReminder()
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.quick_usage_reminder_toast_disabled),
                                Toast.LENGTH_SHORT,
                            )
                            .show()
                    }
                },
                text = stringResource(R.string.quick_usage_reminder_title),
                sub = if (uiState.reminderEnabled) {
                    stringResource(R.string.quick_usage_reminder_summary_on, formattedTime)
                } else {
                    stringResource(R.string.quick_usage_reminder_summary_off, formattedTime)
                },
                iconPainter = rememberVectorPainter(image = Icons.Outlined.NotificationsActive),
                iconColor = SaltTheme.colors.text,
                iconPaddingValues = PaddingValues(all = 1.8.dp),
            )

            Item(
                text = stringResource(R.string.quick_usage_reminder_time_label),
                sub = stringResource(R.string.quick_usage_reminder_time_value, formattedTime),
                iconPainter = rememberVectorPainter(image = Icons.Outlined.AccessTime),
                iconColor = if (uiState.reminderEnabled) {
                    SaltTheme.colors.text
                } else {
                    SaltTheme.colors.text.copy(alpha = 0.4f)
                },
                iconPaddingValues = PaddingValues(all = 1.8.dp),
                onClick = {
                    if (uiState.reminderEnabled) {
                        showTimePicker = true
                    }
                },
            )
        }
    }
}

private fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}

private fun formatReminderTime(minutes: Int): String {
    val hour = minutes / 60
    val minute = minutes % 60
    return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}
