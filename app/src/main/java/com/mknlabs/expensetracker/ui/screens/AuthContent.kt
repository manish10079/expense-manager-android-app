package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.ui.components.input.InputFieldCard
import com.mknlabs.expensetracker.ui.components.input.InputType
import com.mknlabs.expensetracker.ui.viewmodels.AuthState
import com.mknlabs.expensetracker.ui.viewmodels.AuthViewModel
import androidx.compose.ui.graphics.ColorFilter
import com.mknlabs.expensetracker.ui.theme.SurfaceHighlight

private enum class EmailAuthAction {
    Login,
    SignUp
}

@Composable
fun AuthContent(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    onGuestContinue: () -> Unit = onAuthSuccess,
    onSignUpSuccess: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val cooldownSeconds by viewModel.cooldownSeconds.collectAsState()
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pendingEmailAuthAction by remember { mutableStateOf<EmailAuthAction?>(null) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val isEmailValid = remember(email) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val passwordErrors = remember(password, isSignUp) {
        if (!isSignUp || password.isEmpty()) return@remember emptyList<Int>()
        val errors = mutableListOf<Int>()
        if (password.length < 8) errors.add(R.string.error_password_min_chars)
        if (!password.any { it.isDigit() }) errors.add(R.string.error_password_need_digit)
        if (!password.any { !it.isLetterOrDigit() }) errors.add(R.string.error_password_need_special)
        errors
    }

    val isPasswordStrong = passwordErrors.isEmpty()
    val canSubmit = email.isNotEmpty() && isEmailValid && password.isNotEmpty() && (isPasswordStrong || !isSignUp)

    // Handle returning from settings (Add Account)
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (viewModel.shouldAttemptAutoSignInAfterReturn) {
                    viewModel.shouldAttemptAutoSignInAfterReturn = false
                    viewModel.attemptAutoSignInAfterReturn(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    LaunchedEffect(authState) {
        if (authState is AuthState.NoGoogleAccounts) {
            viewModel.shouldAttemptAutoSignInAfterReturn = true
            val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            }
            context.startActivity(intent)
            viewModel.resetState()
        }

        if (authState is AuthState.Success) {
            val isNewUser = (authState as AuthState.Success).isNewUser
            pendingEmailAuthAction = null
            if (isNewUser) {
                (onSignUpSuccess ?: onAuthSuccess).invoke()
            } else {
                onAuthSuccess()
            }
            viewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (authState is AuthState.EmailVerificationRequired) {
            val displayEmail = remember { viewModel.currentUser.value?.email ?: email }
            EmailVerificationContent(
                viewModel = viewModel,
                email = displayEmail
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Google Sign In Button
                OutlinedButton(
                    onClick = { viewModel.signInWithGoogle(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = authState !is AuthState.Loading,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SurfaceHighlight
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (authState is AuthState.Loading && !isSignUp && email.isEmpty()) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = stringResource(id = R.string.label_continue_with),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Image(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google",
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Text(
                        text = stringResource(id = R.string.label_or),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                InputFieldCard(
                    title = stringResource(id = R.string.label_email),
                    value = email,
                    onValueChange = { email = it },
                    inputType = InputType.Text,
                    leadingIcon = Icons.Filled.Email,
                    placeholder = stringResource(id = R.string.placeholder_email),
                    isError = email.isNotEmpty() && !isEmailValid,
                    errorText = if (email.isNotEmpty() && !isEmailValid) stringResource(id = R.string.error_invalid_email) else null
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputFieldCard(
                    title = stringResource(id = R.string.label_password),
                    value = password,
                    onValueChange = { password = it },
                    inputType = InputType.Password,
                    leadingIcon = Icons.Filled.Lock,
                    placeholder = stringResource(id = R.string.placeholder_password),
                    isError = isSignUp && password.isNotEmpty() && !isPasswordStrong,
                    errorText = if (isSignUp && passwordErrors.isNotEmpty()) {
                        stringResource(id = R.string.error_password_required_prefix) + 
                        passwordErrors.joinToString(", ") { context.getString(it) }
                    } else null
                )

                if (!isSignUp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = stringResource(id = R.string.label_forgot_password),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                viewModel.sendPasswordResetEmail(email)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (authState is AuthState.ResetEmailSent) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.msg_auth_reset_email_sent),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (authState is AuthState.MagicLinkSent) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.msg_auth_magic_link_sent),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (authState is AuthState.Error) {
                    val error = authState as AuthState.Error

                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = stringResource(id = error.messageRes),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isSignUp) {
                            pendingEmailAuthAction = EmailAuthAction.SignUp
                            viewModel.signUpWithEmail(email, password)
                        } else {
                            pendingEmailAuthAction = EmailAuthAction.Login
                            viewModel.signInWithEmail(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = canSubmit && authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isSignUp) stringResource(id = R.string.label_create_account) else stringResource(id = R.string.label_login),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Magic Link Button (Passwordless)
                if (!isSignUp) {
                    Button(
                        onClick = {
                            if (cooldownSeconds == 0) {
                                viewModel.sendMagicLink(email)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = email.isNotEmpty() && isEmailValid && authState !is AuthState.Loading && cooldownSeconds == 0,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (authState is AuthState.Loading && !isSignUp && email.isEmpty()) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onSecondary)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (cooldownSeconds > 0) stringResource(id = R.string.label_resend_cooldown, cooldownSeconds) else stringResource(id = R.string.label_send_magic_link),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }

                TextButton(onClick = { 
                    isSignUp = !isSignUp 
                    viewModel.resetState()
                }) {
                    Text(
                        text = if (isSignUp) stringResource(id = R.string.label_already_have_account) else stringResource(id = R.string.label_no_account_signup),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        viewModel.startGuestSignIn()
                        onGuestContinue()
                    },
                    enabled = authState !is AuthState.Loading
                ) {
                    Text(
                        text = stringResource(id = R.string.label_continue_as_guest),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmailVerificationContent(
    viewModel: AuthViewModel,
    email: String
) {
    val authState by viewModel.authState.collectAsState()
    val verificationExpiry by viewModel.verificationExpiry.collectAsState()
    val isVerificationLoading = authState is AuthState.EmailVerificationRequired && (authState as AuthState.EmailVerificationRequired).isLoading

    LaunchedEffect(Unit) {
        viewModel.loadVerificationExpiry()
    }

    var remainingTimeMs by remember { mutableStateOf(0L) }

    LaunchedEffect(verificationExpiry) {
        val expiry = verificationExpiry ?: 0L
        while (true) {
            val now = System.currentTimeMillis()
            remainingTimeMs = (expiry - now).coerceAtLeast(0L)
            if (remainingTimeMs <= 0L) break
            kotlinx.coroutines.delay(1000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.title_email_verification),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = stringResource(id = R.string.label_verification_email_sent, email),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (remainingTimeMs > 0L) {
            Text(
                text = stringResource(id = R.string.label_verification_link_expires_in, formatRemainingTime(remainingTimeMs)),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else if (verificationExpiry != null && verificationExpiry != 0L) {
            Text(
                text = stringResource(id = R.string.label_verification_link_expired),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (authState is AuthState.EmailVerificationRequired) {
            val state = authState as AuthState.EmailVerificationRequired
            if (state.errorRes != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(id = state.errorRes),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else if (state.isResendSuccess) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.info_verification_email_sent_again),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.checkEmailVerificationStatus() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isVerificationLoading
        ) {
            if (isVerificationLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(id = R.string.btn_check_status),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        OutlinedButton(
            onClick = { viewModel.resendVerificationEmail() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isVerificationLoading
        ) {
            if (isVerificationLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(id = R.string.btn_resend_email),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        TextButton(
            onClick = {
                viewModel.signOut()
                viewModel.resetState()
            },
            enabled = !isVerificationLoading
        ) {
            Text(
                text = stringResource(id = R.string.btn_cancel),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
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
