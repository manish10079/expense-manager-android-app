package com.mkn0079.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkn0079.expensetracker.domain.usecase.GrantTemporaryAccessUseCase
import com.mkn0079.expensetracker.domain.usecase.ObserveAccessStatusUseCase
import com.mkn0079.expensetracker.monetization.AccessStatus
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
    private val grantTemporaryAccessUseCase: GrantTemporaryAccessUseCase
) : ViewModel() {

    // Cache flows to prevent recreation and flickering on recomposition
    private val accessStatusCache = mutableMapOf<String, StateFlow<AccessStatus>>()

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
     * Simulates watching an ad and grants temporary access.
     */
    fun onAdWatched(feature: Feature, optionId: String? = null) {
        viewModelScope.launch {
            grantTemporaryAccessUseCase.execute(
                feature = feature,
                optionId = optionId,
                durationMillis = 2 * 60 * 60 * 1000
            )
        }
    }
}
