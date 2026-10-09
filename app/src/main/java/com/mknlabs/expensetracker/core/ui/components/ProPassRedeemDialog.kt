package com.mknlabs.expensetracker.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.sheet
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
    val hasActiveStoreSubscription by viewModel.hasActiveStoreSubscription.collectAsStateWithLifecycle()
    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val isGoogleAccount = firebaseUser?.providerData?.any { it.providerId == "google.com" } == true
    val isEmailVerified = firebaseUser?.isEmailVerified == true || isGoogleAccount

    ProPassRedeemDialogContent(
        code = code,
        onCodeChange = { code = it.uppercase() },
        state = state,
        hasActiveStoreSubscription = hasActiveStoreSubscription,
        isEmailVerified = isEmailVerified,
        onRedeem = { viewModel.redeemProPass(code) },
        onDismiss = {
            viewModel.resetRedemptionState()
            onDismiss()
        },
        onVerifyEmail = {
            firebaseUser?.sendEmailVerification()
            onVerifyEmail()
        }
    )
}

@Composable
fun ProPassRedeemDialogContent(
    code: String,
    onCodeChange: (String) -> Unit,
    state: RedemptionState,
    hasActiveStoreSubscription: Boolean = false,
    isEmailVerified: Boolean = true,
    onRedeem: () -> Unit,
    onDismiss: () -> Unit,
    onVerifyEmail: () -> Unit = {}
) {
    if (hasActiveStoreSubscription) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.sheet,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.ConfirmationNumber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.accentInk,
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

    if (!isEmailVerified) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.sheet,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.accentInk,
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
                    onVerifyEmail()
                    onDismiss()
                }) {
                    Text(text = stringResource(id = R.string.label_verify_now))
                }
            },
            dismissButton = {
                AppTextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.label_later))
                }
            }
        )
        return
    }

    Dialog(
        onDismissRequest = {
            if (state !is RedemptionState.Loading) {
                onDismiss()
            }
        }
    ) {
        ProPassRedeemDialogCard(
            code = code,
            onCodeChange = onCodeChange,
            state = state,
            onRedeem = onRedeem,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun ProPassRedeemDialogCard(
    code: String,
    onCodeChange: (String) -> Unit,
    state: RedemptionState,
    onRedeem: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.sheet,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (state is RedemptionState.Success) Icons.Rounded.CheckCircle else Icons.Rounded.ConfirmationNumber,
                contentDescription = null,
                tint = if (state is RedemptionState.Success) MaterialTheme.colorScheme.accentInk else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.title_activate_pro_pass),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            when (state) {
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
                        onValueChange = onCodeChange,
                        placeholder = { Text(stringResource(id = R.string.placeholder_pro_pass_code)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = state !is RedemptionState.Loading,
                        shape = AppOutlinedFieldDefaults.shape,
                        colors = AppOutlinedFieldDefaults.colors()
                    )
                }
                is RedemptionState.Success -> {
                    Text(
                        text = stringResource(id = R.string.msg_pro_pass_activated, state.days),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
                is RedemptionState.Error -> {
                    Text(
                        text = stringResource(id = redemptionErrorMessageRes(state.error)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = code,
                        onValueChange = onCodeChange,
                        placeholder = { Text(stringResource(id = R.string.placeholder_pro_pass_code)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = true,
                        shape = AppOutlinedFieldDefaults.shape,
                        colors = AppOutlinedFieldDefaults.colors()
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state !is RedemptionState.Success && state !is RedemptionState.Loading) {
                    AppTextButton(onClick = onDismiss) {
                        Text(stringResource(id = R.string.label_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                when (state) {
                    is RedemptionState.Success -> {
                        ProPassGradientButton(onClick = onDismiss) {
                            Text(stringResource(id = R.string.label_ok))
                        }
                    }
                    else -> {
                        ProPassGradientButton(
                            onClick = onRedeem,
                            enabled = code.isNotBlank() && state !is RedemptionState.Loading
                        ) {
                            if (state is RedemptionState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onCta,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(stringResource(id = R.string.btn_activate_pro))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProPassGradientButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onCta,
            disabledContentColor = MaterialTheme.colorScheme.onCta.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(brush = brandGradient(alpha = if (enabled) 1f else 0.45f))
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Preview(name = "Pro Pass Redeem Dialog - Enter Code (Light)", showBackground = true)
@Composable
private fun ProPassRedeemDialogEnterCodeLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        ProPassRedeemDialogCard(
            code = "PROPASS7",
            onCodeChange = {},
            state = RedemptionState.Idle,
            onRedeem = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Pro Pass Redeem Dialog - Enter Code (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProPassRedeemDialogEnterCodeDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        ProPassRedeemDialogCard(
            code = "PROPASS7",
            onCodeChange = {},
            state = RedemptionState.Idle,
            onRedeem = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Pro Pass Redeem Dialog - Success (Light)", showBackground = true)
@Composable
private fun ProPassRedeemDialogSuccessLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        ProPassRedeemDialogCard(
            code = "PROPASS7",
            onCodeChange = {},
            state = RedemptionState.Success(days = 7),
            onRedeem = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Pro Pass Redeem Dialog - Success (Dark)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProPassRedeemDialogSuccessDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        ProPassRedeemDialogCard(
            code = "PROPASS7",
            onCodeChange = {},
            state = RedemptionState.Success(days = 7),
            onRedeem = {},
            onDismiss = {}
        )
    }
}
