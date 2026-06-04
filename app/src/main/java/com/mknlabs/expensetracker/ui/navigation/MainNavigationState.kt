package com.mknlabs.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mknlabs.expensetracker.models.Transaction

@Stable
class MainNavigationState(
    initialRoute: AppRoute = AppRoute.Home
) {
    var currentRoute by mutableStateOf(initialRoute)
        private set

    var previousRoute by mutableStateOf(AppRoute.Home)
        private set

    var profileOriginRoute by mutableStateOf(AppRoute.Home)
        private set

    var isBottomBarVisible by mutableStateOf(false)
        private set

    var selectedTransaction by mutableStateOf<Transaction?>(null)
        private set

    var addTransactionDraftAmount by mutableStateOf<String?>(null)
        private set

    var addTransactionDraftNote by mutableStateOf<String?>(null)
        private set

    fun navigateTo(route: AppRoute) {
        if (route == AppRoute.AddTransaction && currentRoute != AppRoute.ItemizedCalculator) {
            previousRoute = currentRoute
        }
        currentRoute = route
    }

    fun updateProfileOriginRoute(route: AppRoute) {
        profileOriginRoute = route
    }

    fun updateBottomBarVisibility(visible: Boolean) {
        isBottomBarVisible = visible
    }

    fun updateSelectedTransaction(transaction: Transaction?) {
        selectedTransaction = transaction
    }

    fun updateAddTransactionDraftAmount(amount: String?) {
        addTransactionDraftAmount = amount
    }

    fun updateAddTransactionDraftNote(note: String?) {
        addTransactionDraftNote = note
    }

    fun clearTransactionDraftContext() {
        selectedTransaction = null
        addTransactionDraftAmount = null
        addTransactionDraftNote = null
    }
}

@Composable
fun rememberMainNavigationState(
    initialRoute: AppRoute = AppRoute.Home
): MainNavigationState {
    return remember(initialRoute) {
        MainNavigationState(initialRoute = initialRoute)
    }
}
