@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.mknlabs.expensetracker.feature.budget.ui

import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas

import androidx.compose.material3.FilterChipDefaults

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet

import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedButton
import com.mknlabs.expensetracker.core.ui.components.AppOutlinedFieldDefaults
import com.mknlabs.expensetracker.core.ui.components.AppTextButton
import com.mknlabs.expensetracker.core.ui.components.ModernConfirmationDialog
import com.mknlabs.expensetracker.feature.funds.ui.FundsScreen
import com.mknlabs.expensetracker.feature.funds.ui.FundsViewModel
import com.mknlabs.expensetracker.models.BudgetPeriod
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.scale
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.menu
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.mknlabs.expensetracker.R
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.transactionList
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.TransactionCardCustomizationSettings
import com.mknlabs.expensetracker.models.InstallmentOccurrenceStatus
import com.mknlabs.expensetracker.models.InstallmentStatus
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.RecurringPlanEdit
import com.mknlabs.expensetracker.models.RecurringType
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.AppIconBox
import com.mknlabs.expensetracker.core.ui.components.CurrentPeriodIndicator
import com.mknlabs.expensetracker.core.ui.components.GatedAction
import com.mknlabs.expensetracker.core.ui.components.AnimatedTabSwitcher
import com.mknlabs.expensetracker.core.ui.components.AvatarRingBrush
import com.mknlabs.expensetracker.core.ui.components.tabBadgeCount
import com.mknlabs.expensetracker.core.ui.models.TabItem
import com.mknlabs.expensetracker.core.ui.components.PeriodChip
import com.mknlabs.expensetracker.core.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.core.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.core.ui.theme.budgetOnTrack
import com.mknlabs.expensetracker.core.ui.theme.budgetNearLimit
import com.mknlabs.expensetracker.core.ui.theme.budgetOver
import com.mknlabs.expensetracker.core.ui.theme.darkOnlyGradient
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmBodyDark
import com.mknlabs.expensetracker.core.ui.theme.ModernConfirmBodyLight
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.track
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.heroBloom
import com.mknlabs.expensetracker.core.ui.theme.categoryColor
import com.mknlabs.expensetracker.core.ui.theme.categoryTint
import com.mknlabs.expensetracker.core.ui.theme.categorySoft
import com.mknlabs.expensetracker.core.ui.theme.chip
import com.mknlabs.expensetracker.core.ui.theme.chipInkOff
import com.mknlabs.expensetracker.core.ui.theme.chipOutline
import com.mknlabs.expensetracker.core.ui.theme.chipSelected
import com.mknlabs.expensetracker.core.ui.theme.chipSelectedInk
import com.mknlabs.expensetracker.core.ui.theme.cta
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.switchOnThumb
import com.mknlabs.expensetracker.core.ui.theme.switchOnTick
import com.mknlabs.expensetracker.core.ui.theme.switchOnTrack
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.glassHeroGradient
import com.mknlabs.expensetracker.core.ui.theme.income

import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.datePickerSelectionToLocalDateTimestamp
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.utils.toMinorUnits
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.core.ui.components.rememberBindAddFabToScroll
import com.mknlabs.expensetracker.monetization.AdPlacement
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore


