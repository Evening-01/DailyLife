package com.evening.dailylife.ui.presentation.common

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.evening.dailylife.data.preferences.ThemeMode
import com.evening.dailylife.ui.theme.DailyTheme
import com.moriafly.salt.ui.UnstableSaltApi


fun NavHostController.debouncedPopBackStack() {
    val currentRoute = this.currentBackStackEntry?.destination?.route
    val previousRoute = this.previousBackStackEntry?.destination?.route

    if (currentRoute != null && previousRoute != null) {
        this.popBackStack()
    } else {
        Log.w("Navigation", "Attempted to pop empty back stack")
    }
}

@OptIn(UnstableSaltApi::class)
@Composable
fun HomeEntry(themeMode: ThemeMode, dynamicColor: Boolean) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        else -> false
    }

    DailyTheme(
        dynamicColor = dynamicColor,
        darkTheme = darkTheme
    ) {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

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
            // 这里是你的明细页面内容
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("明细页面")
            }
        }
        composable(Route.Chart) {
            // 这里是你的图表页面内容
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("图表页面")
            }
        }
        composable(Route.Discover) {
            // 这里是你的发现页面内容
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("发现页面")
            }
        }
        composable(Route.Me) {
            // 这里是你的我的页面内容
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("我的页面")
            }
        }
    }
}