package com.evening.dailylife.feature.home.ui

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.evening.dailylife.core.ui.designsystem.component.AnimatedBottomBarIcon
import com.evening.dailylife.core.ui.navigation.TopLevelDestination
import com.evening.dailylife.feature.chart.navigation.ChartRoute
import com.evening.dailylife.feature.chart.ui.ChartScreen
import com.evening.dailylife.feature.currency.navigation.CurrencyRoute
import com.evening.dailylife.feature.details.navigation.DetailsRoute
import com.evening.dailylife.feature.details.ui.DetailsScreen
import com.evening.dailylife.feature.discover.navigation.DiscoverRoute
import com.evening.dailylife.feature.discover.ui.DiscoverScreen
import com.evening.dailylife.feature.me.navigation.MeRoute
import com.evening.dailylife.feature.me.ui.MeScreen
import com.evening.dailylife.feature.mortgage.navigation.MortgageRoute
import com.evening.dailylife.feature.transaction.navigation.TransactionRoute

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    topLevelDestinations: List<TopLevelDestination>,
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
                topLevelDestinations.forEach { item ->
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
                                imageVector = item.icon,
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
        startDestination = DetailsRoute.DETAILS,
        modifier = modifier
    ) {
        composable(DetailsRoute.DETAILS) {
            DetailsScreen(
                appNavController = appNavController,
                onTransactionClick = { transactionId ->
                    appNavController.navigate(TransactionRoute.transactionDetails(transactionId))
                },
                onAddTransactionClick = onAddTransactionClick,
            )
        }
        composable(ChartRoute.CHART) {
            ChartScreen()
        }
        composable(DiscoverRoute.DISCOVER) {
            DiscoverScreen(
                onMortgageCalculatorClick = {
                    appNavController.navigate(MortgageRoute.MORTGAGE_CALCULATOR)
                },
                onCurrencyConverterClick = {
                    appNavController.navigate(CurrencyRoute.CURRENCY_CONVERTER)
                },
            )
        }
        composable(MeRoute.ME) {
            MeScreen(
                onAboutAuthorClick = {
                    navController.navigateFromMe(appNavController, MeRoute.ABOUT_AUTHOR)
                },
                onGeneralSettingsClick = {
                    navController.navigateFromMe(appNavController, MeRoute.GENERAL_SETTINGS)
                },
                onQuickUsageClick = {
                    navController.navigateFromMe(appNavController, MeRoute.QUICK_USAGE)
                },
                onDataManagementClick = {
                    navController.navigateFromMe(appNavController, MeRoute.DATA_MANAGEMENT)
                },
                onMoreInfoClick = {
                    navController.navigateFromMe(appNavController, MeRoute.ABOUT_APP)
                },
            )
        }
    }
}

private fun NavHostController.navigateFromMe(
    appNavController: NavHostController,
    destinationRoute: String,
) {
    val isOnMeTab = currentBackStackEntry?.destination?.route == MeRoute.ME
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
