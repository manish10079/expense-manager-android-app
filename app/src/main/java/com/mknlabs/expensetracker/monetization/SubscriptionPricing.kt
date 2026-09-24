package com.mknlabs.expensetracker.monetization

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
