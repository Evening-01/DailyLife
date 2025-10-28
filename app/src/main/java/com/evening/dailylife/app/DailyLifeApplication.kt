package com.evening.dailylife.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.evening.dailylife.core.data.preferences.AppLanguage
import com.evening.dailylife.core.data.preferences.PreferencesKeys
import dagger.hilt.android.HiltAndroidApp
import io.fastkv.FastKV

@HiltAndroidApp
class DailyLifeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        applyPersistedLocale()
    }

    private fun applyPersistedLocale() {
        val fastKV = FastKV.Builder(this, PreferencesKeys.PREFERENCES_NAME).build()
        val storedLanguage = fastKV.getString(
            PreferencesKeys.KEY_APP_LANGUAGE,
            AppLanguage.CHINESE.name
        )
        val appLanguage = AppLanguage.fromName(storedLanguage)
        val desiredLocales = LocaleListCompat.forLanguageTags(appLanguage.languageTag)
        if (AppCompatDelegate.getApplicationLocales() != desiredLocales) {
            AppCompatDelegate.setApplicationLocales(desiredLocales)
        }
    }
}
