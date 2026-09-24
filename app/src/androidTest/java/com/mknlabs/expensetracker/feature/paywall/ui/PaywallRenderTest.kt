package com.mknlabs.expensetracker.feature.paywall.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.monetization.PurchaseState
import com.mknlabs.expensetracker.monetization.SubscriptionOffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Renders the paywall and asserts what a user can actually see and press.
 *
 * Renders `PaywallContent` directly, the same composable `PaywallRoute` feeds, so the
 * assertions are about the layout rather than about the ViewModel state feeding it — the
 * state mapping is already pinned by `PaywallViewModelTest`, and what has never been covered
 * is whether the buttons it computes are *on screen* and *enabled*.
 *
 * The prices here are fake store strings supplied by the test. That is deliberate: the app is
 * forbidden from assembling a price, so the only correct behaviour to assert is that the
 * exact string the store provided is the exact string rendered.
 *
 * Method names are camelCase rather than backticked sentences because instrumented tests are
 * dexed and this app's DEX version rejects spaces in method names.
 */
@RunWith(AndroidJUnit4::class)
class PaywallRenderTest {

    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun text(resId: Int, vararg args: Any): String = context.getString(resId, *args)

    private companion object {
        const val FIRST_LAYOUT_TIMEOUT_MILLIS = 5_000L
    }

    // Store-formatted prices, as Play would return them.
    private val monthly = SubscriptionOffer(
        id = "monthly",
        planLabelRes = R.string.paywall_plan_monthly,
        periodLabelRes = R.string.paywall_period_month,
        priceText = "₹149.00"
    )
    private val sixMonths = SubscriptionOffer(
        id = "six_month",
        planLabelRes = R.string.paywall_plan_six_months,
        periodLabelRes = R.string.paywall_period_months,
        priceText = "₹749.00"
    )
    private val annual = SubscriptionOffer(
        id = "annual",
        planLabelRes = R.string.paywall_plan_twelve_months,
        periodLabelRes = R.string.paywall_period_year,
        priceText = "₹1,299.00"
    )

    private fun renderPaywall(
        state: PaywallUiState,
        onSubscribe: (String) -> Unit = {},
        onRestore: () -> Unit = {},
        onRetry: () -> Unit = {},
        onOpenUrl: (String) -> Unit = {},
    ) {
        compose.setContent {
            ExpenseTrackerTheme {
                PaywallContent(
                    uiState = state,
                    onSubscribeClick = onSubscribe,
                    onRestoreClick = onRestore,
                    onRetryClick = onRetry,
                    onOpenUrl = onOpenUrl,
                )
            }
        }
        // See the membership render test: guard against inspecting the tree before the window
        // has been measured, which would fail a screen that is in fact drawn.
        compose.waitUntil(timeoutMillis = FIRST_LAYOUT_TIMEOUT_MILLIS) {
            compose.onAllNodes(isRoot()).fetchSemanticsNodes().any { !it.boundsInWindow.isEmpty }
        }
    }

