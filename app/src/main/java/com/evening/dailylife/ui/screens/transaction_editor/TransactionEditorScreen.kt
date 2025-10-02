package com.evening.dailylife.ui.screens.transaction_editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// 数据类和分类列表 (这部分保持不变)
data class TransactionCategory(
    val name: String,
    val icon: ImageVector,
    val color: Color = Color.Black
)

val expenseCategories = listOf(
    TransactionCategory("餐饮", Icons.Default.Fastfood, Color(0xFFF44336)),
    TransactionCategory("交通", Icons.Default.Commute, Color(0xFF9C27B0)),
    TransactionCategory("购物", Icons.Default.ShoppingCart, Color(0xFF3F51B5)),
    TransactionCategory("娱乐", Icons.Default.SportsEsports, Color(0xFF009688)),
    TransactionCategory("服饰", Icons.Default.Checkroom, Color(0xFFFF9800)),
    TransactionCategory("住房", Icons.Default.Home, Color(0xFF795548)),
    TransactionCategory("通讯", Icons.Default.Phone, Color(0xFF607D8B)),
    TransactionCategory("医疗", Icons.Default.LocalHospital, Color(0xFFE91E63)),
)

val incomeCategories = listOf(
    TransactionCategory("工资", Icons.Default.MonetizationOn, Color(0xFF4CAF50)),
    TransactionCategory("理财", Icons.Default.TrendingUp, Color(0xFF2196F3)),
    TransactionCategory("红包", Icons.Default.Redeem, Color(0xFFF44336)),
    TransactionCategory("其他", Icons.Default.MoreHoriz, Color(0xFF9E9E9E)),
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditorScreen(
    navController: NavController,
    viewModel: TransactionEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCalculator by remember { mutableStateOf(false) }

    // 用于在UI上显示完整的计算表达式，如 "10 + 5"
    var displayExpression by remember { mutableStateOf(uiState.amount.ifEmpty { "0.00" }) }

    val categories = if (uiState.isExpense) expenseCategories else incomeCategories
    val selectedDate = remember(uiState.date) {
        Calendar.getInstance().apply { timeInMillis = uiState.date }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (uiState.isExpense) "记一笔支出" else "记一笔收入") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 上半部分：分类选择
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {

                // 支出/收入 切换
                TabRow(
                    selectedTabIndex = if (uiState.isExpense) 0 else 1,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Tab(
                        selected = uiState.isExpense,
                        onClick = { viewModel.onTransactionTypeChange(true) },
                        text = { Text("支出") })
                    Tab(
                        selected = !uiState.isExpense,
                        onClick = { viewModel.onTransactionTypeChange(false) },
                        text = { Text("收入") })
                }

                // 分类选择
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    items(categories) { category ->
                        CategoryItem(
                            category = category,
                            isSelected = category.name == uiState.category,
                            onClick = {
                                viewModel.onCategoryChange(category.name)
                                showCalculator = true
                            }
                        )
                    }
                }
            }

            // 下半部分：带动画的金额显示 + 备注 + 计算器
            AnimatedVisibility(
                visible = showCalculator,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column {
                        // 金额显示区域
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.End, // 靠右对齐
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "¥",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.Bottom).padding(bottom = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = displayExpression,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                        }


                        // 备注输入框
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = viewModel::onDescriptionChange,
                            label = { Text("备注(可选)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        )

                        // 计算器
                        CalculatorPad(
                            initialValue = uiState.amount,
                            // 传入回调，同时更新UI表达式和ViewModel中的数值
                            onExpressionChange = { newExpression, numericValue ->
                                displayExpression = newExpression
                                viewModel.onAmountChange(numericValue)
                            },
                            selectedDate = selectedDate,
                            onDateSelected = { calendar -> viewModel.onDateChange(calendar.timeInMillis) },
                            onSaveClick = {
                                viewModel.saveTransaction()
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun CategoryItem(category: TransactionCategory, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (isSelected) category.color else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = category.name, fontSize = 13.sp)
    }
}


@Composable
fun CalculatorPad(
    initialValue: String,
    onExpressionChange: (expression: String, numericValue: String) -> Unit,
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    onSaveClick: () -> Unit,
) {
    // --- 状态变量 ---
    var currentInput by remember { mutableStateOf(initialValue.ifEmpty { "0" }) }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var operator by remember { mutableStateOf<String?>(null) }
    var clearInputOnNextDigit by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("M.d", Locale.getDefault())

    fun formatDecimal(number: Double): String {
        val df = DecimalFormat("#.##")
        df.isGroupingUsed = false
        return df.format(number)
    }

    val visualExpression = remember(firstOperand, operator, currentInput, clearInputOnNextDigit) {
        val firstPart = if (firstOperand != null && operator != null) {
            "${formatDecimal(firstOperand!!)} $operator "
        } else {
            ""
        }
        val secondPart = if (clearInputOnNextDigit && operator != null) "" else currentInput
        val fullExpr = (firstPart + secondPart).trim()
        fullExpr.ifEmpty { "0" }
    }

    val numericValue = currentInput

    LaunchedEffect(visualExpression, numericValue) {
        onExpressionChange(visualExpression, numericValue)
    }

    fun performCalculation() {
        val first = firstOperand ?: return
        val second = currentInput.toDoubleOrNull() ?: first
        val result = when (operator) {
            "+" -> first + second
            "-" -> first - second
            else -> return
        }
        currentInput = formatDecimal(result)
        operator = null
        firstOperand = null
        clearInputOnNextDigit = true
    }

    fun handleInput(input: Any) {
        when (input) {
            is String -> {
                when {
                    input in "0".."9" -> {
                        if (clearInputOnNextDigit) {
                            currentInput = input
                            clearInputOnNextDigit = false
                        } else {
                            currentInput = if (currentInput == "0") input else currentInput + input
                        }
                    }
                    input == "." -> {
                        if (clearInputOnNextDigit) {
                            currentInput = "0."
                            clearInputOnNextDigit = false
                        } else if (!currentInput.contains(".")) {
                            currentInput += "."
                        }
                    }
                    input in listOf("+", "-") -> {
                        if (!clearInputOnNextDigit && firstOperand != null && operator != null) {
                            performCalculation()
                        }
                        firstOperand = currentInput.toDoubleOrNull()
                        operator = input
                        clearInputOnNextDigit = true
                    }
                    input == "=" -> {
                        if (operator != null) {
                            performCalculation()
                        }
                    }
                    input == "完成" -> {
                        onExpressionChange(currentInput, currentInput)
                        onSaveClick()
                    }
                    input == "date" -> showDatePicker = true
                }
            }
            is ImageVector -> {
                if (!clearInputOnNextDigit && currentInput.isNotEmpty()) {
                    currentInput = currentInput.dropLast(1)
                    if (currentInput.isEmpty()) {
                        currentInput = "0"
                    }
                }
            }
        }
    }

    val finalButtonAction = if (operator != null) "=" else "完成"
    val buttons = listOf(
        "7", "8", "9", "date",
        "4", "5", "6", "+",
        "1", "2", "3", "-",
        ".", "0", Icons.AutoMirrored.Filled.Backspace, finalButtonAction
    )

    Surface(
        modifier = Modifier.navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(8.dp),
        ) {
            items(buttons) { item ->
                val modifier = Modifier
                    .padding(4.dp)
                    .height(60.dp)

                val isFinalButton = item == "=" || item == "完成"
                val isOperator = item is String && item in listOf("+", "-")
                val isDate = item == "date"

                if (isFinalButton) {
                    Button(
                        onClick = { handleInput(item) },
                        modifier = modifier,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(item as String, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(
                        onClick = { handleInput(item) },
                        modifier = modifier,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isOperator) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        when (item) {
                            is String -> {
                                val text = if (isDate) dateFormat.format(selectedDate.time) else item
                                Text(
                                    text = text,
                                    fontSize = if (isDate) 16.sp else 22.sp,
                                    fontWeight = if (isOperator || isDate) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                            is ImageVector -> {
                                Icon(item, contentDescription = "Backspace", modifier = Modifier.size(26.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.timeInMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let {
                        val calendar = Calendar.getInstance().apply { timeInMillis = it }
                        onDateSelected(calendar)
                    }
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}