package com.evening.dailylife.ui.screens.main

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
import com.evening.dailylife.ui.component.AnimatedBottomBarIcon
import com.evening.dailylife.ui.navigation.Route
import com.evening.dailylife.ui.navigation.items
import com.evening.dailylife.ui.screens.chart.ChartScreen
import com.evening.dailylife.ui.screens.details.DetailsScreen
import com.evening.dailylife.ui.screens.discover.DiscoverScreen
import com.evening.dailylife.ui.screens.me.MeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTransactionClick: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
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
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
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
        }
    ) { innerPadding ->
        HomeNavHost(
            navController = navController,
            onAddTransactionClick = onAddTransactionClick,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun HomeNavHost(
    navController: NavHostController,
    onAddTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.DETAILS,
        modifier = modifier
    ) {
        composable(Route.DETAILS) {
            DetailsScreen(
                onTransactionClick = { /* TODO: navigate to transaction detail */ },
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