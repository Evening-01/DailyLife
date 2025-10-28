package com.evening.dailylife.core.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector
import com.evening.dailylife.R
import java.util.Locale

enum class TransactionCategoryType(
    val id: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val flows: Set<CategoryFlow>
) {
    FOOD(
        id = "food",
        labelRes = R.string.category_food,
        icon = Icons.Default.Restaurant,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    TRANSPORT(
        id = "transport",
        labelRes = R.string.category_transport,
        icon = Icons.Default.Commute,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    SHOPPING(
        id = "shopping",
        labelRes = R.string.category_shopping,
        icon = Icons.Default.ShoppingCart,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    DAILY_USE(
        id = "daily_use",
        labelRes = R.string.category_daily_use,
        icon = Icons.Default.LocalConvenienceStore,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    VEGETABLE(
        id = "vegetable",
        labelRes = R.string.category_vegetable,
        icon = Icons.Default.LocalFlorist,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    FRUIT(
        id = "fruit",
        labelRes = R.string.category_fruit,
        icon = Icons.Default.EnergySavingsLeaf,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    SNACK(
        id = "snack",
        labelRes = R.string.category_snack,
        icon = Icons.Default.Icecream,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    COMMUNICATION(
        id = "communication",
        labelRes = R.string.category_communication,
        icon = Icons.Default.Phone,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    DELIVERY(
        id = "delivery",
        labelRes = R.string.category_delivery,
        icon = Icons.Default.LocalShipping,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    HOUSING(
        id = "housing",
        labelRes = R.string.category_housing,
        icon = Icons.Default.Home,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    HOME(
        id = "home",
        labelRes = R.string.category_home,
        icon = Icons.Default.Deck,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    ENTERTAINMENT(
        id = "entertainment",
        labelRes = R.string.category_entertainment,
        icon = Icons.Default.SportsEsports,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    MOVIE(
        id = "movie",
        labelRes = R.string.category_movie,
        icon = Icons.Default.Movie,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    SPORT(
        id = "sport",
        labelRes = R.string.category_sport,
        icon = Icons.Default.FitnessCenter,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    TRAVEL(
        id = "travel",
        labelRes = R.string.category_travel,
        icon = Icons.Default.Luggage,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    TOBACCO_ALCOHOL(
        id = "tobacco_alcohol",
        labelRes = R.string.category_tobacco_alcohol,
        icon = Icons.Default.LocalBar,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    CLOTHING(
        id = "clothing",
        labelRes = R.string.category_clothing,
        icon = Icons.Default.Checkroom,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    BEAUTY(
        id = "beauty",
        labelRes = R.string.category_beauty,
        icon = Icons.Default.ContentCut,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    LEARNING(
        id = "learning",
        labelRes = R.string.category_learning,
        icon = Icons.Default.School,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    BOOKS(
        id = "books",
        labelRes = R.string.category_books,
        icon = Icons.AutoMirrored.Filled.MenuBook,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    CHILDREN(
        id = "children",
        labelRes = R.string.category_children,
        icon = Icons.Default.ChildCare,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    ELDER(
        id = "elder",
        labelRes = R.string.category_elder,
        icon = Icons.Default.Elderly,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    PET(
        id = "pet",
        labelRes = R.string.category_pet,
        icon = Icons.Default.Pets,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    SOCIAL(
        id = "social",
        labelRes = R.string.category_social,
        icon = Icons.Default.People,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    FAMILY(
        id = "family",
        labelRes = R.string.category_family,
        icon = Icons.Default.FamilyRestroom,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    GIFT(
        id = "gift",
        labelRes = R.string.category_gift,
        icon = Icons.Default.CardGiftcard,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    CASH_GIFT(
        id = "cash_gift",
        labelRes = R.string.category_cash_gift,
        icon = Icons.Default.Payments,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    MEDICAL(
        id = "medical",
        labelRes = R.string.category_medical,
        icon = Icons.Default.LocalHospital,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    OFFICE(
        id = "office",
        labelRes = R.string.category_office,
        icon = Icons.Default.BusinessCenter,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    DIGITAL(
        id = "digital",
        labelRes = R.string.category_digital,
        icon = Icons.Default.Computer,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    VEHICLE(
        id = "vehicle",
        labelRes = R.string.category_vehicle,
        icon = Icons.Default.DirectionsCar,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    REPAIR(
        id = "repair",
        labelRes = R.string.category_repair,
        icon = Icons.Default.Build,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    DONATION(
        id = "donation",
        labelRes = R.string.category_donation,
        icon = Icons.Default.VolunteerActivism,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    LOTTERY(
        id = "lottery",
        labelRes = R.string.category_lottery,
        icon = Icons.Default.ConfirmationNumber,
        flows = setOf(CategoryFlow.EXPENSE),
    ),
    OTHERS(
        id = "others",
        labelRes = R.string.category_others,
        icon = Icons.Default.MoreHoriz,
        flows = setOf(CategoryFlow.EXPENSE, CategoryFlow.INCOME),
    ),
    SALARY(
        id = "salary",
        labelRes = R.string.category_salary,
        icon = Icons.Default.MonetizationOn,
        flows = setOf(CategoryFlow.INCOME),
    ),
    PART_TIME(
        id = "part_time",
        labelRes = R.string.category_part_time,
        icon = Icons.Default.AddCard,
        flows = setOf(CategoryFlow.INCOME),
    ),
    FINANCE(
        id = "finance",
        labelRes = R.string.category_finance,
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        flows = setOf(CategoryFlow.INCOME),
    ),
    CASH_GIFT_INCOME(
        id = "cash_gift_income",
        labelRes = R.string.category_cash_gift_income,
        icon = Icons.Default.Redeem,
        flows = setOf(CategoryFlow.INCOME),
    );

    companion object {
        fun fromValue(rawValue: String): TransactionCategoryType? {
            val normalized = rawValue.trim().lowercase(Locale.ROOT)
            return entries.firstOrNull { type -> normalized == type.id }
        }
    }
}

enum class CategoryFlow {
    EXPENSE,
    INCOME
}
