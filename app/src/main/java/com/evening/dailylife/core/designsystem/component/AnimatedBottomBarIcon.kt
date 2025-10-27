package com.evening.dailylife.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.evening.dailylife.app.navigation.Screen

@Composable
fun AnimatedBottomBarIcon(
    screen: Screen,
    isSelected: Boolean
) {
    val targetScale = if (isSelected) 1.08f else 1f
    val targetAlpha = if (isSelected) 1f else 0.72f

    val scale by animateFloatAsState(
        targetValue = targetScale,
        label = "bottom_bar_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        label = "bottom_bar_alpha"
    )

    Icon(
        imageVector = screen.icon,
        contentDescription = null,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
    )
}
