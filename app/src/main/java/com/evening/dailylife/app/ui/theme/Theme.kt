package com.evening.dailylife.app.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.saltColorsByColorScheme
import com.moriafly.salt.ui.saltConfigs
import com.moriafly.salt.ui.saltTextStyles

private val lightExtendedColorScheme = ExtendedColorScheme(
    headerContainer = SunnyGold,
    onHeaderContainer = OnSunnyGold,
    success = SuccessGreen
)

private val darkExtendedColorScheme = ExtendedColorScheme(
    headerContainer = WarmGinger,
    onHeaderContainer = OnWarmGinger,
    success = SuccessGreen
)

private val LightColorScheme = lightColorScheme(
    primary = SunnyGold,
    onPrimary = OnSunnyGold,
    primaryContainer = SunnyGoldContainer,
    onPrimaryContainer = OnSunnyGoldContainer,
    inversePrimary = WarmGinger,

    secondary = SoftCitrine,
    onSecondary = OnSoftCitrine,
    secondaryContainer = SoftCitrineContainer,
    onSecondaryContainer = OnSoftCitrineContainer,

    tertiary = MutedSage,
    onTertiary = OnMutedSage,
    tertiaryContainer = MutedSageContainer,
    onTertiaryContainer = OnMutedSageContainer,

    background = ParchmentWhite,
    onBackground = TextPrimaryLight,

    surface = ParchmentWhite,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceTint = SunnyGold,
    inverseSurface = DeepSlate,
    inverseOnSurface = TextPrimaryDark,

    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,

    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = Scrim,
)

private val DarkColorScheme = darkColorScheme(
    primary = WarmGinger,
    onPrimary = OnWarmGinger,
    primaryContainer = WarmGingerContainer,
    onPrimaryContainer = OnWarmGingerContainer,
    inversePrimary = SunnyGold,

    secondary = PaleMoon,
    onSecondary = OnPaleMoon,
    secondaryContainer = PaleMoonContainer,
    onSecondaryContainer = OnPaleMoonContainer,

    tertiary = SageNight,
    onTertiary = OnSageNight,
    tertiaryContainer = SageNightContainer,
    onTertiaryContainer = OnSageNightContainer,

    background = DeepSlate,
    onBackground = TextPrimaryDark,

    surface = DeepSlate,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceTint = WarmGinger,
    inverseSurface = ParchmentWhite,
    inverseOnSurface = TextPrimaryLight,

    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,

    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = Scrim,
)

@SuppressLint("NewApi")
@OptIn(UnstableSaltApi::class)
@Composable
fun DailyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val useDynamicColor = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val materialColorScheme = remember(darkTheme, useDynamicColor) {
        when {
            useDynamicColor -> {
                if (darkTheme) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            }
            else -> {
                if (darkTheme) DarkColorScheme
                else LightColorScheme
            }
        }
    }

    val extendedColorScheme = remember(materialColorScheme, darkTheme, useDynamicColor) {
        if (useDynamicColor) {
            if (darkTheme) {
                ExtendedColorScheme(
                    headerContainer = materialColorScheme.primaryContainer,
                    onHeaderContainer = materialColorScheme.onPrimaryContainer,
                    success = SuccessGreen
                )
            } else {
                ExtendedColorScheme(
                    headerContainer = materialColorScheme.primary,
                    onHeaderContainer = materialColorScheme.onPrimary,
                    success = SuccessGreen
                )
            }
        } else {
            if (darkTheme) darkExtendedColorScheme else lightExtendedColorScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        SideEffect {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val customSaltTextStyles = saltTextStyles(
        main = TextStyle(
            fontFamily = appFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        ),
        sub = TextStyle(
            fontFamily = appFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        ),
        paragraph = TextStyle(
            fontFamily = appFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 22.sp
        )
    )

    CompositionLocalProvider(LocalExtendedColorScheme provides extendedColorScheme) {
        SaltTheme(
            colors = saltColorsByColorScheme(materialColorScheme),
            configs = saltConfigs(isDarkTheme = darkTheme),
            textStyles = customSaltTextStyles
        ) {
            MaterialTheme(
                colorScheme = materialColorScheme,
                typography = Typography,
                content = content
            )
        }
    }
}
