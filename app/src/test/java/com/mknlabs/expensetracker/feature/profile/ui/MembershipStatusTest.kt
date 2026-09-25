package com.mknlabs.expensetracker.feature.profile.ui

import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.monetization.StoreEntitlement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    // --- the card: a date is stated only when the app has the right one to state ---

    private val now = System.currentTimeMillis()
    private val thirtyDays = 1_000L * 60 * 60 * 24 * 30
    private val tenDays = 1_000L * 60 * 60 * 24 * 10
    private val renewalDate = now + thirtyDays

    private fun subscription(
        expirationDateMillis: Long? = renewalDate,
        willRenew: Boolean = true,
        hasBillingIssue: Boolean = false
    ) = StoreEntitlement(expirationDateMillis, willRenew, hasBillingIssue)

    private fun card(
        status: MembershipStatus = MembershipStatus.SUBSCRIPTION,
        proExpiryTimestamp: Long = 0L,
        storeEntitlement: StoreEntitlement? = subscription()
    ) = membershipCardSpec(status, proExpiryTimestamp, storeEntitlement, now)

    private fun dateFacts(spec: MembershipCardSpec) =
        spec.facts.filterIsInstance<MembershipFact.Date>()

    private fun hasFact(spec: MembershipCardSpec, labelRes: Int) =
        spec.facts.any { it is MembershipFact.Label && it.labelRes == labelRes }

    @Test
    fun `a renewing subscription states the store's own renewal date as a fact`() {
        // The date comes from the entitlement, not from the profile: this is the whole reason
        // the store's `expirationDate` is read at all.
        val spec = card()

        assertEquals(R.string.label_membership_card_subscription, spec.headerLabelRes)
        assertEquals(R.string.label_active_caps, spec.badgeRes)
        assertEquals(R.string.title_membership_pro_card, spec.titleRes)

        val dateFact = dateFacts(spec).single()
        assertEquals(R.string.label_renews_on, dateFact.labelRes)
        assertEquals(renewalDate, dateFact.valueMillis)
        assertTrue(hasFact(spec, R.string.label_membership_managed_by_play))
    }

    @Test
    fun `a cancelled subscription states when access ends rather than a renewal`() {
        // Cancelled subscriptions stay active until they expire. Calling that date a renewal
        // would tell the user they are about to be charged when they will not be.
        val dateFact = dateFacts(card(storeEntitlement = subscription(willRenew = false))).single()

        assertEquals(R.string.label_expires_on, dateFact.labelRes)
        assertEquals(renewalDate, dateFact.valueMillis)
    }

    @Test
    fun `a billing issue outranks both the date and the unlocked headline`() {
        val spec = card(storeEntitlement = subscription(hasBillingIssue = true))

        assertTrue(dateFacts(spec).isEmpty())
        // The one line that says access is unlocked must not be asserted at a user the store
        // has just told us it cannot charge.
        assertEquals(R.string.msg_subscription_billing_issue, spec.panel?.headlineRes)
    }

    @Test
    fun `a subscription with no end date states no date`() {
        // Lifetime access: active, but there is nothing to count down to.
        val spec = card(storeEntitlement = subscription(expirationDateMillis = null))

        assertTrue(dateFacts(spec).isEmpty())
        // Still a subscription, and still says where it is managed.
        assertEquals(R.string.label_membership_card_subscription, spec.headerLabelRes)
        assertTrue(hasFact(spec, R.string.label_membership_managed_by_play))
    }

    @Test
    fun `a store date that has already passed is not restated`() {
        // The store's snapshot can lag: a renewal it has not reported yet leaves a past date
        // on the entitlement, and "renews on <yesterday>" is worse than no date.
        val spec = card(storeEntitlement = subscription(expirationDateMillis = now - 1_000L))

        assertTrue(dateFacts(spec).isEmpty())
    }

    @Test
    fun `a subscriber the store has not answered about is not dated`() {
        // Before the first CustomerInfo arrives there is no entitlement object; the card must
        // still render, without a date it made up.
        val spec = card(storeEntitlement = null)

        assertTrue(dateFacts(spec).isEmpty())
        assertEquals(R.string.label_membership_unlocked_headline, spec.panel?.headlineRes)
    }

    @Test
    fun `a subscriber is never dated from the local profile timestamp`() {
        // Reachable and previously wrong twice over: `proExpiryTimestamp` is 0 for a plain
        // subscriber (which printed "01 Jan 1970"), and for a former ProPass holder it still
        // holds the *pass* expiry, which was presented as the next renewal.
        val leftoverPassExpiry = now + tenDays

        val noStoreDate = card(
            proExpiryTimestamp = leftoverPassExpiry,
            storeEntitlement = subscription(expirationDateMillis = null)
        )
        assertTrue(dateFacts(noStoreDate).isEmpty())

        val storeDate = card(
            proExpiryTimestamp = leftoverPassExpiry,
            storeEntitlement = subscription()
        )
        assertEquals(renewalDate, dateFacts(storeDate).single().valueMillis)
        assertFalse(dateFacts(storeDate).any { it.valueMillis == leftoverPassExpiry })
    }

    @Test
    fun `a pro pass states its own expiry and counts down to it`() {
        val passExpiry = now + tenDays

        val spec = card(
            status = MembershipStatus.PRO_PASS,
            proExpiryTimestamp = passExpiry,
            storeEntitlement = null
        )

        assertEquals(R.string.label_membership_card_pro_pass, spec.headerLabelRes)
        // Same product, same title — the badge and the surface are what separate the two Pro
        // states, so the badges must not converge.
        assertEquals(R.string.label_membership_badge_free_pass, spec.badgeRes)
        assertEquals(R.string.title_membership_pro_card, spec.titleRes)

        val countdown = spec.facts.filterIsInstance<MembershipFact.Count>().single()
        assertEquals(R.plurals.label_membership_days_remaining, countdown.pluralsRes)
        assertEquals(10, countdown.count)

        val expiryFact = dateFacts(spec).single()
        assertEquals(R.string.label_expires_on, expiryFact.labelRes)
        assertEquals(passExpiry, expiryFact.valueMillis)

        assertEquals(R.string.label_membership_pass_headline, spec.panel?.headlineRes)
        assertEquals(R.string.msg_membership_pass_body, spec.panel?.bodyRes)
    }

    @Test
    fun `days remaining rounds up and never goes negative`() {
        val day = 1_000L * 60 * 60 * 24

        assertEquals(1, daysRemaining(now + day, now))
        assertEquals(1, daysRemaining(now + 1_000L, now))
        assertEquals(2, daysRemaining(now + day + 1_000L, now))
        assertEquals(30, daysRemaining(now + 30 * day, now))
        assertEquals(0, daysRemaining(now, now))
        assertEquals(0, daysRemaining(now - day, now))
    }

    @Test
    fun `a permanent pro pass grant shows no countdown and no date`() {
        // Pro with no recorded expiry is the legacy permanent grant. There is nothing to count
        // down to, and both a date and a "0 days remaining" would be inventions.
        val spec = card(
            status = MembershipStatus.PRO_PASS,
            proExpiryTimestamp = 0L,
            storeEntitlement = null
        )

        assertTrue(spec.facts.isEmpty())
        assertEquals(R.string.label_membership_pass_headline, spec.panel?.headlineRes)
        assertEquals(R.string.msg_pro_active_no_expiry, spec.panel?.bodyRes)
    }

    @Test
    fun `a lapsed expiry is treated as the permanent grant, not as a countdown`() {
        val spec = card(
            status = MembershipStatus.PRO_PASS,
            proExpiryTimestamp = now - 1_000L,
            storeEntitlement = null
        )

        assertTrue(spec.facts.isEmpty())
    }

    @Test
    fun `the free card carries both ways to reach pro`() {
        val spec = membershipCardSpec(
            status = MembershipStatus.FREE,
            proExpiryTimestamp = 0L,
            storeEntitlement = null,
            now = now
        )

        assertEquals(R.string.label_membership_card_free, spec.headerLabelRes)
        // Free has nothing to declare, so it declares nothing.
        assertNull(spec.badgeRes)
        assertEquals(R.string.title_membership_upgrade_card, spec.titleRes)
        assertEquals(R.string.msg_membership_upgrade_body, spec.bodyRes)
        assertEquals(R.string.btn_membership_buy_subscription, spec.primaryActionRes)
        assertTrue("a free user is the one who needs the code path", spec.showRedeemAction)
        assertTrue(spec.facts.isEmpty())
        assertNull(spec.panel)
    }

    @Test
    fun `the anonymous card offers sign-in and no code path`() {
        val spec = membershipCardSpec(
            status = MembershipStatus.OFFLINE,
            proExpiryTimestamp = 0L,
            storeEntitlement = null,
            now = now
        )

        assertEquals(R.string.label_unlimited_offline, spec.headerLabelRes)
        assertEquals(R.string.btn_sign_in_register, spec.primaryActionRes)
        // The server refuses a code from an anonymous user, so the card must not offer one.
        assertFalse(spec.showRedeemAction)
        assertNull(spec.badgeRes)
    }
}
