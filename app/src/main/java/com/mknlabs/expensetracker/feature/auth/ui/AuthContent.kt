package com.mknlabs.expensetracker.feature.auth.ui



import com.mknlabs.expensetracker.core.ui.theme.disabled
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color



import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.Image

import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.ui.focus.onFocusChanged

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.CheckCircle

import androidx.compose.material.icons.filled.Email

import androidx.compose.material.icons.filled.Lock

import androidx.compose.material.icons.outlined.Circle

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.mknlabs.expensetracker.core.ui.components.AppOutlinedButton

import com.mknlabs.expensetracker.core.ui.components.AppTextButton

import kotlinx.coroutines.launch

import android.content.Intent

import android.provider.Settings

import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextDecoration

import androidx.compose.ui.Modifier

import com.mknlabs.expensetracker.core.ui.theme.accentInk

import com.mknlabs.expensetracker.core.ui.theme.accentSoft

import androidx.compose.ui.res.painterResource

import androidx.compose.ui.res.stringResource

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import com.mknlabs.expensetracker.R

import com.mknlabs.expensetracker.data.local.UserProfileDataStore

import com.mknlabs.expensetracker.models.CURRENT_TERMS_VERSION

import com.mknlabs.expensetracker.core.ui.components.input.InputFieldCard

import com.mknlabs.expensetracker.core.ui.components.input.InputType

import androidx.compose.ui.graphics.ColorFilter

import androidx.compose.foundation.relocation.BringIntoViewRequester

import androidx.compose.foundation.relocation.bringIntoViewRequester

import androidx.compose.foundation.ExperimentalFoundationApi

import com.mknlabs.expensetracker.core.ui.theme.SurfaceHighlight



@Composable

fun AuthRoute(

    viewModel: AuthViewModel,

    onAuthSuccess: () -> Unit,

    onGuestContinue: () -> Unit = onAuthSuccess,

    onSignUpSuccess: (() -> Unit)? = null,

    // Arm app-lock suppression before launching an external activity (e.g. the

    // Add-Account settings screen) so returning doesn't trigger the auto-lock.

    onPrepareForExternalActivity: () -> Unit = {}

) {

    val context = androidx.compose.ui.platform.LocalContext.current

    val authState by viewModel.authState.collectAsStateWithLifecycle()

    val verificationExpiry by viewModel.verificationExpiry.collectAsStateWithLifecycle()



    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current



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



    // Show an in-app dialog when no Google accounts are found, instead of

    // immediately jumping to the system "Add Account" screen.  The Credential

    // Manager can throw NoCredentialException due to stale state after a

    // sign-out; a retry often succeeds without leaving the app.

    var showNoGoogleAccountsDialog by remember { mutableStateOf(false) }



    LaunchedEffect(authState) {

        if (authState is AuthState.NoGoogleAccounts) {

            showNoGoogleAccountsDialog = true

            viewModel.resetState()

        }



        if (authState is AuthState.Success) {

            val isNewUser = (authState as AuthState.Success).isNewUser

            if (isNewUser) {

                (onSignUpSuccess ?: onAuthSuccess).invoke()

            } else {

                onAuthSuccess()

            }

            viewModel.resetState()

        }

    }



    val isEmailVerificationMode = authState is AuthState.EmailVerificationRequired

    val currentUserEmail = viewModel.currentUser.value?.email



    // Load verification expiry when the email verification UI is shown

    LaunchedEffect(isEmailVerificationMode) {

        if (isEmailVerificationMode) {

            viewModel.loadVerificationExpiry()

        }

    }



    // ── No-Google-Accounts dialog ──────────────────────────────────────

    if (showNoGoogleAccountsDialog) {

        AlertDialog(

            onDismissRequest = { showNoGoogleAccountsDialog = false },

            title = { Text(stringResource(id = R.string.title_google_accounts_not_found)) },

            text = { Text(stringResource(id = R.string.msg_google_accounts_not_found)) },

            confirmButton = {

                AppTextButton(onClick = {

                    showNoGoogleAccountsDialog = false

                    // Open device Settings → Add Google Account

                    viewModel.shouldAttemptAutoSignInAfterReturn = true

                    onPrepareForExternalActivity()

                    val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {

                        putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))

                    }

                    context.startActivity(intent)

                }) {

                    Text(stringResource(id = R.string.label_add_account))

                }

            },

            dismissButton = {

                AppTextButton(onClick = {

                    showNoGoogleAccountsDialog = false

                    // Retry sign-in — the Credential Manager may succeed on a

                    // second attempt after clearing its stale state.

                    viewModel.signInWithGoogle(context)

                }) {

                    Text(stringResource(id = R.string.label_retry))

                }

            }

        )

    }



    // ── Sign-in content ────────────────────────────────────────────────

    AuthContent(

        authState = authState,

        verificationExpiry = verificationExpiry,

        email = currentUserEmail ?: "",

        isEmailVerificationMode = isEmailVerificationMode,

        onCheckStatus = { viewModel.checkEmailVerificationStatus() },

        onResend = { viewModel.resendVerificationEmail() },

        onCancel = {

            viewModel.signOut()

            viewModel.resetState()

        },

        onGoogleSignIn = { viewModel.signInWithGoogle(context) },

        onEmailSignIn = { email, password -> viewModel.signInWithEmail(email, password) },

        onEmailSignUp = { email, password -> viewModel.signUpWithEmail(email, password) },

        onForgotPassword = { email -> viewModel.sendPasswordResetEmail(email) },

        onGuestContinue = {

            viewModel.startGuestSignIn()

            onGuestContinue()

        },

        onToggleSignUp = { viewModel.resetState() }

    )

}



