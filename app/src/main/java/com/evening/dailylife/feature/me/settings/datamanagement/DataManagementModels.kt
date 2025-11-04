package com.evening.dailylife.feature.me.settings.datamanagement

import android.net.Uri
import androidx.annotation.StringRes
import com.evening.dailylife.core.data.preferences.UserPreferencesSnapshot
import java.time.LocalDate

const val DATA_MANAGEMENT_BACKUP_VERSION = 1

enum class DataManagementMode {
    BACKUP,
    RESTORE,
}

data class DataManagementUiState(
    val mode: DataManagementMode = DataManagementMode.BACKUP,
    val startDate: LocalDate = LocalDate.now().minusMonths(1),
    val endDate: LocalDate = LocalDate.now(),
    val encryptionEnabled: Boolean = false,
    val password: String = "",
    val confirmPassword: String = "",
    val isProcessing: Boolean = false,
    val processingMessageRes: Int? = null,
    val selectionCount: Int = 0,
    val selectionValid: Boolean = true,
    val lastBackupTimestamp: Long = 0L,
    val restoreUri: Uri? = null,
    val restoreFileName: String? = null,
    val restorePassword: String = "",
)

sealed interface DataManagementMessage {
    data class Resource(@StringRes val resId: Int, val args: List<Any> = emptyList()) : DataManagementMessage
    data class Plain(val value: String) : DataManagementMessage
}

sealed interface DataManagementEvent {
    data class ShowMessage(val message: DataManagementMessage) : DataManagementEvent
}

data class BackupMetadata(
    val startDateEpochMillis: Long,
    val endDateEpochMillis: Long,
    val generatedAtEpochMillis: Long,
    val appVersion: String,
    val itemCount: Int,
    val encrypted: Boolean,
)

data class BackupPayload(
    val metadata: BackupMetadata,
    val transactions: List<BackupTransaction>,
    val preferences: UserPreferencesSnapshot,
)

data class BackupEnvelope(
    val version: Int = DATA_MANAGEMENT_BACKUP_VERSION,
    val encrypted: Boolean,
    val payload: EncryptedPayload? = null,
    val data: BackupPayload? = null,
)

data class EncryptedPayload(
    val cipherAlgorithm: String,
    val salt: String,
    val iv: String,
    val cipherText: String,
    val iterations: Int,
    val kdfAlgorithm: String,
)

data class BackupTransaction(
    val id: Int,
    val category: String,
    val description: String,
    val amount: Double,
    val mood: Int?,
    val source: String,
    val date: Long,
)
