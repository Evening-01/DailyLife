package com.evening.dailylife.app.main

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.evening.dailylife.app.navigation.AppNavHost

@Composable
fun DailyLifeApp() {
    val navController = rememberNavController()
    AppNavHost(navController = navController)
}
