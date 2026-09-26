package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
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
    val isDark = MaterialTheme.colorScheme.isDark
    val bloom = if (isDark) HeroBloomDark else HeroBloomLight

    return this.drawWithCache {
        val brush = Brush.radialGradient(
            colors = listOf(bloom, Color.Transparent),
            center = Offset(size.width * 0.92f, size.height * 0.18f),
            radius = size.width * 0.30f
        )
        onDrawBehind { drawRect(brush) }
    }
}

@Composable
fun brandGradient(alpha: Float = 1f): Brush {
    val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
    val secondaryColor = MaterialTheme.colorScheme.secondary.copy(alpha = alpha)
    return remember(primaryColor, secondaryColor) {
        Brush.linearGradient(
            colors = listOf(primaryColor, secondaryColor)
        )
    }
}

/**
 * Fill for the Add-transaction FAB: a lit-from-the-top-left violet rather than the
 * flat brand fill it used to be.
 *
 * Both ends are derived from `primary` through [lerp] instead of being new hex
 * values, so the circle follows the theme — a lighter and a deeper purple in dark
 * mode, the same relationship in light mode — and the brand hue can never drift
 * away from a gradient that was tuned by hand. The lighter end sits top-left
 * because that is where [Brush.linearGradient] starts, which matches the raised
 * visual language the rest of the app's brand surfaces already use.
 */
@Composable
fun fabGradient(): Brush {
    val lit = lerp(MaterialTheme.colorScheme.primary, Color.White, 0.22f)
    val deep = lerp(MaterialTheme.colorScheme.primary, Color.Black, 0.18f)
    return remember(lit, deep) {
        Brush.linearGradient(
            colors = listOf(lit, deep)
        )
    }
}

@Composable
fun standardCardGradient(): Brush {
    val color1 = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val color2 = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    return remember(color1, color2) {
        Brush.verticalGradient(
            colors = listOf(color1, color2)
        )
    }
}

@Composable
fun surfaceGradient(): Brush {
    val color1 = MaterialTheme.colorScheme.surface
    val color2 = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    return remember(color1, color2) {
        Brush.verticalGradient(
            colors = listOf(color1, color2)
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
