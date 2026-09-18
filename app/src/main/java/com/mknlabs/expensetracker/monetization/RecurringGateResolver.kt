package com.mknlabs.expensetracker.monetization

import com.mknlabs.expensetracker.models.RecurringFrequency

/**
 * Which slice of the recurring allowance a rule count currently falls in.
 * Used by the hint UI to explain the ladder to free users.
 */
enum class RecurringRuleTier { FREE, AD, PREMIUM }

/**
 * Maps a recurring-rule situation onto the [Feature] that governs it.
 *
 * Every function returns null when the situation is free for everyone. Callers must
 * treat null as granted and must never synthesise an [AccessStatus] themselves: the
 * Pro bypass, the ad-pass check and the pro_gating_enabled kill switch all live in
 * MonetizationRepository.observeAccessStatus, which is the single source of truth.
 */
object RecurringGateResolver {

    /** Rules 1-3 are free for every user. */
    const val FREE_RULE_LIMIT = 3

    /** Rules 4-6 are ad-supported; the 7th and beyond need Pro. */
    const val AD_RULE_LIMIT = 6

    /**
     * Gate for adding one more rule when [activeRuleCount] rules already exist,
     * or null while the free allowance lasts.
     */
    fun ruleCreationFeature(activeRuleCount: Int): Feature? = when {
        activeRuleCount < FREE_RULE_LIMIT -> null
        activeRuleCount < AD_RULE_LIMIT -> Feature.RECURRING_RULES_MULTI
        else -> Feature.RECURRING_RULES_UNLIMITED
    }

    /**
     * Gate for flipping the recurring switch on a transaction that already carries a
     * rule. Editing creates nothing, so the count ladder is deliberately not consulted.
     */
    fun ruleEditFeature(): Feature = Feature.RECURRING_RULE_EDIT

    /**
     * Gate for [frequency], or null when that frequency is always free (Monthly).
     */
    fun frequencyFeature(frequency: RecurringFrequency): Feature? = when (frequency) {
        RecurringFrequency.Daily -> Feature.RECURRING_FREQUENCY_DAILY
        RecurringFrequency.Weekly -> Feature.RECURRING_FREQUENCY_WEEKLY
        RecurringFrequency.Yearly -> Feature.RECURRING_FREQUENCY_YEARLY
        RecurringFrequency.Monthly -> null
    }

    /**
     * Gate for enabling the recurring switch: editing an existing rule, or creating a new one.
     */
    fun enableFeature(activeRuleCount: Int, hasExistingRule: Boolean): Feature? =
        if (hasExistingRule) ruleEditFeature() else ruleCreationFeature(activeRuleCount)

    /**
     * The tier slice [activeRuleCount] falls into, for the hint text.
     */
    fun ruleTier(activeRuleCount: Int): RecurringRuleTier = when {
        activeRuleCount < FREE_RULE_LIMIT -> RecurringRuleTier.FREE
        activeRuleCount < AD_RULE_LIMIT -> RecurringRuleTier.AD
        else -> RecurringRuleTier.PREMIUM
    }
}
