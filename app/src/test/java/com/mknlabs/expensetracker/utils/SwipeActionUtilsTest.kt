package com.mknlabs.expensetracker.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class SwipeActionUtilsTest {

    @Test
    fun `left swipe maps to duplicate`() {
        assertEquals(TransactionSwipeAction.Duplicate, transactionSwipeAction(isLeftSwipe = true))
    }

    @Test
    fun `right swipe maps to delete`() {
        assertEquals(TransactionSwipeAction.Delete, transactionSwipeAction(isLeftSwipe = false))
    }

    @Test
    fun `both directions map to distinct actions`() {
        val actions = setOf(
            transactionSwipeAction(isLeftSwipe = true),
            transactionSwipeAction(isLeftSwipe = false)
        )
        assertEquals(
            setOf(TransactionSwipeAction.Duplicate, TransactionSwipeAction.Delete),
            actions
        )
    }

    @Test
    fun `mapping is stable across repeated calls`() {
        repeat(10) {
            assertEquals(TransactionSwipeAction.Duplicate, transactionSwipeAction(isLeftSwipe = true))
            assertEquals(TransactionSwipeAction.Delete, transactionSwipeAction(isLeftSwipe = false))
        }
    }
}
