package com.mknlabs.expensetracker.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `isBottomTabSwitch returns true when switching between bottom navigation tabs`() {
        assertTrue(isBottomTabSwitch(AppRoute.Home, AppRoute.Analytics))
        assertTrue(isBottomTabSwitch(AppRoute.Analytics, AppRoute.Budget))
        assertTrue(isBottomTabSwitch(AppRoute.Calendar, AppRoute.Home))
    }

    @Test
    fun `isBottomTabSwitch returns false when leaving bottom navigation tabs`() {
        assertFalse(isBottomTabSwitch(AppRoute.Home, AppRoute.Settings))
        assertFalse(isBottomTabSwitch(AppRoute.Budget, AppRoute.Transactions))
    }

    @Test
    fun `isBottomTabSwitch returns false when entering bottom navigation tabs`() {
        assertFalse(isBottomTabSwitch(AppRoute.Settings, AppRoute.Home))
        assertFalse(isBottomTabSwitch(AppRoute.AddTransaction, AppRoute.Home))
    }
}
