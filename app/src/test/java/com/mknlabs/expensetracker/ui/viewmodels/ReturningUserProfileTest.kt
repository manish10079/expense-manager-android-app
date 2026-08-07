package com.mknlabs.expensetracker.ui.viewmodels

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the onboarding "skip already-completed steps" logic for returning users:
 *  - a financial goal already in Firestore ⇒ skip the goal page,
 *  - name + gender already in Firestore ⇒ skip the setup page,
 *  - all three empty ⇒ show everything (goal + setup),
 *  - all three present ⇒ jump straight to the Welcome Back page.
 */
class ReturningUserProfileTest {

    @Test
    fun emptyProfile_hasNothingComplete() {
        val profile = ReturningUserProfile("", "", "")

        assertFalse(profile.hasName)
        assertFalse(profile.hasGender)
        assertFalse(profile.hasGoal)
        assertFalse(profile.isComplete)
        assertEquals(ReturningUserStep.FINANCIAL_GOAL, resolveReturningUserStep(profile))
    }

    @Test
    fun completeProfile_isCompleteAndRoutesToWelcomeBack() {
        val profile = ReturningUserProfile("John Doe", "Male", "Home")

        assertTrue(profile.hasName)
        assertTrue(profile.hasGender)
        assertTrue(profile.hasGoal)
        assertTrue(profile.isComplete)
        assertEquals(ReturningUserStep.WELCOME_BACK, resolveReturningUserStep(profile))
    }

    @Test
    fun guestUserName_isNotConsideredARealName() {
        val profile = ReturningUserProfile("Guest User", "Male", "Home")

        assertFalse(profile.hasName)
        assertFalse(profile.isComplete)
    }

    @Test
    fun missingGoal_routesToFinancialGoalAndSkipsGoalCheck() {
        // Name + gender exist but no goal ⇒ user must still see the goal page.
        val profile = ReturningUserProfile("John Doe", "Male", "")

        assertEquals(ReturningUserStep.FINANCIAL_GOAL, resolveReturningUserStep(profile))
    }

    @Test
    fun missingNameOrGender_routesToSetupProfile() {
        assertEquals(
            ReturningUserStep.SETUP_PROFILE,
            resolveReturningUserStep(ReturningUserProfile("", "Male", "Home"))
        )
        assertEquals(
            ReturningUserStep.SETUP_PROFILE,
            resolveReturningUserStep(ReturningUserProfile("John Doe", "", "Home"))
        )
    }

    @Test
    fun onlyNamePresent_stillRoutesToFinancialGoal() {
        // Name exists but gender and goal are missing: goal first, setup after.
        val profile = ReturningUserProfile("John Doe", "", "")

        assertEquals(ReturningUserStep.FINANCIAL_GOAL, resolveReturningUserStep(profile))
    }

    @Test
    fun onlyGoalPresent_routesToSetupProfile() {
        // Goal already chosen on another device ⇒ skip the goal page, ask for name/gender.
        val profile = ReturningUserProfile("", "", "Home")

        assertTrue(profile.hasGoal)
        assertFalse(profile.hasName)
        assertFalse(profile.hasGender)
        assertEquals(ReturningUserStep.SETUP_PROFILE, resolveReturningUserStep(profile))
    }
}
