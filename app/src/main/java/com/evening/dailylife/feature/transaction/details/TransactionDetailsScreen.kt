package com.evening.dailylife.feature.transaction.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.evening.dailylife.R
import com.evening.dailylife.app.navigation.Route
import com.evening.dailylife.core.model.TransactionCategoryRepository
import com.evening.dailylife.feature.transaction.details.component.TransactionDetailsContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    navController: NavController,
    viewModel: TransactionDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.details)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.error != null -> {
                    Text(text = uiState.error ?: stringResource(R.string.error_load_failed))
                }

                uiState.transaction != null -> {
                    val transaction = uiState.transaction!!
                    val context = LocalContext.current
                    val configuration = LocalConfiguration.current
                    val categoryLabel = remember(transaction.category, configuration) {
                        TransactionCategoryRepository.getDisplayName(context, transaction.category)
                    }
                    val iconVector = remember(transaction.category) {
                        TransactionCategoryRepository.getIcon(transaction.category)
                    }
                    TransactionDetailsContent(
                        transaction = transaction,
                        categoryLabel = categoryLabel,
                        onDelete = {
                            showDeleteDialog = true
                        },
                        onEdit = {
                            navController.navigate(
                                Route.addEditTransactionWithId(transaction.id),
                            )
                        },
                        iconVector = iconVector,
                    )

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDeleteDialog = false
                                    viewModel.deleteTransaction(transaction) {
                                        navController.navigateUp()
                                    }
                                }) {
                                    Text(text = stringResource(id = R.string.common_confirm))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text(text = stringResource(id = R.string.common_cancel))
                                }
                            },
                            title = { Text(stringResource(R.string.details_delete_dialog_title)) },
                            text = { Text(stringResource(R.string.details_delete_dialog_description)) }
                        )
                    }
                }
            }
        }
    }
}
