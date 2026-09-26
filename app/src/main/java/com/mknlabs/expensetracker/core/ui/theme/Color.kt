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

// Amount inks for white surfaces. Same hue family as the dark pair above, deepened
// until both clear 4.5:1 on white.
val IncomeInkLight = Color(0xFF15803D)  // deep emerald
val ExpenseInkLight = Color(0xFFDC2626) // crimson

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

// ── Light mode: the premium finance palette ───────────────────────────────────
//
// A soft grey field carrying floating white cards, separated by a hairline rather
// than by a heavy shadow. These literals are the design spec itself rather than a
// hand-tuned approximation of one, so they are the one place in the file where the
// exact hex is the requirement; every light surface in the app reads from here.
val BackgroundLight = Color(0xFFF7F8FA)   // app background, every screen

val SurfaceLight = Color(0xFFFFFFFF)      // primary card surface
val CardLight = Color(0xFFF1F2F4)         // chips, segmented controls, search bars

// The three light weights, in the corrected order. The old third weight was #9CA3AF,
// which measures 2.54:1 on white — below AA, and not fixable by choosing a paler grey,
// because no colour lighter than the second weight clears 4.5:1 on both the white card
// and the #F7F8FA field. The tiers move down one rung instead: secondary takes #5B6270
// (6.13:1 on a card) and tertiary the #6B7280 secondary used to hold (4.83:1 on a card,
// 4.55:1 on the field). Light has room for three weights in this order and no more.
val TextPrimaryLight = Color(0xFF1F2937)
val TextSecondaryLight = Color(0xFF5B6270)
val TextTertiaryLight = Color(0xFF6B7280)

val DividerLight = Color(0xFFE8EBEF)      // card outline, dividers, field borders

// The card lift: black at 5–6% over that grey field, split into the two components
// Compose blends separately (ambient all round, spot below) so the two can be tuned
// apart while both stay inside the spec's 4–6% band.
val CardShadowAmbientLight = Color(0x0D000000) // 5%
val CardShadowSpotLight = Color(0x0F000000)    // 6%

// Nav bar hairline. A hair warmer than the card outline above, because the capsule
// floats over live content instead of sitting in the page's card grid.
val NavEdgeLight = Color(0xFFECEEF2)

// Third text weight, and the one the hero's metric labels read. #77777C measured
// 4.44:1 on the #0A0A0A field, just under AA, so this steps up to the lightest ink that
// clears it: 5.91:1 on the field and 5.17:1 on a card.
val TextTertiaryDark = Color(0xFF8A8C95)

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
// Light values below are aliases of the palette tokens above rather than hexes of
// their own. The hero no longer paints a surface in light mode — it is a white card —
// so these are now only the inks *inside* it, and inks that are aliases cannot drift
// away from the text colours every other light surface uses.
val CashFlowDateTextLight = TextSecondaryLight

val CashFlowPillBgDark = Color(0xFF231D33)
val CashFlowPillBgLight = CardLight
val CashFlowPillBorderDark = Color(0xFF3B3254)
val CashFlowPillBorderLight = DividerLight
val CashFlowPillTextDark = Color(0xFFDDD8EC)
val CashFlowPillTextLight = TextSecondaryLight

val CashFlowExpenseAmountDark = Color(0xFFFF7262) // Coral salmon red
val CashFlowExpenseAmountLight = ExpenseInkLight

val CashFlowIncomeAmountDark = Color(0xFF5DE290)  // Radiant mint green
val CashFlowIncomeAmountLight = IncomeInkLight

val CashFlowLabelDark = Color(0xFF9089A4)
val CashFlowLabelLight = TextSecondaryLight

val CashFlowNetBalanceBgDark = Color(0xFF161224)
val CashFlowNetBalanceBgLight = CardLight
val CashFlowNetBalanceBorderDark = Color(0xFF2E2644)
val CashFlowNetBalanceBorderLight = DividerLight
val CashFlowNetBalanceLabelDark = Color(0xFFB5ADCA)
val CashFlowNetBalanceLabelLight = TextSecondaryLight
val CashFlowNetBalanceAmountDark = Color(0xFFFFFFFF)
val CashFlowNetBalanceAmountLight = TextPrimaryLight

