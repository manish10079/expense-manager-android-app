package com.mknlabs.expensetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ui.viewmodels.UpdateEmailUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Bottom sheet for the complete Update Email flow.
 *
 * Follows the official Firebase recommended approach:
 * 1. Re-authenticate if session is stale (>5 min)
 * 2. Enter new email + confirm new email
 * 3. Call verifyBeforeUpdateEmail → verification link sent to NEW email
 * 4. Show pending verification banner with 60s countdown + resend
 * 5. On resume / "Check Status", reload() checks if Firebase updated the email
 *
 * @param currentEmail The user's current login email (shown as read-only)
 * @param uiState Current state from the ViewModel (UpdateEmailUiState)
 * @param onInitiateUpdate Called when user taps "Send Verification Link" — triggers ViewModel flow
 * @param onCheckStatus Called when user taps "Check Status" or on lifecycle resume
 * @param onResend Called when user taps "Resend Verification Link" after 60s cooldown
 * @param onDismiss Called to close the bottom sheet
 * @param onReset Called to reset the ViewModel state back to Idle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateEmailBottomSheet(
    currentEmail: String,
    uiState: UpdateEmailUiState,
    onInitiateUpdate: (newEmail: String, currentPassword: String) -> Unit,
    onCheckStatus: () -> Unit,
    onResend: (newEmail: String) -> Unit,
    onDismiss: () -> Unit,
    onReset: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // ── Form State ──
    var newEmail by rememberSaveable { mutableStateOf("") }
    var confirmEmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // ── Derived Validation ──
    val isValidNewEmail = remember(newEmail) {
        newEmail.isNotBlank() &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches() &&
            newEmail != currentEmail
    }
    val emailsMatch = remember(newEmail, confirmEmail) {
        confirmEmail.isNotBlank() && newEmail == confirmEmail
    }
    val isFormValid = remember(isValidNewEmail, emailsMatch) {
        isValidNewEmail && emailsMatch
    }

    // ── Error State (cleared when inputs change) ──
    var localError by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(newEmail, confirmEmail, password) {
        localError = null
    }

    // ── Error display from ViewModel ──
    val viewModelError = when (uiState) {
        is UpdateEmailUiState.Error -> stringResource(id = uiState.messageRes)
        else -> null
    }
    val displayError = viewModelError ?: localError

    // ── Clear ViewModel error when inputs change ──
    LaunchedEffect(newEmail, confirmEmail, password) {
        if (uiState is UpdateEmailUiState.Error) {
            onReset()
        }
    }

    // ── Dismiss on Success ──
    LaunchedEffect(uiState) {
        if (uiState is UpdateEmailUiState.Success) {
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            onReset()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                // ────────────────────────────────────────────────────────
                // PHASE 1: Form — Re-auth + New Email Input
                // ────────────────────────────────────────────────────────
                is UpdateEmailUiState.Idle,
                is UpdateEmailUiState.ReAuthenticating,
                is UpdateEmailUiState.Loading,
                is UpdateEmailUiState.Error -> {
                    SheetHeader(
                        title = stringResource(id = R.string.title_update_email),
                        subtitle = stringResource(id = R.string.desc_update_email)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Current email (read-only)
                    OutlinedTextField(
                        value = currentEmail,
                        onValueChange = {},
                        label = { Text(stringResource(id = R.string.label_current_email)) },
                        readOnly = true,
                        leadingIcon = {
                            Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // New email input
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it.trim() },
                        label = { Text(stringResource(id = R.string.label_new_email)) },
                        placeholder = { Text(stringResource(id = R.string.placeholder_enter_new_email)) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = displayError != null && newEmail.isNotBlank(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm new email
                    OutlinedTextField(
                        value = confirmEmail,
                        onValueChange = { confirmEmail = it.trim() },
                        label = { Text(stringResource(id = R.string.label_confirm_new_email)) },
                        placeholder = { Text(stringResource(id = R.string.placeholder_confirm_new_email)) },
                        leadingIcon = {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = confirmEmail.isNotBlank() && !emailsMatch,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = if (confirmEmail.isNotBlank() && !emailsMatch) {
                            { Text(stringResource(id = R.string.error_emails_do_not_match), color = MaterialTheme.colorScheme.error) }
                        } else null
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Current password (re-auth)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(id = R.string.label_current_password)) },
                        placeholder = { Text(stringResource(id = R.string.placeholder_reenter_password_for_security)) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = stringResource(
                                        if (passwordVisible) R.string.desc_hide_password else R.string.desc_show_password
                                    ),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Error message
                    displayError?.let { error ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Update / Send Verification Link button
                    val isLoading = uiState is UpdateEmailUiState.Loading || uiState is UpdateEmailUiState.ReAuthenticating
                    Button(
                        onClick = {
                            if (isFormValid && password.isNotBlank()) {
                                onInitiateUpdate(newEmail, password)
                            } else if (!isFormValid) {
                                localError = context.getString(R.string.msg_fill_all_fields_correctly)
                            } else if (password.isBlank()) {
                                localError = context.getString(R.string.msg_enter_current_password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        enabled = isFormValid && password.isNotBlank() && !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = if (isLoading) {
                                androidx.compose.ui.graphics.Color.Transparent
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            },
                            disabledContentColor = if (isLoading) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = stringResource(id = R.string.label_send_verification_to_new_email),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Cancel
                    TextButton(
                        onClick = {
                            onReset()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.btn_cancel),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ────────────────────────────────────────────────────────
                // PHASE 2: Pending Verification Banner
                // ────────────────────────────────────────────────────────
                is UpdateEmailUiState.PendingVerification -> {
                    PendingVerificationContent(
                        newEmail = uiState.newEmail,
                        currentEmail = uiState.currentEmail,
                        onCheckStatus = onCheckStatus,
                        onResend = { onResend(uiState.newEmail) },
                        onDismiss = {
                            onReset()
                            onDismiss()
                        }
                    )
                }

                // ────────────────────────────────────────────────────────
                // Checking Verification (after resume)
                // ────────────────────────────────────────────────────────
                is UpdateEmailUiState.CheckingVerification -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        CircularProgressIndicator(modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.msg_checking_verification_status),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                // ────────────────────────────────────────────────────────
                // Success (transitional — sheet will auto-dismiss)
                // ────────────────────────────────────────────────────────
                is UpdateEmailUiState.Success -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.msg_email_updated_success),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.msg_email_updated_success_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// Pending Verification Content — self-contained with 60s countdown
// ──────────────────────────────────────────────────────────────────────────
@Composable
private fun PendingVerificationContent(
    newEmail: String,
    currentEmail: String,
    onCheckStatus: () -> Unit,
    onResend: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var countdownSeconds by rememberSaveable { mutableStateOf(60) }
    var isResending by remember { mutableStateOf(false) }
    var resendSuccess by remember { mutableStateOf(false) }
    var resendError by remember { mutableStateOf<String?>(null) }

    // Start 60s countdown
    LaunchedEffect(Unit) {
        while (countdownSeconds > 0) {
            delay(1000L)
            countdownSeconds--
        }
    }

    // Reset countdown on resend
    val handleResend: () -> Unit = {
        isResending = true
        resendSuccess = false
        resendError = null
        onResend()
        scope.launch {
            delay(1500L) // Brief delay for UX feedback
            isResending = false
            resendSuccess = true
            countdownSeconds = 60
            delay(3000L)
            resendSuccess = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = stringResource(id = R.string.title_pending_email_verification),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pending verification card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Email,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(id = R.string.msg_verification_sent_to_new_email, newEmail),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.msg_current_email_unchanged, currentEmail),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Check Status button
        val isChecking = false // Managed by ViewModel
        Button(
            onClick = onCheckStatus,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            enabled = !isChecking,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.btn_check_status),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Resend with countdown
        OutlinedButton(
            onClick = handleResend,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            enabled = countdownSeconds <= 0 && !isResending,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isResending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (countdownSeconds > 0) {
                        stringResource(id = R.string.btn_resend_in_seconds, countdownSeconds)
                    } else {
                        stringResource(id = R.string.btn_resend_email)
                    }
                )
            }
        }

        // Success / Error feedback
        AnimatedVisibility(visible = resendSuccess, enter = fadeIn(), exit = fadeOut()) {
            Text(
                text = stringResource(id = R.string.msg_verification_email_resent),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        resendError?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cancel
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.btn_cancel),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// Shared Sheet Header
// ──────────────────────────────────────────────────────────────────────────
@Composable
private fun SheetHeader(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
