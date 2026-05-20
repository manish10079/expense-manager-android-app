package com.mkn0079.expensetracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

/**
 * A standard wrapper for all ad placements.
 * Handles visibility logic (Premium vs. Free) and provides a clean interface for lifecycle management.
 * 
 * @param isAdsEnabled Whether ads should be shown for the current user.
 * @param modifier Modifier for the container.
 * @param showShimmer Whether to show a shimmer while the ad is loading (handled by the content).
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
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AdContainerVisiblePreview() {
    com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme {
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
    com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme {
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
