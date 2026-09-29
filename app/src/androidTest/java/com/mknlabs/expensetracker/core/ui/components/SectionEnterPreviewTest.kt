package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pins why the section-enter fade blanked every screen preview.
 *
 * The helper starts each section at alpha 0 and only fades it in once the whole process has
 * reached STARTED. A `@Preview` host never dispatches that event, so the fade never ran and
 * the sections stayed at 0 — the screen composed, laid out, and drew absolutely nothing.
 * Nothing errored, which is why the preview was simply blank.
 *
 * Note this was never the *first* thing wrong with the Add Transaction preview: a
 * `NoClassDefFoundError: android/speech/SpeechRecognizer` killed that preview before it even
 * composed. Both are needed for it to render, and both are covered here rather than by eye.
 *
 * Method names are camelCase rather than backticked sentences on purpose: instrumented tests
 * are dexed and this app's DEX version rejects spaces in method names.
 */
@RunWith(AndroidJUnit4::class)
class SectionEnterPreviewTest {

    @get:Rule
    val compose = createComposeRule()

    private class TestOwner(startState: Lifecycle.State) : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry

        init {
            registry.currentState = startState
        }
    }

    /**
     * The owner is built inside `remember`, so the lifecycle registry is created and moved on
     * the composition (main) thread — `LifecycleRegistry` refuses to be touched anywhere else.
     */
    private fun alphasFor(startState: Lifecycle.State, inPreview: Boolean): List<Float> {
        var alphas: List<Float>? = null
        compose.setContent {
            val owner = remember { TestOwner(startState) }
            CompositionLocalProvider(LocalInspectionMode provides inPreview) {
                alphas = rememberSectionEnterAlphas(count = 3, lifecycleOwner = owner)
            }
        }
        compose.waitForIdle()
        return requireNotNull(alphas) { "helper never produced alphas" }
    }

    /** The state a `@Preview` host is in: created, and never started. */
    @Test
    fun previewHostShowsEverySectionFullyVisible() {
        assertEquals(
            "A preview must not be married to a process lifecycle it can never receive",
            listOf(1f, 1f, 1f),
            alphasFor(startState = Lifecycle.State.CREATED, inPreview = true)
        )
    }

    /** The same lifecycle without the preview flag — the fade is still armed, and still 0. */
    @Test
    fun processThatNeverStartedHoldsSectionsHidden() {
        assertEquals(
            listOf(0f, 0f, 0f),
            alphasFor(startState = Lifecycle.State.CREATED, inPreview = false)
        )
    }

    /**
     * Pins that the fade belongs to the screen's own entrance, and is never replayed.
     *
     * Every trip back from the background used to reset the sections and fade them in again.
     * That reset could only land a frame or two after the window was already back on screen,
     * so the finished screen flashed first and faded second. A return should not animate at
     * all — the sections are simply still there.
     *
     * The clock is paused so the states either side of the lifecycle bump are things we can
     * look at, rather than a window we hope to catch.
     */
    @Test
    fun returningToForegroundDoesNotReplayTheEntrance() {
        compose.mainClock.autoAdvance = false
        lateinit var owner: TestOwner
        var alphas: List<Float>? = null
        compose.setContent {
            owner = remember { TestOwner(Lifecycle.State.STARTED) }
            alphas = rememberSectionEnterAlphas(count = 2, lifecycleOwner = owner)
        }
        compose.mainClock.advanceTimeByFrame()
        assertTrue(
            "The entrance must still be in flight a frame in: ${requireNotNull(alphas)}",
            requireNotNull(alphas).all { it < 0.5f }
        )
        compose.mainClock.advanceTimeBy(SectionEnterDurationMs + SectionEnterStaggerMs + 200L)
        assertEquals(
            "The entrance should have finished by now",
            listOf(1f, 1f),
            requireNotNull(alphas)
        )

        compose.runOnUiThread { owner.registry.currentState = Lifecycle.State.CREATED }
        compose.mainClock.advanceTimeByFrame()
        assertEquals(
            "A backgrounded screen keeps the sections it already faded in",
            listOf(1f, 1f),
            requireNotNull(alphas)
        )

        compose.runOnUiThread { owner.registry.currentState = Lifecycle.State.STARTED }
        // Sampled frame by frame rather than at one instant: a replay first hides the sections
        // again and then fades them back in, and how soon after the lifecycle bump that reaches
        // the screen is not something worth racing. The dimmest frame is the whole story.
        var dimmest = 1f
        repeat(40) {
            compose.mainClock.advanceTimeByFrame()
            dimmest = minOf(dimmest, requireNotNull(alphas).min())
        }
        assertEquals(
            "Coming back from the background must not fade the sections in again",
            1f,
            dimmest
        )
    }
}
