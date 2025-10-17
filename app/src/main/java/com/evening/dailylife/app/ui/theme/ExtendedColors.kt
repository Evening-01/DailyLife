package com.evening.dailylife.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * MaterialTheme.colorScheme 未覆盖的扩展色值统一在此声明。
 */
@Immutable
data class ExtendedColorScheme(
    val headerContainer: Color,
    val onHeaderContainer: Color,
    val success: Color
)

/**
 * 通过 CompositionLocal 提供扩展色值，未显式提供时使用 Unspecified 暴露错误。
 */
val LocalExtendedColorScheme = staticCompositionLocalOf {
    ExtendedColorScheme(
        headerContainer = Color.Unspecified,
        onHeaderContainer = Color.Unspecified,
        success = Color.Unspecified
    )
}
