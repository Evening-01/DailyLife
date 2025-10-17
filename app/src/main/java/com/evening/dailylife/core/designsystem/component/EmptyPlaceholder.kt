package com.evening.dailylife.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 通用占位状态组件，支持自定义图标与提示文案。
 */
@Composable
fun EmptyPlaceholder(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint
            )
            Text(
                text = message,
                style = textStyle,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
