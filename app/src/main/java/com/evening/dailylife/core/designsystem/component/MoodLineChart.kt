package com.evening.dailylife.core.designsystem.component

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evening.dailylife.feature.chart.ChartDataCalculator
import com.evening.dailylife.feature.chart.ChartPeriod
import com.evening.dailylife.feature.chart.MoodChartEntry
import java.util.Locale
import kotlin.math.abs

@Composable
fun MoodLineChart(
    entries: List<MoodChartEntry>,
    period: ChartPeriod,
    modifier: Modifier = Modifier,
    maxChartHeight: Dp = 160.dp,
    stepSpacing: Dp = 48.dp,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    pointColor: Color = MaterialTheme.colorScheme.primary,
    gridColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
    axisColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f),
    xLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    yLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    yLabelBgColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
    yLabelFormatter: (Float) -> String = { value ->
        if (value % 1f == 0f) value.toInt().toString() else String.format(Locale.CHINA, "%.1f", value)
    },
    animationSpec: AnimationSpec<Float> = tween(durationMillis = 600, easing = FastOutSlowInEasing),
    animationKey: Any? = null,
) {
    val density = LocalDensity.current

    val moodValues = entries.mapNotNull { it.value }
    val maxAbsValue = moodValues.maxOfOrNull { abs(it) } ?: 0f
    val targetMax = maxAbsValue.coerceAtLeast(2f)
    val roundedMax = ChartDataCalculator.roundUpToNiceNumber(targetMax)
    val yMax = if (roundedMax > 0f) roundedMax else 2f
    val yMin = -yMax

    val steps = 4

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(entries, animationKey) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = animationSpec)
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val chartMaxWidth = this.maxWidth
        val pointCount = entries.size
        val interpolatedCount = (pointCount - 1).coerceAtLeast(1)
        val leftPadding = 8.dp
        val rightPadding = 8.dp
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
            val xLabelTextSizePx: Float,
            val xLabelBaselineOffsetPx: Float,
            val pointRadiusPx: Float,
            val lineStrokePx: Float,
            val gridStrokePx: Float,
            val axisStrokePx: Float,
            val spacingPx: Float,
            val yLabelTextSizePx: Float,
            val yLabelPadHPx: Float,
            val yLabelPadVPx: Float,
            val yLabelCornerPx: Float,
            val yLabelInsideGapPx: Float
        )

        val px = remember(density, usedSpacing) {
            with(density) {
                Px(
                    leftPaddingPx = leftPadding.toPx(),
                    rightPaddingPx = rightPadding.toPx(),
                    topPaddingPx = 8.dp.toPx(),
                    bottomPaddingPx = 24.dp.toPx(),
                    xLabelTextSizePx = 10.sp.toPx(),
                    xLabelBaselineOffsetPx = 16.dp.toPx(),
                    pointRadiusPx = 4.dp.toPx(),
                    lineStrokePx = 2.dp.toPx(),
                    gridStrokePx = 0.5.dp.toPx(),
                    axisStrokePx = 1.dp.toPx(),
                    spacingPx = usedSpacing.toPx(),
                    yLabelTextSizePx = 10.sp.toPx(),
                    yLabelPadHPx = 4.dp.toPx(),
                    yLabelPadVPx = 2.dp.toPx(),
                    yLabelCornerPx = 6.dp.toPx(),
                    yLabelInsideGapPx = 4.dp.toPx()
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
                textAlign = Paint.Align.LEFT
                color = yLabelColor.toArgb()
                textSize = px.yLabelTextSizePx
            }
        }
        val yLabelFontMetrics = remember(yLabelPaint) { yLabelPaint.fontMetrics }
        val yLabelTextHeight = remember(yLabelFontMetrics) {
            yLabelFontMetrics.descent - yLabelFontMetrics.ascent
        }

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

            drawLine(
                color = axisColor,
                start = Offset(chartLeft, chartTop),
                end = Offset(chartLeft, chartBottom),
                strokeWidth = px.axisStrokePx
            )
            drawLine(
                color = axisColor,
                start = Offset(chartRight, chartTop),
                end = Offset(chartRight, chartBottom),
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

            val animatedPoints = entries.mapIndexedNotNull { index, entry ->
                entry.value?.let { value ->
                    val x = if (pointCount <= 1) {
                        chartLeft + chartWidthPx / 2f
                    } else {
                        chartLeft + spacing * index
                    }
                    val targetY = valueToY(value)
                    val animatedY = zeroY + (targetY - zeroY) * animationProgress.value
                    Offset(x, animatedY)
                }
            }

            for (i in 0 until animatedPoints.size - 1) {
                drawLine(
                    color = lineColor,
                    start = animatedPoints[i],
                    end = animatedPoints[i + 1],
                    strokeWidth = px.lineStrokePx,
                    cap = StrokeCap.Round
                )
            }

            animatedPoints.forEach { point ->
                drawCircle(
                    color = pointColor,
                    radius = px.pointRadiusPx,
                    center = point
                )
            }

            val yAxisLabels = (0..steps).map { step ->
                val fraction = step / steps.toFloat()
                val value = yMax - (yMax - yMin) * fraction
                yLabelFormatter(value)
            }

            drawIntoCanvas { canvas ->
                entries.forEachIndexed { index, entry ->
                    val x = if (pointCount <= 1) {
                        chartLeft + chartWidthPx / 2f
                    } else {
                        chartLeft + spacing * index
                    }
                    val labelText = when (period) {
                        ChartPeriod.Week -> entry.label.substringBefore(' ').ifBlank { entry.label }
                        else -> entry.label
                    }
                    canvas.nativeCanvas.drawText(
                        labelText,
                        x,
                        chartBottom + px.xLabelBaselineOffsetPx,
                        xLabelPaint
                    )
                }
            }

            for (i in 0..steps) {
                val label = yAxisLabels[i]
                val fraction = i / steps.toFloat()
                val y = chartTop + chartHeightPx * fraction
                val textWidth = yLabelPaint.measureText(label)
                val bgWidth = textWidth + 2 * px.yLabelPadHPx
                val bgHeight = yLabelTextHeight + 2 * px.yLabelPadVPx
                val bgLeft = chartLeft + px.yLabelInsideGapPx
                val bgTop = (y - bgHeight / 2f).coerceIn(chartTop, chartBottom - bgHeight)

                drawRoundRect(
                    color = yLabelBgColor,
                    topLeft = Offset(bgLeft, bgTop),
                    size = Size(bgWidth, bgHeight),
                    cornerRadius = CornerRadius(px.yLabelCornerPx, px.yLabelCornerPx)
                )

                val baselineY = bgTop + px.yLabelPadVPx - yLabelFontMetrics.ascent
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        label,
                        bgLeft + px.yLabelPadHPx,
                        baselineY,
                        yLabelPaint
                    )
                }
            }
        }
    }
}
