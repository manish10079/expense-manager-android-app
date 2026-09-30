package com.mknlabs.expensetracker.monetization

import java.util.Locale
import kotlin.math.roundToInt

/**
 * The arithmetic behind a plan's savings badge, kept as one pure rule so it can be pinned
 * by tests without an SDK, a store or a locale.
 *
 * Both inputs are prices the store itself reported, in micros, and both come from the
 * *same* purchase option: [fullPriceMicros] is that option's own full price and
 * [discountedPriceMicros] the phase the customer pays first. A saving is therefore only
 * ever claimed between two prices for one agreement — never between a plan and a different
 * plan, and never against a price that was invented here.
 *
 * Returning null is the common case and the safe default: no badge is better than a
 * discount the store would not honour.
 *
 * @return the saving as a whole percent (rounded), or null when there is no real discount.
 */
internal fun discountPercentOf(fullPriceMicros: Long, discountedPriceMicros: Long): Int? {
    // A non-positive full price is not a price, it is missing data.
    if (fullPriceMicros <= 0L) return null
    // A zero or negative discounted price means a free phase, which is described as a free
    // trial rather than as a discount, and a discounted price at or above the full price is
    // no discount at all.
    if (discountedPriceMicros !in 1 until fullPriceMicros) return null

    val savingMicros = fullPriceMicros - discountedPriceMicros
    val percent = (savingMicros * 100.0 / fullPriceMicros).roundToInt()

    // A saving under half a percent rounds to zero, which would render as "0% OFF".
    return percent.takeIf { it > 0 }
}

/** How many months one billing cycle covers. Monthly is the comparison base (1). */
internal fun billingPeriodMonths(packageTypeName: String): Int = when (packageTypeName) {
    "TWO_MONTH" -> 2
    "THREE_MONTH" -> 3
    "SIX_MONTH" -> 6
    "ANNUAL" -> 12
    else -> 1
}

/**
 * Multiplies the monthly formatted price by [months], keeping the store's currency wrapping
 * (e.g. `₹99.00` × 6 → `₹594.00`).
 */
internal fun scalePriceText(monthlyFormatted: String, months: Int): String {
    val match = PRICE_AMOUNT.find(monthlyFormatted) ?: return monthlyFormatted
    val number = match.value.replace(",", "").toDoubleOrNull() ?: return monthlyFormatted
    val scaled = number * months
    val decimals = match.groupValues.getOrElse(2) { "" }
    val newAmount = if (decimals.isEmpty()) {
        scaled.toLong().toString()
    } else {
        "%.${decimals.length}f".format(Locale.US, scaled)
    }
    return monthlyFormatted.replaceRange(match.range, newAmount)
}

internal data class MonthlyComparison(
    val listPriceText: String,
    val discountPercent: Int,
)

/**
 * List price vs the longer plan using monthly as the base.
 * Null when there is no real saving (or the plan is monthly).
 */
internal fun comparisonFromMonthly(
    monthlyFormatted: String,
    monthlyMicros: Long,
    months: Int,
    saleMicros: Long,
): MonthlyComparison? {
    if (months <= 1 || monthlyMicros <= 0L) return null
    val listMicros = monthlyMicros * months
    val percent = discountPercentOf(listMicros, saleMicros) ?: return null
    return MonthlyComparison(
        listPriceText = scalePriceText(monthlyFormatted, months),
        discountPercent = percent,
    )
}

private val PRICE_AMOUNT = Regex("""(\d{1,3}(?:,\d{3})*|\d+)(?:\.(\d+))?""")
