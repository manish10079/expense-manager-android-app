package com.mknlabs.expensetracker.feature.paywall.ui

import android.app.Activity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.domain.repository.BillingRepository
import com.mknlabs.expensetracker.monetization.PurchaseState
import com.mknlabs.expensetracker.monetization.StoreEntitlement
import com.mknlabs.expensetracker.monetization.SubscriptionOffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Renders the paywall *route* — the composable that owns the ViewModel — to pin the one
 * behaviour the pure render test cannot see: an attempt that settles takes the screen with it.
 *
 * The reported bug was precisely the opposite, and it is not visible in `PaywallContent` at
 * all: the success snackbar appeared over a paywall that stayed up, leaving a user who had just
 * paid on a purchase screen. `PaywallUiState.isPurchaseSettled` decides *whether* to close, and
 * `PaywallViewModelTest` pins that decision including every case that must not close; this
 * asserts the decision is actually wired to the navigation callback.
 *
 * Method names are camelCase rather than backticked sentences because instrumented tests are
 * dexed and this app's DEX version rejects spaces in method names.
 */
@RunWith(AndroidJUnit4::class)
class PaywallRouteTest {

    @get:Rule
    val compose = createComposeRule()

    private val billing = RouteFakeBillingRepository()

    /**
     * Written from the composition's own thread while the test thread reads it, so it is
     * atomic rather than a plain `Boolean`.
     */
    private val closed = AtomicBoolean(false)

    private fun renderRoute() {
        compose.setContent {
            ExpenseTrackerTheme {
                PaywallRoute(
                    onBackClick = { closed.set(true) },
                    onPrepareForExternalActivity = {},
                    // Supplied rather than resolved through Hilt, so the test controls the
                    // billing state the route reacts to.
                    viewModel = PaywallViewModel(billing),
                )
            }
        }
    }

    @Test
    fun aCompletedPurchaseClosesThePaywall() {
        renderRoute()

        billing.purchaseState.value = PurchaseState.Completed(PurchaseState.Operation.Purchase)

        // The confirmation is held for a beat before the screen leaves, so this waits for the
        // close rather than asserting it synchronously — and a paywall that never closes is
        // exactly the bug being pinned.
        compose.waitUntil(timeoutMillis = CLOSE_WAIT_TIMEOUT_MILLIS) { closed.get() }

        assertTrue("A completed purchase must take the paywall with it", closed.get())
        // Consumed on the way out: an outcome left unacknowledged would be announced again the
        // next time the paywall opens.
        assertTrue("The outcome must be consumed", billing.acknowledgeCount >= 1)
    }

    @Test
    fun aCancelledPurchaseLeavesThePaywallUp() {
        renderRoute()

        billing.purchaseState.value = PurchaseState.Cancelled(PurchaseState.Operation.Purchase)
        compose.waitForIdle()

        // A negative needs a wait long enough for a wrong close to have happened; both waits
        // here are real time, because the hold is a coroutine delay an instrumented test cannot
        // fast-forward.
        Thread.sleep(CLOSE_OBSERVATION_MILLIS)

        assertFalse("A dismissed Play sheet must leave the paywall up", closed.get())
    }

    private companion object {
        /** Longer than the paywall's own 1.5s confirmation hold, with room for a slow device. */
        const val CLOSE_WAIT_TIMEOUT_MILLIS = 5_000L

        /** The hold, plus margin: past this point a close that was going to happen would have. */
        const val CLOSE_OBSERVATION_MILLIS = 2_500L
    }
}

/**
 * Just enough of the billing contract for the route: every flow the paywall collects, plus the
 * acknowledgement that proves the outcome was consumed on the way out.
 *
 * Deliberately separate from the fakes in the unit tests: this one never records intent, it
 * only publishes the state under test.
 */
private class RouteFakeBillingRepository : BillingRepository {

    override val offers = MutableStateFlow<List<SubscriptionOffer>>(emptyList())
    override val isOffersLoaded = MutableStateFlow(true)
    override val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    override val isPremium = MutableStateFlow(false)
    override val isAdFree = MutableStateFlow(false)
    override val managementUrl = MutableStateFlow<String?>(null)
    override val storeEntitlement = MutableStateFlow<StoreEntitlement?>(null)

    var acknowledgeCount = 0
        private set

    override fun purchase(activity: Activity, offerId: String) = Unit

    override fun restore() = Unit

    override fun refreshEntitlement() = Unit

    override fun refreshOffers() = Unit

    override fun acknowledgePurchase() {
        acknowledgeCount++
        purchaseState.value = PurchaseState.Idle
    }
}
