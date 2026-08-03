package com.mknlabs.expensetracker.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsLearningStoreTest {

    private fun newStore() = SmsLearningStore(FakePreferencesDataStore())

    @Test
    fun observeOverrides_isEmptyInitially() = runTest {
        assertTrue(newStore().observeOverrides().first().isEmpty())
    }

    @Test
    fun setOverride_roundTrips_withNormalizedMerchant() = runTest {
        val store = newStore()

        store.setOverride("  Swiggy ", 1)

        assertEquals(mapOf("swiggy" to 1), store.observeOverrides().first())
    }

    @Test
    fun setOverride_multipleMerchants_preservesAll() = runTest {
        val store = newStore()
        store.setOverride("swiggy", 1)
        store.setOverride("zomato", 22)

        assertEquals(mapOf("swiggy" to 1, "zomato" to 22), store.observeOverrides().first())
    }

    @Test
    fun setOverride_overwritesExistingMapping() = runTest {
        val store = newStore()
        store.setOverride("swiggy", 1)
        store.setOverride("swiggy", 22)

        assertEquals(mapOf("swiggy" to 22), store.observeOverrides().first())
    }

    @Test
    fun setOverride_ignoresBlankMerchant() = runTest {
        val store = newStore()

        store.setOverride("   ", 1)

        assertTrue(store.observeOverrides().first().isEmpty())
    }

    @Test
    fun removeOverride_deletesOnlyThatMerchant() = runTest {
        val store = newStore()
        store.setOverride("swiggy", 1)
        store.setOverride("zomato", 22)

        store.removeOverride("SWIGGY")

        assertEquals(mapOf("zomato" to 22), store.observeOverrides().first())
    }

    @Test
    fun removeOverride_isNoOp_whenNotPresent() = runTest {
        val store = newStore()
        store.setOverride("zomato", 22)

        store.removeOverride("swiggy")

        assertEquals(mapOf("zomato" to 22), store.observeOverrides().first())
    }
}
