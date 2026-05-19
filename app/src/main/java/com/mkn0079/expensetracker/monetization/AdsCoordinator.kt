package com.mkn0079.expensetracker.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centrally manages ad loading, caching, display, and privacy consent (UMP).
 * Aligned with Phase 1 of the Monetization Roadmap.
 */
@Singleton
class AdsCoordinator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rewardedAd: RewardedAd? = null
    private var isRewardedAdLoading = false
    private val isMobileAdsSdkInitialized = AtomicBoolean(false)
    private lateinit var consentInformation: ConsentInformation

    // Using Google's test Rewarded Ad ID
    private val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

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
            loadRewardedAd()
            onComplete()
        }
    }

    fun loadRewardedAd(onAdLoaded: (() -> Unit)? = null) {
        if (rewardedAd != null || isRewardedAdLoading) return
        if (!isMobileAdsSdkInitialized.get()) return

        isRewardedAdLoading = true
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("AdsCoordinator", "Rewarded ad failed to load: ${adError.message}")
                rewardedAd = null
                isRewardedAdLoading = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("AdsCoordinator", "Rewarded ad was loaded.")
                rewardedAd = ad
                isRewardedAdLoading = false
                onAdLoaded?.invoke()
            }
        })
    }

    fun showRewardedAd(activity: Activity, onUserEarnedReward: () -> Unit) {
        rewardedAd?.let { ad ->
            ad.show(activity) { rewardItem ->
                // User earned the reward!
                onUserEarnedReward()
                rewardedAd = null
                loadRewardedAd() // Preload the next one
            }
        } ?: run {
            Log.d("AdsCoordinator", "The rewarded ad wasn't ready yet.")
            loadRewardedAd()
        }
    }
    
    fun isRewardedAdReady(): Boolean = rewardedAd != null
}
