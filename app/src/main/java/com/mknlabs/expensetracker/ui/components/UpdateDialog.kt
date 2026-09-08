package com.mknlabs.expensetracker.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.models.UpdateInfo

/**
 * In-app update notification dialog (Material 3).
 *
 * Title and message come from Firebase Remote Config and fall back to
 * localised strings when blank. When [force] is true the dialog cannot be
 * dismissed — no back press, no outside tap, and no "Later" button.
 */
@Composable
fun UpdateDialog(
    info: UpdateInfo,
    force: Boolean,
    onUpdateNow: () -> Unit,
    onLater: () -> Unit
) {
    AlertDialog(
        onDismissRequest = if (force) ({}) else onLater,
        properties = DialogProperties(
            dismissOnBackPress = !force,
            dismissOnClickOutside = !force
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Rounded.RocketLaunch,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = when {
                    info.updateTitle.isNotBlank() -> info.updateTitle
                    info.latestVersion.isNotBlank() -> stringResource(R.string.title_update_available_version, info.latestVersion)
                    else -> stringResource(R.string.title_update_available)
                },
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = info.updateMessage.ifBlank { stringResource(R.string.msg_update_available) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(onClick = onUpdateNow) {
                Text(stringResource(R.string.btn_update_now))
            }
        },
        dismissButton = {
            if (!force) {
                TextButton(onClick = onLater) {
                    Text(stringResource(R.string.label_later))
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}