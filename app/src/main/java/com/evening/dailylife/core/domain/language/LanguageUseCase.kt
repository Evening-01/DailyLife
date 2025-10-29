package com.evening.dailylife.core.domain.language

import android.content.Context
import com.evening.dailylife.core.data.preferences.LanguagePreferencesRepository
import com.evening.dailylife.core.util.changePlatformLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Coordinates persisted language preference with platform configuration.
 */
@Singleton
class LanguageUseCase @Inject constructor(
    private val repository: LanguagePreferencesRepository,
    @ApplicationContext private val context: Context,
) {

    fun observeLanguageCode(): Flow<String> = repository.getLanguageCode()

    fun getLanguage(): Flow<String> {
        return observeLanguageCode().map { code ->
            if (code.isBlank()) Locale.getDefault().toLanguageTag() else code
        }
    }

    fun getPersistedLanguageCode(): String = repository.currentLanguageCode

    suspend fun setLanguage(langCode: String) {
        repository.setLanguageCode(langCode)
        changePlatformLanguage(context, langCode)
    }

    fun reapplyPersistedLanguage() {
        changePlatformLanguage(context, repository.currentLanguageCode)
    }
}
