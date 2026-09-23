package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance


val transparent= Color.Transparent;
val PurplePrimary = Color(0xFF7B61FF)
val PurpleAccent = Color(0xFFCDBDFF)


val BackgroundDark = Color(0xFF0A0A0A)

val SurfaceDark = Color(0xFF181817)
val CardDark = Color(0xFF353534)


val TextPrimaryDark = Color(0xFFEAEAEA)
val TextSecondaryDark = Color(0xFF9E9E9E)

val DividerDark = Color(0xFF2A2A2A)

// Semantic amount inks. Saturated enough to carry the expense and income figures on
// the dark violet card, where the previous pastels washed out against the surface and
// read as decoration rather than as the one number the row exists to show.
val IncomeGreen = Color(0xFF3DDC97)
val ExpenseRed = Color(0xFFFF6B6B)

// Inbox amount badge: solid fills dark enough to carry a white currency glyph in both
// themes, which the pastel income/expense tones above are too light to do.
val BadgeIncomeGreen = Color(0xFF1E8E3E)
val BadgeExpenseRed = Color(0xFFC5221F)

/** The glyph on those two: fixed white, since the fills are the same in both themes. */
val BadgeOnColor = Color(0xFFFFFFFF)

// Inbox row avatars: soft tints, each paired with a glyph colour of the same hue that
// stays readable on it. A row picks by sender, so any one sender keeps its own colour
// while two senders side by side rarely share one.
val PurplePrimaryLight = Color(0xFF6A4DFF)
val PurpleAccentLight = Color(0xFF8C6DFF)

// Savings Goal Progress Shades
val GoalProgressLow = Color(0xFFD6C8FF)
val GoalProgressMedium = Color(0xFF9E84FF)
val GoalProgressHigh = PurplePrimary

// Default Goal Color Hex (Brand Purple)
const val DEFAULT_GOAL_COLOR_HEX = "#7B61FF"

// Ad Loading Screen Colors
val AdLoadingScrim = Color(0x99000000) // Black with 60% alpha
val AdLoadingText = Color(0xFFFFFFFF)

// App Lock Colors
val AppLockLoadingBackground = Color(0xFF000000)

// Semantic UI Highlights
val SurfaceHighlight = Color(0x0DFFFFFF) // White with 5% alpha

// Neutral Shades
val NeutralGray = Color(0xFF8E8E93)

val BackgroundLight = Color(0xFFEDEDF1)

val SurfaceLight = Color(0xFFFFFFFF)
val CardLight = Color(0xFFD2D2E3)

val TextPrimaryLight = Color(0xFF1A1A1A)
val TextSecondaryLight = Color(0xFF6E6E73)

val DividerLight = Color(0xFFE2E2E6)

val DarkGradientStart = Color(0xFF6C5AE1)
val DarkGradientEnd = Color(0xFF282626)

val LightGradientStart = Color(0xFFF8F5FF)
val LightGradientEnd = Color(0xFF9480EE)

val IconColor = Color(0xFFCDBDFF)
val FeatureGateLockGold = Color(0xFFFFC857)

// Premium StatsCard Gradient
val PremiumCardDarkStart = Color(0xFF2B2349)   // Violet, top-left
val PremiumCardDarkCenter = Color(0xFF1C1632) // Violet-charcoal, centre
val PremiumCardDarkEnd = Color(0xFF100C1F)     // Near-black plum, bottom-right

// The two radial blooms stacked OVER the linear base above, so the surface reads as
// light falling across the card rather than as one flat diagonal. Both are plain
// opaque colours, not alphas of the theme's `primary`: they are blended into the base
// with `BlendMode.Screen`, which is what makes them behave as light rather than as
// paint, and an already-translucent source would only fight that.
val PremiumCardGlowTop = Color(0xFF43346B)
val PremiumCardGlowBottom = Color(0xFF241A42)

// The card family's edge, as its own pair rather than a tint of the brand accent: the
// border has to read as the lit top-left edge of the surface it outlines, and a brand
// violet would sit off the gradient's hue instead of graduating with it.
val PremiumCardBorderStart = Color(0xFF584B7C)
val PremiumCardBorderEnd = Color(0xFF231D38)

val PremiumCardLightStart = Color(0xFFF8F5FF)  // Soft Lavender White
val PremiumCardLightCenter = Color(0xFFEDE8FF) // Pale Lilac
val PremiumCardLightEnd = Color(0xFFE0D8F5)    // Light Violet

val PremiumCardLabelDark = Color(0xFFA09CAB)   // Muted blue-gray for dark mode labels
val PremiumCardDateDark = Color(0xFF7A7585)     // Dimmer gray for dark mode date

// CashFlowCard Palette
val CashFlowCardDarkStart = Color(0xFF1E1735)   // 0%
val CashFlowCardDarkCenter = Color(0xFF131120)  // 55%
val CashFlowCardDarkEnd = Color(0xFF0C0B12)     // 100%
val CashFlowCardGlowTop = Color(0x387A52FF)      // rgba(122, 82, 255, 0.22)
val CashFlowCardGlowBottom = Color(0x214C2ACF)   // rgba(76, 42, 207, 0.13)

val CashFlowCardLightStart = Color(0xFFFAF8FF)
val CashFlowCardLightCenter = Color(0xFFF3EEFC)
val CashFlowCardLightEnd = Color(0xFFECE5F8)

val CashFlowCardBorderDarkStart = Color(0x807A52FF) // rgba(122, 82, 255, 0.50) at 0%
val CashFlowCardBorderDarkMid = Color(0x24BFA6FF)   // rgba(191, 166, 255, 0.14) at 45%
val CashFlowCardBorderLight = Color(0xFFE2DCF0)

