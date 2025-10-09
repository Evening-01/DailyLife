package com.evening.dailylife.feature.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.evening.dailylife.app.navigation.Route
import com.evening.dailylife.app.navigation.items
import com.evening.dailylife.core.designsystem.component.AnimatedBottomBarIcon
import com.evening.dailylife.feature.chart.ChartScreen
import com.evening.dailylife.feature.details.DetailsScreen
import com.evening.dailylife.feature.discover.DiscoverScreen
import com.evening.dailylife.feature.me.MeScreen

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTransactionClick: () -> Unit,
    appNavController: NavHostController
) {
    val homeNavController = rememberNavController()
    val navBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                items.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            AnimatedBottomBarIcon(
                                screen = screen,
                                isSelected = isSelected
                            )
                        },
                        label = { Text(stringResource(id = screen.labelResId)) },
                        selected = isSelected,
                        onClick = {
                            homeNavController.navigate(screen.route) {
                                popUpTo(homeNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HomeNavHost(
            homeNavController = homeNavController,
            appNavController = appNavController,
            onAddTransactionClick = onAddTransactionClick,
            modifier = Modifier.padding(PaddingValues(bottom = innerPadding.calculateBottomPadding()))
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HomeNavHost(
    homeNavController: NavHostController,
    appNavController: NavHostController,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = homeNavController,
        startDestination = Route.DETAILS,
        modifier = modifier
    ) {
        composable(Route.DETAILS) {
            DetailsScreen(
                onTransactionClick = { transactionId ->
                    // 使用主 appNavController 来执行跨页面的跳转
                    appNavController.navigate(Route.transactionDetails(transactionId))
                },
                onAddTransactionClick = onAddTransactionClick
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
    }
}