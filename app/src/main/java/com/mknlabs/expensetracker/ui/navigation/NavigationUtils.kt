package com.mknlabs.expensetracker.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.togetherWith

val primaryNavigationRoutes = setOf(
    AppRoute.Home,
    AppRoute.Analytics,
    AppRoute.Budget,
    AppRoute.Calendar,
    AppRoute.Transactions
)

val bottomTabRoutes = bottomNavBarItems.map { it.route }

val routesRequiringFullTransactions = setOf(
    AppRoute.Analytics,
    AppRoute.Budget,
    AppRoute.Calendar,
    AppRoute.Transactions,
    AppRoute.AddTransaction
)

val routesKeepingTransactionsWarm = routesRequiringFullTransactions + AppRoute.Home

enum class AppLockFlow {
    Setup,
    Unlock
}

fun resolveBackNavigationRoute(
    currentRoute: AppRoute,
    profileOriginRoute: AppRoute,
    previousRoute: AppRoute
): AppRoute? {
    return when (currentRoute) {
        AppRoute.Analytics,
        AppRoute.Budget,
        AppRoute.Calendar,
        AppRoute.Transactions,
        AppRoute.Settings -> AppRoute.Home
        AppRoute.Preferences,
        AppRoute.SecurityPrivacy,
        AppRoute.TransactionCardCustomize,
        AppRoute.CategoryManagement,
        AppRoute.DataManagement,
        AppRoute.About,
        AppRoute.NotificationSettings,
        AppRoute.ConnectedDevices,
        AppRoute.MembershipDetails -> AppRoute.Settings
        AppRoute.Profile -> profileOriginRoute
        AppRoute.AddTransaction,
        AppRoute.Goals -> previousRoute
        AppRoute.ItemizedCalculator -> AppRoute.AddTransaction
        else -> null
    }
}

fun screenTransition(fromRoute: AppRoute, toRoute: AppRoute): ContentTransform {
    val duration = 400

    return fadeIn(
        animationSpec = tween(duration, easing = FastOutSlowInEasing)
    ) togetherWith fadeOut(
        animationSpec = tween(duration, easing = FastOutSlowInEasing)
    )
}
