package com.mknlabs.expensetracker


import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.isDark

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore
import com.mknlabs.expensetracker.data.local.AppLockPreferences
import com.mknlabs.expensetracker.data.local.UserProfileDataStore
import com.mknlabs.expensetracker.domain.repository.JsonImportResult
import com.mknlabs.expensetracker.models.AppSettings
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.CURRENT_TERMS_VERSION
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.InterstitialPlacement
import com.mknlabs.expensetracker.core.ui.components.AppLockOverlay
import com.mknlabs.expensetracker.models.PinVisualMode
import com.mknlabs.expensetracker.core.ui.components.MainScaffold
import com.mknlabs.expensetracker.core.ui.components.PremiumGateSheet
import com.mknlabs.expensetracker.core.ui.components.AiParseReviewSheet
import com.mknlabs.expensetracker.core.ui.components.VoiceInputSheet
import com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem
import com.mknlabs.expensetracker.core.ui.components.VoiceSheetState
import com.mknlabs.expensetracker.core.ui.components.adPassDurationLabel
import com.mknlabs.expensetracker.core.ui.navigation.AppRoute
import com.mknlabs.expensetracker.core.ui.navigation.AppLockFlow
import com.mknlabs.expensetracker.core.ui.navigation.rememberMainNavigationState
import com.mknlabs.expensetracker.core.ui.navigation.routesKeepingTransactionsWarm
import com.mknlabs.expensetracker.notifications.NotificationHelper
import com.mknlabs.expensetracker.notifications.NotificationScheduler
import com.mknlabs.expensetracker.sms.ParsedSms
import com.mknlabs.expensetracker.sms.SmsNotificationManager
import com.mknlabs.expensetracker.feature.auth.ui.OnboardingScreen
import com.mknlabs.expensetracker.feature.smsinbox.ui.SmsChangeRoute
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.core.ui.HomeFirstRunPromptGate
import com.mknlabs.expensetracker.core.ui.MainViewModel
import com.mknlabs.expensetracker.voice.VoiceAddViewModel
import com.mknlabs.expensetracker.monetization.MonetizationViewModel
import com.mknlabs.expensetracker.feature.auth.ui.AuthViewModel
import com.mknlabs.expensetracker.feature.auth.ui.AuthRoute
import com.mknlabs.expensetracker.workers.SyncWorker
import com.mknlabs.expensetracker.utils.toAmountFormatPreferences
import com.mknlabs.expensetracker.utils.toMajorUnits
import com.mknlabs.expensetracker.utils.BiometricAuthManager
import com.mknlabs.expensetracker.utils.findFragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.util.UUID
import com.mknlabs.expensetracker.workers.AutoBackupScheduler
import com.mknlabs.expensetracker.utils.DeviceIntegrityUtils
import com.mknlabs.expensetracker.utils.AppRestartUtils
import com.mknlabs.expensetracker.utils.BackupDecryptionException
import com.mknlabs.expensetracker.core.ui.theme.AdLoadingScrim
import com.mknlabs.expensetracker.core.ui.theme.AdLoadingText

