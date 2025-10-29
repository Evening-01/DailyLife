package com.evening.dailylife.core.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.evening.dailylife.core.data.preferences.PreferencesKeys

import java.util.Locale

/**
 * Applies the requested language to the current Android process.
 */
fun changePlatformLanguage(context: Context, languageCode: String) {
    val appContext = context.applicationContext
    val resources = appContext.resources
    val configuration = Configuration(resources.configuration)

    val targetLocale = resolveLocale(languageCode)

    Locale.setDefault(targetLocale)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val localeList = LocaleList(targetLocale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)
    } else {
        @Suppress("DEPRECATION")
        configuration.setLocale(targetLocale)
    }

    @Suppress("DEPRECATION")
    resources.updateConfiguration(configuration, resources.displayMetrics)

    val compatLocales = if (languageCode.isBlank()) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(targetLocale.toLanguageTag())
    }
    AppCompatDelegate.setApplicationLocales(compatLocales)
}

fun wrapContextWithLanguage(base: Context, languageCode: String): Context {
    if (languageCode.isBlank()) return base

    val targetLocale = resolveLocale(languageCode)
    val configuration = Configuration(base.resources.configuration)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val localeList = LocaleList(targetLocale)
        configuration.setLocales(localeList)
    } else {
        @Suppress("DEPRECATION")
        configuration.setLocale(targetLocale)
    }
    return base.createConfigurationContext(configuration)
}

fun readPersistedLanguageCode(context: Context): String {
    val prefs = context.getSharedPreferences(PreferencesKeys.PREFERENCES_NAME, Context.MODE_PRIVATE)
    return prefs.getString(PreferencesKeys.KEY_APP_LANGUAGE, "") ?: ""
}

internal fun resolveLocale(languageCode: String): Locale {
    if (languageCode.isBlank()) {
        return systemDefaultLocale()
    }

    val normalized = languageCode.replace('_', '-')
    return runCatching {
        val builder = Locale.Builder()
        val parts = normalized.split('-', limit = 3)
        if (parts.isNotEmpty() && parts[0].isNotBlank()) {
            builder.setLanguage(parts[0])
        }
        if (parts.size > 1 && parts[1].isNotBlank()) {
            builder.setRegion(parts[1])
        }
        if (parts.size > 2 && parts[2].isNotBlank()) {
            builder.setVariant(parts[2])
        }
        builder.build()
    }.getOrElse { systemDefaultLocale() }
}

private fun systemDefaultLocale(): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.getDefault()[0] ?: Locale.getDefault()
    } else {
        @Suppress("DEPRECATION")
        Resources.getSystem().configuration.locale ?: Locale.getDefault()
    }
}
