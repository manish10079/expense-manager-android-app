package com.mknlabs.expensetracker.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/**
 * The colour of every category and payment method the app ships with.
 *
 * ## Why this is not in `Color.kt`
 *
 * `Color.kt` states its own identity: it holds the spec's values, diffable token for
 * token against `mock-design-system.html`, and `TokenParityTest` exists to fail if it
 * drifts. This palette came from a different design pass and answers a different question
 * — "which category is this?" rather than "what is this surface?" — so it lives here,
 * where a change to it can never look like a change to the spec.
 *
 * ## Keyed by id, not stored
 *
 * Every entry is a compile-time constant addressed by the row's stable id. Nothing here is
 * written to the database, synced to Firestore or included in a backup. A seeded category
 * has no stored colour at all: its colour is derived from its id on every draw, which is
 * what keeps the palette the single source of truth for the 27 seeded categories and the
 * 6 seeded payment methods, and what lets
 * [ExpenseTrackerDatabaseInitializer][com.mknlabs.expensetracker.data.local.room.ExpenseTrackerDatabaseInitializer]
 * rewrite those rows on every launch without ever destroying anything.
 *
 * The ids are not invented here. They are the ones already in `categoryTypeData.kt` and
 * `paymentTypeData.kt`, and `CategoryPaletteTest` fails if the two ever disagree — so
 * adding a category, or renumbering one, cannot silently leave a colour behind. Note that
 * expense id 21 does not exist, and that the payment ids do not follow the order the
 * palette was supplied in (`SalaryDeposit` is 6, `OtherPayment` is 5).
 *
 * ## Both themes are required
 *
 * A light and a dark map exist for each domain, and the test asserts their key sets are
 * identical. Splitting the palette per theme would otherwise make it possible to add a
 * colour to one and forget the other, which shows up as a category that is coloured in
 * daylight and grey at night.
 *
 * ## These draw verbatim — do not run them through [adaptForContrast]
 *
 * Both hexes for every entry were chosen by eye against their own theme's card. Some of
 * them are deliberately close to the 3:1 floor once the glyph sits on its own tinted tile
 * (`#D97706` and `#059669` in light), and that closeness is part of the approved design
 * rather than an oversight. Passing them through the contrast adapter would move them away
 * from the selected values, so the adapter is wired to picker-authored colours only, in
 * [categoryColor] and [paymentColor].
 */

// ── 01. Expense categories, light ─────────────────────────────────────────────
internal val FoodLight = Color(0xFFFB923C)
internal val TravelLight = Color(0xFF38BDF8)
internal val ShoppingLight = Color(0xFFFBBF24)
internal val BillsLight = Color(0xFFFACC15)
internal val HealthLight = Color(0xFFFB7185)
internal val EntertainmentLight = Color(0xFFC084FC)
internal val RentLight = Color(0xFF60A5FA)
internal val GroceriesLight = Color(0xFF34D399)
internal val EducationLight = Color(0xFF2DD4BF)
internal val SubscriptionsLight = Color(0xFFA78BFA)
internal val InsuranceLight = Color(0xFF4ADE80)
internal val GiftsLight = Color(0xFFF472B6)
internal val PersonalCareLight = Color(0xFFE07A5F)
internal val FuelLight = Color(0xFFD97706)
internal val MaintenanceLight = Color(0xFFA3E635)
internal val TaxesLight = Color(0xFFE11D48)
internal val PetsLight = Color(0xFFD4A373)
internal val ChildcareLight = Color(0xFFE879F9)
internal val DonationsLight = Color(0xFF2B9348)
internal val MiscellaneousLight = Color(0xFF00B4D8)
internal val TransportLight = Color(0xFF6366F1)
internal val OtherExpenseLight = Color(0xFFCBD5E1)

// ── 01. Expense categories, dark ──────────────────────────────────────────────
internal val FoodDark = Color(0xFFFB923C)
internal val TravelDark = Color(0xFF38BDF8)
internal val ShoppingDark = Color(0xFFFBBF24)
internal val BillsDark = Color(0xFFFACC15)
internal val HealthDark = Color(0xFFFB7185)
internal val EntertainmentDark = Color(0xFFC084FC)
internal val RentDark = Color(0xFF60A5FA)
internal val GroceriesDark = Color(0xFF34D399)
internal val EducationDark = Color(0xFF2DD4BF)
internal val SubscriptionsDark = Color(0xFFA78BFA)
internal val InsuranceDark = Color(0xFF4ADE80)
internal val GiftsDark = Color(0xFFF472B6)
internal val PersonalCareDark = Color(0xFFE07A5F)
internal val FuelDark = Color(0xFFD97706)
internal val MaintenanceDark = Color(0xFFA3E635)
internal val TaxesDark = Color(0xFFE11D48)
internal val PetsDark = Color(0xFFD4A373)
internal val ChildcareDark = Color(0xFFE879F9)
internal val DonationsDark = Color(0xFF2B9348)
internal val MiscellaneousDark = Color(0xFF00B4D8)
internal val TransportDark = Color(0xFF6366F1)
internal val OtherExpenseDark = Color(0xFFCBD5E1)

