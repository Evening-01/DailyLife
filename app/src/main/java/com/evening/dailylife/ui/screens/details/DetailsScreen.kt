package com.evening.dailylife.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evening.dailylife.R
import com.evening.dailylife.ui.theme.LocalExtendedColorScheme
import com.evening.dailylife.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

// 模拟数据类
data class Transaction(
    val id: Int,
    val category: String,
    val description: String,
    val amount: Double,
    val icon: ImageVector,
    val date: String
)

data class DailyTransactions(
    val date: String,
    val transactions: List<Transaction>,
    val dailyIncome: Double,
    val dailyExpense: Double
)

//private val sampleTransactions = emptyList<DailyTransactions>()
private val sampleTransactions = listOf(
    DailyTransactions(
        date = "今天 09/28 星期日",
        dailyIncome = 0.0,
        dailyExpense = -80.50,
        transactions = listOf(
            Transaction(1, "餐饮", "跟朋友吃饭", -12.00, Icons.Default.Restaurant, "2025/09/28"),
            Transaction(2, "购物", "出去买东西", -68.50, Icons.Default.ShoppingCart, "2025/09/28")
        )
    ),
    DailyTransactions(
        date = "09/27 星期六",
        dailyIncome = 1000.00,
        dailyExpense = -25.00,
        transactions = listOf(
            Transaction(3, "餐饮", "", -25.00, Icons.Default.Restaurant, "2025/09/27"),
            Transaction(4, "工资", "发工资了", 1000.00, Icons.Default.AttachMoney, "2025/09/27")
        )
    ),
    DailyTransactions(
        date = "09/26 星期五",
        dailyIncome = 0.0,
        dailyExpense = -100.00,
        transactions = listOf(
            Transaction(5, "数码", "买电子配件", -100.00, Icons.Default.Devices, "2025/09/26")
        )
    )
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    onTransactionClick: (Int) -> Unit
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.timeInMillis
    )

    // 日期格式化工具
    val yearFormat = SimpleDateFormat("yyyy年", Locale.getDefault())
    val monthFormat = SimpleDateFormat("M月", Locale.getDefault())

    val headerContainerColor = LocalExtendedColorScheme.current.headerContainer
    val headerContentColor = LocalExtendedColorScheme.current.onHeaderContainer

    // 日期选择对话框
    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePickerDialog = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newCalendar = Calendar.getInstance().apply {
                                timeInMillis = millis
                            }
                            selectedDate = newCalendar
                        }
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePickerDialog = false }
                ) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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
                onClick = { /* TODO: 添加新账单 */ },
                icon = { Icon(Icons.Default.Add, contentDescription = "添加账单") },
                text = { Text("记一笔") }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SummaryHeader(
                year = yearFormat.format(selectedDate.time),
                month = monthFormat.format(selectedDate.time),
                income = "1,000.00",
                expense = "520.50",
                onDateClick = { showDatePickerDialog = true },
                containerColor = headerContainerColor,
                contentColor = headerContentColor
            )

            if (sampleTransactions.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    sampleTransactions.forEach { dailyData ->
                        item {
                            DailyHeader(
                                date = dailyData.date,
                                income = dailyData.dailyIncome,
                                expense = dailyData.dailyExpense
                            )
                        }
                        items(dailyData.transactions) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onClick = { onTransactionClick(transaction.id) }
                            )
                            Divider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                modifier = Modifier.padding(start = 72.dp)
                            )
                        }
                    }
                }
            }
        }
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
private fun IncomeExpenseGroup(income: String, expense: String, contentColor: Color, modifier: Modifier = Modifier) {
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

@Composable
fun DailyHeader(date: String, income: Double, expense: Double) {
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formatDate(date),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
fun TransactionItem(transaction: Transaction, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transaction.icon,
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
                // 备注为空时，只显示分类，并垂直居中
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 金额
        Text(
            text = "%.2f".format(transaction.amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (transaction.amount > 0) SuccessGreen else MaterialTheme.colorScheme.error
        )
    }
}