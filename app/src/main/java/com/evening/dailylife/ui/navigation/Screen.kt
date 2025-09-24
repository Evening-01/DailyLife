package com.evening.dailylife.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.evening.dailylife.R

sealed class Screen(val route: String, @StringRes val labelResId: Int, val icon: ImageVector) {
    // 2. 传入正确的 String 资源 ID
    object Details : Screen(Route.Details, R.string.details, Icons.Default.List)
    object Chart : Screen(Route.Chart, R.string.chart, Icons.Default.BarChart)
    object Discover : Screen(Route.Discover, R.string.discover, Icons.Default.Explore)
    object Me : Screen(Route.Me, R.string.me, Icons.Default.Person)
}

val items = listOf(
    Screen.Details,
    Screen.Chart,
    Screen.Discover,
    Screen.Me,
)