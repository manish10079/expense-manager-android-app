package com.mknlabs.expensetracker.feature.paywall.ui

import android.app.Activity
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.repository.BillingRepository
import com.mknlabs.expensetracker.monetization.PurchaseState
import com.mknlabs.expensetracker.monetization.SubscriptionOffer
import com.mknlabs.expensetracker.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Pins the paywall's behaviour against a hand-written [BillingRepository].
 *
 * The fake records intent rather than modelling RevenueCat, so these tests assert what the
 * ViewModel *asks for* — the plan it purchases, the guards it applies — leaving the real
 * SDK behaviour to `BillingManager`. The fake publishes `InProgress` synchronously exactly
 * as the real manager does; without that, the busy guards would be untestable and would
 * silently stop working.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val billing = FakeBillingRepository()

    /**
     * `uiState` is a `WhileSubscribed` flow, so it stays at its initial value until someone
     * collects it. Without this the tests would assert against the initial state forever.
     */
    private fun TestScope.collectState(viewModel: PaywallViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    private fun viewModel(): PaywallViewModel = PaywallViewModel(billing)

    private val offer = SubscriptionOffer(
        id = "six_months",
        planLabelRes = R.string.paywall_plan_six_months,
        periodLabelRes = R.string.paywall_period_months,
        priceText = "₹749.00",
    )

    // --- Offerings ---------------------------------------------------------------------

    @Test
    fun `plans start in the loading state`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        assertTrue(viewModel.uiState.value.isLoadingOffers)
        assertTrue(viewModel.uiState.value.offers.isEmpty())
    }

    @Test
    fun `plans surface once the fetch completes`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.offers.value = listOf(offer)
        billing.isOffersLoaded.value = true

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingOffers)
        assertEquals(listOf(offer), state.offers)
        assertFalse(state.isPlansUnavailable)
    }

    @Test
    fun `a finished fetch with no plans is reported as unavailable, not as loading`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.isOffersLoaded.value = true

        val state = viewModel.uiState.value
        assertFalse(state.isLoadingOffers)
        // Without this distinction the paywall would spin forever on a dead network
        // instead of offering a retry.
        assertTrue(state.isPlansUnavailable)
    }

    @Test
    fun `retry re-fetches offers`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        viewModel.onRetryClick()

        assertEquals(1, billing.refreshCount)
    }

    // --- Purchasing --------------------------------------------------------------------

    @Test
    fun `subscribing purchases the chosen plan`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)
        billing.offers.value = listOf(offer)
        billing.isOffersLoaded.value = true

        viewModel.onSubscribeClick(activity, "six_months")

        assertEquals(listOf("six_months"), billing.purchasedOfferIds)
    }

    @Test
    fun `a second tap while a purchase is in flight is ignored`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        viewModel.onSubscribeClick(activity, "monthly")
        viewModel.onSubscribeClick(activity, "monthly")

        // The user can double-tap before the Play sheet appears; the second attempt must
        // not create a second transaction.
        assertEquals(listOf("monthly"), billing.purchasedOfferIds)
    }

    @Test
    fun `both actions are disabled while an attempt is in flight`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        viewModel.onSubscribeClick(activity, "monthly")

        assertTrue(viewModel.uiState.value.isBusy)
    }

    @Test
    fun `restoring while a purchase is in flight is ignored`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        viewModel.onSubscribeClick(activity, "monthly")
        viewModel.onRestoreClick()

        assertEquals(0, billing.restoreCount)
        assertTrue(viewModel.uiState.value.isBusy)
    }

    @Test
    fun `restoring when idle runs a restore`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        viewModel.onRestoreClick()

        assertEquals(1, billing.restoreCount)
    }

    // --- Outcomes ----------------------------------------------------------------------

    @Test
    fun `a completed purchase surfaces its message`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.purchaseState.value = PurchaseState.Completed(PurchaseState.Operation.Purchase)

        assertEquals(R.string.msg_paywall_purchase_success, viewModel.uiState.value.messageRes)
    }

    @Test
    fun `acknowledging an outcome clears the message`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)
        billing.purchaseState.value = PurchaseState.Completed(PurchaseState.Operation.Purchase)

        viewModel.onOutcomeShown()

        // Otherwise the same success would be re-announced on the next recomposition or
        // the next visit to the paywall.
        assertEquals(1, billing.acknowledgeCount)
        assertNull(viewModel.uiState.value.messageRes)
    }

    @Test
    fun `a cancelled attempt shows no message`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.purchaseState.value = PurchaseState.Cancelled(PurchaseState.Operation.Purchase)

        assertNull(viewModel.uiState.value.messageRes)
        assertFalse(viewModel.uiState.value.isBusy)
    }

    @Test
    fun `a restore that found nothing is explained rather than reported as a failure`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.purchaseState.value = PurchaseState.Completed(PurchaseState.Operation.Restore)

        assertEquals(
            R.string.msg_paywall_restore_nothing_found,
            viewModel.uiState.value.messageRes
        )
    }

    @Test
    fun `an active entitlement is exposed so the paywall can say the user is already Pro`() =
        runTest {
            val viewModel = viewModel()
            collectState(viewModel)

            billing.isPremium.value = true

            assertTrue(viewModel.uiState.value.isPremium)
        }

    // --- Closing the paywall once it has nothing left to sell ----------------------------

    @Test
    fun `a completed purchase settles the paywall`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.purchaseState.value = PurchaseState.Completed(PurchaseState.Operation.Purchase)

        // Settled on the transaction alone: the entitlement lags the Play sheet, and with a
        // store entitlement that does not match the id the billing layer reads it never
        // arrives at all. Either way the user has paid, so the paywall must not wait for it.
        assertFalse(viewModel.uiState.value.isPremium)
        assertTrue(viewModel.uiState.value.isPurchaseSettled)
    }

    @Test
    fun `a subscriber who opens the paywall is left in it`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.isPremium.value = true

        // No attempt has been made, so nothing settles: a Pro user who opened the paywall to
        // manage their subscription must not be thrown out of it.
        assertFalse(viewModel.uiState.value.isPurchaseSettled)
    }

    @Test
    fun `a restore that found nothing leaves the paywall up`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.purchaseState.value = PurchaseState.Completed(PurchaseState.Operation.Restore)

        // "Nothing to restore" is a completed restore, and the user still needs somewhere to
        // buy.
        assertFalse(viewModel.uiState.value.isPurchaseSettled)
    }

    @Test
    fun `a restore that found a subscription settles the paywall`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.isPremium.value = true
        billing.purchaseState.value = PurchaseState.Completed(PurchaseState.Operation.Restore)

        assertTrue(viewModel.uiState.value.isPurchaseSettled)
    }

    @Test
    fun `an attempt that did not complete leaves the paywall up`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.purchaseState.value = PurchaseState.Cancelled(PurchaseState.Operation.Purchase)
        assertFalse(viewModel.uiState.value.isPurchaseSettled)

        billing.purchaseState.value =
            PurchaseState.PaymentPending(PurchaseState.Operation.Purchase)
        assertFalse(viewModel.uiState.value.isPurchaseSettled)

        billing.purchaseState.value = PurchaseState.Failed(
            PurchaseState.Operation.Purchase,
            PurchaseState.FailureReason.StoreProblem,
        )
        assertFalse(viewModel.uiState.value.isPurchaseSettled)
    }

    // --- Managing an existing subscription ----------------------------------------------

    @Test
    fun `a subscriber with a store management page can manage their subscription`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.isPremium.value = true
        billing.managementUrl.value = "https://play.google.com/store/account/subscriptions"

        val state = viewModel.uiState.value
        assertTrue(state.canManageSubscription)
        assertEquals(
            "https://play.google.com/store/account/subscriptions",
            state.managementUrl
        )
    }

    @Test
    fun `a non-subscriber is not offered the management link`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        // The store can return a management URL before entitlement state settles; offering
        // the link then would open a page with nothing on it.
        billing.managementUrl.value = "https://play.google.com/store/account/subscriptions"

        assertFalse(viewModel.uiState.value.canManageSubscription)
    }

    @Test
    fun `a subscriber with no store management page is not offered a dead link`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.isPremium.value = true
        billing.managementUrl.value = null

        assertFalse(viewModel.uiState.value.canManageSubscription)
    }

    @Test
    fun `a blank management url is treated as no management page`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.isPremium.value = true
        billing.managementUrl.value = ""

        assertFalse(viewModel.uiState.value.canManageSubscription)
    }

    @Test
    fun `billing not configured is reported with its own copy`() = runTest {
        val viewModel = viewModel()
        collectState(viewModel)

        billing.purchaseState.value = PurchaseState.Failed(
            PurchaseState.Operation.Purchase,
            PurchaseState.FailureReason.BillingNotConfigured,
        )

        assertEquals(
            R.string.paywall_error_billing_not_configured,
            viewModel.uiState.value.messageRes
        )
    }

    /**
     * A `Build.FINGERPRINT` stub is not needed: the ViewModel only ever passes this
     * reference through to RevenueCat, and the fake never touches it.
     */
    private val activity: Activity = Activity()
}

