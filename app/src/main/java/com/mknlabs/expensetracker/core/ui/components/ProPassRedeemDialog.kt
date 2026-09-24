package com.mknlabs.expensetracker.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.monetization.RedemptionState

@Composable
fun ProPassRedeemDialog(
    viewModel: MonetizationViewModel,
    onDismiss: () -> Unit,
    onVerifyEmail: () -> Unit = {}
) {
    var code by remember { mutableStateOf("") }
    val state by viewModel.redemptionState.collectAsStateWithLifecycle()
    // Only a *store subscription* is grounds for refusing a code.
    //
    // This check used to be `userTier == PREMIUM`, which refused two users it should not:
    // a ProPass holder, whose code would have stacked (the `redeemProPass` function computes
    // `max(now, existingExpiry) + durationDays` and is explicit that existing premium is
    // "extended, not overwritten"), and a subscriber, who was shown ProPass-specific copy
    // about waiting for a pass to expire. The first is why the Settings row stays enabled for
    // a ProPass holder at all, and this dialog was contradicting it one tap later.
    val hasActiveStoreSubscription by viewModel.hasActiveStoreSubscription.collectAsStateWithLifecycle()
    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val isGoogleAccount = firebaseUser?.providerData?.any { it.providerId == "google.com" } == true
    val isEmailVerified = firebaseUser?.isEmailVerified == true || isGoogleAccount

    // A subscription already covers the window a new grant would occupy, so the code would
    // run out unused. Say that, rather than asking the user to wait for an expiry.
    if (hasActiveStoreSubscription) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.ConfirmationNumber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(id = R.string.title_pro_pass_subscribed),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.msg_pro_pass_subscribed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.btn_ok))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
        return
    }

    // If email is not verified, show verification prompt instead of redemption form
    if (!isEmailVerified) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(id = R.string.dialog_verify_email_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.dialog_verify_email_pro_pass),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(onClick = {
                    firebaseUser?.sendEmailVerification()
                    onVerifyEmail()
                    onDismiss()
                }) {
                    Text(text = stringResource(id = R.string.label_verify_now))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.label_later))
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = { 
            if (state !is RedemptionState.Loading) {
                viewModel.resetRedemptionState()
                onDismiss()
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = if (state is RedemptionState.Success) Icons.Rounded.CheckCircle else Icons.Rounded.ConfirmationNumber,
                contentDescription = null,
                tint = if (state is RedemptionState.Success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.title_activate_pro_pass),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (val currentState = state) {
                    is RedemptionState.Idle, is RedemptionState.Loading -> {
                        Text(
                            text = stringResource(id = R.string.msg_enter_pro_pass_code),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it.uppercase() },
                            placeholder = { Text(stringResource(id = R.string.placeholder_pro_pass_code)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = currentState !is RedemptionState.Loading,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    is RedemptionState.Success -> {
                        Text(
                            text = stringResource(id = R.string.msg_pro_pass_activated, currentState.days),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                    is RedemptionState.Error -> {
                        Text(
                            // Localized here rather than carried in the state: the server's
                            // English message is a developer aid, not copy.
                            text = stringResource(id = redemptionErrorMessageRes(currentState.error)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it.uppercase() },
                            placeholder = { Text(stringResource(id = R.string.placeholder_pro_pass_code)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            when (state) {
                is RedemptionState.Success -> {
                    Button(
                        onClick = {
                            viewModel.resetRedemptionState()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(id = R.string.label_ok))
                    }
                }
                else -> {
                    Button(
                        onClick = { viewModel.redeemProPass(code) },
                        enabled = code.isNotBlank() && state !is RedemptionState.Loading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state is RedemptionState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(id = R.string.btn_activate_pro))
                        }
                    }
                }
            }
        },
        dismissButton = {
            if (state !is RedemptionState.Success && state !is RedemptionState.Loading) {
                TextButton(onClick = { 
                    viewModel.resetRedemptionState()
                    onDismiss() 
                }) {
                    Text(stringResource(id = R.string.label_cancel))
                }
            }
        }
    )
}
