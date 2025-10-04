package com.evening.dailylife.ui.screens.transaction_editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Checkroom
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
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.sharp.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.moriafly.salt.ui.UnstableSaltApi
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val MAX_AMOUNT = 100_000_000.0
private const val MAX_INTEGER_LENGTH = 8


// 数据类和分类列表
data class TransactionCategory(
    val name: String,
    val icon: ImageVector
)

val expenseCategories = listOf(
    TransactionCategory("餐饮", Icons.Default.Fastfood),
    TransactionCategory("交通", Icons.Default.Commute),
    TransactionCategory("购物", Icons.Default.ShoppingCart),
    TransactionCategory("娱乐", Icons.Default.SportsEsports),
    TransactionCategory("服饰", Icons.Default.Checkroom),
    TransactionCategory("住房", Icons.Default.Home),
    TransactionCategory("通讯", Icons.Default.Phone),
    TransactionCategory("医疗", Icons.Default.LocalHospital),
)

val incomeCategories = listOf(
    TransactionCategory("工资", Icons.Default.MonetizationOn),
    TransactionCategory("理财", Icons.Default.TrendingUp),
    TransactionCategory("红包", Icons.Default.Redeem),
    TransactionCategory("其他", Icons.Default.MoreHoriz),
)

@Composable
fun TransactionEditorScreen(
    navController: NavController,
    viewModel: TransactionEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    TransactionEditorContent(
        uiState = uiState,
        onTransactionTypeChange = viewModel::onTransactionTypeChange,
        onCategoryChange = viewModel::onCategoryChange,
        onAmountChange = viewModel::onAmountChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onDateChange = viewModel::onDateChange,
        onSaveTransaction = {
            viewModel.saveTransaction()
            navController.popBackStack()
        },
        onNavigateBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, UnstableSaltApi::class)
@Composable
fun TransactionEditorContent(
    uiState: TransactionEditorUiState,
    onTransactionTypeChange: (Boolean) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onSaveTransaction: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showCalculator by remember { mutableStateOf(false) }
    var displayExpression by remember { mutableStateOf(uiState.amount.ifEmpty { "0.00" }) }

    val categories = if (uiState.isExpense) expenseCategories else incomeCategories
    val selectedDate = remember(uiState.date) {
        Calendar.getInstance().apply { timeInMillis = uiState.date }
    }

    val remarkFocusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    BackHandler(enabled = showCalculator) {
        showCalculator = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (uiState.isExpense) "记一笔支出" else "记一笔收入") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showCalculator) {
                            showCalculator = false
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Sharp.ArrowBackIosNew, contentDescription = "关闭")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                TabRow(
                    selectedTabIndex = if (uiState.isExpense) 0 else 1
                ) {
                    Tab(
                        selected = uiState.isExpense,
                        onClick = { onTransactionTypeChange(true) },
                        text = { Text("支出") },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Tab(
                        selected = !uiState.isExpense,
                        onClick = { onTransactionTypeChange(false) },
                        text = { Text("收入") },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        isSelected = category.name == uiState.category,
                        onClick = {
                            onCategoryChange(category.name)
                            showCalculator = true
                        }
                    )
                }
            }

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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .widthIn(min = 120.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            remarkFocusRequester.requestFocus()
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    BasicTextField(
                                        value = uiState.description,
                                        onValueChange = {
                                            if (it.length <= 18) {
                                                onDescriptionChange(it)
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("备注不能超过18个字")
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(remarkFocusRequester),
                                        textStyle = LocalTextStyle.current.copy(
                                            fontSize = 16.sp,
                                            color = LocalContentColor.current
                                        ),
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        decorationBox = { innerTextField ->
                                            Box(contentAlignment = Alignment.CenterStart) {
                                                if (uiState.description.isEmpty()) {
                                                    Text(
                                                        text = "备注(可选)",
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 16.sp,
                                                        maxLines = 1
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )
                                }

                                Row(
                                    modifier = Modifier.padding(end = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "¥",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = displayExpression,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        softWrap = false,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }

                        CalculatorPad(
                            initialValue = uiState.amount,
                            onExpressionChange = { newExpression, numericValue ->
                                displayExpression = newExpression
                                onAmountChange(numericValue)
                            },
                            selectedDate = selectedDate,
                            onDateSelected = { calendar -> onDateChange(calendar.timeInMillis) },
                            onSaveClick = onSaveTransaction
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
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = category.name, fontSize = 13.sp)
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorPad(
    initialValue: String,
    onExpressionChange: (expression: String, numericValue: String) -> Unit,
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    onSaveClick: () -> Unit,
) {
    var currentInput by remember { mutableStateOf(initialValue.ifEmpty { "0" }) }
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    var operator by remember { mutableStateOf<String?>(null) }
    var clearInputOnNextDigit by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current

    fun isToday(calendar: Calendar): Boolean {
        val today = Calendar.getInstance()
        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
    }

    fun formatDecimal(number: Double): String {
        val clampedNumber = number.coerceAtMost(MAX_AMOUNT - 1)
        val df = DecimalFormat("#.##")
        df.isGroupingUsed = false
        return df.format(clampedNumber)
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

    fun clearAll() {
        currentInput = "0"
        firstOperand = null
        operator = null
        clearInputOnNextDigit = true
    }

    fun handleInput(input: Any) {
        if (input !is Unit) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.VirtualKey)
        }

        when (input) {
            is String -> {
                when (input) {
                    in "0".."9" -> {
                        if (clearInputOnNextDigit) {
                            currentInput = input
                            clearInputOnNextDigit = false
                            return
                        }

                        val parts = currentInput.split('.')
                        if (parts.size == 1) {
                            if (parts[0] == "0") {
                                currentInput = input
                            } else if (parts[0].length < MAX_INTEGER_LENGTH) {
                                currentInput += input
                            }
                        } else {
                            if (parts[1].length < 2) {
                                currentInput += input
                            }
                        }
                    }
                    "." -> {
                        if (!currentInput.contains(".")) {
                            currentInput += "."
                            clearInputOnNextDigit = false
                        }
                    }
                    in listOf("+", "-") -> {
                        if (!clearInputOnNextDigit && firstOperand != null && operator != null) {
                            performCalculation()
                        }
                        firstOperand = currentInput.toDoubleOrNull()
                        operator = input
                        clearInputOnNextDigit = true
                    }
                    "=" -> {
                        if (operator != null) {
                            performCalculation()
                        }
                    }
                    "完成" -> {
                        onExpressionChange(currentInput, currentInput)
                        onSaveClick()
                    }
                    "date" -> showDatePicker = true
                }
            }
            is ImageVector -> {
                if (clearInputOnNextDigit && operator != null) {
                    currentInput = firstOperand?.let { formatDecimal(it) } ?: "0"
                    operator = null
                    firstOperand = null
                    clearInputOnNextDigit = false
                } else if (currentInput.isNotEmpty() && currentInput != "0") {
                    currentInput = currentInput.dropLast(1)
                    if (currentInput.isEmpty() || currentInput == "-") {
                        currentInput = "0"
                        clearInputOnNextDigit = true
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

                when (item) {
                    "=", "完成" -> {
                        Button(
                            onClick = { handleInput(item) },
                            modifier = modifier,
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(item as String, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    is ImageVector -> {
                        Box(
                            modifier = modifier
                                .clip(MaterialTheme.shapes.medium)
                                .combinedClickable(
                                    onClick = { handleInput(item) },
                                    onLongClick = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        clearAll()
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item,
                                contentDescription = "Backspace",
                                modifier = Modifier.size(26.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    else -> {
                        TextButton(
                            onClick = { handleInput(item) },
                            modifier = modifier,
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            if (item == "date") {
                                if (isToday(selectedDate)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.EditCalendar,
                                            contentDescription = "今天",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("今天", fontSize = 16.sp)
                                    }
                                } else {
                                    val dateFormat = SimpleDateFormat("yy/M/d", Locale.getDefault())
                                    Text(
                                        text = dateFormat.format(selectedDate.time),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Text(
                                    text = item as String,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Normal
                                )
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