package com.evening.dailylife.feature.details

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.evening.dailylife.R
import com.evening.dailylife.app.ui.theme.LocalExtendedColorScheme
import com.evening.dailylife.core.designsystem.component.CalendarPickerBottomSheet
import com.evening.dailylife.core.designsystem.component.CalendarPickerType
import com.evening.dailylife.feature.details.components.DailyHeader
import com.evening.dailylife.feature.details.components.DetailsEmptyState
import com.evening.dailylife.feature.details.components.DetailsSummaryHeader
import com.evening.dailylife.feature.details.components.TransactionListItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    onTransactionClick: (Int) -> Unit,
    onAddTransactionClick: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    val yearPattern = stringResource(R.string.details_year_pattern)
    val monthPattern = stringResource(R.string.details_month_pattern)
    val yearFormat = remember(yearPattern) { SimpleDateFormat(yearPattern, Locale.getDefault()) }
    val monthFormat = remember(monthPattern) { SimpleDateFormat(monthPattern, Locale.getDefault()) }

    val headerContainerColor = LocalExtendedColorScheme.current.headerContainer
    val headerContentColor = LocalExtendedColorScheme.current.onHeaderContainer

    LaunchedEffect(Unit) {
        viewModel.filterByMonth(selectedDate)
    }

    if (showDatePickerDialog) {
        CalendarPickerBottomSheet(
            showBottomSheet = true,
            onDismiss = { showDatePickerDialog = false },
            type = CalendarPickerType.MONTH,
            initialDate = selectedDate,
            onDateSelected = { _, _, _ -> },
            onMonthSelected = { year, month ->
                val newCalendar = Calendar.getInstance().apply {
                    clear()
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month - 1)
                }
                selectedDate = newCalendar
                viewModel.filterByMonth(newCalendar)
                showDatePickerDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = headerContainerColor,
                    titleContentColor = headerContentColor
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddTransactionClick() },
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.details_add_transaction_content_description)
                    )
                },
                text = { Text(stringResource(R.string.details_add_transaction_label)) }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            DetailsSummaryHeader(
                year = yearFormat.format(selectedDate.time),
                month = monthFormat.format(selectedDate.time),
                income = "%.2f".format(uiState.totalIncome),
                expense = "%.2f".format(abs(uiState.totalExpense)),
                onDateClick = { showDatePickerDialog = true },
                containerColor = headerContainerColor,
                contentColor = headerContentColor
            )

            if (uiState.isLoading) {
                // 你可以在这里添加一个加载指示器
            } else if (uiState.transactions.isEmpty()) {
                DetailsEmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    uiState.transactions.forEach { dailyData ->
                        item {
                            DailyHeader(
                                date = dailyData.date,
                                income = dailyData.dailyIncome,
                                expense = dailyData.dailyExpense,
                                mood = dailyData.dailyMood
                            )
                        }
                        items(dailyData.transactions, key = { it.id }) { transaction ->
                            val animationDuration = 300
                            var visible by remember { mutableStateOf(true) }

                            // 当 visible 状态变为 false 时，此效应会启动
                            LaunchedEffect(visible) {
                                if (!visible) {
                                    // 等待动画播放完毕
                                    delay(animationDuration.toLong())
                                    // 动画结束后，再真正删除数据
                                    viewModel.deleteTransaction(transaction)
                                }
                            }

                            AnimatedVisibility(
                                visible = visible,
                                exit = shrinkVertically(animationSpec = tween(durationMillis = animationDuration)) +
                                        fadeOut(animationSpec = tween(durationMillis = animationDuration)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val coroutineScope = rememberCoroutineScope()
                                val deleteButtonWidth = 80.dp
                                val deleteButtonWidthPx =
                                    with(LocalDensity.current) { deleteButtonWidth.toPx() }
                                val offsetX = remember { Animatable(0f) }

                                Box {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(MaterialTheme.colorScheme.error)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .fillMaxHeight()
                                                .width(deleteButtonWidth),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch { offsetX.snapTo(0f) }
                                                    // 触发动画，而不是直接删除
                                                    visible = false
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.common_delete),
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                                            .pointerInput(Unit) {
                                                detectHorizontalDragGestures(
                                                    onDragEnd = {
                                                        coroutineScope.launch {
                                                            val threshold = -deleteButtonWidthPx * 0.6f
                                                            if (offsetX.value < threshold) {
                                                                offsetX.animateTo(
                                                                    targetValue = -deleteButtonWidthPx,
                                                                    animationSpec = tween(durationMillis = 200)
                                                                )
                                                            } else {
                                                                offsetX.animateTo(
                                                                    targetValue = 0f,
                                                                    animationSpec = tween(durationMillis = 200)
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onHorizontalDrag = { change, dragAmount ->
                                                        change.consume()
                                                        coroutineScope.launch {
                                                            val newOffset =
                                                                offsetX.value + dragAmount
                                                            offsetX.snapTo(
                                                                newOffset.coerceIn(
                                                                    -deleteButtonWidthPx,
                                                                    0f
                                                                )
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                    ) {
                                    TransactionListItem(
                                            transaction = transaction,
                                            onClick = {
                                                if (offsetX.value == 0f) {
                                                    onTransactionClick(transaction.id)
                                                }
                                            }
                                        )
                                    }

                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(start = 72.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

