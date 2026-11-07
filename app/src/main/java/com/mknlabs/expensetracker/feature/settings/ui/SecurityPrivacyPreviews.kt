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


@Preview(showBackground = true)
@Composable
internal fun SecurityPrivacyScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        SecurityPrivacyContent(
            isAppLockEnabled = true,
            hasAppLockPin = true,
            isBiometricEnabled = false,
            isScrambledPinKeypadEnabled = false,
            isBlurInRecentsEnabled = true,
            isScreenshotProtectionEnabled = false,
            autoLockDurationMinutes = 5,
            isAdsEnabled = false,
            isEmailPasswordUser = true,
            updatePasswordState = UpdatePasswordState.Idle,
            onAppLockChange = {},
            onBiometricChange = {},
            onScrambledPinKeypadChange = {},
            onBlurInRecentsChange = {},
            onScreenshotProtectionChange = {},
            onAutoLockDurationChange = {},
            onBackClick = {},
            onUpdatePassword = { _, _ -> },
            onResetUpdatePasswordState = {}
        )
    }
}
