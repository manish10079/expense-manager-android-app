package com.mknlabs.expensetracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.ui.theme.standardCardGradient
import kotlinx.coroutines.delay

/**
 * A **static** skeleton placeholder for Native Ads that mimics the layout of a TransactionCard.
 * Ensures UI stability while the (Phase 1 preloaded) ad is attached.
 *
 * Phase 3 (ADS_UI_JANK_FIX_PLAN §6): the always-on animated shimmer (rememberInfiniteTransition)
 * was removed — with all placements preloaded, the placeholder renders for ~0 frames, and a
 * per-frame animating subtree competing for the 16 ms budget was a P0 jank source. Same
 * geometry as before (80.dp tall — matches the inflated NativeAdView), so the shimmer ↔ ad
 * swap never re-measures the slot.
 *
 * 2026-08-09 (product decision): a **bounded** animated shimmer was added back — see
 * [AdLoadingShimmer], which runs the sweep for at most 1 second and then falls back to this
 * static skeleton. [NativeAdShimmer] itself stays static so the ~0-frame preloaded case and
 * the post-1s case never animate.
 */
@Composable
fun NativeAdShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon placeholder (Increased to 52dp)
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Headline placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Body placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // CTA Button placeholder
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
        )
    }
}

/**
 * Static skeleton for the **large** (media-first) native ad layout: a tall media placeholder
 * above the compact icon/headline/body/CTA row. Keeps the shimmer geometry close to the
 * inflated [com.google.android.gms.ads.nativead.NativeAdView] (native_ad_large_layout.xml)
 * so the shimmer → ad swap doesn't re-measure the slot.
 */
@Composable
fun NativeAdLargeShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        // Media placeholder — 16:9 like the inflated MediaView, so the shimmer → ad
        // swap doesn't re-measure the slot.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.78f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon placeholder
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Headline placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Body placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // CTA Button placeholder
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
            )
        }
    }
}

/**
 * Ad-loading placeholder with a **bounded** shimmer: a sweeping highlight runs for the first
 * [SHIMMER_ANIMATION_MS] (1 s), then the card falls back to the static [NativeAdShimmer]
 * skeleton so no per-frame animation keeps running while an ad is still pending.
 *
 * This reconciles the product desire for a "loading" affordance with the Phase 3 jank finding
 * (P0 finding #2: an *unbounded* infinite shimmer was a P0 jank source). The animation budget
 * is strictly capped — at most ~1 s of an infinite-transition sweep, then a fully static subtree.
 *
 * @param skeleton The static skeleton the sweep runs over (defaults to the compact
 * [NativeAdShimmer]; pass { NativeAdLargeShimmer() } for the large media-first layout).
 */
private const val SHIMMER_ANIMATION_MS = 1_000L

@Composable
fun AdLoadingShimmer(
    skeleton: @Composable () -> Unit = { NativeAdShimmer() }
) {
    // After 1 s, drop the animated highlight and keep only the static skeleton.
    var animate by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(SHIMMER_ANIMATION_MS)
        animate = false
    }

    if (!animate) {
        // Static fallback — the infinite transition has left composition entirely, so no
        // per-frame animation keeps running (Phase 3 finding #2 stays bounded at ~1 s).
        skeleton()
        return
    }

    // Sweep progress 0f → 1f. Created here so it is removed from composition the moment the
    // 1 s budget elapses (conditional composition stops the animation clock, unlike hoisting
    // the transition above the early return).
    val transition = rememberInfiniteTransition(label = "ad_loading_shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_ANIMATION_MS.toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ad_loading_shimmer_progress"
    )

    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        skeleton()

        // Sweeping highlight band drawn on top of the same geometry as the static skeleton.
        Canvas(modifier = Modifier.matchParentSize()) {
            val bandWidth = size.width * 0.6f
            val startX = -bandWidth + progress * (size.width + bandWidth)
            drawRect(
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        0.5f to highlight,
                        1f to Color.Transparent
                    ),
                    startX = startX,
                    endX = startX + bandWidth
                )
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NativeAdShimmerLightPreview() {
    com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme(darkTheme = false) {
        androidx.compose.material3.Surface {
            Box(modifier = Modifier.padding(16.dp)) {
                NativeAdShimmer()
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NativeAdShimmerDarkPreview() {
    com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme(darkTheme = true) {
        androidx.compose.material3.Surface {
            Box(modifier = Modifier.padding(16.dp)) {
                NativeAdShimmer()
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 360)
@Composable
fun AdLoadingShimmerPreview() {
    com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme {
        androidx.compose.material3.Surface {
            Box(modifier = Modifier.padding(16.dp)) {
                AdLoadingShimmer()
            }
        }
    }
}
