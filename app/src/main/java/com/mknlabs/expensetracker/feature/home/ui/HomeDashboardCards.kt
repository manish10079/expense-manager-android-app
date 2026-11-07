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

@Composable
internal fun HomeStatsSection(
    userProfile: UserProfile,
    appSettings: com.mknlabs.expensetracker.models.AppSettings?,
    uiState: HomeScreenUiState,
    isAdsEnabled: Boolean,
    onProfileClick: () -> Unit,
    onTodaySpendingClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onToggleBalanceVisibility: () -> Unit,
    smsSetupUiState: SmsSetupUiState,
    showMissingPermissionsCard: Boolean = false,
    missingNotifications: Boolean = false,
    missingSms: Boolean = false,
    onMissingPermissionsOpenSettings: () -> Unit = {},
    onMissingPermissionsDismiss: () -> Unit = {},
    onSmsPermissionCardOpenSettings: () -> Unit,
    onSmsPermissionCardDismiss: () -> Unit,
    onMiuiSetupCardOpenAppSettings: () -> Unit,
    onMiuiSetupCardBatterySettings: () -> Unit,
    onMiuiSetupCardDismiss: () -> Unit,
    onPeriodChanged: (CashFlowPeriod) -> Unit = {},
    // Wide windows (tablets/foldables/desktop) render the tall media-first native ad;
    // phones keep the compact banner row.
    isWide: Boolean = false,
    cashFlowEnter: Float = 1f,
    miniCardsEnter: Float = 1f
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = MaterialTheme.colorScheme.isDark

    // Whether this section opens with a card above the stats. The spacer below uses it
    // to avoid adding a gap that is only needed when something is actually there.
    val hasSetupCard = appSettings != null ||
        showMissingPermissionsCard ||
        smsSetupUiState.showSmsPermissionCard ||
        smsSetupUiState.showMiuiSetupCard

    Column(modifier = Modifier.alpha(cashFlowEnter)) {
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
            onActionClick = onProfileClick,
            onVerifyEmail = onProfileClick
        )
    }

    if (showMissingPermissionsCard) {
        Spacer(modifier = Modifier.height(10.dp))
        MissingPermissionsCard(
            missingNotifications = missingNotifications,
            missingSms = missingSms,
            onOpenSettings = onMissingPermissionsOpenSettings,
            onDismiss = onMissingPermissionsDismiss,
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

    // Only when a setup/hint card was actually drawn above: those cards already carry
    // their own 10.dp gap, and with none of them the 7.dp spacer above the section is
    // the entire gap between the header row and the Cash Flow card.
    if (hasSetupCard) {
        Spacer(modifier = Modifier.height(10.dp))
    }

    CurrentPeriodIndicator(
        startMillis = uiState.currentPeriodStartMillis,
        endMillis = uiState.currentPeriodEndMillis,
        monthStartDay = uiState.monthStartDay,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    CashFlowStatsCard(
        expense = uiState.totalExpense,
        income = uiState.totalIncome,
        netBalance = uiState.totalBalance,
        isBalanceHidden = uiState.isBalanceHidden,
        onToggleVisibility = onToggleBalanceVisibility,
        selectedPeriod = uiState.selectedPeriod,
        onPeriodChanged = onPeriodChanged,
        yearExpense = uiState.yearTotalExpense,
        yearIncome = uiState.yearTotalIncome,
        yearNetBalance = uiState.yearTotalBalance
    )
    }

    Spacer(modifier = Modifier.height(7.dp))

    Column(modifier = Modifier.alpha(miniCardsEnter)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SmallHomeCard(
            title = stringResource(R.string.label_spent_today),
            value = uiState.todaySpending,
            icon = PhosphorIcons.Regular.CalendarBlank,
            modifier = Modifier.weight(1f),
            onClick = onTodaySpendingClick,
            iconTileColor = if (isDark) SpentTodayTileDark else SpentTodayTileLight,
            iconTileBorderColor =
                if (isDark) SpentTodayTileBorderDark else SpentTodayTileBorderLight,
            iconTileShape = RoundedCornerShape(12.dp),
            iconTint = if (isDark) SpentTodayIconDark else SpentTodayIconLight,
            containerColor = if (isDark) SpentTodayCardDark else SpentTodayCardLight,
            borderColor =
                if (isDark) SpentTodayCardBorderDark else SpentTodayCardBorderLight
        )

        SmallHomeCard(
            title = stringResource(R.string.title_my_goals),
            // Total saved across all active goals (the badge carries the count).
            value = uiState.activeGoalsSaved,
            icon = PhosphorIcons.Regular.PiggyBank,
            badgeCount = uiState.goalCount,
            modifier = Modifier.weight(1f),
            onClick = onGoalsClick,
            iconTileColor = if (isDark) GoalsTileDark else GoalsTileLight,
            iconTileBorderColor =
                if (isDark) GoalsTileBorderDark else GoalsTileBorderLight,
            iconTileShape = RoundedCornerShape(12.dp),
            iconTint = if (isDark) GoalsIconDark else GoalsIconLight,
            containerColor = if (isDark) GoalsCardDark else GoalsCardLight,
            borderColor =
                if (isDark) GoalsCardBorderDark else GoalsCardBorderLight,
            badgeColor = GoalsBadgeFill,
            badgeContentColor = GoalsBadgeInk,
            badgeRingColor = if (isDark) GoalsBadgeRingDark else GoalsBadgeRingLight
        )
    }

    // Paired with the spacer before Recent Activities, this keeps the gap from the
    // Spent Today / My Goals row down to that heading tight, whether or not an ad
    // card occupies the slot between them.
    Spacer(modifier = Modifier.height(8.dp))

    // Ad slot: free users see the native ad (tall media-first card on wide windows,
    // compact row on phones). Ad-free/premium users get the Upcoming Recurring card
    // strictly on tablets and foldables (width >= 600dp / isWide).
    if (isAdsEnabled) {
        AdContainer(isAdsEnabled = true) {
            NativeAdCard(
                placement = AdPlacement.HOME_DASHBOARD,
                large = isWide
            )
        }
    } else if (isWide) {
        UpcomingRecurringCard(
            upcomingExpenses = uiState.upcomingRecurring
        )
    }
    }
}

