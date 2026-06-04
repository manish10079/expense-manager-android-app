package com.mknlabs.expensetracker.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.AdLoader
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Defines all locations where ads can be displayed.
 * Maps to unique AdMob unit IDs for granular analytics.
 */
enum class AdPlacement {
    HOME_DASHBOARD,
    TRANSACTIONS_LIST,
    ANALYTICS_INSIGHTS,
    BUDGET_CALENDAR,
    SETTINGS_GENERAL
}

/**
 * Defines unique identifiers for Rewarded ads.
 */
enum class RewardedPlacement {
    FEATURE_UNLOCK,
    AD_FREE_ACCESS
}

/**
 * Defines unique identifiers for Interstitial ads.
 */
enum class InterstitialPlacement {
    DATA_ACTION
}

/**
 * Centrally manages ad loading, caching, display, and privacy consent (UMP).
 * Aligned with Phase 1 of the Monetization Roadmap.
 */
@Singleton
class AdsCoordinator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val rewardedAds = mutableMapOf<RewardedPlacement, RewardedAd?>()
    private val isRewardedAdLoading = mutableMapOf<RewardedPlacement, Boolean>()

    private val interstitialAds = mutableMapOf<InterstitialPlacement, InterstitialAd?>()
    private val isInterstitialAdLoading = mutableMapOf<InterstitialPlacement, Boolean>()
    
    private val lastInterstitialTime = mutableMapOf<InterstitialPlacement, Long>()
    private val INTERSTITIAL_COOLDOWN_MILLIS = 15 * 60 * 1000L // 15 minutes

    private val nativeAds = mutableMapOf<AdPlacement, NativeAd?>()
    private val isNativeAdLoading = mutableMapOf<AdPlacement, Boolean>()

    private val isMobileAdsSdkInitialized = AtomicBoolean(false)
    private lateinit var consentInformation: ConsentInformation

    // Google Test Ad IDs
    private val REWARDED_TEST_ID = "ca-app-pub-3940256099942544/5224354917"
    private val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
    private val NATIVE_TEST_ID = "ca-app-pub-3940256099942544/2247696110"

    // Real Native Ad Unit IDs
    private val NATIVE_HOME_ID = "ca-app-pub-3052232912913226/1140149865"
    private val NATIVE_TRANSACTIONS_ID = "ca-app-pub-3052232912913226/4486018419"
    private val NATIVE_BUDGET_CALENDAR_ID = "ca-app-pub-3052232912913226/6920610063"
    private val NATIVE_SETTINGS_ID = "ca-app-pub-3052232912913226/2261659841"
    private val NATIVE_ANALYTICS_ID = "ca-app-pub-3052232912913226/9786446554"

    // Real Interstitial Ad Unit IDs
    private val INTERSTITIAL_DATA_ACTION_ID = "ca-app-pub-3052232912913226/9797369089"

    // Real Rewarded Ad Unit IDs
    private val REWARDED_AD_FREE_ID = "ca-app-pub-3052232912913226/5216950076"

    /**
     * Maps placements to their respective AdMob Unit IDs.
     */
    private fun getNativeAdUnitId(placement: AdPlacement): String {
        // ALWAYS use test IDs in Debug builds to avoid account suspension
        if (com.mknlabs.expensetracker.BuildConfig.DEBUG) return NATIVE_TEST_ID

        return when (placement) {
            AdPlacement.HOME_DASHBOARD -> NATIVE_HOME_ID
            AdPlacement.TRANSACTIONS_LIST -> NATIVE_TRANSACTIONS_ID
            AdPlacement.ANALYTICS_INSIGHTS -> NATIVE_ANALYTICS_ID
            AdPlacement.BUDGET_CALENDAR -> NATIVE_BUDGET_CALENDAR_ID
            AdPlacement.SETTINGS_GENERAL -> NATIVE_SETTINGS_ID
        }
    }

    private fun getRewardedAdUnitId(placement: RewardedPlacement): String {
        if (com.mknlabs.expensetracker.BuildConfig.DEBUG) return REWARDED_TEST_ID

        return when (placement) {
            RewardedPlacement.FEATURE_UNLOCK -> REWARDED_AD_FREE_ID
            RewardedPlacement.AD_FREE_ACCESS -> REWARDED_AD_FREE_ID
        }
    }

    private fun getInterstitialAdUnitId(placement: InterstitialPlacement): String {
        if (com.mknlabs.expensetracker.BuildConfig.DEBUG) return INTERSTITIAL_TEST_ID

        return when (placement) {
            InterstitialPlacement.DATA_ACTION -> INTERSTITIAL_DATA_ACTION_ID
        }
    }

    /**
     * Initializes the privacy consent flow (UMP).
     * Should be called from the Activity.
     */
    fun initPrivacyFlow(activity: Activity, onComplete: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w("AdsCoordinator", "${formError.errorCode}: ${formError.message}")
                    }

                    if (consentInformation.canRequestAds()) {
                        initializeMobileAdsSdk(onComplete)
                    } else {
                        onComplete()
                    }
                }
            },
            { requestConsentError ->
                Log.w("AdsCoordinator", "${requestConsentError.errorCode}: ${requestConsentError.message}")
                if (consentInformation.canRequestAds()) {
                    initializeMobileAdsSdk(onComplete)
                } else {
                    onComplete()
                }
            }
        )

        // Check if SDK can be initialized immediately
        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk(onComplete)
        }
    }

    private fun initializeMobileAdsSdk(onComplete: () -> Unit) {
        if (isMobileAdsSdkInitialized.getAndSet(true)) {
            onComplete()
            return
        }

        MobileAds.initialize(context) {
            // Preload common ads
            loadRewardedAd(RewardedPlacement.FEATURE_UNLOCK)
            loadNativeAd(AdPlacement.HOME_DASHBOARD)
            onComplete()
        }
    }

    /**
     * Loads an Interstitial Ad for a specific placement.
     */
    fun loadInterstitialAd(placement: InterstitialPlacement, onAdLoaded: (() -> Unit)? = null) {
        if (interstitialAds[placement] != null || isInterstitialAdLoading[placement] == true) return
        if (!isMobileAdsSdkInitialized.get()) return

        isInterstitialAdLoading[placement] = true
        val adUnitId = getInterstitialAdUnitId(placement)
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("AdsCoordinator", "Interstitial ad ($placement) failed to load: ${adError.message}")
                interstitialAds[placement] = null
                isInterstitialAdLoading[placement] = false
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d("AdsCoordinator", "Interstitial ad ($placement) was loaded.")
                interstitialAds[placement] = ad
                isInterstitialAdLoading[placement] = false
                onAdLoaded?.invoke()
            }
        })
    }

    /**
     * Checks if an interstitial ad can be shown based on frequency capping (15 min cooldown).
     */
    fun canShowInterstitial(placement: InterstitialPlacement): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastTime = lastInterstitialTime[placement] ?: 0
        val timeSinceLastAd = currentTime - lastTime
        val isCooldownOver = timeSinceLastAd >= INTERSTITIAL_COOLDOWN_MILLIS
        
        return interstitialAds[placement] != null && isCooldownOver
    }

    /**
     * Shows the Interstitial Ad if ready and cooldown is over.
     */
    fun showInterstitial(activity: Activity, placement: InterstitialPlacement, onAdDismissed: () -> Unit = {}) {
        if (canShowInterstitial(placement)) {
            interstitialAds[placement]?.let { ad ->
                ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("AdsCoordinator", "Interstitial ad ($placement) was dismissed.")
                        interstitialAds[placement] = null
                        lastInterstitialTime[placement] = System.currentTimeMillis()
                        loadInterstitialAd(placement) // Preload next
                        onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        Log.d("AdsCoordinator", "Interstitial ad ($placement) failed to show: ${adError.message}")
                        interstitialAds[placement] = null
                        onAdDismissed()
                    }
                }
                ad.show(activity)
            }
        } else {
            Log.d("AdsCoordinator", "Interstitial ad ($placement) not ready or cooldown in progress.")
            if (interstitialAds[placement] == null) loadInterstitialAd(placement)
            onAdDismissed()
        }
    }

    fun loadRewardedAd(placement: RewardedPlacement, onAdLoaded: (() -> Unit)? = null) {
        if (rewardedAds[placement] != null || isRewardedAdLoading[placement] == true) return
        if (!isMobileAdsSdkInitialized.get()) return

        isRewardedAdLoading[placement] = true
        val adUnitId = getRewardedAdUnitId(placement)
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("AdsCoordinator", "Rewarded ad ($placement) failed to load: ${adError.message}")
                rewardedAds[placement] = null
                isRewardedAdLoading[placement] = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("AdsCoordinator", "Rewarded ad ($placement) was loaded.")
                rewardedAds[placement] = ad
                isRewardedAdLoading[placement] = false
                onAdLoaded?.invoke()
            }
        })
    }

    fun showRewardedAd(activity: Activity, placement: RewardedPlacement, onUserEarnedReward: () -> Unit) {
        rewardedAds[placement]?.let { ad ->
            ad.show(activity) { rewardItem ->
                // User earned the reward!
                onUserEarnedReward()
                rewardedAds[placement] = null
                loadRewardedAd(placement) // Preload the next one
            }
        } ?: run {
            Log.d("AdsCoordinator", "The rewarded ad ($placement) wasn't ready yet.")
            loadRewardedAd(placement)
        }
    }
    
    fun isRewardedAdReady(placement: RewardedPlacement): Boolean = rewardedAds[placement] != null

    /**
     * Loads a Native Ad for a specific placement.
     */
    fun loadNativeAd(placement: AdPlacement, onAdLoaded: ((NativeAd) -> Unit)? = null) {
        if (isNativeAdLoading[placement] == true) return
        if (!isMobileAdsSdkInitialized.get()) return

        isNativeAdLoading[placement] = true
        val adUnitId = getNativeAdUnitId(placement)
        
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad : NativeAd ->
                nativeAds[placement]?.destroy()
                nativeAds[placement] = ad
                isNativeAdLoading[placement] = false
                onAdLoaded?.invoke(ad)
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("AdsCoordinator", "Native ad ($placement) failed to load: ${adError.message}")
                    isNativeAdLoading[placement] = false
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun getNativeAd(placement: AdPlacement): NativeAd? = nativeAds[placement]
}
