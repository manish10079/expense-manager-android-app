package com.mknlabs.expensetracker.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorHistoryStoreTest {

    private fun newStore() = CalculatorHistoryStore(FakePreferencesDataStore())

    @Test
    fun observeHistory_isEmptyInitially() = runTest {
        assertTrue(newStore().observeHistory().first().isEmpty())
    }

    @Test
    fun addEntry_prependsNewestFirst() = runTest {
        val store = newStore()

        store.addEntry("12 + 5", "17")
        store.addEntry("9 × 3", "27")

        val entries = store.observeHistory().first()
        assertEquals(2, entries.size)
        assertEquals("9 × 3", entries[0].expression)
        assertEquals("27", entries[0].result)
        assertEquals("12 + 5", entries[1].expression)
        assertEquals("17", entries[1].result)
    }

    @Test
    fun addEntry_roundTripsTimestamps() = runTest {
        val store = newStore()
        val before = System.currentTimeMillis()

        store.addEntry("12 + 5", "17")

        val entry = store.observeHistory().first().single()
        assertTrue(entry.timestampMillis >= before)
        assertTrue(entry.timestampMillis <= System.currentTimeMillis())
    }

    @Test
    fun addEntry_trimsOldestBeyondCap() = runTest {
        val store = newStore()

        repeat(55) { index ->
            store.addEntry("$index + 0", index.toString())
        }

        val entries = store.observeHistory().first()
        assertEquals(50, entries.size)
        // Newest (54) is kept, oldest (0..4) dropped.
        assertEquals("54 + 0", entries.first().expression)
        assertEquals("5 + 0", entries.last().expression)
    }

    @Test
    fun clearHistory_removesAllEntries() = runTest {
        val store = newStore()
        store.addEntry("12 + 5", "17")
        store.addEntry("9 × 3", "27")

        store.clearHistory()

        assertTrue(store.observeHistory().first().isEmpty())
    }
}
