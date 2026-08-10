package com.mknlabs.expensetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home

import com.mknlabs.expensetracker.R

val bottomNavBarItems = listOf(
    BottomNavBarItem(
        route = AppRoute.Home,
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home,
        titleRes = R.string.title_nav_home
    ),
    BottomNavBarItem(
        route = AppRoute.Analytics,
        icon = Icons.Outlined.Analytics,
        selectedIcon = Icons.Filled.Analytics,
        titleRes = R.string.title_nav_analytics
    ),
    BottomNavBarItem(
        route = AppRoute.Budget,
        icon = Icons.Outlined.AccountBalanceWallet,
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        titleRes = R.string.title_nav_budget
    ),
    BottomNavBarItem(
        route = AppRoute.Calendar,
        icon = Icons.Outlined.CalendarMonth,
        selectedIcon = Icons.Filled.CalendarMonth,
        titleRes = R.string.title_nav_calendar
    )
)
