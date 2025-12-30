package com.evening.dailylife.core.ui.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AnimatedBottomBarIcon(
    imageVector: ImageVector,
    isSelected: Boolean,
    contentDescription: String? = null
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
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
    )
}
