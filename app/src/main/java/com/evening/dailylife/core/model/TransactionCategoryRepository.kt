package com.evening.dailylife.core.model

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector

object TransactionCategoryRepository {

    private val expenseCategoryOrder = listOf(
        TransactionCategoryType.FOOD,
        TransactionCategoryType.TRANSPORT,
        TransactionCategoryType.SHOPPING,
        TransactionCategoryType.DAILY_USE,
        TransactionCategoryType.VEGETABLE,
        TransactionCategoryType.FRUIT,
        TransactionCategoryType.SNACK,
        TransactionCategoryType.COMMUNICATION,
        TransactionCategoryType.DELIVERY,
        TransactionCategoryType.HOUSING,
        TransactionCategoryType.HOME,
        TransactionCategoryType.ENTERTAINMENT,
        TransactionCategoryType.MOVIE,
        TransactionCategoryType.SPORT,
        TransactionCategoryType.TRAVEL,
        TransactionCategoryType.TOBACCO_ALCOHOL,
        TransactionCategoryType.CLOTHING,
        TransactionCategoryType.BEAUTY,
        TransactionCategoryType.LEARNING,
        TransactionCategoryType.BOOKS,
        TransactionCategoryType.CHILDREN,
        TransactionCategoryType.ELDER,
        TransactionCategoryType.PET,
        TransactionCategoryType.SOCIAL,
        TransactionCategoryType.FAMILY,
        TransactionCategoryType.GIFT,
        TransactionCategoryType.CASH_GIFT,
        TransactionCategoryType.MEDICAL,
        TransactionCategoryType.OFFICE,
        TransactionCategoryType.DIGITAL,
        TransactionCategoryType.VEHICLE,
        TransactionCategoryType.REPAIR,
        TransactionCategoryType.DONATION,
        TransactionCategoryType.LOTTERY,
        TransactionCategoryType.OTHERS,
    )

    private val incomeCategoryOrder = listOf(
        TransactionCategoryType.SALARY,
        TransactionCategoryType.PART_TIME,
        TransactionCategoryType.FINANCE,
        TransactionCategoryType.CASH_GIFT_INCOME,
        TransactionCategoryType.OTHERS,
    )

    fun getExpenseCategories(context: Context): List<TransactionCategory> =
        expenseCategoryOrder.map { it.toUiModel(context) }

    fun getIncomeCategories(context: Context): List<TransactionCategory> =
        incomeCategoryOrder.map { it.toUiModel(context) }

    fun getIcon(categoryValue: String): ImageVector =
        TransactionCategoryType.fromValue(categoryValue)?.icon ?: Icons.Default.MoreHoriz

    fun getIcon(context: Context, categoryValue: String): ImageVector =
        getIcon(categoryValue)

    fun getDisplayName(context: Context, categoryValue: String): String =
        TransactionCategoryType.fromValue(categoryValue)
            ?.let { type -> context.getString(type.labelRes) }
            ?: categoryValue

    fun normalizeCategoryId(rawValue: String): String =
        TransactionCategoryType.fromValue(rawValue)?.id ?: rawValue

    private fun TransactionCategoryType.toUiModel(context: Context): TransactionCategory {
        return TransactionCategory(
            id = id,
            name = context.getString(labelRes),
            icon = icon
        )
    }
}
