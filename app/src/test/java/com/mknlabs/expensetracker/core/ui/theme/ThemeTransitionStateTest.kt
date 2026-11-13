package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTransitionStateTest {

    @Test
    fun initialState_isNotTransitioning() {
        val state = ThemeTransitionState()
        assertFalse(state.isTransitioning)
        assertNull(state.snapshot)
        assertEquals(Offset.Zero, state.origin)
    }

    @Test
    fun startTransition_ignoresNullBitmap() {
        val state = ThemeTransitionState()
        state.startTransition(null, Offset(100f, 200f))
        assertFalse(state.isTransitioning)
        assertNull(state.snapshot)
        assertEquals(Offset.Zero, state.origin)
    }

    @Test
    fun endTransition_resetsState() {
        val state = ThemeTransitionState()
        state.endTransition()
        assertFalse(state.isTransitioning)
        assertNull(state.snapshot)
    }
}
