package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.ui.components.input.InputFieldCard
import com.mkn0079.expensetracker.ui.components.input.InputType
import com.mkn0079.expensetracker.ui.viewmodels.AuthState
import com.mkn0079.expensetracker.ui.viewmodels.AuthViewModel

import androidx.compose.ui.graphics.ColorFilter

@Composable
fun AuthContent(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val cooldownSeconds by viewModel.cooldownSeconds.collectAsState()
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isEmailValid = remember(email) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val passwordErrors = remember(password, isSignUp) {
        if (!isSignUp || password.isEmpty()) return@remember emptyList<String>()
        val errors = mutableListOf<String>()
        if (password.length < 8) errors.add("At least 8 characters")
        if (!password.any { it.isDigit() }) errors.add("At least 1 number")
        if (!password.any { !it.isLetterOrDigit() }) errors.add("At least 1 special character")
        errors
    }

    val isPasswordStrong = passwordErrors.isEmpty()
    val canSubmit = email.isNotEmpty() && isEmailValid && password.isNotEmpty() && (isPasswordStrong || !isSignUp)

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onAuthSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Google Sign In Button
        OutlinedButton(
            onClick = { viewModel.signInWithGoogle() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = authState !is AuthState.Loading,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White.copy(alpha = 0.05f)
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
                    if (authState is AuthState.Loading && !isSignUp && email.isNotEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onSecondary)
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (cooldownSeconds > 0) "Resend in ${cooldownSeconds}s" else stringResource(id = R.string.label_send_magic_link),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
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
            placeholder = "name@example.com",
            isError = email.isNotEmpty() && !isEmailValid,
            errorText = if (email.isNotEmpty() && !isEmailValid) "Please enter a valid email address" else null
        )

        Spacer(modifier = Modifier.height(16.dp))

        InputFieldCard(
            title = stringResource(id = R.string.label_password),
            value = password,
            onValueChange = { password = it },
            inputType = InputType.Password,
            leadingIcon = Icons.Filled.Lock,
            placeholder = "••••••••",
            isError = isSignUp && password.isNotEmpty() && !isPasswordStrong,
            errorText = if (isSignUp && passwordErrors.isNotEmpty()) "Required: ${passwordErrors.joinToString(", ")}" else null
        )

        if (!isSignUp) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(enabled = email.isNotEmpty() && isEmailValid) {
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
                    viewModel.signUpWithEmail(email, password)
                } else {
                    viewModel.signInWithEmail(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = canSubmit && authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading && isSignUp) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(
                    text = if (isSignUp) stringResource(id = R.string.label_create_account) else stringResource(id = R.string.label_login),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
            onClick = { viewModel.signInAnonymously() },
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
