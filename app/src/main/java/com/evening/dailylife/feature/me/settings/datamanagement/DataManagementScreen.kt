package com.evening.dailylife.feature.me.settings.datamanagement

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.evening.dailylife.R
import com.evening.dailylife.core.designsystem.component.CalendarPickerBottomSheet
import com.evening.dailylife.core.designsystem.component.CalendarPickerType
import com.evening.dailylife.core.navigation.safePopBackStack
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.ext.safeMainPadding
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableSaltApi::class)
@Composable
fun DataManagementScreen(
    navController: NavHostController,
    viewModel: DataManagementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is DataManagementEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message.resolve(context))
                }
            }
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri ->
            if (uri != null) {
                viewModel.performBackup(context, uri)
            }
        },
    )

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                val name = resolveDisplayName(context, uri)
                viewModel.setRestoreFile(uri, name)
            }
        },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
            .safeMainPadding(),
    ) {
        TitleBar(
            onBack = { navController.safePopBackStack() },
            text = stringResource(id = R.string.data_management),
        )

        if (uiState.isProcessing) {
            uiState.processingMessageRes?.let { messageRes ->
                ProcessingBanner(stringResource(id = messageRes))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ModeSection(
                uiState = uiState,
                onModeSelected = viewModel::setMode,
            )

            if (uiState.mode == DataManagementMode.BACKUP) {
                BackupSection(
                    uiState = uiState,
                    onStartDateClick = { showStartDatePicker = true },
                    onEndDateClick = { showEndDatePicker = true },
                    onToggleEncryption = viewModel::toggleEncryption,
                    onPasswordChange = viewModel::updatePassword,
                    onConfirmPasswordChange = viewModel::updateConfirmPassword,
                    onCreateBackupClick = {
                        createDocumentLauncher.launch(viewModel.suggestedBackupFileName())
                    },
                )
            } else {
                RestoreSection(
                    uiState = uiState,
                    onSelectFileClick = {
                        openDocumentLauncher.launch(arrayOf("*/*"))
                    },
                    onPasswordChange = viewModel::updateRestorePassword,
                    onRestoreClick = { viewModel.performRestore(context) },
                )
            }
        }
        CalendarPickerBottomSheet(
            showBottomSheet = showStartDatePicker,
            onDismiss = { showStartDatePicker = false },
            type = CalendarPickerType.DATE,
            initialDate = toCalendar(uiState.startDate),
            onDateSelected = { year, month, day ->
                viewModel.updateStartDate(LocalDate.of(year, month, day))
            },
            onMonthSelected = { year, month ->
                viewModel.updateStartDate(LocalDate.of(year, month, 1))
            },
        )

        CalendarPickerBottomSheet(
            showBottomSheet = showEndDatePicker,
            onDismiss = { showEndDatePicker = false },
            type = CalendarPickerType.DATE,
            initialDate = toCalendar(uiState.endDate),
            onDateSelected = { year, month, day ->
                viewModel.updateEndDate(LocalDate.of(year, month, day))
            },
            onMonthSelected = { year, month ->
                viewModel.updateEndDate(LocalDate.of(year, month, 1))
            },
        )
    }


}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ModeSection(
    uiState: DataManagementUiState,
    onModeSelected: (DataManagementMode) -> Unit,
) {
    val lastBackupText = uiState.lastBackupTimestamp.takeIf { it > 0L }?.let(::formatTimestamp)

    RoundedColumn(modifier = Modifier.fillMaxWidth()) {
        ItemTitle(text = stringResource(id = R.string.data_management_mode_title))
        Text(
            text = stringResource(id = R.string.data_management_mode_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModeChip(
                text = stringResource(id = R.string.data_management_mode_backup),
                selected = uiState.mode == DataManagementMode.BACKUP,
                onClick = { onModeSelected(DataManagementMode.BACKUP) },
                icon = Icons.Outlined.Archive,
            )
            ModeChip(
                text = stringResource(id = R.string.data_management_mode_restore),
                selected = uiState.mode == DataManagementMode.RESTORE,
                onClick = { onModeSelected(DataManagementMode.RESTORE) },
                icon = Icons.Outlined.Restore,
            )
        }

        lastBackupText?.let { formatted ->
            Text(
                text = stringResource(id = R.string.data_management_last_backup_time, formatted),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
        } ?: Text(
            text = stringResource(id = R.string.data_management_last_backup_never),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun BackupSection(
    uiState: DataManagementUiState,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onToggleEncryption: (Boolean) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onCreateBackupClick: () -> Unit,
) {
    RoundedColumn(modifier = Modifier.fillMaxWidth()) {
        ItemTitle(text = stringResource(id = R.string.data_management_backup_section_title))

        Text(
            text = stringResource(id = R.string.data_management_date_range_help),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )

        DateRangeRow(
            start = uiState.startDate,
            end = uiState.endDate,
            onStartClick = onStartDateClick,
            onEndClick = onEndDateClick,
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (uiState.selectionValid) {
                stringResource(id = R.string.data_management_backup_summary, uiState.selectionCount)
            } else {
                stringResource(id = R.string.data_management_error_invalid_range)
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (uiState.selectionValid) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.error
            },
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))
        ItemSwitcher(
            state = uiState.encryptionEnabled,
            onChange = onToggleEncryption,
            text = stringResource(id = R.string.data_management_encryption_label),
            sub = stringResource(id = R.string.data_management_encryption_sub),
            iconPainter = rememberVectorPainter(Icons.Outlined.Lock),
            iconPaddingValues = PaddingValues(all = 1.8.dp),
            iconColor = SaltTheme.colors.text,
        )

        if (uiState.encryptionEnabled) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(id = R.string.data_management_password_placeholder)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Password,
                    ),
                )
                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(id = R.string.data_management_password_confirm_placeholder)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Password,
                    ),
                )
                Text(
                    text = stringResource(id = R.string.data_management_password_helper),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val backupEnabled = uiState.selectionValid && !uiState.isProcessing && (!uiState.encryptionEnabled || (uiState.password.isNotBlank() && uiState.password.length >= 6 && uiState.password == uiState.confirmPassword))

        Button(
            onClick = onCreateBackupClick,
            enabled = backupEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(text = stringResource(id = R.string.data_management_backup_button))
        }
    }
}

@Composable
private fun RestoreSection(
    uiState: DataManagementUiState,
    onSelectFileClick: () -> Unit,
    onPasswordChange: (String) -> Unit,
    onRestoreClick: () -> Unit,
) {
    RoundedColumn(modifier = Modifier.fillMaxWidth()) {
        ItemTitle(text = stringResource(id = R.string.data_management_restore_section_title))

        Text(
            text = stringResource(id = R.string.data_management_restore_file_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        )

        Button(
            onClick = onSelectFileClick,
            enabled = !uiState.isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Text(text = stringResource(id = R.string.data_management_select_file))
        }

        uiState.restoreFileName?.let { name ->
            Text(
                text = stringResource(id = R.string.data_management_selected_file, name),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .fillMaxWidth(),
            )
        }

        OutlinedTextField(
            value = uiState.restorePassword,
            onValueChange = onPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            label = { Text(stringResource(id = R.string.data_management_restore_password_placeholder)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                keyboardType = KeyboardType.Password,
            ),
        )

        Text(
            text = stringResource(id = R.string.data_management_restore_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )

        val restoreEnabled = uiState.restoreUri != null && !uiState.isProcessing

        Button(
            onClick = onRestoreClick,
            enabled = restoreEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(text = stringResource(id = R.string.data_management_restore_button))
        }
    }
}

@Composable
private fun RowScope.ModeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    val background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateRangeRow(
    start: LocalDate,
    end: LocalDate,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DateCard(
            label = stringResource(id = R.string.data_management_start_date),
            date = start,
            onClick = onStartClick,
            modifier = Modifier.weight(1f),
        )
        DateCard(
            label = stringResource(id = R.string.data_management_end_date),
            date = end,
            onClick = onEndClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateCard(
    label: String,
    date: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ProcessingBanner(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun DataManagementMessage.resolve(context: Context): String {
    return when (this) {
        is DataManagementMessage.Plain -> value
        is DataManagementMessage.Resource -> context.getString(resId, *args.toTypedArray())
    }
}

private fun resolveDisplayName(context: Context, uri: Uri): String? {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    return context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            cursor.getString(index)
        } else {
            null
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun toCalendar(date: LocalDate): java.util.Calendar {
    return java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.YEAR, date.year)
        set(java.util.Calendar.MONTH, date.monthValue - 1)
        set(java.util.Calendar.DAY_OF_MONTH, date.dayOfMonth)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTimestamp(timestamp: Long): String {
    val instant = Instant.ofEpochMilli(timestamp)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())
    return formatter.format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()))
}
