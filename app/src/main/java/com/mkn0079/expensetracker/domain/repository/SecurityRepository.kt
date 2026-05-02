package com.mkn0079.expensetracker.domain.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for handling app security and lock state.
 * Follows the Fail-Secure principle: if state is uncertain, it should be considered locked.
 */
interface SecurityRepository {
    /**
     * Checks if a security PIN has been set by the user.
     */
    fun hasPin(): Boolean

    /**
     * Checks if the app is currently configured to be locked.
     */
    fun isLockEnabled(): Boolean

    /**
     * Records the current time as the last time the app was backgrounded.
     */
    fun markBackgrounded(timestamp: Long = System.currentTimeMillis())

    /**
     * Records the current time as the last time the app was successfully unlocked.
     */
    fun markUnlocked(timestamp: Long = System.currentTimeMillis())

    /**
     * Determines if the app should require an unlock based on the auto-lock timeout.
     */
    fun shouldRequireUnlock(): Boolean

    /**
     * Validates a PIN attempt.
     */
    fun validatePin(pin: String): Boolean

    /**
     * Returns the auto-lock duration in minutes.
     */
    fun getAutoLockDurationMinutes(): Int

    /**
     * Notifies the repository that the app has returned to the foreground.
     */
    fun notifyAppForeground()

    /**
     * A flow that emits when the app returns to the foreground.
     */
    val appForegroundEvents: kotlinx.coroutines.flow.Flow<Unit>
}
