package com.mknlabs.expensetracker.feature.paywall.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.monetization.PurchaseState
import com.mknlabs.expensetracker.monetization.SubscriptionOffer
import com.mknlabs.expensetracker.utils.findFragmentActivity

private const val TAG = "PaywallScreen"

@Composable
fun PaywallRoute(
    onBackClick: () -> Unit,
    onPrepareForExternalActivity: () -> Unit,
    onNavigateToMembership: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    var waitingForPlaySheet by remember { mutableStateOf(false) }
    var sawPlaySheet by remember { mutableStateOf(false) }

    BackHandler { onBackClick() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE && uiState.purchaseState is PurchaseState.InProgress) {
                waitingForPlaySheet = false
                sawPlaySheet = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.purchaseState) {
        when (uiState.purchaseState) {
            is PurchaseState.InProgress -> Unit
            is PurchaseState.Cancelled, PurchaseState.Idle -> {
                waitingForPlaySheet = false
                sawPlaySheet = false
            }
            else -> waitingForPlaySheet = false
        }
    }

    val openUrl: (String) -> Unit = { url ->
        try {
            onPrepareForExternalActivity()
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "No app can open $url", e)
        }
    }

    LaunchedEffect(uiState.messageRes, uiState.purchaseState) {
        val messageRes = uiState.messageRes ?: return@LaunchedEffect
        if (uiState.isPurchaseSuccess) return@LaunchedEffect
        if (uiState.purchaseState is PurchaseState.Failed) return@LaunchedEffect
        snackbarHostState.showSnackbar(context.getString(messageRes))
        viewModel.onOutcomeShown()
    }

    val showProcessing = uiState.purchaseState is PurchaseState.InProgress &&
        sawPlaySheet &&
        !waitingForPlaySheet

    PaywallContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        isOpeningStore = waitingForPlaySheet,
        showProcessingOverlay = showProcessing,
        onSubscribeClick = { offerId ->
            context.findFragmentActivity()?.let { activity ->
                waitingForPlaySheet = true
                viewModel.onSubscribeClick(activity, offerId)
            }
        },
        onRestoreClick = viewModel::onRestoreClick,
        onRetryClick = viewModel::onRetryClick,
        onOpenUrl = openUrl,
        onPurchaseSuccessConfirmed = {
            viewModel.onOutcomeShown()
            onNavigateToMembership()
        },
        onErrorDismissed = viewModel::onOutcomeShown,
    )
}

@Preview(name = "Paywall - Light", showBackground = true)
@Composable
private fun PaywallPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        PaywallContent(
            uiState = PaywallPreviewState,
            onSubscribeClick = {},
            onRestoreClick = {},
            onRetryClick = {},
            onOpenUrl = {},
        )
    }
}

@Preview(name = "Paywall - Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PaywallPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        PaywallContent(
            uiState = PaywallPreviewState,
            onSubscribeClick = {},
            onRestoreClick = {},
            onRetryClick = {},
            onOpenUrl = {},
        )
    }
}

private val PaywallPreviewState = PaywallUiState(
    offers = listOf(
        SubscriptionOffer(
            id = "annual",
            planLabelRes = R.string.paywall_plan_twelve_months,
            periodLabelRes = R.string.paywall_period_year,
            priceText = "₹708.00",
            discountPercent = 40,
            strikethroughPriceText = "₹1188.00",
            perMonthPriceText = "₹59.00",
            savingsPriceText = "₹480.00",
            billingMonths = 12,
        ),
        SubscriptionOffer(
            id = "six_months",
            planLabelRes = R.string.paywall_plan_six_months,
            periodLabelRes = R.string.paywall_period_months,
            priceText = "₹474.00",
            discountPercent = 20,
            strikethroughPriceText = "₹594.00",
            perMonthPriceText = "₹79.00",
            savingsPriceText = "₹120.00",
            billingMonths = 6,
        ),
        SubscriptionOffer(
            id = "monthly",
            planLabelRes = R.string.paywall_plan_monthly,
            periodLabelRes = R.string.paywall_period_month,
            priceText = "₹99.00",
            perMonthPriceText = "₹99.00",
            billingMonths = 1,
        ),
    ),
    isLoadingOffers = false,
)
