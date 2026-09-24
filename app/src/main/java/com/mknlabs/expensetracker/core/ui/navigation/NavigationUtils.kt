package com.mknlabs.expensetracker.core.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
        AppRoute.Settings,
        // The detected-SMS inbox is opened from the Home bell, so system Back must land back on
        // Home rather than fall through to `else` and close the app.
        AppRoute.DetectedSms -> AppRoute.Home
        AppRoute.Preferences,
        AppRoute.SecurityPrivacy,
        AppRoute.TransactionCardCustomize,
        AppRoute.CategoryManagement,
        AppRoute.DataManagement,
        AppRoute.About,
        AppRoute.NotificationSettings,
        AppRoute.ConnectedDevices,
        AppRoute.MembershipDetails -> AppRoute.Settings
        // Create-a-category is opened from Category Management, so Back returns to
        // the screen it came from. Left unmapped it fell into `else -> null`, which
        // disables the scaffold's back handler and sends the app to the background.
        AppRoute.AddCategory -> AppRoute.CategoryManagement
        // Feedback is reached from About, and returns there whether Back is handled
        // by its own handler or by this map.
        AppRoute.Feedback -> AppRoute.About
        AppRoute.Profile -> profileOriginRoute
        AppRoute.AddTransaction,
        AppRoute.Goals -> previousRoute
        // The paywall is opened from upsells all over the app (gated actions, settings
        // rows, the transaction editor), so Back returns to whichever screen opened it.
        // Unmapped, this fell into `else -> null`, which disables the scaffold's back
        // handler and sends the app to the background instead of the previous screen.
        AppRoute.Paywall -> previousRoute
        AppRoute.ItemizedCalculator -> AppRoute.AddTransaction
        else -> null
    }
}

fun isBottomTabSwitch(fromRoute: AppRoute, toRoute: AppRoute): Boolean =
    fromRoute in bottomTabRoutes && toRoute in bottomTabRoutes

fun screenTransition(fromRoute: AppRoute, toRoute: AppRoute): ContentTransform {
    // Switching between bottom navigation tabs should be instant and snappy.
    if (isBottomTabSwitch(fromRoute, toRoute)) {
        return EnterTransition.None togetherWith ExitTransition.None
    }

    val duration = 400

    return fadeIn(
        animationSpec = tween(duration, easing = FastOutSlowInEasing)
    ) togetherWith fadeOut(
        animationSpec = tween(duration, easing = FastOutSlowInEasing)
    )
}
