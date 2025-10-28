package com.evening.dailylife.app.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.saltColorsByColorScheme
import com.moriafly.salt.ui.saltConfigs
import com.moriafly.salt.ui.saltTextStyles

@SuppressLint("NewApi")
@OptIn(UnstableSaltApi::class)
@Composable
fun DailyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    textSizeScale: Float = 1f,
    useCustomFont: Boolean = true,
    seedColor: Color = DailySeedColor,
    paletteStyle: PaletteStyle = PaletteStyle.TonalSpot,
    specVersion: ColorSpec.SpecVersion = ColorSpec.SpecVersion.SPEC_2025,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val useDynamicColor = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val materialColorScheme = if (useDynamicColor) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        rememberDynamicColorScheme(
            seedColor = seedColor,
            isDark = darkTheme,
            style = paletteStyle,
            specVersion = specVersion
        )
    }

    val extendedColorScheme = ExtendedColorScheme(
        headerContainer = if (darkTheme) materialColorScheme.primaryContainer else materialColorScheme.primary,
        onHeaderContainer = if (darkTheme) materialColorScheme.onPrimaryContainer else materialColorScheme.onPrimary,
        success = SuccessGreen
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        SideEffect {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val targetFontFamily = if (useCustomFont) appFontFamily else FontFamily.Default

    val customSaltTextStyles = saltTextStyles(
        main = TextStyle(
            fontFamily = targetFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        ).scaled(textSizeScale),
        sub = TextStyle(
            fontFamily = targetFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        ).scaled(textSizeScale),
        paragraph = TextStyle(
            fontFamily = targetFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 22.sp
        ).scaled(textSizeScale)
    )

    CompositionLocalProvider(LocalExtendedColorScheme provides extendedColorScheme) {
        SaltTheme(
            colors = saltColorsByColorScheme(materialColorScheme),
            configs = saltConfigs(isDarkTheme = darkTheme),
            textStyles = customSaltTextStyles
        ) {
            MaterialTheme(
                colorScheme = materialColorScheme,
                typography = createDailyTypography(
                    fontFamily = targetFontFamily,
                    scale = textSizeScale
                ),
                content = content
            )
        }
    }
}

private fun TextStyle.scaled(scale: Float): TextStyle {
    val scaledFontSize = if (fontSize.isSpecified) (fontSize.value * scale).sp else fontSize
    val scaledLineHeight = if (lineHeight.isSpecified) (lineHeight.value * scale).sp else lineHeight
    return copy(
        fontSize = scaledFontSize,
        lineHeight = scaledLineHeight,
    )
}
