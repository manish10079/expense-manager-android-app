package com.mkn0079.expensetracker.ui.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*


val bottomNavBarItems= listOf(
    BottomNavBarItem("home", Icons.Default.Home, "Home"),
    BottomNavBarItem("analytics", Icons.Default.Analytics, "Analytics"),
    BottomNavBarItem("budget", Icons.Default.AccountBalanceWallet, "Budget"),
    BottomNavBarItem("calendar", Icons.Default.CalendarMonth, "Calendar")
)
