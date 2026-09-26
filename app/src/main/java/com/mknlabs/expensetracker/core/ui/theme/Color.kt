package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance


val transparent= Color.Transparent;
val PurplePrimary = Color(0xFF7B61FF)


val BackgroundDark = Color(0xFF0A0A0A)

val SurfaceDark = Color(0xFF141418)   // spec --s1


val TextPrimaryDark = Color(0xFFF2F2F5)   // spec --tp
val TextSecondaryDark = Color(0xFFA8A8B3) // spec --ts

val DividerDark = Color(0xFF2A2A31)   // spec --line

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

val FeatureGateLockGold = Color(0xFFFFC857)

// Premium StatsCard Gradient
val PremiumCardDarkStart = Color(0xFF2B2349)   // Violet, top-left
val PremiumCardDarkCenter = Color(0xFF1C1632) // Violet-charcoal, centre
val PremiumCardDarkEnd = Color(0xFF100C1F)     // Near-black plum, bottom-right

// The premium card's glow, border, light-theme and label sub-palettes were retired with
// the inspect-and-replace pass: the membership surface is one gradient (PremiumGradient*)
// plus its gold and its lilac border, and every other member of the old family had no
// reader. The dark trio below survives because the goals screen still paints a goal card
// with it; it is an app surface, not a spec token, and is kept apart from the membership
// ramp for that reason.

// ── Retired: the Cash Flow palette (indexmockup.html) ────────────────────────
//
// This was the violet-gradient hero of the old mock, and it was retired when the hero was
// rebuilt as a neutral card carrying the brand only in a rail, a bloom and the amount
// inks. Nothing survives to replace it token-for-token, because the neutral hero is not a
// recoloured version of the old surface — it is a different surface, and it reads from the
// spec's own card ladder (HeroSurface*, HeroOutline*) and the shared text and semantic
// tokens instead. The whole family is deleted rather than left as dead values that a
// future screen could pick up and quietly revive the gradient with.

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

// ── Bottom navigation (retargeted from indexmockup.html to the spec) ─────────
//
// The old values were a lilac pair of the previous mock's own making: #BFA6FF on and
// #7A778C off in dark, #6C52EE on in light. The spec draws the same two states with the
// tokens it already names — the active destination is `--accent`, an inactive one is the
// third ink weight `--tt` — so both themes now read those instead. The pill tint was
// #7A52FF/.16, a violet the spec never uses; its active pill is `--accentSoft`, already
// available as such, and nothing in the app referenced the token, so both pill values are
// retired rather than retargeted to a colour no call site was asking for.
val NavOnDark = Color(0xFF9E84FF)     // spec --accent, dark
val NavOffDark = TextTertiaryDark     // spec --tt (#8A8C95), dark

val NavOnLight = Color(0xFF6A4DFF)    // spec --accent, light
val NavOffLight = TextTertiaryLight   // spec --tt (#6B7280), light

// ── Chip parts, retargeted from indexmockup.html to the spec ────────────────
//
// The retired family was twelve tokens describing four parts of a chip in two states, and
// it described them in the old mock's violet (#7A52FF / #6C52EE, on 12-20% tints). The
// spec expresses the same four parts once each, with values of their own: `--chip`,
// `--chipLine`, `--chipSel`, `--chipInk`, `--chipInkOff`. The selected pair is
// chipSelected/chipSelectedInk above; the unselected three are here. They are aliases of
// the values the spec names rather than new literals, so a chip and the hero pill beside
// it cannot drift apart.
val ColorScheme.chip: Color
    get() = if (isDark) HeroPillDark else HeroPillLight              // spec --chip

val ColorScheme.chipOutline: Color
    get() = if (isDark) HeroPillOutlineDark else HeroPillOutlineLight // spec --chipLine

val ColorScheme.chipInkOff: Color
    get() = if (isDark) TextSecondaryDark else TextSecondaryLight    // spec --chipInkOff

