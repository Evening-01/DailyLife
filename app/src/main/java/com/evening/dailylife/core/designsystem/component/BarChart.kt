package com.evening.dailylife.core.designsystem.component

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evening.dailylife.R
import com.evening.dailylife.feature.chart.ChartDataCalculator
import com.evening.dailylife.feature.chart.ChartEntry
import java.util.Locale
import kotlin.math.min

@Composable
fun BarChart(
    entries: List<ChartEntry>,
    modifier: Modifier = Modifier,
    averageValue: Float = 0f,
    maxBarHeight: Dp = 140.dp,
    barWidth: Dp = 28.dp,
    spacing: Dp = 14.dp,
    barColor: Color = MaterialTheme.colorScheme.primary,
    gridColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
    axisColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f),
    averageLineColor: Color = MaterialTheme.colorScheme.tertiary,
    averageLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    yLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    yLabelBgColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
    xLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    yAxisOvershootTop: Dp = 10.dp,
    gridStrokeWidth: Dp = 0.5.dp,
    valueFormatter: (Float) -> String = { value ->
        if (value % 1f == 0f) value.toInt().toString() else String.format(Locale.CHINA, "%.1f", value)
    },
    labelFormatter: (String) -> String = { it },
    barAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 600, easing = FastOutSlowInEasing),
    animationKey: Any? = null,
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
    val context = LocalContext.current

    val rawMaxValue = remember(entries, averageValue) {
        val entryMax = entries.maxOfOrNull(ChartEntry::value) ?: 0f
        maxOf(entryMax, averageValue)
    }
    val maxValue = remember(rawMaxValue) {
        val rounded = ChartDataCalculator.roundUpToNiceNumber(rawMaxValue)
        if (rounded > 0f) rounded else 1f
    }

    val steps = 4
    val currentFormatter by rememberUpdatedState(valueFormatter)

    val yAxisLabels = remember(maxValue) {
        (0..steps).map { step ->
            val fraction = step / steps.toFloat()
            currentFormatter(maxValue * (1f - fraction))
        }
    }

    val minChartWidth = 240.dp
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        val maxWidthDp = this.maxWidth
        val count = entries.size
        val totalSpacing = spacing * (count - 1)
        val sidePadding = spacing / 2

        val availableChartWidth = maxWidthDp
        val desiredChartWidth = (sidePadding * 2 + barWidth * count + totalSpacing).coerceAtLeast(minChartWidth)
        val shouldScroll = desiredChartWidth > availableChartWidth

        val usedBarWidth: Dp = if (shouldScroll) {
            barWidth
        } else {
            val barsArea = (availableChartWidth - totalSpacing - sidePadding * 2).coerceAtLeast(0.dp)
            (barsArea / count).coerceIn(8.dp, barWidth)
        }
        val usedChartWidth: Dp = if (shouldScroll) desiredChartWidth else availableChartWidth

        data class Px(
            val barWidthPx: Float,
            val spacingPx: Float,
            val sidePaddingPx: Float,
            val barCornerRadiusPx: Float,
            val axisStrokePx: Float,
            val gridStrokePx: Float,
            val overshootTopPx: Float,
            val yLabelTextSizePx: Float,
            val yLabelPadHPx: Float,
            val yLabelPadVPx: Float,
            val yLabelCornerPx: Float,
            val yLabelInsideGapPx: Float,
            val avgTextSizePx: Float,
            val avgTextRightPadPx: Float,
            val avgTextTopPadPx: Float,
        )

        val px = remember(density, usedBarWidth, spacing, sidePadding, gridStrokeWidth, yAxisOvershootTop) {
            with(density) {
                Px(
                    barWidthPx = usedBarWidth.toPx(),
                    spacingPx = spacing.toPx(),
                    sidePaddingPx = sidePadding.toPx(),
                    barCornerRadiusPx = 8.dp.toPx(),
                    axisStrokePx = 1.dp.toPx(),
                    gridStrokePx = gridStrokeWidth.toPx(),
                    overshootTopPx = yAxisOvershootTop.toPx(),
                    yLabelTextSizePx = 10.sp.toPx(),
                    yLabelPadHPx = 4.dp.toPx(),
                    yLabelPadVPx = 2.dp.toPx(),
                    yLabelCornerPx = 6.dp.toPx(),
                    yLabelInsideGapPx = 4.dp.toPx(),
                    avgTextSizePx = 12.sp.toPx(),
                    avgTextRightPadPx = 4.dp.toPx(),
                    avgTextTopPadPx = 6.dp.toPx(),
                )
            }
        }

        val yLabelPaint = remember(yLabelColor, px.yLabelTextSizePx) {
            Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.LEFT
                color = yLabelColor.toArgb()
                textSize = px.yLabelTextSizePx
            }
        }
        val yLabelFontMetrics = remember(yLabelPaint) { yLabelPaint.fontMetrics }
        val yLabelTextHeight = remember(yLabelFontMetrics) { yLabelFontMetrics.descent - yLabelFontMetrics.ascent }

        val avgLabelPaint = remember(averageLabelColor, px.avgTextSizePx) {
            Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.RIGHT
                color = averageLabelColor.toArgb()
                textSize = px.avgTextSizePx
            }
        }

        val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(12f, 12f)) }
        val animationProgress = remember { Animatable(0f) }

        LaunchedEffect(entries, animationKey) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(1f, animationSpec = barAnimationSpec)
        }

        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(maxBarHeight)
            ) {
                val chartCanvasModifier =
                    if (shouldScroll) Modifier
                        .horizontalScroll(scrollState)
                        .width(usedChartWidth)
                        .fillMaxHeight()
                    else Modifier
                        .width(usedChartWidth)
                        .fillMaxHeight()

                Canvas(modifier = chartCanvasModifier) {
                    val contentWidth = size.width
                    val contentHeight = size.height

                    val stepHeight = contentHeight / steps
                    for (i in 0..steps) {
                        val y = contentHeight - i * stepHeight
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(contentWidth, y),
                            strokeWidth = px.gridStrokePx
                        )
                    }

                    entries.forEachIndexed { index, entry ->
                        val barHeightTarget = if (maxValue == 0f) 0f else (entry.value / maxValue) * contentHeight
                        val barHeight = barHeightTarget * animationProgress.value
                        val left = px.sidePaddingPx + index * (px.barWidthPx + px.spacingPx)
                        val top = contentHeight - barHeight
                        val right = left + px.barWidthPx
                        val bottom = contentHeight
                        val dynamicRadius = min(px.barCornerRadiusPx, barHeight / 2f)

                        val roundRect = RoundRect(
                            rect = Rect(left, top, right, bottom),
                            topLeft = CornerRadius(dynamicRadius, dynamicRadius),
                            topRight = CornerRadius(dynamicRadius, dynamicRadius),
                            bottomRight = CornerRadius.Zero,
                            bottomLeft = CornerRadius.Zero
                        )
                        val path = Path().apply { addRoundRect(roundRect) }
                        drawPath(path = path, color = barColor)
                    }

                    if (averageValue > 0f && maxValue > 0f) {
                        val averageLabel = context.getString(
                            R.string.chart_average_label,
                            currentFormatter(averageValue)
                        )
                        val ratio = (averageValue / maxValue).coerceAtMost(1f)
                        val y = contentHeight - (contentHeight * ratio)
                        drawLine(
                            color = averageLineColor,
                            start = Offset(0f, y),
                            end = Offset(contentWidth, y),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = dashEffect
                        )
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(
                                averageLabel,
                                contentWidth - px.avgTextRightPadPx,
                                (y - px.avgTextTopPadPx).coerceAtLeast(0f),
                                avgLabelPaint
                            )
                        }
                    }
                }

                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    for (i in 0..steps) {
                        val label = yAxisLabels[i]
                        val fractionTopToBottom = i / steps.toFloat()
                        val yCenter = h * fractionTopToBottom

                        val textWidth = yLabelPaint.measureText(label)
                        val bgW = textWidth + 2 * px.yLabelPadHPx
                        val bgH = yLabelTextHeight + 2 * px.yLabelPadVPx

                        val bgLeft = 0f + px.yLabelInsideGapPx
                        val bgTop = (yCenter - bgH / 2f).coerceIn(0f, h - bgH)

                        drawRoundRect(
                            color = yLabelBgColor,
                            topLeft = Offset(bgLeft, bgTop),
                            size = Size(bgW, bgH),
                            cornerRadius = CornerRadius(px.yLabelCornerPx, px.yLabelCornerPx)
                        )
                        val baselineY = bgTop + px.yLabelPadVPx - yLabelFontMetrics.ascent
                        drawIntoCanvas { c ->
                            c.nativeCanvas.drawText(
                                label,
                                bgLeft + px.yLabelPadHPx,
                                baselineY,
                                yLabelPaint
                            )
                        }
                    }

                    if (px.axisStrokePx > 0f && w > 0f && h > 0f) {
                        val strokeHalf = px.axisStrokePx / 2f
                        val leftX = strokeHalf
                        val rightX = w - strokeHalf
                        val topY = strokeHalf
                        val bottomY = h - strokeHalf

                        drawLine(
                            color = axisColor,
                            start = Offset(leftX, topY),
                            end = Offset(leftX, bottomY),
                            strokeWidth = px.axisStrokePx
                        )
                        drawLine(
                            color = axisColor,
                            start = Offset(leftX, bottomY),
                            end = Offset(rightX, bottomY),
                            strokeWidth = px.axisStrokePx
                        )
                        drawLine(
                            color = axisColor,
                            start = Offset(leftX, topY),
                            end = Offset(rightX, topY),
                            strokeWidth = px.axisStrokePx
                        )

                        val showRightAxis = if (!shouldScroll) {
                            true
                        } else {
                            scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue
                        }
                        if (showRightAxis) {
                            drawLine(
                                color = axisColor,
                                start = Offset(rightX, topY),
                                end = Offset(rightX, bottomY),
                                strokeWidth = px.axisStrokePx
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val labelsRowModifier =
                    if (shouldScroll) Modifier
                        .horizontalScroll(scrollState)
                        .width(usedChartWidth)
                    else Modifier.width(usedChartWidth)

                Row(
                    modifier = labelsRowModifier,
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(sidePadding))
                    entries.forEachIndexed { index, entry ->
                        Box(
                            modifier = Modifier.width(usedBarWidth),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = labelFormatter(entry.label),
                                style = MaterialTheme.typography.labelMedium,
                                color = xLabelColor,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                        if (index != entries.lastIndex) {
                            Spacer(modifier = Modifier.width(spacing))
                        }
                    }
                    Spacer(modifier = Modifier.width(sidePadding))
                }
            }
        }
    }
}