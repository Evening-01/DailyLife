package com.evening.dailylife.app.navigation

object Route {
    const val HOME = "home"
    const val DETAILS = "details"
    const val CHART = "chart"
    const val DISCOVER = "discover"
    const val ME = "me"
    const val ABOUT_AUTHOR = "about_author"
    const val GENERAL_SETTINGS = "general_settings"
    const val ACCOUNTING_PREFERENCES = "accounting_preferences"

    private const val ADD_EDIT_TRANSACTION_ROUTE = "add_edit_transaction"
    const val ADD_EDIT_TRANSACTION = "$ADD_EDIT_TRANSACTION_ROUTE?transactionId={transactionId}"

    const val TRANSACTION_DETAILS = "transaction_details/{transactionId}"

    fun transactionDetails(transactionId: Int) = "transaction_details/$transactionId"

    fun addEditTransactionWithId(transactionId: Int): String {
        return "$ADD_EDIT_TRANSACTION_ROUTE?transactionId=$transactionId"
    }
}