@Composable

fun AuthContent(

    authState: AuthState,

    verificationExpiry: Long?,

    email: String,

    isEmailVerificationMode: Boolean,

    onCheckStatus: () -> Unit,

    onResend: () -> Unit,

    onCancel: () -> Unit,

    onGoogleSignIn: () -> Unit,

    onEmailSignIn: (String, String) -> Unit,

    onEmailSignUp: (String, String) -> Unit,

    onForgotPassword: (String) -> Unit,

    onGuestContinue: () -> Unit,

    onToggleSignUp: () -> Unit

) {

    if (isEmailVerificationMode) {

        EmailVerificationContent(

            authState = authState,

            verificationExpiry = verificationExpiry,

            email = email,

            onCheckStatus = onCheckStatus,

            onResend = onResend,

            onCancel = onCancel

        )

    } else {

        AuthContentBody(

            authState = authState,

                onGoogleSignIn = onGoogleSignIn,

            onEmailSignIn = onEmailSignIn,

            onEmailSignUp = onEmailSignUp,

            onForgotPassword = onForgotPassword,

            onGuestContinue = onGuestContinue,

            onToggleSignUp = onToggleSignUp

        )

    }

}



@Composable

private fun EmailVerificationContent(

    authState: AuthState,

    verificationExpiry: Long?,

    email: String,

    onCheckStatus: () -> Unit,

    onResend: () -> Unit,

    onCancel: () -> Unit

) {

    val isVerificationLoading = authState is AuthState.EmailVerificationRequired && (authState as AuthState.EmailVerificationRequired).isLoading



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

                color = MaterialTheme.colorScheme.accentInk,

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



        Box(

            modifier = Modifier

                .fillMaxWidth()

                .heightIn(min = 56.dp)

                .clip(RoundedCornerShape(16.dp))

                .background(brandGradient())

                .clickable(enabled = !isVerificationLoading, onClick = onCheckStatus),

            contentAlignment = Alignment.Center

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

                    style = MaterialTheme.typography.titleMedium,

                    color = Color.White

                )

            }

        }



        AppOutlinedButton(

            onClick = onResend,

            modifier = Modifier

                .fillMaxWidth()

                .heightIn(min = 56.dp),

            shape = RoundedCornerShape(16.dp),

            enabled = !isVerificationLoading

        ) {

            if (isVerificationLoading) {

                CircularProgressIndicator(

                    modifier = Modifier.size(24.dp),

                    color = MaterialTheme.colorScheme.accentInk,

                    strokeWidth = 2.dp

                )

            } else {

                Text(

                    text = stringResource(id = R.string.btn_resend_email),

                    style = MaterialTheme.typography.titleMedium

                )

            }

        }



        AppTextButton(

            onClick = onCancel,

            enabled = !isVerificationLoading

        ) {

            Text(

                text = stringResource(id = R.string.btn_cancel),

                style = MaterialTheme.typography.bodyLarge.copy(

                    fontWeight = FontWeight.Bold

                ),

                color = MaterialTheme.colorScheme.accentInk

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

