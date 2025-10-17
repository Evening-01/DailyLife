package com.evening.dailylife.feature.chart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.evening.dailylife.R
import com.evening.dailylife.app.ui.theme.LocalExtendedColorScheme
import com.evening.dailylife.core.designsystem.component.CategoryRankingSection
import com.evening.dailylife.core.designsystem.component.MoodLineChart
import com.evening.dailylife.feature.chart.components.ChartOverviewSection
import com.evening.dailylife.feature.chart.components.ChartPeriodSelector
import com.evening.dailylife.feature.chart.components.ChartRangeTabRow
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    viewModel: ChartViewModel = hiltViewModel()
) {
    val headerContainerColor = LocalExtendedColorScheme.current.headerContainer
    val headerContentColor = LocalExtendedColorScheme.current.onHeaderContainer

    var typeMenuExpanded by remember { mutableStateOf(false) }
    var barAnimationTrigger by remember { mutableStateOf(0) }

    val uiState by viewModel.uiState.collectAsState()
    val chartEntries = uiState.entries
    val selectedType = uiState.selectedType
    val selectedPeriod = uiState.selectedPeriod
    val totalLabel = stringResource(id = selectedType.labelRes)
    val formatAmount = remember {
        { value: Double -> String.format(Locale.getDefault(), "%,.2f", value) }
    }
    val formattedTotal = remember(uiState.totalAmount) {
        formatAmount(uiState.totalAmount)
    }
    val formattedAverage = remember(uiState.averageAmount) {
        formatAmount(uiState.averageAmount)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                barAnimationTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(selectedType, selectedPeriod) {
        barAnimationTrigger++
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box {
                        Row(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { typeMenuExpanded = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = totalLabel,
                                color = headerContentColor
                            )
                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDown,
                                contentDescription = null,
                                tint = headerContentColor,
                            )
                        }

                        DropdownMenu(
                            expanded = typeMenuExpanded,
                            onDismissRequest = { typeMenuExpanded = false }
                        ) {
                            val chartTypes = ChartType.entries.toTypedArray()
                            chartTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(text = stringResource(id = type.labelRes))
                                    },
                                    onClick = {
                                        viewModel.onTypeSelected(type)
                                        typeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = headerContainerColor,
                    titleContentColor = headerContentColor
                )
            )
        }
    ) { innerPadding ->
        val rangeTabs = uiState.rangeTabs

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = headerContainerColor
            ) {
                ChartPeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = viewModel::onPeriodSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (rangeTabs.isNotEmpty()) {
                ChartRangeTabRow(
                    rangeTabs = rangeTabs,
                    selectedOptionId = uiState.selectedRangeOption?.id,
                    onRangeSelected = viewModel::onRangeOptionSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),

                ) {
                item {
                    ChartOverviewSection(
                        title = stringResource(id = R.string.chart_overview_title),
                        totalDescription = stringResource(id = R.string.chart_total_label, totalLabel) + "：" + formattedTotal,
                        averageDescription = stringResource(id = R.string.chart_average_label, formattedAverage),
                        isLoading = uiState.isLoading,
                        entries = chartEntries,
                        averageValue = uiState.averageAmount,
                        valueFormatter = formatAmount,
                        animationKey = barAnimationTrigger,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    CategoryRankingSection(
                        ranks = uiState.categoryRanks,
                        type = selectedType,
                        amountFormatter = formatAmount,
                        animationKey = barAnimationTrigger
                    )
                }

                item {
                    RoundedColumn(modifier = Modifier.fillMaxWidth()) {
                        ItemTitle(text = stringResource(id = R.string.chart_mood_trend_title))
                        MoodLineChart(
                            entries = uiState.moodEntries,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            animationKey = barAnimationTrigger
                        )
                    }
                }
            }
        }
    }
}
