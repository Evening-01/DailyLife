// file: com/evening/dailylife/ui/model/CategoryRepo.kt

package com.evening.dailylife.ui.model

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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector

data class TransactionCategory(
    val name: String,
    val icon: ImageVector
)

object CategoryRepo {
    val expenseCategories = listOf(
        TransactionCategory("餐饮", Icons.Default.Restaurant),
        TransactionCategory("购物", Icons.Default.ShoppingCart),
        TransactionCategory("日用", Icons.Default.LocalConvenienceStore),
        TransactionCategory("交通", Icons.Default.Commute),
        TransactionCategory("蔬菜", Icons.Default.LocalFlorist),
        TransactionCategory("水果", Icons.Default.EnergySavingsLeaf),
        TransactionCategory("零食", Icons.Default.Icecream),
        TransactionCategory("运动", Icons.Default.FitnessCenter),
        TransactionCategory("娱乐", Icons.Default.SportsEsports),
        TransactionCategory("通讯", Icons.Default.Phone),
        TransactionCategory("服饰", Icons.Default.Checkroom),
        TransactionCategory("美容", Icons.Default.ContentCut),
        TransactionCategory("住房", Icons.Default.Home),
        TransactionCategory("居家", Icons.Default.Deck),
        TransactionCategory("孩子", Icons.Default.ChildCare),
        TransactionCategory("长辈", Icons.Default.Elderly),
        TransactionCategory("社交", Icons.Default.People),
        TransactionCategory("旅行", Icons.Default.Luggage),
        TransactionCategory("烟酒", Icons.Default.LocalBar),
        TransactionCategory("数码", Icons.Default.Computer),
        TransactionCategory("汽车", Icons.Default.DirectionsCar),
        TransactionCategory("医疗", Icons.Default.LocalHospital),
        TransactionCategory("书籍", Icons.Default.MenuBook),
        TransactionCategory("学习", Icons.Default.School),
        TransactionCategory("宠物", Icons.Default.Pets),
        TransactionCategory("礼金", Icons.Default.Payments),
        TransactionCategory("礼物", Icons.Default.CardGiftcard),
        TransactionCategory("办公", Icons.Default.BusinessCenter),
        TransactionCategory("维修", Icons.Default.Build),
        TransactionCategory("捐赠", Icons.Default.VolunteerActivism),
        TransactionCategory("彩票", Icons.Default.ConfirmationNumber),
        TransactionCategory("亲友", Icons.Default.FamilyRestroom),
        TransactionCategory("快递", Icons.Default.LocalShipping),
        TransactionCategory("其他", Icons.Default.MoreHoriz)
        // *** 修改点: 从这里移除了 "设置" ***
    )

    val incomeCategories = listOf(
        TransactionCategory("工资", Icons.Default.MonetizationOn),
        TransactionCategory("兼职", Icons.Default.AddCard),
        TransactionCategory("理财", Icons.Default.TrendingUp),
        TransactionCategory("礼金", Icons.Default.Redeem),
        TransactionCategory("其他", Icons.Default.MoreHoriz)
    )

    private val allCategories = (expenseCategories + incomeCategories).associate { it.name to it.icon }

    fun getIcon(categoryName: String): ImageVector {
        return allCategories[categoryName] ?: Icons.Default.MoreHoriz
    }
}