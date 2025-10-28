package com.evening.dailylife.feature.transaction.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
                            viewModel.deleteTransaction(transaction) {
                                navController.navigateUp()
                            }
                        },
                        onEdit = {
                            navController.navigate(
                                Route.addEditTransactionWithId(transaction.id),
                            )
                        },
                        iconVector = iconVector,
                    )
                }
            }
        }
    }
}
