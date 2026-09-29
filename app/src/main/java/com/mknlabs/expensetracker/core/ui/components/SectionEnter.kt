package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** How long a single section takes to fade in. */
const val SectionEnterDurationMs = 160

/** Delay between the start of one section's fade and the next one's. */
const val SectionEnterStaggerMs = 90

val LocalLockOverlayActive = compositionLocalOf { false }

/**
 * Section-wise fade-in alphas, one per screen section: the sections start in order but
 * overlap, so a screen's whole entrance costs [SectionEnterDurationMs] plus one
 * [SectionEnterStaggerMs] per following section, instead of growing by a full
 * [SectionEnterDurationMs] per section.
 *
 * The fade belongs to the screen's own entrance and plays once. It waits for the first
 * moment the screen is actually visible — the app being in the foreground, and no lock
 * overlay covering it — because a screen composes before either of those is true. Returning
 * from the background is not an entrance: the sections are simply still there, faded in.
 * Every trip back used to replay the fade, which meant resetting them to invisible first,
 * and that reset can only land a frame or two after the window is back on screen — so the
 * finished screen flashed first and faded second.
 */
@Composable
fun rememberSectionEnterAlphas(
    count: Int,
    lifecycleOwner: LifecycleOwner = ProcessLifecycleOwner.get(),
): List<Float> {
    // A @Preview host has no process lifecycle that ever reaches STARTED, so the fade below
    // would never run and every section would stay at its initial alpha of 0 — an invisible,
    // blank preview. Render the sections fully visible at design time instead.
    if (LocalInspectionMode.current) {
        return remember(count) { List(count) { 1f } }
    }

    val lockOverlayActive = LocalLockOverlayActive.current
    var inForeground by remember(lifecycleOwner) {
        mutableStateOf(
            lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        )
    }
    var entrancePlayed by remember { mutableStateOf(false) }
    val anims = remember(count) { List(count) { Animatable(0f) } }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> inForeground = true
                Lifecycle.Event.ON_STOP -> inForeground = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(inForeground, lockOverlayActive, count) {
        if (inForeground && !lockOverlayActive && !entrancePlayed) {
            entrancePlayed = true
            anims.forEach { it.snapTo(0f) }
            coroutineScope {
                anims.forEachIndexed { index, anim ->
                    launch {
                        // The stagger rides on the animation clock's own delay rather than a
                        // coroutine delay, so a paused or slow frame clock keeps the sections
                        // in step with each other instead of leaking wall-clock time in.
                        anim.animateTo(
                            1f,
                            tween(
                                durationMillis = SectionEnterDurationMs,
                                delayMillis = index * SectionEnterStaggerMs,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }
                }
            }
        }
    }
    return anims.map { it.value }
}

fun Modifier.sectionEnter(alpha: Float): Modifier = this.alpha(alpha)
