package com.evening.dailylife.app.navigation

object Route {
    const val HOME = "home"
    const val DETAILS = "details"
    const val CHART = "chart"
    const val DISCOVER = "discover"
    const val ME = "me"
    const val ADD_EDIT_TRANSACTION = "add_edit_transaction"

    const val TRANSACTION_DETAILS = "transaction_details/{transactionId}"

    fun transactionDetails(transactionId: Int) = "transaction_details/$transactionId"
}