// ── The quick-action card (retargeted from indexmockup.html) ────────────────
//
// The old family painted a violet-tinted card, but the spec's surfaces are neutral and
// the brand lives in the icon tile instead: `--accentSoft` behind the glyph and `--accent`
// on it. So the card itself now reads the shared card ladder and only the tile is tinted,
// which is what keeps the home row off the brand budget. The two washes are kept apart by
// theme because one alpha cannot serve both — 14% is a wash on black and a stain on white.
internal val AccentSoftDark = Color(0x249E84FF)   // accent 14%
internal val AccentSoftLight = Color(0x1A6A4DFF)  // accent 10%

val ColorScheme.accentSoft: Color
    get() = if (isDark) AccentSoftDark else AccentSoftLight

// Premium Membership Palette
// Retargeted to the mock's master reference table, which is authoritative over its own
// CSS: the card fill is the CTA ramp `#5838FA -> #3713EC`, not the `#5030E8 -> #3713EC`
// the `.premium` rule happens to paint. Gold and the lilac border are unchanged.
val PremiumGradientStart = Color(0xFF5838FA) // spec Premium = --cta
val PremiumGradientEnd = Color(0xFF3713EC)   // spec Premium = --cta2
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
val ProPassGradientStart = Color(0xFF0F3B86) // Deep Blue, darkened to hold PremiumGold
val ProPassGradientEnd = Color(0xFF1A638A) // was #2E9BD6: sky blue cannot carry gold OR white
val ProPassBorder = Color(0xFF7CC4F5)
val ProPassOnGradient = Color(0xFFFFFFFF)

// The ink on the brand fill in dark mode. Was #24114C, which measures 3.97:1 on
// PurplePrimary #7B61FF and so failed AA on every filled primary button in the theme.
// That fill is intrinsically hostile — at 4.20:1 with white and 4.12:1 even with the card
// colour, only a genuinely black ink clears it — so the ink goes as dark as the palette
// goes and lands at 4.71:1. This token also feeds onSecondary and onError, where the same
// change measures 11.61:1 and 7.13:1, so neither regresses.
// Retargeted to the spec's own tokens (mock-design-system.html). The old values were the
// legacy violet palette's (#7B61FF fill, #CDBDFF, M3-default containers), none of which
// the spec names. The mapping now reads:
//   primary    --cta       #5838FA      a FILL (label white)
//   onPrimary  onCta       #FFFFFF
//   secondary  --accent    #9E84FF      the brand INK
//   *Container --accentSoft / --chipSel / --savings, i.e. tints DERIVED from those tokens,
//              because the spec defines no container role of its own.
private val DarkOnPrimary = Color(0xFFFFFFFF)            // spec onCta
private val DarkPrimaryContainer = Color(0x249E84FF)     // spec --accentSoft, dark
private val DarkOnPrimaryContainer = Color(0xFF9E84FF)   // spec --accent
private val DarkSecondaryContainer = Color(0xFF2C283F)   // spec --chipSel
private val DarkOnSecondaryContainer = Color(0xFF9E84FF) // spec --chipInk
private val DarkTertiary = Color(0xFF3DDC97)             // spec --income
private val DarkOnTertiary = Color(0xFF0A0A0A)
private val DarkTertiaryContainer = Color(0x245EEAD4)    // spec --savings 14%
private val DarkErrorContainer = Color(0xFF4B1E20)       // no spec slot: derived expense tint
private val DarkOnErrorContainer = Color(0xFFFFAAA0)

private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0x1A6A4DFF)    // spec --accentSoft, light
private val LightOnPrimaryContainer = Color(0xFF5B45D6)  // spec --chipInk
private val LightSecondary = Color(0xFF6A4DFF)           // spec --accent, light
private val LightOnSecondary = Color(0xFFFFFFFF)
private val LightSecondaryContainer = Color(0xFFEDEAFF)  // spec --chipSel
private val LightOnSecondaryContainer = Color(0xFF5B45D6) // spec --chipInk
// The dark theme's inks are tuned for a near-black field; on white they fall to
// roughly 2.5:1 (mint) and 4.0:1 (coral), which is not enough for the one number a
// row exists to show. Both are deepened here until they clear 4.5:1 on a white card,
// keeping the hue family so income still reads green and expense still reads red.
private val LightTertiary = IncomeInkLight
private val LightOnTertiary = Color(0xFFFFFFFF)
private val LightTertiaryContainer = Color(0x1A0F766E)   // spec --savings 10%
private val LightOnTertiaryContainer = Color(0xFF0F2417)
private val LightErrorContainer = Color(0xFFFFDAD6)
private val LightOnErrorContainer = Color(0xFF410002)