import com.mknlabs.expensetracker.monetization.FeatureRegistry
import com.mknlabs.expensetracker.monetization.AccessLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreenOverlays(
    isReady: Boolean,
    isUiInteractive: Boolean,
    isAdLoading: Boolean,
    adExpiryMinutesRemaining: Int,
    adPassDuration: String,
    selectedCurrencyId: Int,
    isBiometricEnabled: Boolean,
    isScrambledPinKeypadEffective: Boolean,
    effectiveUserTier: com.mknlabs.expensetracker.models.UserTier,
    effectiveAppSettings: com.mknlabs.expensetracker.models.AppSettings,
    mainUiState: com.mknlabs.expensetracker.core.ui.MainDataUiState,
    navigationState: com.mknlabs.expensetracker.core.ui.navigation.MainNavigationState,
    mainViewModel: com.mknlabs.expensetracker.core.ui.MainViewModel,
    authViewModel: com.mknlabs.expensetracker.feature.auth.ui.AuthViewModel,
    monetizationViewModel: com.mknlabs.expensetracker.monetization.MonetizationViewModel,
    activity: android.app.Activity?,
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    authSheetState: androidx.compose.material3.SheetState,
    biometricAvailability: com.mknlabs.expensetracker.utils.BiometricAvailability,
    voiceReviewItems: androidx.compose.runtime.snapshots.SnapshotStateList<com.mknlabs.expensetracker.ai.cloud.AiParsedReviewItem>,
    showPremiumSheetState: androidx.compose.runtime.MutableState<Boolean>,
    showAdExpiryWarningDialogState: androidx.compose.runtime.MutableState<Boolean>,
    showDeviceIntegrityNoticeState: androidx.compose.runtime.MutableState<Boolean>,
    showLogoutDialogState: androidx.compose.runtime.MutableState<Boolean>,
    showAccountCreatedPopupState: androidx.compose.runtime.MutableState<Boolean>,
    showAuthSheetState: androidx.compose.runtime.MutableState<Boolean>,
    smsChangeRequestState: androidx.compose.runtime.MutableState<com.mknlabs.expensetracker.sms.ParsedSms?>,
    appLockFlowState: androidx.compose.runtime.MutableState<com.mknlabs.expensetracker.core.ui.navigation.AppLockFlow?>,
    appLockStateState: androidx.compose.runtime.MutableState<com.mknlabs.expensetracker.data.local.AppLockCachedState>,
    voiceReviewSourceTextState: androidx.compose.runtime.MutableState<String>,
    showToast: (String) -> Unit,
    handleSuccessfulAuth: () -> Unit,
    unlockWithBiometric: () -> Unit,
    completeUnlock: () -> Unit,
    disableAppLock: (Boolean) -> Unit,
) {
    var showPremiumSheet by showPremiumSheetState
    var showAdExpiryWarningDialog by showAdExpiryWarningDialogState
    var showDeviceIntegrityNotice by showDeviceIntegrityNoticeState
    var showLogoutDialog by showLogoutDialogState
    var showAccountCreatedPopup by showAccountCreatedPopupState
    var showAuthSheet by showAuthSheetState
    var smsChangeRequest by smsChangeRequestState
    var appLockFlow by appLockFlowState
    var appLockState by appLockStateState
    var voiceReviewSourceText by voiceReviewSourceTextState

    if (showPremiumSheet && isUiInteractive) {
        PremiumGateSheet(
            onDismiss = { showPremiumSheet = false },
            onUpgradeClick = {
                showPremiumSheet = false
                // This sheet is rendered by the shell itself, outside the subtree that
                // installs LocalUpgradeToPro, so the shell navigates directly.
                navigationState.updateBottomBarVisibility(false)
                navigationState.navigateTo(AppRoute.Paywall)
            }
        )
    }

    if (showAdExpiryWarningDialog && isUiInteractive) {
            ModernConfirmationDialog(
                onDismissRequest = { showAdExpiryWarningDialog = false },
                title = stringResource(id = R.string.title_ad_expiry_warning),
                text = stringResource(
                    id = R.string.msg_ad_expiry_warning,
                    adExpiryMinutesRemaining,
                    adPassDuration
                ),
                confirmButtonText = stringResource(id = R.string.btn_extend_now),
                onConfirm = {
                    showAdExpiryWarningDialog = false
                    if (activity != null) {
                        monetizationViewModel.onWatchAdFreeClicked(activity)
                    }
                },
                headerIcon = Icons.Rounded.PlayArrow,
                dismissButtonText = stringResource(id = R.string.btn_maybe_later),
                onDismiss = { showAdExpiryWarningDialog = false },
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        }

        if (showDeviceIntegrityNotice && isUiInteractive) {
            val acknowledgeIntegrityNotice: () -> Unit = {
                showDeviceIntegrityNotice = false
                HomeFirstRunPromptGate.markIntegrityComplete()
                coroutineScope.launch {
                    AppSettingsDataStore.updateAppSettings(context) { settings ->
                        settings.copy(deviceIntegrityNoticeAcknowledged = true)
                    }
                }
            }
            ModernConfirmationDialog(
                onDismissRequest = acknowledgeIntegrityNotice,
                title = stringResource(id = R.string.title_device_integrity_notice),
                text = stringResource(id = R.string.msg_device_integrity_notice),
                confirmButtonText = stringResource(id = R.string.btn_got_it),
                onConfirm = acknowledgeIntegrityNotice,
                headerIcon = Icons.Rounded.Security,
                showDismissButton = false
            )
        }

        if (showLogoutDialog && isUiInteractive) {
            ModernConfirmationDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = stringResource(id = R.string.label_logout),
                text = stringResource(id = R.string.msg_logout_confirm),
                confirmButtonText = stringResource(id = R.string.label_logout),
                onConfirm = {
                    showLogoutDialog = false
                    authViewModel.signOut()
                },
                headerIcon = Icons.AutoMirrored.Rounded.Logout,
                dismissButtonText = stringResource(id = R.string.label_cancel_1),
                onDismiss = { showLogoutDialog = false }
            )
        }

        if (showAccountCreatedPopup) {
            AnimatedVisibility(
                visible = showAccountCreatedPopup,
                enter = fadeIn(animationSpec = tween(250)) + scaleIn(animationSpec = tween(250), initialScale = 0.96f),
                exit = fadeOut(animationSpec = tween(450)) + scaleOut(animationSpec = tween(450), targetScale = 0.96f),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                        tonalElevation = 8.dp,
                        shadowElevation = 10.dp,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = R.string.title_account_created),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(id = R.string.msg_account_created_successfully),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        if (showAuthSheet && isUiInteractive) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showAuthSheet = false
                    authViewModel.resetState()
                },
                sheetState = authSheetState,
                containerColor = if (MaterialTheme.colorScheme.isDark) {
                    com.mknlabs.expensetracker.feature.auth.ui.AuthSheetDark
                } else {
                    com.mknlabs.expensetracker.feature.auth.ui.AuthSheetLight
                },
                contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(48.dp, 6.dp)
                            .background(
                                if (MaterialTheme.colorScheme.isDark) {
                                    com.mknlabs.expensetracker.feature.auth.ui.AuthHandleDark
                                } else {
                                    com.mknlabs.expensetracker.feature.auth.ui.AuthHandleLight
                                },
                                RoundedCornerShape(50)
                            )
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(bottom = 32.dp)
                ) {
                    AuthRoute(
                        viewModel = authViewModel,
                        onAuthSuccess = {
                            showAuthSheet = false
                            handleSuccessfulAuth()
                        },
                        onGuestContinue = {
                            showAuthSheet = false
                        },
                        onSignUpSuccess = {
                            showAuthSheet = false
                            showAccountCreatedPopup = true
                            handleSuccessfulAuth()
                        },
                        // Add-Account flow launches the system settings screen.
                        onPrepareForExternalActivity = { AppLockPreferences.setLockSuppressed(true) }
                    )
                }
            }
        }

        // Smart SMS Import "Change" bottom sheet (plan §8 / Phase 4): shown when
        // the app was opened via the notification's Change action. On save, the
        // notification is dismissed and Room flows refresh everything.
        smsChangeRequest?.takeIf { isUiInteractive }?.let { parsed ->
            SmsChangeRoute(
                parsedSms = parsed,
                categories = mainUiState.categories,
                onDismiss = { smsChangeRequest = null },
                onSaved = {
                    SmsNotificationManager.cancel(context)
                    smsChangeRequest = null
                    showToast(context.getString(R.string.toast_sms_transaction_saved))
                }
            )
        }

        // ── Voice Input Sheet (Speak-to-Add shortcut) ──
        if (navigationState.showVoiceInputSheet && isUiInteractive) {
            val voiceViewModel: VoiceAddViewModel = hiltViewModel()
            val voiceUiState by voiceViewModel.uiState.collectAsStateWithLifecycle()

            val hasMicPermission = remember {
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
            }
            var micPermissionGranted by remember { mutableStateOf(hasMicPermission) }

            val voicePermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                micPermissionGranted = granted
                if (granted) {
                    voiceViewModel.resetToListening()
                } else {
                    voiceViewModel.onRecognizerError(R.string.msg_voice_error_no_permission)
                }
            }

            // SpeechRecognizer for voice capture
            val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
            DisposableEffect(speechRecognizer) {
                onDispose { speechRecognizer.destroy() }
            }

            // Auto-start listening when sheet opens
            LaunchedEffect(navigationState.showVoiceInputSheet) {
                if (navigationState.showVoiceInputSheet) {
                    if (micPermissionGranted) {
                        voiceViewModel.resetToListening()
                    } else {
                        voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }

            // Start speech recognizer when state is LISTENING
            LaunchedEffect(voiceUiState.sheetState) {
                if (voiceUiState.sheetState == VoiceSheetState.LISTENING) {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    }
                    speechRecognizer.setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {}
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {}
                        override fun onError(error: Int) {
                            val errorResId = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH,
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> R.string.msg_voice_error_empty_input
                                SpeechRecognizer.ERROR_NETWORK,
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> R.string.msg_voice_error_network
                                SpeechRecognizer.ERROR_AUDIO -> R.string.msg_voice_error_audio
                                else -> R.string.msg_voice_error_recognizer
                            }
                            voiceViewModel.onRecognizerError(errorResId)
                        }
                        override fun onResults(results: Bundle?) {
                            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                ?.firstOrNull().orEmpty()
                            voiceViewModel.onSpeechResult(text)
                        }
                        override fun onPartialResults(partialResults: Bundle?) {
                            val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                ?.firstOrNull().orEmpty()
                            voiceViewModel.onPartialResult(text)
                        }
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                    speechRecognizer.startListening(intent)
                }
            }

            LaunchedEffect(voiceUiState.sheetState, voiceUiState.reviewItems) {
                if (voiceUiState.sheetState != VoiceSheetState.RESULT) return@LaunchedEffect
                speechRecognizer.cancel()
                val items = voiceUiState.reviewItems
                val transcript = voiceUiState.transcript
                voiceViewModel.dismiss()
                navigationState.updateShowVoiceInputSheet(false)
                if (items.isEmpty()) {
                    navigationState.updateAddTransactionDraftNote(transcript)
                    navigationState.navigateTo(AppRoute.AddTransaction)
                } else {
                    voiceReviewSourceText = transcript
                    voiceReviewItems.clear()
                    voiceReviewItems.addAll(items)
                }
            }

            VoiceInputSheet(
                sheetState = voiceUiState.sheetState,
                transcript = voiceUiState.transcript,
                errorMessage = voiceUiState.errorMessageResId?.let { context.getString(it) },
                onDismissRequest = {
                    speechRecognizer.cancel()
                    voiceViewModel.dismiss()
                    navigationState.updateShowVoiceInputSheet(false)
                },
                onRetry = {
                    speechRecognizer.cancel()
                    voiceViewModel.resetToListening()
                },
                onDone = { speechRecognizer.stopListening() },
            )
        }

        if (voiceReviewItems.isNotEmpty()) {
            AiParseReviewSheet(
                items = voiceReviewItems.toList(),
                currencyId = selectedCurrencyId,
                onDismissRequest = {
                    voiceReviewItems.clear()
                },
                onAdd = { item ->
                    val now = System.currentTimeMillis()
                    mainViewModel.saveTransaction(
                        Transaction(
                            id = "",
                            note = item.note,
                            createdAt = now,
                            amountMinor = item.amountMinor,
                            transactionTypeId = item.typeId,
                            paymentTypeId = item.paymentTypeId,
                            categoryId = item.categoryId,
                            contentHash = null,
                            syncState = SyncState.PENDING_UPLOAD,
                            isDeleted = false,
                            updatedAt = now,
                            sourceRecurringRuleId = null,
                            fundId = null,
                        ),
                        null,
                        null,
                        false,
                        emptyList(),
                    )
                    voiceReviewItems.removeAll { it.id == item.id }
                },

            )
        }

        if (appLockFlow != null) {
            AppLockOverlay(
                isReady = isReady,
                appSettings = effectiveAppSettings,
                initialFlow = appLockFlow!!,
                isAppUnlocked = true, // MainScreen only exists in Unlocked state
                // Non-null onDismiss switches AppLockOverlay into its fullscreen
                // Dialog mode so the Setup/Unlock flow renders in its own window
                // ABOVE any open ModalBottomSheet / AlertDialog (bottom sheets and
                // dialogs live in separate windows and would otherwise cover an
                // inline overlay). The flow itself still only closes via its own
                // back button (onBackClick); the dialog is not dismissible by back
                // press or outside tap.
                onDismiss = { appLockFlow = null },
                biometricEnabled = isBiometricEnabled && biometricAvailability.isAvailable,
                scrambledPinKeypadEnabled = isScrambledPinKeypadEffective,
                isBiometricAvailable = biometricAvailability.isAvailable,
                // Recovery questions (real + decoys) are derived internally by AppLockOverlay.
                onBackClick = { appLockFlow = null },
                autoTriggerBiometricOnShow = appLockFlow == AppLockFlow.Unlock,
                onBiometricClick = unlockWithBiometric,
                onSetupComplete = { pin, questionId, answer ->
                    // 1. Dismiss UI immediately
                    completeUnlock()

                    // 2. Background heavy work
                    coroutineScope.launch(Dispatchers.Default) {
                        AppLockPreferences.savePin(context, pin)
                        AppLockPreferences.saveSecurityQuestion(context, questionId, answer)
                        appLockState = AppLockPreferences.getCachedState()

                        AppSettingsDataStore.updateAppSettings(context) { settings ->
                            settings.copy(appLockEnabled = true)
                        }
                    }
                },
                onUnlockSuccess = completeUnlock,
                validateUnlockPin = { pin ->
                    AppLockPreferences.validatePinForUnlock(context, pin).also {
                        appLockState = AppLockPreferences.getCachedState()
                    }
                },
                onForgotPinRecovery = {
                    disableAppLock(true)
                },
                validateSecurityAnswer = { questionId, answer ->
                    AppLockPreferences.validateSecurityQuestionAnswer(context, questionId, answer)
                },
                pinVisualMode = if (effectiveUserTier == UserTier.PREMIUM) PinVisualMode.PRO_ANIMATED else PinVisualMode.NORMAL
            )
        }

        if (isAdLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AdLoadingScrim)
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        color = MaterialTheme.colorScheme.accentInk,
                        strokeWidth = 4.dp
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = stringResource(id = R.string.msg_preparing_pro_experience),
                        color = AdLoadingText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.msg_wont_take_long),
                        color = AdLoadingText.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
}
