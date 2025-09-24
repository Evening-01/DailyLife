package com.evening.dailylife.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Details : Screen(Route.Details, "明细", Icons.Default.List)
    object Chart : Screen(Route.Chart, "图表", Icons.Default.BarChart)
    object Discover : Screen(Route.Discover, "发现", Icons.Default.Explore)
    object Me : Screen(Route.Me, "我的", Icons.Default.Person)
}

val items = listOf(
    Screen.Details,
    Screen.Chart,
    Screen.Discover,
    Screen.Me,
)