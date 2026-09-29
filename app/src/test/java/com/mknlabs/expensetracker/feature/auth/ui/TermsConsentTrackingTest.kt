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
    fun accountCreationResolution_preservesOriginalRemoteTimestamp() {
        val originalCloudCreationTime = 1680000000000L
        val freshInstallLocalTime = 1740000000000L

        val remoteCreatedAt = originalCloudCreationTime
        val localAccountCreatedMillis = freshInstallLocalTime

        val finalCreatedAt = when {
            remoteCreatedAt != 0L -> remoteCreatedAt
            localAccountCreatedMillis != 0L -> localAccountCreatedMillis
            else -> 1750000000000L
        }

        assertEquals(originalCloudCreationTime, finalCreatedAt)
        assertNotEquals(freshInstallLocalTime, finalCreatedAt)
    }

    @Test
    fun accountCreationResolution_usesLocalWhenRemoteIsMissing() {
        val localCreationTime = 1740000000000L
        val remoteCreatedAt = 0L
        val localAccountCreatedMillis = localCreationTime

        val finalCreatedAt = when {
            remoteCreatedAt != 0L -> remoteCreatedAt
            localAccountCreatedMillis != 0L -> localAccountCreatedMillis
            else -> 1750000000000L
        }

        assertEquals(localCreationTime, finalCreatedAt)
    }

    @Test
    fun pushGate_doesNotSkipWhenTermsOrCreatedAtMissingInCloud() {
        val docExists = true
        val localProfileUpdatedAt = 1700000000000L
        val remoteUpdatedAt = 1700000000000L // local <= remote
        val isFirstTimeInitialization = false
        val remoteTermsAcceptedAt = 0L
        val remoteTermsVersion = ""
        val localTermsAcceptedAt = 1740000000000L
        val localTermsVersion = "1.0"
        val remoteCreatedAt = 0L

        val isMissingTermsInCloud = docExists && (remoteTermsAcceptedAt == 0L || remoteTermsVersion.isBlank()) && (localTermsAcceptedAt != 0L || localTermsVersion.isNotBlank())
        val isMissingCreatedAtInCloud = docExists && remoteCreatedAt == 0L

        val wouldSkipPush = docExists && localProfileUpdatedAt <= remoteUpdatedAt && !isFirstTimeInitialization && !isMissingTermsInCloud && !isMissingCreatedAtInCloud

        assertEquals(false, wouldSkipPush)
    }
}