// ── Cash Flow hero: a neutral card carrying the brand as an accent ─────────────
//
// The hero was the one surface in the app painted entirely in brand violet, which made
// the largest object on the first screen also the loudest, and forced light mode to
// invent a second dark-violet ramp to match it. It is now the same card as everything
// else in both themes, with the brand in three small places instead: a 3dp rail down the
// leading edge, one bloom off the top-trailing corner, and the amount inks.
//
// That is roughly 9% of the card's area in brand colour where it used to be ~100%. And
// because the surface no longer differs between themes, the inks no longer need a set of
// their own either — they are the shared text and semantic tokens, so the hero cannot
// drift away from the rest of the app.
val HeroSurfaceDark = Color(0xFF1A1A20)        // 1.14:1 over the #0A0A0A field
val HeroSurfaceLight = Color(0xFFFFFFFF)       // the app's card white
val HeroOutlineDark = Color(0xFF2A2A31)        // 1.22:1 on the card
val HeroOutlineLight = Color(0xFFE8EBEF)       // the app's hairline

// The rail's own fill. Deliberately not `primary`: that is fully saturated at
// hsl(250,100,69), the luminance where neither black nor white ink clears 4.5:1 on it,
// so a fill using it cannot be made accessible — only replaced. These are the two ends
// of the CTA ramp, which carry white ink at 6.25:1 and 8.51:1 in dark and 5.10:1 in
// light, and which is what the app's filled buttons use.
val HeroRailStartDark = Color(0xFF5838FA)      // purple-600, white ink 6.25:1
val HeroRailEndDark = Color(0xFF3713EC)        // purple-700, white ink 8.51:1
val HeroRailStartLight = Color(0xFF6A4DFF)     // 5.10:1 with white ink
val HeroRailEndLight = Color(0xFF5B45D6)       // the deeper end of the same ramp

// One bloom, at the accent's own hue. 22% over a near-black card reads as light
// catching the corner; the same alpha on white would read as a stain, so light takes
// 10% and the accent stands at full strength only in the rail.
val HeroBloomDark = Color(0x389E84FF)          // accent, 22% alpha
val HeroBloomLight = Color(0x1A6A4DFF)         // accent, 10% alpha

// The net-balance inset steps one rung off the card surface and the period pill two, in
// each theme's own direction: shallower in light, where the card is the lighter of the
// pair, and deeper in dark, where it is the darker.
val HeroInsetDark = Color(0xFF22222A)
val HeroInsetLight = Color(0xFFFAFAFC)
val HeroInsetOutlineDark = Color(0xFF2A2A31)
val HeroInsetOutlineLight = Color(0xFFE8EBEF)
val HeroPillDark = Color(0xFF242428)
val HeroPillLight = Color(0xFFF5F5F5)
val HeroPillOutlineDark = Color(0xFF2D2D31)
val HeroPillOutlineLight = Color(0x14000000)   // black 8%

// Bottom Navigation Bar Palette (indexmockup.html)
val NavOnDark = Color(0xFFBFA6FF)     // --nav-on: #BFA6FF
val NavOffDark = Color(0xFF7A778C)    // --nav-off: #7A778C
val NavPillDark = Color(0x297A52FF)   // --nav-pill: rgba(122,82,255,.16)

val NavOnLight = Color(0xFF6C52EE)    // Vibrant lilac/violet
// An alias of the third ink weight rather than a muted slate of its own: an unselected
// destination is exactly that weight, and an alias cannot drift away from the text
// colour every other light surface uses.
val NavOffLight = TextTertiaryLight
val NavPillLight = Color(0x1F6C52EE)  // rgba(108,82,238,.12)

// SmallHomeCard / Quick Action Palette (indexmockup.html)
val SmallCardDarkStart = Color(0xFF1B1530)      // --qcard-bg
val SmallCardDarkEnd = Color(0xFF121019)
val SmallCardBorderDark = Color(0x12FFFFFF)     // rgba(255, 255, 255, 0.07)

val SmallCardLightStart = Color(0xFFFAF7FF)
val SmallCardLightEnd = Color(0xFFF0EAFB)
val SmallCardBorderLight = Color(0x1F7A52FF)    // rgba(122, 82, 255, 0.12)

val SmallCardIconBgDark = Color(0x2E7A52FF)     // --qicon-bg: rgba(122, 82, 255, 0.18)
val SmallCardIconBgLight = Color(0x246C52EE)    // rgba(108, 82, 238, 0.14)
val SmallCardIconDark = Color(0xFFBFA6FF)       // --qicon-c: #BFA6FF
val SmallCardIconLight = Color(0xFF6C52EE)

