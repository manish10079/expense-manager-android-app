package com.mknlabs.expensetracker.domain.repository

/**
 * Registers this device's FCM token so Cloud Functions can push alerts to it.
 * Token docs live at /users/{uid}/fcmTokens/{deviceId} (notification plan §5.6)
 * — the server reads them to know which tokens to target.
 */
interface FcmTokenRepository {

    /**
     * Upserts this device's FCM token under the signed-in user.
     * No-op (returns success) when nobody is signed in.
     */
    suspend fun registerCurrentDeviceToken(token: String): Result<Unit>

    /**
     * Deletes this device's token doc for the signed-in user (sign-out cleanup).
     * No-op (returns success) when nobody is signed in.
     */
    suspend fun removeCurrentDeviceToken(): Result<Unit>
}
