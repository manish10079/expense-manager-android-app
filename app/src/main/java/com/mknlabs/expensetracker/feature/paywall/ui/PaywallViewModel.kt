package com.mknlabs.expensetracker.feature.paywall.ui

import android.app.Activity
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.BillingRepository
import com.mknlabs.expensetracker.monetization.PurchaseState
import com.mknlabs.expensetracker.monetization.SubscriptionOffer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Everything the paywall renders.
 *
 * [messageRes] rather than a message: per the project's i18n rule a ViewModel never
 * resolves user-facing text, and the mapping from a purchase outcome to its copy lives in
 * `PaywallCopy.kt` as a pure function the tests can assert.
 */
data class PaywallUiState(
    val offers: List<SubscriptionOffer> = emptyList(),
    val isLoadingOffers: Boolean = true,
    val purchaseState: PurchaseState = PurchaseState.Idle,
    val isPremium: Boolean = false,
    val managementUrl: String? = null,
    @StringRes val messageRes: Int? = null,
) {
    /** An attempt is running, so both actions are disabled until it settles. */
    val isBusy: Boolean get() = purchaseState is PurchaseState.InProgress

    /**
     * The store has an active subscription *and* gave us a page to manage it on.
     *
     * Both halves are required: offering the link to a non-subscriber would lead nowhere,
     * and opening a null URL is not possible.
     */
    val canManageSubscription: Boolean
        get() = isPremium && !managementUrl.isNullOrBlank()

    /** Offerings finished loading and produced nothing selectable. */
    val isPlansUnavailable: Boolean get() = !isLoadingOffers && offers.isEmpty()
}

/**
 * Drives the paywall from the billing contract alone.
 *
 * Depends on [BillingRepository], not the concrete manager, so it can be tested with a
 * hand-written fake — the concrete RevenueCat manager configures the SDK in its
 * constructor and cannot exist in a JVM test.
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
) : ViewModel() {

    val uiState: StateFlow<PaywallUiState> = combine(
        billingRepository.offers,
        billingRepository.isOffersLoaded,
        billingRepository.purchaseState,
        billingRepository.isPremium,
        billingRepository.managementUrl,
    ) { offers, isOffersLoaded, purchaseState, isPremium, managementUrl ->
        PaywallUiState(
            offers = offers,
            isLoadingOffers = !isOffersLoaded,
            purchaseState = purchaseState,
            isPremium = isPremium,
            managementUrl = managementUrl,
            messageRes = purchaseMessageRes(purchaseState, isPremium),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PaywallUiState(),
    )

    /** Starts the purchase of [offerId]. Ignored while another attempt is in flight. */
    fun onSubscribeClick(activity: Activity, offerId: String) {
        if (uiState.value.isBusy) return
        billingRepository.purchase(activity, offerId)
    }

    /** Restores previous purchases. Ignored while another attempt is in flight. */
    fun onRestoreClick() {
        if (uiState.value.isBusy) return
        billingRepository.restore()
    }

    /** Retries the offerings fetch after a failure or an unreachable store. */
    fun onRetryClick() {
        billingRepository.refreshOffers()
    }

    /**
     * Releases the last outcome once its message has been shown.
     *
     * Without this the same success or error would be re-announced on the next
     * recomposition, because the state stays current until something clears it.
     */
    fun onOutcomeShown() {
        billingRepository.acknowledgePurchase()
    }
}
