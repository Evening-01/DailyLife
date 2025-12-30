package com.evening.dailylife.feature.home.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.evening.dailylife.core.ui.navigation.TopLevelDestination
import com.evening.dailylife.feature.home.ui.HomeScreen

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.homeNavGraph(
    topLevelDestinations: List<TopLevelDestination>,
    appNavController: NavHostController,
    onAddTransactionClick: () -> Unit,
) {
    composable(HomeDestination.HOME) {
        HomeScreen(
            topLevelDestinations = topLevelDestinations,
            onAddTransactionClick = onAddTransactionClick,
            appNavController = appNavController,
        )
    }
}