    /** See the note in the membership render test: a lazy list only composes what is near. */
    private fun scrollTo(label: String) {
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(label))
    }

    /** The last item in the list, used to prove a row is absent at the bottom. */
    private fun scrollToEnd() {
        scrollTo(text(R.string.title_privacy_policy))
    }

    @Test
    fun nonSubscriberSeesStorePricesAndCanBuyAndRestore() {
        var purchasedOfferId: String? = null
        var restoreCount = 0

        renderPaywall(
            state = PaywallUiState(
                offers = listOf(monthly, sixMonths, annual),
                isLoadingOffers = false
            ),
            onSubscribe = { purchasedOfferId = it },
            onRestore = { restoreCount++ }
        )

        // Every plan renders the store's own string, unmodified.
        scrollTo(monthly.priceText)
        compose.onNodeWithText(monthly.priceText).assertIsDisplayed()
        compose.onNodeWithText(sixMonths.priceText).assertIsDisplayed()
        compose.onNodeWithText(annual.priceText).assertIsDisplayed()

        val restoreLabel = text(R.string.btn_restore_purchase)
        scrollTo(restoreLabel)
        compose.onNodeWithText(restoreLabel).assertIsDisplayed().performClick()
        assertEquals("Restore must reach the billing layer", 1, restoreCount)

        val subscribeLabel = text(R.string.btn_paywall_subscribe)
        scrollTo(subscribeLabel)
        compose.onNodeWithText(subscribeLabel).assertIsEnabled().performClick()
        // The first plan is preselected, so the primary action is never a dead tap.
        assertEquals(monthly.id, purchasedOfferId)
    }

    @Test
    fun paywallIsFullBleedWithNoBackChevron() {
        renderPaywall(
            state = PaywallUiState(offers = listOf(monthly), isLoadingOffers = false)
        )

        // An upsell, not a page with a title bar: the header and its chevron are gone. The
        // chevron was also a second way out of the screen, which disagreed with system Back.
        compose.onNodeWithContentDescription(text(R.string.desc_back)).assertDoesNotExist()
    }

    @Test
    fun subscriberIsToldOnPro_canManage_andCanStillChangePlan() {
        renderPaywall(
            state = PaywallUiState(
                offers = listOf(monthly, sixMonths, annual),
                isLoadingOffers = false,
                isPremium = true,
                managementUrl = "https://play.google.com/store/account/subscriptions"
            )
        )

        val alreadyPro = text(R.string.msg_paywall_already_pro)
        scrollTo(alreadyPro)
        compose.onNodeWithText(alreadyPro).assertIsDisplayed()

        val manageLabel = text(R.string.btn_manage_subscription)
        scrollTo(manageLabel)
        compose.onNodeWithText(manageLabel).assertIsDisplayed()

        // Switching plan is a supported action, so the button stays live for a subscriber.
        val subscribeLabel = text(R.string.btn_paywall_subscribe)
        scrollTo(subscribeLabel)
        compose.onNodeWithText(subscribeLabel).assertIsEnabled()
    }

    @Test
    fun subscriberWithoutManagementPageIsNotOfferedLink() {
        renderPaywall(
            state = PaywallUiState(
                offers = listOf(monthly),
                isLoadingOffers = false,
                isPremium = true,
                managementUrl = null
            )
        )

        // Anchored at the end of the list, where that row would be composed if it existed.
        scrollToEnd()
        compose.onNodeWithText(text(R.string.btn_manage_subscription)).assertDoesNotExist()
    }

    @Test
    fun bothActionsDisabledWhileAttemptInFlight() {
        renderPaywall(
            state = PaywallUiState(
                offers = listOf(monthly),
                isLoadingOffers = false,
                purchaseState = PurchaseState.InProgress(PurchaseState.Operation.Purchase)
            )
        )

        val subscribeLabel = text(R.string.btn_paywall_subscribe)
        scrollTo(subscribeLabel)
        compose.onNodeWithText(subscribeLabel).assertIsNotEnabled()

        val restoreLabel = text(R.string.btn_restore_purchase)
        scrollTo(restoreLabel)
        compose.onNodeWithText(restoreLabel).assertIsNotEnabled()
    }

    @Test
    fun plansThatFailedToLoadOfferRetryThatRetries() {
        var retried = false

        renderPaywall(
            state = PaywallUiState(offers = emptyList(), isLoadingOffers = false),
            onRetry = { retried = true }
        )

        val unavailable = text(R.string.label_paywall_plans_unavailable)
        scrollTo(unavailable)
        compose.onNodeWithText(unavailable).assertIsDisplayed()

        compose.onNodeWithText(text(R.string.btn_paywall_retry)).assertIsDisplayed().performClick()
        assertTrue("Retry must reach the billing layer", retried)

        // Nothing to buy: the primary action stays visible but cannot be pressed, so the
        // screen never offers a tap that would do nothing.
        val subscribeLabel = text(R.string.btn_paywall_subscribe)
        scrollTo(subscribeLabel)
        compose.onNodeWithText(subscribeLabel).assertIsNotEnabled()
    }

    @Test
    fun autoRenewalTermsAreOnPurchaseScreenItself() {
        renderPaywall(
            state = PaywallUiState(offers = listOf(monthly), isLoadingOffers = false)
        )

        // Google Play requires this disclosure on the screen where the purchase happens.
        val disclosure = text(R.string.msg_paywall_renewal_disclosure)
        scrollTo(disclosure)
        compose.onNodeWithText(disclosure).assertIsDisplayed()
    }
}
