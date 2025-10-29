package com.evening.dailylife.core.domain.app

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.exitProcess

/**
 * Wraps application-wide imperative actions that should originate outside the UI layer.
 */
@Singleton
class AppActionsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun restart() {
        val component = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.component
            ?: return

        val restartIntent = Intent.makeRestartActivityTask(component).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(restartIntent)
        exitProcess(0)
    }
}
