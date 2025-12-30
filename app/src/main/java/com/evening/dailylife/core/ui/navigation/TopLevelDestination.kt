package com.evening.dailylife.core.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class TopLevelDestination(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector,
)
