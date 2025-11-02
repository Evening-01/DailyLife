package com.evening.dailylife.feature.discover.component

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.evening.dailylife.R
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * “常用工具” 区域，展示静态入口卡片并统一处理点击反馈。
 */
@Composable
fun DiscoverCommonToolsSection(
    modifier: Modifier = Modifier,
    onMortgageCalculatorClick: () -> Unit,
) {
    val context = LocalContext.current
    val toastComingSoon = {
        Toast
            .makeText(context, R.string.discover_ai_toast, Toast.LENGTH_SHORT)
            .show()
    }
    val tools = listOf(
        DiscoverCommonTool(
            icon = Icons.Outlined.Calculate,
            title = stringResource(id = R.string.discover_common_tool_mortgage_title),
            onClick = onMortgageCalculatorClick,
        ),
        DiscoverCommonTool(
            icon = Icons.Outlined.SwapHoriz,
            title = stringResource(id = R.string.discover_common_tool_fx_title),
            onClick = toastComingSoon,
        ),
        DiscoverCommonTool(
            icon = Icons.Outlined.Inventory2,
            title = stringResource(id = R.string.discover_common_tool_check_title),
            onClick = toastComingSoon,
        ),
        DiscoverCommonTool(
            icon = Icons.Outlined.AccountBalance,
            title = stringResource(id = R.string.discover_common_tool_budget_title),
            onClick = toastComingSoon,
        ),
    )
    RoundedColumn(modifier = modifier) {
        ItemTitle(text = stringResource(id = R.string.discover_common_tools_title))
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            tools.forEach { tool ->
                DiscoverCommonToolCard(
                    title = tool.title,
                    icon = tool.icon,
                    onClick = tool.onClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DiscoverCommonToolCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var isClickEnabled by remember { mutableStateOf(true) }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = isClickEnabled,
                onClick = {
                    if (!isClickEnabled) return@clickable
                    isClickEnabled = false
                    onClick()
                    coroutineScope.launch {
                        delay(DEBOUNCE_DURATION_MILLIS)
                        isClickEnabled = true
                    }
                },
            )
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

private data class DiscoverCommonTool(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit,
)

private const val DEBOUNCE_DURATION_MILLIS = 600L
