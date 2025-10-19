package com.evening.dailylife.core.model

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Deck
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalConvenienceStore
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.VolunteerActivism
import com.evening.dailylife.R

object TransactionCategoryRepository {

    fun getExpenseCategories(context: Context): List<TransactionCategory> = listOf(
        // --- 高频日常 ---
        TransactionCategory(context.getString(R.string.category_food), Icons.Default.Restaurant),
        TransactionCategory(context.getString(R.string.category_transport), Icons.Default.Commute),
        TransactionCategory(context.getString(R.string.category_shopping), Icons.Default.ShoppingCart),
        TransactionCategory(context.getString(R.string.category_daily_use), Icons.Default.LocalConvenienceStore),
        TransactionCategory(context.getString(R.string.category_vegetable), Icons.Default.LocalFlorist),
        TransactionCategory(context.getString(R.string.category_fruit), Icons.Default.EnergySavingsLeaf),
        TransactionCategory(context.getString(R.string.category_snack), Icons.Default.Icecream),
        TransactionCategory(context.getString(R.string.category_communication), Icons.Default.Phone),
        TransactionCategory(context.getString(R.string.category_delivery), Icons.Default.LocalShipping),

        // --- 住房居家 ---
        TransactionCategory(context.getString(R.string.category_housing), Icons.Default.Home),
        TransactionCategory(context.getString(R.string.category_home), Icons.Default.Deck),

        // --- 休闲娱乐 ---
        TransactionCategory(context.getString(R.string.category_entertainment), Icons.Default.SportsEsports),
        TransactionCategory(context.getString(R.string.category_movie), Icons.Default.Movie),
        TransactionCategory(context.getString(R.string.category_sport), Icons.Default.FitnessCenter),
        TransactionCategory(context.getString(R.string.category_travel), Icons.Default.Luggage),
        TransactionCategory(context.getString(R.string.category_tobacco_alcohol), Icons.Default.LocalBar),

        // --- 个人提升与形象 ---
        TransactionCategory(context.getString(R.string.category_clothing), Icons.Default.Checkroom),
        TransactionCategory(context.getString(R.string.category_beauty), Icons.Default.ContentCut),
        TransactionCategory(context.getString(R.string.category_learning), Icons.Default.School),
        TransactionCategory(context.getString(R.string.category_books), Icons.AutoMirrored.Filled.MenuBook),

        // --- 家庭与社交 ---
        TransactionCategory(context.getString(R.string.category_children), Icons.Default.ChildCare),
        TransactionCategory(context.getString(R.string.category_elder), Icons.Default.Elderly),
        TransactionCategory(context.getString(R.string.category_pet), Icons.Default.Pets),
        TransactionCategory(context.getString(R.string.category_social), Icons.Default.People),
        TransactionCategory(context.getString(R.string.category_family), Icons.Default.FamilyRestroom),
        TransactionCategory(context.getString(R.string.category_gift), Icons.Default.CardGiftcard),
        TransactionCategory(context.getString(R.string.category_cash_gift), Icons.Default.Payments),

        // --- 医疗与办公 ---
        TransactionCategory(context.getString(R.string.category_medical), Icons.Default.LocalHospital),
        TransactionCategory(context.getString(R.string.category_office), Icons.Default.BusinessCenter),

        // --- 资产与维修 ---
        TransactionCategory(context.getString(R.string.category_digital), Icons.Default.Computer),
        TransactionCategory(context.getString(R.string.category_vehicle), Icons.Default.DirectionsCar),
        TransactionCategory(context.getString(R.string.category_repair), Icons.Default.Build),

        // --- 其他 ---
        TransactionCategory(context.getString(R.string.category_donation), Icons.Default.VolunteerActivism),
        TransactionCategory(context.getString(R.string.category_lottery), Icons.Default.ConfirmationNumber),
        TransactionCategory(context.getString(R.string.category_others), Icons.Default.MoreHoriz)
    )

    fun getIncomeCategories(context: Context): List<TransactionCategory> = listOf(
        TransactionCategory(context.getString(R.string.category_salary), Icons.Default.MonetizationOn),
        TransactionCategory(context.getString(R.string.category_part_time), Icons.Default.AddCard),
        TransactionCategory(context.getString(R.string.category_finance), Icons.AutoMirrored.Filled.TrendingUp),
        TransactionCategory(context.getString(R.string.category_cash_gift_income), Icons.Default.Redeem),
        TransactionCategory(context.getString(R.string.category_others), Icons.Default.MoreHoriz)
    )

    fun getIcon(context: Context, categoryName: String) =
        (getExpenseCategories(context) + getIncomeCategories(context))
            .firstOrNull { it.name == categoryName }
            ?.icon ?: Icons.Default.MoreHoriz
}
