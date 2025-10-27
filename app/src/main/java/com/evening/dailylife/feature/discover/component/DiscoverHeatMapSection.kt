package com.evening.dailylife.feature.discover.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evening.dailylife.R
import com.evening.dailylife.feature.discover.model.DiscoverHeatMapEntry
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.compose.HeatMapCalendar
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.yearMonth
import com.moriafly.salt.ui.SaltTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DiscoverHeatMapSection(
    isLoading: Boolean,
    contributions: Map<LocalDate, DiscoverHeatMapEntry>,
    calendarState: HeatMapCalendarState,
    dateRange: ClosedRange<LocalDate>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                HeatMapCalendar(
                    modifier = Modifier.fillMaxWidth(),
                    state = calendarState,
                    contentPadding = PaddingValues(end = 6.dp),
                    dayContent = { day, _ ->
                        val entry = contributions[day.date]
                        val intensity = entry?.intensity ?: 0
                        ContributionDay(
                            day = day,
                            dateRange = dateRange,
                            level = ContributionLevel.fromIntensity(intensity),
                        )
                    },
                    weekHeader = { WeekHeader(it) },
                    monthHeader = { MonthHeader(it, dateRange.endInclusive, calendarState) },
                )
            }
        }

        CalendarLegend(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            lessLabel = stringResource(R.string.discover_heatmap_legend_less),
            moreLabel = stringResource(R.string.discover_heatmap_legend_more),
        )
    }
}

@Composable
private fun CalendarLegend(
    modifier: Modifier,
    lessLabel: String,
    moreLabel: String,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = lessLabel,
            style = SaltTheme.textStyles.sub,
            color = SaltTheme.colors.subText,
            fontSize = 10.sp,
        )
        ContributionLevel.entries.forEach { level ->
            LegendBox(level.color)
        }
        Text(
            text = moreLabel,
            style = SaltTheme.textStyles.sub,
            color = SaltTheme.colors.subText,
            fontSize = 10.sp,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ContributionDay(
    day: CalendarDay,
    dateRange: ClosedRange<LocalDate>,
    level: ContributionLevel,
) {
    val color = if (day.date in dateRange) level.color else Color.Transparent
    LegendBox(color = color)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeekHeader(dayOfWeek: DayOfWeek) {
    val label = remember(dayOfWeek) {
        dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
    Box(
        modifier = Modifier
            .height(DaySize)
            .padding(horizontal = 4.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.align(Alignment.Center),
            color = SaltTheme.colors.subText,
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthHeader(
    calendarMonth: CalendarMonth,
    endDate: LocalDate,
    state: HeatMapCalendarState,
) {
    val density = LocalDensity.current
    val firstFullyVisibleMonth = remember { derivedStateOf { getMonthWithYear(state.layoutInfo, DaySize, density) } }
    if (calendarMonth.weekDays.first().first().date <= endDate) {
        val month = calendarMonth.yearMonth
        val title = if (month == firstFullyVisibleMonth.value) {
            formatMonthYear(month)
        } else {
            formatMonth(month)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 1.dp, start = 2.dp),
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = SaltTheme.colors.subText,
            )
        }
    }
}

private fun getMonthWithYear(
    layoutInfo: CalendarLayoutInfo,
    daySize: Dp,
    density: Density,
): YearMonth? {
    val visibleItemsInfo = layoutInfo.visibleMonthsInfo
    return when {
        visibleItemsInfo.isEmpty() -> null
        visibleItemsInfo.count() == 1 -> visibleItemsInfo.first().month.yearMonth
        else -> {
            val firstItem = visibleItemsInfo.first()
            val daySizePx = with(density) { daySize.toPx() }
            if (
                firstItem.size < daySizePx * 3 ||
                firstItem.offset < layoutInfo.viewportStartOffset &&
                (layoutInfo.viewportStartOffset - firstItem.offset > daySizePx)
            ) {
                visibleItemsInfo[1].month.yearMonth
            } else {
                firstItem.month.yearMonth
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun LegendBox(
    color: Color,
) {
    Box(
        modifier = Modifier
            .size(DaySize)
            .padding(2.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color = color),
    )
}

private enum class ContributionLevel(val color: Color) {
    Zero(EmptyContributionColor),
    One(Color(0xFF9BE9A8)),
    Two(Color(0xFF40C463)),
    Three(Color(0xFF30A14E)),
    Four(Color(0xFF216E3A));

    companion object {
        fun fromIntensity(intensity: Int): ContributionLevel = when (intensity) {
            4 -> Four
            3 -> Three
            2 -> Two
            1 -> One
            else -> Zero
        }
    }
}

private val EmptyContributionColor = Color(0xFFC5CCD7)
private val DaySize = 15.dp

@RequiresApi(Build.VERSION_CODES.O)
private fun formatMonthYear(month: YearMonth): String =
    month.format(DateTimeFormatter.ofPattern("yyyy年M月", Locale.getDefault()))

@RequiresApi(Build.VERSION_CODES.O)
private fun formatMonth(month: YearMonth): String =
    month.format(DateTimeFormatter.ofPattern("M月", Locale.getDefault()))
