package com.mkn0079.expensetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.rounded.Home

import com.mkn0079.expensetracker.R

val bottomNavBarItems = listOf(
    BottomNavBarItem(AppRoute.Home, Icons.Outlined.Home, R.string.title_nav_home),
    BottomNavBarItem(AppRoute.Analytics, Icons.Outlined.Analytics, R.string.title_nav_analytics),
    BottomNavBarItem(AppRoute.Budget, Icons.Outlined.Wallet, R.string.title_nav_budget),
    BottomNavBarItem(AppRoute.Calendar, Icons.Outlined.Event, R.string.title_nav_calendar)
)
