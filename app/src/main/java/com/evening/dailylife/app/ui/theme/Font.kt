package com.evening.dailylife.app.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.evening.dailylife.R

/**
 * 应用默认字体族，如需新增字重在此集中维护。
 */
val appFontFamily = FontFamily(
    // 使用同一字体文件映射常用字重，避免 SaltUI 组件在请求粗体等样式时回退系统字体。
    Font(R.font.font, FontWeight.Thin),
    Font(R.font.font, FontWeight.ExtraLight),
    Font(R.font.font, FontWeight.Light),
    Font(R.font.font, FontWeight.Normal),
    Font(R.font.font, FontWeight.Medium),
    Font(R.font.font, FontWeight.SemiBold),
    Font(R.font.font, FontWeight.Bold),
    Font(R.font.font, FontWeight.ExtraBold),
    Font(R.font.font, FontWeight.Black)
)
