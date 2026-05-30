package com.mkn0079.expensetracker.domain.repository

import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.monetization.Feature
import kotlinx.coroutines.flow.Flow

interface MonetizationRepository {
    /**
     * Emits true if ads should be shown, false if the user is premium or has an active pass.
     */
    val isAdsEnabled: Flow<Boolean>

    /**
     * Emits the timestamp (ms) when the current temporary ad-free pass expires.
     */
    val globalAdAccessExpiry: Flow<Long>

    /**
     * Observes the access status for a specific feature or option.
     */
    fun observeAccessStatus(feature: Feature, optionId: String? = null): Flow<AccessStatus>

    /**
     * Grants temporary access to a feature (e.g., after watching an ad).
     */
    suspend fun grantTemporaryAccess(feature: Feature, optionId: String? = null, durationMillis: Long)

    /**
     * Simulates premium access for testing.
     */
    suspend fun becomePremium()
}
