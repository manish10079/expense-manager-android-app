package com.mkn0079.expensetracker.data.repository

import android.content.Context
import com.mkn0079.expensetracker.data.local.AppSettingsDataStore
import com.mkn0079.expensetracker.domain.repository.MonetizationRepository
import com.mkn0079.expensetracker.models.UserTier
import com.mkn0079.expensetracker.monetization.AccessLevel
import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.monetization.AdAccessStore
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.monetization.FeatureRegistry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonetizationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MonetizationRepository {

    override fun observeAccessStatus(feature: Feature, optionId: String?): Flow<AccessStatus> {
        val featureKey = getFeatureKey(feature, optionId)
        
        return combine(
            AppSettingsDataStore.getAppSettingsFlow(context),
            AdAccessStore.unlockedFeatures
        ) { settings, temporaryUnlocks ->
            val tier = settings.userTier
            
            // 1. Premium users have full access
            if (tier == UserTier.PREMIUM) return@combine AccessStatus.Granted
            
            // 2. Check registry for level
            val requiredLevel = FeatureRegistry.getAccessLevel(feature, optionId)
            
            when (requiredLevel) {
                AccessLevel.FREE -> AccessStatus.Granted
                AccessLevel.PREMIUM -> AccessStatus.DeniedPremium
                AccessLevel.AD_SUPPORTED -> {
                    val expiry = temporaryUnlocks[featureKey] ?: 0L
                    if (expiry > System.currentTimeMillis()) {
                        AccessStatus.Granted
                    } else {
                        AccessStatus.DeniedAd
                    }
                }
            }
        }
    }

    override suspend fun grantTemporaryAccess(feature: Feature, optionId: String?, durationMillis: Long) {
        AdAccessStore.grantAccess(feature, optionId, durationMillis)
    }

    override suspend fun becomePremium() {
        AppSettingsDataStore.updateAppSettings(context) { settings ->
            settings.copy(userTier = UserTier.PREMIUM)
        }
    }

    private fun getFeatureKey(feature: Feature, optionId: String?): String {
        return if (optionId != null) "${feature.id}_$optionId" else feature.id
    }
}
