package com.mknlabs.expensetracker.core.ui.navigation

import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.ChartDonut
import com.adamglin.phosphoricons.fill.Gear
import com.adamglin.phosphoricons.fill.House
import com.adamglin.phosphoricons.fill.Wallet
import com.adamglin.phosphoricons.regular.ChartDonut
import com.adamglin.phosphoricons.regular.Gear
import com.adamglin.phosphoricons.regular.House
import com.adamglin.phosphoricons.regular.Wallet
import com.mknlabs.expensetracker.R

val bottomNavBarItems = listOf(
    BottomNavBarItem(
        route = AppRoute.Home,
        icon = PhosphorIcons.Regular.House,
        selectedIcon = PhosphorIcons.Fill.House,
        titleRes = R.string.title_nav_home
    ),
    BottomNavBarItem(
        route = AppRoute.Analytics,
        icon = PhosphorIcons.Regular.ChartDonut,
        selectedIcon = PhosphorIcons.Fill.ChartDonut,
        titleRes = R.string.title_nav_analytics
    ),
    BottomNavBarItem(
        route = AppRoute.Budget,
        icon = PhosphorIcons.Regular.Wallet,
        selectedIcon = PhosphorIcons.Fill.Wallet,
        titleRes = R.string.title_nav_budget
    ),
    BottomNavBarItem(
        route = AppRoute.Settings,
        icon = PhosphorIcons.Regular.Gear,
        selectedIcon = PhosphorIcons.Fill.Gear,
        titleRes = R.string.title_settings
    )
)
