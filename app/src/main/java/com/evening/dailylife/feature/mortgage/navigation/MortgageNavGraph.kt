package com.evening.dailylife.feature.mortgage.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.evening.dailylife.feature.mortgage.ui.MortgageCalculatorScreen

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.mortgageNavGraph(
    navController: NavHostController,
) {
    composable(MortgageRoute.MORTGAGE_CALCULATOR) {
        MortgageCalculatorScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
}
