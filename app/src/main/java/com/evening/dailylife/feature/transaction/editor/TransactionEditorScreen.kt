package com.evening.dailylife.feature.transaction.editor

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.evening.dailylife.R
import com.evening.dailylife.core.model.TransactionCategoryRepository
import com.evening.dailylife.feature.transaction.editor.component.CalculatorPad
import com.evening.dailylife.feature.transaction.editor.component.MoodSelector
import com.evening.dailylife.feature.transaction.editor.component.RemarkAmountCard
import com.evening.dailylife.feature.transaction.editor.component.TransactionCategoryGrid
import com.evening.dailylife.feature.transaction.editor.component.TransactionTypeTabs
import com.evening.dailylife.feature.transaction.editor.model.TransactionEditorEvent
import com.evening.dailylife.feature.transaction.editor.model.TransactionEditorUiState
import com.moriafly.salt.ui.UnstableSaltApi
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TransactionEditorScreen(
    navController: NavController,
    viewModel: TransactionEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TransactionEditorEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                TransactionEditorEvent.SaveSuccess -> {
                    navController.popBackStack()
                }
            }
        }
    }

    TransactionEditorContent(
        uiState = uiState,
        onTransactionTypeChange = viewModel::onTransactionTypeChange,
        onCategoryChange = viewModel::onCategoryChange,
        onAmountChange = viewModel::onAmountChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onDateChange = viewModel::onDateChange,
        onMoodChange = viewModel::onMoodChange, // 传递 ViewModel 的方法
        onSaveTransaction = viewModel::saveTransaction,
        onNavigateBack = { navController.popBackStack() }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, UnstableSaltApi::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionEditorContent(
    uiState: TransactionEditorUiState,
    onTransactionTypeChange: (Boolean) -> Unit,
    onCategoryChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onMoodChange: (String) -> Unit,
    onSaveTransaction: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showCalculator by remember { mutableStateOf(false) }
    var displayExpression by remember { mutableStateOf(uiState.amount.ifEmpty { "0.00" }) }
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current

    LaunchedEffect(uiState.amount) {
        if (uiState.amount.isBlank()) {
            displayExpression = "0.00"
        } else if (displayExpression == "0.00") {
            displayExpression = uiState.amount
        }
    }

    LaunchedEffect(uiState.isEditing, uiState.category) {
        if (uiState.isEditing && uiState.category.isNotBlank()) {
            showCalculator = true
        }
    }

    val categories = remember(uiState.isExpense, context) {
        if (uiState.isExpense) {
            TransactionCategoryRepository.getExpenseCategories(context)
        } else {
            TransactionCategoryRepository.getIncomeCategories(context)
        }
    }
    val selectedDate = remember(uiState.date) {
        Calendar.getInstance().apply { timeInMillis = uiState.date }
    }

    val remarkFocusRequester = remember { FocusRequester() }

    BackHandler(enabled = showCalculator) {
        showCalculator = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val title = when {
                        uiState.isEditing -> stringResource(R.string.editor_title_edit_transaction)
                        uiState.isExpense -> stringResource(R.string.editor_title_add_expense)
                        else -> stringResource(R.string.editor_title_add_income)
                    }
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showCalculator) {
                            showCalculator = false
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            Icons.Sharp.ArrowBackIosNew,
                            contentDescription = stringResource(R.string.editor_nav_close)
                        )
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
            TransactionTypeTabs(
                isExpense = uiState.isExpense,
                expenseLabel = stringResource(R.string.editor_tab_expense),
                incomeLabel = stringResource(R.string.editor_tab_income),
                onTransactionTypeChange = onTransactionTypeChange,
            )

            TransactionCategoryGrid(
                categories = categories,
                selectedCategory = uiState.category,
                onCategorySelected = { category ->
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCategoryChange(category.name)
                    showCalculator = true
                },
                onManageCategory = {
                    Toast.makeText(
                        context,
                        context.getString(R.string.editor_toast_category_settings),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                manageLabel = stringResource(R.string.editor_category_settings),
                modifier = Modifier.weight(1f)
            )

            AnimatedVisibility(
                visible = showCalculator,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
                label = "CalculatorVisibility"
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            MoodSelector(
                                selectedMood = uiState.mood,
                                onMoodSelected = onMoodChange,
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            val handleDescriptionChange: (String) -> Unit = { newText ->
                                if (newText.length > MAX_DESCRIPTION_LENGTH && uiState.description.length < MAX_DESCRIPTION_LENGTH) {
                                    Toast.makeText(
                                        context,
                                        context.getString(
                                            R.string.editor_remark_length_warning,
                                            MAX_DESCRIPTION_LENGTH
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                onDescriptionChange(newText.take(MAX_DESCRIPTION_LENGTH))
                            }

                            RemarkAmountCard(
                                description = uiState.description,
                                onDescriptionChange = handleDescriptionChange,
                                focusRequester = remarkFocusRequester,
                                onRequestFocus = { remarkFocusRequester.requestFocus() },
                                placeholderText = stringResource(R.string.editor_remark_hint_optional),
                                currencySymbol = "¥",
                                amountText = displayExpression,
                                modifier = Modifier.weight(1f)
                            )
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