internal val ExpenseTrackerDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF5838FA),   // spec --cta, dark (white ink 6.25:1)
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = Color(0xFF9E84FF), // spec --accent, dark (the brand ink)
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
    surfaceVariant = Color(0xFF26262E), // spec --menu / --track, dark
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
 * White in BOTH themes. The gradient under it is [brandGradient]/[fabGradient], which is
 * now the mock's filled-CTA ramp — `#5838FA → #3713EC` in dark, `#6A4DFF → #5B45D6` in
 * light. Both are deep purples, and the darker end of the light ramp would fall under
 * AA for a black glyph, so the polarity no longer flips with the theme. Aliasing
 * [onCta] rather than repeating the literal keeps the glyph and the label of every
 * filled brand control on one value: white measures 6.25:1 on the dark end and 5.10:1
 * on light's.
 *
 * Deliberately NOT `onPrimary`: that slot now carries the same white [OnCta], because the
 * retarget moved `primary` onto the CTA fill and took its label with it — but this token
 * names the glyph role, so a future `primary` change cannot silently repaint the FAB.
 */
val ColorScheme.onBrandGradient: Color
    get() = OnCta

// ── Categorical chart palette ────────────────────────────────────────────────
// Chart slices, in draw order. The order is load-bearing: the first tone is the
// brand accent so the largest slice still reads as the app's own colour, and every
// later tone is picked to stay clear of each earlier one under red-green deficiency,
// not merely under normal vision.
//
// That constraint is what shapes the last entry of each ramp. Under red-green
// deficiency only blue-versus-yellow survives, so a second blue cannot be told from
// the brand purple by hue at all — only by lightness. So the dark ramp takes a pale
// sky and the light ramp a deep navy, pulling in opposite directions to put as much
// lightness as possible between themselves and #9E84FF / #6A4DFF. Picking two blues
// that merely differ in hue is the trap the old palette fell into: its dark trio was
// two purples, which collapsed to a separation of 7.1 under deuteranopia.
//
// Contrast on each theme's card, in the same order, all clear of the 3:1 a chart
// slice needs: dark 5.9 11.7 10.4 6.4 13.1 against #1A1A20, and light 5.1 5.5 5.0
// 10.4 4.6 against white.
internal val ChartSeriesDark = listOf(
    Color(0xFF9E84FF), // purple — the brand accent
    Color(0xFF5EEAD4), // teal
    Color(0xFFFBBF24), // amber
    Color(0xFFFB7185), // rose
    Color(0xFFBAE6FD), // pale sky — the lightness lever against the purple
)

internal val ChartSeriesLight = listOf(
    Color(0xFF6A4DFF), // purple — the brand accent
    Color(0xFF0F766E), // teal
    Color(0xFFB45309), // amber
    Color(0xFF1E3A8A), // deep navy — the same lever, pushed the other way
    Color(0xFFDB2777), // magenta
)

// The tone every slice past the ramp takes. The ramp is only five tones long — the most
// red-green deficiency lets these charts tell apart — but the "view all" sheet lists
// every category the user has, and there is no upper bound on that, because categories
// are user-created. Arithmetic-wrapping the index back to the brand purple would make
// the sixth category wear the first category's colour and leave the legend unable to say
// which was which, which is the trap the ramp above was built to avoid. So the overflow
// takes one neutral instead of a recycled hue: a desaturated grey separates from all
// five by chroma rather than hue, and chroma is what survives red-green deficiency.
//
// Both are aliases of the tertiary ink rather than new literals, the same way NavOffLight
// aliases it, so the neutral cannot drift away from the palette's own greys.
internal val ChartOtherLight = TextTertiaryLight  // 4.83:1 on white,    min dE76 30.0 to the ramp
internal val ChartOtherDark = TextTertiaryDark    // 5.17:1 on the card, min dE76 34.0 to the ramp

