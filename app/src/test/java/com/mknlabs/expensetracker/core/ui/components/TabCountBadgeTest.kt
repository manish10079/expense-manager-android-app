package com.mknlabs.expensetracker.core.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TabCountBadgeTest {

    @Test
    fun `pro user sees the count on the selected tab`() {
        assertEquals(3, tabBadgeCount(count = 3, isSelected = true, isProUser = true))
    }

    @Test
    fun `unselected tab never shows a count`() {
        assertNull(tabBadgeCount(count = 3, isSelected = false, isProUser = true))
    }

    @Test
    fun `free user never sees a count`() {
        assertNull(tabBadgeCount(count = 3, isSelected = true, isProUser = false))
        assertNull(tabBadgeCount(count = 3, isSelected = false, isProUser = false))
    }

    @Test
    fun `empty list shows no badge`() {
        assertNull(tabBadgeCount(count = 0, isSelected = true, isProUser = true))
        assertNull(tabBadgeCount(count = -1, isSelected = true, isProUser = true))
    }

    @Test
    fun `overflow is reported above 99`() {
        assertTrue(isTabBadgeOverflow(100))
        assertEquals(137, tabBadgeCount(count = 137, isSelected = true, isProUser = true))
    }
}
