package com.mknlabs.expensetracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Minimum height reserved for an ad slot (Phase 3, ADS_UI_JANK_FIX_PLAN §6).
 * Matches the tallest of {static skeleton, inflated NativeAdView} — both are 80.dp
 * (14.dp vertical padding + 52.dp icon), so the shimmer ↔ ad swap never re-measures
 * the surrounding layout (finding #4: Home list viewport jump on ad arrival).
 */
private val AdSlotMinHeight = 80.dp

/**
 * A standard wrapper for all ad placements.
 * Handles visibility logic (Premium vs. Free) and provides a clean interface for lifecycle management.
 *
 * Phase 3 (ADS_UI_JANK_FIX_PLAN §6): size-animating `expandVertically`/`shrinkVertically` were
 * removed — they shifted surrounding content when the slot toggled. Transitions are **fade-only**
 * and the visible slot reserves a stable minimum height, so the column/list never re-flows.
 *
 * The minimum height is applied to the inner Box (NOT the AnimatedVisibility), so a hidden
 * slot (ads disabled, e.g. Pro/ad-free users) collapses to **zero** height and leaves no gap.
 *
 * @param isAdsEnabled Whether ads should be shown for the current user.
 * @param modifier Modifier for the container.
 * @param content The actual ad content (Banner, Native, etc.).
 */
@Composable
fun AdContainer(
    isAdsEnabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Phase 2: Lifecycle Safety
    // This DisposableEffect ensures that any ad resources are properly cleaned up when the component leaves the composition.
    DisposableEffect(Unit) {
        onDispose {
            // Cleanup logic for ad views can be added here in future phases.
        }
    }

    AnimatedVisibility(
        visible = isAdsEnabled,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AdSlotMinHeight)
        ) {
            content()
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AdContainerVisiblePreview() {
    com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme {
        androidx.compose.material3.Surface {
            Box(modifier = Modifier.padding(16.dp)) {
                AdContainer(isAdsEnabled = true) {
                    NativeAdShimmer()
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AdContainerHiddenPreview() {
    com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme {
        androidx.compose.material3.Surface {
            Box(modifier = Modifier.padding(16.dp)) {
                // Should be empty
                AdContainer(isAdsEnabled = false) {
                    NativeAdShimmer()
                }
            }
        }
    }
}
