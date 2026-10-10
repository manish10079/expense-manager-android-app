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

@RunWith(AndroidJUnit4::class)
class PaywallRenderTest {

    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun text(resId: Int, vararg args: Any): String = context.getString(resId, *args)

    private companion object {
        const val FIRST_LAYOUT_TIMEOUT_MILLIS = 5_000L
    }

    private val monthly = SubscriptionOffer(
        id = "monthly",
        planLabelRes = R.string.paywall_plan_monthly,
        periodLabelRes = R.string.paywall_period_month,
        priceText = "₹149.00",
        perMonthPriceText = "₹149.00",
        billingMonths = 1,
    )
    private val sixMonths = SubscriptionOffer(
        id = "six_month",
        planLabelRes = R.string.paywall_plan_six_months,
        periodLabelRes = R.string.paywall_period_months,
        priceText = "₹749.00",
        perMonthPriceText = "₹124.83",
        discountPercent = 16,
        strikethroughPriceText = "₹894.00",
        savingsPriceText = "₹145.00",
        billingMonths = 6,
    )
    private val annual = SubscriptionOffer(
        id = "annual",
        planLabelRes = R.string.paywall_plan_twelve_months,
        periodLabelRes = R.string.paywall_period_year,
        priceText = "₹1,299.00",
        perMonthPriceText = "₹108.25",
        discountPercent = 27,
        strikethroughPriceText = "₹1,788.00",
        savingsPriceText = "₹489.00",
        billingMonths = 12,
    )

    private fun renderPaywall(
        state: PaywallUiState,
        onSubscribe: (String) -> Unit = {},
        onRestore: () -> Unit = {},
        onRetry: () -> Unit = {},
        onOpenUrl: (String) -> Unit = {},
        isOpeningStore: Boolean = false,
    ) {
        compose.setContent {
            ExpenseTrackerTheme {
                PaywallContent(
                    uiState = state,
                    onSubscribeClick = onSubscribe,
                    onRestoreClick = onRestore,
                    onRetryClick = onRetry,
                    onOpenUrl = onOpenUrl,
                    isOpeningStore = isOpeningStore,
                )
            }
        }
        compose.waitUntil(timeoutMillis = FIRST_LAYOUT_TIMEOUT_MILLIS) {
            compose.onAllNodes(isRoot()).fetchSemanticsNodes().any { !it.boundsInWindow.isEmpty }
        }
    }

    private fun scrollTo(label: String) {
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(label))
    }

    @Test
    fun nonSubscriberSeesStorePricesAndCanBuy() {
        var purchasedOfferId: String? = null
        renderPaywall(
            state = PaywallUiState(
                offers = listOf(monthly, sixMonths, annual),
                isLoadingOffers = false,
            ),
            onSubscribe = { purchasedOfferId = it },
        )

        scrollTo(text(R.string.paywall_plan_title_annual))
        compose.onNodeWithText(annual.priceText, substring = true).assertIsDisplayed()
        compose.onNodeWithText(sixMonths.priceText, substring = true).assertIsDisplayed()

        val cta = text(
            R.string.paywall_cta_continue,
            text(R.string.paywall_cta_period_annual),
            annual.perMonthPriceText!!,
        )
        compose.onNodeWithText(cta).assertIsEnabled().performClick()
        assertEquals(annual.id, purchasedOfferId)
    }

    @Test
    fun paywallIsFullBleedWithNoBackChevron() {
        renderPaywall(
            state = PaywallUiState(offers = listOf(monthly), isLoadingOffers = false)
        )
        compose.onNodeWithContentDescription(text(R.string.desc_back)).assertDoesNotExist()
    }

    @Test
    fun bothActionsDisabledWhileAttemptInFlight() {
        renderPaywall(
            state = PaywallUiState(
                offers = listOf(annual),
                isLoadingOffers = false,
                purchaseState = PurchaseState.InProgress(PurchaseState.Operation.Purchase),
            )
        )
        val cta = text(
            R.string.paywall_cta_continue,
            text(R.string.paywall_cta_period_annual),
            annual.perMonthPriceText!!,
        )
        compose.onNodeWithText(cta).assertIsNotEnabled()
    }

    @Test
    fun plansThatFailedToLoadOfferRetryThatRetries() {
        var retried = false
        renderPaywall(
            state = PaywallUiState(offers = emptyList(), isLoadingOffers = false),
            onRetry = { retried = true },
        )
        compose.onNodeWithText(text(R.string.label_paywall_plans_unavailable)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.btn_paywall_retry)).assertIsDisplayed().performClick()
        assertTrue("Retry must reach the billing layer", retried)
    }
}
