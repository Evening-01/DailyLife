package com.evening.dailylife.app.main.splash

import android.content.Context
import android.os.Build
import com.evening.dailylife.core.data.preferences.PreferencesKeys
import io.fastkv.FastKV

fun shouldUseDynamicSplashIcon(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return false
    }
    val fastKV = FastKV.Builder(context.applicationContext, PreferencesKeys.PREFERENCES_NAME).build()
    return fastKV.getBoolean(PreferencesKeys.KEY_DYNAMIC_COLOR, false)
}
