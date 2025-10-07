package com.evening.dailylife.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.evening.dailylife.R

// 为 Screen 添加一个新参数 showTopBar，并提供默认值 false
sealed class Screen(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector,
    val showTopBar: Boolean = false // <-- 新增属性
) {
    // 为需要显示顶栏的页面，将 showTopBar 设为 true
    object Details : Screen(Route.DETAILS, R.string.details, Icons.Default.List, showTopBar = true)
    object Chart : Screen(Route.CHART, R.string.chart, Icons.Default.BarChart, showTopBar = true)
    object Discover : Screen(Route.DISCOVER, R.string.discover, Icons.Default.Explore, showTopBar = true)
    object Me : Screen(Route.ME, R.string.me, Icons.Default.Person, showTopBar = true)
}

val items = listOf(
    Screen.Details,
    Screen.Chart,
    Screen.Discover,
    Screen.Me,
)