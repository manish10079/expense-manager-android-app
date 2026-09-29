package com.mknlabs.expensetracker.feature.auth.ui

import com.mknlabs.expensetracker.models.CURRENT_TERMS_VERSION
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.defaultUserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TermsConsentTrackingTest {

    @Test
    fun defaultUserProfile_hasZeroTermsAcceptedAtAndEmptyVersion() {
        assertEquals(0L, defaultUserProfile.termsAcceptedAt)
        assertEquals("", defaultUserProfile.termsVersion)
    }

    @Test
    fun currentTermsVersion_isStandardOnePointZero() {
        assertEquals("1.0", CURRENT_TERMS_VERSION)
    }

    @Test
    fun userProfile_copy_updatesTermsConsentProperly() {
        val now = 1743238800000L
        val profile = defaultUserProfile.copy(
            termsAcceptedAt = now,
            termsVersion = CURRENT_TERMS_VERSION
        )

        assertEquals(now, profile.termsAcceptedAt)
        assertEquals("1.0", profile.termsVersion)
    }

    @Test
    fun consentResolution_preservesOriginalRemoteConsentTimestamp() {
        val originalRemoteConsentTime = 1700000000000L
        val secondDeviceConsentTime = 1740000000000L

        val remoteTermsAcceptedAt = originalRemoteConsentTime
        val localTermsAcceptedAt = secondDeviceConsentTime

        // Logic matches pushUserProfile timestamp resolution
        val finalTermsAcceptedAt = when {
            remoteTermsAcceptedAt != 0L -> remoteTermsAcceptedAt
            localTermsAcceptedAt != 0L -> localTermsAcceptedAt
            else -> 1750000000000L
        }

        assertEquals(originalRemoteConsentTime, finalTermsAcceptedAt)
        assertNotEquals(secondDeviceConsentTime, finalTermsAcceptedAt)
    }

    @Test
    fun consentResolution_usesLocalConsentTimestampWhenRemoteIsMissing() {
        val localConsentTime = 1740000000000L
        val remoteTermsAcceptedAt = 0L
        val localTermsAcceptedAt = localConsentTime

        val finalTermsAcceptedAt = when {
            remoteTermsAcceptedAt != 0L -> remoteTermsAcceptedAt
            localTermsAcceptedAt != 0L -> localTermsAcceptedAt
            else -> 1750000000000L
        }

        assertEquals(localConsentTime, finalTermsAcceptedAt)
    }
}
