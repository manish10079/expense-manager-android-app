package com.mknlabs.expensetracker.core.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class MainNavigationStateTest {

    @Test
    fun calculatorRoundTripKeepsAddTransactionOrigin() {
        val state = MainNavigationState()
        state.navigateTo(AppRoute.Transactions)
        state.navigateTo(AppRoute.AddTransaction)
        assertEquals(AppRoute.Transactions, state.addTransactionOriginRoute)

        state.navigateTo(AppRoute.ItemizedCalculator)
        state.navigateTo(AppRoute.AddTransaction)

        assertEquals(AppRoute.Transactions, state.addTransactionOriginRoute)
        assertEquals(
            AppRoute.Transactions,
            addTransactionExitRoute(state.addTransactionOriginRoute, state.previousRoute)
        )
    }
}
