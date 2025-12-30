package com.evening.dailylife.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import com.evening.dailylife.R
import com.evening.dailylife.core.ui.navigation.TopLevelDestination
import com.evening.dailylife.feature.chart.navigation.ChartRoute
import com.evening.dailylife.feature.details.navigation.DetailsRoute
import com.evening.dailylife.feature.discover.navigation.DiscoverRoute
import com.evening.dailylife.feature.me.navigation.MeRoute

val topLevelDestinations = listOf(
    TopLevelDestination(DetailsRoute.DETAILS, R.string.details, Icons.AutoMirrored.Filled.List),
    TopLevelDestination(ChartRoute.CHART, R.string.chart, Icons.Default.BarChart),
    TopLevelDestination(DiscoverRoute.DISCOVER, R.string.discover, Icons.Default.Explore),
    TopLevelDestination(MeRoute.ME, R.string.me, Icons.Default.Person),
)
