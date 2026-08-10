package com.mknlabs.expensetracker.ui.viewmodels

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.usecase.BecomePremiumUseCase
import com.mknlabs.expensetracker.domain.usecase.GrantTemporaryAccessUseCase
import com.mknlabs.expensetracker.domain.usecase.ObserveAccessStatusUseCase
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.AdsCoordinator
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.RewardedPlacement
import com.mknlabs.expensetracker.monetization.InterstitialPlacement
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.domain.repository.ProPassRepository
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.workers.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RedemptionState {
    object Idle : RedemptionState()
    object Loading : RedemptionState()
    data class Success(val days: Int) : RedemptionState()
    data class Error(val message: String) : RedemptionState()
}

@HiltViewModel
class MonetizationViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val monetizationRepository: MonetizationRepository,
    private val proPassRepository: ProPassRepository,
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

    /**
     * Unified reactive stream for the effective UserTier.
     */
    val userTier: StateFlow<UserTier> = monetizationRepository.userTier
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserTier.FREE
        )

    private val _isAdLoading = MutableStateFlow(false)
    val isAdLoading: StateFlow<Boolean> = _isAdLoading.asStateFlow()

    private val _redemptionState = MutableStateFlow<RedemptionState>(RedemptionState.Idle)
    val redemptionState: StateFlow<RedemptionState> = _redemptionState.asStateFlow()

    // Cache flows to prevent recreation and flickering on recomposition
    private val accessStatusCache = mutableMapOf<String, StateFlow<AccessStatus>>()

    /**
     * Redeems a ProPass code.
     */
    fun redeemProPass(code: String) {
        viewModelScope.launch {
            _redemptionState.value = RedemptionState.Loading
            proPassRepository.redeemCode(code)
                .onSuccess { days ->
                    _redemptionState.value = RedemptionState.Success(days)
                    // ProPass activated: push local data to Firestore and pull cloud
                    // changes so premium access and transactions are in sync immediately.
                    SyncWorker.startImmediate(appContext)
                }
                .onFailure { error ->
                    _redemptionState.value = RedemptionState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun resetRedemptionState() {
        _redemptionState.value = RedemptionState.Idle
    }

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
