package com.mkn0079.expensetracker.data.repository

import android.content.Context
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.data.local.MonetizationDataStore
import com.mkn0079.expensetracker.domain.repository.MonetizationRepository
import com.mkn0079.expensetracker.monetization.AccessLevel
import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.monetization.FeatureRegistry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonetizationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MonetizationRepository {
    private companion object {
        const val TEST_PREMIUM_DURATION_MILLIS = 1 * 60 * 60 * 1000L
    }

    override fun observeAccessStatus(feature: Feature, optionId: String?): Flow<AccessStatus> {
        return combine(
            AppSettingsDataStore.getAppSettingsFlow(context),
            MonetizationDataStore.getGlobalAdAccessExpiry(context)
        ) { settings, globalAdExpiry ->
            // 1. Check if user is permanent Premium
            if (settings.userTier.name == "PREMIUM") {
                return@combine AccessStatus.Granted
            }
            
            val requiredLevel = FeatureRegistry.getAccessLevel(feature, optionId)
            
            when (requiredLevel) {
                AccessLevel.FREE -> AccessStatus.Granted
                
                // Premium features are ONLY granted if user is permanent Premium
                AccessLevel.PREMIUM -> AccessStatus.DeniedPremium
                
                // Ad-supported features are granted if the Global Pass is active
                AccessLevel.AD_SUPPORTED -> {
                    if (globalAdExpiry > System.currentTimeMillis()) {
                        AccessStatus.Granted
                    } else {
                        AccessStatus.DeniedAd
                    }
                }
            }
        }
    }

    override suspend fun grantTemporaryAccess(feature: Feature, optionId: String?, durationMillis: Long) {
        // In the Global Pass strategy, we ignore the specific feature and grant access to ALL ad-gated features
        val newExpiry = System.currentTimeMillis() + durationMillis
        MonetizationDataStore.updateGlobalAdAccessExpiry(context, newExpiry)
    }

    override suspend fun becomePremium() {
        // This is a test method to simulate being premium
        AppSettingsDataStore.updateAppSettings(context) { settings ->
            settings.copy(userTier = com.mkn0079.expensetracker.models.UserTier.FREE)
        }
        // Grant a 2-hour global pass for testing
        val newExpiry = System.currentTimeMillis() + TEST_PREMIUM_DURATION_MILLIS
        MonetizationDataStore.updateGlobalAdAccessExpiry(context, newExpiry)
    }
}
