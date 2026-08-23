package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.mknlabs.expensetracker.R
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Bottom sheet for updating the user's email address.
 * Uses verifyBeforeUpdateEmail — the new email is only updated after verification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateEmailBottomSheet(
    currentEmail: String,
    onDismiss: () -> Unit,
    onEmailUpdateInitiated: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var newEmail by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPasswordPrompt by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }

    val isValidEmail = remember(newEmail) {
        newEmail.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches() &&
                newEmail != currentEmail
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.title_update_email),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.desc_update_email),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Current email (read-only)
            OutlinedTextField(
                value = currentEmail,
                onValueChange = {},
                label = { Text(stringResource(id = R.string.label_current_email)) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // New email input
            OutlinedTextField(
                value = newEmail,
                onValueChange = { newEmail = it.trim() },
                label = { Text(stringResource(id = R.string.label_new_email)) },
                placeholder = { Text(stringResource(id = R.string.placeholder_enter_new_email)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )

            // Error message
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Update button
            Button(
                onClick = {
                    scope.launch {
                        isUpdating = true
                        errorMessage = null
                        try {
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                // Check if user signed in recently (within 5 minutes)
                                // Firebase requires recent sign-in for email update
                                val tokenResult = user.getIdToken(false).await()
                                val authTime = tokenResult.claims["auth_time"] as? Long ?: 0L
                                val now = System.currentTimeMillis() / 1000
                                val fiveMinutesAgo = now - 300

                                if (authTime < fiveMinutesAgo) {
                                    // Need re-authentication — prompt for password
                                    showPasswordPrompt = true
                                    isUpdating = false
                                    return@launch
                                }

                                // User signed in recently — proceed with verifyBeforeUpdateEmail
                                user.verifyBeforeUpdateEmail(newEmail).await()
                                onEmailUpdateInitiated()
                                onDismiss()
                            }
                        } catch (e: Exception) {
                            val msg = e.message ?: ""
                            errorMessage = when {
                                msg.contains("recent login") || msg.contains("REQUIRES_RECENT_LOGIN") ->
                                    context.getString(R.string.msg_reauth_required)
                                msg.contains("email-already-in-use") || msg.contains("EMAIL_ALREADY_IN_USE") ->
                                    context.getString(R.string.msg_email_already_in_use)
                                msg.contains("invalid-email") || msg.contains("INVALID_EMAIL") ->
                                    context.getString(R.string.error_invalid_email)
                                else -> context.getString(R.string.msg_failed_to_update_email)
                            }
                        }
                        isUpdating = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValidEmail && !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = stringResource(id = R.string.label_send_verification))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.btn_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Re-authentication prompt
            if (showPasswordPrompt) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.msg_reauth_required),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(id = R.string.label_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isUpdating = true
                            errorMessage = null
                            try {
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user != null && user.email != null) {
                                    // Re-authenticate with password
                                    val credential = EmailAuthProvider.getCredential(user.email!!, password)
                                    user.reauthenticate(credential).await()

                                    // Now proceed with verifyBeforeUpdateEmail
                                    user.verifyBeforeUpdateEmail(newEmail).await()
                                    onEmailUpdateInitiated()
                                    onDismiss()
                                }
                            } catch (e: Exception) {
                                val msg = e.message ?: ""
                                errorMessage = when {
                                    msg.contains("wrong-password") || msg.contains("INVALID_CREDENTIAL") ||
                                    msg.contains("invalid-credential") ->
                                        context.getString(R.string.msg_incorrect_password)
                                    msg.contains("email-already-in-use") || msg.contains("EMAIL_ALREADY_IN_USE") ->
                                        context.getString(R.string.msg_email_already_in_use)
                                    else -> context.getString(R.string.msg_auth_failed)
                                }
                            }
                            isUpdating = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = password.isNotEmpty() && !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = stringResource(id = R.string.label_verify_and_update))
                    }
                }

                TextButton(
                    onClick = { showPasswordPrompt = false; password = "" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
