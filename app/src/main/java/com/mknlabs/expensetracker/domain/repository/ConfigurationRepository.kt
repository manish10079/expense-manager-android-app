package com.mknlabs.expensetracker.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface ConfigurationRepository {
    val minRequiredVersion: StateFlow<Int>
    val isUnderMaintenance: StateFlow<Boolean>
    val currentPromoCode: StateFlow<String>
    val isSyncEnabled: StateFlow<Boolean>
    val maxSyncDevices: StateFlow<Int>

    fun fetchAndActivate()
    fun isUpdateRequired(): Boolean
}
