package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.fill.Diamond
import com.adamglin.phosphoricons.fill.FlowerLotus
import com.adamglin.phosphoricons.fill.Heart
import com.adamglin.phosphoricons.fill.Leaf
import com.adamglin.phosphoricons.fill.PawPrint
import com.adamglin.phosphoricons.fill.RocketLaunch
import com.adamglin.phosphoricons.fill.Sparkle
import com.adamglin.phosphoricons.fill.Star
import com.mknlabs.expensetracker.core.ui.theme.BrandGradientStartLight
import com.mknlabs.expensetracker.core.ui.theme.ExpenseInkDark
import com.mknlabs.expensetracker.core.ui.theme.HeroRailEndLight
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.models.PinVisualMode
import kotlinx.coroutines.delay

/**
 * How long premium's glyph holds before handing over to the dot — the 400ms the pre-rewrite
 * unlock surface used (`PinSlot` in AppLockScreen).
 */
private const val APP_LOCK_GLYPH_HOLD_MS = 400L

/**
 * The glyph's box, 24dp — the size the glyphs had before the rewrite.
 *
 * Bigger than the mock's 14dp dot on purpose: the dot is the resting state, the glyph is the
 * flourish that lands on it. See the dot's own note for why it must not be clamped.
 */
private val AppLockGlyphSize = 24.dp

/**
 * The PIN row: the entered dots, or the error sentence in their place.
 *
 * The shake is applied by the caller, which owns the animatable, so the row itself stays a
 * pure function of the state it is handed.
 */
@Composable
fun AppLockPinStatusRow(
    filledCount: Int,
    pinVisualMode: PinVisualMode,
    isError: Boolean,
    shakeOffsetPx: Float,
    statusText: String?,
    errorDescription: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .graphicsLayer { translationX = shakeOffsetPx }
            .semantics {
                // The shake and the red dots are the whole error signal on screen, and
                // neither reaches a screen reader. Surface the same sentence it means.
                if (isError) {
                    contentDescription = errorDescription
                    liveRegion = LiveRegionMode.Assertive
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = statusText,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "app_lock_pin_status"
        ) { message ->
            if (message != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = message,
                        color = ExpenseInkDark,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(filledCount) {
                        AppLockPinDot(
                            isError = isError,
                            pinVisualMode = pinVisualMode
                        )
                    }
                }
            }
        }
    }
}

/**
 * One entered PIN digit: the mock's brand-painted dot, with premium's glyph landing on it.
 *
 * The dot is the mock's — 14dp, the `pin-dot` ramp, a 6dp brand shadow, the `dot-pop`
 * overshoot. Premium keeps the flourish the pre-rewrite surface had: a glyph from the same
 * pool eases in from 0.7 on a soft spring, holds 400ms, then hands over to the dot.
 */
@Composable
fun AppLockPinDot(
    isError: Boolean,
    pinVisualMode: PinVisualMode
) {
    val iconPool = remember {
        listOf(
            PhosphorIcons.Fill.PawPrint,
            PhosphorIcons.Fill.Leaf,
            PhosphorIcons.Fill.Heart,
            PhosphorIcons.Fill.Star,
            PhosphorIcons.Fill.Diamond,
            PhosphorIcons.Fill.Sparkle,
            PhosphorIcons.Fill.RocketLaunch,
            PhosphorIcons.Fill.FlowerLotus
        )
    }
    var glyph by remember { mutableStateOf<ImageVector?>(null) }
    // The mock's popIn overshoots to 1.2 before landing on 1, which is what a low damping
    // ratio gives for free.
    val scale = remember { Animatable(0f) }
    // The glyph does not pop from nothing: it eases in from 0.7, as the pre-rewrite
    // surface's scaleIn did.
    val glyphScale = remember { Animatable(0.7f) }
    val dotBrush = remember(isError) {
        if (isError) {
            SolidColor(ExpenseInkDark)
        } else {
            Brush.linearGradient(colors = listOf(BrandGradientStartLight, HeroRailEndLight))
        }
    }

    LaunchedEffect(isError, pinVisualMode) {
        if (!isError && pinVisualMode == PinVisualMode.PRO_ANIMATED) {
            // While the glyph is up it carries the entrance on its own, so the dot's pop is
            // parked at full size instead of fighting it up from zero.
            scale.snapTo(1f)
            glyph = iconPool.random()
            glyphScale.snapTo(0.7f)
            glyphScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            delay(APP_LOCK_GLYPH_HOLD_MS)
            glyph = null
            scale.snapTo(0f)
        } else {
            glyph = null
            scale.snapTo(0f)
        }
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    Box(
        modifier = Modifier
            .size(14.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
        contentAlignment = Alignment.Center
    ) {
        val currentGlyph = glyph
        if (currentGlyph != null) {
            // requiredSize, not size: the glyph is meant to be larger than the dot it lands
            // on, and a plain size() would be clamped to this 14dp slot — which is what made
            // the glyphs read as specks.
            Box(
                modifier = Modifier
                    .requiredSize(AppLockGlyphSize)
                    .graphicsLayer {
                        scaleX = glyphScale.value
                        scaleY = glyphScale.value
                    }
            ) {
                AppLockGradientIcon(
                    imageVector = currentGlyph,
                    brush = brandGradient(),
                    size = AppLockGlyphSize
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = BrandGradientStartLight.copy(alpha = 0.5f),
                        spotColor = BrandGradientStartLight.copy(alpha = 0.5f)
                    )
                    .clip(CircleShape)
                    .background(brush = dotBrush)
            )
        }
    }
}
