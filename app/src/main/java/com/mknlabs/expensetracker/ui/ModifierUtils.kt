package com.mknlabs.expensetracker.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlin.math.abs

/**
 * How much further sideways than up-and-down a gesture has to move before it is treated as
 * a horizontal swipe rather than a scroll.
 *
 * `1f` accepts anything that is even slightly more sideways than vertical, which is what
 * lets a fast vertical fling — where the finger almost always drifts a little sideways —
 * occasionally register as a swipe. A clear sideways lead is what tells a deliberate swipe
 * apart from a scroll that wobbles.
 *
 * **To make swiping stricter** (harder to trigger by accident), raise this — try `1.8f` or
 * `2f`. **To make it easier**, lower it towards `1f`. Values below `1f` start accepting
 * gestures that lead vertically, so the list's scrolling will feel unreliable.
 */
const val HORIZONTAL_SWIPE_AXIS_DOMINANCE = 1.5f

/**
 * Reusable modifier for handling horizontal swipe gestures across the UI layer.
 *
 * A gesture is claimed only once it has moved past the touch slop **and** has travelled
 * further sideways than vertically (see [axisDominance]). Until then the modifier stays out
 * of the way, so a vertical list keeps its scroll no matter how fast the flick is; once
 * claimed, every pointer movement is consumed so nothing scrolls underneath the finger.
 *
 * @param key Optional key to reset the gesture detection state (e.g., when content changes).
 * @param threshold The drag distance in pixels to trigger a swipe action.
 * @param flingVelocityThreshold Release velocity (px/s) that triggers a swipe action even when
 *   the drag distance is below [threshold]. 0f (default) disables velocity-based triggering.
 * @param axisDominance How much the opening movement has to lead sideways before the gesture
 *   becomes a swipe. See [HORIZONTAL_SWIPE_AXIS_DOMINANCE] to tune it.
 * @param onDragOffset Called with the current accumulated horizontal drag in px while the
 *   finger moves — useful to visually translate the target (e.g. a card following the swipe).
 * @param onThresholdCrossed Called exactly once per gesture, the first time the accumulated drag
 *   crosses [threshold] — useful for a subtle haptic confirmation at the commit point.
 * @param onSwipeLeft Callback triggered when a left swipe (negative horizontal drag) exceeds the threshold or fling velocity.
 * @param onSwipeRight Callback triggered when a right swipe (positive horizontal drag) exceeds the threshold or fling velocity.
 * @param onDragEnd Called when the pointer lifts after a drag, after any swipe callbacks fired.
 * @param onDragCancel Called when the gesture is cancelled before release (e.g. consumed elsewhere).
 */
fun Modifier.horizontalSwipe(
    key: Any? = null,
    threshold: Float = 80f,
    flingVelocityThreshold: Float = 0f,
    axisDominance: Float = HORIZONTAL_SWIPE_AXIS_DOMINANCE,
    onDragOffset: (Float) -> Unit = {},
    onThresholdCrossed: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {}
): Modifier = this.pointerInput(key) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val touchSlop = viewConfiguration.touchSlop
        val velocityTracker = VelocityTracker()
        // Tracked from the very first touch so a flick's speed is measured over the whole
        // gesture, not just the part that followed the slop.
        velocityTracker.addPosition(down.uptimeMillis, down.position)

        // ── Decide whether this gesture is ours ──────────────────────────────────
        // Wait until the slop has been passed in SOME direction, then look at which way the
        // finger has actually been travelling. A scroll — even a very fast one with sideways
        // wobble — leads vertically and is left alone, which is what stops a quick flick up
        // the list from being mistaken for a swipe.
        var travelledX = 0f
        var travelledY = 0f
        var claimed = false
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            // Lifted, or already taken by the scrolling parent: not a swipe.
            if (!change.pressed || change.isConsumed) break
            val delta = change.positionChange()
            travelledX += delta.x
            travelledY += delta.y
            if (abs(travelledX) >= touchSlop || abs(travelledY) >= touchSlop) {
                claimed = abs(travelledX) > abs(travelledY) * axisDominance
                break
            }
        }
        if (!claimed) return@awaitEachGesture

        // ── Follow the finger ────────────────────────────────────────────────────
        // The finger has already travelled [travelledX] by the time the gesture was claimed,
        // so the target starts from there instead of jumping back to zero.
        var totalDrag = travelledX
        var thresholdCrossedFired = false
        if (abs(totalDrag) >= threshold) {
            thresholdCrossedFired = true
            onThresholdCrossed()
        }
        onDragOffset(totalDrag)

        var cancelled = false
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id }
            if (change == null) {
                cancelled = true
                break
            }
            if (!change.pressed) break
            if (change.isConsumed) {
                cancelled = true
                break
            }
            // Read before consuming: positionChange() reports nothing once consumed.
            val delta = change.positionChange()
            change.consume()
            totalDrag += delta.x
            velocityTracker.addPosition(change.uptimeMillis, change.position)
            if (!thresholdCrossedFired && abs(totalDrag) >= threshold) {
                thresholdCrossedFired = true
                onThresholdCrossed()
            }
            onDragOffset(totalDrag)
        }

        if (cancelled) {
            onDragCancel()
            return@awaitEachGesture
        }

        val velocityX = velocityTracker.calculateVelocity().x
        when {
            totalDrag > threshold ||
                (flingVelocityThreshold > 0f && velocityX > flingVelocityThreshold && totalDrag >= 0f) -> onSwipeRight()
            totalDrag < -threshold ||
                (flingVelocityThreshold > 0f && velocityX < -flingVelocityThreshold && totalDrag <= 0f) -> onSwipeLeft()
        }
        onDragEnd()
    }
}
