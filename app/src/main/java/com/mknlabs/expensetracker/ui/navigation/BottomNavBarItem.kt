package com.mknlabs.expensetracker.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavBarItem(
    val route: AppRoute,
    val icon: ImageVector,
    @StringRes val titleRes: Int
)
