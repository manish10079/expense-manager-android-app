package com.mkn0079.expensetracker.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    // Using Google's test Rewarded Ad ID
    private val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    init {
        loadRewardedAd()
    }

    fun loadRewardedAd(onAdLoaded: (() -> Unit)? = null) {
        if (rewardedAd != null || isLoading) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("AdMobManager", adError.toString())
                rewardedAd = null
                isLoading = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("AdMobManager", "Ad was loaded.")
                rewardedAd = ad
                isLoading = false
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
            Log.d("AdMobManager", "The rewarded ad wasn't ready yet.")
            loadRewardedAd()
        }
    }
    
    fun isAdReady(): Boolean = rewardedAd != null
}
