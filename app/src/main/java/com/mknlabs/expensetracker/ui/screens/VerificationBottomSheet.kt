package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.mknlabs.expensetracker.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * A reusable bottom sheet for email verification status.
 * Shows verification email sent message, check status, and resend buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationBottomSheet(
    email: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var isChecking by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var verificationStatus by remember { mutableStateOf<String?>(null) }
    var isVerified by remember { mutableStateOf(false) }

    // Timer for verification expiry (72 hours from when email was last sent)
    var remainingTimeMs by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        // Load verification expiry from Firestore
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            try {
                val snapshot = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(user.uid).get().await()
                val expiry = snapshot.getLong("verificationExpiry")
                if (expiry != null) {
                    while (true) {
                        val now = System.currentTimeMillis()
                        remainingTimeMs = (expiry - now).coerceAtLeast(0L)
                        if (remainingTimeMs!! <= 0L) break
                        delay(1000L)
                    }
                } else {
                    // No expiry stored, nothing to show
                    remainingTimeMs = null
                }
            } catch (_: Exception) {
                remainingTimeMs = null
            }
        } else {
            remainingTimeMs = null
        }
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
                text = stringResource(id = R.string.title_email_verification),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.label_verification_email_sent, email),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (remainingTimeMs != null && remainingTimeMs!! > 0L) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        id = R.string.label_verification_link_expires_in,
                        formatRemainingTime(remainingTimeMs!!)
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            } else if (remainingTimeMs != null && remainingTimeMs!! == 0L) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.label_verification_link_expired),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            // null = still loading from Firestore, show nothing

            Spacer(modifier = Modifier.height(24.dp))

            // Check Status Button
            Button(
                onClick = {
                    scope.launch {
                        isChecking = true
                        try {
                            val user = FirebaseAuth.getInstance().currentUser
                            user?.reload()?.await()
                            isVerified = user?.isEmailVerified == true
                            if (isVerified) {
                                verificationStatus = null
                                val toast = Toast.makeText(context, context.getString(R.string.msg_email_verified_success), Toast.LENGTH_SHORT)
                                toast.setGravity(android.view.Gravity.CENTER, 0, 0)
                                toast.show()
                                onDismiss()
                            } else {
                                verificationStatus = "email_not_verified"
                            }
                        } catch (e: Exception) {
                            verificationStatus = "check_status_failed"
                        }
                        isChecking = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isChecking
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = stringResource(id = R.string.btn_check_status))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resend Button
            OutlinedButton(
                onClick = {
                    scope.launch {
                        isResending = true
                        try {
                            FirebaseAuth.getInstance().currentUser?.sendEmailVerification()?.await()
                            verificationStatus = "email_resent"
                            // Reset timer
                            remainingTimeMs = 72L * 60 * 60 * 1000
                        } catch (e: Exception) {
                            verificationStatus = "resend_failed"
                        }
                        isResending = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isResending
            ) {
                if (isResending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = stringResource(id = R.string.btn_resend_email))
                }
            }

            // Status message
            verificationStatus?.let { statusKey ->
                Spacer(modifier = Modifier.height(16.dp))
                val statusText = when (statusKey) {
                    "email_not_verified" -> stringResource(id = R.string.msg_email_not_verified_yet)
                    "check_status_failed" -> stringResource(id = R.string.msg_failed_to_check_status)
                    "email_resent" -> stringResource(id = R.string.msg_verification_email_resent)
                    "resend_failed" -> stringResource(id = R.string.msg_failed_to_resend)
                    else -> statusKey
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isVerified) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }



            Spacer(modifier = Modifier.height(16.dp))

            // Cancel button (only show if not verified)
            if (!isVerified) {
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
    }
}

private fun formatRemainingTime(millis: Long): String {
    val seconds = millis / 1000
    val days = seconds / (24 * 3600)
    val hours = (seconds % (24 * 3600)) / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0 || days > 0) append("${hours}h ")
        if (minutes > 0 || hours > 0 || days > 0) append("${minutes}m ")
        append("${secs}s")
    }
}
