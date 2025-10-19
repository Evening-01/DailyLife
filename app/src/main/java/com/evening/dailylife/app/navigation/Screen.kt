package com.evening.dailylife.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.evening.dailylife.R

sealed class Screen(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector,
) {
    object Details : Screen(Route.DETAILS, R.string.details, Icons.AutoMirrored.Filled.List)
    object Chart : Screen(Route.CHART, R.string.chart, Icons.Default.BarChart)
    object Discover : Screen(Route.DISCOVER, R.string.discover, Icons.Default.Explore)
    object Me : Screen(Route.ME, R.string.me, Icons.Default.Person)
}

val items = listOf(
    Screen.Details,
    Screen.Chart,
    Screen.Discover,
    Screen.Me,
)
