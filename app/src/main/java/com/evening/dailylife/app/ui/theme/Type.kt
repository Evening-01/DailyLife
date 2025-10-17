package com.evening.dailylife.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 应用统一排版体系，按 Display/Headline/Title/Body/Label 分组。
 */
val Typography = Typography(
    // Display 系列：用于超大号标题
    displayLarge = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 55.sp,
        lineHeight = 62.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 43.sp,
        lineHeight = 50.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp
    ),

    // Headline 系列：用于页面级标题
    headlineLarge = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),

    // Title 系列：用于模块标题或导航栏
    titleLarge = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),

    // Body 系列：用于正文内容
    bodyLarge = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp
    ),

    // Label 系列：用于按钮与标签
    labelLarge = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = appFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
