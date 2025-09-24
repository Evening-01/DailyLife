package com.evening.dailylife.ui.presentation.common

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Details : Screen("details", "明细", Icons.Default.List)
    object Chart : Screen("chart", "图表", Icons.Default.BarChart)
    object Discover : Screen("discover", "发现", Icons.Default.Explore)
    object Me : Screen("Me", "我的", Icons.Default.Person)
}

val items = listOf(
    Screen.Details,
    Screen.Chart,
    Screen.Discover,
    Screen.Me,
)