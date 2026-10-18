package com.mknlabs.expensetracker.feature.settings.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Feedback
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Envelope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardColors
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient

@Composable
fun FeedbackRoute(
    onBackClick: () -> Unit,
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // System back must return to the previous screen (About), not send the
    // app to the background â€” same convention as every other route.
    BackHandler {
        onBackClick()
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            // Can optionally navigate back after success or let the user dismiss
        }
    }

    FeedbackScreenContent(
        uiState = uiState,
        onFeedbackTextChanged = viewModel::onFeedbackTextChanged,
        onSubmitClick = viewModel::submitFeedback,
        onBackClick = onBackClick,
        onDismissSuccess = viewModel::clearSuccessMessage
    )
}

@Composable
private fun FeedbackScreenContent(
    uiState: FeedbackUiState,
    onFeedbackTextChanged: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onBackClick: () -> Unit,
    onDismissSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()

    if (uiState.isSuccess) {
        ModernConfirmationDialog(
            onDismissRequest = {
                onDismissSuccess()
                onBackClick()
            },
            title = stringResource(R.string.msg_feedback_success),
            text = "",
            confirmButtonText = stringResource(android.R.string.ok),
            onConfirm = {
                onDismissSuccess()
                onBackClick()
            },
            headerIcon = Icons.Rounded.CheckCircle,
            showDismissButton = false
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val enter = rememberSectionEnterAlphas(2)
        AppHeader(
            title = stringResource(R.string.title_feedback),
            onBackClick = onBackClick,
            modifier = Modifier.padding(
                start = Dimens.ScreenPadding,
                end = Dimens.ScreenPadding,
                top = Dimens.HeaderSpacing
            ).alpha(enter[0])
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.ScreenPadding)
                .alpha(enter[1]),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Info Card with User Context
            val scheme = MaterialTheme.colorScheme
            val dark = scheme.isDark
            val tellUsInk = if (dark) Color(0xFFF1F5F9) else scheme.onSurface
            val sendingInk = if (dark) Color(0xFF94A3B8) else scheme.onSurfaceVariant
            val emailInk = if (dark) Color(0xFFCBD5E1) else scheme.onSurface
            val fieldFill = if (dark) Color(0xFF0B0E17) else scheme.surface
            val fieldBorder = if (dark) Color(0xFF8B5CF6) else scheme.accentInk
            val placeholderInk = if (dark) Color(0xFF64748B) else scheme.onSurfaceVariant

            // Mockup sender card: obsidian-850/90 over slate-800/80, violet-washed icon.
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                shape = AppCardDefaults.shape(24.dp),
                colors = AppCardColors(
                    containerColor = if (dark) Color(0xE6101421) else scheme.surface,
                    contentColor = tellUsInk,
                    border = BorderStroke(
                        1.dp,
                        if (dark) Color(0xCC1E293B) else scheme.outline
                    )
                ),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Envelope,
                            contentDescription = null,
                            tint = if (dark) Color(0xFFA78BFA) else scheme.accentInk,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.label_send_feedback_desc),
                            color = tellUsInk,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = sendingInk)) {
                                append(stringResource(R.string.label_logged_in_as, ""))
                            }
                            withStyle(SpanStyle(color = emailInk)) {
                                append(uiState.userEmail.ifEmpty { "Anonymous" })
                            }
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Multiline feedback input field
            OutlinedTextField(
                value = uiState.feedbackText,
                onValueChange = onFeedbackTextChanged,
                placeholder = {
                    Text(
                        text = stringResource(R.string.placeholder_feedback_hint),
                        color = placeholderInk
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = fieldFill,
                    unfocusedContainerColor = fieldFill,
                    disabledContainerColor = fieldFill,
                    errorContainerColor = fieldFill,
                    focusedBorderColor = fieldBorder,
                    unfocusedBorderColor = fieldBorder.copy(alpha = 0.5f),
                    disabledBorderColor = fieldBorder.copy(alpha = 0.3f),
                    cursorColor = fieldBorder,
                    focusedTextColor = tellUsInk,
                    unfocusedTextColor = tellUsInk
                ),
                maxLines = 10,
                enabled = !uiState.isLoading && !uiState.isCooldownActive
            )

            if (uiState.errorMessageRes != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(uiState.errorMessageRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            if (uiState.isCooldownActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        R.string.msg_feedback_cooldown,
                        uiState.cooldownRemainingMinutes.toString()
                    ),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            val submitTextReady = uiState.feedbackText.isNotBlank() && !uiState.isCooldownActive
            val submitEnabled = submitTextReady && !uiState.isLoading
            Button(
                onClick = onSubmitClick,
                enabled = submitEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(28.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBrandGradient,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .then(
                            if (submitTextReady) Modifier.background(brandGradient())
                            else Modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onBrandGradient,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Feedback,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.btn_submit_feedback),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, name = "Feedback Screen - Dark Mode")
@Composable
private fun FeedbackScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        FeedbackScreenContent(
            uiState = FeedbackUiState(
                userEmail = "user@example.com",
                userId = "user_id_12345678"
            ),
            onFeedbackTextChanged = {},
            onSubmitClick = {},
            onBackClick = {},
            onDismissSuccess = {}
        )
    }
}
