package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R

@Composable
fun AdRewardDialog(
    featureName: String,
    /** Length of the pass this ad grants, from Remote Config (never hardcode it here). */
    durationMinutes: Int,
    onDismiss: () -> Unit,
    onWatchAdClick: () -> Unit,
    isLoading: Boolean = false
) {
    val durationLabel = adPassDurationLabel(durationMinutes)

    ModernConfirmationDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.label_full_access_for_duration, durationLabel),
        text = stringResource(R.string.label_watch_a_short_video_to_unlock, featureName, durationLabel),
        confirmButtonText = stringResource(R.string.label_watch_ad_unlock),
        onConfirm = {
            if (!isLoading) onWatchAdClick()
        },
        headerIcon = Icons.Filled.PlayArrow,
        dismissButtonText = stringResource(R.string.label_cancel),
        onDismiss = onDismiss
    )
}
