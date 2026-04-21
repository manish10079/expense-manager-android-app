package com.mkn0079.expensetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

val bottomNavBarItems = listOf(
    BottomNavBarItem(AppRoute.Home, Icons.Default.Home, "Home"),
    BottomNavBarItem(AppRoute.Analytics, Icons.Default.Analytics, "Analytics"),
    BottomNavBarItem(AppRoute.Budget, Icons.Default.AccountBalanceWallet, "Budget"),
    BottomNavBarItem(AppRoute.Calendar, Icons.Default.CalendarMonth, "Calendar")
)
