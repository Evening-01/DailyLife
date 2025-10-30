package com.evening.dailylife.feature.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import android.os.Build
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTransactionClick: () -> Unit,
    appNavController: NavHostController,
) {
    val homeNavController = rememberNavController()
    val navBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                items.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            homeNavController.navigate(item.route) {
                                popUpTo(homeNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            AnimatedBottomBarIcon(
                                screen = item,
                                isSelected = isSelected,
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(id = item.labelResId),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        ),
    ) { innerPadding ->
        HomeNavHost(
            navController = homeNavController,
            appNavController = appNavController,
            onAddTransactionClick = onAddTransactionClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun HomeNavHost(
    navController: NavHostController,
    appNavController: NavHostController,
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
                onTransactionClick = { transactionId ->
                    appNavController.navigate(Route.transactionDetails(transactionId))
                },
                onAddTransactionClick = onAddTransactionClick,
            )
        }
        composable(Route.CHART) {
            ChartScreen()
        }
        composable(Route.DISCOVER) {
            DiscoverScreen()
        }
        composable(Route.ME) {
            MeScreen(
                onAboutAuthorClick = {
                    navController.navigateFromMe(appNavController, Route.ABOUT_AUTHOR)
                },
                onGeneralSettingsClick = {
                    navController.navigateFromMe(appNavController, Route.GENERAL_SETTINGS)
                },
                onQuickUsageClick = {
                    navController.navigateFromMe(appNavController, Route.QUICK_USAGE)
                },
            )
        }
    }
}

private fun NavHostController.navigateFromMe(
    appNavController: NavHostController,
    destinationRoute: String,
) {
    val isOnMeTab = currentBackStackEntry?.destination?.route == Route.ME
    if (!isOnMeTab) {
        return
    }

    val currentGlobalRoute = appNavController.currentBackStackEntry?.destination?.route
    if (currentGlobalRoute == destinationRoute) {
        return
    }

    appNavController.navigate(destinationRoute) {
        launchSingleTop = true
    }
}
