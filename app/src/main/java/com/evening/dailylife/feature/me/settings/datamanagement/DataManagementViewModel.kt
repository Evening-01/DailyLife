package com.evening.dailylife.feature.me.settings.datamanagement

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evening.dailylife.BuildConfig
import com.evening.dailylife.R
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.data.preferences.PreferencesManager
import com.evening.dailylife.core.data.preferences.ThemeMode
import com.evening.dailylife.core.data.preferences.UserPreferencesSnapshot
import com.evening.dailylife.core.data.repository.TransactionRepository
import com.evening.dailylife.core.domain.language.LanguageUseCase
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding"
private const val KEY_ALGORITHM_AES = "AES"
private const val DEFAULT_PBKDF_ITERATIONS = 120_000
private const val KEY_LENGTH_BITS = 256

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val preferencesManager: PreferencesManager,
    private val languageUseCase: LanguageUseCase,
) : ViewModel() {

    private val gson = Gson()
    private val secureRandom = SecureRandom()
    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val fileNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm", Locale.getDefault())

    private val _uiState = MutableStateFlow(
        DataManagementUiState(
            startDate = LocalDate.now().minusMonths(1),
            endDate = LocalDate.now(),
            lastBackupTimestamp = preferencesManager.lastBackupTimestamp.value,
        ),
    )
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DataManagementEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            preferencesManager.lastBackupTimestamp.collect { timestamp ->
                _uiState.update { current -> current.copy(lastBackupTimestamp = timestamp) }
            }
        }
        refreshSelectionCount()
    }

    fun setMode(mode: DataManagementMode) {
        _uiState.update { current ->
            if (current.mode == mode) {
                current
            } else {
                current.copy(mode = mode)
            }
        }
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.update { current ->
            val adjustedEnd = if (date.isAfter(current.endDate)) date else current.endDate
            current.copy(startDate = date, endDate = adjustedEnd)
        }
        refreshSelectionCount()
    }

    fun updateEndDate(date: LocalDate) {
        _uiState.update { current ->
            val adjustedStart = if (date.isBefore(current.startDate)) date else current.startDate
            current.copy(endDate = date, startDate = adjustedStart)
        }
        refreshSelectionCount()
    }

    fun toggleEncryption(enabled: Boolean) {
        _uiState.update { current ->
            if (!enabled) {
                current.copy(encryptionEnabled = false, password = "", confirmPassword = "")
            } else {
                current.copy(encryptionEnabled = true)
            }
        }
    }

    fun toggleIncludePreferences(enabled: Boolean) {
        _uiState.update { it.copy(includePreferences = enabled) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun updateConfirmPassword(value: String) {
        _uiState.update { it.copy(confirmPassword = value) }
    }

    fun updateRestorePassword(value: String) {
        _uiState.update { it.copy(restorePassword = value) }
    }

    fun setRestoreFile(uri: Uri?, fileName: String?) {
        _uiState.update { it.copy(restoreUri = uri, restoreFileName = fileName) }
    }

    fun suggestedBackupFileName(): String {
        val timestamp = fileNameFormatter.format(LocalDateTime.now())
        return "DailyLife-$timestamp.dlbackup"
    }

    fun performBackup(context: Context, destination: Uri) {
        val state = _uiState.value
        if (!state.selectionValid) {
            emitMessage(DataManagementMessage.Resource(R.string.data_management_error_invalid_range))
            return
        }
        if (state.encryptionEnabled) {
            if (state.password.isBlank()) {
                emitMessage(DataManagementMessage.Resource(R.string.data_management_error_password_required))
                return
            }
            if (state.password.length < 6) {
                emitMessage(DataManagementMessage.Resource(R.string.data_management_password_helper))
                return
            }
            if (state.password != state.confirmPassword) {
                emitMessage(DataManagementMessage.Resource(R.string.data_management_error_password_mismatch))
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, processingMessageRes = R.string.data_management_processing_backup) }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val transactions = transactionRepository
                        .getTransactionsByDateRange(startMillis(state.startDate), endMillis(state.endDate))
                        .first()

                    val preferencesSnapshot = if (state.includePreferences) {
                        capturePreferencesSnapshot()
                    } else {
                        null
                    }
                    val payload = BackupPayload(
                        metadata = BackupMetadata(
                            startDateEpochMillis = startMillis(state.startDate),
                            endDateEpochMillis = endMillis(state.endDate),
                            generatedAtEpochMillis = System.currentTimeMillis(),
                            appVersion = BuildConfig.VERSION_NAME,
                            itemCount = transactions.size,
                            encrypted = state.encryptionEnabled,
                        ),
                        transactions = transactions.map { it.toBackupTransaction() },
                        preferences = preferencesSnapshot,
                    )

                    val envelope = if (state.encryptionEnabled) {
                        val encrypted = encryptPayload(payload, state.password)
                        BackupEnvelope(
                            encrypted = true,
                            payload = encrypted,
                            data = null,
                        )
                    } else {
                        BackupEnvelope(
                            encrypted = false,
                            payload = null,
                            data = payload,
                        )
                    }

                    val json = gson.toJson(envelope)
                    context.contentResolver.openOutputStream(destination, "w")?.use { outputStream ->
                        OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                            writer.write(json)
                            writer.flush()
                        }
                    } ?: error("Failed to open destination stream")

                    preferencesManager.setLastBackupTimestamp(System.currentTimeMillis())
                    destination.toString()
                }
            }

            result.onSuccess { uriString ->
                emitMessage(
                    DataManagementMessage.Resource(
                        R.string.data_management_snackbar_backup_success_path,
                        listOf(uriString),
                    ),
                )
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        processingMessageRes = null,
                    )
                }
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                emitMessage(DataManagementMessage.Resource(R.string.data_management_error_unexpected))
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        processingMessageRes = null,
                    )
                }
            }
        }
    }

    fun performRestore(context: Context) {
        val state = _uiState.value
        val uri = state.restoreUri
        if (uri == null) {
            emitMessage(DataManagementMessage.Resource(R.string.data_management_error_restore_file_missing))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, processingMessageRes = R.string.data_management_processing_restore) }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw RestoreException.InvalidPayload(IllegalStateException("Unable to read backup stream"))
                    val envelope = gson.fromJson(String(rawBytes, StandardCharsets.UTF_8), BackupEnvelope::class.java)

                    val payload = if (envelope.encrypted) {
                        val encrypted = envelope.payload ?: throw RestoreException.InvalidPayload(null)
                        if (state.restorePassword.isBlank()) {
                            throw RestoreException.PasswordRequired
                        }
                        val decryptedBytes = decryptPayload(encrypted, state.restorePassword)
                        gson.fromJson(String(decryptedBytes, StandardCharsets.UTF_8), BackupPayload::class.java)
                            ?: throw RestoreException.InvalidPayload(null)
                    } else {
                        envelope.data ?: throw RestoreException.InvalidPayload(null)
                    }

                    restoreFromPayload(payload)
                }
            }

            result.onSuccess {
                emitMessage(DataManagementMessage.Resource(R.string.data_management_snackbar_restore_success))
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        processingMessageRes = null,
                        restorePassword = "",
                    )
                }
                refreshSelectionCount()
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                when (throwable) {
                    is RestoreException.PasswordRequired -> emitMessage(DataManagementMessage.Resource(R.string.data_management_error_restore_password_missing))
                    is RestoreException.DecryptionFailed -> emitMessage(DataManagementMessage.Resource(R.string.data_management_error_restore_decrypt))
                    is RestoreException.InvalidPayload -> emitMessage(DataManagementMessage.Resource(R.string.data_management_error_restore_parse))
                    is JsonSyntaxException -> emitMessage(DataManagementMessage.Resource(R.string.data_management_error_restore_parse))
                    else -> emitMessage(DataManagementMessage.Resource(R.string.data_management_error_unexpected))
                }
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        processingMessageRes = null,
                    )
                }
            }
        }
    }

    private suspend fun restoreFromPayload(payload: BackupPayload) {
        payload.transactions.forEach { record ->
            val entity = record.toTransactionEntity()
            transactionRepository.insertTransaction(entity)
        }

        payload.preferences?.let { preferences ->
            val themeMode = runCatching { ThemeMode.valueOf(preferences.themeMode) }.getOrDefault(ThemeMode.SYSTEM)
            preferencesManager.setThemeMode(themeMode)
            preferencesManager.setDynamicColor(preferences.dynamicColor)
            preferencesManager.setFingerprintLockEnabled(preferences.fingerprintLockEnabled)
            preferencesManager.setUiScale(preferences.uiScale, persist = true)
            preferencesManager.setFontScale(preferences.fontScale, persist = true)
            preferencesManager.setCustomFontEnabled(preferences.customFontEnabled)
            preferencesManager.setQuickUsageReminderEnabled(preferences.quickUsageReminderEnabled)
            preferencesManager.setQuickUsageReminderTimeMinutes(preferences.quickUsageReminderTimeMinutes)

            if (preferences.languageCode.isNotBlank()) {
                languageUseCase.setLanguage(preferences.languageCode)
            }
        }
    }

    private fun TransactionEntity.toBackupTransaction(): BackupTransaction {
        return BackupTransaction(
            id = id,
            category = category,
            description = description,
            amount = amount,
            mood = mood,
            source = source,
            date = date,
        )
    }

    private fun BackupTransaction.toTransactionEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            category = category,
            description = description,
            amount = amount,
            mood = mood,
            source = source,
            date = date,
            isDeleted = false,
        )
    }

    private fun capturePreferencesSnapshot(): UserPreferencesSnapshot {
        return UserPreferencesSnapshot(
            themeMode = preferencesManager.themeMode.value.name,
            dynamicColor = preferencesManager.dynamicColor.value,
            fingerprintLockEnabled = preferencesManager.fingerprintLockEnabled.value,
            uiScale = preferencesManager.uiScale.value,
            fontScale = preferencesManager.fontScale.value,
            customFontEnabled = preferencesManager.customFontEnabled.value,
            quickUsageReminderEnabled = preferencesManager.quickUsageReminderEnabled.value,
            quickUsageReminderTimeMinutes = preferencesManager.quickUsageReminderTimeMinutes.value,
            languageCode = languageUseCase.getPersistedLanguageCode(),
        )
    }

    private fun encryptPayload(payload: BackupPayload, password: String): EncryptedPayload {
        val payloadJson = gson.toJson(payload).toByteArray(StandardCharsets.UTF_8)
        val salt = ByteArray(16).also(secureRandom::nextBytes)
        val iv = ByteArray(12).also(secureRandom::nextBytes)
        val keySpec = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(128, iv))
        val cipherBytes = cipher.doFinal(payloadJson)

        return EncryptedPayload(
            cipherAlgorithm = ENCRYPTION_ALGORITHM,
            salt = Base64.encodeToString(salt, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP),
            cipherText = Base64.encodeToString(cipherBytes, Base64.NO_WRAP),
            iterations = DEFAULT_PBKDF_ITERATIONS,
            kdfAlgorithm = pbkdfAlgorithm(),
        )
    }

    private fun decryptPayload(payload: EncryptedPayload, password: String): ByteArray {
        return try {
            val salt = Base64.decode(payload.salt, Base64.NO_WRAP)
            val iv = Base64.decode(payload.iv, Base64.NO_WRAP)
            val cipherBytes = Base64.decode(payload.cipherText, Base64.NO_WRAP)
            val keySpec = deriveKey(password, salt, payload.iterations)
            val cipher = Cipher.getInstance(payload.cipherAlgorithm)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(128, iv))
            cipher.doFinal(cipherBytes)
        } catch (error: GeneralSecurityException) {
            throw RestoreException.DecryptionFailed(error)
        } catch (error: IllegalArgumentException) {
            throw RestoreException.InvalidPayload(error)
        }
    }

    private fun deriveKey(password: String, salt: ByteArray, iterations: Int = DEFAULT_PBKDF_ITERATIONS): SecretKeySpec {
        val algorithm = pbkdfAlgorithm()
        val factory = SecretKeyFactory.getInstance(algorithm)
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        val encoded = factory.generateSecret(spec).encoded
        return SecretKeySpec(encoded, KEY_ALGORITHM_AES)
    }

    private fun pbkdfAlgorithm(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            "PBKDF2WithHmacSHA256"
        } else {
            "PBKDF2WithHmacSHA1"
        }
    }

    private fun startMillis(date: LocalDate): Long {
        return date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    private fun endMillis(date: LocalDate): Long {
        return date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1L
    }

    private fun emitMessage(message: DataManagementMessage) {
        viewModelScope.launch {
            _events.emit(DataManagementEvent.ShowMessage(message))
        }
    }

    private fun refreshSelectionCount() {
        val state = _uiState.value
        if (state.startDate.isAfter(state.endDate)) {
            _uiState.update { it.copy(selectionValid = false, selectionCount = 0) }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val count = transactionRepository
                .getTransactionsByDateRange(startMillis(state.startDate), endMillis(state.endDate))
                .first()
                .size
            _uiState.update { it.copy(selectionValid = true, selectionCount = count) }
        }
    }

    private sealed class RestoreException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
        data object PasswordRequired : RestoreException()
        class DecryptionFailed(cause: Throwable) : RestoreException(cause = cause)
        class InvalidPayload(cause: Throwable?) : RestoreException(cause = cause)
    }
}