val CashFlowDateTextDark = Color(0xFFA792E8)
val CashFlowDateTextLight = Color(0xFF6C52EE)

val CashFlowPillBgDark = Color(0xFF231D33)
val CashFlowPillBgLight = Color(0xFFEDE8F8)
val CashFlowPillBorderDark = Color(0xFF3B3254)
val CashFlowPillBorderLight = Color(0xFFDCD4F0)
val CashFlowPillTextDark = Color(0xFFDDD8EC)
val CashFlowPillTextLight = Color(0xFF372D54)

val CashFlowExpenseAmountDark = Color(0xFFFF7262) // Coral salmon red
val CashFlowExpenseAmountLight = Color(0xFFE04343) // Vivid crimson red

val CashFlowIncomeAmountDark = Color(0xFF5DE290)  // Radiant mint green
val CashFlowIncomeAmountLight = Color(0xFF16A34A) // Deep emerald green

val CashFlowLabelDark = Color(0xFF9089A4)
val CashFlowLabelLight = Color(0xFF746B8B)

val CashFlowNetBalanceBgDark = Color(0xFF161224)
val CashFlowNetBalanceBgLight = Color(0xFFEDE8F8)
val CashFlowNetBalanceBorderDark = Color(0xFF2E2644)
val CashFlowNetBalanceBorderLight = Color(0xFFDBD3EE)
val CashFlowNetBalanceLabelDark = Color(0xFFB5ADCA)
val CashFlowNetBalanceLabelLight = Color(0xFF574E6F)
val CashFlowNetBalanceAmountDark = Color(0xFFFFFFFF)
val CashFlowNetBalanceAmountLight = Color(0xFF1E1738)

// SmallHomeCard Gradient
val SmallCardDarkStart = Color(0xFF1E1730)      // Deep violet-charcoal
val SmallCardDarkEnd = Color(0xFF151218)        // Muted dark slate
val SmallCardLightStart = Color(0xFFFAF7FF)     // Soft lavender white
val SmallCardLightEnd = Color(0xFFF0EAFB)       // Pale lilac
val SmallCardIconBgDark = Color(0xFF6C5CE7)     // Purple for icon bg (dark)
val SmallCardIconBgLight = Color(0xFF7B61FF)    // Purple for icon bg (light)

// Premium Membership Palette
val PremiumGradientStart = Color(0xFF7C4DFF) // Deep Violet
val PremiumGradientEnd = Color(0xFF651FFF) // Vibrant Purple
val PremiumBorder = Color(0xFFB388FF)
val PremiumGold = Color(0xFFFFD700)
val PremiumOnGradient = Color(0xFFFFFFFF)
val PremiumShadowNeutral = Color(0xFF000000)

private val DarkOnPrimary = Color(0xFF24114C)
private val DarkPrimaryContainer = Color(0xFF2D243F)
private val DarkOnPrimaryContainer = Color(0xFFF0E9FF)
private val DarkSecondaryContainer = Color(0xFF3D3159)
private val DarkOnSecondaryContainer = Color(0xFFE2D8FF)
private val DarkTertiary = Color(0xFFFFB74D)
private val DarkOnTertiary = Color(0xFF24114C)
private val DarkTertiaryContainer = Color(0xFF533B2A)
private val DarkErrorContainer = Color(0xFF4B1E20)
private val DarkOnErrorContainer = Color(0xFFFFAAA0)

private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFE5DEFF)
private val LightOnPrimaryContainer = Color(0xFF21005D)
private val LightSecondary = Color(0xFF6750A4)
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFE8DEF8)
private val LightOnSecondaryContainer = Color(0xFF1D192B)
private val LightTertiary = IncomeGreen
private val LightOnTertiary = Color(0xFF07361A)
private val LightTertiaryContainer = Color(0xFFD8F3DD)
private val LightOnTertiaryContainer = Color(0xFF0F2417)
private val LightErrorContainer = Color(0xFFFFDAD6)
private val LightOnErrorContainer = Color(0xFF410002)

internal val ExpenseTrackerDarkColorScheme: ColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = PurpleAccent,
    onSecondary = DarkOnPrimary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    error = ExpenseRed,
    onError = DarkOnPrimary,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = DividerDark,
    outlineVariant = DividerDark,
    scrim = BackgroundDark
)

internal val ExpenseTrackerLightColorScheme: ColorScheme = lightColorScheme(
    primary = PurplePrimaryLight,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = ExpenseRed,
    onError = LightOnPrimary,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = CardLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerLight,
    outlineVariant = DividerLight,
    scrim = BackgroundLight
)

/** Detects dark mode from the active color scheme (works with app-level theme, not just system). */
val ColorScheme.isDark: Boolean
    get() = background.luminance() < 0.5f

val ColorScheme.featureGateLock: Color
    get() = FeatureGateLockGold

/**
 * Glyph colour for the brand-gradient Add affordances — the docked FAB and the
 * reveal handle that replaces the bar while it is hidden.
 *
 * White in dark mode, black in light mode. Deliberately NOT `onPrimary`, which is
 * the scheme's own pairing for the flat brand fill: that pairing flips to a dark ink
 * in dark mode, which leaves the glyph muddy against the saturated gradient this
 * affordance actually uses. Following the theme's own light/dark polarity instead
 * keeps the same white-on-purple read the mock calls for in dark mode, and the same
 * black-on-purple read that stays legible once the light scheme's paler gradient
 * ends land under it.
 */
val ColorScheme.onBrandGradient: Color
    get() = if (isDark) Color.White else Color.Black
