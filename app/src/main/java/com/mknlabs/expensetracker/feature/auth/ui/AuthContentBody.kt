package com.mknlabs.expensetracker.feature.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.models.CURRENT_TERMS_VERSION
import kotlinx.coroutines.launch

@Composable
internal fun AuthContentBody(
    authState: AuthState,
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (String, String) -> Unit,
    onEmailSignUp: (String, String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onGuestContinue: () -> Unit,
    onToggleSignUp: () -> Unit,
) {
    val context = LocalContext.current
    val dark = MaterialTheme.colorScheme.isDark
    var isSignUp by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var isTermsAccepted by remember { mutableStateOf(true) }
    var showTermsRequiredDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val linkColor = if (dark) AuthLinkDark else AuthFocusGlow
    val muted = if (dark) AuthMutedDark else AuthMutedLight
    val fieldShape = RoundedCornerShape(16.dp)

    val checkConsentAndRun: (() -> Unit) -> Unit = { action ->
        if (!isTermsAccepted) showTermsRequiredDialog = true else action()
    }

    if (showTermsRequiredDialog) {
        ModernConfirmationDialog(
            onDismissRequest = { showTermsRequiredDialog = false },
            title = stringResource(R.string.title_terms_required),
            text = stringResource(R.string.msg_agree_terms_required),
            confirmButtonText = stringResource(R.string.label_ok),
            onConfirm = { showTermsRequiredDialog = false },
            headerIcon = Icons.Filled.Info,
            showDismissButton = false
        )
    }

    val isEmailValid = remember(email) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    val passwordErrors = remember(password, isSignUp) {
        if (!isSignUp || password.isEmpty()) return@remember emptyList<Int>()
        buildList {
            if (password.length < 8) add(R.string.error_password_min_chars)
            if (!password.any { it.isDigit() }) add(R.string.error_password_need_digit)
            if (!password.any { !it.isLetterOrDigit() }) add(R.string.error_password_need_special)
        }
    }
    val canSubmit = email.isNotEmpty() && isEmailValid && password.isNotEmpty() &&
        (passwordErrors.isEmpty() || !isSignUp)
    val loadingType = (authState as? AuthState.Loading)?.type
    val loading = loadingType != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = if (isSignUp) stringResource(R.string.auth_welcome) else stringResource(R.string.label_welcome_back),
            color = if (dark) Color.White else AuthTitleLight,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.4).sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(
                if (isSignUp) R.string.auth_signup_subtitle else R.string.auth_signin_subtitle,
            ),
            color = muted,
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(24.dp))

        val googleLoading = loadingType == AuthLoadingType.GOOGLE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(fieldShape)
                .background(if (dark) AuthGoogleFillDark else AuthGoogleFillLight)
                .border(
                    BorderStroke(1.dp, if (dark) AuthGoogleBorderDark else AuthGoogleBorderLight),
                    fieldShape,
                )
                .clickable(enabled = !loading) { checkConsentAndRun(onGoogleSignIn) }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (googleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = if (dark) Color.White else AuthTitleLight,
                        strokeWidth = 2.5.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.auth_connecting_google),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (dark) Color(0xFFF3F4F6) else Color(0xFF1E293B),
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_google_logo),
                        contentDescription = stringResource(R.string.content_desc_google_logo),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.label_continue_with_google),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (dark) Color(0xFFF3F4F6) else Color(0xFF1E293B),
                    )
                }
            }
            if (!googleLoading) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(brandGradient())
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.label_recommended),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f), color = if (dark) AuthDividerDark else AuthDividerLight)
            Text(
                text = stringResource(R.string.label_or),
                modifier = Modifier.padding(horizontal = 12.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.6.sp,
                color = if (dark) Color(0xFF6B7280) else Color(0xFF9CA3AF),
            )
            HorizontalDivider(Modifier.weight(1f), color = if (dark) AuthDividerDark else AuthDividerLight)
        }
        Spacer(Modifier.height(16.dp))

        AuthLabeledField(
            label = stringResource(R.string.label_email),
            value = email,
            onValueChange = { email = it },
            placeholder = stringResource(R.string.placeholder_email),
            leadingIcon = Icons.Filled.Email,
            dark = dark,
            isError = email.isNotEmpty() && !isEmailValid,
        )
        if (email.isNotEmpty() && !isEmailValid) {
            Text(
                text = stringResource(R.string.error_invalid_email),
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.label_password).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = muted,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.auth_forgot),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = linkColor,
                modifier = Modifier.clickable { onForgotPassword(email) },
            )
        }
        Spacer(Modifier.height(6.dp))
        AuthLabeledField(
            label = null,
            value = password,
            onValueChange = { password = it },
            placeholder = "••••••••",
            leadingIcon = Icons.Filled.Lock,
            dark = dark,
            isPassword = true,
            isError = isSignUp && password.isNotEmpty() && passwordErrors.isNotEmpty(),
        )
        if (isSignUp && passwordErrors.isNotEmpty()) {
            Text(
                text = stringResource(R.string.error_password_required_prefix) +
                    passwordErrors.joinToString(", ") { context.getString(it) },
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (authState is AuthState.ResetEmailSent) {
            Text(
                text = stringResource(R.string.msg_auth_reset_email_sent),
                color = MaterialTheme.colorScheme.accentInk,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        if (authState is AuthState.Error) {
            Text(
                text = stringResource(authState.messageRes),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val newChecked = !isTermsAccepted
                    isTermsAccepted = newChecked
                    if (newChecked) {
                        val consentTimestamp = System.currentTimeMillis()
                        coroutineScope.launch {
                            UserProfileDataStore.updateUserProfile(context) { profile ->
                                profile.copy(
                                    termsAcceptedAt = consentTimestamp,
                                    termsVersion = CURRENT_TERMS_VERSION,
                                )
                            }
                        }
                    }
                },
            verticalAlignment = Alignment.Top,
        ) {
            val checked = isTermsAccepted
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        if (checked) AuthFocusGlow else if (dark) AuthInputFillDark else Color.White,
                    )
                    .border(
                        2.dp,
                        if (checked) AuthFocusGlow else if (dark) Color(0xFF4B5563) else Color(0xFFCBD5E1),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (checked) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            val termsUrl = stringResource(R.string.url_terms_conditions)
            val privacyUrl = stringResource(R.string.url_privacy_policy)
            val annotatedString = buildAnnotatedString {
                append("I have read and agree to the ")
                pushStringAnnotation("TERMS", termsUrl)
                withStyle(SpanStyle(color = linkColor, fontWeight = FontWeight.SemiBold)) {
                    append("Terms & Conditions")
                }
                pop()
                append(" and ")
                pushStringAnnotation("PRIVACY", privacyUrl)
                withStyle(SpanStyle(color = linkColor, fontWeight = FontWeight.SemiBold)) {
                    append("Privacy Policy")
                }
                pop()
                append(".")
            }
            ClickableText(
                text = annotatedString,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = muted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                ),
                onClick = { offset ->
                    annotatedString.getStringAnnotations("TERMS", offset, offset).firstOrNull()?.let {
                        try { uriHandler.openUri(it.item) } catch (_: Exception) {}
                    }
                    annotatedString.getStringAnnotations("PRIVACY", offset, offset).firstOrNull()?.let {
                        try { uriHandler.openUri(it.item) } catch (_: Exception) {}
                    }
                },
            )
        }

        Spacer(Modifier.height(24.dp))
        val emailLoading = loadingType == AuthLoadingType.EMAIL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, fieldShape, ambientColor = AuthFocusGlow.copy(alpha = 0.35f), spotColor = AuthFocusGlow.copy(alpha = 0.35f))
                .clip(fieldShape)
                .background(brandGradient())
                .clickable(enabled = canSubmit && !loading) {
                    if (isSignUp) checkConsentAndRun { onEmailSignUp(email, password) }
                    else onEmailSignIn(email, password)
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (emailLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(18.dp), Color.White, 2.5.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.auth_authenticating), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = if (isSignUp) stringResource(R.string.label_create_account) else stringResource(R.string.label_login),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = if (isSignUp) {
                    stringResource(R.string.auth_already_have_account)
                } else {
                    stringResource(R.string.auth_dont_have_account)
                },
                fontSize = 12.sp,
                color = muted,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (isSignUp) stringResource(R.string.label_login) else stringResource(R.string.label_create_account),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = linkColor,
                modifier = Modifier.clickable {
                    isSignUp = !isSignUp
                    onToggleSignUp()
                },
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.label_continue_as_guest),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = muted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !loading) { checkConsentAndRun(onGuestContinue) }
                .padding(vertical = 8.dp),
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun AuthLabeledField(
    label: String?,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    dark: Boolean,
    isPassword: Boolean = false,
    isError: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    if (label != null) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = if (dark) AuthMutedDark else AuthMutedLight,
            modifier = Modifier.padding(bottom = 6.dp),
        )
    }
    val border = when {
        isError -> MaterialTheme.colorScheme.error
        focused -> AuthFocusGlow
        dark -> AuthInputBorderDark
        else -> AuthInputBorderLight
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (dark) AuthInputFillDark else AuthInputFillLight)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = if (dark) Color(0xFF6B7280) else Color(0xFF9CA3AF),
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            cursorBrush = SolidColor(AuthFocusGlow),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = if (dark) Color.White else Color(0xFF0F172A),
                fontSize = 14.sp,
            ),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focused = it.isFocused },
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = if (dark) AuthPlaceholderDark else AuthPlaceholderLight,
                            fontSize = 14.sp,
                        )
                    }
                    inner()
                }
            },
        )
        if (isPassword) {
            Icon(
                imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                contentDescription = stringResource(
                    if (passwordVisible) R.string.desc_hide_password else R.string.desc_show_password,
                ),
                tint = if (dark) Color(0xFF6B7280) else Color(0xFF9CA3AF),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { passwordVisible = !passwordVisible },
            )
        }
    }
}
