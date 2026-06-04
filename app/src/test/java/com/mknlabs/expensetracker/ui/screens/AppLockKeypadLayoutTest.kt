package com.mknlabs.expensetracker.ui.screens

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLockKeypadLayoutTest {

    @Test
    fun fixedLayout_preservesStandardKeyPositions() {
        val layout = buildAppLockKeypadLayout(scrambled = false)

        assertEquals(listOf("1", "2", "3"), layout[0])
        assertEquals(listOf("4", "5", "6"), layout[1])
        assertEquals(listOf("7", "8", "9"), layout[2])
        assertEquals(listOf(APP_LOCK_FORGOT_KEY, "0", APP_LOCK_DELETE_KEY), layout[3])
    }

    @Test
    fun scrambledLayout_containsEachDigitExactlyOnce_andKeepsFixedActionKeys() {
        val layout = buildAppLockKeypadLayout(
            scrambled = true,
            random = Random(42)
        )

        val flattenedKeys = layout.flatten()
        val digits = flattenedKeys.filter { it.all(Char::isDigit) }

        assertEquals(APP_LOCK_FORGOT_KEY, layout[3][0])
        assertEquals(APP_LOCK_DELETE_KEY, layout[3][2])
        assertEquals(10, digits.size)
        assertEquals((0..9).map(Int::toString).toSet(), digits.toSet())
        assertTrue(flattenedKeys.containsAll(listOf(APP_LOCK_FORGOT_KEY, APP_LOCK_DELETE_KEY)))
    }
}
