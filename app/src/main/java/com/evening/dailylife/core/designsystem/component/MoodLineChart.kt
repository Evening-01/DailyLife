package com.evening.dailylife.core.designsystem.component

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import com.evening.dailylife.feature.chart.MoodChartEntry
import java.util.Locale
import kotlin.math.abs

@Composable
fun MoodLineChart(
    entries: List<MoodChartEntry>,
    modifier: Modifier = Modifier,
    maxChartHeight: Dp = 160.dp,
    stepSpacing: Dp = 48.dp,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    pointColor: Color = MaterialTheme.colorScheme.primary,
    gridColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
    axisColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f),
    yLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    xLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    valueFormatter: (Float) -> String = { value ->
        String.format(Locale.CHINA, "%.1f", value)
    },
    animationSpec: AnimationSpec<Float> = tween(durationMillis = 600, easing = FastOutSlowInEasing),
    animationKey: Any? = null,
) {
    val hasData = entries.any { it.value != null }
    if (!hasData) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(maxChartHeight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.chart_empty_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val density = LocalDensity.current

    val moodValues = entries.mapNotNull { it.value }
    val maxAbsValue = moodValues.maxOf { abs(it) }
    val targetMax = maxAbsValue.coerceAtLeast(2f)
    val roundedMax = ChartDataCalculator.roundUpToNiceNumber(targetMax)
    val yMax = if (roundedMax > 0f) roundedMax else 2f
    val yMin = -yMax

    val steps = 4
    val yAxisLabels = remember(yMax, yMin, steps, valueFormatter) {
        val formatter = valueFormatter
        (0..steps).map { step ->
            val fraction = step / steps.toFloat()
            val value = yMax - fraction * (yMax - yMin)
            formatter(value)
        }
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(entries, animationKey) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = animationSpec)
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val chartMaxWidth = this.maxWidth
        val pointCount = entries.size
        val interpolatedCount = (pointCount - 1).coerceAtLeast(1)
        val leftPadding = 44.dp
        val rightPadding = 16.dp
        val shouldScroll = pointCount > 1 && (leftPadding + rightPadding + stepSpacing * interpolatedCount) > chartMaxWidth
        val usedSpacing = when {
            pointCount <= 1 -> 0.dp
            shouldScroll -> stepSpacing
            else -> (chartMaxWidth - leftPadding - rightPadding) / interpolatedCount
        }
        val chartWidth = when {
            pointCount <= 1 -> chartMaxWidth
            shouldScroll -> leftPadding + rightPadding + stepSpacing * interpolatedCount
            else -> chartMaxWidth
        }

        val scrollState = rememberScrollState()

        data class Px(
            val leftPaddingPx: Float,
            val rightPaddingPx: Float,
            val topPaddingPx: Float,
            val bottomPaddingPx: Float,
            val yLabelTextSizePx: Float,
            val yLabelSpacePx: Float,
            val xLabelTextSizePx: Float,
            val xLabelBaselineOffsetPx: Float,
            val pointRadiusPx: Float,
            val lineStrokePx: Float,
            val gridStrokePx: Float,
            val axisStrokePx: Float,
            val spacingPx: Float
        )

        val px = remember(density, usedSpacing) {
            with(density) {
                Px(
                    leftPaddingPx = leftPadding.toPx(),
                    rightPaddingPx = rightPadding.toPx(),
                    topPaddingPx = 16.dp.toPx(),
                    bottomPaddingPx = 40.dp.toPx(),
                    yLabelTextSizePx = 10.sp.toPx(),
                    yLabelSpacePx = 8.dp.toPx(),
                    xLabelTextSizePx = 10.sp.toPx(),
                    xLabelBaselineOffsetPx = 12.dp.toPx(),
                    pointRadiusPx = 4.dp.toPx(),
                    lineStrokePx = 2.dp.toPx(),
                    gridStrokePx = 0.5.dp.toPx(),
                    axisStrokePx = 1.dp.toPx(),
                    spacingPx = usedSpacing.toPx()
                )
            }
        }

        val xLabelPaint = remember(xLabelColor, px.xLabelTextSizePx) {
            Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                color = xLabelColor.toArgb()
                textSize = px.xLabelTextSizePx
            }
        }

        val yLabelPaint = remember(yLabelColor, px.yLabelTextSizePx) {
            Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.RIGHT
                color = yLabelColor.toArgb()
                textSize = px.yLabelTextSizePx
            }
        }
        val yLabelFontMetrics = remember(yLabelPaint) { yLabelPaint.fontMetrics }

        val chartModifier = if (shouldScroll) {
            Modifier
                .horizontalScroll(scrollState)
                .width(chartWidth)
                .height(maxChartHeight)
        } else {
            Modifier
                .width(chartWidth)
                .height(maxChartHeight)
        }

        Canvas(modifier = chartModifier) {
            val width = size.width
            val height = size.height

            val chartLeft = px.leftPaddingPx
            val chartRight = width - px.rightPaddingPx
            val chartTop = px.topPaddingPx
            val chartBottom = height - px.bottomPaddingPx

            val chartWidthPx = chartRight - chartLeft
            val chartHeightPx = chartBottom - chartTop

            val zeroY = if (yMax == yMin) chartBottom else {
                val ratio = (0f - yMin) / (yMax - yMin)
                chartBottom - chartHeightPx * ratio
            }

            drawRect(color = backgroundColor, size = size)

            // horizontal grid lines
            for (i in 0..steps) {
                val fraction = i / steps.toFloat()
                val y = chartTop + chartHeightPx * fraction
                drawLine(
                    color = gridColor,
                    start = Offset(chartLeft, y),
                    end = Offset(chartRight, y),
                    strokeWidth = px.gridStrokePx
                )
            }

            // axes
            drawLine(
                color = axisColor,
                start = Offset(chartLeft, chartTop),
                end = Offset(chartLeft, chartBottom),
                strokeWidth = px.axisStrokePx
            )
            drawLine(
                color = axisColor,
                start = Offset(chartLeft, chartBottom),
                end = Offset(chartRight, chartBottom),
                strokeWidth = px.axisStrokePx
            )

            if (0f in yMin..yMax) {
                drawLine(
                    color = axisColor.copy(alpha = 0.6f),
                    start = Offset(chartLeft, zeroY),
                    end = Offset(chartRight, zeroY),
                    strokeWidth = px.axisStrokePx
                )
            }

            fun valueToY(value: Float): Float {
                if (yMax == yMin) return chartBottom
                val ratio = (value - yMin) / (yMax - yMin)
                return chartBottom - chartHeightPx * ratio
            }

            val spacing = if (pointCount <= 1) {
                0f
            } else if (px.spacingPx.isFinite()) {
                px.spacingPx
            } else {
                chartWidthPx / interpolatedCount
            }

            var lastPoint: Offset? = null
            entries.forEachIndexed { index, entry ->
                val value = entry.value ?: run {
                    lastPoint = null
                    return@forEachIndexed
                }

                val x = if (pointCount <= 1) {
                    chartLeft + chartWidthPx / 2f
                } else {
                    chartLeft + spacing * index
                }
                val targetY = valueToY(value)
                val animatedY = zeroY + (targetY - zeroY) * animationProgress.value
                val currentPoint = Offset(x, animatedY)

                lastPoint?.let { previous ->
                    drawLine(
                        color = lineColor,
                        start = previous,
                        end = currentPoint,
                        strokeWidth = px.lineStrokePx,
                        cap = StrokeCap.Round
                    )
                }
                lastPoint = currentPoint

                drawCircle(
                    color = pointColor,
                    radius = px.pointRadiusPx,
                    center = currentPoint
                )
            }

            drawIntoCanvas { canvas ->
                // y-axis labels
                yAxisLabels.forEachIndexed { index, label ->
                    val fraction = index / steps.toFloat()
                    val y = chartTop + chartHeightPx * fraction
                    val baseline = y - (yLabelFontMetrics.ascent + yLabelFontMetrics.descent) / 2f
                    canvas.nativeCanvas.drawText(
                        label,
                        chartLeft - px.yLabelSpacePx,
                        baseline,
                        yLabelPaint
                    )
                }

                // x-axis labels
                entries.forEachIndexed { index, entry ->
                    val x = if (pointCount <= 1) {
                        chartLeft + chartWidthPx / 2f
                    } else {
                        chartLeft + spacing * index
                    }
                    canvas.nativeCanvas.drawText(
                        entry.label,
                        x,
                        chartBottom + px.xLabelBaselineOffsetPx,
                        xLabelPaint
                    )
                }
            }
        }
    }
}
