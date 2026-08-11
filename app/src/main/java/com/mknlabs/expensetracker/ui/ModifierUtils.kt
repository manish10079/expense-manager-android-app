package com.mknlabs.expensetracker.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker

/**
 * Reusable modifier for handling horizontal swipe gestures across the UI layer.
 * 
 * @param key Optional key to reset the gesture detection state (e.g., when content changes).
 * @param threshold The drag distance in pixels to trigger a swipe action (default: 80dp converted to px elsewhere or just raw px here).
 * @param flingVelocityThreshold Release velocity (px/s) that triggers a swipe action even when
 *   the drag distance is below [threshold]. 0f (default) disables velocity-based triggering.
 * @param onDragOffset Called with the current accumulated horizontal drag in px while the
 *   finger moves — useful to visually translate the target (e.g. a card following the swipe).
 * @param onSwipeLeft Callback triggered when a left swipe (negative horizontal drag) exceeds the threshold or fling velocity.
 * @param onSwipeRight Callback triggered when a right swipe (positive horizontal drag) exceeds the threshold or fling velocity.
 * @param onDragEnd Called when the pointer lifts after a drag, after any swipe callbacks fired.
 * @param onDragCancel Called when the gesture is cancelled before release (e.g. consumed elsewhere).
 */
fun Modifier.horizontalSwipe(
    key: Any? = null,
    threshold: Float = 80f,
    flingVelocityThreshold: Float = 0f,
    onDragOffset: (Float) -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {}
): Modifier = this.pointerInput(key) {
    var totalDrag = 0f
    val velocityTracker = VelocityTracker()
    detectHorizontalDragGestures(
        onHorizontalDrag = { change, dragAmount ->
            velocityTracker.addPosition(change.uptimeMillis, change.position)
            totalDrag += dragAmount
            onDragOffset(totalDrag)
        },
        onDragEnd = {
            val velocityX = velocityTracker.calculateVelocity().x
            when {
                totalDrag > threshold ||
                    (flingVelocityThreshold > 0f && velocityX > flingVelocityThreshold && totalDrag >= 0f) -> onSwipeRight()
                totalDrag < -threshold ||
                    (flingVelocityThreshold > 0f && velocityX < -flingVelocityThreshold && totalDrag <= 0f) -> onSwipeLeft()
            }
            onDragEnd()
            totalDrag = 0f
            velocityTracker.resetTracking()
        },
        onDragCancel = {
            onDragCancel()
            totalDrag = 0f
            velocityTracker.resetTracking()
        }
    )
}
