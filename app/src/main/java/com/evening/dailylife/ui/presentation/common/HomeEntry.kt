package com.evening.dailylife.ui.presentation.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Details : Screen("details", "明细", Icons.Default.List)
    object Chart : Screen("chart", "图表", Icons.Default.BarChart)
    object Discover : Screen("discover", "发现", Icons.Default.Explore)
    object Me : Screen("me", "我的", Icons.Default.Person)
}

@Composable
fun HomeEntry() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Details,
        Screen.Chart,
        Screen.Discover,
        Screen.Me,
    )
    Scaffold(
        bottomBar = {
            NavigationBar {
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
        NavHost(navController, startDestination = Screen.Details.route, Modifier.padding(innerPadding)) {
            composable(Screen.Details.route) { /* 你的明细页面 */ Text("明细") }
            composable(Screen.Chart.route) { /* 你的图表页面 */ Text("图表") }
            composable(Screen.Discover.route) { /* 你的发现页面 */ Text("发现") }
            composable(Screen.Me.route) { /* 你的我的页面 */ Text("我的") }
        }
    }
}