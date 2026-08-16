package com.mknlabs.expensetracker.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity

/**
 * Coarse system font-scale tiers. Since Android 14 (API 34) font scaling is
 * non-linear up to 200%, the raw [androidx.compose.ui.unit.TextUnit]-scale value
 * must never be used in layout math — layouts branch on these tiers instead and
 * let text-bearing containers flex (heightIn / wrap), never shrink to fit.
 *
 * Thresholds mirror the pre-existing `AppHeader` idiom:
 * Default <1.3f · Large 1.3–1.5f · Huge >1.5f.
 */
enum class FontScaleTier { Default, Large, Huge }

/** Snapshot of the current system font-scale tier. */
data class FontScaleInfo(val tier: FontScaleTier)

/** Maps the raw system font scale to a coarse tier (informational only). */
@Composable
fun rememberFontScaleInfo(): FontScaleInfo {
    val fontScale = LocalDensity.current.fontScale
    val tier = when {
        fontScale > 1.5f -> FontScaleTier.Huge
        fontScale > 1.3f -> FontScaleTier.Large
        else -> FontScaleTier.Default
    }
    return FontScaleInfo(tier = tier)
}

/**
 * Provided once at the app root (beside [LocalAppWindowInfo]) so every screen
 * reads the same stable value. Screens branch on [FontScaleInfo.tier] only —
 * never multiply sizes by the raw font scale.
 */
val LocalFontScaleInfo = staticCompositionLocalOf {
    FontScaleInfo(tier = FontScaleTier.Default)
}
