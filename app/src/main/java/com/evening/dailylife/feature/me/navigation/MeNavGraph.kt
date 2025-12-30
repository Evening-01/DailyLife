package com.evening.dailylife.feature.me.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.evening.dailylife.feature.me.ui.about.AboutAuthorScreen
import com.evening.dailylife.feature.me.ui.about.AboutAppScreen
import com.evening.dailylife.feature.me.ui.settings.datamanagement.DataManagementScreen
import com.evening.dailylife.feature.me.ui.settings.general.GeneralSettingsScreen
import com.evening.dailylife.feature.me.ui.settings.quickusage.QuickUsageScreen

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.meNavGraph(
    navController: NavHostController,
) {
    composable(MeRoute.ABOUT_AUTHOR) {
        AboutAuthorScreen(navController = navController)
    }
    composable(MeRoute.ABOUT_APP) {
        AboutAppScreen(navController = navController)
    }
    composable(MeRoute.GENERAL_SETTINGS) {
        GeneralSettingsScreen(navController = navController)
    }
    composable(MeRoute.QUICK_USAGE) {
        QuickUsageScreen(navController = navController)
    }
    composable(MeRoute.DATA_MANAGEMENT) {
        DataManagementScreen(navController = navController)
    }
}
