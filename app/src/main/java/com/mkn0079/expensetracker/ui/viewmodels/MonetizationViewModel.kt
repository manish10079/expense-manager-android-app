package com.mkn0079.expensetracker.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.domain.usecase.BecomePremiumUseCase
import com.mkn0079.expensetracker.domain.usecase.GrantTemporaryAccessUseCase
import com.mkn0079.expensetracker.domain.usecase.ObserveAccessStatusUseCase
import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.monetization.AdsCoordinator
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.monetization.RewardedPlacement
import com.mkn0079.expensetracker.monetization.InterstitialPlacement
import com.mkn0079.expensetracker.domain.repository.MonetizationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonetizationViewModel @Inject constructor(
    private val monetizationRepository: MonetizationRepository,
    private val observeAccessStatusUseCase: ObserveAccessStatusUseCase,
    private val grantTemporaryAccessUseCase: GrantTemporaryAccessUseCase,
    private val becomePremiumUseCase: BecomePremiumUseCase,
    private val adsCoordinator: AdsCoordinator
) : ViewModel() {

    /**
     * Reactive stream indicating if ads should be shown.
     * False for Premium users.
     */
    val isAdsEnabled: StateFlow<Boolean> = monetizationRepository.isAdsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: StateFlow<Boolean> = _isAdLoading.asStateFlow()

    // Cache flows to prevent recreation and flickering on recomposition
    private val accessStatusCache = mutableMapOf<String, StateFlow<AccessStatus>>()

    /**
     * Simulates a purchase and grants full access for a limited test window.
     */
    fun onPurchaseSimulated() {
        viewModelScope.launch {
            becomePremiumUseCase.execute()
        }
    }

    /**
     * Returns a reactive stream of the access status for a feature.
     * Caches the flow per feature/option to ensure stability and prevent flickering.
     */
    fun getAccessStatus(feature: Feature, optionId: String? = null): StateFlow<AccessStatus> {
        val key = if (optionId != null) "${feature.id}_$optionId" else feature.id
        
        return accessStatusCache.getOrPut(key) {
            observeAccessStatusUseCase(feature, optionId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = AccessStatus.Granted // Default to Granted to avoid "Lock Flicker" on load
                )
        }
    }

    /**
     * Shows an interstitial ad if ready and cooldown is over.
     */
    fun showInterstitial(activity: Activity, placement: InterstitialPlacement, onAdDismissed: () -> Unit = {}) {
        adsCoordinator.showInterstitial(activity, placement, onAdDismissed)
    }

    /**
     * Shows a rewarded ad and grants temporary access upon completion.
     * Includes a 5-second grace period: if ad isn't ready in 5s, access is granted for free.
     */
    fun onAdWatched(activity: Activity, feature: Feature, optionId: String? = null) {
        val placement = if (feature == Feature.AD_FREE_GLOBAL) {
            RewardedPlacement.AD_FREE_ACCESS
        } else {
            RewardedPlacement.FEATURE_UNLOCK
        }

        viewModelScope.launch {
            if (adsCoordinator.isRewardedAdReady(placement)) {
                showAdAndGrantAccess(activity, placement, feature, optionId)
            } else {
                _isAdLoading.value = true
                
                // Try to load and wait for up to 5 seconds
                var adLoaded = false
                val loadJob = launch {
                    adsCoordinator.loadRewardedAd(placement) {
                        adLoaded = true
                    }
                    
                    // Poll for readiness every 100ms
                    for (i in 1..50) { 
                        if (adLoaded || adsCoordinator.isRewardedAdReady(placement)) break
                        kotlinx.coroutines.delay(100)
                    }
                }
                
                loadJob.join()
                _isAdLoading.value = false

                if (adLoaded || adsCoordinator.isRewardedAdReady(placement)) {
                    showAdAndGrantAccess(activity, placement, feature, optionId)
                } else {
                    // Grace period over: Grant for free
                    grantAccess(feature, optionId)
                }
            }
        }
    }

    private fun showAdAndGrantAccess(activity: Activity, placement: RewardedPlacement, feature: Feature, optionId: String?) {
        adsCoordinator.showRewardedAd(activity, placement) {
            grantAccess(feature, optionId)
        }
    }

    private fun grantAccess(feature: Feature, optionId: String?) {
        viewModelScope.launch {
            grantTemporaryAccessUseCase.execute(
                feature = feature,
                optionId = optionId,
                durationMillis = 1 * 60 * 60 * 1000
            )
        }
    }

    /**
     * Specifically handles the "Watch Ad to Remove Ads" flow from Settings.
     */
    fun onWatchAdFreeClicked(activity: Activity) {
        onAdWatched(activity, Feature.AD_FREE_GLOBAL)
    }
}
