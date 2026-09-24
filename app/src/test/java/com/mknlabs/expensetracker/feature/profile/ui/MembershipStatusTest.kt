package com.mknlabs.expensetracker.feature.profile.ui

import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.monetization.StoreEntitlement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the branch that decides what the membership screen offers, and which date it may state.
 *
 * The case that matters most is `premium without a store subscription`: it must resolve to
 * [MembershipStatus.PRO_PASS], not [MembershipStatus.SUBSCRIPTION]. Getting that wrong is
 * exactly what produced a "Manage Subscription" button for users who had no subscription.
 */
class MembershipStatusTest {

    // --- which state the screen is in ---

    @Test
    fun `free signed-in user is free`() {
        assertEquals(
            MembershipStatus.FREE,
            resolveMembershipStatus(
                userTier = UserTier.FREE,
                isAnonymous = false,
                hasActiveStoreSubscription = false
            )
        )
    }

    @Test
    fun `anonymous user is offline even when the tier says premium`() {
        // An anonymous install cannot have a store purchase tied to an account, and the
        // screen treats the tier as unknowable rather than trusting a local mirror.
        assertEquals(
            MembershipStatus.OFFLINE,
            resolveMembershipStatus(
                userTier = UserTier.PREMIUM,
                isAnonymous = true,
                hasActiveStoreSubscription = true
            )
        )
    }

    @Test
    fun `premium with a store entitlement is a subscription`() {
        assertEquals(
            MembershipStatus.SUBSCRIPTION,
            resolveMembershipStatus(
                userTier = UserTier.PREMIUM,
                isAnonymous = false,
                hasActiveStoreSubscription = true
            )
        )
    }

    @Test
    fun `premium without a store entitlement is a pro pass`() {
        assertEquals(
            MembershipStatus.PRO_PASS,
            resolveMembershipStatus(
                userTier = UserTier.PREMIUM,
                isAnonymous = false,
                hasActiveStoreSubscription = false
            )
        )
    }

    @Test
    fun `only the two pro states count as pro`() {
        assertTrue(MembershipStatus.SUBSCRIPTION.isPro)
        assertTrue(MembershipStatus.PRO_PASS.isPro)
        assertFalse(MembershipStatus.FREE.isPro)
        assertFalse(MembershipStatus.OFFLINE.isPro)
    }

    @Test
    fun `the resolved tier outranks the raw store flag`() {
        // `userTier` is already resolved against the store entitlement upstream
        // (EntitlementResolver short-circuits on it), so this combination cannot occur in
        // production. Pinned anyway because it states the precedence: this screen never
        // second-guesses the resolved tier with a raw flag, which is what keeps the two
        // Pro states from disagreeing with the feature gates.
        assertEquals(
            MembershipStatus.FREE,
            resolveMembershipStatus(
                userTier = UserTier.FREE,
                isAnonymous = false,
                hasActiveStoreSubscription = true
            )
        )
    }

    // --- hero description: a date is printed only when the app has the right one ---

    private val now = System.currentTimeMillis()
    private val renewalDate = now + 1_000L * 60 * 60 * 24 * 30

    private fun subscription(
        expirationDateMillis: Long? = renewalDate,
        willRenew: Boolean = true,
        hasBillingIssue: Boolean = false
    ) = StoreEntitlement(expirationDateMillis, willRenew, hasBillingIssue)

    @Test
    fun `a renewing subscription states its real renewal date from the store`() {
        // The date comes from the entitlement, not from the profile: this is the whole
        // reason the store's `expirationDate` is read at all.
        val copy = membershipHeroCopy(
            status = MembershipStatus.SUBSCRIPTION,
            proExpiryTimestamp = 0L,
            storeEntitlement = subscription(),
            now = now
        )

        assertEquals(R.string.label_pro_renews_on, copy.descriptionRes)
        assertEquals(renewalDate, copy.dateMillis)
    }

    @Test
    fun `a cancelled subscription says it ends rather than renews`() {
        // Cancelled subscriptions stay active until they expire. Calling that date a renewal
        // would tell a user they will be charged when they will not be.
        val copy = membershipHeroCopy(
            status = MembershipStatus.SUBSCRIPTION,
            proExpiryTimestamp = 0L,
            storeEntitlement = subscription(willRenew = false),
            now = now
        )

        assertEquals(R.string.label_pro_subscription_ends_on, copy.descriptionRes)
        assertEquals(renewalDate, copy.dateMillis)
    }

