package com.mknlabs.expensetracker.feature.profile.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.navigation.LocalUpgradeToPro
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.monetization.StoreEntitlement
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Renders the membership screen in each of its four states and asserts the exact copy and
 * the exact call to action.
 *
 * <b>Why rendering, not just the resolver.</b> `MembershipStatusTest` proves which state the
 * screen *decides* it is in, but nothing in a JVM test proves what it then *draws*. The two
 * bugs this screen has actually shipped — a paying subscriber dated "01 Jan 1970", and a
 * ProPass holder offered a Manage Subscription button for a subscription they did not have —
 * both type-checked and both passed every unit test. They are only visible in a rendered
 * tree, which is what this asserts.
 *
 * Dates are compared against the same formatting the screen performs, so a wrong date fails
 * here rather than looking plausible.
 *
 * Method names are camelCase rather than backticked sentences on purpose: instrumented
 * tests are dexed, and this app's DEX version rejects spaces in method names, so a
 * backticked name fails the build at `dexBuilderDebugAndroidTest`. JVM tests in `app/src/test`
 * are unaffected and keep the sentence style.
 */
@RunWith(AndroidJUnit4::class)
class MembershipDetailsRenderTest {

    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val now = System.currentTimeMillis()

    private fun text(resId: Int, vararg args: Any): String = context.getString(resId, *args)

    /** Formats a timestamp the way the screen does, so the assertion is about the value. */
    private fun dateFor(timestamp: Long, patternResId: Int): String =
        SimpleDateFormat(text(patternResId), Locale.getDefault()).format(Date(timestamp))

    private fun renderScreen(
        userTier: UserTier = UserTier.FREE,
        proExpiryTimestamp: Long = 0L,
        isAnonymous: Boolean = false,
        storeEntitlement: StoreEntitlement? = null,
        isRestoring: Boolean = false,
        onUpgradeToPro: () -> Unit = {},
        onRestore: () -> Unit = {},
    ) {
        compose.setContent {
            ExpenseTrackerTheme {
                CompositionLocalProvider(LocalUpgradeToPro provides onUpgradeToPro) {
                    MembershipDetailsContent(
                        userTier = userTier,
                        proExpiryTimestamp = proExpiryTimestamp,
                        isAnonymous = isAnonymous,
                        storeEntitlement = storeEntitlement,
                        isRestoring = isRestoring,
                        onBackClick = {},
                        onRestoreClick = onRestore
                    )
                }
            }
        }
        // The first composition in a fresh test process can be inspected before the window
        // has been measured, which makes a node that is genuinely on screen report zero
        // bounds and fail `assertIsDisplayed`. Waiting for the root to have real bounds
        // keeps every assertion below about the layout rather than about that frame.
        compose.waitUntil(timeoutMillis = FIRST_LAYOUT_TIMEOUT_MILLIS) {
            compose.onAllNodes(isRoot()).fetchSemanticsNodes().any { !it.boundsInWindow.isEmpty }
        }
    }

