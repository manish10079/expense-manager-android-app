package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.utils.findFragmentActivity

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
    // In preview/design mode, skip Hilt dependency and always show as granted
    if (LocalInspectionMode.current) {
        content(AccessStatus.Granted, onAction)
        return
    }

    val context = LocalContext.current
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val accessStatus by monetizationViewModel.getAccessStatus(feature, optionId).collectAsStateWithLifecycle()

    var showPremiumSheet by remember { mutableStateOf(false) }
    var showComingSoonDialog by remember { mutableStateOf(false) }
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
                showPremiumSheet = false
                showComingSoonDialog = true
            }
        )
    }

    if (showComingSoonDialog) {
        ComingSoonDialog(
            onDismiss = { showComingSoonDialog = false }
        )
    }

    if (showAdDialog) {
        val adPassMinutes by monetizationViewModel.adPassDurationMinutes.collectAsStateWithLifecycle()
        AdRewardDialog(
            featureName = actualDisplayName,
            durationMinutes = adPassMinutes,
            onDismiss = { showAdDialog = false },
            onWatchAdClick = {
                // Gated content can live in its own dialog window (e.g. a bottom sheet),
                // where LocalContext is a ContextThemeWrapper rather than the Activity —
                // a plain `as? Activity` cast would be null and no ad would ever load.
                val activity = context.findFragmentActivity()
                if (activity != null) {
                    monetizationViewModel.onAdWatched(activity, feature, optionId)
                }
                showAdDialog = false
            }
        )
    }
}
