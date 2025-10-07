package com.evening.dailylife.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.evening.dailylife.ui.screens.main.HomeScreen
import com.evening.dailylife.ui.screens.transaction_editor.TransactionEditorScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.HOME,
        modifier = modifier
    ) {
        composable(Route.HOME) {
            HomeScreen(
                onAddTransactionClick = {
                    navController.navigate(Route.ADD_EDIT_TRANSACTION)
                }
            )
        }
        composable(Route.ADD_EDIT_TRANSACTION) {
            TransactionEditorScreen(navController = navController)
        }
    }
}