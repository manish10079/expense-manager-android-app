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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner

const val SectionEnterDurationMs = 250

val LocalLockOverlayActive = compositionLocalOf { false }

/**
 * Sequential fade-in alphas, one per screen section. Replays whenever the app
 * returns to the foreground (and the lock overlay is not covering the UI).
 */
@Composable
fun rememberSectionEnterAlphas(count: Int): List<Float> {
    val lockOverlayActive = LocalLockOverlayActive.current
    val anims = remember(count) { List(count) { Animatable(0f) } }
    val lifecycleOwner = remember { ProcessLifecycleOwner.get() }
    var inForeground by remember {
        mutableStateOf(
            lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        )
    }
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
        if (inForeground && !lockOverlayActive) {
            anims.forEach { it.snapTo(0f) }
            val spec = tween<Float>(
                durationMillis = SectionEnterDurationMs,
                easing = FastOutSlowInEasing
            )
            anims.forEach { it.animateTo(1f, spec) }
        }
    }
    return anims.map { it.value }
}

fun Modifier.sectionEnter(alpha: Float): Modifier = this.alpha(alpha)
