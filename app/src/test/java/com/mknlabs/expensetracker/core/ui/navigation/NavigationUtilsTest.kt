package com.mknlabs.expensetracker.core.ui.navigation

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
    fun `resolveBackNavigationRoute returns home for the detected sms inbox`() {
        // Opened from the Home bell, so Back must go Home. Left unmapped it falls into the
        // `else -> null` branch, which disables the back handler and closes the whole app.
        val backRoute = resolveBackNavigationRoute(
            currentRoute = AppRoute.DetectedSms,
            profileOriginRoute = AppRoute.Home,
            previousRoute = AppRoute.Home
        )

        assertEquals(AppRoute.Home, backRoute)
    }

    @Test
    fun `resolveBackNavigationRoute returns category management for the add category screen`() {
        // Create-a-category is opened from Category Management. Unmapped it hit the
        // `else -> null` branch, so system Back left the app instead of going back.
        val backRoute = resolveBackNavigationRoute(
            currentRoute = AppRoute.AddCategory,
            profileOriginRoute = AppRoute.Settings,
            previousRoute = AppRoute.CategoryManagement
        )

        assertEquals(AppRoute.CategoryManagement, backRoute)
    }

    @Test
    fun `every route except the home root has a back destination`() {
        // A route missing from the map resolves to null, which disables the scaffold's
        // back handler and sends the whole app to the background. Home is the one
        // deliberate exception: it is the root, so Back there leaves the app.
        val unmapped = AppRoute.entries.filter { route ->
            route != AppRoute.Home &&
                resolveBackNavigationRoute(
                    currentRoute = route,
                    profileOriginRoute = AppRoute.Settings,
                    previousRoute = AppRoute.Transactions
                ) == null
        }

        assertEquals("routes with no back destination", emptyList<AppRoute>(), unmapped)
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
