package com.evening.dailylife.core.designsystem.component

import android.graphics.Paint
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
    maxBarHeight: Dp = 220.dp,
    barWidth: Dp = 32.dp,
    spacing: Dp = 16.dp,
    // 颜色与样式
    barColor: Color = MaterialTheme.colorScheme.primary,
    // 辅助线颜色
    gridColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
    axisColor: Color = Color.Black, // 轴线为实心黑色
    averageLineColor: Color = MaterialTheme.colorScheme.tertiary,
    averageLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    yLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    yLabelBgColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
    // Y 轴略高于柱子
    yAxisOvershootTop: Dp = 12.dp,
    // 辅助线粗细
    gridStrokeWidth: Dp = 0.5.dp,
    valueFormatter: (Float) -> String = { value ->
        if (value % 1f == 0f) value.toInt().toString() else String.format(Locale.CHINA, "%.1f", value)
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
    val context = LocalContext.current

    // 计算最大值与刻度
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

    // 从上到下（顶部到底部）固定刻度标签
    val yAxisLabels = remember(maxValue) {
        (0..steps).map { step ->
            val fraction = step / steps.toFloat()
            currentFormatter(maxValue * (1f - fraction))
        }
    }

    val minChartWidth = 240.dp
    val scrollState = rememberScrollState()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val maxWidthDp = this.maxWidth
        val n = entries.size
        val totalSpacing = spacing * (n - 1)
        // 与柱子对应的“对称留白”，确保第一根柱左侧有空间（但整体布局仍铺满整宽，轴贴左 x=0）
        val sidePadding = spacing / 2

        // 铺满整宽：不为 Y 轴单独预留布局空白
        val availableChartWidth = maxWidthDp

        // 宽度（决定是否滚动），包含左右留白
        val desiredChartWidth = (sidePadding * 2 + barWidth * n + totalSpacing).coerceAtLeast(minChartWidth)
        val shouldScroll = desiredChartWidth > availableChartWidth

        // 不滚动时自适应柱宽（扣除左右留白）
        val usedBarWidth: Dp = if (shouldScroll) {
            barWidth
        } else {
            val barsArea = (availableChartWidth - totalSpacing - sidePadding * 2).coerceAtLeast(0.dp)
            (barsArea / n).coerceIn(8.dp, barWidth)
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
            val yLabelPadH: Float,
            val yLabelPadV: Float,
            val yLabelCorner: Float,
            val yLabelInsideGap: Float,
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
                    axisStrokePx = 2.dp.toPx(),
                    gridStrokePx = gridStrokeWidth.toPx(),
                    overshootTopPx = yAxisOvershootTop.toPx(),
                    yLabelTextSizePx = 10.sp.toPx(),
                    yLabelPadH = 4.dp.toPx(),
                    yLabelPadV = 2.dp.toPx(),
                    yLabelCorner = 6.dp.toPx(),
                    yLabelInsideGap = 4.dp.toPx(), // 文字离 Y 轴的水平间距
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

        // 虚线路径样式
        val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(12f, 12f)) }

        Column {
            // 图表区域（上：柱+网格+平均线，可滚动；上面覆盖层画轴与刻度数字，固定不动）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(maxBarHeight)
            ) {
                // 可滚动画布，宽度为 usedChartWidth
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

                    // 水平辅助线
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

                    // 柱形（整体右移 sidePadding，保证第一根柱前有留白；与下方标签完全同构）
                    entries.forEachIndexed { index, entry ->
                        val barHeight = if (maxValue == 0f) 0f else (entry.value / maxValue) * contentHeight
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

                    // 平均线（虚线）与标注
                    if (averageValue > 0f && maxValue > 0f) {
                        val averageLabel = context.getString(
                            R.string.chart_average_inline,
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

                // 覆盖层：固定的轴线（原点对齐）与固定的 Y 轴刻度数字
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // 固定的 Y 轴刻度数字（在轴线右侧、图内）
                    for (i in 0..steps) {
                        val label = yAxisLabels[i]
                        val fractionTopToBottom = i / steps.toFloat()
                        val yCenter = h * fractionTopToBottom

                        val textWidth = yLabelPaint.measureText(label)
                        val bgW = textWidth + 2 * px.yLabelPadH
                        val bgH = yLabelTextHeight + 2 * px.yLabelPadV

                        val bgLeft = 0f + px.yLabelInsideGap
                        val bgTop = (yCenter - bgH / 2f).coerceIn(0f, h - bgH)

                        drawRoundRect(
                            color = yLabelBgColor,
                            topLeft = Offset(bgLeft, bgTop),
                            size = Size(bgW, bgH),
                            cornerRadius = CornerRadius(px.yLabelCorner, px.yLabelCorner)
                        )
                        val baselineY = bgTop + px.yLabelPadV - yLabelFontMetrics.ascent
                        drawIntoCanvas { c ->
                            c.nativeCanvas.drawText(
                                label,
                                bgLeft + px.yLabelPadH,
                                baselineY,
                                yLabelPaint
                            )
                        }
                    }

                    // Y 轴（固定，略高于柱子；x=0 贴左）
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, -px.overshootTopPx), // 顶部上挑
                        end = Offset(0f, h),
                        strokeWidth = px.axisStrokePx,
                        cap = StrokeCap.Butt
                    )

                    // X 轴（固定），起点与 Y 轴重合（原点对齐）
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, h),
                        end = Offset(w, h),
                        strokeWidth = px.axisStrokePx,
                        cap = StrokeCap.Butt
                    )
                }
            }

            // X 轴标签（随内容滚动；与柱子几何完全一致：sidePadding、barWidth、spacing）
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
                    horizontalArrangement = Arrangement.Start, // 不能用 spacedBy，否则会多出两段间距
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左右与柱保持一致的对称留白
                    Spacer(modifier = Modifier.width(sidePadding))
                    entries.forEachIndexed { index, entry ->
                        Box(
                            modifier = Modifier.width(usedBarWidth),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = entry.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
