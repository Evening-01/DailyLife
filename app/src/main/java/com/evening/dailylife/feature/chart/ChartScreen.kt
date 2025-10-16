package com.evening.dailylife.feature.chart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.evening.dailylife.R
import com.evening.dailylife.core.designsystem.component.BarChart
import com.evening.dailylife.core.designsystem.theme.LocalExtendedColorScheme
import java.text.DecimalFormat

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
    val numberFormatter = remember { DecimalFormat("#,##0.00") }
    val formattedTotal = remember(uiState.totalAmount) {
        numberFormatter.format(uiState.totalAmount)
    }
    val formattedAverage = remember(uiState.averageAmount) {
        numberFormatter.format(uiState.averageAmount)
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val rangeTabs = uiState.rangeTabs

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = headerContainerColor
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val periods = ChartPeriod.entries.toTypedArray()
                    periods.forEachIndexed { index, period ->
                        SegmentedButton(
                            selected = selectedPeriod == period,
                            onClick = { viewModel.onPeriodSelected(period) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = periods.size
                            ),
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

            if (rangeTabs.isNotEmpty()) {
                val selectedTabIndex = rangeTabs.indexOfFirst { option ->
                    option.id == uiState.selectedRangeOption?.id
                }.takeIf { it >= 0 } ?: 0

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        if (selectedTabIndex in tabPositions.indices) {
                            TabRowDefaults.Indicator(
                                modifier = Modifier
                                    .tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = MaterialTheme.colorScheme.primary,
                                height = 2.dp
                            )
                        }
                    }
                ) {
                    rangeTabs.forEachIndexed { index, option ->
                        val interactionSource = remember { MutableInteractionSource() }
                        val isSelected = index == selectedTabIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .heightIn(min = 30.dp)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { viewModel.onRangeOptionSelected(option.id) }
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Surface(
                    tonalElevation = 2.dp,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val totalLine = stringResource(id = R.string.chart_total_label, totalLabel) +
                                "：" + formattedTotal
                        Text(
                            text = totalLine,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        if (uiState.isLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        } else {
                            BarChart(
                                entries = chartEntries,
                                modifier = Modifier.fillMaxWidth(),
                                averageValue = uiState.averageAmount.toFloat(),
                                valueFormatter = { value ->
                                    numberFormatter.format(value.toDouble())
                                },
                                animationKey = barAnimationTrigger
                            )
                        }
                    }
                }
            }
        }
    }
}