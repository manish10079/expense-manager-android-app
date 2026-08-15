package com.mknlabs.expensetracker.ui.screens

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
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.avatarInitials
import com.mknlabs.expensetracker.models.defaultUserProfile
import com.mknlabs.expensetracker.models.hasPhoneNumber
import com.mknlabs.expensetracker.ui.components.*
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.brandGradient
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.viewmodels.HomeViewModel
import com.mknlabs.expensetracker.ui.viewmodels.HomeScreenUiState
import com.mknlabs.expensetracker.ui.viewmodels.SmsSetupUiState
import com.mknlabs.expensetracker.ui.viewmodels.SmsSetupViewModel
import com.mknlabs.expensetracker.utils.DeviceVendorUtils
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.monetization.AdPlacement

@Composable
fun HomeScreen(
    userProfile: UserProfile = defaultUserProfile,
    appSettings: com.mknlabs.expensetracker.models.AppSettings? = null,
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    categories: List<CategoryType> = emptyList(),
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    onViewAllClick: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onTodaySpendingClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onPrepareForExternalActivity: () -> Unit = {},
    isAdsEnabled: Boolean = false,
    isLockOverlayActive: Boolean = false
) {
    val context = LocalContext.current

    // Bumped after the SMS permission dialog closes so the setup cards re-evaluate
    // against the fresh permission state (denied → permission card deep link).
    var smsPermissionCheckTrigger by remember { mutableIntStateOf(0) }

    // Flips to true once the notification permission dialog (if any) is resolved,
    // so the SMS prompt never stacks on top of it — it fires right after instead.
    var notificationPromptResolved by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Notification dialog dismissed (Allow / Deny / back) — it is now safe to
        // show the SMS prompt (the SMS LaunchedEffect waits on this flag).
        notificationPromptResolved = true
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        // Benchmark builds must never pop the runtime permission dialog — it would
        // cover the UI under test (the Macrobenchmark "See All" journeys depend on
        // an unobstructed Home screen). BuildConfig.BUILD_TYPE is a compile-time
        // constant, so R8 folds this gate away in release/debug.
        val isBenchmarkBuild = BuildConfig.BUILD_TYPE == "benchmark"
        val notificationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isBenchmarkBuild && !notificationGranted) {
            // Show the notification prompt FIRST; the SMS prompt waits for its
            // dismissal (see permissionLauncher callback above).
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Notification already granted, or no prompt on this API level / in
            // benchmark builds — nothing to wait for, so the SMS prompt may fire.
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
    ) { _ -> smsPermissionCheckTrigger++ }

    // Smart SMS setup cards (permission resilience + MIUI guidance): re-evaluated
    // whenever the permission state changes (bump the trigger) or app settings
    // (dismiss flags) change. BuildConfig.BUILD_TYPE is a compile-time constant, so
    // R8 folds the benchmark gate away in release/debug.
    val smsSetupViewModel: SmsSetupViewModel = hiltViewModel()
    val smsSetupUiState by smsSetupViewModel.uiState.collectAsStateWithLifecycle()
    val smsSetupScope = rememberCoroutineScope()

    androidx.compose.runtime.LaunchedEffect(appSettings?.smartSmsPrompted, notificationPromptResolved) {
        val shouldPrompt = notificationPromptResolved &&
            appSettings != null &&
            !appSettings.smartSmsPrompted &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) !=
            PackageManager.PERMISSION_GRANTED
        if (shouldPrompt) {
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
            // No suspension point between the persist and the launch, so the launch
            // runs synchronously before any recomposition can cancel this effect.
            smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
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
            smsPermissionCardDismissed = appSettings?.smsPermissionCardDismissed ?: false,
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

    val homeViewModel: HomeViewModel = hiltViewModel()

    androidx.compose.runtime.LaunchedEffect(
        userProfile,
        appSettings?.userTier,
        currencyId,
        amountFormatPreferences,
        dateFormatPattern,
        timeFormat,
        categories,
        transactionCardCustomizationSettings
    ) {
        homeViewModel.updateInputs(
            userProfile = userProfile,
            userTier = appSettings?.userTier ?: com.mknlabs.expensetracker.models.UserTier.FREE,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = dateFormatPattern,
            timeFormat = timeFormat,
            categories = categories,
            customizationSettings = transactionCardCustomizationSettings
        )
    }
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        userProfile = userProfile,
        uiState = uiState,
        appSettings = appSettings,
        isAdsEnabled = isAdsEnabled,
        onViewAllClick = onViewAllClick,
        onTransactionClick = onTransactionClick,
        onProfileClick = onProfileClick,
        onSettingsClick = onSettingsClick,
        onTodaySpendingClick = onTodaySpendingClick,
        onGoalsClick = onGoalsClick,
        onToggleBalanceVisibility = homeViewModel::toggleBalanceVisibility,
        smsSetupUiState = smsSetupUiState,
        onSmsPermissionCardOpenSettings = { openAppDetailsSettings() },
        onSmsPermissionCardDismiss = { dismissSmsPermissionCard() },
        onMiuiSetupCardOpenAppSettings = { openAppDetailsSettings() },
        onMiuiSetupCardBatterySettings = { requestBatteryExemption() },
        onMiuiSetupCardDismiss = { dismissMiuiSetupCard() },
        isLockOverlayActive = isLockOverlayActive
    )
}

