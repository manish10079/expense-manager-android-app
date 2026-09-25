package com.mknlabs.expensetracker.feature.settings.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
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
    // app to the background — same convention as every other route.
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
        AlertDialog(
            onDismissRequest = {
                onDismissSuccess()
                onBackClick()
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.msg_feedback_success),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismissSuccess()
                        onBackClick()
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AppHeader(
            title = stringResource(R.string.title_feedback),
            onBackClick = onBackClick,
            modifier = Modifier.padding(
                start = Dimens.ScreenPadding,
                end = Dimens.ScreenPadding,
                top = Dimens.HeaderSpacing,
                bottom = 12.dp
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Dimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Info Card with User Context
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                // The gradient is the dark surface and this card's only fill, so the
                // container beneath it stays transparent; light takes the white card.
                brush = darkOnlyGradient(standardCardGradient()),
                shape = AppCardDefaults.shape(24.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.label_send_feedback_desc),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(
                            R.string.label_logged_in_as,
                            uiState.userEmail.ifEmpty { "Anonymous" },
                            uiState.userId.take(8)
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(
                        RoundedCornerShape(
                            if (MaterialTheme.colorScheme.isDark) 24.dp else 16.dp
                        )
                    )
                    .background(MaterialTheme.colorScheme.surface),
                // Dark keeps the 24dp radius and the quarter-strength border it has always
                // drawn; light takes the field spec's 16dp and its full-strength outline.
                shape = RoundedCornerShape(
                    if (MaterialTheme.colorScheme.isDark) 24.dp else 16.dp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (MaterialTheme.colorScheme.isDark) {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
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
            Button(
                onClick = onSubmitClick,
                enabled = !uiState.isLoading && !uiState.isCooldownActive && uiState.feedbackText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
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