// ── 02. Income categories, light ──────────────────────────────────────────────
internal val SalaryLight = Color(0xFF10B981)
internal val BusinessLight = Color(0xFF06B6D4)
internal val InvestmentLight = Color(0xFF14B8A6)
internal val FreelanceLight = Color(0xFF8B5CF6)
internal val OtherIncomeLight = Color(0xFFCBD5E1)

// ── 02. Income categories, dark ───────────────────────────────────────────────
internal val SalaryDark = Color(0xFF10B981)
internal val BusinessDark = Color(0xFF06B6D4)
internal val InvestmentDark = Color(0xFF14B8A6)
internal val FreelanceDark = Color(0xFF8B5CF6)
internal val OtherIncomeDark = Color(0xFFCBD5E1)

// ── 03. Payment methods, light ────────────────────────────────────────────────
// Card ships as CardPayment*, not Card*: `Color.kt` already declares `CardLight` as the
// surface behind chips and search bars, and one name cannot mean both a payment method and
// a card. Suffixing says which is which at the call site rather than leaving a reader to
// check the import.
internal val UpiLight = Color(0xFF9D4EDD)
internal val CashLight = Color(0xFF52B788)
internal val BankLight = Color(0xFF3A86FF)
internal val CardPaymentLight = Color(0xFF7209B7)
internal val SalaryDepositLight = Color(0xFF10B981)
internal val OtherPaymentLight = Color(0xFFCBD5E1)

// ── 03. Payment methods, dark ─────────────────────────────────────────────────
internal val UpiDark = Color(0xFF9D4EDD)
internal val CashDark = Color(0xFF52B788)
internal val BankDark = Color(0xFF3A86FF)
internal val CardPaymentDark = Color(0xFF7209B7)
internal val SalaryDepositDark = Color(0xFF10B981)
internal val OtherPaymentDark = Color(0xFFCBD5E1)

// ── The maps ──────────────────────────────────────────────────────────────────
// Built once at class load rather than resolved per frame, and addressable by iteration
// so a test can walk every entry instead of enumerating branches. Both the expense and the
// income categories share one map because a category id is unique across both: 1–23 are
// expenses and 101–105 are incomes, and nothing else is in either range.

/** Expense ids 1–23 (21 is unused) and income ids 101–105, light. */
internal val CategoryAccentLight: Map<Int, Color> = mapOf(
    1 to FoodLight,
    2 to TravelLight,
    3 to ShoppingLight,
    4 to BillsLight,
    5 to HealthLight,
    6 to EntertainmentLight,
    7 to RentLight,
    8 to GroceriesLight,
    9 to EducationLight,
    10 to SubscriptionsLight,
    11 to InsuranceLight,
    12 to GiftsLight,
    13 to PersonalCareLight,
    14 to FuelLight,
    15 to MaintenanceLight,
    16 to TaxesLight,
    17 to PetsLight,
    18 to ChildcareLight,
    19 to DonationsLight,
    20 to MiscellaneousLight,
    22 to TransportLight,
    23 to OtherExpenseLight,
    101 to SalaryLight,
    102 to BusinessLight,
    103 to InvestmentLight,
    104 to FreelanceLight,
    105 to OtherIncomeLight
)

/** Expense ids 1–23 (21 is unused) and income ids 101–105, dark. */
internal val CategoryAccentDark: Map<Int, Color> = mapOf(
    1 to FoodDark,
    2 to TravelDark,
    3 to ShoppingDark,
    4 to BillsDark,
    5 to HealthDark,
    6 to EntertainmentDark,
    7 to RentDark,
    8 to GroceriesDark,
    9 to EducationDark,
    10 to SubscriptionsDark,
    11 to InsuranceDark,
    12 to GiftsDark,
    13 to PersonalCareDark,
    14 to FuelDark,
    15 to MaintenanceDark,
    16 to TaxesDark,
    17 to PetsDark,
    18 to ChildcareDark,
    19 to DonationsDark,
    20 to MiscellaneousDark,
    22 to TransportDark,
    23 to OtherExpenseDark,
    101 to SalaryDark,
    102 to BusinessDark,
    103 to InvestmentDark,
    104 to FreelanceDark,
    105 to OtherIncomeDark
)

/** Payment method ids 1–6, light. */
internal val PaymentAccentLight: Map<Int, Color> = mapOf(
    1 to UpiLight,
    2 to CashLight,
    3 to BankLight,
    4 to CardPaymentLight,
    5 to OtherPaymentLight,
    6 to SalaryDepositLight
)