/**
 * The categorical palette for chart slices, in draw order.
 *
 * Index it with the slice's own position. The list is also what defines how many
 * series these charts can actually tell apart: a caller that draws more slices than
 * there are tones here has to fold the remainder into a group of its own rather than
 * let the index wrap, because wrapping would draw two different series in one colour
 * and the legend would then be unable to say which was which.
 */
val ColorScheme.chartSeries: List<Color>
    get() = if (isDark) ChartSeriesDark else ChartSeriesLight

/**
 * The tone a slice takes once the ramp is exhausted, i.e. for every category or payment
 * type past the fifth. Callers must branch on the index rather than let it wrap: see
 * [chartSeries] for why a recycled hue is the one thing this palette cannot do.
 */
val ColorScheme.chartOther: Color
    get() = if (isDark) ChartOtherDark else ChartOtherLight

// ── Accent as ink ────────────────────────────────────────────────────────────
// The brand purple used as a GLYPH or label on a neutral control — a different job from
// `primary`, which is now the CTA FILL. The mock keeps the two roles apart (`--accent` is
// ink, `--cta` is fill) and they do not agree: the accent is the lighter #9E84FF in dark,
// and light's operator glyph #6A4DFF in light. In dark it must be the accent and not the
// fill, because a #5838FA glyph on a dark control is only 3.17:1 — under the 3:1 a large
// glyph needs — while #9E84FF clears it. Nothing else moves: an ink, never a fill.
internal val AccentInkLight = Color(0xFF6A4DFF)
internal val AccentInkDark = Color(0xFF9E84FF)

val ColorScheme.accentInk: Color
    get() = if (isDark) AccentInkDark else AccentInkLight

// ── CTA fill ──────────────────────────────────────────────────────────────
// The mock's --cta is a fill, never an ink: #5838FA in dark, and byte-identical to light's
// primary so nothing in light moves. Its label is white, which measures 6.25:1 against the
// dark fill -- the figure the spec's own matrix records for this pair.
//
// The ink travels with the fill: the scheme's dark onPrimary is now white (#FFFFFF), which
// is the label these runs carry, and the two are declared together so a change to one
// cannot leave the other behind. #5838FA takes white at 6.25:1, where the near-black ink it
// used to carry drops to 3.17:1 — which is why the label moved when the fill did.
internal val CtaLight = Color(0xFF6A4DFF)
internal val CtaDark = Color(0xFF5838FA)
internal val OnCta = Color(0xFFFFFFFF)

val ColorScheme.cta: Color
    get() = if (isDark) CtaDark else CtaLight

val ColorScheme.onCta: Color
    get() = OnCta

// -- Brand budget, per screen -------------------------------------------------
// The spec caps brand colour at 30% of a screen's area, and its own accounting lands
// the home screen at 15-25%: hero rail + bloom ~9%, FAB ~2%, active nav pill ~3%,
// selected chip ~2%, goal progress ~2%. The token layer is what enforces that:
//
//   * the field, every card and every sheet are NEUTRAL in both themes -- no surface
//     token carries brand at full bleed, so the brand never arrives as a region;
//   * the brand reaches a screen only through small roles -- `cta` (a control's 600/700
//     fill), `accentInk` (a glyph or label), `brandGradient` (a control's ramp) and the
//     handful of blooms (`HeroBloom*`, the voice radial) whose alphas keep them a wash;
//   * `primary` now IS the CTA fill (#5838FA dark / #6A4DFF light), matching Material's
//     "primary is a fill" contract, so a control that reads it without an override is
//     still on spec. The saturated accent (#9E84FF) is `secondary`/`accentInk` -- an INK,
//     never a fill -- which is what stops the largest object on a screen from also being
//     the loudest.
//
// A new full-bleed brand surface is the one thing this budget forbids. The sanctioned
// exception is the membership card family (Premium / Pro Pass), which stays a dark brand
// surface in BOTH themes by design.

/** The spec's ceiling on brand-coloured area for a single screen, as a fraction. */
internal const val BRAND_AREA_BUDGET = 0.30f

