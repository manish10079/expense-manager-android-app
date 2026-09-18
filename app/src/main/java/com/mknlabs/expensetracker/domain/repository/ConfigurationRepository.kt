package com.mknlabs.expensetracker.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface ConfigurationRepository {
    val minRequiredVersion: StateFlow<Int>
    val isUnderMaintenance: StateFlow<Boolean>
    val currentPromoCode: StateFlow<String>
    val isProPassEnabled: StateFlow<Boolean>
    val isSyncEnabled: StateFlow<Boolean>
    val maxSyncDevices: StateFlow<Int>
    val googleSheetsFeedbackUrl: StateFlow<String>
    val isProGatingEnabled: StateFlow<Boolean>

    /**
     * How long the global ad pass granted by a rewarded ad lasts, in minutes.
     * Backed by the `ad_pass_duration_minutes` Remote Config parameter so the length can be
     * tuned from the Firebase console without shipping a build.
     */
    val adPassDurationMinutes: StateFlow<Int>

    fun fetchAndActivate()
    fun isUpdateRequired(): Boolean
}
