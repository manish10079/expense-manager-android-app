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

internal fun sanitizeCardSettings(
    settings: TransactionCardCustomizationSettings,
    userTier: UserTier
): TransactionCardCustomizationSettings {
    if (userTier == UserTier.PREMIUM) return settings
    return settings.copy(
        showTransactionTime = if (FeatureRegistry.getAccessLevel(Feature.CARD_CUSTOMIZATION, "showTransactionTime") == AccessLevel.PREMIUM) false else settings.showTransactionTime,
        showDateSeparators = if (FeatureRegistry.getAccessLevel(Feature.CARD_CUSTOMIZATION, "showDateSeparators") == AccessLevel.PREMIUM) false else settings.showDateSeparators,
        showPaymentMethod = if (FeatureRegistry.getAccessLevel(Feature.CARD_CUSTOMIZATION, "showPaymentMethod") == AccessLevel.PREMIUM) false else settings.showPaymentMethod,
        showTransactionListSummaries = if (FeatureRegistry.getAccessLevel(Feature.CARD_CUSTOMIZATION, "showTransactionListSummaries") == AccessLevel.PREMIUM) false else settings.showTransactionListSummaries
    )
}

internal fun AppSettings.toTransactionCardCustomizationSettings(): TransactionCardCustomizationSettings {
    return TransactionCardCustomizationSettings(
        showIncomeExpenseLabels = transactionCardShowIncomeExpenseLabels,
        showTransactionDate = transactionCardShowTransactionDate,
        showPaymentMethod = transactionCardShowPaymentMethod,
        showTransactionTime = transactionCardShowTransactionTime,
        showCategoryIcon = transactionCardShowCategoryIcon,
        showCategoryLabel = transactionCardShowCategoryLabel,
        showDateSeparators = transactionCardShowDateSeparators,
        showTransactionListSummaries = transactionCardShowListSummaries
    )
}

internal fun AppSettings.withTransactionCardCustomizationSettings(
    settings: TransactionCardCustomizationSettings
): AppSettings {
    return copy(
        transactionCardShowIncomeExpenseLabels = settings.showIncomeExpenseLabels,
        transactionCardShowTransactionDate = settings.showTransactionDate,
        transactionCardShowPaymentMethod = settings.showPaymentMethod,
        transactionCardShowTransactionTime = settings.showTransactionTime,
        transactionCardShowCategoryIcon = settings.showCategoryIcon,
        transactionCardShowCategoryLabel = settings.showCategoryLabel,
        transactionCardShowDateSeparators = settings.showDateSeparators,
        transactionCardShowListSummaries = settings.showTransactionListSummaries
    )
}

internal fun JsonImportResult.toJsonImportMessage(context: Context): String {
    return context.getString(
        R.string.toast_json_import_message,
        importedTransactions,
        importedBudgets,
        importedRecurringRules,
        importedGoals,
        skippedTransactions
    )
}
