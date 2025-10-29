package com.evening.dailylife.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Persists the app's preferred language and exposes updates via [StateFlow].
 */
@Singleton
class LanguagePreferencesRepository @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PreferencesKeys.PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val key = PreferencesKeys.KEY_APP_LANGUAGE

    private val _languageCode = MutableStateFlow(sharedPreferences.getString(key, "") ?: "")

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { source, changedKey ->
        if (changedKey == key) {
            _languageCode.value = source.getString(key, "") ?: ""
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    val currentLanguageCode: String
        get() = _languageCode.value

    fun getLanguageCode(): StateFlow<String> = _languageCode.asStateFlow()

    suspend fun setLanguageCode(langCode: String) {
        withContext(Dispatchers.IO) {
            if (sharedPreferences.edit().putString(key, langCode).commit()) {
                _languageCode.value = langCode
            }
        }
    }
}