    @Test
    fun `a billing issue outranks any renewal promise`() {
        val copy = membershipHeroCopy(
            status = MembershipStatus.SUBSCRIPTION,
            proExpiryTimestamp = 0L,
            storeEntitlement = subscription(hasBillingIssue = true),
            now = now
        )

        assertEquals(R.string.msg_subscription_billing_issue, copy.descriptionRes)
        assertNull(copy.dateMillis)
    }

    @Test
    fun `a subscription with no end date is not dated`() {
        // Lifetime access: active, but there is nothing to count down to.
        val copy = membershipHeroCopy(
            status = MembershipStatus.SUBSCRIPTION,
            proExpiryTimestamp = 0L,
            storeEntitlement = subscription(expirationDateMillis = null),
            now = now
        )

        assertNull(copy.dateMillis)
        assertEquals(R.string.msg_subscription_active_desc, copy.descriptionRes)
    }

    @Test
    fun `a store date that has already passed is not announced as a renewal`() {
        // The store's snapshot can lag: a renewal it has not reported yet leaves a past date
        // on the entitlement. Saying "will renew automatically on <yesterday>" would be worse
        // than saying nothing.
        val copy = membershipHeroCopy(
            status = MembershipStatus.SUBSCRIPTION,
            proExpiryTimestamp = 0L,
            storeEntitlement = subscription(expirationDateMillis = now - 1_000L),
            now = now
        )

        assertNull(copy.dateMillis)
        assertEquals(R.string.msg_subscription_active_desc, copy.descriptionRes)
    }

    @Test
    fun `a subscriber the store has not answered about falls back to wording`() {
        // Before the first CustomerInfo arrives there is no entitlement object; the card
        // must show a sentence rather than a date it made up.
        val copy = membershipHeroCopy(
            status = MembershipStatus.SUBSCRIPTION,
            proExpiryTimestamp = 0L,
            storeEntitlement = null,
            now = now
        )

        assertNull(copy.dateMillis)
        assertEquals(R.string.msg_subscription_active_desc, copy.descriptionRes)
    }

    @Test
    fun `a subscriber is never dated from the local profile timestamp`() {
        // Reachable and previously wrong twice over: `proExpiryTimestamp` is 0 for a plain
        // subscriber (which printed "01 Jan 1970"), and for a former ProPass holder it still
        // holds the *pass* expiry, which was presented as the next renewal.
        val leftoverPassExpiry = now + 1_000L * 60 * 60 * 24 * 10

        val noStoreDate = membershipHeroCopy(
            status = MembershipStatus.SUBSCRIPTION,
            proExpiryTimestamp = leftoverPassExpiry,
            storeEntitlement = subscription(expirationDateMillis = null),
            now = now
        )
        assertNull(noStoreDate.dateMillis)

        val storeDate = membershipHeroCopy(
            status = MembershipStatus.SUBSCRIPTION,
            proExpiryTimestamp = leftoverPassExpiry,
            storeEntitlement = subscription(),
            now = now
        )
        assertEquals(renewalDate, storeDate.dateMillis)
        assertNotEquals(leftoverPassExpiry, storeDate.dateMillis)
    }

    @Test
    fun `a pro pass states its own expiry, which no store entitlement can supply`() {
        val passExpiry = now + 1_000L * 60 * 60 * 24

        val copy = membershipHeroCopy(
            status = MembershipStatus.PRO_PASS,
            proExpiryTimestamp = passExpiry,
            storeEntitlement = null,
            now = now
        )

        assertEquals(R.string.label_pro_expires_on, copy.descriptionRes)
        assertEquals(passExpiry, copy.dateMillis)
    }

    @Test
    fun `a permanent pro pass grant shows wording instead of an epoch date`() {
        val copy = membershipHeroCopy(
            status = MembershipStatus.PRO_PASS,
            proExpiryTimestamp = 0L,
            storeEntitlement = null,
            now = now
        )

        assertNull(copy.dateMillis)
        assertEquals(R.string.msg_pro_active_no_expiry, copy.descriptionRes)
    }

    @Test
    fun `free and offline states carry no date`() {
        assertNull(
            membershipHeroCopy(MembershipStatus.FREE, 0L, null, now).dateMillis
        )
        assertNull(
            membershipHeroCopy(MembershipStatus.OFFLINE, 0L, null, now).dateMillis
        )
        assertEquals(
            R.string.msg_going_pro_benefits,
            membershipHeroCopy(MembershipStatus.FREE, 0L, null, now).descriptionRes
        )
    }
}