val SmallCardLabelDark = Color(0xFFA5A1B8)      // --t-secondary: #A5A1B8 (indexmockup.html)
val SmallCardLabelLight = TextSecondaryLight     // Light mode muted slate

// Chip / Filter Pill Palette (indexmockup.html)
val ChipBgSelectedDark = Color(0x337A52FF)       // --chip-on-bg: rgba(122, 82, 255, 0.20)
val ChipBorderSelectedDark = Color(0x737A52FF)   // --chip-on-bd: rgba(122, 82, 255, 0.45)
val ChipTextSelectedDark = Color(0xFFBFA6FF)     // --chip-on-c: #BFA6FF

val ChipBgSelectedLight = Color(0x296C52EE)      // rgba(108, 82, 238, 0.16)
val ChipBorderSelectedLight = Color(0x666C52EE)  // rgba(108, 82, 238, 0.40)
// Was #6C52EE, the same value as the tint it sits on: a chip whose selected fill is 16%
// of its own label colour composites to #E7E3FC over a white card, and the label then
// measured 4.12:1 against it — below AA, and invisible on the field where the same tint
// composites to #E1DDF8. The label is now a shade deeper than the tint it labels, which
// is the whole reason this pair exists rather than one token doing both jobs: 5.14:1 on
// the card, 6.05:1 on the field.
val ChipTextSelectedLight = Color(0xFF5B45D6)

val ChipBgUnselectedDark = Color(0x0FFFFFFF)     // --chip-bg: rgba(255, 255, 255, 0.06)
val ChipBorderUnselectedDark = Color(0x1AFFFFFF) // --chip-bd: rgba(255, 255, 255, 0.10)
val ChipTextUnselectedDark = Color(0xFFA5A1B8)   // --chip-c: #A5A1B8

val ChipBgUnselectedLight = Color(0x0A000000)   // rgba(0, 0, 0, 0.04)
val ChipBorderUnselectedLight = Color(0x14000000) // rgba(0, 0, 0, 0.08)
val ChipTextUnselectedLight = Color(0xFF746B8B) // #746B8B

// Premium Membership Palette
val PremiumGradientStart = Color(0xFF7C4DFF) // Deep Violet
val PremiumGradientEnd = Color(0xFF651FFF) // Vibrant Purple
val PremiumBorder = Color(0xFFB388FF)
val PremiumGold = Color(0xFFFFD700)
val PremiumOnGradient = Color(0xFFFFFFFF)
val PremiumShadowNeutral = Color(0xFF000000)

// Pro Pass Membership Palette
//
// A Pro Pass is a grant, not a charge, and a holder has to be able to tell the two apart
// at a glance. The pass therefore carries a surface of its own instead of the
// subscription's violet. The white ink and the gold accent are shared with the premium
// card, so the two still read as one family — the surface colour plus the badge say which
// state this is, without the user having to read the copy to find out.
val ProPassGradientStart = Color(0xFF1E5FD0) // Deep Blue
val ProPassGradientEnd = Color(0xFF2E9BD6) // Sky Blue
val ProPassBorder = Color(0xFF7CC4F5)
val ProPassOnGradient = Color(0xFFFFFFFF)

// The ink on the brand fill in dark mode. Was #24114C, which measures 3.97:1 on
// PurplePrimary #7B61FF and so failed AA on every filled primary button in the theme.
// That fill is intrinsically hostile — at 4.20:1 with white and 4.12:1 even with the card
// colour, only a genuinely black ink clears it — so the ink goes as dark as the palette
// goes and lands at 4.71:1. This token also feeds onSecondary and onError, where the same
// change measures 11.61:1 and 7.13:1, so neither regresses.
private val DarkOnPrimary = Color(0xFF0A0A0A)
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
// The dark theme's inks are tuned for a near-black field; on white they fall to
// roughly 2.5:1 (mint) and 4.0:1 (coral), which is not enough for the one number a
// row exists to show. Both are deepened here until they clear 4.5:1 on a white card,
// keeping the hue family so income still reads green and expense still reads red.
private val LightTertiary = IncomeInkLight
private val LightOnTertiary = Color(0xFFFFFFFF)
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
    error = ExpenseInkLight,
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
