package com.mknlabs.expensetracker.feature.home.ui


import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.track

import android.content.res.Configuration
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.CheckCircle
import com.adamglin.phosphoricons.regular.CalendarBlank
import com.adamglin.phosphoricons.regular.ChatCircle
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Circle
import com.adamglin.phosphoricons.regular.Gear
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.PiggyBank
import com.adamglin.phosphoricons.regular.X
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.avatarInitials
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.models.hasPhoneNumber
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.core.ui.components.*
import com.mknlabs.expensetracker.core.ui.HomeFirstRunPromptGate
import com.mknlabs.expensetracker.core.ui.components.LocalSplashGone
import com.mknlabs.expensetracker.core.ui.components.MissingPermissionsBottomSheet
import com.mknlabs.expensetracker.core.ui.components.RateAppBottomSheet
import com.mknlabs.expensetracker.core.ui.components.ReferOfferBottomSheet
import com.mknlabs.expensetracker.data.local.ReferPromptStore
import com.mknlabs.expensetracker.data.local.RateAppPromptStore
import com.mknlabs.expensetracker.data.local.SmsInboxStaleRemindStore
import com.mknlabs.expensetracker.utils.RateAppPrompt
import com.mknlabs.expensetracker.utils.SmsInboxStaleRemind
import kotlinx.coroutines.delay
import com.mknlabs.expensetracker.utils.LaunchSheetPick
import kotlinx.coroutines.launch
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.NavOnDark
import com.mknlabs.expensetracker.core.ui.theme.NavOnLight
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.feature.smsinbox.ui.SmsSetupUiState
import com.mknlabs.expensetracker.feature.smsinbox.ui.SmsSetupViewModel
import com.mknlabs.expensetracker.utils.DeviceVendorUtils
import com.mknlabs.expensetracker.utils.findFragmentActivity
import com.mknlabs.expensetracker.utils.PermissionNudge
import com.mknlabs.expensetracker.data.local.PermissionNudgeState
import com.mknlabs.expensetracker.data.local.PermissionNudgeStore
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.AdPlacement


internal data class HomeSmsSetupBindings(
    val smsSetupUiState: SmsSetupUiState,
    val onSmsPermissionCardOpenSettings: () -> Unit,
    val onSmsPermissionCardDismiss: () -> Unit,
    val onMiuiSetupCardOpenAppSettings: () -> Unit,
    val onMiuiSetupCardBatterySettings: () -> Unit,
    val onMiuiSetupCardDismiss: () -> Unit,
)

