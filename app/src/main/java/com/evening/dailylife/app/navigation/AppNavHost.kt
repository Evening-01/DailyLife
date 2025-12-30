package com.evening.dailylife.app.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.evening.dailylife.feature.currency.navigation.currencyNavGraph
import com.evening.dailylife.feature.home.navigation.HomeDestination
import com.evening.dailylife.feature.home.navigation.homeNavGraph
import com.evening.dailylife.feature.me.navigation.meNavGraph
import com.evening.dailylife.feature.mortgage.navigation.mortgageNavGraph
import com.evening.dailylife.feature.transaction.navigation.TransactionRoute
import com.evening.dailylife.feature.transaction.navigation.transactionNavGraph

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
        homeNavGraph(
            topLevelDestinations = topLevelDestinations,
            appNavController = navController,
            onAddTransactionClick = {
                // 导航到编辑页面，不带参数表示新建
                navController.navigate(TransactionRoute.addEditTransactionWithId(-1))
            }
        )
        transactionNavGraph(navController = navController)
        meNavGraph(navController = navController)
        mortgageNavGraph(navController = navController)
        currencyNavGraph(navController = navController)
    }
}
