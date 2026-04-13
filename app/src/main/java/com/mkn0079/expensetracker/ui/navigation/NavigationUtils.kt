package com.mkn0079.expensetracker.ui.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import com.mkn0079.expensetracker.ui.navigation.bottomNavBarItems

val primaryNavigationRoutes = setOf(
    "home",
    "analytics",
    "budget",
    "calendar",
    "transactions"
)

val bottomTabRoutes = bottomNavBarItems.map { it.route }

val routesRequiringFullTransactions = setOf(
    "analytics",
    "budget",
    "calendar",
    "transactions",
    "add_transaction"
)

val routesKeepingTransactionsWarm = routesRequiringFullTransactions + "home"

enum class AppLockFlow {
    Setup,
    Unlock
}

fun resolveBackNavigationRoute(
    currentRoute: String,
    profileOriginRoute: String,
    previousRoute: String
): String? {
    return when (currentRoute) {
        "analytics",
        "budget",
        "calendar",
        "transactions",
        "settings" -> "home"
        "preferences",
        "security_privacy",
        "transaction_card_customize",
        "category_management",
        "data_management",
        "about",
        "notification_settings" -> "settings"
        "profile" -> profileOriginRoute
        "add_transaction" -> previousRoute
        "itemized_calculator" -> "add_transaction"
        else -> null
    }
}

fun screenTransition(fromRoute: String, toRoute: String): ContentTransform {
    val duration = 300
    
    return fadeIn(animationSpec = tween(duration)) togetherWith
        fadeOut(animationSpec = tween(duration))
}
