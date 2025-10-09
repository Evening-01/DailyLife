package com.evening.dailylife.app.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.evening.dailylife.feature.discover.TransactionDetailsScreen
import com.evening.dailylife.feature.home.HomeScreen
import com.evening.dailylife.feature.transaction.editor.TransactionEditorScreen

@RequiresApi(Build.VERSION_CODES.O)
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
                },
                appNavController = navController
            )
        }
        composable(Route.ADD_EDIT_TRANSACTION) {
            TransactionEditorScreen(navController = navController)
        }
        composable(
            route = Route.TRANSACTION_DETAILS,
            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
        ) {
            TransactionDetailsScreen(navController = navController)
        }
    }
}
