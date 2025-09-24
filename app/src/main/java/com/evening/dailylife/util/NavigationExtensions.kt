package com.evening.dailylife.util

import android.util.Log
import androidx.navigation.NavHostController

fun NavHostController.debouncedPopBackStack() {
    val currentRoute = this.currentBackStackEntry?.destination?.route
    val previousRoute = this.previousBackStackEntry?.destination?.route

    if (currentRoute != null && previousRoute != null) {
        this.popBackStack()
    } else {
        Log.w("AppNavigation", "Attempted to pop from an empty back stack.")
    }
}