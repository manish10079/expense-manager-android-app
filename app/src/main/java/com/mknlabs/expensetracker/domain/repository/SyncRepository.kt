package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.UserProfile
import kotlinx.coroutines.flow.StateFlow

/**
 * Domain model representing a device registered for cloud sync.
 */
data class RegisteredDevice(
    val id: String,
    val modelName: String,
    val lastActiveMillis: Long,
    val isCurrentDevice: Boolean
)

/**
 * Domain-level interface for cloud synchronization and device management.
 */
interface SyncRepository {
    /**
     * Emits a list of all devices registered under the current user's account.
     */
    val registeredDevices: StateFlow<List<RegisteredDevice>>

    /**
     * Whether the cloud sync is currently active and authorized.
     */
    val isSyncEnabled: StateFlow<Boolean>

    /**
     * Whether a synchronization operation is currently in progress.
     */
    val isSyncing: StateFlow<Boolean>

    /**
     * Registers the current device in the cloud.
     * Returns a [Result] indicating success or failure (e.g., limit reached).
     */
    suspend fun registerCurrentDevice(): Result<Unit>

    /**
     * Unregisters a specific device by its unique ID.
     */
    suspend fun unregisterDevice(deviceId: String): Result<Unit>

    /**
     * Refreshes the list of registered devices from the cloud.
     */
    suspend fun refreshDevices(): Result<Unit>

    /**
     * Synchronizes the user's profile document to the cloud and back.
     */
    suspend fun syncUserProfile(isNewUser: Boolean = false): Result<Unit>

    /**
     * Triggers a full synchronization (Push local data to cloud, Pull cloud data to local).
     */
    suspend fun syncTransactions(): Result<Unit>

    /**
     * Reads the user's profile document from Firestore without writing anything.
     * Returns null when the document does not exist or the read fails.
     *
     * Used by onboarding to decide which setup steps a returning user can skip
     * (financial goal, name/gender) after signing in with Google or email/password.
     */
    suspend fun fetchUserProfileFromCloud(uid: String): UserProfile?

    /**
     * Force push local changes and pull cloud changes, ignoring the last sync time.
     */
    suspend fun forceSyncTransactions(): Result<Unit>
}
