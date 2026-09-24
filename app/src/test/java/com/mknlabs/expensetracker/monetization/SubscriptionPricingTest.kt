package com.mknlabs.expensetracker.monetization

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins the savings rule the paywall badge is built on.
 *
 * The rule reads two prices the store reported for the *same* purchase option, so the cases
 * worth pinning are the ones where a badge must **not** appear: no discount at all, a free
 * phase, a price the store never reported, and a saving too small to round to a percent.
 */
class SubscriptionPricingTest {

    private val full = 894_000_000L
    private val discounted = 749_000_000L

    @Test
    fun `a real discount is reported as a whole percent`() {
        // (894 - 749) / 894 = 16.2%, shown as 16.
        assertEquals(
            16,
            discountPercentOf(fullPriceMicros = full, discountedPriceMicros = discounted)
        )
    }

    @Test
    fun `the percent rounds to the nearest whole number`() {
        // Exactly half a percent rounds up, so a small real saving is still shown.
        assertEquals(1, discountPercentOf(fullPriceMicros = 200L, discountedPriceMicros = 199L))
        // 33.33% rounds down.
        assertEquals(33, discountPercentOf(fullPriceMicros = 3L, discountedPriceMicros = 2L))
    }

    @Test
    fun `an unchanged price is not a discount`() {
        assertEquals(
            null,
            discountPercentOf(fullPriceMicros = full, discountedPriceMicros = full)
        )
    }

    @Test
    fun `a zero priced phase is a free trial rather than a discount`() {
        assertEquals(
            null,
            discountPercentOf(fullPriceMicros = full, discountedPriceMicros = 0L)
        )
    }

    @Test
    fun `a price above the store's full price is not a discount`() {
        assertEquals(
            null,
            discountPercentOf(fullPriceMicros = full, discountedPriceMicros = full + 1)
        )
    }

    @Test
    fun `a missing full price cannot support a discount`() {
        // Zero is what a price the store never reported looks like, and no badge is the
        // honest thing to show there.
        assertEquals(
            null,
            discountPercentOf(fullPriceMicros = 0L, discountedPriceMicros = discounted)
        )
    }

    @Test
    fun `a saving that rounds to nothing is not shown as zero percent off`() {
        // 1 micro in 100_000 is 0.001%, which rounds to 0 and would render as "0% OFF".
        assertEquals(
            null,
            discountPercentOf(fullPriceMicros = 100_000L, discountedPriceMicros = 99_999L)
        )
    }
}
