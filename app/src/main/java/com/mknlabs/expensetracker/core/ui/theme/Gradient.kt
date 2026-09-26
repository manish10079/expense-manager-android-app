package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

/**
 * Standardized gradients for the application to ensure brand consistency.
 */

/**
 * A card fill that exists only in dark mode.
 *
 * The app's hero surfaces are brand gradients against the dark field and plain white
 * cards against the light one, so the light redesign's rule for them is "gradient in
 * dark, flat in light". Returning null in light lets a call site hand the result
 * straight to a card's brush, which then falls back to the theme's card colour rather
 * than needing the branch at every site.
 */
@Composable
fun darkOnlyGradient(gradient: Brush): Brush? =
    if (MaterialTheme.colorScheme.isDark) gradient else null

/**
 * The Cash Flow hero's leading rail: a 3dp bar down the card's start edge.
 *
 * Vertical rather than horizontal because the rail runs the card's full height, so the
 * gradient has to travel the same axis to read as one lit edge rather than as a band that
 * fades as it descends.
 */
@Composable
fun heroRailBrush(): Brush {
    val isDark = MaterialTheme.colorScheme.isDark
    val start = if (isDark) HeroRailStartDark else HeroRailStartLight
    val end = if (isDark) HeroRailEndDark else HeroRailEndLight
    return remember(start, end) {
        Brush.verticalGradient(colors = listOf(start, end))
    }
}

/** Rail width, part of the recipe rather than a call site's own choice. */
private val HeroRailWidth = 3.dp

/**
 * Draws that rail against the start edge of whatever this is attached to.
 *
 * Attach it to a fill that spans the card. The card's own clip rounds the rail's ends to
 * the card corner for free, which is why this draws a plain rectangle rather than a shape.
 */
@Composable
fun Modifier.heroRail(): Modifier {
    val brush = heroRailBrush()
    return this.drawWithCache {
        val width = HeroRailWidth.toPx()
        onDrawBehind { drawRect(brush = brush, size = Size(width, size.height)) }
    }
}

/**
 * The hero's single bloom: the brand accent washed across the card's top-trailing corner.
 *
 * One bloom rather than the old pair, and off a corner rather than spread over the whole
 * surface. At this alpha it reads as light catching the edge, which is what carries the
 * brand here now that the surface underneath it is neutral.
 *
 * Position and radius are fractions of the card rather than the fixed pixels the mock
 * specifies, because the mock is drawn at desktop width and these have to hold at phone
 * width too. The fractions are the mock's own geometry expressed against its card width.
 */
@Composable
fun Modifier.heroBloom(): Modifier {
    val bloom = MaterialTheme.colorScheme.glow

    return this.drawWithCache {
        val brush = Brush.radialGradient(
            colors = listOf(bloom, Color.Transparent),
            center = Offset(size.width * 0.92f, size.height * 0.18f),
            radius = size.width * 0.30f
        )
        onDrawBehind { drawRect(brush) }
    }
}

/**
 * The brand as an INK, ramped for the one affordance that paints a glyph with it.
 *
 * The mock keeps accent and fill as two different roles, and they stop agreeing in dark:
 * `--accent` is `#9E84FF`, which is meant to be read as a colour on a neutral surface,
 * while `--cta` is `#5838FA`, which is meant to be sat on. A destination icon or label
 * painted with [brandGradient] would therefore be near-invisible in dark once that
 * gradient moved to the CTA ramp — dark ink on a dark field. This keeps the glyph side
 * of the brand on the accent instead: 5.94:1 on the card in dark, 5.10:1 in light.
 *
 * Never use this as a fill: it is the ink role, and a fill needs [brandGradient]'s 600/700
 * so a label can sit on it.
 */
@Composable
fun accentInkGradient(): Brush {
    val isDark = MaterialTheme.colorScheme.isDark
    val start = if (isDark) AccentInkDark else AccentInkLight
    val end = if (isDark) PurplePrimary else ChipSelectedInkLight
    return remember(start, end) {
        Brush.linearGradient(
            colors = listOf(start, end)
        )
    }
}

