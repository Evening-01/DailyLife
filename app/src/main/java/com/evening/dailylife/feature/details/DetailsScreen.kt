package com.evening.dailylife.feature.details

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.evening.dailylife.R
import com.evening.dailylife.core.data.local.entity.TransactionEntity
import com.evening.dailylife.core.designsystem.component.CalendarPickerBottomSheet
import com.evening.dailylife.core.designsystem.component.CalendarPickerType
import com.evening.dailylife.core.designsystem.theme.LocalExtendedColorScheme
import com.evening.dailylife.core.designsystem.theme.SuccessGreen
import com.evening.dailylife.core.model.MoodRepository
import com.evening.dailylife.core.model.TransactionCategoryRepository
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

    val yearFormat = SimpleDateFormat("yyyy年", Locale.getDefault())
    val monthFormat = SimpleDateFormat("M月", Locale.getDefault())

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
                icon = { Icon(Icons.Default.Add, contentDescription = "添加账单") },
                text = { Text("记一笔") }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SummaryHeader(
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
                EmptyState()
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
                                                    contentDescription = "删除",
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
                                        TransactionItem(
                                            transaction = transaction,
                                            onClick = {
                                                if (offsetX.value == 0f) {
                                                    onTransactionClick(transaction.id)
                                                }
                                            }
                                        )
                                    }

                                    Divider(
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


@Composable
fun DailyHeader(date: String, income: Double, expense: Double, mood: String) {
    fun formatDate(date: String): String {
        if (date.startsWith("今天")) {
            return "今天"
        }
        val parts = date.split(" ")
        val dateParts = parts[0].split("/")
        return "${dateParts[0]}月${dateParts[1]}日 ${parts[1]}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatDate(date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))

        if (mood.isNotBlank()) {
            Icon(
                imageVector = MoodRepository.getIcon(mood),
                contentDescription = "Daily Mood",
                modifier = Modifier.size(20.dp),
                tint = MoodRepository.getColor(mood)
            )
        }
        Row {
            if (income > 0) {
                Text(
                    text = "收入: ${"%.2f".format(income)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (expense < 0) {
                Text(
                    text = "支出: ${"%.2f".format(abs(expense))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = TransactionCategoryRepository.getIcon(transaction.category),
                contentDescription = transaction.category,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (transaction.description.isNotBlank()) {
                Column {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = "%.2f".format(transaction.amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (transaction.amount > 0) SuccessGreen else MaterialTheme.colorScheme.error
        )
    }
}
@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = "空数据",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无账单数据\n点击右下角的按钮记一笔吧！",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun SummaryHeader(
    year: String,
    month: String,
    income: String,
    expense: String,
    onDateClick: () -> Unit,
    containerColor: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .padding(top = 4.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DatePickerModule(
                year = year,
                month = month,
                onClick = onDateClick,
                contentColor = contentColor,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(contentColor)
            IncomeExpenseGroup(
                income = income,
                expense = expense,
                contentColor = contentColor,
                modifier = Modifier.weight(3f)
            )
        }
    }
}

@Composable
private fun DatePickerModule(
    year: String,
    month: String,
    onClick: () -> Unit,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 16.dp)
    ) {
        Text(
            text = year,
            color = contentColor.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = month,
                color = contentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "选择月份",
                tint = contentColor
            )
        }
    }
}

@Composable
private fun IncomeExpenseGroup(
    income: String,
    expense: String,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SummaryItem(title = "收入", amount = income, contentColor = contentColor)
        SummaryItem(title = "支出", amount = expense, contentColor = contentColor)
    }
}

@Composable
private fun VerticalDivider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxHeight(0.6f)
            .width(1.dp)
            .background(color.copy(alpha = 0.3f))
    )
}

@Composable
fun SummaryItem(title: String, amount: String, contentColor: Color) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            color = contentColor.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = amount,
            color = contentColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}