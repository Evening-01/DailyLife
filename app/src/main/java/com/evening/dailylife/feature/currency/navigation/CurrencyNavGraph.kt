package com.evening.dailylife.feature.currency.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.evening.dailylife.feature.currency.ui.CurrencyConverterScreen

fun NavGraphBuilder.currencyNavGraph(
    navController: NavHostController,
) {
    composable(CurrencyRoute.CURRENCY_CONVERTER) {
        CurrencyConverterScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
}
