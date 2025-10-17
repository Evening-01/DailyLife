package com.evening.dailylife.feature.transaction.editor

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evening.dailylife.R
import com.evening.dailylife.core.designsystem.component.CalendarPickerBottomSheet
import com.evening.dailylife.core.designsystem.component.CalendarPickerType
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 底部计算器区域，负责金额输入、日期选择与保存动作。
 */
@RequiresApi(Build.VERSION_CODES.O)
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
    val doneLabel = stringResource(R.string.editor_action_done)
    val todayLabel = stringResource(R.string.label_today)

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
                    doneLabel -> {
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

    val finalButtonAction = if (operator != null) "=" else doneLabel
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
                    "=", doneLabel -> {
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
                                contentDescription = stringResource(R.string.editor_action_backspace),
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
                                            contentDescription = todayLabel,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(todayLabel, fontSize = 16.sp)
                                    }
                                } else {
                                    val dateFormat =
                                        SimpleDateFormat("yy/M/d", Locale.getDefault())
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

    CalendarPickerBottomSheet(
        showBottomSheet = showDatePicker,
        onDismiss = { showDatePicker = false },
        type = CalendarPickerType.DATE,
        initialDate = selectedDate,
        onDateSelected = { year, month, day ->
            val newCalendar = Calendar.getInstance().apply {
                clear()
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
            }
            onDateSelected(newCalendar)
        },
        onMonthSelected = { _, _ -> }
    )
}
