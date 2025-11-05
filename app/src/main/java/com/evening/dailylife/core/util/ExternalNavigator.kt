package com.evening.dailylife.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.net.toUri
import com.evening.dailylife.R

/**
 * Launches an external browser or app capable of handling [url].
 * Shows a short toast if no handler is available.
 */
fun Context.launchExternalUrl(
    url: String,
    @StringRes errorMessageRes: Int = R.string.common_open_link_error,
) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        if (this@launchExternalUrl !is Activity) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    runCatching {
        startActivity(intent)
    }.onFailure {
        Toast.makeText(this, getString(errorMessageRes), Toast.LENGTH_SHORT).show()
    }
}
