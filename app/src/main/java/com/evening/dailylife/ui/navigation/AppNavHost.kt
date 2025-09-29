package com.evening.dailylife.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.evening.dailylife.ui.screens.chart.ChartScreen
import com.evening.dailylife.ui.screens.details.DetailsScreen
import com.evening.dailylife.ui.screens.details.DiscoverScreen
import com.evening.dailylife.ui.screens.me.MeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Details,
        modifier = modifier
    ) {
        composable(Route.Details) {
            DetailsScreen(
                onTransactionClick = { transactionId ->
                }
            )
        }
        composable(Route.Chart) {
            ChartScreen()
        }
        composable(Route.Discover) {
            DiscoverScreen()
        }
        composable(Route.Me) {
            MeScreen(viewModel = hiltViewModel())
        }
    }
}