package com.mknlabs.expensetracker.data.constants

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import com.mknlabs.expensetracker.utils.ExpenseTrackerIconRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins the one rule that keeps the icon picker honest: every key it offers, and every key the
 * seed data stores, must be a key [ExpenseTrackerIconRegistry] can actually draw.
 *
 * The picker and the registry used to keep separate vector tables, and nothing held them in
 * step. 49 options were offered while the registry had never heard of them, so choosing one
 * looked right in the picker and then drew a question mark in every list row. The picker
 * renders its own vector, which is precisely why the mismatch stayed invisible until after the
 * choice was already made and stored.
 */
class CategoryIconCatalogTest {

    @Test
    fun `every icon the picker offers can be drawn`() {
        val unrenderable = categoryIconOptions
            .map { it.id }
            .filter { ExpenseTrackerIconRegistry.iconForKey(it) == Icons.Filled.QuestionMark }

        assertEquals(emptyList<String>(), unrenderable)
    }

    @Test
    fun `every seeded category and payment method can be drawn`() {
        val seeded = categoryMap.values.map { it.iconKey } +
            paymentTypeMap.values.map { it.iconKey }
        val unrenderable = seeded.filter {
            ExpenseTrackerIconRegistry.iconForKey(it) == Icons.Filled.QuestionMark
        }

        assertEquals(emptyList<String>(), unrenderable)
    }

    @Test
    fun `an unknown key still falls back to the question mark these tests detect`() {
        // The control for the two assertions above. Both compare against the fallback icon, so
        // if a key ever stopped resolving to it the tests would pass while checking nothing.
        assertEquals(
            Icons.Filled.QuestionMark,
            ExpenseTrackerIconRegistry.iconForKey("no_such_icon_key")
        )
    }
}
