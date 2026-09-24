package com.mknlabs.expensetracker.monetization

import androidx.annotation.StringRes
import com.mknlabs.expensetracker.R

/**
 * One purchasable subscription plan, in a shape the UI can render directly.
 *
 * <b>Deliberately free of RevenueCat types.</b> The paywall reads this instead of a
 * `Package`, so the ViewModel that consumes it can be unit-tested without constructing
 * SDK objects — and so no RevenueCat identifier can leak into user-facing text.
 *
 * [priceText] is the store's own localized price string (`StoreProduct.price.formatted`,
 * fed by Play's `ProductDetails`), which the store has already formatted for the user's
 * currency and locale. It is never assembled in code, so no currency or number formatting
 * is invented here.
 *
 * [planLabelRes] and [periodLabelRes] are `@StringRes` ids rather than text, per the
 * project's i18n rule: the billing layer chooses *which* label, the UI owns the wording.
 */
data class SubscriptionOffer(
    /** The RevenueCat package identifier, used to start the purchase for this plan. */
    val id: String,
    /** The plan's length label, e.g. "6 months". */
    @StringRes val planLabelRes: Int,
    /** The plan's cadence label, e.g. "per month". */
    @StringRes val periodLabelRes: Int,
    /** The store-formatted price, shown exactly as the store supplied it. */
    val priceText: String,
    /** Optional discount/savings badge label resource, e.g. "20% OFF". */
    @StringRes val discountBadgeRes: Int? = null,
    /** Optional strikethrough un-discounted original base price text. */
    val strikethroughPriceText: String? = null,
)

/**
 * Builds a [SubscriptionOffer] from a RevenueCat package.
 *
 * The input is primitives on purpose. `Package`, `PackageType` and `StoreProduct` are SDK
 * types that cannot be instantiated in a JVM unit test, so taking them directly would make
 * the mapping untestable — and this mapping is the part worth pinning, because an SDK
 * upgrade that renames a package type would otherwise silently degrade every plan to the
 * generic label.
 *
 * Ordering is not decided here: the paywall shows offers in the order RevenueCat returns
 * them, which is the order the plans were arranged in the dashboard's default offering.
 */
object SubscriptionOfferMapper {

    /**
     * @param packageIdentifier the RevenueCat package identifier.
     * @param packageTypeName `PackageType.name` — passed as text so the caller, not this
     *   mapper, is the only place that touches the SDK enum.
     * @param storeFormattedPrice `StoreProduct.price.formatted`, already localized by the
     *   store.
     * @param strikethroughPriceText optional un-discounted original base price text.
     */
    fun from(
        packageIdentifier: String,
        packageTypeName: String,
        storeFormattedPrice: String,
        strikethroughPriceText: String? = null,
    ): SubscriptionOffer = SubscriptionOffer(
        id = packageIdentifier,
        planLabelRes = planLabelFor(packageTypeName),
        periodLabelRes = periodLabelFor(packageTypeName),
        priceText = storeFormattedPrice,
        discountBadgeRes = discountBadgeFor(packageTypeName),
        strikethroughPriceText = strikethroughPriceText,
    )

    /** Maps RevenueCat's package type to an optional savings/discount badge string. */
    internal fun discountBadgeFor(packageTypeName: String): Int? = when (packageTypeName) {
        "SIX_MONTH" -> R.string.paywall_discount_six_months
        "ANNUAL" -> R.string.paywall_discount_twelve_months
        else -> null
    }

    /**
     * Maps RevenueCat's package type to the plan's length label.
     *
     * `PackageType` is a foreign enum that can gain values between upgrades, so the final
     * branch is intentional: an unrecognised type still renders as a purchasable plan
     * labelled generically, rather than crashing or vanishing from the paywall.
     */
    internal fun planLabelFor(packageTypeName: String): Int = when (packageTypeName) {
        "WEEKLY" -> R.string.paywall_plan_weekly
        "MONTHLY" -> R.string.paywall_plan_monthly
        "TWO_MONTH" -> R.string.paywall_plan_two_months
        "THREE_MONTH" -> R.string.paywall_plan_three_months
        "SIX_MONTH" -> R.string.paywall_plan_six_months
        "ANNUAL" -> R.string.paywall_plan_twelve_months
        "LIFETIME" -> R.string.paywall_plan_lifetime
        else -> R.string.paywall_plan_generic
    }

    /** Maps RevenueCat's package type to the plan's cadence label. */
    internal fun periodLabelFor(packageTypeName: String): Int = when (packageTypeName) {
        "WEEKLY" -> R.string.paywall_period_week
        "MONTHLY" -> R.string.paywall_period_month
        "TWO_MONTH", "THREE_MONTH", "SIX_MONTH" -> R.string.paywall_period_months
        "ANNUAL" -> R.string.paywall_period_year
        else -> R.string.paywall_period_one_time
    }
}