/** Payment method ids 1–6, dark. */
internal val PaymentAccentDark: Map<Int, Color> = mapOf(
    1 to UpiDark,
    2 to CashDark,
    3 to BankDark,
    4 to CardPaymentDark,
    5 to OtherPaymentDark,
    6 to SalaryDepositDark
)

// ── The roles ─────────────────────────────────────────────────────────────────
// Read through `isDark`, which is derived from the active scheme's background rather than
// from the system configuration. That matters: the app has its own theme toggle, and a
// category must not be the one colour in the interface that follows a different setting
// from the surface it is drawn on.

/** The category palette for the active theme, keyed by id. */
val ColorScheme.categoryAccent: Map<Int, Color>
    get() = if (isDark) CategoryAccentDark else CategoryAccentLight

/** The payment method palette for the active theme, keyed by id. */
val ColorScheme.paymentAccent: Map<Int, Color>
    get() = if (isDark) PaymentAccentDark else PaymentAccentLight

/**
 * The colour for a category the palette has never heard of — a user-created one, or an id
 * from a newer build. The brand ink, which is what every category was drawn in before this
 * palette existed, so an unseeded category looks deliberate rather than unthemed.
 */
val ColorScheme.categoryAccentFallback: Color
    get() = accentInk

/**
 * The tile behind a category glyph: the glyph's own colour, washed out.
 *
 * The two alphas are not symmetric, for the same reason `accentSoft` is not: 14% of a
 * light pastel over the near-black field reads as a wash, while the same 14% of a deep
 * tone over white reads as a stain. Light gets the lighter hand.
 */
internal const val CategorySoftAlphaLight = 31f / 255f // 0x1F wash from lucide palette
internal const val CategorySoftAlphaDark = 31f / 255f

/** [color]'s wash for the active theme, for the tile behind its glyph. */
fun ColorScheme.categorySoft(color: Color): Color = color.copy(
    alpha = if (isDark) CategorySoftAlphaDark else CategorySoftAlphaLight
)

/**
 * The wash behind a glyph tile drawn straight on the field rather than on a card: the icon
 * picker's grid, and the category and payment chips in Add Transaction.
 *
 * One alpha for both themes, unlike the pair above. The two callers are asking different
 * questions — the picker shows the colour a row is about to be given, a chip shows the colour a row
 * already has — but both draw on the same field, and neither may let the theme decide how strongly
 * a colour the user is looking at appears. A wash tuned per theme would show two colours for one
 * choice, and would make the chips the only place a category is washed at another strength.
 */
internal const val GlyphTileAlpha = 0.20f

// ── The resolvers ─────────────────────────────────────────────────────────────
// One function per domain so no call site has to remember the fallback chain, and so the
// "adapt only picked colours" rule is enforced in exactly one place rather than repeated
// at every surface.

/**
 * The pick the user made, adapted for the theme it was not chosen in, or null when they made none.
 *
 * Split out because it is the one step every resolver shares: a palette entry is never adapted, so
 * "there was a pick" is a different question from "which colour does this row get".
 */
private fun ColorScheme.adaptPicked(colorHex: String?): Color? =
    parseHexColorOrNull(colorHex)?.let { picked ->
        adaptForContrast(picked, background = surface, minRatio = GLYPH_MIN_CONTRAST)
    }

/**
 * The colour for a category row: the user's pick if there is one, else the palette, else
 * [categoryAccentFallback].
 *
 * A pick is adapted for the theme it was not chosen in, because one stored hex has to
 * serve two backgrounds — see [adaptForContrast]. A palette entry is **not**, because both
 * of its hexes were chosen against their own theme already.
 *
 * Tolerant at every step: a malformed or blank stored value falls through to the palette
 * rather than drawing nothing, and an unknown id falls through to the brand ink.
 */
fun ColorScheme.categoryColor(categoryId: Int, colorHex: String? = null): Color =
    adaptPicked(colorHex) ?: categoryAccent[categoryId] ?: categoryAccentFallback

/**
 * The colour a row will be given before it has an id to be looked up by — what the icon picker
 * previews while the user chooses.
 *
 * The chain of [categoryColor] with the palette step removed: a row that does not exist yet has no
 * id, so the only colour there is to resolve is the user's own. With none picked that is the brand
 * ink, which is what a category took before this palette existed and so reads as "not chosen yet"
 * rather than as a colour the user did choose.
 */
fun ColorScheme.identityColor(colorHex: String? = null): Color =
    adaptPicked(colorHex) ?: categoryAccentFallback

/** The colour for a payment method row. The same chain as [categoryColor], per domain. */
fun ColorScheme.paymentColor(paymentId: Int, colorHex: String? = null): Color =
    adaptPicked(colorHex) ?: paymentAccent[paymentId] ?: categoryAccentFallback