@Composable
fun BudgetAndRecurringScreen(
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    transactions: List<Transaction> = transactionList,
    availableCategories: List<CategoryType> = categoryMap.values.toList(),
    paymentMethods: List<PaymentType> = emptyList(),
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    recurringRules: List<RecurringTransactionRule> = emptyList(),
    monthStartDay: Int = 1,
    onDeleteRecurring: (String) -> Unit = {},
    onRecurringEnabledChange: (String, Boolean) -> Unit = { _, _ -> },
    onRecurringNotificationsEnabledChange: (String, Boolean) -> Unit = { _, _ -> },
    onUpdateRecurringRule: (String, RecurringFrequency, Int) -> Unit = { _, _, _ -> },
    onSaveRecurringPlan: (String, RecurringFrequency, RecurringPlanEdit) -> Unit = { _, _, _ -> },
    onConvertRecurringToRegular: (String) -> Unit = {},
    onPayInstallments: (List<String>) -> Unit = {},
    onSkipInstallment: (String) -> Unit = {},
    onUndoInstallment: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    isAdsEnabled: Boolean = false,
    isProUser: Boolean = false
) {
    val budgetViewModel: BudgetAndRecurringViewModel = hiltViewModel()

    LifecycleResumeEffect(Unit) {
        budgetViewModel.refreshCurrentPeriod()
        onPauseOrDispose { }
    }

    LaunchedEffect(transactions, availableCategories, currencyId, amountFormatPreferences, recurringRules, monthStartDay) {
        budgetViewModel.updateInputs(
            transactions = transactions,
            categories = availableCategories,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            recurringRules = recurringRules,
            monthStartDay = monthStartDay
        )
    }

    val uiState by budgetViewModel.uiState.collectAsStateWithLifecycle()
    val fundsViewModel: FundsViewModel = hiltViewModel()
    val fundsUiState by fundsViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BudgetAndRecurringContent(
        uiState = uiState,
        fundsCount = fundsUiState.funds.size,
        isAdsEnabled = isAdsEnabled,
        // Counts are a Pro perk; the route resolves that once and the content stays
        // a pure, previewable function.
        isProUser = isProUser,
        currencyId = currencyId,
        amountFormatPreferences = amountFormatPreferences,
        availableCategories = availableCategories,
        transactions = transactions,
        paymentMethods = paymentMethods,
        dateFormatPattern = dateFormatPattern,
        timeFormat = timeFormat,
        transactionCardCustomizationSettings = transactionCardCustomizationSettings,
        onDeleteRecurring = onDeleteRecurring,
        onRecurringEnabledChange = onRecurringEnabledChange,
        onRecurringNotificationsEnabledChange = onRecurringNotificationsEnabledChange,
        onUpdateRecurringRule = onUpdateRecurringRule,
        onSaveRecurringPlan = onSaveRecurringPlan,
        onConvertRecurringToRegular = onConvertRecurringToRegular,
        onPayInstallments = onPayInstallments,
        onSkipInstallment = onSkipInstallment,
        onUndoInstallment = onUndoInstallment,
        onBackClick = onBackClick,
        onSelectTab = { budgetViewModel.selectTab(it) },
        onSelectPeriod = { budgetViewModel.selectPeriod(it) },
        onSelectCustomMonth = { budgetViewModel.selectCustomMonth(it) },
        onSaveBudget = { budgetId, categoryIds, limit, name, period ->
            budgetViewModel.saveBudget(budgetId, categoryIds, limit, name, period)
        },
        onDeleteBudget = { budgetId -> budgetViewModel.deleteBudget(budgetId) },
        onCopyAllBudgets = {
            budgetViewModel.copyAllPreviousMonthBudgets()
            Toast.makeText(context, R.string.msg_copy_all_budgets_success, Toast.LENGTH_SHORT).show()
        },
        onCopySelectedBudgets = { budgetIds ->
            budgetViewModel.copySelectedPreviousMonthBudgets(budgetIds)
            Toast.makeText(context, R.string.msg_copy_selected_budgets_success, Toast.LENGTH_SHORT).show()
        }
    )
}

