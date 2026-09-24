package com.mknlabs.expensetracker.monetization

import androidx.annotation.StringRes
import com.mknlabs.expensetracker.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Pins the mapping from a RevenueCat package type to the plan labels the paywall renders.
 *
 * The input is the package type's *name* rather than the SDK enum, which is what makes
 * this testable without the SDK — and it is the part worth testing, because RevenueCat's
 * `PackageType` is a foreign enum that can gain values between upgrades.
 */
class SubscriptionOfferMapperTest {

    /**
     * Every `PackageType` value in RevenueCat 10.15.1.
     *
     * If an SDK upgrade adds a type, this list is the reminder to label it here rather
     * than let it fall through to the generic wording.
     */
    private val knownPackageTypes = listOf(
        "WEEKLY",
        "MONTHLY",
        "TWO_MONTH",
        "THREE_MONTH",
        "SIX_MONTH",
        "ANNUAL",
        "LIFETIME",
    )

    @Test
    fun `every package type the SDK can report gets a real plan label`() {
        knownPackageTypes.forEach { packageType ->
            assertNotEquals(
                "package type $packageType fell through to the generic label",
                R.string.paywall_plan_generic,
                SubscriptionOfferMapper.planLabelFor(packageType)
            )
        }
    }

    @Test
    fun `each package type gets its own plan label`() {
        val labels = knownPackageTypes.map { SubscriptionOfferMapper.planLabelFor(it) }
        assertEquals(knownPackageTypes.size, labels.distinct().size)
    }

    // --- The three plans this app actually sells ---------------------------------------

    @Test
    fun `a monthly package is labelled as one month with no discount badge`() {
        val offer = SubscriptionOfferMapper.from("monthly", "MONTHLY", "₹149.00")
        assertEquals(R.string.paywall_plan_monthly, offer.planLabelRes)
        assertEquals(R.string.paywall_period_month, offer.periodLabelRes)
        assertEquals(null, offer.discountBadgeRes)
    }

    @Test
    fun `a six month package is labelled as six months with 20 percent off badge and strikethrough price`() {
        val offer = SubscriptionOfferMapper.from("six_months", "SIX_MONTH", "₹749.00", "₹894.00")
        assertEquals(R.string.paywall_plan_six_months, offer.planLabelRes)
        assertEquals(R.string.paywall_period_months, offer.periodLabelRes)
        assertEquals(R.string.paywall_discount_six_months, offer.discountBadgeRes)
        assertEquals("₹894.00", offer.strikethroughPriceText)
    }

    @Test
    fun `an annual package is labelled as twelve months with 40 percent off badge and strikethrough price`() {
        val offer = SubscriptionOfferMapper.from("annual", "ANNUAL", "₹1,299.00", "₹1,788.00")
        assertEquals(R.string.paywall_plan_twelve_months, offer.planLabelRes)
        assertEquals(R.string.paywall_period_year, offer.periodLabelRes)
        assertEquals(R.string.paywall_discount_twelve_months, offer.discountBadgeRes)
        assertEquals("₹1,788.00", offer.strikethroughPriceText)
    }

    // --- Pass-through ------------------------------------------------------------------

    @Test
    fun `the package identifier and store price are passed through verbatim`() {
        val offer = SubscriptionOfferMapper.from("six_months", "SIX_MONTH", "₹749.00")
        // The identifier is what the purchase call sends back to RevenueCat, and the price
        // is already formatted by the store — re-formatting either would be a bug.
        assertEquals("six_months", offer.id)
        assertEquals("₹749.00", offer.priceText)
    }

    // --- Degradation -------------------------------------------------------------------

    @Test
    fun `an unrecognised package type still renders as a purchasable plan`() {
        // A future SDK value must not crash the paywall or make a plan vanish from it.
        val offer = SubscriptionOfferMapper.from("mystery", "SOME_FUTURE_TYPE", "$4.99")
        assertEquals(R.string.paywall_plan_generic, offer.planLabelRes)
        assertEquals(R.string.paywall_period_one_time, offer.periodLabelRes)
        assertEquals("mystery", offer.id)
    }

    @Test
    fun `a custom package reports the generic label rather than nothing`() {
        // RevenueCat reports CUSTOM and UNKNOWN for packages the dashboard defines by hand.
        listOf("CUSTOM", "UNKNOWN").forEach { packageType ->
            @StringRes val label = SubscriptionOfferMapper.planLabelFor(packageType)
            assertNotEquals(0, label)
        }
    }
}