    /**
     * Brings [label] into view before it is asserted on.
     *
     * The calls to action live at the bottom of a `LazyColumn`, and a lazy list composes only
     * what is near the viewport — so without this a button could look "missing" because it was
     * never created. Scrolling first also proves the user can actually reach it.
     */
    private fun scrollTo(label: String) {
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(label))
    }

    // --- the two Pro states, which need opposite actions ---

    @Test
    fun renewingSubscriberSeesStoreDateAndManageAction() {
        val renewalDate = now + THIRTY_DAYS
        var paywallOpened = false

        renderScreen(
            userTier = UserTier.PREMIUM,
            // Zero on purpose: this is what a real subscriber's profile holds.
            proExpiryTimestamp = 0L,
            storeEntitlement = StoreEntitlement(
                expirationDateMillis = renewalDate,
                willRenew = true,
                hasBillingIssue = false
            ),
            onUpgradeToPro = { paywallOpened = true }
        )

        compose.onNodeWithText(text(R.string.label_pro_subscription_active)).assertIsDisplayed()
        // Where Pro comes from is named on the card, not left to be inferred.
        compose.onNodeWithText(text(R.string.label_pro_source_subscription)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.label_pro_source_pro_pass)).assertDoesNotExist()
        compose.onNodeWithText(
            text(
                R.string.label_pro_renews_on,
                dateFor(renewalDate, R.string.date_pattern_full_short)
            )
        ).assertIsDisplayed()

        val manageLabel = text(R.string.btn_manage_subscription)
        scrollTo(manageLabel)
        compose.onNodeWithText(manageLabel).assertIsDisplayed().performClick()
        assertTrue("Manage Subscription must open the paywall", paywallOpened)

        // A subscriber has a subscription to manage, so no buy CTA on the card.
        scrollTo(text(R.string.btn_restore_purchase))
        compose.onNodeWithText(text(R.string.btn_paywall_subscribe)).assertDoesNotExist()
        // Cancelling is the store's job, and the screen says where: right under the restore
        // action a subscriber would otherwise try first.
        compose.onNodeWithText(text(R.string.label_cancel_subscription_anytime)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.label_cancel_subscription_where)).assertIsDisplayed()
    }

    @Test
    fun cancelledSubscriptionSaysItEndsInsteadOfRenewing() {
        val endDate = now + THIRTY_DAYS

        renderScreen(
            userTier = UserTier.PREMIUM,
            storeEntitlement = StoreEntitlement(
                expirationDateMillis = endDate,
                willRenew = false,
                hasBillingIssue = false
            )
        )

        compose.onNodeWithText(
            text(
                R.string.label_pro_subscription_ends_on,
                dateFor(endDate, R.string.date_pattern_full_short)
            )
        ).assertIsDisplayed()
    }

    @Test
    fun failedChargeIsExplainedInsteadOfPromisingRenewal() {
        val renewalDate = now + THIRTY_DAYS

        renderScreen(
            userTier = UserTier.PREMIUM,
            storeEntitlement = StoreEntitlement(
                expirationDateMillis = renewalDate,
                willRenew = true,
                hasBillingIssue = true
            )
        )

        compose.onNodeWithText(text(R.string.msg_subscription_billing_issue)).assertIsDisplayed()
        compose.onNodeWithText(
            text(
                R.string.label_pro_renews_on,
                dateFor(renewalDate, R.string.date_pattern_full_short)
            )
        ).assertDoesNotExist()
    }

    @Test
    fun proPassHolderIsOfferedSubscriptionNotManageAction() {
        val passExpiry = now + TEN_DAYS
        var paywallOpened = false

        renderScreen(
            userTier = UserTier.PREMIUM,
            proExpiryTimestamp = passExpiry,
            storeEntitlement = null,
            onUpgradeToPro = { paywallOpened = true }
        )

        compose.onNodeWithText(text(R.string.label_pro_pass_active)).assertIsDisplayed()
        // The same Pro state means the opposite thing here, and the card now says so.
        compose.onNodeWithText(text(R.string.label_pro_source_pro_pass)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.label_pro_source_subscription)).assertDoesNotExist()
        compose.onNodeWithText(
            text(
                R.string.label_pro_expires_on,
                dateFor(passExpiry, R.string.date_pattern_pro_expiry)
            )
        ).assertIsDisplayed()

        val subscribeLabel = text(R.string.btn_paywall_subscribe)
        scrollTo(subscribeLabel)
        compose.onNodeWithText(subscribeLabel).assertIsDisplayed().performClick()
        assertTrue("Subscribe must open the paywall", paywallOpened)

        // Pro with no store subscription has nothing to manage — and nothing to cancel
        // either, so the Play Store cancellation path must not be offered.
        scrollTo(text(R.string.btn_restore_purchase))
        compose.onNodeWithText(text(R.string.btn_manage_subscription)).assertDoesNotExist()
        compose.onNodeWithText(text(R.string.label_cancel_subscription_anytime)).assertDoesNotExist()
    }

    // --- the date regressions this suite exists for ---

    @Test
    fun subscriberWithNoStoreDateIsNeverShownEpochDate() {
        renderScreen(
            userTier = UserTier.PREMIUM,
            proExpiryTimestamp = 0L,
            // Active entitlement with no end date (lifetime, or a store that has not reported
            // one yet). Still a subscriber — the point is that no date may be printed for it.
            storeEntitlement = StoreEntitlement(
                expirationDateMillis = null,
                willRenew = true,
                hasBillingIssue = false
            )
        )

        compose.onNodeWithText(text(R.string.msg_subscription_active_desc)).assertIsDisplayed()
        // Formatting the unset local timestamp is what printed "01 Jan 1970" to paying users.
        compose.onNodeWithText(dateFor(0L, R.string.date_pattern_full_short)).assertDoesNotExist()
        compose.onNodeWithText(dateFor(0L, R.string.date_pattern_pro_expiry)).assertDoesNotExist()
    }

    @Test
    fun permanentProPassGrantIsNeverShownEpochDate() {
        // Pro with no store entitlement and no recorded expiry: the legacy permanent grant.
        //
        // This is also what a subscriber looks like *before* the first `CustomerInfo`
        // arrives — the screen cannot know they subscribe yet, so it resolves the one state
        // it can justify. Worth knowing: no date is printed either way, which is the property
        // this test pins.
        renderScreen(userTier = UserTier.PREMIUM, proExpiryTimestamp = 0L, storeEntitlement = null)

        compose.onNodeWithText(text(R.string.msg_pro_active_no_expiry)).assertIsDisplayed()
        compose.onNodeWithText(dateFor(0L, R.string.date_pattern_pro_expiry)).assertDoesNotExist()
        compose.onNodeWithText(dateFor(0L, R.string.date_pattern_full_short)).assertDoesNotExist()
    }

    @Test
    fun subscriberWithLeftoverPassExpiryIsNeverDatedFromIt() {
        val leftoverPassExpiry = now + FIVE_DAYS

        renderScreen(
            userTier = UserTier.PREMIUM,
            // Reachable: a ProPass holder who subscribes while the pass still runs.
            proExpiryTimestamp = leftoverPassExpiry,
            storeEntitlement = StoreEntitlement(
                expirationDateMillis = null,
                willRenew = true,
                hasBillingIssue = false
            )
        )

        compose.onNodeWithText(text(R.string.msg_subscription_active_desc)).assertIsDisplayed()
        compose.onNodeWithText(
            dateFor(leftoverPassExpiry, R.string.date_pattern_pro_expiry)
        ).assertDoesNotExist()
    }

    @Test
    fun storeRenewalDateOutranksLeftoverPassTimestamp() {
        val leftoverPassExpiry = now + FIVE_DAYS
        val renewalDate = now + THIRTY_DAYS

        renderScreen(
            userTier = UserTier.PREMIUM,
            proExpiryTimestamp = leftoverPassExpiry,
            storeEntitlement = StoreEntitlement(
                expirationDateMillis = renewalDate,
                willRenew = true,
                hasBillingIssue = false
            )
        )

        compose.onNodeWithText(
            text(
                R.string.label_pro_renews_on,
                dateFor(renewalDate, R.string.date_pattern_full_short)
            )
        ).assertIsDisplayed()
        compose.onNodeWithText(
            dateFor(leftoverPassExpiry, R.string.date_pattern_pro_expiry)
        ).assertDoesNotExist()
    }

    // --- the non-Pro states ---

    @Test
    fun freeUserIsOfferedUpgradeAndNoRenewalCopy() {
        renderScreen(userTier = UserTier.FREE)

        compose.onNodeWithText(text(R.string.label_free_tier)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.btn_upgrade_now)).assertIsDisplayed()

        // Anchored at the bottom of the list so the absence is checked where those rows
        // would actually be composed, not merely off-screen.
        scrollTo(text(R.string.btn_restore_purchase))
        compose.onNodeWithText(text(R.string.btn_manage_subscription)).assertDoesNotExist()
        compose.onNodeWithText(text(R.string.btn_paywall_subscribe)).assertDoesNotExist()
        compose.onNodeWithText(text(R.string.msg_subscription_active_desc)).assertDoesNotExist()
        // Nothing is being charged, so there is nothing to tell them to cancel.
        compose.onNodeWithText(text(R.string.label_cancel_subscription_anytime)).assertDoesNotExist()
    }

    @Test
    fun anonymousUserSeesOfflineStateWithSignInAction() {
        renderScreen(userTier = UserTier.FREE, isAnonymous = true)

        compose.onNodeWithText(text(R.string.label_unlimited_offline)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.btn_sign_in_register)).assertIsDisplayed()
    }

    // --- the restore action, which used to leave the screen ---

    @Test
    fun restoreRunsInPlaceInsteadOfOpeningThePaywall() {
        var restoreCount = 0
        var paywallOpened = false

        renderScreen(
            userTier = UserTier.PREMIUM,
            proExpiryTimestamp = now + TEN_DAYS,
            onUpgradeToPro = { paywallOpened = true },
            onRestore = { restoreCount++ }
        )

        val restoreLabel = text(R.string.btn_restore_purchase)
        scrollTo(restoreLabel)
        compose.onNodeWithText(restoreLabel).performClick()

        assertEquals("Restore must reach the billing layer", 1, restoreCount)
        assertFalse("Restore must not send the user to the paywall", paywallOpened)
    }

    @Test
    fun restoreIsDisabledWhileARestoreIsInFlight() {
        renderScreen(
            userTier = UserTier.PREMIUM,
            proExpiryTimestamp = now + TEN_DAYS,
            isRestoring = true
        )

        val restoreLabel = text(R.string.btn_restore_purchase)
        scrollTo(restoreLabel)
        compose.onNodeWithText(restoreLabel).assertIsNotEnabled()
    }

    private companion object {
        const val FIRST_LAYOUT_TIMEOUT_MILLIS = 5_000L
        const val FIVE_DAYS = 1_000L * 60 * 60 * 24 * 5
        const val TEN_DAYS = 1_000L * 60 * 60 * 24 * 10
        const val THIRTY_DAYS = 1_000L * 60 * 60 * 24 * 30
    }
}