// ── Budget health ────────────────────────────────────────────────────────────
// Green on track, amber near the limit, deep red over it — the traffic-light read the
// mock asks for, so the state of a bar is legible before any of its text is read.
//
// The over state is red-900 rather than a brighter red on purpose. Under red-green
// deficiency amber and red collapse together — the mock measures them 4.4 apart under
// deuteranopia — and every lighter red that separates from the amber above fails 3:1
// on the light track. Dropping the over state this far is what buys the separation
// back, which is why the three tones are not simply "green, orange, red".
//
// Contrast on the light track #E8EBEF is 4.19 / 4.20 / 8.38, and on the dark track
// #26262E it is 7.81 / 8.99 / 5.43. The bar is drawn as a gradient fading to 80% alpha,
// so its far end is the weakest point; there the pairs still measure 3.09 / 3.13 / 5.44
// light and 5.48 / 6.25 / 3.97 dark, all clear of the 3:1 a UI element needs.
internal val BudgetOnTrackDark = Color(0xFF34D399)
internal val BudgetNearLimitDark = Color(0xFFFBBF24)
internal val BudgetOverDark = Color(0xFFF87171)

internal val BudgetOnTrackLight = Color(0xFF15803D)
internal val BudgetNearLimitLight = Color(0xFFB45309)
internal val BudgetOverLight = Color(0xFF7F1D1D)

val ColorScheme.budgetOnTrack: Color
    get() = if (isDark) BudgetOnTrackDark else BudgetOnTrackLight

val ColorScheme.budgetNearLimit: Color
    get() = if (isDark) BudgetNearLimitDark else BudgetNearLimitLight

val ColorScheme.budgetOver: Color
    get() = if (isDark) BudgetOverDark else BudgetOverLight

// -- Surface and control roles the scheme lacked ------------------------------
// The mock's two token sets are not fully expressible through Material's own roles: it
// names a third surface rung, a track, a second outline weight, a disabled ink line and
// three control inks that have no ColourScheme slot. Each is added here as a named role
// reading that theme's literal, so the mock's tables and this file can be diffed token
// for token. The literals, in the order they appear below:
//
//   mock DARK   --s3 #1E1E23   --menu/--track #26262E   --lineStrong #6A6A7E
//               --dis #6E6E7A  --transfer #60A5FA        --debt #FB7185
//               --chipSel #2C283F
//   mock LIGHT  --s3 #FAFAFC   --track #F1F2F4          --lineStrong #A7B0BC
//               --dis #A8B0BB  --transfer #1D4ED8        --debt #BE123C
//               --chipSel #EDEAFF

// Sheet: the container a dialog or bottom sheet is painted on, one rung off the card.
// Dark steps UP its ladder from the #1A1A20 card (#1E1E23); light steps DOWN from the
// #FFFFFF card (#FAFAFC), which is why the two are not the same relationship to their
// own card and are therefore two values rather than one name.
internal val SheetDark = Color(0xFF1E1E23)     // spec --s3, dark
internal val SheetLight = Color(0xFFFAFAFC)    // spec --s3, light

val ColorScheme.sheet: Color
    get() = if (isDark) SheetDark else SheetLight

// The empty part of a progress bar. Named rather than borrowed from surfaceVariant
// because the spec gives it a value of its own in each theme, and a bar whose track moved
// with the card would stop reading as a track.
internal val TrackDark = Color(0xFF26262E)     // spec --track / --menu
internal val TrackLight = Color(0xFFF1F2F4)    // spec --track

val ColorScheme.track: Color
    get() = if (isDark) TrackDark else TrackLight

// The spec names --lineStrong (#6A6A7E dark / #A7B0BC light) for the boundary a control
// needs when a hairline cannot carry one. It is intentionally NOT exposed as a role: no
// control in the app draws that edge, and the spec's own matrix records light failing 3:1
// on it (2.19) -- so light is told to use a fill or a 2dp stroke instead. Declaring it
// would leave a token whose only reader is a future screen that should reach for a fill.

