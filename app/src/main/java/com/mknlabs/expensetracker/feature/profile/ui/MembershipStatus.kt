package com.mknlabs.expensetracker.feature.profile.ui

import androidx.annotation.PluralsRes
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
 * The glyph a card line leads with.
 *
 * A plain enum rather than a Compose icon: which glyph a state uses is a decision, and the
 * decisions in this file are unit-tested without a Context. The card maps each value to the
 * icon it draws.
 */
internal enum class MembershipGlyph {
    /** Access bought and managed by the store. */
    VERIFIED,

    /** Access handed out as a code rather than paid for. */
    TICKET,

    /** Nothing unlocked. */
    LOCK,

    /** A date. */
    DATE,

    /** Time left on a grant. */
    CLOCK,
}

/**
 * One `<glyph> label` fact in the card's meta row.
 *
 * Three kinds, because three different things travel in a fact: a plain label, a date the UI
 * formats, and a count it pluralises. Sealed rather than one class with nullable value slots,
 * so a fact cannot claim to carry both — and so the card has to say what it does with each
 * kind instead of guessing from which field happens to be set.
 */
internal sealed interface MembershipFact {
    val glyph: MembershipGlyph

    /** A fact that is only a label. */
    data class Label(
        override val glyph: MembershipGlyph,
        @StringRes val labelRes: Int,
    ) : MembershipFact

    /** A fact whose label takes a date, which the UI formats with the pattern its card uses. */
    data class Date(
        override val glyph: MembershipGlyph,
        @StringRes val labelRes: Int,
        val valueMillis: Long,
    ) : MembershipFact

    /** A fact whose label is a plural, told how many of the thing there are. */
    data class Count(
        override val glyph: MembershipGlyph,
        @PluralsRes val pluralsRes: Int,
        val count: Int,
    ) : MembershipFact
}

/**
 * The panel inside a Pro card: the line it states in bold, and the sentence behind it.
 *
 * This is where a Pro state says the one thing its surface cannot — how the access is
 * billed, or that it is not billed at all.
 */
internal data class MembershipPanel(
    val glyph: MembershipGlyph,
    @StringRes val headlineRes: Int,
    @StringRes val bodyRes: Int,
)

/**
 * Everything one state of the membership card draws.
 *
 * All three states share this shape — header label, badge, title, meta facts, panel — and
 * differ only in what fills it. That is deliberate: every one of them is answering the same
 * question ("what do I have?"), so only the answer changes, never the layout.
 */
internal data class MembershipCardSpec(
    val glyph: MembershipGlyph,
    @StringRes val headerLabelRes: Int,
    /** Right-aligned badge, or null when the state has nothing to declare. */
    @StringRes val badgeRes: Int?,
    @StringRes val titleRes: Int,
    val facts: List<MembershipFact> = emptyList(),
    /** The sentence under the title, for the states that carry no panel. */
    @StringRes val bodyRes: Int? = null,
    val panel: MembershipPanel? = null,
    /** The card's own call to action, when the card carries one (free and offline states). */
    @StringRes val primaryActionRes: Int? = null,
    /** Whether the card also offers the second way in, a ProPass code. */
    val showRedeemAction: Boolean = false,
)

/**
 * Builds the card for a state.
 *
 * A date is only ever put in a fact when the app has the *right* one to print, and the two
 * Pro states get theirs from completely different places:
 *
 *  - **Subscription** — from [storeEntitlement], i.e. RevenueCat's own `expirationDate`.
 *    `willRenew` decides whether that date is a renewal or the end of access, because a
 *    cancelled subscription stays active until it expires and calling that a renewal would
 *    promise a charge that is not coming. A billing issue outranks both: the store has
 *    already said it cannot collect, so no date is promised and the panel states the problem
 *    instead of claiming everything is unlocked.
 *  - **ProPass** — from `proExpiryTimestamp`, which the `redeemProPass` Cloud Function is
 *    the sole trusted writer of.
 *
 * The local `proExpiryTimestamp` is never used for a subscriber: it is `0` for one who
 * never redeemed a pass, which used to print "01 Jan 1970", and for a former ProPass holder
 * it still holds the *pass* expiry, which used to be presented as the next renewal.
 */
