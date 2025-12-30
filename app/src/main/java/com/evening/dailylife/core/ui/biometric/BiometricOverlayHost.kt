package com.evening.dailylife.core.ui.biometric

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.unit.dp

interface BiometricOverlayController {
    fun setVisible(visible: Boolean)
}

private object NoOpBiometricOverlayController : BiometricOverlayController {
    override fun setVisible(visible: Boolean) = Unit
}

val LocalBiometricOverlayController = staticCompositionLocalOf<BiometricOverlayController> {
    NoOpBiometricOverlayController
}

@OptIn(ExperimentalGraphicsApi::class)
@Composable
fun BiometricOverlayHost(
    lockRequired: Boolean,
    content: @Composable () -> Unit,
) {
    val manualVisibleState = remember { mutableStateOf(false) }
    val controller = remember {
        object : BiometricOverlayController {
            override fun setVisible(visible: Boolean) {
                manualVisibleState.value = visible
            }
        }
    }
    val showOverlay = lockRequired || manualVisibleState.value
    val blurRadius by animateDpAsState(
        targetValue = if (showOverlay) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 220),
        label = "biometricBlurRadius",
    )
    val scrimAlpha by animateFloatAsState(
        targetValue = if (showOverlay) 0.3f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "biometricScrimAlpha",
    )
    val blurModifier = if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier

    CompositionLocalProvider(LocalBiometricOverlayController provides controller) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().then(blurModifier)) {
                content()
            }
            if (scrimAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = scrimAlpha))
                )
            }
        }
    }
}