@Composable
private fun HomeScreenContent(
    userProfile: UserProfile,
    uiState: HomeScreenUiState,
    appSettings: com.mknlabs.expensetracker.models.AppSettings? = null,
    isAdsEnabled: Boolean = false,
    onViewAllClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTodaySpendingClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onToggleBalanceVisibility: () -> Unit,
    smsSetupUiState: SmsSetupUiState = SmsSetupUiState(),
    onSmsPermissionCardOpenSettings: () -> Unit = {},
    onSmsPermissionCardDismiss: () -> Unit = {},
    onMiuiSetupCardOpenAppSettings: () -> Unit = {},
    onMiuiSetupCardBatterySettings: () -> Unit = {},
    onMiuiSetupCardDismiss: () -> Unit = {},
    isLockOverlayActive: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Pro-gated transaction-card note tooltip (effective tier from app settings).
    val isProUser = uiState.userTier == com.mknlabs.expensetracker.models.UserTier.PREMIUM

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = Dimens.ScreenPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(Dimens.HeaderSpacing))

            // Shared by the hand wave (inside the greeting Column) and the settings-icon
            // spin (in the sibling Row) below — hence hoisted to this common scope.
            val waveRotation = remember { androidx.compose.animation.core.Animatable(0f) }
            val settingsRotation = remember { androidx.compose.animation.core.Animatable(0f) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val greetingName = uiState.greetingName

                    // Entrance animation (hand wave + settings-icon spin): plays when the home
                    // screen is actually VISIBLE — i.e. the activity is resumed AND no app-lock
                    // overlay is covering it. ON_RESUME alone fires while the lock overlay is
                    // still up, so the wave used to finish behind the lock before the user saw it.
                    var entrancePending by remember { mutableStateOf(false) }
                    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                                entrancePending = true
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    LaunchedEffect(entrancePending, isLockOverlayActive) {
                        if (entrancePending && !isLockOverlayActive) {
                            entrancePending = false
                            // Hand wave — ~2.5s total.
                            waveRotation.snapTo(0f)
                            waveRotation.animateTo(25f, tween(500, easing = LinearOutSlowInEasing))
                            waveRotation.animateTo(-20f, tween(450, easing = FastOutSlowInEasing))
                            waveRotation.animateTo(18f, tween(400, easing = FastOutSlowInEasing))
                            waveRotation.animateTo(-14f, tween(350, easing = FastOutSlowInEasing))
                            waveRotation.animateTo(10f, tween(300, easing = FastOutSlowInEasing))
                            waveRotation.animateTo(-6f, tween(250, easing = FastOutSlowInEasing))
                            waveRotation.animateTo(0f, tween(250, easing = FastOutLinearInEasing))
                            // Settings icon — one full 360° spin in 1s.
                            settingsRotation.snapTo(0f)
                            settingsRotation.animateTo(360f, tween(1000, easing = LinearEasing))
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.label_hi_val, greetingName),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Normal
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "👋",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.graphicsLayer {
                                rotationZ = waveRotation.value
                                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.7f, 0.9f)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(id = R.string.label_track_every_move_with_confiden),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.graphicsLayer {
                            rotationZ = settingsRotation.value
                        }
                    ) {
                        SettingsButton(onClick = onSettingsClick)
                    }

                    val isAnonymous = userProfile.authProvider == "anonymous"
                    val isPremium = uiState.userTier == com.mknlabs.expensetracker.models.UserTier.PREMIUM &&
                        !isAnonymous

                    ProfileAvatar(
                        gender = userProfile.gender,
                        size = 50.dp,
                        photoUri = userProfile.photoUri,
                        userTier = uiState.userTier,
                        isSyncing = uiState.isSyncing,
                        isAnonymous = isAnonymous,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        showBadge = isPremium,
                        badgeIconRes = R.drawable.ic_crown,
                        badgeContentDescription = stringResource(R.string.label_pro)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (appSettings != null) {
                AccountSetupCard(
                    userProfile = userProfile,
                    appSettings = appSettings,
                    onDismiss = {
                        scope.launch {
                            val randomDays = (3..4).random()
                            val nextShowTime = System.currentTimeMillis() + (randomDays * 24 * 60 * 60 * 1000L)
                            com.mknlabs.expensetracker.data.local.AppSettingsDataStore.updateAppSettings(context) {
                                it.copy(setupDismissedUntilMillis = nextShowTime)
                            }
                        }
                    },
                    onActionClick = onProfileClick
                )
            }

            if (smsSetupUiState.showSmsPermissionCard) {
                Spacer(modifier = Modifier.height(10.dp))
                SmsPermissionCard(
                    onOpenSettings = onSmsPermissionCardOpenSettings,
                    onDismiss = onSmsPermissionCardDismiss
                )
            }

            if (smsSetupUiState.showMiuiSetupCard) {
                Spacer(modifier = Modifier.height(10.dp))
                MiuiSmsSetupCard(
                    autostartUnknown = smsSetupUiState.miuiAutostartAllowed == null,
                    onOpenAppSettings = onMiuiSetupCardOpenAppSettings,
                    onBatterySettings = onMiuiSetupCardBatterySettings,
                    onDismiss = onMiuiSetupCardDismiss
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            StatsCard(
                totalBalance = uiState.totalBalance,
                previousMonthBalance = uiState.previousMonthBalance,
                income = uiState.totalIncome,
                expense = uiState.totalExpense,
                isBalanceHidden = uiState.isBalanceHidden,
                onToggleVisibility = onToggleBalanceVisibility
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SmallHomeCard(
                    title = stringResource(R.string.label_todays_spending),
                    value = uiState.todaySpending,
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1f),
                    onClick = onTodaySpendingClick
                )
                
                SmallHomeCard(
                    title = stringResource(R.string.title_savings_goals),
                    // Total saved across all active goals (the badge carries the count).
                    value = uiState.activeGoalsSaved,
                    icon = Icons.Default.Savings,
                    badgeCount = uiState.goalCount,
                    modifier = Modifier.weight(1f),
                    onClick = onGoalsClick
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Native Ad Placement
            AdContainer(isAdsEnabled = isAdsEnabled) {
                NativeAdCard(placement = AdPlacement.HOME_DASHBOARD)
            }

            Spacer(modifier = Modifier.height(15.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.label_recent_activities),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                var isPressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.9f else 1f,
                    animationSpec = tween(150)
                )
                val scope = rememberCoroutineScope()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .scale(scale)
                        .clickable {
                            isPressed = true
                            onViewAllClick()

                            scope.launch {
                                delay(150)
                                isPressed = false
                            }
                        }
                ) {
                    Text(
                        text = stringResource(id = R.string.label_view_all),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(
                    items = uiState.recentTransactions,
                    key = { card -> card.transaction.id },
                    contentType = { "transaction" }
                ) { card ->
                    TransactionCard(
                        note = card.note,
                        transactionDate = card.transactionDate,
                        transactionTime = card.transactionTime,
                        amount = card.amount,
                        icon = card.icon,
                        transactionTypeId = card.transactionTypeId,
                        paymentType = card.paymentType,
                        categoryLabel = card.categoryLabel,
                        showTypeLabel = uiState.customizationSettings.showIncomeExpenseLabels,
                        showTransactionDate = uiState.customizationSettings.showTransactionDate,
                        showPaymentMethod = uiState.customizationSettings.showPaymentMethod,
                        showTransactionTime = uiState.customizationSettings.showTransactionTime,
                        showCategoryIcon = uiState.customizationSettings.showCategoryIcon,
                        showCategoryLabel = uiState.customizationSettings.showCategoryLabel,
                        showNoteTooltip = isProUser,
                        onClick = { onTransactionClick(card.transaction) }
                    )
                }
            }
        }
    }
}


@Composable
fun AccountSetupCard(
    userProfile: UserProfile,
    appSettings: com.mknlabs.expensetracker.models.AppSettings,
    onDismiss: () -> Unit,
    onActionClick: () -> Unit
) {
    var showChecklist by remember { mutableStateOf(false) }

    val guestUserLabel = stringResource(id = R.string.placeholder_guest_user)
    val checklist = remember(userProfile, guestUserLabel) {
        listOf(
            Triple(R.string.label_checklist_full_name, userProfile.fullName.isNotEmpty() && userProfile.fullName != guestUserLabel, null),
            Triple(R.string.label_checklist_email, userProfile.emailAddress.isNotEmpty(), R.string.label_checklist_signin_to_add),
            Triple(R.string.label_checklist_dob, userProfile.dateOfBirthMillis != null && userProfile.dateOfBirthMillis != 0L, null),
            Triple(R.string.label_checklist_gender, userProfile.gender.isNotEmpty(), null),
            Triple(R.string.label_checklist_phone, userProfile.hasPhoneNumber, null)
        )
    }

    val score = remember(checklist) {
        checklist.count { it.second } * 20
    }

    val isComplete = score >= 100
    val isDismissed = System.currentTimeMillis() < appSettings.setupDismissedUntilMillis

    if (isComplete || isDismissed) return

    androidx.compose.material3.Card(
        onClick = onActionClick,
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.msg_discipline_score_title),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$score%",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.label_cancel_1),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            androidx.compose.material3.LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.msg_discipline_score_desc),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { showChecklist = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(id = R.string.title_setup_progress),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showChecklist) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showChecklist = false },
            title = {
                Text(
                    text = stringResource(R.string.title_setup_progress),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    checklist.forEach { (labelRes, isDone, subtitleRes) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(labelRes),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!isDone && subtitleRes != null) {
                                    Text(
                                        text = stringResource(subtitleRes),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showChecklist = false }) {
                    Text(stringResource(R.string.btn_got_it))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun SettingsButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = stringResource(id = R.string.desc_settings),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
    }
}




@Preview(showBackground = true)
@Composable
fun HomeScreenDarkPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        HomeScreenContent(
            userProfile = defaultUserProfile,
            uiState = HomeScreenUiState(),
            onViewAllClick = {},
            onTransactionClick = {},
            onProfileClick = {},
            onSettingsClick = {},
            onTodaySpendingClick = {},
            onGoalsClick = {},
            onToggleBalanceVisibility = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenLightPreview() {
    ExpenseTrackerTheme(darkTheme = false) {
        HomeScreenContent(
            userProfile = defaultUserProfile,
            uiState = HomeScreenUiState(),
            onViewAllClick = {},
            onTransactionClick = {},
            onProfileClick = {},
            onSettingsClick = {},
            onTodaySpendingClick = {},
            onGoalsClick = {},
            onToggleBalanceVisibility = {}
        )
    }
}
