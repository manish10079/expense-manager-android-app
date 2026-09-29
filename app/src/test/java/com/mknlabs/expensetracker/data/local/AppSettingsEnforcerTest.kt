package com.mknlabs.expensetracker.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins the reset rule the premium enforcer in [AppSettingsDataStore.updateAppSettings] runs on
 * every premium-only setting.
 *
 * Two cases carry the weight. A value this call did not touch is stale and must be cleared —
 * that is the cleanup the block exists for, so a lapsed Pro does not keep a blurred recents
 * preview or a cloud toggle nobody is entitled to. A value this call just wrote is a decision
 * made by the same transaction, and reverting it there silently undoes the user's action: the
 * cloud-sync switch was recorded as on and forced straight back to off in one write, which is
 * how a paying subscriber read as "the toggle does not turn on".
 */
class AppSettingsEnforcerTest {

    @Test
    fun `a value this call left alone is reset when the user is not premium`() {
        assertEquals(false, resetUnlessSet(current = true, updated = true, reset = false))
        assertEquals(1, resetUnlessSet(current = 20, updated = 20, reset = 1))
        assertEquals(false, resetUnlessSet(current = false, updated = false, reset = false))
    }

    @Test
    fun `a value this call set is never reverted by the enforcer`() {
        assertEquals(true, resetUnlessSet(current = false, updated = true, reset = false))
        assertEquals(true, resetUnlessSet(current = true, updated = true, reset = true))
    }

    @Test
    fun `an explicitly cleared value stays cleared`() {
        assertEquals(false, resetUnlessSet(current = true, updated = false, reset = false))
    }

    @Test
    fun `the reset value wins only where the caller never spoke`() {
        // The stale path still cleans up: an old invalid timeout is untouched by the call and
        // therefore eligible for the normalised value.
        assertEquals(1, resetUnlessSet(current = 20, updated = 20, reset = 1))
        // A call that wrote the value itself keeps it, valid or not, because the caller spoke.
        assertEquals(30, resetUnlessSet(current = 20, updated = 30, reset = 1))
    }
}
