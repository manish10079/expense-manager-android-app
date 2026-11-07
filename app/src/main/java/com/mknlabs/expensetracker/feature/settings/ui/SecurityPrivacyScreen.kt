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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityPrivacyScreen(
    isAppLockEnabled: Boolean = false,
    hasAppLockPin: Boolean = false,
    isBiometricEnabled: Boolean = DEFAULT_BIOMETRIC_LOCK_ENABLED,
    isScrambledPinKeypadEnabled: Boolean = DEFAULT_SCRAMBLED_PIN_KEYPAD_ENABLED,
    isBlurInRecentsEnabled: Boolean = DEFAULT_BLUR_IN_RECENTS_ENABLED,
    isScreenshotProtectionEnabled: Boolean = DEFAULT_SCREENSHOT_PROTECTION_ENABLED,
    autoLockDurationMinutes: Int = DEFAULT_APP_LOCK_TIMEOUT_MINUTES,
    onAppLockChange: (Boolean) -> Unit = {},
    onBiometricChange: (Boolean) -> Unit = {},
    onScrambledPinKeypadChange: (Boolean) -> Unit = {},
    onBlurInRecentsChange: (Boolean) -> Unit = {},
    onScreenshotProtectionChange: (Boolean) -> Unit = {},
    onAutoLockDurationChange: (Int) -> Unit = {},
    onBackClick: () -> Unit = {},
    isAdsEnabled: Boolean = false
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isEmailPasswordUser = remember(currentUser) {
        currentUser?.providerData?.any { it.providerId == com.google.firebase.auth.EmailAuthProvider.PROVIDER_ID } == true
    }
    val updatePasswordState by authViewModel.updatePasswordState.collectAsStateWithLifecycle()

    SecurityPrivacyContent(
        isAppLockEnabled = isAppLockEnabled,
        hasAppLockPin = hasAppLockPin,
        isBiometricEnabled = isBiometricEnabled,
        isScrambledPinKeypadEnabled = isScrambledPinKeypadEnabled,
        isBlurInRecentsEnabled = isBlurInRecentsEnabled,
        isScreenshotProtectionEnabled = isScreenshotProtectionEnabled,
        autoLockDurationMinutes = autoLockDurationMinutes,
        isAdsEnabled = isAdsEnabled,
        isEmailPasswordUser = isEmailPasswordUser,
        updatePasswordState = updatePasswordState,
        onAppLockChange = onAppLockChange,
        onBiometricChange = onBiometricChange,
        onScrambledPinKeypadChange = onScrambledPinKeypadChange,
        onBlurInRecentsChange = onBlurInRecentsChange,
        onScreenshotProtectionChange = onScreenshotProtectionChange,
        onAutoLockDurationChange = onAutoLockDurationChange,
        onBackClick = onBackClick,
        onUpdatePassword = { current, new -> authViewModel.updatePassword(current, new) },
        onResetUpdatePasswordState = { authViewModel.resetUpdatePasswordState() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SecurityPrivacyContent(
    isAppLockEnabled: Boolean,
    hasAppLockPin: Boolean,
    isBiometricEnabled: Boolean,
    isScrambledPinKeypadEnabled: Boolean,
    isBlurInRecentsEnabled: Boolean,
    isScreenshotProtectionEnabled: Boolean,
    autoLockDurationMinutes: Int,
    isAdsEnabled: Boolean,
    isEmailPasswordUser: Boolean,
    updatePasswordState: UpdatePasswordState,
    onAppLockChange: (Boolean) -> Unit,
    onBiometricChange: (Boolean) -> Unit,
    onScrambledPinKeypadChange: (Boolean) -> Unit,
    onBlurInRecentsChange: (Boolean) -> Unit,
    onScreenshotProtectionChange: (Boolean) -> Unit,
    onAutoLockDurationChange: (Int) -> Unit,
    onBackClick: () -> Unit,
    onUpdatePassword: (String, String) -> Unit,
    onResetUpdatePasswordState: () -> Unit
) {
    var isAutoLockDurationPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isChangePasswordSheetVisible by rememberSaveable { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            Spacer(modifier = Modifier.height(Dimens.HeaderSpacing))

            val enter = rememberSectionEnterAlphas(2)
            AppHeader(
                title = stringResource(R.string.title_security_privacy),
                onBackClick = onBackClick,
                modifier = Modifier.alpha(enter[0])
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().alpha(enter[1]),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (isAdsEnabled) {
                    item {
                        AdContainer(isAdsEnabled = true) {
                            NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
                        }
                    }
                }

                item {
                    SettingsGroup(colors = settingsGroupCardColors()) {
                        SettingsItemCard(
                            title = stringResource(R.string.title_app_lock),
                            subtitle = stringResource(R.string.label_secure_app_pin),
                            icon = PhosphorIcons.Regular.Lock,
                            iconTile = SettingsTiles.Emerald,
                            type = SettingsItemType.Toggle,
                            standalone = false,
                            isChecked = isAppLockEnabled,
                            onCheckedChange = onAppLockChange
                        )
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(R.string.title_auto_lock_duration),
                            subtitle = stringResource(R.string.label_auto_lock_subtitle),
                            icon = PhosphorIcons.Regular.Timer,
                            iconTile = SettingsTiles.Sky,
                            type = SettingsItemType.Value,
                            standalone = false,
                            valueText = formatAutoLockDurationLabel(LocalContext.current, autoLockDurationMinutes),
                            isEnabled = isAppLockEnabled,
                            onClick = { isAutoLockDurationPickerVisible = true }
                        )
                        SettingsGroupDivider()
                        SettingsItemCard(
                            title = stringResource(R.string.title_biometric),
                            subtitle = stringResource(R.string.label_use_biometric_subtitle),
                            icon = PhosphorIcons.Regular.Fingerprint,
                            iconTile = SettingsTiles.Violet,
                            type = SettingsItemType.Toggle,
                            standalone = false,
                            isEnabled = isAppLockEnabled && hasAppLockPin,
                            isChecked = isBiometricEnabled,
                            onCheckedChange = onBiometricChange
                        )
                        SettingsGroupDivider()
                        GatedAction(
                            feature = Feature.SCRAMBLED_PIN_KEYPAD,
                            onAction = { onScrambledPinKeypadChange(!isScrambledPinKeypadEnabled) }
                        ) { status, onClick ->
                            val accessLevel = FeatureRegistry.getAccessLevel(Feature.SCRAMBLED_PIN_KEYPAD)
                            SettingsItemCard(
                                title = stringResource(R.string.label_scrambled_keypad),
                                subtitle = stringResource(R.string.label_scrambled_keypad_subtitle),
                                icon = PhosphorIcons.Regular.GridFour,
                                iconTile = SettingsTiles.Fuchsia,
                                type = SettingsItemType.Toggle,
                                standalone = false,
                                accessLevel = accessLevel,
                                isLocked = !isScrambledPinKeypadEnabled && status !is AccessStatus.Granted,
                                isEnabled = isAppLockEnabled && hasAppLockPin,
                                isChecked = isScrambledPinKeypadEnabled,
                                onCheckedChange = { onClick() },
                                onClick = onClick
                            )
                        }
                    }
                }

                item {
                    SettingsGroup(colors = settingsGroupCardColors()) {
                        GatedAction(
                            feature = Feature.PRIVACY_PROTECTION,
                            onAction = { onBlurInRecentsChange(!isBlurInRecentsEnabled) }
                        ) { status, onClick ->
                            val accessLevel = FeatureRegistry.getAccessLevel(Feature.PRIVACY_PROTECTION)
                            SettingsItemCard(
                                title = stringResource(R.string.label_blur_in_recents),
                                subtitle = stringResource(R.string.label_blur_in_recents_subtitle),
                                icon = PhosphorIcons.Regular.EyeSlash,
                                iconTile = SettingsTiles.Indigo,
                                type = SettingsItemType.Toggle,
                                standalone = false,
                                accessLevel = accessLevel,
                                isLocked = !isBlurInRecentsEnabled && status !is AccessStatus.Granted,
                                isChecked = isBlurInRecentsEnabled,
                                onCheckedChange = { onClick() },
                                onClick = onClick
                            )
                        }
                        SettingsGroupDivider()
                        GatedAction(
                            feature = Feature.PRIVACY_PROTECTION,
                            onAction = { onScreenshotProtectionChange(!isScreenshotProtectionEnabled) }
                        ) { status, onClick ->
                            val accessLevel = FeatureRegistry.getAccessLevel(Feature.PRIVACY_PROTECTION)
                            SettingsItemCard(
                                title = stringResource(R.string.title_block_screenshots),
                                subtitle = stringResource(R.string.label_block_screenshots_subtitle),
                                icon = PhosphorIcons.Regular.CameraSlash,
                                iconTile = SettingsTiles.Rose,
                                type = SettingsItemType.Toggle,
                                standalone = false,
                                accessLevel = accessLevel,
                                isLocked = !isScreenshotProtectionEnabled && status !is AccessStatus.Granted,
                                isChecked = isScreenshotProtectionEnabled,
                                onCheckedChange = { onClick() },
                                onClick = onClick
                            )
                        }
                    }
                }

                if (isEmailPasswordUser) {
                    item {
                        SettingsGroup(colors = settingsGroupCardColors()) {
                            SettingsItemCard(
                                title = stringResource(R.string.title_change_password),
                                subtitle = stringResource(R.string.label_change_password_subtitle),
                                icon = PhosphorIcons.Regular.Password,
                                iconTile = SettingsTiles.Amber,
                                type = SettingsItemType.Navigation,
                                standalone = true,
                                onClick = { isChangePasswordSheetVisible = true }
                            )
                        }
                    }
                }

                item {
                    // Inline Native Ad after Groups
                    AdContainer(isAdsEnabled = isAdsEnabled) {
                        NativeAdCard(placement = AdPlacement.SETTINGS_GENERAL)
                    }
                }
            }
        }
    }

    if (isAutoLockDurationPickerVisible) {
        AutoLockDurationPickerSheet(
            selectedDurationMinutes = autoLockDurationMinutes,
            onDismiss = { isAutoLockDurationPickerVisible = false },
            onDurationSelected = onAutoLockDurationChange
        )
    }

    if (isChangePasswordSheetVisible) {
        ChangePasswordSheet(
            onDismiss = { isChangePasswordSheetVisible = false },
            updatePasswordState = updatePasswordState,
            onUpdatePassword = onUpdatePassword,
            onResetUpdatePasswordState = onResetUpdatePasswordState
        )
    }
}


