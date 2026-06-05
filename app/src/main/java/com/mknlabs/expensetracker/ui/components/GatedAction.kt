package com.mknlabs.expensetracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel

/**
 * A reactive wrapper component that gates actions based on monetization status.
 * 
 * @param feature The feature to check access for.
 * @param optionId Optional specific option within the feature.
 * @param onAction The logic to execute if access is granted.
 * @param content The UI content (e.g., a button or card). Receives the current access status.
 */
@Composable
fun GatedAction(
    feature: Feature,
    optionId: String? = null,
    displayName: String? = null, // Optional override for more specific names
    onAction: () -> Unit,
    content: @Composable (status: AccessStatus, onClick: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val accessStatus by monetizationViewModel.getAccessStatus(feature, optionId).collectAsStateWithLifecycle()

    var showPremiumSheet by remember { mutableStateOf(false) }
    var showAdDialog by remember { mutableStateOf(false) }

    val actualDisplayName = displayName ?: feature.displayName

    val handleAction = {
        when (accessStatus) {
            is AccessStatus.Granted -> onAction()
            is AccessStatus.DeniedPremium -> showPremiumSheet = true
            is AccessStatus.DeniedAd -> showAdDialog = true
        }
    }

    content(accessStatus, handleAction)

    if (showPremiumSheet) {
        PremiumGateSheet(
            onDismiss = { showPremiumSheet = false },
            onUpgradeClick = {
                monetizationViewModel.onPurchaseSimulated()
                showPremiumSheet = false
            }
        )
    }

    if (showAdDialog) {
        AdRewardDialog(
            featureName = actualDisplayName,
            onDismiss = { showAdDialog = false },
            onWatchAdClick = {
                val activity = context as? Activity
                if (activity != null) {
                    monetizationViewModel.onAdWatched(activity, feature, optionId)
                }
                showAdDialog = false
            }
        )
    }
}
