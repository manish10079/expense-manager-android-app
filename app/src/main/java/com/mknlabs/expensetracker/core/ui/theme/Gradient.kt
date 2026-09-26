package com.mknlabs.expensetracker.core.ui.theme

import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.material3.MaterialTheme

/**
 * Standardized gradients for the application to ensure brand consistency.
 */

/**
 * A card fill that paints a bitmap stretched to the surface's bounds.
 *
 * The Cash Flow hero's dark surface is a baked PNG rather than a gradient assembled
 * from tokens, so it cannot be handed over as a plain [Brush] the way every other hero
 * fill is. Scaling a shader to the bounds reproduces what an `Image` with
 * `ContentScale.FillBounds` was doing, which is what lets that card move onto the
 * shared card chrome without its dark appearance changing.
 */
fun bitmapFill(bitmap: ImageBitmap): Brush = BitmapFillBrush(bitmap)

private class BitmapFillBrush(private val bitmap: ImageBitmap) : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        val image = bitmap.asAndroidBitmap()
        val matrix = Matrix().apply {
            setScale(size.width / image.width, size.height / image.height)
        }
        return BitmapShader(image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
            setLocalMatrix(matrix)
        }
    }
}

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
 * The Cash Flow hero's base fill: a three-stop violet ramp from top-left to bottom-right.
 *
 * Both themes run the identical recipe — same hue, same saturation, same bloom positions
 * and radii, same inks — and differ only in the lightness of this ramp. That is what makes
 * the dark and light heroes read as the same card, and it is why this returns a fill in
 * light too: the hero no longer falls back to a plain white card, and the baked dark PNG
 * is gone. The two blooms that sit over this ramp are painted by
 * [cashFlowHeroGlows], since a single [Brush] cannot stack them.
 */
@Composable
fun cashFlowHeroBaseBrush(): Brush {
    val isDark = MaterialTheme.colorScheme.isDark
    val start = if (isDark) CashFlowHeroDarkStart else CashFlowHeroLightStart
    val center = if (isDark) CashFlowHeroDarkCenter else CashFlowHeroLightCenter
    val end = if (isDark) CashFlowHeroDarkEnd else CashFlowHeroLightEnd

    return remember(start, center, end) {
        Brush.linearGradient(
            colors = listOf(start, center, end),
            start = Offset.Zero,
            end = Offset.Infinite
        )
    }
}

/**
 * Overpaints the Cash Flow hero's two radial blooms — a light violet wash at the top-left
 * and a deep violet one at the bottom-right — over the base from [cashFlowHeroBaseBrush].
 *
 * A [Modifier] rather than a [Brush] because the blooms are positioned relative to the
 * surface: [Brush.radialGradient] takes its centre and radius in pixels, and only
 * [drawWithCache] hands back a [Size] to compute them from. Attach it to a fill that spans
 * the card; the card's own clip keeps the blooms inside its rounded corners.
 */
@Composable
fun Modifier.cashFlowHeroGlows(): Modifier {
    val isDark = MaterialTheme.colorScheme.isDark
    val glowBottom = if (isDark) CashFlowHeroGlowBottomDark else CashFlowHeroGlowBottomLight

    return this.drawWithCache {
        val topGlow = Brush.radialGradient(
            colors = listOf(CashFlowHeroGlowTop, Color.Transparent),
            center = Offset(size.width * 0.12f, 0f),
            radius = size.maxDimension * 0.9f
        )
        val bottomGlow = Brush.radialGradient(
            colors = listOf(glowBottom, Color.Transparent),
            center = Offset(size.width * 0.9f, size.height),
            radius = size.maxDimension * 1.1f
        )
        onDrawBehind {
            drawRect(topGlow)
            drawRect(bottomGlow)
        }
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
