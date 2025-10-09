package com.evening.dailylife.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.evening.dailylife.feature.home.HomeScreen
import com.evening.dailylife.feature.transaction.details.TransactionDetailsScreen
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
                },
                // 1. 将主 NavController 传递给 HomeScreen
                //    这样 HomeScreen 内部就可以调用它来进行全局跳转
                appNavController = navController
            )
        }
        composable(Route.ADD_EDIT_TRANSACTION) {
            TransactionEditorScreen(navController = navController)
        }
        // 2. 在主导航图中注册账单详情页
        composable(
            route = Route.TRANSACTION_DETAILS,
            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
        ) {
            // 这个页面也使用主 NavController 来处理返回等操作
            TransactionDetailsScreen(navController = navController)
        }
    }
}
