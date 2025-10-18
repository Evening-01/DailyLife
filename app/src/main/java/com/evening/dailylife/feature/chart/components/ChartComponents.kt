package com.evening.dailylife.feature.chart.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.evening.dailylife.R
import com.evening.dailylife.core.designsystem.component.BarChart
import com.evening.dailylife.feature.chart.ChartContentStatus
import com.evening.dailylife.feature.chart.ChartPeriod
import com.evening.dailylife.feature.chart.ChartRangeOption
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn

@Composable
fun ChartPeriodSelector(
    selectedPeriod: ChartPeriod,
    onPeriodSelected: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val periods = ChartPeriod.entries.toTypedArray()
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
    ) {
        periods.forEachIndexed { index, period ->
            SegmentedButton(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    activeBorderColor = Color.Black,
                    inactiveBorderColor = Color.Black
                ),
                icon = {},
                label = {
                    Text(
                        text = stringResource(id = period.labelRes),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}

@Composable
fun ChartRangeTabRow(
    rangeTabs: List<ChartRangeOption>,
    selectedOptionId: String?,
    onRangeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = rangeTabs.indexOfFirst { it.id == selectedOptionId }.takeIf { it >= 0 } ?: 0

    SecondaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        edgePadding = 0.dp,
        containerColor = Color.Transparent,
        divider = {},
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedIndex),
                color = MaterialTheme.colorScheme.primary,
                height = 2.dp
            )
        }
    ) {
        rangeTabs.forEachIndexed { index, option ->
            val interactionSource = remember { MutableInteractionSource() }
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .heightIn(min = 30.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onRangeSelected(option.id) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun ChartOverviewSection(
    title: String,
    totalDescription: String,
    averageDescription: String,
    contentStatus: ChartContentStatus,
    entries: List<com.evening.dailylife.feature.chart.ChartEntry>,
    averageValue: Double,
    valueFormatter: (Double) -> String,
    animationKey: Int,
    modifier: Modifier = Modifier
) {
    RoundedColumn(modifier = modifier.fillMaxWidth()) {
        ItemTitle(text = title)

        Text(
            text = totalDescription,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Text(
            text = averageDescription,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        when (contentStatus) {
            ChartContentStatus.Loading -> {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                )
            }
            ChartContentStatus.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.chart_empty_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            ChartContentStatus.Content -> {
                if (entries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp)
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                } else {
                    BarChart(
                        entries = entries,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        averageValue = averageValue.toFloat(),
                        valueFormatter = { value -> valueFormatter(value.toDouble()) },
                        animationKey = animationKey
                    )
                }
            }
        }
    }
}
