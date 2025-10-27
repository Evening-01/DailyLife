package com.evening.dailylife.core.appicon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppIconManager @Inject constructor(
    @ApplicationContext private val context: Context
) : DefaultLifecycleObserver {

    private val packageManager: PackageManager = context.packageManager
    private val processLifecycle = ProcessLifecycleOwner.get().lifecycle

    private val defaultAlias = ComponentName(
        context,
        "${context.packageName}.app.main.MainActivityAliasDefault"
    )
    private val dynamicAlias = ComponentName(
        context,
        "${context.packageName}.app.main.MainActivityAliasDynamic"
    )

    private var pendingShouldEnableDynamic: Boolean? = null

    init {
        processLifecycle.addObserver(this)
    }

    fun applyDynamicIcon(enabled: Boolean) {
        val shouldEnableDynamic = enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        synchronized(this) {
            pendingShouldEnableDynamic = shouldEnableDynamic
            if (!processLifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                consumePendingStateLocked()
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        synchronized(this) {
            consumePendingStateLocked()
        }
    }

    private fun consumePendingStateLocked() {
        val target = pendingShouldEnableDynamic ?: return
        if (isCurrentStateDynamic() == target) {
            pendingShouldEnableDynamic = null
            return
        }
        pendingShouldEnableDynamic = null
        updateAliasState(target)
    }

    private fun isCurrentStateDynamic(): Boolean {
        val state = packageManager.getComponentEnabledSetting(dynamicAlias)
        return when (state) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> false
            else -> false
        }
    }

    private fun updateAliasState(enableDynamic: Boolean) {
        setComponentState(
            dynamicAlias,
            if (enableDynamic) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
        )
        setComponentState(
            defaultAlias,
            if (enableDynamic) {
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
        runCatching {
            packageManager.setComponentEnabledSetting(
                componentName,
                newState,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