/**
 * Premium (ad-free) replacement for the home ad slot: shows the latest 2 upcoming active recurring transactions.
 */
@Composable
internal fun UpcomingRecurringCard(
    upcomingExpenses: List<UpcomingRecurringUi>
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        // 30dp in dark, where this card is a brand gradient; the standard card in light.
        shape = if (MaterialTheme.colorScheme.isDark) {
            RoundedCornerShape(30.dp)
        } else {
            AppCardDefaults.shape()
        },
        brush = darkOnlyGradient(standardCardGradient()),
        contentPadding = PaddingValues(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.title_upcoming_recurring),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (upcomingExpenses.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.label_no_upcoming_recurring),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                upcomingExpenses.forEachIndexed { index, expense ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = expense.icon,
                                    contentDescription = expense.categoryLabel,
                                    tint = MaterialTheme.colorScheme.accentInk,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = expense.title,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = expense.dueLabel.asString(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = expense.dueAmountLabel,
                            color = MaterialTheme.colorScheme.expense,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    if (index < upcomingExpenses.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
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
    onActionClick: () -> Unit,
    onVerifyEmail: () -> Unit = {}
) {
    var showChecklist by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val guestUserLabel = stringResource(id = R.string.placeholder_guest_user)
    val isEmailVerified = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.isEmailVerified == true
    val isGoogleAccount = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.providerData?.any { it.providerId == "google.com" } == true
    // Google accounts are implicitly verified
    val emailVerified = isEmailVerified || isGoogleAccount

    val checklist = remember(userProfile, guestUserLabel, emailVerified) {
        listOf(
            Triple(R.string.label_checklist_full_name, userProfile.fullName.isNotEmpty() && userProfile.fullName != guestUserLabel, null),
            Triple(R.string.label_checklist_email, userProfile.emailAddress.isNotEmpty(), R.string.label_checklist_signin_to_add),
            Triple(R.string.label_checklist_dob, userProfile.dateOfBirthMillis != null && userProfile.dateOfBirthMillis != 0L, null),
            Triple(R.string.label_checklist_gender, userProfile.gender.isNotEmpty(), null),
            Triple(R.string.label_checklist_phone, userProfile.hasPhoneNumber, null),
            Triple(R.string.label_checklist_email_verified, emailVerified, R.string.label_checklist_verify_email)
        )
    }

    val score = remember(checklist) {
        checklist.count { it.second } * 100 / checklist.size
    }

    val isComplete = score >= 100
    val isDismissed = System.currentTimeMillis() < appSettings.setupDismissedUntilMillis

    if (isComplete || isDismissed) return

    AppCard(
        onClick = onActionClick,
        modifier = Modifier.fillMaxWidth(),
        colors = AppCardDefaults.tintedColors()
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
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$score%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.accentInk
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.X,
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
                color = MaterialTheme.colorScheme.accentInk,
                trackColor = MaterialTheme.colorScheme.track
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
                        imageVector = PhosphorIcons.Regular.Info,
                        contentDescription = stringResource(id = R.string.title_setup_progress),
                        tint = MaterialTheme.colorScheme.accentInk,
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
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    checklist.forEachIndexed { index, (labelRes, isDone, subtitleRes) ->
                        val isEmailVerifyItem = labelRes == R.string.label_checklist_email_verified
                        val isClickable = !isDone && isEmailVerifyItem

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isClickable) {
                                        Modifier.clickable {
                                            showChecklist = false
                                            // Send verification email
                                            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                                            android.widget.Toast.makeText(
                                                context,
                                                context.getString(R.string.toast_verification_email_sent),
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                            onVerifyEmail()
                                        }
                                    } else Modifier
                                )
                        ) {
                            Icon(
                                imageVector = if (isDone) PhosphorIcons.Fill.CheckCircle else PhosphorIcons.Regular.Circle,
                                contentDescription = null,
                                tint = if (isDone) MaterialTheme.colorScheme.accentInk else MaterialTheme.colorScheme.onSurfaceVariant,
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
                                        color = MaterialTheme.colorScheme.accentInk
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                AppTextButton(onClick = { showChecklist = false }) {
                    Text(stringResource(R.string.btn_got_it))
                }
            },
            containerColor = MaterialTheme.colorScheme.sheet,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        )
    }
}