@Composable
internal fun HomePrompts(
    appSettings: com.mknlabs.expensetracker.models.AppSettings?,
    isLockOverlayActive: Boolean,
    transactionCount: Int,
    smsInboxUnreadCount: Int,
    smsInboxOldestUnreadCreatedAt: Long?,
    onPrepareForExternalActivity: () -> Unit,
    onReferOfferClick: () -> Unit,
    onSmsInboxClick: () -> Unit,
    content: @Composable (HomeSmsSetupBindings) -> Unit,
) {
    val context = LocalContext.current

    // Bumped after the SMS permission dialog closes so the setup cards re-evaluate
    // against the fresh permission state (denied → permission card deep link).
    var smsPermissionCheckTrigger by remember { mutableIntStateOf(0) }

    // Flips to true once the notification permission dialog (if any) is resolved,
    // so the SMS prompt never stacks on top of it — it fires right after instead.
    var notificationPromptResolved by remember { mutableStateOf(false) }
    var osPermissionCycleFinished by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        notificationPromptResolved = true
    }

    val permissionNudge by PermissionNudgeStore.flow(context).collectAsStateWithLifecycle(
        PermissionNudgeState()
    )
    val permissionActivity = context.findFragmentActivity()
    val splashGone = LocalSplashGone.current
    val homeVisibleForPrompts = splashGone && !isLockOverlayActive
    var homeReadyForPermissionUi by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(homeVisibleForPrompts) {
        if (!homeVisibleForPrompts) {
            homeReadyForPermissionUi = false
            osPermissionCycleFinished = false
            return@LaunchedEffect
        }
        delay(400)
        homeReadyForPermissionUi = true
    }

    androidx.compose.runtime.LaunchedEffect(
        homeReadyForPermissionUi,
        permissionNudge.notificationAskCount,
        permissionNudge.notificationLastAskAtMillis,
    ) {
        if (!homeReadyForPermissionUi) return@LaunchedEffect
        val isBenchmarkBuild = BuildConfig.BUILD_TYPE == "benchmark"
        val notificationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        val canAskNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !isBenchmarkBuild
        val rationale = permissionActivity?.let {
            androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                it,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        } ?: true
        val ask = canAskNotifications && PermissionNudge.shouldAsk(
            granted = notificationGranted,
            askCount = permissionNudge.notificationAskCount,
            lastAskAtMillis = permissionNudge.notificationLastAskAtMillis,
            nowMillis = System.currentTimeMillis(),
            intervalDays = 0,
            canShowRationale = rationale,
        )
        if (ask) {
            osPermissionCycleFinished = false
            PermissionNudgeStore.markNotificationAsked(context)
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            notificationPromptResolved = true
        }
    }

    // Smart SMS Import — one-time permission prompt (plan §4 / D5).
    // Deliberately DIFFERENT from the notification prompt above: guarded by
    // AppSettings.smartSmsPrompted, which is set BEFORE the request so the
    // prompt fires exactly once ever (a denial is never re-nagged; only a
    // reinstall wipes the flag). API <= 25 grants RECEIVE_SMS at install time
    // (no prompt needed); on API 34+ a denial is permanently blocked by the OS.
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        smsPermissionCheckTrigger++
        osPermissionCycleFinished = true
        HomeFirstRunPromptGate.markPermissionsComplete()
    }

    // Smart SMS setup cards (permission resilience + MIUI guidance): re-evaluated
    // whenever the permission state changes (bump the trigger) or app settings
    // (dismiss flags) change. BuildConfig.BUILD_TYPE is a compile-time constant, so
    // R8 folds the benchmark gate away in release/debug.
    val smsSetupViewModel: SmsSetupViewModel = hiltViewModel()
    val smsSetupUiState by smsSetupViewModel.uiState.collectAsStateWithLifecycle()
    val smsSetupScope = rememberCoroutineScope()

    androidx.compose.runtime.LaunchedEffect(
        homeReadyForPermissionUi,
        appSettings?.smartSmsPrompted,
        notificationPromptResolved,
        permissionNudge.smsAskCount,
        permissionNudge.smsLastAskAtMillis,
    ) {
        if (!homeReadyForPermissionUi) return@LaunchedEffect
        val smsGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        val smsRationale = permissionActivity?.let {
            androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                it,
                Manifest.permission.RECEIVE_SMS,
            )
        } ?: true
        val shouldPrompt = notificationPromptResolved &&
            appSettings != null &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            PermissionNudge.shouldAsk(
                granted = smsGranted,
                askCount = permissionNudge.smsAskCount,
                lastAskAtMillis = permissionNudge.smsLastAskAtMillis,
                nowMillis = System.currentTimeMillis(),
                intervalDays = 0,
                canShowRationale = smsRationale,
            )
        if (shouldPrompt) {
            osPermissionCycleFinished = false
            // Small delay so the SMS dialog never races the notification dialog's
            // dismiss animation (some OEMs swallow a request launched too early).
            // NOTE: must come BEFORE persisting the flag — once smartSmsPrompted
            // flips to true the effect's key changes and this coroutine is
            // cancelled, which would silently drop the launch below.
            delay(250)
            // Persist the flag before showing the dialog so the decision is honored
            // even if the user immediately leaves or the app is killed during it.
            com.mknlabs.expensetracker.data.local.AppSettingsDataStore.updateAppSettings(context) {
                it.copy(smartSmsPrompted = true)
            }
            PermissionNudgeStore.markSmsAsked(context)
            smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
        } else if (notificationPromptResolved && appSettings != null) {
            osPermissionCycleFinished = true
            HomeFirstRunPromptGate.markPermissionsComplete()
        }
    }

    androidx.compose.runtime.LaunchedEffect(
        appSettings?.smartSmsPrompted,
        appSettings?.smsPermissionCardDismissed,
        appSettings?.smsMiuiSetupAcknowledged,
        smsPermissionCheckTrigger
    ) {
        smsSetupViewModel.refresh(
            smsPermissionGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED,
            smsPromptAlreadyShown = appSettings?.smartSmsPrompted ?: false,
            smsPermissionCardDismissed = true,
            smsMiuiSetupAcknowledged = appSettings?.smsMiuiSetupAcknowledged ?: false,
            isBenchmarkBuild = BuildConfig.BUILD_TYPE == "benchmark"
        )
    }

    fun openAppDetailsSettings() {
        try {
            onPrepareForExternalActivity()
            context.startActivity(DeviceVendorUtils.appDetailsSettingsIntent(context))
        } catch (e: Exception) {
            // Some OEMs block the details-settings deep link — nothing to fall back to.
        }
    }

    fun requestBatteryExemption() {
        try {
            onPrepareForExternalActivity()
            context.startActivity(DeviceVendorUtils.batteryOptimizationIntent(context))
        } catch (e: Exception) {
            // MIUI sometimes refuses the exemption request — the user can still use
            // the app-settings path from the other card button.
        }
    }

    fun dismissSmsPermissionCard() {
        smsSetupScope.launch {
            com.mknlabs.expensetracker.data.local.AppSettingsDataStore.updateAppSettings(context) {
                it.copy(smsPermissionCardDismissed = true)
            }
        }
    }

    fun dismissMiuiSetupCard() {
        smsSetupScope.launch {
            com.mknlabs.expensetracker.data.local.AppSettingsDataStore.updateAppSettings(context) {
                it.copy(smsMiuiSetupAcknowledged = true)
            }
        }
    }

    content(
        HomeSmsSetupBindings(
            smsSetupUiState = smsSetupUiState,
            onSmsPermissionCardOpenSettings = { openAppDetailsSettings() },
            onSmsPermissionCardDismiss = { dismissSmsPermissionCard() },
            onMiuiSetupCardOpenAppSettings = { openAppDetailsSettings() },
            onMiuiSetupCardBatterySettings = { requestBatteryExemption() },
            onMiuiSetupCardDismiss = { dismissMiuiSetupCard() },
        )
    )

    val missingNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    val missingSms = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) !=
        PackageManager.PERMISSION_GRANTED
    var permissionCardDismissedThisLaunch by remember { mutableStateOf(false) }
    val permissionCardScope = rememberCoroutineScope()

    val referPrompt by ReferPromptStore.flow(context).collectAsStateWithLifecycle(ReferPromptStore.State())
    val ratePrompt by RateAppPromptStore.flow(context).collectAsStateWithLifecycle(
        com.mknlabs.expensetracker.data.local.RateAppPromptState()
    )
    var referDismissed by remember { mutableStateOf(false) }
    var rateDismissed by remember { mutableStateOf(false) }
    val referScope = rememberCoroutineScope()
    val permissionsDone by HomeFirstRunPromptGate.permissionsComplete.collectAsStateWithLifecycle()
    val integrityDone by HomeFirstRunPromptGate.integrityComplete.collectAsStateWithLifecycle()
    val notifRationale = permissionActivity?.let {
        androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
            it,
            Manifest.permission.POST_NOTIFICATIONS,
        )
    } ?: true
    val smsRationaleForSheet = permissionActivity?.let {
        androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
            it,
            Manifest.permission.RECEIVE_SMS,
        )
    } ?: true
    val permissionDue = BuildConfig.BUILD_TYPE != "benchmark" &&
        !permissionCardDismissedThisLaunch &&
        osPermissionCycleFinished &&
        homeReadyForPermissionUi &&
        !isLockOverlayActive &&
        PermissionNudge.shouldShowSettingsCard(
            missingNotifications = missingNotifications,
            missingSms = missingSms,
            notificationAskCount = permissionNudge.notificationAskCount,
            smsAskCount = permissionNudge.smsAskCount,
            lastOsAskAtMillis = maxOf(
                permissionNudge.notificationLastAskAtMillis,
                permissionNudge.smsLastAskAtMillis,
            ),
            lastCardAtMillis = permissionNudge.lastCardAtMillis,
            nextIntervalDays = permissionNudge.nextCardIntervalDays.let {
                if (it > 0) it else 7
            },
            nowMillis = System.currentTimeMillis(),
            notificationOsFinished = permissionNudge.notificationAskCount >= PermissionNudge.MAX_ASKS ||
                (permissionNudge.notificationAskCount >= 1 && !notifRationale),
            smsOsFinished = permissionNudge.smsAskCount >= PermissionNudge.MAX_ASKS ||
                (permissionNudge.smsAskCount >= 1 && !smsRationaleForSheet),
        )
    val referDue = !isLockOverlayActive && !referDismissed &&
        permissionsDone && integrityDone &&
        ReferPromptStore.shouldShow(
            lastShownAtMillis = referPrompt.lastShownAtMillis,
            firstOpenAtMillis = ratePrompt.firstOpenAtMillis,
        )
    val rateDue = !isLockOverlayActive && !rateDismissed &&
        permissionsDone && integrityDone &&
        RateAppPrompt.shouldShow(
            transactionCount = transactionCount,
            firstOpenAtMillis = ratePrompt.firstOpenAtMillis,
            nowMillis = System.currentTimeMillis(),
            completed = ratePrompt.completed,
            dismissCount = ratePrompt.dismissCount,
            lastDismissAtMillis = ratePrompt.lastDismissAtMillis,
        )
    var pickedSheet by remember { mutableStateOf<LaunchSheetPick.Kind?>(null) }
    var sheetPickFrozen by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(
        osPermissionCycleFinished,
        homeReadyForPermissionUi,
        integrityDone,
        permissionDue,
        referDue,
        rateDue,
        ratePrompt.firstOpenAtMillis,
    ) {
        if (!osPermissionCycleFinished || !homeReadyForPermissionUi || !integrityDone) return@LaunchedEffect
        if (ratePrompt.firstOpenAtMillis <= 0L) return@LaunchedEffect
        if (sheetPickFrozen) return@LaunchedEffect
        pickedSheet = LaunchSheetPick.resolve(permissionDue, referDue, rateDue)
        sheetPickFrozen = true
    }
    val showPermissionSheet = pickedSheet == LaunchSheetPick.Kind.Permission && permissionDue
    val showRefer = pickedSheet == LaunchSheetPick.Kind.Refer && referDue
    val showRateSheet = pickedSheet == LaunchSheetPick.Kind.Rate && rateDue
    androidx.compose.runtime.LaunchedEffect(showRefer) {
        HomeFirstRunPromptGate.setShareSheetVisible(showRefer)
    }
    if (showPermissionSheet) {
        MissingPermissionsBottomSheet(
            missingNotifications = missingNotifications,
            missingSms = missingSms,
            onOpenSettings = {
                permissionCardDismissedThisLaunch = true
                permissionCardScope.launch { PermissionNudgeStore.markCardShown(context) }
                openAppDetailsSettings()
            },
            onDismiss = {
                permissionCardDismissedThisLaunch = true
                permissionCardScope.launch { PermissionNudgeStore.markCardShown(context) }
            },
        )
    }
    if (showRefer) {
        ReferOfferBottomSheet(
            hasReferred = referPrompt.successCount > 0,
            onRefer = {
                referDismissed = true
                HomeFirstRunPromptGate.setShareSheetVisible(false)
                referScope.launch { ReferPromptStore.markShown(context) }
                onReferOfferClick()
            },
            onDismiss = {
                referDismissed = true
                HomeFirstRunPromptGate.setShareSheetVisible(false)
                referScope.launch { ReferPromptStore.markShown(context) }
            }
        )
    }
    if (showRateSheet) {
        RateAppBottomSheet(
            onDismiss = {
                rateDismissed = true
                referScope.launch { RateAppPromptStore.markDismissed(context) }
            },
            onRated = {
                rateDismissed = true
                referScope.launch { RateAppPromptStore.markCompleted(context) }
            }
        )
    }
    val staleRemind by SmsInboxStaleRemindStore.flow(context).collectAsStateWithLifecycle(
        SmsInboxStaleRemindStore.State()
    )
    val staleEligible = SmsInboxStaleRemind.isEligible(
        unreadCount = smsInboxUnreadCount,
        oldestUnreadCreatedAt = smsInboxOldestUnreadCreatedAt,
        nowMillis = System.currentTimeMillis(),
        dismissedDayStart = staleRemind.dismissedDayStart,
    )
    androidx.compose.runtime.LaunchedEffect(
        staleEligible,
        staleRemind.scheduledShowAt,
        smsInboxUnreadCount,
        smsInboxOldestUnreadCreatedAt,
    ) {
        val now = System.currentTimeMillis()
        if (SmsInboxStaleRemind.needsSchedule(staleEligible, staleRemind.scheduledShowAt, now)) {
            SmsInboxStaleRemindStore.setScheduledShowAt(context, SmsInboxStaleRemind.pickShowAt(now))
        }
    }
    var staleDue by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(staleEligible, staleRemind.scheduledShowAt) {
        staleDue = false
        if (!staleEligible) return@LaunchedEffect
        val at = staleRemind.scheduledShowAt
        if (at <= 0L) return@LaunchedEffect
        val wait = at - System.currentTimeMillis()
        if (wait > 0L) delay(wait)
        staleDue = true
    }
    val showStaleRemind = staleDue &&
        homeVisibleForPrompts &&
        !showPermissionSheet &&
        !showRefer &&
        !showRateSheet &&
        SmsInboxStaleRemind.shouldShowCard(
            unreadCount = smsInboxUnreadCount,
            oldestUnreadCreatedAt = smsInboxOldestUnreadCreatedAt,
            nowMillis = System.currentTimeMillis(),
            scheduledShowAt = staleRemind.scheduledShowAt,
            dismissedDayStart = staleRemind.dismissedDayStart,
        )
    if (showStaleRemind) {
        val markStaleHandled: () -> Unit = {
            referScope.launch {
                SmsInboxStaleRemindStore.markHandledToday(
                    context,
                    SmsInboxStaleRemind.startOfLocalDay(System.currentTimeMillis()),
                )
            }
        }
        ModernConfirmationDialog(
            onDismissRequest = {},
            title = stringResource(R.string.title_sms_stale_remind),
            text = stringResource(
                R.string.msg_sms_stale_remind,
                smsInboxUnreadCount,
            ),
            confirmButtonText = stringResource(R.string.label_review_sms_inbox),
            onConfirm = {
                markStaleHandled()
                onSmsInboxClick()
            },
            headerIcon = PhosphorIcons.Regular.ChatCircle,
            headerIconContentDescription = stringResource(R.string.title_sms_stale_remind),
            dismissButtonText = stringResource(R.string.label_later),
            onDismiss = markStaleHandled,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )
    }
}
