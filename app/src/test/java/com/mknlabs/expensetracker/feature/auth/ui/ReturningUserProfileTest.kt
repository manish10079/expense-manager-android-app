package com.mknlabs.expensetracker.feature.auth.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the onboarding "skip already-completed steps" logic for returning users:
 *  - name + gender already in Firestore ⇒ skip straight to the Welcome Back page,
 *  - either of them missing ⇒ show the setup (name/gender) page,
 *  - the financial goal never decides the route on its own.
 */
class ReturningUserProfileTest {

    @Test
    fun emptyProfile_hasNothingComplete() {
        val profile = ReturningUserProfile("", "", "")

        assertFalse(profile.hasName)
        assertFalse(profile.hasGender)
        assertFalse(profile.hasGoal)
        assertFalse(profile.isComplete)
        assertEquals(ReturningUserStep.SETUP_PROFILE, resolveReturningUserStep(profile))
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
    fun missingGoal_stillRoutesToWelcomeBack() {
        // The regression this guards: a stored goal could be erased by a blank local
        // value, and requiring it then pushed the user back through onboarding instead
        // of welcoming them. Identity is enough.
        val profile = ReturningUserProfile("John Doe", "Male", "")

        assertTrue(profile.isComplete)
        assertEquals(ReturningUserStep.WELCOME_BACK, resolveReturningUserStep(profile))
    }

    @Test
    fun missingGender_routesToSetupProfileEvenWithAGoal() {
        val profile = ReturningUserProfile("John Doe", "", "Home")

        assertTrue(profile.hasGoal)
        assertFalse(profile.isComplete)
        assertEquals(ReturningUserStep.SETUP_PROFILE, resolveReturningUserStep(profile))
    }

    @Test
    fun missingName_routesToSetupProfile() {
        assertEquals(
            ReturningUserStep.SETUP_PROFILE,
            resolveReturningUserStep(ReturningUserProfile("", "Male", "Home"))
        )
    }

    @Test
    fun guestUserName_isNotConsideredARealName() {
        val profile = ReturningUserProfile("Guest User", "Male", "Home")

        assertFalse(profile.hasName)
        assertFalse(profile.isComplete)
        assertEquals(ReturningUserStep.SETUP_PROFILE, resolveReturningUserStep(profile))
    }

    @Test
    fun onlyGoalPresent_routesToSetupProfile() {
        // Goal already chosen on another device ⇒ ask for the identity fields.
        val profile = ReturningUserProfile("", "", "Home")

        assertTrue(profile.hasGoal)
        assertFalse(profile.hasName)
        assertFalse(profile.hasGender)
        assertEquals(ReturningUserStep.SETUP_PROFILE, resolveReturningUserStep(profile))
    }
}
