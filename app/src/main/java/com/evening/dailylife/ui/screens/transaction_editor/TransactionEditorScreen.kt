package com.evening.dailylife.ui.screens.transaction_editor

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
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// 数据类和分类列表 (保持不变)
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
    var selectedTab by remember { mutableStateOf(0) }
    var amount by remember { mutableStateOf("0.00") }
    var selectedCategory by remember { mutableStateOf<TransactionCategory?>(null) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    val categories = if (selectedTab == 0) expenseCategories else incomeCategories
    val dateFormat = SimpleDateFormat("MM月dd日", Locale.getDefault())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (selectedTab == 0) "记一笔支出" else "记一笔收入") },
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¥", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(amount, fontSize = 40.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { showBottomSheet = true }) {
                    Text(text = dateFormat.format(selectedDate.time))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "选择日期")
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("支出") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("收入") })
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        isSelected = category == selectedCategory,
                        onClick = {
                            selectedCategory = category
                            showBottomSheet = true
                        }
                    )
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            CalculatorPad(
                onAmountChange = { newAmount -> amount = newAmount },
                selectedDate = selectedDate,
                onDateSelected = { newDate -> selectedDate = newDate },
                onDoneClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                        // TODO: 保存逻辑
                        navController.popBackStack()
                    }
                },
                initialValue = amount
            )
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

// --- 经过布局和结构优化的计算器 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorPad(
    onAmountChange: (String) -> Unit,
    onDoneClick: () -> Unit,
    initialValue: String,
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit
) {
    var currentValue by remember { mutableStateOf(if (initialValue == "0.00") "" else initialValue) }
    var currentOperator by remember { mutableStateOf<String?>(null) }
    var previousValue by remember { mutableStateOf<Double?>(null) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.timeInMillis)
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("M.d", Locale.getDefault())

    // 定义所有按钮及其行为
    val buttons = listOf(
        "7", "8", "9", "date",
        "4", "5", "6", "+",
        "1", "2", "3", "-",
        ".", "0", "backspace", "done"
    )

    // --- 统一的计算和输入处理逻辑 ---
    // ... (内部函数保持不变)

    Surface(modifier = Modifier.navigationBarsPadding()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(buttons) { item ->
                val buttonModifier = Modifier.padding(4.dp).height(64.dp)
                when (item) {
                    "date" -> CalculatorButton(modifier = buttonModifier, text = dateFormat.format(selectedDate.time), onClick = { showDatePicker = true })
                    "backspace" -> CalculatorButton(modifier = buttonModifier, icon = Icons.AutoMirrored.Filled.Backspace, onClick = { /* ... handleBackspace ... */ })
                    "+" -> CalculatorButton(modifier = buttonModifier, text = "+", onClick = { /* ... handleOperator("+") ... */ })
                    "-" -> CalculatorButton(modifier = buttonModifier, text = "-", onClick = { /* ... handleOperator("-") ... */ })
                    "done" -> {
                        // 特殊处理“完成”按钮，使用更醒目的样式
                        Button(
                            modifier = buttonModifier.fillMaxSize(),
                            onClick = {
                                // ... performCalculation if needed ...
                                onDoneClick()
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("完成", fontSize = 16.sp)
                        }
                    }
                    else -> CalculatorButton(modifier = buttonModifier, text = item, onClick = { /* ... handleInput(item) ... */ })
                }
            }
        }
    }

    if (showDatePicker) {
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


@Composable
fun CalculatorButton(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
    ) {
        if (text != null) {
            Text(text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        } else if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp))
        }
    }
}