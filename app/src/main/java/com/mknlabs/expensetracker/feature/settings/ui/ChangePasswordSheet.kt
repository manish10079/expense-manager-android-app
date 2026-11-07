package com.mknlabs.expensetracker.feature.settings.ui
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CameraSlash
import com.adamglin.phosphoricons.regular.EyeSlash
import com.adamglin.phosphoricons.regular.Fingerprint
import com.adamglin.phosphoricons.regular.GridFour
import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.Password
import com.adamglin.phosphoricons.regular.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.data.constants.DEFAULT_APP_LOCK_TIMEOUT_MINUTES
import com.mknlabs.expensetracker.data.constants.DEFAULT_BIOMETRIC_LOCK_ENABLED
import com.mknlabs.expensetracker.data.constants.DEFAULT_BLUR_IN_RECENTS_ENABLED
import com.mknlabs.expensetracker.data.constants.DEFAULT_SCRAMBLED_PIN_KEYPAD_ENABLED
import com.mknlabs.expensetracker.data.constants.DEFAULT_SCREENSHOT_PROTECTION_ENABLED
import com.mknlabs.expensetracker.models.SettingsItemType
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.AdPlacement
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.FeatureRegistry
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.AppCardColors
import com.mknlabs.expensetracker.core.ui.components.dismissSheetThen
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.GatedAction
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.core.ui.components.SettingsGroup
import com.mknlabs.expensetracker.core.ui.components.SettingsGroupDivider
import com.mknlabs.expensetracker.core.ui.components.settingsGroupCardColors
import com.mknlabs.expensetracker.core.ui.theme.SettingsTiles
import com.mknlabs.expensetracker.core.ui.theme.SettingsTilePalette
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.components.SettingsItemCard
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.mknlabs.expensetracker.core.ui.components.input.InputFieldCard
import com.mknlabs.expensetracker.core.ui.components.input.InputType
import com.mknlabs.expensetracker.feature.auth.ui.AuthViewModel
import com.mknlabs.expensetracker.feature.auth.ui.UpdatePasswordState
import androidx.compose.ui.tooling.preview.Preview
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme


@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun ChangePasswordSheet(
    onDismiss: () -> Unit,
    updatePasswordState: UpdatePasswordState,
    onUpdatePassword: (String, String) -> Unit,
    onResetUpdatePasswordState: () -> Unit
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmNewPassword by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current
    val currentPasswordFocusRequester = androidx.compose.ui.focus.FocusRequester()
    val scrollState = rememberScrollState()
    val isKeyboardVisible = WindowInsets.isImeVisible

    DisposableEffect(Unit) {
        onDispose {
            onResetUpdatePasswordState()
        }
    }

    LaunchedEffect(Unit) {
        currentPasswordFocusRequester.requestFocus()
    }

    LaunchedEffect(isKeyboardVisible, scrollState.maxValue) {
        if (isKeyboardVisible) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(updatePasswordState) {
        if (updatePasswordState is UpdatePasswordState.Success) {
            Toast.makeText(context, context.getString(R.string.success_password_updated), Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.sheet,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.title_change_password),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.label_change_password_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (updatePasswordState is UpdatePasswordState.Error) {
                val errorMsg = stringResource((updatePasswordState as UpdatePasswordState.Error).messageRes)
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            val isLoading = updatePasswordState is UpdatePasswordState.Loading

            InputFieldCard(
                title = stringResource(R.string.label_current_password),
                value = currentPassword,
                onValueChange = { currentPassword = it },
                inputType = InputType.Password,
                placeholder = stringResource(R.string.placeholder_current_password),
                isEnabled = !isLoading,
                focusRequester = currentPasswordFocusRequester
            )

            val isNewPasswordShort = newPassword.isNotEmpty() && newPassword.length < 6
            InputFieldCard(
                title = stringResource(R.string.label_new_password),
                value = newPassword,
                onValueChange = { newPassword = it },
                inputType = InputType.Password,
                placeholder = stringResource(R.string.placeholder_new_password),
                isEnabled = !isLoading,
                isError = isNewPasswordShort,
                errorText = if (isNewPasswordShort) stringResource(R.string.error_auth_weak_password) else null
            )

            val doPasswordsMismatch = confirmNewPassword.isNotEmpty() && newPassword != confirmNewPassword
            InputFieldCard(
                title = stringResource(R.string.label_confirm_new_password),
                value = confirmNewPassword,
                onValueChange = { confirmNewPassword = it },
                inputType = InputType.Password,
                placeholder = stringResource(R.string.placeholder_confirm_new_password),
                isEnabled = !isLoading,
                isError = doPasswordsMismatch,
                errorText = if (doPasswordsMismatch) stringResource(R.string.error_passwords_do_not_match) else null
            )

            val isFormValid = currentPassword.isNotEmpty() &&
                    newPassword.length >= 6 &&
                    newPassword == confirmNewPassword

            Button(
                onClick = {
                    if (isFormValid) {
                        onUpdatePassword(currentPassword, newPassword)
                    }
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.btn_change_password),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