internal fun membershipCardSpec(
    status: MembershipStatus,
    proExpiryTimestamp: Long,
    storeEntitlement: StoreEntitlement?,
    now: Long,
): MembershipCardSpec = when (status) {
    MembershipStatus.SUBSCRIPTION -> subscriptionCardSpec(storeEntitlement, now)
    MembershipStatus.PRO_PASS -> proPassCardSpec(proExpiryTimestamp, now)

    MembershipStatus.FREE -> MembershipCardSpec(
        glyph = MembershipGlyph.LOCK,
        headerLabelRes = R.string.label_membership_card_free,
        badgeRes = null,
        titleRes = R.string.title_membership_upgrade_card,
        bodyRes = R.string.msg_membership_upgrade_body,
        // There is no access to describe yet, so the card is where both ways to reach Pro
        // belong: buying one, and spending a code.
        primaryActionRes = R.string.btn_membership_buy_subscription,
        showRedeemAction = true,
    )

    // An anonymous install is device-local, so it has no purchase state to report — and the
    // server refuses a code for it, so the one honest call to action is to sign in.
    MembershipStatus.OFFLINE -> MembershipCardSpec(
        glyph = MembershipGlyph.LOCK,
        headerLabelRes = R.string.label_unlimited_offline,
        badgeRes = null,
        titleRes = R.string.title_membership_upgrade_card,
        bodyRes = R.string.label_offline_warning_desc,
        primaryActionRes = R.string.btn_sign_in_register,
        showRedeemAction = false,
    )
}

private fun subscriptionCardSpec(
    storeEntitlement: StoreEntitlement?,
    now: Long,
): MembershipCardSpec {
    val hasBillingIssue = storeEntitlement?.hasBillingIssue == true
    // A date in the past is not printed: the store's snapshot can lag behind a renewal it has
    // not reported yet, and "renews on <yesterday>" is worse than no date at all.
    val usableDate = storeEntitlement?.expirationDateMillis?.takeIf { it > now }

    val dateFact = when {
        hasBillingIssue -> null
        usableDate == null -> null
        // A renewal is a charge; a cancelled subscription keeps working until it ends, and
        // calling that date a renewal would tell the user they are about to be billed.
        storeEntitlement?.willRenew == true ->
            MembershipFact.Date(MembershipGlyph.DATE, R.string.label_renews_on, usableDate)
        else ->
            MembershipFact.Date(MembershipGlyph.DATE, R.string.label_expires_on, usableDate)
    }

    return MembershipCardSpec(
        glyph = MembershipGlyph.VERIFIED,
        headerLabelRes = R.string.label_membership_card_subscription,
        badgeRes = R.string.label_active_caps,
        titleRes = R.string.title_membership_pro_card,
        facts = listOfNotNull(
            dateFact,
            // Stated even when the date is not, and even when a payment failed: Play is where
            // this is billed either way, and that is the fact the user acts on.
            MembershipFact.Label(MembershipGlyph.VERIFIED, R.string.label_membership_managed_by_play),
        ),
        panel = MembershipPanel(
            glyph = MembershipGlyph.VERIFIED,
            // A failed charge is the one thing that outranks "everything is unlocked": the
            // store has said it cannot collect, so that promise must not be made.
            headlineRes = if (hasBillingIssue) {
                R.string.msg_subscription_billing_issue
            } else {
                R.string.label_membership_unlocked_headline
            },
            bodyRes = R.string.msg_membership_unlocked_body,
        ),
    )
}

private fun proPassCardSpec(proExpiryTimestamp: Long, now: Long): MembershipCardSpec {
    val isRunning = proExpiryTimestamp > now

    return MembershipCardSpec(
        glyph = MembershipGlyph.TICKET,
        headerLabelRes = R.string.label_membership_card_pro_pass,
        // Both Pro states are Pro, and the surface colour alone only says access is paid for
        // in some way — the badge is what says which way.
        badgeRes = R.string.label_membership_badge_free_pass,
        titleRes = R.string.title_membership_pro_card,
        facts = if (isRunning) {
            listOf(
                MembershipFact.Count(
                    glyph = MembershipGlyph.CLOCK,
                    pluralsRes = R.plurals.label_membership_days_remaining,
                    count = daysRemaining(proExpiryTimestamp, now),
                ),
                MembershipFact.Date(
                    glyph = MembershipGlyph.DATE,
                    labelRes = R.string.label_expires_on,
                    valueMillis = proExpiryTimestamp,
                ),
            )
        } else {
            // Pro with no recorded expiry: a legacy permanent grant. There is no countdown to
            // show, and dating it would be inventing one.
            emptyList()
        },
        panel = MembershipPanel(
            glyph = MembershipGlyph.TICKET,
            headlineRes = R.string.label_membership_pass_headline,
            // The pass sentence says the access dies on its own; that is exactly what is
            // untrue of a permanent grant, so the legacy state says something else.
            bodyRes = if (isRunning) {
                R.string.msg_membership_pass_body
            } else {
                R.string.msg_pro_active_no_expiry
            },
        ),
    )
}

/**
 * Whole days left before [expiryMillis], rounded up so an hour short of a day still reads as
 * a day remaining, and never negative.
 */
internal fun daysRemaining(expiryMillis: Long, now: Long): Int {
    val remainingMillis = expiryMillis - now
    if (remainingMillis <= 0L) return 0
    return ((remainingMillis + MILLIS_PER_DAY - 1) / MILLIS_PER_DAY).toInt()
}

private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
