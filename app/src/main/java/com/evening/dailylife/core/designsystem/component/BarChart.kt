package com.evening.dailylife.core.designsystem.component

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evening.dailylife.R
import com.evening.dailylife.feature.chart.ChartDataCalculator
import com.evening.dailylife.feature.chart.ChartEntry

@Composable
fun BarChart(
    entries: List<ChartEntry>,
    modifier: Modifier = Modifier,
    averageValue: Float = 0f,
    maxBarHeight: Dp = 220.dp,
    barWidth: Dp = 32.dp,
    spacing: Dp = 16.dp,
    axisLabelWidth: Dp = 48.dp,
    barColor: Color = MaterialTheme.colorScheme.primary,
    gridColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    axisColor: Color = MaterialTheme.colorScheme.outline,
    averageLineColor: Color = MaterialTheme.colorScheme.tertiary,
    averageLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueFormatter: (Float) -> String = { value ->
        if (value % 1f == 0f) {
            value.toInt().toString()
        } else {
            String.format("%.1f", value)
        }
    }
) {
    if (entries.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.chart_empty_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val density = LocalDensity.current
    val barWidthPx = with(density) { barWidth.toPx() }
    val spacingPx = with(density) { spacing.toPx() }
    val barCornerRadius = with(density) { 8.dp.toPx() }

    val rawMaxValue = remember(entries, averageValue) {
        val entryMax = entries.maxOfOrNull(ChartEntry::value) ?: 0f
        maxOf(entryMax, averageValue)
    }
    val maxValue = remember(rawMaxValue) {
        val rounded = ChartDataCalculator.roundUpToNiceNumber(rawMaxValue)
        if (rounded > 0f) rounded else 1f
    }
    val steps = 4
    val yAxisLabels = remember(maxValue) {
        (0..steps).map { step ->
            val fraction = step / steps.toFloat()
            valueFormatter(maxValue * fraction)
        }.reversed()
    }

    val minChartWidth = 240.dp
    val calculatedWidth = with(density) {
        val barsWidth = barWidthPx * entries.size
        val spacesWidth = spacingPx * (entries.size - 1).coerceAtLeast(0)
        (barsWidth + spacesWidth).toDp()
    }
    val chartWidth = maxOf(minChartWidth, calculatedWidth)

    val scrollState = rememberScrollState()

    // 整体布局是一个 Row，包含 Y 轴 和 图表主体 两部分
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 第一部分：Y 轴标签
        Column(
            modifier = Modifier
                .width(axisLabelWidth)
                .height(maxBarHeight),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            yAxisLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
            }
        }

        // 第二部分：图表主体（Canvas + X轴），可以水平滚动
        Column(
            modifier = Modifier.horizontalScroll(scrollState)
        ) {
            // 图表 Canvas
            Box(
                modifier = Modifier
                    .height(maxBarHeight)
                    .width(chartWidth)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val contentWidth = size.width
                    val contentHeight = size.height

                    // Grid lines
                    for (step in 0..steps) {
                        val fraction = step / steps.toFloat()
                        val y = contentHeight - (contentHeight * fraction)
                        drawLine(
                            color = if (step == steps) axisColor else gridColor.copy(alpha = 0.6f),
                            start = Offset(0f, y),
                            end = Offset(contentWidth, y),
                            strokeWidth = if (step == steps) 1.dp.toPx() else 0.5.dp.toPx()
                        )
                    }

                    // Y axis
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, contentHeight),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Bars
                    entries.forEachIndexed { index, entry ->
                        val barHeight = if (maxValue == 0f) 0f else (entry.value / maxValue) * contentHeight
                        val left = index * (barWidthPx + spacingPx)
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, contentHeight - barHeight),
                            size = Size(barWidthPx, barHeight),
                            cornerRadius = CornerRadius(barCornerRadius, barCornerRadius)
                        )
                    }

                    // Average line
                    if (averageValue > 0f && maxValue > 0f) {
                        val ratio = (averageValue / maxValue).coerceAtMost(1f)
                        val y = contentHeight - (contentHeight * ratio)
                        drawLine(
                            color = averageLineColor,
                            start = Offset(0f, y),
                            end = Offset(contentWidth, y),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
                        )

                        val paint = Paint().apply {
                            color = averageLabelColor.toArgb()
                            textSize = with(density) { 12.sp.toPx() }
                            textAlign = Paint.Align.RIGHT
                            isAntiAlias = true
                        }
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(
                                "平均 ${valueFormatter(averageValue)}",
                                contentWidth,
                                y - 8.dp.toPx(),
                                paint
                            )
                        }
                    }
                }
            }

            // X 轴标签
            Row(
                modifier = Modifier
                    .width(chartWidth)
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                entries.forEach { entry ->
                    Box(
                        modifier = Modifier.width(barWidth),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}