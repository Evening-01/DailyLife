package com.evening.dailylife.core.util

import android.util.DisplayMetrics
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 判定设备类型的辅助方法。
 */
object DeviceTypeUtils {
    fun isTablet(displayMetrics: DisplayMetrics): Boolean {
        val screenWidthInches = displayMetrics.widthPixels / displayMetrics.xdpi
        val screenHeightInches = displayMetrics.heightPixels / displayMetrics.ydpi
        val diagonalInches = sqrt(
            screenWidthInches.toDouble().pow(2.0) + screenHeightInches.toDouble().pow(2.0)
        )
        return diagonalInches >= 7
    }
}
