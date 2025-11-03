package com.evening.dailylife.feature.details

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.evening.dailylife.R
import com.evening.dailylife.app.ui.theme.LocalExtendedColorScheme
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.designsystem.component.CalendarPickerBottomSheet
import com.evening.dailylife.core.designsystem.component.CalendarPickerType
import com.evening.dailylife.feature.details.component.DailyHeader
import com.evening.dailylife.feature.details.component.DetailsEmptyState
import com.evening.dailylife.feature.details.component.DetailsSummaryHeader
import com.evening.dailylife.feature.details.component.TransactionListItem
import com.evening.dailylife.feature.details.model.DailyTransactions
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
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val fallbackCalendar = remember { Calendar.getInstance() }
    val selectedYear = uiState.selectedYear ?: fallbackCalendar.get(Calendar.YEAR)
    val selectedMonth = uiState.selectedMonth ?: (fallbackCalendar.get(Calendar.MONTH) + 1)
    val selectedCalendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, selectedYear)
        set(Calendar.MONTH, selectedMonth - 1)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val yearPattern = stringResource(R.string.details_year_pattern)
    val monthPattern = stringResource(R.string.details_month_pattern)
    val yearFormat = remember(yearPattern) { SimpleDateFormat(yearPattern, Locale.getDefault()) }
    val monthFormat = remember(monthPattern) { SimpleDateFormat(monthPattern, Locale.getDefault()) }

    val headerContainerColor = LocalExtendedColorScheme.current.headerContainer
    val headerContentColor = LocalExtendedColorScheme.current.onHeaderContainer

    if (showDatePickerDialog) {
        CalendarPickerBottomSheet(
            showBottomSheet = true,
            onDismiss = { showDatePickerDialog = false },
            type = CalendarPickerType.MONTH,
            initialDate = (selectedCalendar.clone() as Calendar),
            onDateSelected = { _, _, _ -> },
            onMonthSelected = { year, month ->
                val newCalendar = Calendar.getInstance().apply {
                    clear()
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month - 1)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                viewModel.filterByMonth(newCalendar)
                showDatePickerDialog = false
            },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = headerContainerColor,
                    titleContentColor = headerContentColor,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddTransactionClick() },
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.details_add_transaction_content_description),
                    )
                },
                text = { Text(stringResource(R.string.details_add_transaction_label)) },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            DetailsSummaryHeader(
                year = yearFormat.format(selectedCalendar.time),
                month = monthFormat.format(selectedCalendar.time),
                income = "%.2f".format(uiState.totalIncome),
                expense = "%.2f".format(abs(uiState.totalExpense)),
                onDateClick = { showDatePickerDialog = true },
                containerColor = headerContainerColor,
                contentColor = headerContentColor,
            )

            when {
                uiState.isLoading -> Unit
                uiState.transactions.isEmpty() -> DetailsEmptyState()
                else -> DetailsTransactionList(
                    transactions = uiState.transactions,
                    onTransactionClick = onTransactionClick,
                    onDeleteTransaction = viewModel::deleteTransaction,
                )
            }
        }
    }
}

@Composable
private fun DetailsTransactionList(
    transactions: List<DailyTransactions>,
    onTransactionClick: (Int) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
) {
    val density = LocalDensity.current
    val deleteButtonSize = 48.dp
    val deleteActionWidth = deleteButtonSize + 24.dp
    val deleteActionWidthPx = with(density) { deleteActionWidth.toPx() }
    val maxSwipeOffset = -deleteActionWidthPx
    val swipeThreshold = deleteActionWidthPx * 0.4f
    var expandedTransactionId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        transactions.forEach { dailyData ->
            item(key = "header-${dailyData.date}") {
                DailyHeader(
                    date = dailyData.date,
                    income = dailyData.dailyIncome,
                    expense = dailyData.dailyExpense,
                    mood = dailyData.dailyMood,
                )
            }

            if (dailyData.transactions.isNotEmpty()) {
                items(
                    items = dailyData.transactions,
                    key = { it.id },
                ) { transaction ->
                    SwipeableTransactionRow(
                        transaction = transaction,
                        isExpanded = expandedTransactionId == transaction.id,
                        onExpandedChange = { shouldExpand ->
                            expandedTransactionId = if (shouldExpand) transaction.id else null
                        },
                        onCollapseOthers = {
                            if (expandedTransactionId != null && expandedTransactionId != transaction.id) {
                                expandedTransactionId = null
                            }
                        },
                        maxSwipeOffset = maxSwipeOffset,
                        swipeThreshold = swipeThreshold,
                        deleteActionWidth = deleteActionWidth,
                        deleteButtonSize = deleteButtonSize,
                        onTransactionClick = onTransactionClick,
                        onDeleteTransaction = onDeleteTransaction,
                    )
                }
            }

            item(key = "spacer-${dailyData.date}") {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
            }
        }
    }
}

@Composable
private fun SwipeableTransactionRow(
    transaction: TransactionEntity,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCollapseOthers: () -> Unit,
    maxSwipeOffset: Float,
    swipeThreshold: Float,
    deleteActionWidth: Dp,
    deleteButtonSize: Dp,
    onTransactionClick: (Int) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(transaction.id) {
        offsetX.snapTo(0f)
    }

    LaunchedEffect(isExpanded) {
        val target = if (isExpanded) maxSwipeOffset else 0f
        offsetX.animateTo(targetValue = target, animationSpec = tween(200))
    }

    fun settleSwipe(shouldExpand: Boolean) {
        onExpandedChange(shouldExpand)
        coroutineScope.launch {
            val target = if (shouldExpand) maxSwipeOffset else 0f
            offsetX.animateTo(targetValue = target, animationSpec = tween(200))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(isExpanded, transaction.id) {
                detectHorizontalDragGestures(
                    onDragStart = { onCollapseOthers() },
                    onHorizontalDrag = { _, dragAmount ->
                        coroutineScope.launch {
                            val newValue = (offsetX.value + dragAmount)
                                .coerceIn(maxSwipeOffset, 0f)
                            offsetX.snapTo(newValue)
                        }
                    },
                    onDragEnd = {
                        val shouldExpand = abs(offsetX.value) > swipeThreshold
                        settleSwipe(shouldExpand)
                    },
                    onDragCancel = {
                        val shouldExpand = abs(offsetX.value) > swipeThreshold
                        settleSwipe(shouldExpand)
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(deleteActionWidth)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(deleteButtonSize)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .clickable {
                        settleSwipe(false)
                        onDeleteTransaction(transaction)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.common_delete),
                    tint = Color.White,
                )
            }
        }

        TransactionListItem(
            transaction = transaction,
            onClick = {
                if (isExpanded) {
                    settleSwipe(false)
                } else {
                    onTransactionClick(transaction.id)
                }
            },
            modifier = Modifier.offset {
                IntOffset(offsetX.value.roundToInt(), 0)
            },
        )
    }
}
