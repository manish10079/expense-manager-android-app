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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonetizationViewModel @Inject constructor(
    private val observeAccessStatusUseCase: ObserveAccessStatusUseCase,
    private val grantTemporaryAccessUseCase: GrantTemporaryAccessUseCase,
    private val becomePremiumUseCase: BecomePremiumUseCase,
    private val adsCoordinator: AdsCoordinator
) : ViewModel() {

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
     * Shows a rewarded ad and grants temporary access upon completion.
     */
    fun onAdWatched(activity: Activity, feature: Feature, optionId: String? = null) {
        if (adsCoordinator.isRewardedAdReady()) {
            adsCoordinator.showRewardedAd(activity) {
                viewModelScope.launch {
                    grantTemporaryAccessUseCase.execute(
                        feature = feature,
                        optionId = optionId,
                        durationMillis = 1 * 60 * 60 * 1000
                    )
                }
            }
        } else {
            // If ad is not ready, load it and grant access for now so user isn't blocked during testing
            adsCoordinator.loadRewardedAd()
            viewModelScope.launch {
                grantTemporaryAccessUseCase.execute(
                    feature = feature,
                    optionId = optionId,
                    durationMillis = 1 * 60 * 60 * 1000
                )
            }
        }
    }
}
