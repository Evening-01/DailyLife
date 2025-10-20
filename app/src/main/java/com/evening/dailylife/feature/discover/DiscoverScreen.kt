package com.evening.dailylife.feature.discover

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.evening.dailylife.R
import com.evening.dailylife.app.ui.theme.LocalExtendedColorScheme
import com.evening.dailylife.feature.discover.component.DiscoverAiSection
import com.evening.dailylife.feature.discover.component.DiscoverCommonToolsSection
import com.evening.dailylife.feature.discover.component.DiscoverHeatMapSection
import com.evening.dailylife.feature.discover.component.TypeProfileSection
import com.evening.dailylife.feature.discover.model.DiscoverHeatMapUiState
import com.evening.dailylife.feature.discover.model.DiscoverTypeProfileUiState
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.kizitonwose.calendar.compose.heatmapcalendar.rememberHeatMapCalendarState
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.yearMonth
import java.text.DecimalFormat

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    val typeProfileState by viewModel.typeProfileState.collectAsState()
    val heatMapUiState by viewModel.heatMapUiState.collectAsState()
    val headerContainerColor = LocalExtendedColorScheme.current.headerContainer
    val headerContentColor = LocalExtendedColorScheme.current.onHeaderContainer
    val numberFormatter = remember { DecimalFormat("#,##0.00") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.discover),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = headerContainerColor,
                    titleContentColor = headerContentColor,
                ),
            )
        },
    ) { innerPadding ->
        DiscoverContent(
            innerPadding = innerPadding,
            heatMapState = heatMapUiState,
            typeProfileState = typeProfileState,
            numberFormatter = numberFormatter,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DiscoverContent(
    innerPadding: PaddingValues,
    heatMapState: DiscoverHeatMapUiState,
    typeProfileState: DiscoverTypeProfileUiState,
    numberFormatter: DecimalFormat,
) {
    val scrollState = rememberScrollState()
    val startMonth = heatMapState.startDate.yearMonth
    val endMonth = heatMapState.endDate.yearMonth
    val calendarState = rememberHeatMapCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = endMonth,
        firstDayOfWeek = firstDayOfWeekFromLocale(),
    )
    val sectionSpacing = 12.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(scrollState),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        RoundedColumn(modifier = Modifier.fillMaxWidth()) {
            ItemTitle(text = stringResource(id = R.string.discover_heatmap_title))
            DiscoverHeatMapSection(
                uiState = heatMapState,
                calendarState = calendarState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )
        }
        Spacer(modifier = Modifier.height(sectionSpacing))
        RoundedColumn(modifier = Modifier.fillMaxWidth()) {
            ItemTitle(text = stringResource(id = R.string.chart_type_profile_title))
            DiscoverTypeProfileContent(typeProfileState, numberFormatter)
        }
        Spacer(modifier = Modifier.height(sectionSpacing))
        DiscoverAiSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
        )
        Spacer(modifier = Modifier.height(sectionSpacing))
        DiscoverCommonToolsSection()
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DiscoverTypeProfileContent(
    state: DiscoverTypeProfileUiState,
    numberFormatter: DecimalFormat,
) {
    val year = state.year
    val month = state.month
    if (year != null && month != null) {
        Text(
            text = stringResource(
                id = R.string.discover_type_profile_month_label,
                year,
                month,
            ),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        )
        if (!state.isLoading) {
            Text(
                text = stringResource(id = R.string.discover_type_profile_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
            )
        }
    }
    if (state.isLoading) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        )
    } else {
        TypeProfileSection(
            profile = state.typeProfile,
            numberFormatter = numberFormatter,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
        )
    }
}
