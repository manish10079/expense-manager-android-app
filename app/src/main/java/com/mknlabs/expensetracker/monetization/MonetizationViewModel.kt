package com.mknlabs.expensetracker.monetization

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.usecase.GrantTemporaryAccessUseCase
import com.mknlabs.expensetracker.domain.usecase.ObserveAccessStatusUseCase
import com.mknlabs.expensetracker.domain.repository.BillingRepository
import com.mknlabs.expensetracker.domain.repository.ConfigurationRepository
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.domain.repository.ProPassRepository
import com.mknlabs.expensetracker.domain.repository.RedemptionError
import com.mknlabs.expensetracker.domain.repository.RedemptionOutcome
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RedemptionState {
    object Idle : RedemptionState()
    object Loading : RedemptionState()
    data class Success(val days: Int) : RedemptionState()
    data class Error(val error: RedemptionError) : RedemptionState()
}

@HiltViewModel
class MonetizationViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val monetizationRepository: MonetizationRepository,
    /**
     * The store's billing surface. Injected here rather than reached through
     * [monetizationRepository] because restore is an *action* the membership screen owns:
     * routing it through the entitlement reader would have made that interface carry a
     * purchase trigger for one caller.
     */
    private val billingRepository: BillingRepository,
    private val proPassRepository: ProPassRepository,
    private val observeAccessStatusUseCase: ObserveAccessStatusUseCase,
    private val grantTemporaryAccessUseCase: GrantTemporaryAccessUseCase,
    private val configurationRepository: ConfigurationRepository,
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

    /**
     * True when Pro comes from a store purchase rather than a ProPass grant.
     *
     * Separate from [userTier] on purpose: both states are Pro, but they need different
     * calls to action — a subscriber manages a subscription, a ProPass holder has none to
     * manage and should be offered the store's plans instead.
     */
    val hasActiveStoreSubscription: StateFlow<Boolean> = monetizationRepository.hasActiveStoreSubscription
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            // False until the store answers: the safe default is "no subscription", which
            // only ever shows a buy CTA the store will refuse if it is wrong.
            initialValue = false
        )

    /**
     * The store's view of the active `premium` entitlement, or null when there is none.
     *
     * Null is the honest default before the store answers: it makes a caller show wording
     * rather than a date, and a date that arrives a moment later simply replaces it.
     */
    val storeEntitlement: StateFlow<StoreEntitlement?> = monetizationRepository.storeEntitlement
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * The outcome of a restore started from this screen, or [PurchaseState.Idle].
     *
     * Scoped to [PurchaseState.Operation.Restore] on purpose: a purchase is announced by the
     * paywall that ran it, and the paywall owns that state until it has shown the message.
     * Without the filter, a user who bought Pro and immediately backed out to this screen
     * would be told about the same purchase twice.
     */
    val restoreState: StateFlow<PurchaseState> = billingRepository.purchaseState
        .map { state ->
            if (state.operationOrNull == PurchaseState.Operation.Restore) {
                state
            } else {
                PurchaseState.Idle
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PurchaseState.Idle
        )

    /**
     * Length of the pass a rewarded ad grants, in minutes — Remote Config
     * (`ad_pass_duration_minutes`). Gating UI copy must read this instead of stating a fixed
     * duration, so the promise and the grant can never disagree.
     */
    val adPassDurationMinutes: StateFlow<Int> = configurationRepository.adPassDurationMinutes

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
            when (val outcome = proPassRepository.redeemCode(code)) {
                is RedemptionOutcome.Success -> {
                    _redemptionState.value = RedemptionState.Success(outcome.durationDays)
                    // ProPass activated: push local data to Firestore and pull cloud
                    // changes so premium access and transactions are in sync immediately.
                    SyncWorker.startImmediate(appContext)
                }

                is RedemptionOutcome.Failure -> {
                    // Typed on purpose: wording is the UI's business (strings.xml), and the
                    // server's English message must never reach the screen.
                    _redemptionState.value = RedemptionState.Error(outcome.error)
                }
            }
        }
    }

    fun resetRedemptionState() {
        _redemptionState.value = RedemptionState.Idle
    }

    /**
     * Restores the account's previous store purchases, in place.
     *
     * Belongs here rather than behind a trip to the paywall: the user asked to restore, and
     * the purchase screen's Play sheet is not what they asked for. Progress and the outcome
     * are reported through [restoreState].
     */
    fun restorePurchases() {
        if (restoreState.value is PurchaseState.InProgress) return
        billingRepository.restore()
    }

    /** Releases the restore outcome once its message has been shown. */
    fun onRestoreOutcomeShown() {
        billingRepository.acknowledgePurchase()
    }

    /**
     * Re-reads the store's entitlement state.
     *
     * Called when the membership screen opens. That screen has to state where Pro comes
     * from, and RevenueCat only delivers its customer info through its listener, a login or
     * a purchase — so a subscriber who is already signed in can reach the screen with no
     * store snapshot at all, leaving a ProPass grant to describe their access.
     */
    fun refreshStoreEntitlement() {
        billingRepository.refreshEntitlement()
    }

    // The simulated-purchase entry point was removed once real purchases existed. It had no
    // caller left in the UI, and it granted permanent Pro — which would have outranked a
    // real subscription and hidden billing failures during sandbox testing. Use a license
    // tester purchase, or a ProPass code, to grant Pro now.

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
     *
     * [onAccessGranted] fires once access has actually been granted — after the reward is
     * earned, or after the grace period when no ad could be shown. Callers that gate a UI
     * action (e.g. the recurring-rule switch) must apply that action from here instead of
     * doing it up front: granting eagerly would unlock the feature without ever showing an ad.
     */
    fun onAdWatched(
        activity: Activity,
        feature: Feature,
        optionId: String? = null,
        onAccessGranted: () -> Unit = {}
    ) {
        val placement = if (feature == Feature.AD_FREE_GLOBAL) {
            RewardedPlacement.AD_FREE_ACCESS
        } else {
            RewardedPlacement.FEATURE_UNLOCK
        }

        viewModelScope.launch {
            if (adsCoordinator.isRewardedAdReady(placement)) {
                showAdAndGrantAccess(activity, placement, feature, optionId, onAccessGranted)
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
                    showAdAndGrantAccess(activity, placement, feature, optionId, onAccessGranted)
                } else {
                    // Grace period over: Grant for free
                    grantAccess(feature, optionId)
                    onAccessGranted()
                }
            }
        }
    }

    private fun showAdAndGrantAccess(
        activity: Activity,
        placement: RewardedPlacement,
        feature: Feature,
        optionId: String?,
        onAccessGranted: () -> Unit
    ) {
        adsCoordinator.showRewardedAd(activity, placement) {
            grantAccess(feature, optionId)
            onAccessGranted()
        }
    }

    private fun grantAccess(feature: Feature, optionId: String?) {
        viewModelScope.launch {
            // Pass length is remote-configurable (`ad_pass_duration_minutes`) so it can be
            // tuned from the console without shipping a build; defaults to 60 minutes.
            val durationMinutes = configurationRepository.adPassDurationMinutes.value
            grantTemporaryAccessUseCase.execute(
                feature = feature,
                optionId = optionId,
                durationMillis = durationMinutes * 60_000L
            )
        }
    }

    /**
     * Specifically handles the "Watch Ad to Remove Ads" flow from Settings.
     */
    fun onWatchAdFreeClicked(activity: Activity) {
        onAdWatched(activity, Feature.AD_FREE_GLOBAL)
    }

    /**
     * Re-opens the UMP privacy options form so the user can update their consent
     * choices (GDPR / US state privacy regulations). Delegates to the AdsCoordinator.
     */
    fun showPrivacyOptionsForm(activity: Activity) {
        adsCoordinator.showPrivacyOptionsForm(activity)
    }
}
