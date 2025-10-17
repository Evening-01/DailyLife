package com.evening.dailylife.core.util

/**
 * 计算弹窗在屏幕上的水平偏移量，避免超出屏幕或贴边。
 */
object PopupPositionCalculator {

    fun calculateOffset(
        density: Float,
        clickOffsetX: Float,
        popupWidth: Int,
        screenWidthDp: Int
    ): Float {
        val estimatedPosition = clickOffsetX / (density + 0.245f)
        return when {
            estimatedPosition < 42 + popupWidth / 2 -> 16f
            estimatedPosition > (screenWidthDp - popupWidth) -> (screenWidthDp - popupWidth - 42).toFloat()
            else -> estimatedPosition - popupWidth + 84
        }
    }
}

@Deprecated(
    message = "请改用 PopupPositionCalculator.calculateOffset",
    replaceWith = ReplaceWith("PopupPositionCalculator.calculateOffset(density, clickOffsetX, popupWidth, screenWidthDp)")
)
fun calPopupLocation(
    density: Float,
    clickOffsetX: Float,
    popupWidth: Int,
    screenWidthDp: Int
): Float = PopupPositionCalculator.calculateOffset(density, clickOffsetX, popupWidth, screenWidthDp)

fun calculatePopupOffset(
    density: Float,
    clickOffsetX: Float,
    popupWidth: Int,
    screenWidthDp: Int
): Float = PopupPositionCalculator.calculateOffset(density, clickOffsetX, popupWidth, screenWidthDp)
