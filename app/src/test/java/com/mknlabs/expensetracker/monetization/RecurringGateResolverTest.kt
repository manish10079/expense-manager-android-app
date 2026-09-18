package com.mknlabs.expensetracker.monetization

import com.mknlabs.expensetracker.models.RecurringFrequency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Covers the recurring-rule gate ladder that replaced the hardcoded AccessStatus
 * table in AddTransactionScreen.
 *
 * Two things are locked down here:
 *  1. The situation -> Feature mapping, before the tier check. A Pro bypass or the
 *     pro_gating_enabled kill switch is applied later, inside
 *     MonetizationRepository.observeAccessStatus, so it is intentionally absent here.
 *  2. The registry contract this mapping depends on. If someone reclassifies a
 *     recurring feature, these tests fail rather than the gate silently changing.
 */
class RecurringGateResolverTest {

    // --- rule creation ladder: 1-3 free, 4-6 ad, 7+ premium ---

    @Test
    fun `rules one to three are ungated`() {
        assertNull(RecurringGateResolver.ruleCreationFeature(0))
        assertNull(RecurringGateResolver.ruleCreationFeature(1))
        assertNull(RecurringGateResolver.ruleCreationFeature(2))
    }

    @Test
    fun `rules four to six require an ad`() {
        assertSame(Feature.RECURRING_RULES_MULTI, RecurringGateResolver.ruleCreationFeature(3))
        assertSame(Feature.RECURRING_RULES_MULTI, RecurringGateResolver.ruleCreationFeature(4))
        assertSame(Feature.RECURRING_RULES_MULTI, RecurringGateResolver.ruleCreationFeature(5))
    }

    @Test
    fun `seventh rule and beyond require premium`() {
        assertSame(Feature.RECURRING_RULES_UNLIMITED, RecurringGateResolver.ruleCreationFeature(6))
        assertSame(Feature.RECURRING_RULES_UNLIMITED, RecurringGateResolver.ruleCreationFeature(7))
        assertSame(Feature.RECURRING_RULES_UNLIMITED, RecurringGateResolver.ruleCreationFeature(50))
    }

    @Test
    fun `ladder boundaries sit exactly on three and six`() {
        assertNull(RecurringGateResolver.ruleCreationFeature(RecurringGateResolver.FREE_RULE_LIMIT - 1))
        assertSame(
            Feature.RECURRING_RULES_MULTI,
            RecurringGateResolver.ruleCreationFeature(RecurringGateResolver.FREE_RULE_LIMIT)
        )
        assertSame(
            Feature.RECURRING_RULES_UNLIMITED,
            RecurringGateResolver.ruleCreationFeature(RecurringGateResolver.AD_RULE_LIMIT)
        )
    }

    // --- editing vs creating ---

    @Test
    fun `editing an existing rule gates on rule edit, not the count ladder`() {
        // Even a user well past the premium boundary is editing, not creating,
        // so the count ladder must not be consulted.
        listOf(0, 3, 6, 99).forEach { count ->
            assertSame(
                Feature.RECURRING_RULE_EDIT,
                RecurringGateResolver.enableFeature(activeRuleCount = count, hasExistingRule = true)
            )
        }
    }

    @Test
    fun `creating a rule uses the count ladder`() {
        assertNull(RecurringGateResolver.enableFeature(activeRuleCount = 2, hasExistingRule = false))
        assertSame(
            Feature.RECURRING_RULES_MULTI,
            RecurringGateResolver.enableFeature(activeRuleCount = 3, hasExistingRule = false)
        )
        assertSame(
            Feature.RECURRING_RULES_UNLIMITED,
            RecurringGateResolver.enableFeature(activeRuleCount = 6, hasExistingRule = false)
        )
    }

    @Test
    fun `a transaction with no rule is gated by count even when the transaction is being edited`() {
        // Regression: the old gate short-circuited on "editing any transaction" and
        // handed out unlimited free rules. Only an existing *rule* may skip the ladder.
        assertSame(
            Feature.RECURRING_RULES_UNLIMITED,
            RecurringGateResolver.enableFeature(activeRuleCount = 9, hasExistingRule = false)
        )
    }

    // --- frequency ---

    @Test
    fun `daily weekly and yearly are gated`() {
        assertSame(
            Feature.RECURRING_FREQUENCY_DAILY,
            RecurringGateResolver.frequencyFeature(RecurringFrequency.Daily)
        )
        assertSame(
            Feature.RECURRING_FREQUENCY_WEEKLY,
            RecurringGateResolver.frequencyFeature(RecurringFrequency.Weekly)
        )
        assertSame(
            Feature.RECURRING_FREQUENCY_YEARLY,
            RecurringGateResolver.frequencyFeature(RecurringFrequency.Yearly)
        )
    }

    @Test
    fun `monthly is never gated`() {
        // Monthly is the default frequency. Gating it would lock recurring
        // transactions for every free user.
        assertNull(RecurringGateResolver.frequencyFeature(RecurringFrequency.Monthly))
    }

    // --- tier position for the hint UI ---

    @Test
    fun `rule tier tracks the ladder`() {
        assertEquals(RecurringRuleTier.FREE, RecurringGateResolver.ruleTier(0))
        assertEquals(RecurringRuleTier.FREE, RecurringGateResolver.ruleTier(2))
        assertEquals(RecurringRuleTier.AD, RecurringGateResolver.ruleTier(3))
        assertEquals(RecurringRuleTier.AD, RecurringGateResolver.ruleTier(5))
        assertEquals(RecurringRuleTier.PREMIUM, RecurringGateResolver.ruleTier(6))
        assertEquals(RecurringRuleTier.PREMIUM, RecurringGateResolver.ruleTier(20))
    }

    // --- registry contract ---

    @Test
    fun `registry classifies recurring features as the ladder assumes`() {
        assertEquals(AccessLevel.FREE, FeatureRegistry.getAccessLevel(Feature.RECURRING_FREQUENCY_MONTHLY))
        assertEquals(AccessLevel.AD_SUPPORTED, FeatureRegistry.getAccessLevel(Feature.RECURRING_RULES_MULTI))
        assertEquals(AccessLevel.PREMIUM, FeatureRegistry.getAccessLevel(Feature.RECURRING_RULES_UNLIMITED))
        assertEquals(AccessLevel.AD_SUPPORTED, FeatureRegistry.getAccessLevel(Feature.RECURRING_RULE_EDIT))
        assertEquals(AccessLevel.AD_SUPPORTED, FeatureRegistry.getAccessLevel(Feature.RECURRING_FREQUENCY_DAILY))
        assertEquals(AccessLevel.AD_SUPPORTED, FeatureRegistry.getAccessLevel(Feature.RECURRING_FREQUENCY_WEEKLY))
        assertEquals(AccessLevel.PREMIUM, FeatureRegistry.getAccessLevel(Feature.RECURRING_FREQUENCY_YEARLY))
    }
}