@Composable
internal fun BudgetAndRecurringContent(
    uiState: BudgetAndRecurringScreenUiState,
    fundsCount: Int = 0,
    isAdsEnabled: Boolean,
    isProUser: Boolean = false,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    availableCategories: List<CategoryType>,
    transactions: List<Transaction>,
    paymentMethods: List<PaymentType> = emptyList(),
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    timeFormat: String = DEFAULT_TIME_FORMAT,
    transactionCardCustomizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings(),
    onDeleteRecurring: (String) -> Unit,
    onRecurringEnabledChange: (String, Boolean) -> Unit,
    onRecurringNotificationsEnabledChange: (String, Boolean) -> Unit,
    onUpdateRecurringRule: (String, RecurringFrequency, Int) -> Unit,
    onSaveRecurringPlan: (String, RecurringFrequency, RecurringPlanEdit) -> Unit,
    onConvertRecurringToRegular: (String) -> Unit,
    onPayInstallments: (List<String>) -> Unit = {},
    onSkipInstallment: (String) -> Unit = {},
    onUndoInstallment: (String) -> Unit = {},
    onBackClick: () -> Unit,
    onSelectTab: (BudgetTab) -> Unit,
    onSelectPeriod: (BudgetPeriodFilter) -> Unit,
    onSelectCustomMonth: (Long) -> Unit,
    onSaveBudget: (String?, List<Int>, Double, String, BudgetPeriod) -> Unit,
    onDeleteBudget: (String) -> Unit,
    onCopyAllBudgets: () -> Unit,
    onCopySelectedBudgets: (List<String>) -> Unit
) {
    var isMonthPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isBudgetEditorVisible by rememberSaveable { mutableStateOf(false) }
    var editingBudgetId by rememberSaveable { mutableStateOf<String?>(null) }
    var budgetEditorSessionKey by rememberSaveable { mutableStateOf(0L) }
    var pendingDeleteBudgetId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteRecurringId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingRecurringRule by remember { mutableStateOf<BudgetRecurringExpenseUi?>(null) }
    var pendingMuteRecurringId by rememberSaveable { mutableStateOf<String?>(null) }
    var muteDialogDismissed by rememberSaveable { mutableStateOf(false) }
    var infoBudgetId by rememberSaveable { mutableStateOf<String?>(null) }
    var isGroupInfoSheetVisible by rememberSaveable { mutableStateOf(false) }
    val groupInfoSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Id of the EMI rule whose installment ledger is open (tap-through from a card).
    var ledgerRuleId by rememberSaveable { mutableStateOf<String?>(null) }
    val ledgerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isCopySheetVisible by rememberSaveable { mutableStateOf(false) }
    var pendingCopyRequest by remember { mutableStateOf<PendingBudgetCopy?>(null) }
    // IDs currently playing their exit animation before the real delete fires.
    val deletingRecurringIds = remember { mutableStateSetOf<String>() }


    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    // Per-tab scroll states for the two pager pages (Budgets / Recurring). The
    // current page's scroll direction drives the standalone add FAB's auto-hide
    // on compact portrait.
    val budgetsListState = rememberLazyListState()
    val recurringListState = rememberLazyListState()
    rememberBindAddFabToScroll(
        if (pagerState.currentPage == 0) budgetsListState else recurringListState
    )
    val enter = rememberSectionEnterAlphas(4)

    // Sync ViewModel tab state with PagerState
    LaunchedEffect(pagerState.currentPage) {
        val tab = when (pagerState.currentPage) {
            0 -> BudgetTab.Budgets
            1 -> BudgetTab.Recurring
            else -> BudgetTab.Funds
        }
        if (uiState.selectedTab != tab) {
            onSelectTab(tab)
        }
    }

    // Sync PagerState with ViewModel tab state (for programmatic clicks)
    LaunchedEffect(uiState.selectedTab) {
        val page = when (uiState.selectedTab) {
            BudgetTab.Budgets -> 0
            BudgetTab.Recurring -> 1
            BudgetTab.Funds -> 2
        }
        if (pagerState.currentPage != page) {
            pagerState.animateScrollToPage(page)
        }
    }

    val expenseCategories = remember(availableCategories) {
        availableCategories
            .filter { it.transactionTypeId != 1 }
            .sortedBy { it.name.lowercase() }
    }
    val expenseTransactions = remember(transactions) {
        transactions
            .filter { it.transactionTypeId != 1 }
            .sortedByDescending { it.createdAt }
    }
    val editingBudget = uiState.categoryBudgets.firstOrNull { it.id == editingBudgetId }
    val pendingDeleteBudget = uiState.categoryBudgets.firstOrNull { it.id == pendingDeleteBudgetId }
    val pendingDeleteRecurring = uiState.recurringExpenses.firstOrNull { it.id == pendingDeleteRecurringId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimens.ScreenPadding, top = Dimens.HeaderSpacing, end = Dimens.ScreenPadding)
                    .alpha(enter[0])
            ) {
                AppHeader(title = stringResource(id = R.string.title_budget_recurring), onBackClick = onBackClick)
            }

            Box(modifier = Modifier.padding(horizontal = Dimens.ScreenPadding).alpha(enter[1])) {
                CurrentPeriodIndicator(
                    startMillis = uiState.currentPeriodStartMillis,
                    endMillis = uiState.currentPeriodEndMillis,
                    monthStartDay = uiState.monthStartDay,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Box(modifier = Modifier.padding(horizontal = Dimens.ScreenPadding).alpha(enter[2])) {
                AnimatedTabSwitcher(
                    items = listOf(
                        TabItem(
                            id = BudgetTab.Budgets,
                            label = stringResource(id = R.string.label_tab_budgets),
                            badgeCount = tabBadgeCount(
                                count = uiState.categoryBudgets.size,
                                isSelected = uiState.selectedTab == BudgetTab.Budgets,
                                isProUser = isProUser
                            )
                        ),
                        TabItem(
                            id = BudgetTab.Recurring,
                            label = stringResource(id = R.string.label_tab_recurring),
                            badgeCount = tabBadgeCount(
                                count = uiState.recurringExpenses.size,
                                isSelected = uiState.selectedTab == BudgetTab.Recurring,
                                isProUser = isProUser
                            )
                        ),
                        TabItem(
                            id = BudgetTab.Funds,
                            label = stringResource(id = R.string.label_tab_funds),
                            badgeCount = tabBadgeCount(
                                count = fundsCount,
                                isSelected = uiState.selectedTab == BudgetTab.Funds,
                                isProUser = isProUser
                            )
                        )
                    ),
                    selectedItemId = uiState.selectedTab,
                    onItemSelected = onSelectTab,
                    containerColor = if (MaterialTheme.colorScheme.isDark) BudgetCardFillDark else BudgetCardFillLight
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .alpha(enter[3]),
                verticalAlignment = Alignment.Top
            ) { page ->
                // The Funds tab owns the whole page: it is a feature screen of its own
                // (list, detail, breakdown) rather than another list of budget cards, so it
                // is composed here instead of being another branch of the LazyColumn below.
                if (page == 2) {
                    FundsScreen(
                        currencyId = currencyId,
                        amountFormatPreferences = amountFormatPreferences,
                        categories = availableCategories,
                        paymentMethods = paymentMethods,
                        dateFormatPattern = dateFormatPattern,
                        timeFormat = timeFormat,
                        transactionCardCustomizationSettings = transactionCardCustomizationSettings,
                        isProUser = isProUser
                    )
                    return@HorizontalPager
                }
                // Both pages open with content rather than a heading, so they share one inset.
                // It is tighter than the 18.dp that separates the cards below: on the budgets
                // page this is the gap to the Budgets/Recurring switcher above, two blocks of
                // the same weight that read as a pair.
                LazyColumn(
                    state = if (page == 0) budgetsListState else recurringListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(start = Dimens.ScreenPadding, top = 10.dp, end = Dimens.ScreenPadding, bottom = 126.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    if (page == 0) {
                        // TAB 1: BUDGETS
                        item {
                            GatedAction(
                                feature = Feature.BUDGET_CUSTOM_MONTH,
                                onAction = { isMonthPickerVisible = true }
                            ) { status, onCustomMonthClick ->
                                BudgetPeriodRow(
                                    selectedPeriod = uiState.selectedPeriod,
                                    thisMonthLabel = uiState.thisMonthChipLabel,
                                    lastMonthLabel = uiState.lastMonthChipLabel,
                                    isCustomMonthLocked = status !is AccessStatus.Granted,
                                    onPeriodSelected = { period ->
                                        if (period == BudgetPeriodFilter.CustomMonth) {
                                            onCustomMonthClick()
                                        } else {
                                            onSelectPeriod(period)
                                        }
                                    }
                                )
                            }
                        }

                        item {
                            GatedAction(
                                feature = Feature.BUDGET_COPY_PREVIOUS_MONTH,
                                displayName = stringResource(id = R.string.title_copy_previous_month_budgets),
                                onAction = { isCopySheetVisible = true }
                            ) { status, onCopyClick ->
                                BudgetSummaryCard(
                                    summary = uiState.summary,
                                    showCopyPrevious = uiState.selectedPeriod == BudgetPeriodFilter.ThisMonth,
                                    copyLocked = status !is AccessStatus.Granted,
                                    onCopyPrevious = onCopyClick,
                                    addTitle = if (uiState.isMonthLocked) stringResource(id = R.string.label_history_locked) else stringResource(id = R.string.title_add_budget),
                                    addEnabled = uiState.canAddBudget,
                                    onAddBudget = {
                                        editingBudgetId = null
                                        budgetEditorSessionKey = System.currentTimeMillis()
                                        isBudgetEditorVisible = true
                                    }
                                )
                            }
                        }

                        // Guarded by the flag as well as by AdContainer, for the same reason as the
                        // per-rule slots on the recurring tab: a hidden slot does collapse to zero
                        // height, but an emitted lazy item still costs the list's item spacing on
                        // both sides, which doubled the gap between the summary card and the
                        // heading below it for every ad-free user.
                        if (isAdsEnabled) {
                            item {
                                AdContainer(isAdsEnabled = isAdsEnabled) {
                                    NativeAdCard(placement = AdPlacement.BUDGET_CALENDAR)
                                }
                            }
                        }

                        item { SectionTitle(title = stringResource(id = R.string.title_budget_category)) }

                        if (uiState.categoryBudgets.isEmpty()) {
                            item {
                                EmptySectionCard(
                                    message = uiState.emptyCategoryMessage?.asString()
                                        ?: stringResource(id = R.string.msg_no_category_budget_data)
                                )
                            }
                        } else {
                            items(uiState.categoryBudgets, key = { it.id }) { budget ->
                                CategoryBudgetCard(
                                    budget = budget,
                                    onEditClick = {
                                        editingBudgetId = budget.id
                                        budgetEditorSessionKey = System.currentTimeMillis()
                                        isBudgetEditorVisible = true
                                    },
                                    onDeleteClick = {
                                        pendingDeleteBudgetId = budget.id
                                    },
                                    onInfoClick = {
                                        infoBudgetId = budget.id
                                        isGroupInfoSheetVisible = true
                                    }
                                )
                            }
                        }

                        item {
                            AdContainer(isAdsEnabled = isAdsEnabled) {
                                NativeAdCard(placement = AdPlacement.BUDGET_CALENDAR)
                            }
                        }
                    } else {
                        // TAB 2: RECURRING
                        // No section heading: the tab above is labelled "Recurring" and carries
                        // the count, so a "Recurring Expenses" title directly under it only
                        // repeated the tab and separated nothing. The budget tab's heading stays
                        // because it sits below the summary card and names the breakdown.
                        if (uiState.recurringExpenses.isEmpty()) {
                            item {
                                EmptySectionCard(
                                    message = uiState.emptyRecurringMessage?.asString()
                                        ?: stringResource(id = R.string.msg_no_recurring_items)
                                )
                            }
                            item {
                                AdContainer(isAdsEnabled = isAdsEnabled) {
                                    NativeAdCard(placement = AdPlacement.BUDGET_CALENDAR)
                                }
                            }
                        } else {
                            uiState.recurringExpenses.forEach { expense ->
                                item(key = expense.id) {
                                    val isBeingDeleted = expense.id in deletingRecurringIds
                                    AnimatedVisibility(
                                        visible = !isBeingDeleted,
                                        modifier = Modifier.animateItem(
                                            fadeInSpec = null,
                                            fadeOutSpec = tween(durationMillis = 300),
                                            placementSpec = spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        ),
                                        enter = expandVertically(
                                            animationSpec = tween(durationMillis = 260)
                                        ) + fadeIn(animationSpec = tween(durationMillis = 260)),
                                        exit = shrinkVertically(
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        ) + fadeOut(animationSpec = tween(durationMillis = 220))
                                    ) {
                                        RecurringExpenseCard(
                                            expense = expense,
                                            onEnabledChange = { enabled ->
                                                onRecurringEnabledChange(expense.id, enabled)
                                            },
                                            onNotificationsEnabledChange = { enabled ->
                                                if (!enabled && !muteDialogDismissed) {
                                                    pendingMuteRecurringId = expense.id
                                                } else {
                                                    onRecurringNotificationsEnabledChange(expense.id, enabled)
                                                }
                                            },
                                            onEditClick = { editingRecurringRule = expense },
                                            onDeleteClick = {
                                                pendingDeleteRecurringId = expense.id
                                            },
                                            onLedgerClick = if (expense.isInstallment && expense.slots.isNotEmpty()) {
                                                { ledgerRuleId = expense.id }
                                            } else {
                                                null
                                            }
                                        )
                                    }
                                    // Fire the real repository delete once the card has
                                    // finished its exit animation and collapsed to nothing.
                                    LaunchedEffect(isBeingDeleted) {
                                        if (isBeingDeleted) {
                                            kotlinx.coroutines.delay(350)
                                            onDeleteRecurring(expense.id)
                                            deletingRecurringIds.remove(expense.id)
                                        }
                                    }
                                }
                                // Guarded by the flag as well as by AdContainer. A hidden slot does
                                // collapse to zero height as AdContainer documents, but an emitted
                                // lazy item still costs the list's item spacing on both of its sides,
                                // so every ad-free user got a second 18.dp stacked onto the gap
                                // between two rule cards. The item now simply does not exist for
                                // them, and is unchanged for everyone seeing ads.
                                if (isAdsEnabled) {
                                    item(key = "ad_${expense.id}") {
                                        val isBeingDeleted = expense.id in deletingRecurringIds
                                        AnimatedVisibility(
                                            visible = !isBeingDeleted,
                                            exit = shrinkVertically(
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                    stiffness = Spring.StiffnessMedium
                                                )
                                            ) + fadeOut(animationSpec = tween(durationMillis = 220))
                                        ) {
                                            AdContainer(isAdsEnabled = isAdsEnabled) {
                                                NativeAdCard(placement = AdPlacement.BUDGET_CALENDAR)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isMonthPickerVisible) {
        WheelDateTimePickerModal(
            mode = WheelPickerMode.MONTH_YEAR,
            initialStartMillis = uiState.customMonthStart,
            onDismissRequest = { isMonthPickerVisible = false },
            onConfirm = { pickedDateMillis, _ ->
                val adjustedTimestamp = datePickerSelectionToLocalDateTimestamp(
                    selectedDateMillis = pickedDateMillis,
                    referenceTimestamp = uiState.customMonthStart,
                    isInputUtc = false
                )
                onSelectCustomMonth(adjustedTimestamp)
                isMonthPickerVisible = false
            }
        )
    }

    if (isBudgetEditorVisible) {
        BudgetEditorSheet(
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            monthLabel = uiState.summary.monthLabel,
            expenseCategories = expenseCategories,
            existingBudget = editingBudget,
            categoryTrackedMap = uiState.categoryTrackedMap,
            sessionKey = budgetEditorSessionKey,
            onDismiss = {
                isBudgetEditorVisible = false
                editingBudgetId = null
            },
            onSave = { categoryIds, limitAmount, name, period ->
                onSaveBudget(editingBudget?.id, categoryIds, limitAmount, name, period)
                isBudgetEditorVisible = false
                editingBudgetId = null
            }
        )
    }

    val infoBudget = uiState.categoryBudgets.firstOrNull { it.id == infoBudgetId }
    if (isGroupInfoSheetVisible && infoBudget != null) {
        BudgetGroupInfoSheet(
            budget = infoBudget,
            categories = availableCategories,
            sheetState = groupInfoSheetState,
            onDismiss = {
                isGroupInfoSheetVisible = false
                infoBudgetId = null
            }
        )
    }

    if (pendingDeleteBudget != null) {
        DeleteBudgetDialog(
            budgetName = pendingDeleteBudget.title,
            onDismiss = { pendingDeleteBudgetId = null },
            onConfirm = {
                onDeleteBudget(pendingDeleteBudget.id)
                pendingDeleteBudgetId = null
            }
        )
    }

    if (pendingDeleteRecurring != null) {
        DeleteRecurringDialog(
            recurringName = pendingDeleteRecurring.title,
            onDismiss = { pendingDeleteRecurringId = null },
            onConfirm = {
                // Stage for animated exit â€” the actual delete fires once the
                // AnimatedVisibility exit animation completes (see list below).
                deletingRecurringIds.add(pendingDeleteRecurring.id)
                pendingDeleteRecurringId = null
            }
        )
    }

    if (pendingMuteRecurringId != null) {
        val pendingMuteExpense = uiState.recurringExpenses.firstOrNull { it.id == pendingMuteRecurringId }
        MuteRecurringDialog(
            ruleName = pendingMuteExpense?.title ?: "",
            onDismiss = { pendingMuteRecurringId = null },
            onConfirm = { dontShowAgain ->
                onRecurringNotificationsEnabledChange(pendingMuteRecurringId!!, false)
                if (dontShowAgain) {
                    muteDialogDismissed = true
                }
                pendingMuteRecurringId = null
            }
        )
    }

    if (editingRecurringRule != null) {
        val rule = editingRecurringRule!!
        RecurringRuleEditorModal(
            rule = rule,
            onDismiss = { editingRecurringRule = null },
            onSave = { frequency, installments ->
                onUpdateRecurringRule(rule.id, frequency, installments)
                editingRecurringRule = null
            },
            onSavePlan = { frequency, plan ->
                onSaveRecurringPlan(rule.id, frequency, plan)
                editingRecurringRule = null
            },
            onConvertToRegular = {
                onConvertRecurringToRegular(rule.id)
                editingRecurringRule = null
            }
        )
    }

    // A plan converted back to REGULAR (or deleted) while its ledger is open has
    // no slots left to show â€” the sheet closes with it.
    val ledgerExpense = uiState.recurringExpenses
        .firstOrNull { it.id == ledgerRuleId && it.isInstallment }
    if (ledgerExpense != null) {
        InstallmentLedgerSheet(
            expense = ledgerExpense,
            sheetState = ledgerSheetState,
            onDismiss = { ledgerRuleId = null },
            onPay = onPayInstallments,
            onSkip = onSkipInstallment,
            onUndo = onUndoInstallment
        )
    }

    if (isCopySheetVisible) {
        val currentMonthHasBudgets =
            uiState.selectedPeriod == BudgetPeriodFilter.ThisMonth && uiState.categoryBudgets.isNotEmpty()
        CopyPreviousMonthBudgetsSheet(
            candidates = uiState.previousMonthBudgets,
            availableCategories = availableCategories,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            previousMonthLabel = uiState.previousMonthLabel,
            currentMonthLabel = uiState.summary.monthLabel,
            onDismiss = { isCopySheetVisible = false },
            onCreateBudget = {
                isCopySheetVisible = false
                editingBudgetId = null
                budgetEditorSessionKey = System.currentTimeMillis()
                isBudgetEditorVisible = true
            },
            onCopyAll = {
                if (currentMonthHasBudgets) {
                    pendingCopyRequest = PendingBudgetCopy(
                        mode = BudgetCopyMode.All,
                        budgetIds = uiState.previousMonthBudgets.map { it.id }
                    )
                } else {
                    isCopySheetVisible = false
                    onCopyAllBudgets()
                }
            },
            onCopySelected = { ids ->
                if (currentMonthHasBudgets) {
                    pendingCopyRequest = PendingBudgetCopy(
                        mode = BudgetCopyMode.Selected,
                        budgetIds = ids
                    )
                } else {
                    isCopySheetVisible = false
                    onCopySelectedBudgets(ids)
                }
            }
        )
    }

    val pendingCopy = pendingCopyRequest
    if (pendingCopy != null) {
        ConfirmCopyBudgetsDialog(
            monthLabel = uiState.summary.monthLabel,
            onDismiss = { pendingCopyRequest = null },
            onConfirm = {
                pendingCopyRequest = null
                isCopySheetVisible = false
                when (pendingCopy.mode) {
                    BudgetCopyMode.All -> onCopyAllBudgets()
                    BudgetCopyMode.Selected -> onCopySelectedBudgets(pendingCopy.budgetIds)
                }
            }
        )
    }
}


