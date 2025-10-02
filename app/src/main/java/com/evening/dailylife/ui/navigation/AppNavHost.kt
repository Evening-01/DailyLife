package com.evening.dailylife.ui.navigation

// 导入我们新创建的屏幕
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.evening.dailylife.ui.screens.chart.ChartScreen
import com.evening.dailylife.ui.screens.details.DetailsScreen
import com.evening.dailylife.ui.screens.discover.DiscoverScreen
import com.evening.dailylife.ui.screens.me.MeScreen
import com.evening.dailylife.ui.screens.transaction_editor.TransactionEditorScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.DETAILS,
        modifier = modifier
    ) {
        composable(Route.DETAILS) {
            DetailsScreen(
                onTransactionClick = { /*TODO*/ },
                // 在这里处理导航
                onAddTransactionClick = { navController.navigate(Route.ADD_EDIT_TRANSACTION) }
            )
        }
        composable(Route.CHART) {
            ChartScreen()
        }
        composable(Route.DISCOVER) {
            DiscoverScreen()
        }
        composable(Route.ME) {
            MeScreen()
        }
        // 添加新屏幕的 composable
        composable(Route.ADD_EDIT_TRANSACTION) {
            TransactionEditorScreen(navController = navController)
        }
    }
}