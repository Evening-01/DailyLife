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
import com.evening.dailylife.feature.home.HomeScreen
import com.evening.dailylife.feature.me.about.AboutAuthorScreen
import com.evening.dailylife.feature.me.about.AboutAppScreen
import com.evening.dailylife.feature.me.settings.quickusage.QuickUsageScreen
import com.evening.dailylife.feature.me.settings.datamanagement.DataManagementScreen
import com.evening.dailylife.feature.me.settings.general.GeneralSettingsScreen
import com.evening.dailylife.feature.mortgage.MortgageCalculatorScreen
import com.evening.dailylife.feature.currency.CurrencyConverterScreen
import com.evening.dailylife.feature.transaction.details.TransactionDetailsScreen
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
                    // 导航到编辑页面，不带参数表示新建
                    navController.navigate(Route.addEditTransactionWithId(-1))
                },
                appNavController = navController
            )
        }
        composable(
            route = Route.ADD_EDIT_TRANSACTION,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.IntType
                    defaultValue = -1 // -1 表示新建
                },
                navArgument("categoryId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                },
                navArgument("isExpense") {
                    type = NavType.BoolType
                    defaultValue = true
                }
            )
        ) {
            TransactionEditorScreen(navController = navController)
        }
        composable(
            route = Route.TRANSACTION_DETAILS,
            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
        ) {
            TransactionDetailsScreen(navController = navController)
        }
        composable(Route.ABOUT_AUTHOR) {
            AboutAuthorScreen(navController = navController)
        }
        composable(Route.ABOUT_APP) {
            AboutAppScreen(navController = navController)
        }
        composable(Route.GENERAL_SETTINGS) {
            GeneralSettingsScreen(navController = navController)
        }
        composable(Route.QUICK_USAGE) {
            QuickUsageScreen(navController = navController)
        }
        composable(Route.DATA_MANAGEMENT) {
            DataManagementScreen(navController = navController)
        }
        composable(Route.MORTGAGE_CALCULATOR) {
            MortgageCalculatorScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(Route.CURRENCY_CONVERTER) {
            CurrencyConverterScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
