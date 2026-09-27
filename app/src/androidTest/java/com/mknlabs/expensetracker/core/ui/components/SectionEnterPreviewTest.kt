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
}
