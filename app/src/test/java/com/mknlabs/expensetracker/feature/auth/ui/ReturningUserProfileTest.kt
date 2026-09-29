package com.mknlabs.expensetracker.feature.auth.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the onboarding "skip already-completed steps" logic for returning users:
 *  - name + gender already in Firestore ⇒ skip straight to the Welcome Back page,
 *  - either of them missing ⇒ show the setup (name/gender) page.
 */
class ReturningUserProfileTest {

    @Test
    fun emptyProfile_hasNothingComplete() {
        val profile = ReturningUserProfile("", "")

        assertFalse(profile.hasName)
        assertFalse(profile.hasGender)
        assertFalse(profile.isComplete)
        assertEquals(ReturningUserStep.SETUP_PROFILE, resolveReturningUserStep(profile))
    }

    @Test
    fun completeProfile_isCompleteAndRoutesToWelcomeBack() {
        val profile = ReturningUserProfile("John Doe", "Male")

        assertTrue(profile.hasName)
        assertTrue(profile.hasGender)
        assertTrue(profile.isComplete)
        assertEquals(ReturningUserStep.WELCOME_BACK, resolveReturningUserStep(profile))
    }

    @Test
    fun missingGender_routesToSetupProfile() {
        val profile = ReturningUserProfile("John Doe", "")

        assertTrue(profile.hasName)
        assertFalse(profile.hasGender)
        assertFalse(profile.isComplete)
        assertEquals(ReturningUserStep.SETUP_PROFILE, resolveReturningUserStep(profile))
    }

    @Test
    fun missingName_routesToSetupProfile() {
        assertEquals(
            ReturningUserStep.SETUP_PROFILE,
            resolveReturningUserStep(ReturningUserProfile("", "Male"))
        )
    }

    @Test
    fun guestUserName_isNotConsideredARealName() {
        val profile = ReturningUserProfile("Guest User", "Male")

        assertFalse(profile.hasName)
        assertFalse(profile.isComplete)
        assertEquals(ReturningUserStep.SETUP_PROFILE, resolveReturningUserStep(profile))
    }
}
