package com.mkn0079.expensetracker.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NavigationUtilsTest {

    @Test
    fun `fromRoute returns matching typed route`() {
        assertEquals(AppRoute.AddTransaction, AppRoute.fromRoute("add_transaction"))
        assertNull(AppRoute.fromRoute("unknown_route"))
    }

    @Test
    fun `resolveBackNavigationRoute returns profile origin for profile screen`() {
        val backRoute = resolveBackNavigationRoute(
            currentRoute = AppRoute.Profile,
            profileOriginRoute = AppRoute.Settings,
            previousRoute = AppRoute.Home
        )

        assertEquals(AppRoute.Settings, backRoute)
    }

    @Test
    fun `resolveBackNavigationRoute returns add transaction for calculator screen`() {
        val backRoute = resolveBackNavigationRoute(
            currentRoute = AppRoute.ItemizedCalculator,
            profileOriginRoute = AppRoute.Home,
            previousRoute = AppRoute.Transactions
        )

        assertEquals(AppRoute.AddTransaction, backRoute)
    }
}
