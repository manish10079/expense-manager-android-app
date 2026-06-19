package com.mknlabs.expensetracker.data.repository

import android.content.Context
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.MonetizationDataStore
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.monetization.AccessLevel
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.FeatureRegistry
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

    override val isAdsEnabled: Flow<Boolean> = combine(
        AppSettingsDataStore.getAppSettingsFlow(context),
        UserProfileDataStore.getUserProfileFlow(context),
        MonetizationDataStore.getGlobalAdAccessExpiry(context)
    ) { settings, profile, globalAdExpiry ->
        val now = System.currentTimeMillis()
        val isPremium = settings.userTier == com.mknlabs.expensetracker.models.UserTier.PREMIUM ||
                (profile.accountTier == "PREMIUM" && profile.proExpiryTimestamp > now)
        
        val hasActivePass = globalAdExpiry > now
        
        // Ads are enabled if NOT premium AND NOT having an active pass
        !isPremium && !hasActivePass
    }

    override val globalAdAccessExpiry: Flow<Long> = MonetizationDataStore.getGlobalAdAccessExpiry(context)

    override fun observeAccessStatus(feature: Feature, optionId: String?): Flow<AccessStatus> {
        return combine(
            AppSettingsDataStore.getAppSettingsFlow(context),
            UserProfileDataStore.getUserProfileFlow(context),
            MonetizationDataStore.getGlobalAdAccessExpiry(context)
        ) { settings, profile, globalAdExpiry ->
            val now = System.currentTimeMillis()
            // 1. Check if user is Premium (permanent or active temporary ProPass)
            val isPremium = settings.userTier == com.mknlabs.expensetracker.models.UserTier.PREMIUM ||
                    (profile.accountTier == "PREMIUM" && profile.proExpiryTimestamp > now)
            
            if (isPremium) {
                return@combine AccessStatus.Granted
            }
            
            val requiredLevel = FeatureRegistry.getAccessLevel(feature, optionId)
            
            when (requiredLevel) {
                AccessLevel.FREE -> AccessStatus.Granted
                
                // Premium features are ONLY granted if user is permanent Premium and not expired
                AccessLevel.PREMIUM -> AccessStatus.DeniedPremium
                
                // Ad-supported features are granted if the Global Pass is active
                AccessLevel.AD_SUPPORTED -> {
                    if (globalAdExpiry > now) {
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
        val now = System.currentTimeMillis()
        
        // 1. Update App Settings (Primary source for UI features & ads)
        AppSettingsDataStore.updateUserTier(context, com.mknlabs.expensetracker.models.UserTier.PREMIUM)
        
        // 2. Update User Profile (Ensures the status is synced to Firestore)
        UserProfileDataStore.updateUserProfile(context) { profile ->
            profile.copy(
                accountTier = com.mknlabs.expensetracker.models.UserTier.PREMIUM.name,
                updatedAtMillis = now
            )
        }
        
        // Clear any temporary passes as they are no longer needed
        MonetizationDataStore.updateGlobalAdAccessExpiry(context, 0L)
    }
}
