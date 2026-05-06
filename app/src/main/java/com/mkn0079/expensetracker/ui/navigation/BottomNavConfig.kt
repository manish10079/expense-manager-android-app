package com.mkn0079.expensetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.rounded.Home

val bottomNavBarItems = listOf(
    BottomNavBarItem(AppRoute.Home, Icons.Outlined.Home, "Home"),
    BottomNavBarItem(AppRoute.Analytics, Icons.Outlined.Analytics, "Analytics"),
    BottomNavBarItem(AppRoute.Budget, Icons.Outlined.Wallet, "Budget"),
    BottomNavBarItem(AppRoute.Calendar, Icons.Outlined.Event, "Calendar")
)
