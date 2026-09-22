package com.mknlabs.expensetracker.core.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [tabBadgeCount] decides who sees a budget/recurring tab count. The badge itself is drawn by
 * [TabCountBadge] from Android resources, which a JVM unit test cannot read, so these tests pin
 * the plain-Kotlin half that carries the monetization rules:
 *
 * | count | selected | Pro   | badge |
 * |-------|----------|-------|-------|
 * | 3     | yes      | yes   | 3     |
 * | 3     | no       | yes   | none  |
 * | 3     | yes      | no    | none  |
 * | 0     | yes      | yes   | none  |
 */
class TabCountBadgeTest {

    @Test
    fun `selected tab shows its count to a pro user`() {
        assertEquals(3, tabBadgeCount(count = 3, isSelected = true, isProUser = true))
    }

    @Test
    fun `unselected tab never shows a count`() {
        assertNull(
            "the other tab's count describes content the user cannot see from here",
            tabBadgeCount(count = 3, isSelected = false, isProUser = true)
        )
    }

    @Test
    fun `free users never see a count`() {
        assertNull(tabBadgeCount(count = 3, isSelected = true, isProUser = false))
        assertNull(tabBadgeCount(count = 3, isSelected = false, isProUser = false))
    }

    @Test
    fun `an empty tab shows nothing instead of a zero badge`() {
        assertNull(tabBadgeCount(count = 0, isSelected = true, isProUser = true))
    }

    @Test
    fun `a negative count can never render a badge`() {
        assertNull(tabBadgeCount(count = -1, isSelected = true, isProUser = true))
    }

    @Test
    fun `only the selected tab resolves a badge`() {
        val counts = mapOf("budgets" to 5, "recurring" to 3)

        val visible = counts.mapValues { (_, count) ->
            tabBadgeCount(count = count, isSelected = true, isProUser = true)
        }

        assertEquals(mapOf("budgets" to 5, "recurring" to 3), visible)

        val hidden = counts.mapValues { (_, count) ->
            tabBadgeCount(count = count, isSelected = false, isProUser = true)
        }

        assertTrue("nothing may render on the tab that is not selected", hidden.values.all { it == null })
    }

    @Test
    fun `the real count is preserved so screen readers read the true number`() {
        assertEquals(137, tabBadgeCount(count = 137, isSelected = true, isProUser = true))
    }

    @Test
    fun `counts above the cap render as overflow`() {
        assertTrue(isTabBadgeOverflow(MAX_TAB_BADGE_COUNT + 1))
        assertFalse("the cap itself still renders in full", isTabBadgeOverflow(MAX_TAB_BADGE_COUNT))
        assertFalse(isTabBadgeOverflow(1))
    }
}