/**
 * The app's one brand fill: the mock's CTA ramp, `#5838FA → #3713EC` in dark and
 * `#6A4DFF → #5B45D6` in light.
 *
 * Deliberately NOT `primary → secondary`. The scheme's dark primary is `#7B61FF`, the
 * fully-saturated 500, which the mock keeps as accent, glow and focus ring only — it
 * cannot carry either black or white ink at 4.5:1, so a fill built from it has to be
 * replaced rather than recoloured, and its pale `secondary` end (`#CDBDFF`) made the
 * gradient read as a bright wash rather than as a brand surface. Both ends are now the
 * 600/700 the spec prescribes for fills, which is what the filled buttons, selected
 * chips and segment indicators already reach for through [ColorScheme.cta].
 *
 * Ink on this fill is white in BOTH themes ([ColorScheme.onCta] / [ColorScheme.onBrandGradient]);
 * black fails on `#5838FA` (3.17:1). The `alpha` parameter is for tints — a chip's selected
 * background is this ramp at 20% — not for the fill itself.
 */
@Composable
fun brandGradient(alpha: Float = 1f): Brush {
    val isDark = MaterialTheme.colorScheme.isDark
    val start = (if (isDark) HeroRailStartDark else HeroRailStartLight).copy(alpha = alpha)
    val end = (if (isDark) HeroRailEndDark else HeroRailEndLight).copy(alpha = alpha)
    return remember(start, end) {
        Brush.linearGradient(
            colors = listOf(start, end)
        )
    }
}

/**
 * Fill for the Add-transaction FAB: the brand ramp lit from the top-left corner.
 *
 * These are the mock's own CTA ends — `#5838FA → #3713EC` in dark and
 * `#6A4DFF → #5B45D6` in light — not a hand-tuned lerp of the scheme's `primary`.
 * `primary` in dark is the fully-saturated 500 (`#7B61FF`), which the spec keeps as
 * accent, glow and focus ring and never as a fill; a large brand block built by
 * lerping it is exactly the "over-purple" surface the redesign replaces. Routing the
 * FAB through the same ramp every filled control uses also means the FAB and the
 * reveal handle cannot drift apart.
 *
 * The lighter end sits top-left because that is where [Brush.linearGradient] starts.
 */
@Composable
fun fabGradient(): Brush {
    val isDark = MaterialTheme.colorScheme.isDark
    val lit = if (isDark) HeroRailStartDark else HeroRailStartLight
    val deep = if (isDark) HeroRailEndDark else HeroRailEndLight
    return remember(lit, deep) {
        Brush.linearGradient(
            colors = listOf(lit, deep)
        )
    }
}

// The card fill, read straight from the spec's card ladder rather than a wash of
// `surfaceVariant`. The old recipe blended a legacy #353534 down over the field to reach
// the spec by accident; now that `surfaceVariant` is the spec's own --menu (#26262E), that
// blend would land off-spec, so the ladder is read directly:
// dark runs #1A1A20 (card) to #1E1E23 (the sheet rung one step up), light runs #FFFFFF
// (card) to #FAFAFC (its sheet rung). Both ends are spec tokens in their own theme.
@Composable
fun standardCardGradient(): Brush {
    val isDark = MaterialTheme.colorScheme.isDark
    val top = if (isDark) HeroSurfaceDark else HeroSurfaceLight
    val bottom = if (isDark) SheetDark else SheetLight
    return remember(top, bottom) {
        Brush.verticalGradient(
            colors = listOf(top, bottom)
        )
    }
}

// The screen surface, one rung below the card: dark steps the field #141418 up to the
// card #1A1A20, light steps the field #FFFFFF down to its sheet rung #FAFAFC.
@Composable
fun surfaceGradient(): Brush {
    val isDark = MaterialTheme.colorScheme.isDark
    val top = MaterialTheme.colorScheme.surface
    val bottom = if (isDark) HeroSurfaceDark else SheetLight
    return remember(top, bottom) {
        Brush.verticalGradient(
            colors = listOf(top, bottom)
        )
    }
}

@Composable
fun subtlePrimaryGradient(): Brush {
    val color1 = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    val color2 = MaterialTheme.colorScheme.surface
    return remember(color1, color2) {
        Brush.horizontalGradient(
            colors = listOf(color1, color2)
        )
    }
}
