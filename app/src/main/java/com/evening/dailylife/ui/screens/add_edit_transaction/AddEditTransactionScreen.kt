package com.evening.dailylife.ui.screens.add_edit_transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddEditTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Listen for the saving state to navigate back
    LaunchedEffect(uiState.isSaving) {
        if (!uiState.isSaving && uiState.error == null) {
            // A slight delay could be added here if needed, but for now, let's assume direct navigation is fine.
            // onNavigateUp() // This might be too abrupt. Let's handle navigation on a successful save explicitly.
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记一笔") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        // Use Icons.AutoMirrored.Filled.ArrowBack
                        Icon(contentDescription = "返回", imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.saveTransaction()
                // Navigate up after saving
                onNavigateUp()
            }) {
                Icon(contentDescription = "保存", imageVector = androidx.compose.material.icons.Icons.Default.Check)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Amount Input
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("金额") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Category Input (For now, a simple text field. Could be replaced with a dropdown/selector)
            OutlinedTextField(
                value = uiState.category,
                onValueChange = viewModel::onCategoryChange,
                label = { Text("分类 (例如: 餐饮, 购物)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Description Input
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("备注 (可选)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Transaction Type (Expense/Income)
            Row(modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = uiState.isExpense,
                    onClick = { viewModel.onTransactionTypeChange(true) },
                    label = { Text("支出") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = !uiState.isExpense,
                    onClick = { viewModel.onTransactionTypeChange(false) },
                    label = { Text("收入") }
                )
            }

            if (uiState.error != null) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}