// Disabled: exempt from the text-contrast rule (WCAG 1.4.3 exempts inactive controls), so
// these are the spec's own values rather than an ink picked to clear a ratio. Kept clear
// of each theme's enabled inks so a disabled control never reads as a live one.
internal val DisabledDark = Color(0xFF6E6E7A)   // spec --dis
internal val DisabledLight = Color(0xFFA8B0BB)  // spec --dis

val ColorScheme.disabled: Color
    get() = if (isDark) DisabledDark else DisabledLight

// The two amount inks the summary rows did not have: transfer and debt. Same treatment as
// income and expense -- a saturated tone on the dark field, deepened until it clears
// 4.5:1 on white in light. Debt's dark value is the same rose the chart ramp already uses.
internal val TransferDark = Color(0xFF60A5FA)   // 7.79:1 on #0A0A0A
internal val TransferLight = Color(0xFF1D4ED8)  // 6.70:1 on #FFFFFF
internal val DebtDark = Color(0xFFFB7185)       // 7.36:1 on #0A0A0A
internal val DebtLight = Color(0xFFBE123C)      // 6.29:1 on #FFFFFF

val ColorScheme.transfer: Color
    get() = if (isDark) TransferDark else TransferLight

val ColorScheme.debt: Color
    get() = if (isDark) DebtDark else DebtLight

// Selected chip: the fill and the ink are a pair, and the mock states both. Dark's fill is
// its own grey-violet rather than a tint of the accent, and the label on it is the accent
// ink (4.86:1). Light's fill is a pale lavender carrying the deeper #5B45D6 the palette
// already uses for exactly this job (5.45:1). Declared together so a chip cannot ship with
// one theme's fill under the other theme's ink.
internal val ChipSelectedDark = Color(0xFF2C283F)       // spec --chipSel
internal val ChipSelectedLight = Color(0xFFEDEAFF)      // spec --chipSel
// The inks are aliases rather than new literals: dark's chip ink is the accent ink, and
// light's is the deeper violet the palette declares for a label on a tint. An alias
// cannot drift away from the value it names.
internal val ChipSelectedInkDark = AccentInkDark        // spec --chipInk = --accent
internal val ChipSelectedInkLight = Color(0xFF5B45D6)   // spec --chipInk

val ColorScheme.chipSelected: Color
    get() = if (isDark) ChipSelectedDark else ChipSelectedLight

val ColorScheme.chipSelectedInk: Color
    get() = if (isDark) ChipSelectedInkDark else ChipSelectedInkLight

// ── Savings, invest, menu and glow ──────────────────────────────────────────
// Four more spec tokens Material has no slot for. `--savings` and `--invest` complete the
// spec's amount-ink set beside income/expense/transfer/debt (the goals and chart surfaces
// are where they are read). `--menu` is the surface a popup menu takes: in dark it is the
// same #26262E as the track, in light it is the card white rather than the track's grey,
// which is why it is its own role and not an alias of `track`. `--glow` is the accent wash
// the hero bloom paints (Gradient.heroBloom reads this role). `--menu` and `--glow` have
// live readers; `--savings` and `--invest` are declared ahead of their surfaces, exactly as
// `transfer`/`debt` are — the goals and chart code will read them when it distinguishes
// those buckets, and no screen invents its own green for them in the meantime.
internal val SavingsDark = Color(0xFF5EEAD4)   // spec --savings, dark
internal val SavingsLight = Color(0xFF0F766E)  // spec --savings, light
internal val InvestDark = Color(0xFFFBBF24)    // spec --invest, dark
internal val InvestLight = Color(0xFFB45309)   // spec --invest, light
internal val MenuDark = Color(0xFF26262E)      // spec --menu, dark
internal val MenuLight = Color(0xFFFFFFFF)     // spec --menu, light

val ColorScheme.savings: Color
    get() = if (isDark) SavingsDark else SavingsLight

val ColorScheme.invest: Color
    get() = if (isDark) InvestDark else InvestLight

val ColorScheme.menu: Color
    get() = if (isDark) MenuDark else MenuLight

val ColorScheme.glow: Color
    get() = if (isDark) HeroBloomDark else HeroBloomLight
