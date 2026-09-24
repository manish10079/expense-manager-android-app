package com.mknlabs.expensetracker.data.repository

import android.content.Context
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.MonetizationDataStore
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.domain.repository.BillingRepository
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.monetization.AccessLevel
import com.mknlabs.expensetracker.monetization.EntitlementResolver
import com.mknlabs.expensetracker.monetization.StoreEntitlement
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.FeatureRegistry
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.benchmark.BenchmarkHooks
import com.mknlabs.expensetracker.domain.repository.ConfigurationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonetizationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configurationRepository: ConfigurationRepository,
    /**
     * The store's entitlement source. Read here rather than from Firestore on purpose: a
     * completed purchase must unlock features immediately, even while the RevenueCat ->
     * Firestore sync is still lagging or unreachable.
     */
    private val billingRepository: BillingRepository
) : MonetizationRepository {
    private companion object {
        const val TEST_PREMIUM_DURATION_MILLIS = 1 * 60 * 60 * 1000L
    }

    /**
     * Delegates to [EntitlementResolver] so the rule lives in one pure, testable place
     * instead of being restated in each of the three flows below.
     *
     * [revenueCatEntitlementActive] is the store's own verdict and outranks the locally
     * persisted tier, which can only ever be stale.
     */
    private fun isPremiumUser(
        settings: AppSettings,
        profile: UserProfile,
        now: Long,
        revenueCatEntitlementActive: Boolean
    ): Boolean = EntitlementResolver.isPremium(
        appSettingsTier = settings.userTier,
        accountTier = profile.accountTier,
        proExpiryTimestamp = profile.proExpiryTimestamp,
        revenueCatEntitlementActive = revenueCatEntitlementActive,
        now = now
    )

    override val userTier: Flow<UserTier> = combine(
        AppSettingsDataStore.getAppSettingsFlow(context),
        UserProfileDataStore.getUserProfileFlow(context),
        // A third input rather than a synchronous read, so a revenue-cat purchase, renewal
        // or expiration re-evaluates the tier the moment it arrives.
        billingRepository.isPremium
    ) { settings, profile, revenueCatPremium ->
        // Benchmark hook (Phase 0): forces the tier for Macrobenchmark journeys. BuildConfig
        // is a compile-time constant — R8 removes this from release/debug (null there).
        val forcePro = if (BuildConfig.BUILD_TYPE == "benchmark") BenchmarkHooks.forcePro else null
        forcePro?.let { return@combine if (it) UserTier.PREMIUM else UserTier.FREE }
        val now = System.currentTimeMillis()
        if (isPremiumUser(settings, profile, now, revenueCatPremium)) UserTier.PREMIUM else UserTier.FREE
    }

    override val isAdsEnabled: Flow<Boolean> = combine(
        AppSettingsDataStore.getAppSettingsFlow(context),
        UserProfileDataStore.getUserProfileFlow(context),
        MonetizationDataStore.getGlobalAdAccessExpiry(context),
        // The *wider* entitlement on purpose: this is `premium` OR the standalone
        // `ad_free_global` entitlement, so buying ad removal alone stops the ads while a
        // Pro purchase also does. Feature gates below use the narrower `isPremium`.
        billingRepository.isAdFree
    ) { settings, profile, globalAdExpiry, adFreeEntitlement ->
        // Benchmark hook (Phase 0): forces ad state for Macrobenchmark journeys. BuildConfig
        // is a compile-time constant — R8 removes this from release/debug (null there).
        val forcePro = if (BuildConfig.BUILD_TYPE == "benchmark") BenchmarkHooks.forcePro else null
        forcePro?.let { return@combine !it }
        val now = System.currentTimeMillis()
        val isPremium = isPremiumUser(settings, profile, now, adFreeEntitlement)
        val hasActivePass = globalAdExpiry > now
        
        // Ads are enabled if NOT premium AND NOT having an active pass
        !isPremium && !hasActivePass
    }

    override val globalAdAccessExpiry: Flow<Long> = MonetizationDataStore.getGlobalAdAccessExpiry(context)

    /**
     * Deliberately the raw store entitlement with no local input: a ProPass grant makes
     * [userTier] PREMIUM without ever making this true, which is exactly the distinction the
     * membership screen needs to tell "renews in the store" from "expires on this date".
     */
    override val storeEntitlement: Flow<StoreEntitlement?> = billingRepository.storeEntitlement

    /**
     * Derived from [storeEntitlement] rather than duplicating the entitlement lookup, so the
     * two can never disagree about whether a subscription exists.
     */
    override val hasActiveStoreSubscription: Flow<Boolean> =
        billingRepository.storeEntitlement.map { it != null }

    override fun observeAccessStatus(feature: Feature, optionId: String?): Flow<AccessStatus> {
        return combine(
            AppSettingsDataStore.getAppSettingsFlow(context),
            UserProfileDataStore.getUserProfileFlow(context),
            MonetizationDataStore.getGlobalAdAccessExpiry(context),
            configurationRepository.isProGatingEnabled,
            // Feature gates read the *narrower* `premium` entitlement: an ad-free-only
            // purchase must not unlock Pro features.
            billingRepository.isPremium
        ) { settings, profile, globalAdExpiry, proGatingEnabled, revenueCatPremium ->
            // Benchmark hook (Phase 0): forces granted access for the Pro journey.
            val forcePro = if (BuildConfig.BUILD_TYPE == "benchmark") BenchmarkHooks.forcePro else null
            forcePro?.let { if (it) return@combine AccessStatus.Granted }

            // When pro_gating_enabled is false (Remote Config), all features are unlocked
            if (!proGatingEnabled) {
                return@combine AccessStatus.Granted
            }

            val now = System.currentTimeMillis()
            val isPremium = isPremiumUser(settings, profile, now, revenueCatPremium)
            
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

    // becomePremium() was removed with the simulated-purchase path: it granted *permanent*
    // Pro with no expiry, which would silently outrank a real subscription and mask every
    // billing failure during testing. Pro is now granted only by a store entitlement or by
    // a time-limited ProPass, so no path can set a tier that never expires.
}
