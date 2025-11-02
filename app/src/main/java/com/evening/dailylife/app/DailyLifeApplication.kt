package com.evening.dailylife.app

import android.app.Application
import android.content.Context
import com.evening.dailylife.core.domain.language.LanguageUseCase
import com.evening.dailylife.app.widget.TransactionWidgetUpdater
import com.evening.dailylife.core.util.readPersistedLanguageCode
import com.evening.dailylife.core.util.wrapContextWithLanguage
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DailyLifeApplication : Application() {

    @Inject lateinit var languageUseCase: LanguageUseCase
    @Inject lateinit var transactionWidgetUpdater: TransactionWidgetUpdater

    override fun attachBaseContext(base: Context) {
        val languageCode = readPersistedLanguageCode(base)
        val wrapped = wrapContextWithLanguage(base, languageCode)
        super.attachBaseContext(wrapped)
    }

    override fun onCreate() {
        super.onCreate()
        applyPersistedLocale()
    }

    private fun applyPersistedLocale() {
        languageUseCase.reapplyPersistedLanguage()
    }
}
