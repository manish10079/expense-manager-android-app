package com.mknlabs.expensetracker.feature.profile.ui

import androidx.annotation.StringRes
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.monetization.StoreEntitlement

/**
 * The four states the membership screen can be in.
 *
 * Pro is deliberately split in two, because the two ways of becoming Pro need opposite
 * calls to action:
 *
 *  - [SUBSCRIPTION] — the store's `premium` entitlement is active. The user pays on a
 *    schedule, so the useful action is to *manage* that subscription.
 *  - [PRO_PASS] — Pro with no store subscription: a ProPass grant, which is time-limited
 *    and expires on its own. There is nothing to manage, so the useful action is to offer
 *    the store's plans instead.
 *
 * Collapsing them into a single "Pro" is what made the screen offer a Manage Subscription
 * button to users who had no subscription to manage.
 */
internal enum class MembershipStatus {
    /** Anonymous/offline user: data is device-local, so no purchase state is knowable. */
    OFFLINE,

    /** No Pro at all. */
    FREE,

    /** Pro from a store subscription. */
    SUBSCRIPTION,

    /** Pro from a ProPass grant (or a legacy permanent grant) — no store subscription. */
    PRO_PASS,
}

/** True for the two states that unlock Pro features. */
internal val MembershipStatus.isPro: Boolean
    get() = this == MembershipStatus.SUBSCRIPTION || this == MembershipStatus.PRO_PASS

/**
 * Decides which of the four states applies.
 *
 * Pure and free of Android types on purpose: this is the branch that chooses what the user
 * is offered, so it is unit-tested directly rather than only through the composable.
 *
 * [hasActiveStoreSubscription] is read from the store entitlement instead of the profile's
 * `isSubscription` mirror, which only a Firestore sync ever writes — meaning a real
 * subscriber routinely read as `false` here and was shown ProPass wording and a date
 * derived from a timestamp nobody had written.
 */
internal fun resolveMembershipStatus(
    userTier: UserTier,
    isAnonymous: Boolean,
    hasActiveStoreSubscription: Boolean,
): MembershipStatus {
    if (isAnonymous) return MembershipStatus.OFFLINE
    if (userTier != UserTier.PREMIUM) return MembershipStatus.FREE
    return if (hasActiveStoreSubscription) {
        MembershipStatus.SUBSCRIPTION
    } else {
        MembershipStatus.PRO_PASS
    }
}

/**
 * The sentence the hero card shows, and the timestamp it should be formatted with.
 *
 * The two travel together on purpose: [dateMillis] is null whenever printing a date would
 * mean printing something the app does not actually know, and non-null exactly when it
 * knows which date belongs in that sentence.
 */
internal data class MembershipHeroCopy(
    @StringRes val descriptionRes: Int,
    /** Epoch millis to substitute into [descriptionRes], or null for copy with no date. */
    val dateMillis: Long?,
)

/**
 * Chooses the hero description, and which date (if any) it may state.
 *
 * A date is only ever printed when the app has the *right* one to print, and the two Pro
 * states get it from completely different places:
 *
 *  - **Subscription** — from [storeEntitlement], i.e. RevenueCat's own `expirationDate`, and
 *    the sentence depends on what that date means. `willRenew` decides "renews on" versus
 *    "ends on", because a cancelled subscription stays active until it expires and calling
 *    that a renewal would be false. A billing issue outranks both: the store has already
 *    said it cannot collect, so no date is promised. With no entitlement at all (the store
 *    has not answered yet) the copy states the subscription without dating it.
 *  - **ProPass** — from `proExpiryTimestamp`, which the `redeemProPass` Cloud Function is
 *    the sole trusted writer of.
 *
 * The local `proExpiryTimestamp` is never used for a subscriber: it is `0` for one who
 * never redeemed a pass, which used to print "01 Jan 1970", and for a former ProPass holder
 * it still holds the *pass* expiry, which used to be presented as the next renewal.
 */
internal fun membershipHeroCopy(
    status: MembershipStatus,
    proExpiryTimestamp: Long,
    storeEntitlement: StoreEntitlement?,
    now: Long,
): MembershipHeroCopy = when (status) {
    MembershipStatus.SUBSCRIPTION -> when {
        // Two ways there is no usable store date: no entitlement yet, or one whose date has
        // already passed because the store's snapshot is behind. Both show wording — a date
        // in the past under "will renew on" is worse than no date at all.
        storeEntitlement == null || storeEntitlement.expirationDateMillis == null ||
            storeEntitlement.expirationDateMillis <= now -> MembershipHeroCopy(
            descriptionRes = R.string.msg_subscription_active_desc,
            dateMillis = null
        )

        // A failed charge outranks any renewal promise: the store has said it cannot collect.
        storeEntitlement.hasBillingIssue -> MembershipHeroCopy(
            descriptionRes = R.string.msg_subscription_billing_issue,
            dateMillis = null
        )

        storeEntitlement.willRenew -> MembershipHeroCopy(
            descriptionRes = R.string.label_pro_renews_on,
            dateMillis = storeEntitlement.expirationDateMillis
        )

        // Cancelled but still running: the date is when access ends, not a renewal.
        else -> MembershipHeroCopy(
            descriptionRes = R.string.label_pro_subscription_ends_on,
            dateMillis = storeEntitlement.expirationDateMillis
        )
    }

    MembershipStatus.PRO_PASS -> if (proExpiryTimestamp > now) {
        MembershipHeroCopy(
            descriptionRes = R.string.label_pro_expires_on,
            dateMillis = proExpiryTimestamp
        )
    } else {
        // Legacy permanent grant: Pro with no expiry recorded at all.
        MembershipHeroCopy(
            descriptionRes = R.string.msg_pro_active_no_expiry,
            dateMillis = null
        )
    }

    MembershipStatus.OFFLINE -> MembershipHeroCopy(
        descriptionRes = R.string.label_offline_warning_desc,
        dateMillis = null
    )

    MembershipStatus.FREE -> MembershipHeroCopy(
        descriptionRes = R.string.msg_going_pro_benefits,
        dateMillis = null
    )
}