/**
 * Records what the paywall asked for, and mirrors the real manager's one observable
 * behaviour: publishing `InProgress` before the attempt starts, so the busy guards are
 * exercised rather than trivially passing.
 */
private class FakeBillingRepository : BillingRepository {

    val purchasedOfferIds = mutableListOf<String>()
    var restoreCount = 0
    var refreshCount = 0
    var refreshEntitlementCount = 0
    var acknowledgeCount = 0

    override val offers = MutableStateFlow<List<SubscriptionOffer>>(emptyList())
    override val isOffersLoaded = MutableStateFlow(false)
    override val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    override val isPremium = MutableStateFlow(false)
    override val isAdFree = MutableStateFlow(false)
    override val managementUrl = MutableStateFlow<String?>(null)
    override val storeEntitlement =
        MutableStateFlow<com.mknlabs.expensetracker.monetization.StoreEntitlement?>(null)

    override fun purchase(activity: Activity, offerId: String) {
        purchasedOfferIds += offerId
        purchaseState.value = PurchaseState.InProgress(PurchaseState.Operation.Purchase)
    }

    override fun restore() {
        restoreCount++
        purchaseState.value = PurchaseState.InProgress(PurchaseState.Operation.Restore)
    }

    override fun refreshOffers() {
        refreshCount++
    }

    override fun refreshEntitlement() {
        refreshEntitlementCount++
    }

    override fun acknowledgePurchase() {
        acknowledgeCount++
        purchaseState.value = PurchaseState.Idle
    }
}
