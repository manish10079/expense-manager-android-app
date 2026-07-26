package com.mknlabs.expensetracker.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.viewmodels.MonetizationViewModel
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
    onGoalsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val homeViewModel: HomeViewModel = hiltViewModel()
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val isAdsEnabled by monetizationViewModel.isAdsEnabled.collectAsStateWithLifecycle()

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
        onToggleBalanceVisibility = homeViewModel::toggleBalanceVisibility
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
    onToggleBalanceVisibility: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatar(
                        gender = userProfile.gender,
                        size = 60.dp,
                        photoUri = userProfile.photoUri,
                        userTier = uiState.userTier,
                        isSyncing = uiState.isSyncing,
                        isAnonymous = userProfile.authProvider == "anonymous",
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.label_hi_val, uiState.greetingName),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(id = R.string.label_track_every_move_with_confiden),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                SettingsButton(onClick = onSettingsClick)
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
                    value = uiState.activeGoal?.let { "${(it.progress * 100).toInt()}%" } ?: "0%",
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
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = tween(150)
    )
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .size(52.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(brandGradient())
            .clickable {
                isPressed = true
                onClick()
                scope.launch {
                    delay(150)
                    isPressed = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = stringResource(id = R.string.desc_settings),
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(30.dp)
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
