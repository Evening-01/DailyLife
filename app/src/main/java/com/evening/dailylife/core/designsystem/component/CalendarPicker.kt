package com.evening.dailylife.core.designsystem.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.util.Calendar
import kotlin.math.abs

/**
 * 滚轮日期选择器
 *
 * @param items 要显示的项目列表
 * @param modifier Modifier
 * @param initialIndex 初始选中的项目索引
 * @param onItemSelected 当项目被选中时的回调
 */
@Composable
fun <T> WheelPicker(
    items: List<T>,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    onItemSelected: (index: Int) -> Unit
) {
    if (items.isEmpty()) {
        return // 如果列表为空，直接返回，避免后续代码出错
    }

    val itemHeight: Dp = 50.dp
    val visibleItemsCount = 3
    val containerHeight = itemHeight * visibleItemsCount

    val correctedInitialIndex = initialIndex.coerceIn(0, items.lastIndex)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = correctedInitialIndex)

    val originalSnapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val flingBehavior = remember(originalSnapFlingBehavior) {
        object : FlingBehavior {
            val dampingFactor = 3.0f
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                return originalSnapFlingBehavior.run {
                    this@performFling.performFling(initialVelocity / dampingFactor)
                }
            }
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // 在这里干掉剩余的滚动事件，阻止它向上传递给 BottomSheet
                // 这样即使用户在列表顶部/底部继续滑动，BottomSheet 也不会关闭
                return available
            }
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = calculateCenterIndex(listState)
            if (centerIndex != -1 && centerIndex < items.size) {
                listState.animateScrollToItem(centerIndex)
                onItemSelected(centerIndex)
            }
        }
    }

    Box(
        modifier = modifier.height(containerHeight),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(nestedScrollConnection),
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = (containerHeight - itemHeight) / 2)
        ) {
            itemsIndexed(items = items, key = { index, _ -> index }) { index, item ->
                val (scale, alpha) = calculateScaleAndAlpha(listState, index)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .graphicsLayer {
                            this.scaleX = scale
                            this.scaleY = scale
                            this.alpha = alpha
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
        ) {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.weight(1f))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun calculateScaleAndAlpha(listState: LazyListState, index: Int): Pair<Float, Float> {
    val center = listState.layoutInfo.viewportEndOffset / 2f
    val itemInfo = listState.layoutInfo.visibleItemsInfo.find { it.index == index }

    val distanceFromCenter = if (itemInfo != null) {
        abs((itemInfo.offset + itemInfo.size / 2) - center)
    } else {
        Float.MAX_VALUE
    }

    val scale = 1f - (distanceFromCenter / center).coerceAtMost(1f) * 0.5f
    val alpha = 1f - (distanceFromCenter / center).coerceAtMost(1f) * 0.7f

    return Pair(scale, alpha)
}

private fun calculateCenterIndex(listState: LazyListState): Int {
    val layoutInfo = listState.layoutInfo
    val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
    val closestItem = layoutInfo.visibleItemsInfo.minByOrNull {
        abs((it.offset + it.size / 2) - viewportCenter)
    }
    return closestItem?.index ?: -1
}


enum class CalendarPickerType {
    DATE,
    MONTH
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarPicker(
    type: CalendarPickerType = CalendarPickerType.DATE,
    year: Int,
    month: Int,
    day: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit
) {
    val years = (1900..2100).toList()
    val months = (1..12).toList()

    val daysInMonth = remember(year, month) {
        LocalDate.of(year, month, 1).lengthOfMonth()
    }
    val days = (1..daysInMonth).toList()

    LaunchedEffect(daysInMonth) {
        if (day > daysInMonth) {
            onDayChange(1)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WheelPicker(
            items = years,
            initialIndex = years.indexOf(year).coerceAtLeast(0),
            onItemSelected = { index -> onYearChange(years[index]) },
            modifier = Modifier.weight(1.2f)
        )
        Text("年", modifier = Modifier.padding(horizontal = 8.dp))
        WheelPicker(
            items = months,
            initialIndex = months.indexOf(month).coerceAtLeast(0),
            onItemSelected = { index -> onMonthChange(months[index]) },
            modifier = Modifier.weight(1f)
        )
        Text("月", modifier = Modifier.padding(horizontal = 8.dp))
        if (type == CalendarPickerType.DATE) {
            WheelPicker(
                items = days,
                initialIndex = days.indexOf(day).coerceAtLeast(0),
                onItemSelected = { index -> onDayChange(days[index]) },
                modifier = Modifier.weight(1f)
            )
            Text("日", modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPickerBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    type: CalendarPickerType,
    initialDate: Calendar = Calendar.getInstance(),
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    onMonthSelected: (year: Int, month: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showBottomSheet) {
        var selectedYear by remember { mutableIntStateOf(initialDate.get(Calendar.YEAR)) }
        var selectedMonth by remember { mutableIntStateOf(initialDate.get(Calendar.MONTH) + 1) }
        var selectedDay by remember { mutableIntStateOf(initialDate.get(Calendar.DAY_OF_MONTH)) }

        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 【优化】标题单独居中显示
                Text(
                    text = if (type == CalendarPickerType.DATE) "选择日期" else "选择月份",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                CalendarPicker(
                    type = type,
                    year = selectedYear,
                    month = selectedMonth,
                    day = selectedDay,
                    onYearChange = { selectedYear = it },
                    onMonthChange = { selectedMonth = it },
                    onDayChange = { selectedDay = it }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp)) // 按钮间距
                    TextButton(onClick = {
                        if (type == CalendarPickerType.DATE) {
                            onDateSelected(selectedYear, selectedMonth, selectedDay)
                        } else {
                            onMonthSelected(selectedYear, selectedMonth)
                        }
                        onDismiss()
                    }) {
                        Text("确定")
                    }
                }
            }
        }
    }
}