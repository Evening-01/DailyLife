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
import com.evening.dailylife.app.navigation.topLevelDestinations
import com.evening.dailylife.feature.currency.navigation.CurrencyRoute
import com.evening.dailylife.feature.currency.ui.CurrencyConverterScreen
import com.evening.dailylife.feature.home.navigation.HomeDestination
import com.evening.dailylife.feature.home.ui.HomeScreen
import com.evening.dailylife.feature.me.navigation.MeRoute
import com.evening.dailylife.feature.me.ui.about.AboutAuthorScreen
import com.evening.dailylife.feature.me.ui.about.AboutAppScreen
import com.evening.dailylife.feature.me.ui.settings.datamanagement.DataManagementScreen
import com.evening.dailylife.feature.me.ui.settings.general.GeneralSettingsScreen
import com.evening.dailylife.feature.me.ui.settings.quickusage.QuickUsageScreen
import com.evening.dailylife.feature.mortgage.navigation.MortgageRoute
import com.evening.dailylife.feature.mortgage.ui.MortgageCalculatorScreen
import com.evening.dailylife.feature.transaction.details.ui.TransactionDetailsScreen
import com.evening.dailylife.feature.transaction.editor.ui.TransactionEditorScreen
import com.evening.dailylife.feature.transaction.navigation.TransactionRoute

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.HOME,
        modifier = modifier
    ) {
        composable(HomeDestination.HOME) {
            HomeScreen(
                topLevelDestinations = topLevelDestinations,
                onAddTransactionClick = {
                    // 导航到编辑页面，不带参数表示新建
                    navController.navigate(TransactionRoute.addEditTransactionWithId(-1))
                },
                appNavController = navController,
            )
        }
        composable(
            route = TransactionRoute.ADD_EDIT_TRANSACTION,
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
            route = TransactionRoute.TRANSACTION_DETAILS,
            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
        ) {
            TransactionDetailsScreen(navController = navController)
        }
        composable(MeRoute.ABOUT_AUTHOR) {
            AboutAuthorScreen(navController = navController)
        }
        composable(MeRoute.ABOUT_APP) {
            AboutAppScreen(navController = navController)
        }
        composable(MeRoute.GENERAL_SETTINGS) {
            GeneralSettingsScreen(navController = navController)
        }
        composable(MeRoute.QUICK_USAGE) {
            QuickUsageScreen(navController = navController)
        }
        composable(MeRoute.DATA_MANAGEMENT) {
            DataManagementScreen(navController = navController)
        }
        composable(MortgageRoute.MORTGAGE_CALCULATOR) {
            MortgageCalculatorScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(CurrencyRoute.CURRENCY_CONVERTER) {
            CurrencyConverterScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
