package com.mknlabs.expensetracker.domain.repository

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
    suspend fun syncUserProfile(): Result<Unit>

    /**
     * Triggers a full synchronization (Push local data to cloud, Pull cloud data to local).
     */
    suspend fun syncTransactions(): Result<Unit>
}
