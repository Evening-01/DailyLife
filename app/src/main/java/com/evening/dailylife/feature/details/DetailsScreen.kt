package com.evening.dailylife.feature.details

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.designsystem.component.CalendarPickerBottomSheet
import com.evening.dailylife.core.designsystem.component.CalendarPickerType
import com.evening.dailylife.feature.details.component.DailyHeader
import com.evening.dailylife.feature.details.component.DetailsEmptyState
import com.evening.dailylife.feature.details.component.DetailsSummaryHeader
import com.evening.dailylife.feature.details.component.TransactionListItem
import com.evening.dailylife.feature.details.model.DailyTransactions
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
    viewModel: DetailsViewModel = hiltViewModel(),
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
                year = yearFormat.format(selectedDate.time),
                month = monthFormat.format(selectedDate.time),
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
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val deleteWidthDp = 80.dp
    val deleteWidthPx = with(density) { deleteWidthDp.toPx() }
    val snapOpenThreshold = deleteWidthPx * (2f / 3f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        transactions.forEach { dailyData ->
            item {
                DailyHeader(
                    date = dailyData.date,
                    income = dailyData.dailyIncome,
                    expense = dailyData.dailyExpense,
                    mood = dailyData.dailyMood,
                )

                AnimatedVisibility(
                    visible = dailyData.transactions.isNotEmpty(),
                    enter = expandVertically(expandFrom = Alignment.Top),
                    exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(tween(200)),
                ) {
                    Column {
                        dailyData.transactions.forEachIndexed { index, transaction ->
                            val offsetX = remember { Animatable(0f) }
                            val isDeleting = remember { mutableStateOf(false) }

                            LaunchedEffect(transaction.id) {
                                offsetX.snapTo(0f)
                                isDeleting.value = false
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(transaction.id) {
                                        detectHorizontalDragGestures(
                                            onDragEnd = {
                                                coroutineScope.launch {
                                                    if (abs(offsetX.value) >= snapOpenThreshold) {
                                                        offsetX.animateTo(
                                                            targetValue = -deleteWidthPx,
                                                            animationSpec = tween(200)
                                                        )
                                                    } else {
                                                        offsetX.animateTo(
                                                            targetValue = 0f,
                                                            animationSpec = tween(200)
                                                        )
                                                    }
                                                }
                                            }
                                        ) { _, dragAmount ->
                                            coroutineScope.launch {
                                                val newValue = (offsetX.value + dragAmount)
                                                    .coerceIn(-deleteWidthPx, 0f)
                                                offsetX.snapTo(newValue)
                                            }
                                        }
                                    }
                            ) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            isDeleting.value = true
                                            offsetX.animateTo(
                                                targetValue = -density.run { 200.dp.toPx() },
                                                animationSpec = tween(250),
                                            )
                                            delay(150)
                                            onDeleteTransaction(transaction)
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 16.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.error,
                                            shape = MaterialTheme.shapes.medium,
                                        ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.common_delete),
                                        tint = Color.White,
                                    )
                                }

                                TransactionListItem(
                                    transaction = transaction,
                                    onClick = { onTransactionClick(transaction.id) },
                                    modifier = Modifier
                                        .offset { IntOffset(offsetX.value.roundToInt(), 0) },
                                )
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(start = 72.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color.Transparent),
                )
            }
        }
    }
}