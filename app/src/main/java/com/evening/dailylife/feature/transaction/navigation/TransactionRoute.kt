package com.evening.dailylife.feature.transaction.navigation

object TransactionRoute {
    private const val ADD_EDIT_TRANSACTION_ROUTE = "add_edit_transaction"
    const val ADD_EDIT_TRANSACTION_PREFIX = ADD_EDIT_TRANSACTION_ROUTE
    const val ADD_EDIT_TRANSACTION =
        "$ADD_EDIT_TRANSACTION_ROUTE?transactionId={transactionId}&categoryId={categoryId}&isExpense={isExpense}"

    private const val TRANSACTION_DETAILS_BASE = "transaction_details"
    const val TRANSACTION_DETAILS = "$TRANSACTION_DETAILS_BASE/{transactionId}"
    const val TRANSACTION_DETAILS_PREFIX = TRANSACTION_DETAILS_BASE

    fun transactionDetails(transactionId: Int) = "$TRANSACTION_DETAILS_BASE/$transactionId"

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
