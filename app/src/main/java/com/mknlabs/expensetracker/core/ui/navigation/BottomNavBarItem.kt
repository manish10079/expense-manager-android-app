package com.mknlabs.expensetracker.core.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavBarItem(
    val route: AppRoute,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    @StringRes val titleRes: Int
)
