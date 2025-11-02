package com.evening.dailylife.app.navigation

object Route {
    const val HOME = "home"
    const val DETAILS = "details"
    const val CHART = "chart"
    const val DISCOVER = "discover"
    const val ME = "me"
    const val ABOUT_AUTHOR = "about_author"
    const val GENERAL_SETTINGS = "general_settings"
    const val QUICK_USAGE = "quick_usage"
    const val MORTGAGE_CALCULATOR = "mortgage_calculator"
    const val CURRENCY_CONVERTER = "currency_converter"

    private const val ADD_EDIT_TRANSACTION_ROUTE = "add_edit_transaction"
    const val ADD_EDIT_TRANSACTION =
        "$ADD_EDIT_TRANSACTION_ROUTE?transactionId={transactionId}&categoryId={categoryId}&isExpense={isExpense}"

    const val TRANSACTION_DETAILS = "transaction_details/{transactionId}"

    fun transactionDetails(transactionId: Int) = "transaction_details/$transactionId"

    fun addEditTransactionWithId(
        transactionId: Int,
        categoryId: String? = null,
        isExpense: Boolean? = null
    ): String {
        val builder = StringBuilder()
            .append("$ADD_EDIT_TRANSACTION_ROUTE?transactionId=$transactionId")
        categoryId?.let { value ->
            builder.append("&categoryId=$value")
        }
        isExpense?.let { value ->
            builder.append("&isExpense=$value")
        }
        return builder.toString()
    }

    fun addNewTransactionShortcut(
        categoryId: String?,
        isExpense: Boolean?
    ): String {
        return addEditTransactionWithId(
            transactionId = -1,
            categoryId = categoryId,
            isExpense = isExpense
        )
    }
}
