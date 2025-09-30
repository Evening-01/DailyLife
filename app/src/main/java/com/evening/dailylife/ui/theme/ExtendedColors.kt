package com.evening.dailylife.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 定义了 MaterialTheme.colorScheme 未包含的扩展颜色。
 * 这有助于将所有应用的颜色集中管理，方便主题切换和维护。
 */
@Immutable
data class ExtendedColorScheme(
    val headerContainer: Color,
    val onHeaderContainer: Color,
    val success: Color
)

/**
 * 通过 CompositionLocalProvider 提供扩展颜色方案。
 * 默认值设为 Unspecified，以在未提供时尽早暴露问题。
 */
val LocalExtendedColorScheme = staticCompositionLocalOf {
    ExtendedColorScheme(
        headerContainer = Color.Unspecified,
        onHeaderContainer = Color.Unspecified,
        success = Color.Unspecified
    )
}