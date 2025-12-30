package com.evening.dailylife.feature.transaction.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.evening.dailylife.feature.transaction.details.ui.TransactionDetailsScreen
import com.evening.dailylife.feature.transaction.editor.ui.TransactionEditorScreen

@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.transactionNavGraph(
    navController: NavHostController,
) {
    composable(
        route = TransactionRoute.ADD_EDIT_TRANSACTION,
        arguments = listOf(
            navArgument("transactionId") {
                type = NavType.IntType
                defaultValue = -1 // -1 表示新建
            },
            navArgument("categoryId") {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            },
            navArgument("isExpense") {
                type = NavType.BoolType
                defaultValue = true
            }
        )
    ) {
        TransactionEditorScreen(navController = navController)
    }
    composable(
        route = TransactionRoute.TRANSACTION_DETAILS,
        arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
    ) {
        TransactionDetailsScreen(navController = navController)
    }
}
