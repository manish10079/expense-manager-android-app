package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * The app lock's palette: the one light/dark lookup shared by the unlock surface
 * (AppLockOverlay) and the setup flow (AppLockScreen).
 *
 * Every value is a token from Color.kt — nothing here is a literal — so the lock's
 * design lives in one place and a retune of the brand ramp reaches both screens.
 *
 * The lock keeps a single identity across both themes, which is why only the six
 * constructor properties repaint between them. The brand ramp, the PIN dot's deep end,
 * the error rose, the aurora and the biometric ring are identical in both: the mock
 * repaints only the container, the ink and the keypad chrome.
 */
@Immutable
class AppLockPalette(
    /** Mock `bg-black` / `bg-white` on the phone container. */
    val container: Color,
    /** Mock `text-slate-100` / `text-slate-800`. */
    val ink: Color,
    /** Mock `keypad-btn-*`: `background`. */
    val keyFill: Color,
    /** Mock `keypad-btn-*`: `border`. */
    val keyBorder: Color,
    /** Mock `keypad-btn-*:active`: `background`. */
    val keyPressFill: Color,
    /** Mock `keypad-btn-*:active`: `border-color`. */
    val keyPressBorder: Color,
) {
    /**
     * The brand ramp's start, as a solid.
     *
     * Read from the Light twin deliberately: `BrandGradientStartDark` is the same value,
     * so the lock is the same purple in both themes. For the ramp itself prefer
     * [brandGradient], which is built from this same pair.
     */
    val brand: Color = BrandGradientStartLight

    /** The ramp's title end. Identical in both themes, as the mock has it. */
    val brandCompanion: Color = BrandGradientCompanionLight

    /**
     * The PIN dot's deeper end, deeper than the title's ramp end.
     *
     * `HeroRailEndLight` rather than a token of its own: the value is the same, and the
     * lock's dot has always read from it. Note its Dark twin is `#3713EC`, which is the
     * *hero rail's* dark ramp — the lock deliberately does not follow it, and reads the
     * light value in both themes.
     */
    val pinDotEnd: Color = HeroRailEndLight

    /** Wrong PIN / error ink. ExpenseInkDark and ExpenseInkLight are the same rose. */
    val errorInk: Color = ExpenseInkDark

    /** The aurora's two glows, painted into opposite corners. */
    val auroraSoft: Color = AppLockAuroraSoft
    val auroraStrong: Color = AppLockAuroraStrong

    /** The biometric ring's stroke and the halo inside it. */
    val biometricRing: Color = AppLockBiometricRing
    val biometricHalo: Color = AppLockBiometricHalo
}

/** Dark: true black behind pale ink, the mock's `bg-black` and `text-slate-100`. */
val AppLockPaletteDark = AppLockPalette(
    container = AppLockLoadingBackground,
    ink = InkStrongDark,
    keyFill = SurfaceHighlight,
    keyBorder = AppLockKeyBorderDark,
    keyPressFill = AppLockKeyPressFillDark,
    keyPressBorder = AppLockKeyPressBorderDark,
)

/** Light: the mock's `bg-white` and `text-slate-800`. */
val AppLockPaletteLight = AppLockPalette(
    container = SurfaceLight,
    ink = InkStrongLight,
    keyFill = AppLockKeyFillLight,
    keyBorder = AppLockKeyBorderLight,
    keyPressFill = AppLockKeyPressFillLight,
    keyPressBorder = AppLockKeyPressBorderLight,
)

@Composable
fun appLockPalette(): AppLockPalette =
    if (MaterialTheme.colorScheme.isDark) AppLockPaletteDark else AppLockPaletteLight
