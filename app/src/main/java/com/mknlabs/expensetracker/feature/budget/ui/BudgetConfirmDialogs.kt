package com.mknlabs.expensetracker.feature.budget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmBodyDark
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmBodyLight
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.isDark

@Composable
internal fun DeleteBudgetDialog(
    budgetName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModernConfirmationDialog(
        onDismissRequest = onDismiss,
        title = stringResource(id = R.string.label_delete_budget),
        text = stringResource(id = R.string.label_remove_the_budget_for_val_this, budgetName),
        confirmButtonText = stringResource(id = R.string.label_delete_1),
        onConfirm = onConfirm,
        headerIcon = Icons.Filled.Delete,
        dismissButtonText = stringResource(id = R.string.label_cancel_1),
        onDismiss = onDismiss
    )
}

@Composable
internal fun DeleteRecurringDialog(
    recurringName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModernConfirmationDialog(
        onDismissRequest = onDismiss,
        title = stringResource(id = R.string.label_delete_recurring),
        text = stringResource(id = R.string.label_remove_val_from_recurring_trac, recurringName),
        confirmButtonText = stringResource(id = R.string.label_delete_1),
        onConfirm = onConfirm,
        headerIcon = Icons.Filled.Delete,
        dismissButtonText = stringResource(id = R.string.label_cancel_1),
        onDismiss = onDismiss
    )
}

@Composable
internal fun MuteRecurringDialog(
    ruleName: String,
    onDismiss: () -> Unit,
    onConfirm: (dontShowAgain: Boolean) -> Unit
) {
    var dontShowAgain by rememberSaveable { mutableStateOf(false) }

    ModernConfirmationDialog(
        onDismissRequest = onDismiss,
        title = stringResource(id = R.string.label_mute_recurring_title),
        text = stringResource(id = R.string.label_mute_recurring_message, ruleName),
        confirmButtonText = stringResource(id = R.string.label_mute_yes),
        onConfirm = { onConfirm(dontShowAgain) },
        headerIcon = Icons.Filled.NotificationsOff,
        dismissButtonText = stringResource(id = R.string.label_no),
        onDismiss = onDismiss,
        extraContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { dontShowAgain = !dontShowAgain }
                    .padding(vertical = 4.dp)
            ) {
                val checked = dontShowAgain
                val accent = MaterialTheme.colorScheme.accentInk
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (checked) accent else Color.Transparent)
                        .border(
                            width = 1.5.dp,
                            color = if (checked) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (checked) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.label_dont_show_again),
                    color = if (MaterialTheme.colorScheme.isDark) {
                        ModernConfirmBodyDark
                    } else {
                        ModernConfirmBodyLight
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 22.sp
                )
            }
        }
    )
}
