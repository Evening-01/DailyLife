package com.evening.dailylife.core.appicon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppIconManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val packageManager: PackageManager = context.packageManager

    private val defaultAlias = ComponentName(
        context,
        "${context.packageName}.app.main.MainActivityAliasDefault"
    )
    private val dynamicAlias = ComponentName(
        context,
        "${context.packageName}.app.main.MainActivityAliasDynamic"
    )

    fun applyDynamicIcon(enabled: Boolean) {
        val shouldEnableDynamic = enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        setComponentState(
            dynamicAlias,
            if (shouldEnableDynamic) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
        )
        setComponentState(
            defaultAlias,
            if (shouldEnableDynamic) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }
        )
    }

    private fun setComponentState(componentName: ComponentName, newState: Int) {
        if (packageManager.getComponentEnabledSetting(componentName) == newState) {
            return
        }
        packageManager.setComponentEnabledSetting(
            componentName,
            newState,
            PackageManager.DONT_KILL_APP
        )
    }
}