package com.mkn0079.expensetracker.domain.repository

import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.monetization.Feature
import kotlinx.coroutines.flow.Flow

interface MonetizationRepository {
    /**
     * Observes the access status for a specific feature or option.
     */
    fun observeAccessStatus(feature: Feature, optionId: String? = null): Flow<AccessStatus>

    /**
     * Grants temporary access to a feature (e.g., after watching an ad).
     */
    suspend fun grantTemporaryAccess(feature: Feature, optionId: String? = null, durationMillis: Long)

    /**
     * Grants permanent premium access (simulates a purchase).
     */
    suspend fun becomePremium()
}
