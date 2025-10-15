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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.evening.dailylife.R
import com.evening.dailylife.core.designsystem.theme.LocalExtendedColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen() {
    val headerContainerColor = LocalExtendedColorScheme.current.headerContainer
    val headerContentColor = LocalExtendedColorScheme.current.onHeaderContainer

    var selectedType by rememberSaveable { mutableStateOf(ChartType.Expense) }
    var selectedPeriod by rememberSaveable { mutableStateOf(ChartPeriod.Week) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    val chartEntries = remember(selectedType, selectedPeriod) {
        sampleChartData[selectedType]?.get(selectedPeriod).orEmpty()
    }
    val totalAmount = remember(chartEntries) {
        chartEntries.sumOf { it.value.toDouble() }.toFloat()
    }
    val totalLabel = stringResource(id = selectedType.labelRes)

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
                            val chartTypes = ChartType.values()
                            chartTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(text = stringResource(id = type.labelRes))
                                    },
                                    onClick = {
                                        selectedType = type
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = headerContainerColor
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    val periods = ChartPeriod.values()
                    periods.forEachIndexed { index, period ->
                        SegmentedButton(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = periods.size
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                inactiveContainerColor = Color.Transparent,
                                inactiveContentColor = headerContentColor.copy(alpha = 0.8f),
                                activeBorderColor = headerContentColor
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(
                    tonalElevation = 2.dp,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.chart_total_label, totalLabel),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.2f", totalAmount),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedType == ChartType.Expense) {
                                MaterialTheme.colorScheme.error
                            } else {
                                LocalExtendedColorScheme.current.success
                            }
                        )

                        BarChart(
                            entries = chartEntries,
                            modifier = Modifier.fillMaxWidth(),
                            barColor = if (selectedType == ChartType.Expense) {
                                MaterialTheme.colorScheme.error
                            } else {
                                LocalExtendedColorScheme.current.success
                            }
                        )
                    }
                }
            }
        }
    }
}


private val sampleChartData: Map<ChartType, Map<ChartPeriod, List<ChartEntry>>> = mapOf(
    ChartType.Expense to mapOf(
        ChartPeriod.Week to listOf(
            ChartEntry("周一", 120.0f),
            ChartEntry("周二", 98.0f),
            ChartEntry("周三", 140.0f),
            ChartEntry("周四", 76.0f),
            ChartEntry("周五", 165.0f),
            ChartEntry("周六", 210.0f),
            ChartEntry("周日", 132.0f)
        ),
        ChartPeriod.Month to listOf(
            ChartEntry("第1周", 640.0f),
            ChartEntry("第2周", 720.0f),
            ChartEntry("第3周", 580.0f),
            ChartEntry("第4周", 760.0f)
        ),
        ChartPeriod.Year to listOf(
            ChartEntry("1月", 3200.0f),
            ChartEntry("2月", 2980.0f),
            ChartEntry("3月", 3410.0f),
            ChartEntry("4月", 2890.0f),
            ChartEntry("5月", 3550.0f),
            ChartEntry("6月", 3700.0f),
            ChartEntry("7月", 3320.0f),
            ChartEntry("8月", 3610.0f),
            ChartEntry("9月", 3440.0f),
            ChartEntry("10月", 3180.0f),
            ChartEntry("11月", 3520.0f),
            ChartEntry("12月", 3890.0f)
        )
    ),
    ChartType.Income to mapOf(
        ChartPeriod.Week to listOf(
            ChartEntry("周一", 180.0f),
            ChartEntry("周二", 200.0f),
            ChartEntry("周三", 220.0f),
            ChartEntry("周四", 210.0f),
            ChartEntry("周五", 250.0f),
            ChartEntry("周六", 260.0f),
            ChartEntry("周日", 190.0f)
        ),
        ChartPeriod.Month to listOf(
            ChartEntry("第1周", 880.0f),
            ChartEntry("第2周", 920.0f),
            ChartEntry("第3周", 960.0f),
            ChartEntry("第4周", 1020.0f)
        ),
        ChartPeriod.Year to listOf(
            ChartEntry("1月", 4200.0f),
            ChartEntry("2月", 4010.0f),
            ChartEntry("3月", 4380.0f),
            ChartEntry("4月", 4120.0f),
            ChartEntry("5月", 4520.0f),
            ChartEntry("6月", 4650.0f),
            ChartEntry("7月", 4300.0f),
            ChartEntry("8月", 4780.0f),
            ChartEntry("9月", 4620.0f),
            ChartEntry("10月", 4400.0f),
            ChartEntry("11月", 4680.0f),
            ChartEntry("12月", 4950.0f)
        )
    )
)