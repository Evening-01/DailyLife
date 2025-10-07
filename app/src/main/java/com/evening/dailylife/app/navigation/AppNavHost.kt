package com.evening.dailylife.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.evening.dailylife.feature.home.HomeScreen
import com.evening.dailylife.feature.transaction.editor.TransactionEditorScreen

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