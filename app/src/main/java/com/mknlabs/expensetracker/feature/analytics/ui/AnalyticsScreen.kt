package com.mknlabs.expensetracker.feature.analytics.ui

import com.mknlabs.expensetracker.core.ui.theme.appScreenBackground
import com.mknlabs.expensetracker.core.ui.theme.sheet
import com.mknlabs.expensetracker.core.ui.theme.textTertiary

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.transactionList
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.utils.UiText
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.utils.formatCurrencyValue
import com.mknlabs.expensetracker.core.ui.components.AppCard
import com.mknlabs.expensetracker.core.ui.components.AppCardColors
import com.mknlabs.expensetracker.core.ui.components.AppCardDefaults
import com.mknlabs.expensetracker.core.ui.components.rememberBindAddFabToScroll
import com.mknlabs.expensetracker.core.ui.components.CurrentPeriodIndicator
import com.mknlabs.expensetracker.core.ui.components.hasCurrentPeriodIndicator
import com.mknlabs.expensetracker.core.ui.components.PeriodChip
import com.mknlabs.expensetracker.core.ui.components.PeriodChipDefaults

import com.adamglin.phosphoricons.regular.Wallet
import com.adamglin.phosphoricons.regular.TrendUp
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.income
import java.util.Locale
import com.mknlabs.expensetracker.core.ui.components.DialogModeOption
import com.mknlabs.expensetracker.core.ui.components.DialogModeSelector
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.Fund
import com.mknlabs.expensetracker.models.Tag
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.core.ui.components.rememberSectionEnterAlphas
import com.mknlabs.expensetracker.core.ui.components.AppHeader
import com.mknlabs.expensetracker.core.ui.components.GatedAction
import com.mknlabs.expensetracker.core.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.core.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.monetization.AccessStatus
import android.os.Build
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import com.mknlabs.expensetracker.core.ui.theme.income
import com.mknlabs.expensetracker.core.ui.theme.expense
import com.mknlabs.expensetracker.core.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.core.ui.theme.Dimens
import com.mknlabs.expensetracker.core.ui.theme.accentInk
import com.mknlabs.expensetracker.core.ui.theme.accentSoft
import com.mknlabs.expensetracker.core.ui.theme.chipInkOff

import com.mknlabs.expensetracker.core.ui.theme.chipSelected
import com.mknlabs.expensetracker.core.ui.theme.flatCard
import com.mknlabs.expensetracker.core.ui.theme.brandGradient
import com.mknlabs.expensetracker.core.ui.theme.onBrandGradient
import com.mknlabs.expensetracker.core.ui.theme.categoryColor
import com.mknlabs.expensetracker.core.ui.theme.paymentColor
import com.mknlabs.expensetracker.core.ui.theme.glassHeroGradient
import com.mknlabs.expensetracker.core.ui.theme.heroBloom
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.textTertiary
import com.mknlabs.expensetracker.core.ui.theme.track
import com.mknlabs.expensetracker.core.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.core.ui.theme.featureGateLock
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.profileSheet

import com.mknlabs.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mknlabs.expensetracker.utils.formatDate
import com.mknlabs.expensetracker.core.ui.components.TransactionCard
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.core.ui.components.AdContainer
import com.mknlabs.expensetracker.core.ui.components.NativeAdCard
import com.mknlabs.expensetracker.core.ui.adaptive.LocalAppWindowInfo
import com.mknlabs.expensetracker.monetization.AdPlacement
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin


/**
 * What the analytics hero is showing.
 *
 * These labels are the title-case forms - "Expense", "Income", "Both" - because they
 * are the options of a changer the user reads as three words, and the upper-case
 * [R.string.label_expense] / [R.string.label_income] mixed with the title-case
 * [R.string.label_both] read as two different kinds of label in one control. The
 * upper-case pair stays as it is for the transaction pills and the legends that were
 * built around it.
 */
enum class HeroDisplayMode(val labelRes: Int) {
    EXPENSE(R.string.label_expense_title),
    INCOME(R.string.label_income_title),
    BOTH(R.string.label_both)
}

@Composable
fun AnalyticsScreen(
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    dateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN,
    transactions: List<Transaction> = transactionList,
    categories: List<CategoryType> = emptyList(),
    paymentMethods: List<PaymentType> = emptyList(),
    monthStartDay: Int = 1,
    onBackClick: () -> Unit = {},
    analyticsViewModel: AnalyticsViewModel = hiltViewModel(),
    tagLinksViewModel: AnalyticsTagLinksViewModel = hiltViewModel(),
    isAdsEnabled: Boolean = false,
    isProUser: Boolean = false
) {
    val transactionTags by tagLinksViewModel.transactionTags.collectAsStateWithLifecycle()
    val analyticsFundsViewModel: AnalyticsFundsViewModel = hiltViewModel()
    val funds by analyticsFundsViewModel.funds.collectAsStateWithLifecycle()
    LaunchedEffect(transactions, categories, paymentMethods, currencyId, amountFormatPreferences, monthStartDay, transactionTags, funds) {
        analyticsViewModel.updateInputs(
            transactions = transactions,
            categories = categories,
            paymentTypes = paymentMethods,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            monthStartDay = monthStartDay,
            transactionTags = transactionTags,
            funds = funds
        )
    }
    val uiState by analyticsViewModel.uiState.collectAsStateWithLifecycle()

    AnalyticsScreenContent(
        uiState = uiState,
        isAdsEnabled = isAdsEnabled,
        isProUser = isProUser,
        transactions = transactions,
        categories = categories,
        paymentMethods = paymentMethods,
        currencyId = currencyId,
        amountFormatPreferences = amountFormatPreferences,
        dateFormatPattern = dateFormatPattern,
        onBackClick = onBackClick,
        onDateRangeSelected = { analyticsViewModel.selectPeriod(it) },
        onCustomRangeApplied = { start, end -> analyticsViewModel.applyCustomRange(start, end) },
        onClearCustomRange = analyticsViewModel::clearCustomRange,
        transactionTags = transactionTags,
        funds = funds
    )
}

@Composable
fun AnalyticsScreenContent(
    uiState: AnalyticsScreenUiState,
    isAdsEnabled: Boolean,
    isProUser: Boolean = false,
    transactions: List<Transaction>,
    categories: List<CategoryType>,
    paymentMethods: List<PaymentType>,
    currencyId: Int,
    amountFormatPreferences: com.mknlabs.expensetracker.models.AmountFormatPreferences,
    dateFormatPattern: String,
    onBackClick: () -> Unit,
    onDateRangeSelected: (AnalyticsPeriod) -> Unit,
    onCustomRangeApplied: (Long, Long) -> Unit,
    onClearCustomRange: () -> Unit,
    transactionTags: Map<String, List<Tag>> = emptyMap(),
    funds: List<Fund> = emptyList()
) {
    var isCustomRangePickerVisible by rememberSaveable { mutableStateOf(false) }
    var isCategorySheetVisible by rememberSaveable { mutableStateOf(false) }
    var isPaymentSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isIncomeSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isTagSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isFundSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isTopSpendingSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isTransactionSheetVisible by rememberSaveable { mutableStateOf(false) }
    var heroDisplayMode by rememberSaveable { mutableStateOf(HeroDisplayMode.EXPENSE) }

    var selectedFilterId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedFilterTagId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedFilterLabel by rememberSaveable { mutableStateOf("") }
    var filterByPayment by rememberSaveable { mutableStateOf(false) }
    var filterByIncome by rememberSaveable { mutableStateOf(false) }

    val filteredTransactions = remember(
        selectedFilterId,
        selectedFilterTagId,
        uiState.activeRange,
        filterByPayment,
        filterByIncome,
        transactions,
        transactionTags
    ) {
        val range = uiState.activeRange
        when {
            selectedFilterTagId != null -> {
                transactions.filter { tx ->
                    tx.createdAt in range &&
                        tx.transactionTypeId == 2 &&
                        transactionTags[tx.id].orEmpty().any { it.id == selectedFilterTagId }
                }.sortedByDescending { it.createdAt }
            }
            selectedFilterId == null -> emptyList()
            filterByIncome -> {
                transactions.filter {
                    it.createdAt in range &&
                        it.transactionTypeId == 1 &&
                        it.categoryId == selectedFilterId
                }.sortedByDescending { it.createdAt }
            }
            else -> {
                transactions.filter {
                    it.createdAt in range &&
                        it.transactionTypeId == 2 &&
                        (if (filterByPayment) it.paymentTypeId == selectedFilterId else it.categoryId == selectedFilterId)
                }.sortedByDescending { it.createdAt }
            }
        }
    }
    val customRange = uiState.customRange
    val snapshot = uiState.snapshot
    // Two-column card grid on wide windows (tablets, foldables, desktop); the
    // period navigator, hero chart, and stat row stay full-width.
    val isWide = LocalAppWindowInfo.current.isWide

    // Primary scroll surface for the whole analytics screen; its scroll
    // direction drives the standalone add FAB's auto-hide on compact portrait.
    val analyticsListState = rememberLazyListState()
    rememberBindAddFabToScroll(analyticsListState)
    val enter = rememberSectionEnterAlphas(4)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appScreenBackground()
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.ScreenPadding, top = Dimens.HeaderSpacing, end = Dimens.ScreenPadding)
                .alpha(enter[0])
        ) {
            AppHeader(title = stringResource(id = R.string.title_analytics), onBackClick = onBackClick)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding)
                .alpha(enter[1]),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (hasCurrentPeriodIndicator(uiState.monthStartDay)) {
                CurrentPeriodIndicator(
                    startMillis = uiState.currentPeriodStartMillis,
                    endMillis = uiState.currentPeriodEndMillis,
                    monthStartDay = uiState.monthStartDay
                )
            }
            GatedAction(
                feature = Feature.ANALYTICS_PERIOD_YEAR,
                displayName = stringResource(id = R.string.title_yearly_analytics),
                onAction = { onDateRangeSelected(AnalyticsPeriod.YEAR) }
            ) { status, onLockedClick ->
                val isYearLocked = status !is AccessStatus.Granted

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalyticsPeriod.entries
                            .filter { it != AnalyticsPeriod.CUSTOM }
                            .forEach { period ->
                                val isLocked = period == AnalyticsPeriod.YEAR && isYearLocked
                                PeriodChip(
                                    label = stringResource(id = period.labelRes),
                                    isSelected = period == uiState.selectedPeriod,
                                    isLocked = isLocked,
                                    onClick = {
                                        if (isLocked) onLockedClick() else onDateRangeSelected(period)
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                    }

                    CustomRangeSelector(
                        selectedPeriod = uiState.selectedPeriod,
                        customRange = customRange,
                        onClick = {
                            isCustomRangePickerVisible = true
                        },
                        onClear = onClearCustomRange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        LazyColumn(
            state = analyticsListState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = Dimens.ScreenPadding,
                top = 8.dp,
                end = Dimens.ScreenPadding,
                bottom = 142.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Box(Modifier.alpha(enter[1])) {
                    HeroAnalyticsSection(
                        snapshot = snapshot,
                        displayMode = heroDisplayMode,
                        onDisplayModeChange = { heroDisplayMode = it }
                    )
                }
            }

            // Guarded on the ad being on at all. AdContainer collapses to nothing when ads are
            // off, but the item it was emitted from is still in the list, and an invisible lazy
            // item still costs the 18.dp spacing above and below it. Measured on a real window
            // with ads off, the hole left between two cards was 94px where every other pair is 47px.
            if (isAdsEnabled) {
                item {
                    AdContainer(isAdsEnabled = true) {
                        NativeAdCard(placement = AdPlacement.ANALYTICS_INSIGHTS)
                    }
                }
            }
            item { Box(Modifier.alpha(enter[2])) { StatsRow(snapshot) } }
            // Cash Flow Ratio is always full-width: in the two-column layout it
            // would otherwise be squeezed into half the row width.
            item {
                Box(Modifier.alpha(enter[2])) { CashFlowCard(snapshot) }
            }
            item {
                Box(Modifier.alpha(enter[3])) {
                AnalyticsSectionRow(
                    isWide = isWide,
                    first = {
                    GatedAction(
                        feature = Feature.ANALYTICS_CATEGORY_BREAKDOWN,
                        displayName = stringResource(id = R.string.title_full_category_breakdown),
                        onAction = {}
                    ) { status, onClick ->
                        val isLocked = status !is AccessStatus.Granted
                        Box {
                            GatedCardContent(isLocked = isLocked) { gated ->
                                CategoryCard(
                                    modifier = gated,
                                    snapshot = snapshot,
                                    onViewAllClick = { isCategorySheetVisible = true },
                                    onShowTransactions = { id, label ->
                                        selectedFilterId = id
                                        selectedFilterTagId = null
                                        selectedFilterLabel = label
                                        filterByPayment = false
                                        filterByIncome = false
                                        isTransactionSheetVisible = true
                                    }
                                )
                            }
                            if (isLocked) {
                                PremiumLockedOverlay(
                                    displayText = stringResource(id = R.string.label_unlock_breakdown),
                                    onClick = onClick
                                )
                            }
                        }
                    }
                    },
                    second = {
                    GatedAction(
                        feature = Feature.ANALYTICS_PAYMENT_BREAKDOWN,
                        displayName = stringResource(id = R.string.title_payment_mode_breakdown),
                        onAction = {}
                    ) { status, onClick ->
                        val isLocked = status !is AccessStatus.Granted
                        Box {
                            GatedCardContent(isLocked = isLocked) { gated ->
                                PaymentTypeCard(
                                    modifier = gated,
                                    snapshot = snapshot,
                                    onViewAllClick = { isPaymentSheetVisible = true },
                                    onShowTransactions = { id, label ->
                                        selectedFilterId = id
                                        selectedFilterTagId = null
                                        selectedFilterLabel = label
                                        filterByPayment = true
                                        filterByIncome = false
                                        isTransactionSheetVisible = true
                                    }
                                )
                            }
                            if (isLocked) {
                                PremiumLockedOverlay(
                                    displayText = stringResource(id = R.string.label_unlock_breakdown),
                                    onClick = onClick
                                )
                            }
                        }
                    }
                    }
                )
                }
            }

            item {
                Box(Modifier.alpha(enter[3])) {
                AnalyticsSectionRow(
                    isWide = isWide,
                    first = {
                    GatedAction(
                        feature = Feature.ANALYTICS_CATEGORY_BREAKDOWN,
                        displayName = stringResource(id = R.string.title_full_income_breakdown),
                        onAction = {}
                    ) { status, onClick ->
                        val isLocked = status !is AccessStatus.Granted
                        Box {
                            GatedCardContent(isLocked = isLocked) { gated ->
                                CategoryCard(
                                    modifier = gated,
                                    snapshot = snapshot,
                                    titleRes = R.string.label_income_breakdown,
                                    breakdown = snapshot.incomeBreakdown,
                                    allBreakdown = snapshot.allIncomeBreakdown,
                                    emptyRes = R.string.label_no_income_breakdown,
                                    onViewAllClick = { isIncomeSheetVisible = true },
                                    onShowTransactions = { id, label ->
                                        selectedFilterId = id
                                        selectedFilterTagId = null
                                        selectedFilterLabel = label
                                        filterByPayment = false
                                        filterByIncome = true
                                        isTransactionSheetVisible = true
                                    }
                                )
                            }
                            if (isLocked) {
                                PremiumLockedOverlay(
                                    displayText = stringResource(id = R.string.label_unlock_breakdown),
                                    onClick = onClick
                                )
                            }
                        }
                    }
                    },
                    second = {
                    GatedAction(
                        feature = Feature.ANALYTICS_CATEGORY_BREAKDOWN,
                        displayName = stringResource(id = R.string.title_full_tag_breakdown),
                        onAction = {}
                    ) { status, onClick ->
                        val isLocked = status !is AccessStatus.Granted
                        Box {
                            GatedCardContent(isLocked = isLocked) { gated ->
                                TagBreakdownCard(
                                    modifier = gated,
                                    snapshot = snapshot,
                                    onViewAllClick = { isTagSheetVisible = true },
                                    onShowTransactions = { id, label ->
                                        selectedFilterId = null
                                        selectedFilterTagId = id
                                        selectedFilterLabel = label
                                        filterByPayment = false
                                        filterByIncome = false
                                        isTransactionSheetVisible = true
                                    }
                                )
                            }
                            if (isLocked) {
                                PremiumLockedOverlay(
                                    displayText = stringResource(id = R.string.label_unlock_breakdown),
                                    onClick = onClick
                                )
                            }
                        }
                    }
                    }
                )
                }
            }
            // Funds get their own full-width row rather than a half of one: a bucket name
            // is longer than a category or a payment method, and the row is about money
            // moving out of named pools rather than another axis of the same split.
            item {
                Box(Modifier.alpha(enter[3])) {
                    GatedAction(
                        feature = Feature.ANALYTICS_FUND_BREAKDOWN,
                        displayName = stringResource(id = R.string.title_full_fund_breakdown),
                        onAction = {}
                    ) { status, onClick ->
                        val isLocked = status !is AccessStatus.Granted
                        Box {
                            GatedCardContent(isLocked = isLocked) { gated ->
                                FundBreakdownCard(
                                    modifier = gated,
                                    snapshot = snapshot,
                                    onViewAllClick = { isFundSheetVisible = true }
                                )
                            }
                            if (isLocked) {
                                PremiumLockedOverlay(
                                    displayText = stringResource(id = R.string.label_unlock_breakdown),
                                    onClick = onClick
                                )
                            }
                        }
                    }
                }
            }

            // Single-column: keep the native ad between the payment breakdown
            // and top spending. In the two-column layout it becomes a full-width
            // row at the bottom instead (see below).
            if (!isWide && isAdsEnabled) {
                item {
                    AdContainer(isAdsEnabled = true) {
                        NativeAdCard(placement = AdPlacement.ANALYTICS_INSIGHTS)
                    }
                }
            }
            item {
                Box(Modifier.alpha(enter[3])) {
                AnalyticsSectionRow(
                    isWide = isWide,
                    first = {
                    GatedAction(
                        feature = Feature.ANALYTICS_TOP_SPENDING,
                        displayName = stringResource(id = R.string.title_top_spending_details),
                        onAction = {}
                    ) { status, onClick ->
                        Box {
                            val isLocked = status !is AccessStatus.Granted
                            GatedCardContent(isLocked = isLocked) { gated ->
                                TopSpendingCard(
                                    modifier = gated,
                                    topTransactions = snapshot.topTransactions,
                                    dateFormatPattern = dateFormatPattern,
                                    onViewAllClick = { isTopSpendingSheetVisible = true }
                                )
                            }
                            if (isLocked) {
                                PremiumLockedOverlay(
                                    displayText = stringResource(id = R.string.label_unlock_top_spending),
                                    onClick = onClick
                                )
                            }
                        }
                    }
                    },
                    second = {
                    GatedAction(
                        feature = Feature.ANALYTICS_SMART_TIPS,
                        displayName = stringResource(id = R.string.title_spending_insights),
                        onAction = {}
                    ) { status, onClick ->
                        Box {
                            val isLocked = status !is AccessStatus.Granted
                            GatedCardContent(isLocked = isLocked) { gated ->
                                SmartTipCard(
                                    modifier = gated,
                                    tip = snapshot.smartTip
                                )
                            }
                            if (isLocked) {
                                PremiumLockedOverlay(
                                    displayText = stringResource(id = R.string.label_unlock_insights),
                                    icon = Icons.Filled.AutoAwesome,
                                    onClick = onClick
                                )
                            }
                        }
                    }
                }
                )
                }
            }
            // Two-column layout: native ad as a full-width row at the bottom.
            if (isWide && isAdsEnabled) {
                item {
                    AdContainer(isAdsEnabled = true) {
                        NativeAdCard(placement = AdPlacement.ANALYTICS_INSIGHTS)
                    }
                }
            }
        }
    }

    if (isCustomRangePickerVisible) {
        WheelDateTimePickerModal(
            mode = WheelPickerMode.DATE_RANGE,
            initialStartMillis = uiState.customRangeStart ?: System.currentTimeMillis(),
            initialEndMillis = uiState.customRangeEnd,
            onDismissRequest = { isCustomRangePickerVisible = false },
            onConfirm = { pickedStart, pickedEnd ->
                onCustomRangeApplied(pickedStart, pickedEnd ?: pickedStart)
                isCustomRangePickerVisible = false
            }
        )
    }

    if (isCategorySheetVisible) {
        GatedAction(
            feature = Feature.ANALYTICS_CATEGORY_BREAKDOWN,
            displayName = stringResource(id = R.string.title_full_category_breakdown),
            onAction = { isCategorySheetVisible = true }
        ) { status, onClick ->
            if (status is AccessStatus.Granted) {
                CategoryBreakdownBottomSheet(
                    categories = snapshot.allCategoryBreakdown,
                    onDismiss = { isCategorySheetVisible = false },
                    onShowTransactions = { id, label ->
                        selectedFilterId = id
                        selectedFilterLabel = label
                        filterByPayment = false
                        isTransactionSheetVisible = true
                    }
                )
            } else {
                LaunchedEffect(Unit) { 
                    isCategorySheetVisible = false
                    onClick()
                }
            }
        }
    }

    if (isIncomeSheetVisible) {
        GatedAction(
            feature = Feature.ANALYTICS_CATEGORY_BREAKDOWN,
            displayName = stringResource(id = R.string.title_full_income_breakdown),
            onAction = { isIncomeSheetVisible = true }
        ) { status, onClick ->
            if (status is AccessStatus.Granted) {
                CategoryBreakdownBottomSheet(
                    categories = snapshot.allIncomeBreakdown,
                    onDismiss = { isIncomeSheetVisible = false },
                    onShowTransactions = { id, label ->
                        selectedFilterId = id
                        selectedFilterTagId = null
                        selectedFilterLabel = label
                        filterByPayment = false
                        filterByIncome = true
                        isTransactionSheetVisible = true
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    isIncomeSheetVisible = false
                    onClick()
                }
            }
        }
    }

    if (isTagSheetVisible) {
        GatedAction(
            feature = Feature.ANALYTICS_CATEGORY_BREAKDOWN,
            displayName = stringResource(id = R.string.title_full_tag_breakdown),
            onAction = { isTagSheetVisible = true }
        ) { status, onClick ->
            if (status is AccessStatus.Granted) {
                TagBreakdownBottomSheet(
                    tags = snapshot.allTagBreakdown,
                    onDismiss = { isTagSheetVisible = false },
                    onShowTransactions = { id, label ->
                        selectedFilterId = null
                        selectedFilterTagId = id
                        selectedFilterLabel = label
                        filterByPayment = false
                        filterByIncome = false
                        isTransactionSheetVisible = true
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    isTagSheetVisible = false
                    onClick()
                }
            }
        }
    }

    if (isPaymentSheetVisible) {
        GatedAction(
            feature = Feature.ANALYTICS_PAYMENT_BREAKDOWN,
            displayName = stringResource(id = R.string.title_full_payment_breakdown),
            onAction = { isPaymentSheetVisible = true }
        ) { status, onClick ->
            if (status is AccessStatus.Granted) {
                PaymentTypeBreakdownBottomSheet(
                    categories = snapshot.allPaymentTypeBreakdown,
                    onDismiss = { isPaymentSheetVisible = false },
                    onShowTransactions = { id, label ->
                        selectedFilterId = id
                        selectedFilterLabel = label
                        filterByPayment = true
                        isTransactionSheetVisible = true
                    }
                )
            } else {
                LaunchedEffect(Unit) { 
                    isPaymentSheetVisible = false
                    onClick()
                }
            }
        }
    }

    if (isFundSheetVisible) {
        GatedAction(
            feature = Feature.ANALYTICS_FUND_BREAKDOWN,
            displayName = stringResource(id = R.string.title_full_fund_breakdown),
            onAction = { isFundSheetVisible = true }
        ) { status, onClick ->
            if (status is AccessStatus.Granted) {
                FundBreakdownBottomSheet(
                    funds = snapshot.allFundBreakdown,
                    onDismiss = { isFundSheetVisible = false }
                )
            } else {
                LaunchedEffect(Unit) {
                    isFundSheetVisible = false
                    onClick()
                }
            }
        }
    }

    if (isTransactionSheetVisible) {
        FilteredTransactionsBottomSheet(
            label = selectedFilterLabel,
            transactions = filteredTransactions,
            categories = categories,
            paymentTypes = paymentMethods,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = dateFormatPattern,
            isProUser = isProUser,
            transactionTags = transactionTags,
            fundNames = funds.associate { it.id to it.name },
            onDismiss = {
                isTransactionSheetVisible = false
                selectedFilterId = null
                selectedFilterTagId = null
            }
        )
    }

    if (isTopSpendingSheetVisible) {
        TopSpendingBottomSheet(
            transactions = snapshot.allTopTransactions,
            dateFormatPattern = dateFormatPattern,
            onDismiss = { isTopSpendingSheetVisible = false }
        )
    }
}







// ──────────────────────────────────────────────
// Preview helpers
// ──────────────────────────────────────────────

private fun buildPreviewAnalyticsUiState(): AnalyticsScreenUiState {
    val currencyId = DEFAULT_CURRENCY_ID
    val fmtPrefs = defaultAmountFormatPreferences

    // Filter transactions for a realistic February 2026 month range
    val febStart = 1769936400000L // Feb 1, 2026
    val febEnd = 1774998300000L   // Feb 28, 2026
    val monthlyTransactions = transactionList.filter { it.createdAt in febStart..febEnd }

    // Compute totals
    val income = monthlyTransactions.filter { it.transactionTypeId == 1 }.sumOf { it.amount }
    val expense = monthlyTransactions.filter { it.transactionTypeId == 2 }.sumOf { it.amount }
    val savings = income - expense
    val avgDailyExpense = expense / 28.0
    val split = cashFlowSplit(income, expense)

    // Category breakdown
    val categoryTotals = monthlyTransactions
        .filter { it.transactionTypeId == 2 }
        .groupBy { it.categoryId }
        .mapValues { (_, items) -> items.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
    val totalExp = categoryTotals.sumOf { it.second }.takeIf { it > 0.0 } ?: 1.0

    val allCategoryBreakdown = categoryTotals.map { (catId, amount) ->
        val cat = categoryMap[catId]
        CategoryBreakdownUi(
            id = catId,
            label = cat?.name ?: "",
            isOther = cat == null,
            amountDisplay = formatCurrencyValue(amount, currencyId, fmtPrefs),
            fraction = (amount / totalExp).toFloat(),
            percentLabel = ((amount / totalExp) * 100).toInt()
        )
    }

    // Payment breakdown
    val paymentTotals = monthlyTransactions
        .filter { it.transactionTypeId == 2 }
        .groupBy { it.paymentTypeId }
        .mapValues { (_, items) -> items.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val allPaymentBreakdown = paymentTotals.map { (pmtId, amount) ->
        val pmt = paymentTypeMap[pmtId]
        PaymentTypeBreakdownUi(
            id = pmtId,
            label = pmt?.name ?: "",
            isOther = pmt == null,
            amountDisplay = formatCurrencyValue(amount, currencyId, fmtPrefs),
            fraction = (amount / totalExp).toFloat(),
            percentLabel = ((amount / totalExp) * 100).toInt(),
            icon = pmt?.icon ?: Icons.Filled.Analytics
        )
    }

    // Top transactions
    val topTransactions = monthlyTransactions
        .filter { it.transactionTypeId == 2 }
        .sortedByDescending { it.amount }
        .take(3)
        .map { t ->
            val cat = categoryMap[t.categoryId]
            TopSpendingItemUi(
                id = t.id.toString(),
                note = t.note,
                amountDisplay = formatCurrencyValue(t.amount, currencyId, fmtPrefs, prefix = "-"),
                categoryLabel = cat?.name ?: "",
                isGeneral = cat == null,
                icon = cat?.icon ?: Icons.Filled.Analytics,
                createdAt = t.createdAt
            )
        }

    val allTopTransactions = monthlyTransactions
        .filter { it.transactionTypeId == 2 }
        .sortedByDescending { it.amount }
        .take(10)
        .map { t ->
            val cat = categoryMap[t.categoryId]
            TopSpendingItemUi(
                id = t.id.toString(),
                note = t.note,
                amountDisplay = formatCurrencyValue(t.amount, currencyId, fmtPrefs, prefix = "-"),
                categoryLabel = cat?.name ?: "",
                isGeneral = cat == null,
                icon = cat?.icon ?: Icons.Filled.Analytics,
                createdAt = t.createdAt
            )
        }

    // Weekly chart buckets
    val weekStarts = longArrayOf(febStart, febStart + 7 * 86400000L, febStart + 14 * 86400000L, febStart + 21 * 86400000L)
    val expenseChartPoints = (0..3).map { i ->
        val start = weekStarts[i]
        val end = if (i < 3) weekStarts[i + 1] - 1 else febEnd
        monthlyTransactions.filter { it.transactionTypeId == 2 && it.createdAt in start..end }.sumOf { it.amount }.toFloat()
    }
    val incomeChartPoints = (0..3).map { i ->
        val start = weekStarts[i]
        val end = if (i < 3) weekStarts[i + 1] - 1 else febEnd
        monthlyTransactions.filter { it.transactionTypeId == 1 && it.createdAt in start..end }.sumOf { it.amount }.toFloat()
    }
    val chartLabels = listOf(
        ChartLabelUi(label = UiText.dynamic("W1")),
        ChartLabelUi(label = UiText.dynamic("W2")),
        ChartLabelUi(label = UiText.dynamic("W3")),
        ChartLabelUi(label = UiText.dynamic("W4"))
    )

    val snapshot = AnalyticsSnapshotUi(
        summaryLabel = SummaryLabelUi(patternResId = R.string.date_pattern_month_year, timestamp = febStart),
        totalDisplay = formatCurrencyValue(income + expense, currencyId, fmtPrefs),
        changeDisplay = UiText.dynamic("+12.5%"),
        changePercent = 12.5f,
        avgDailyDisplay = formatCurrencyValue(avgDailyExpense, currencyId, fmtPrefs),
        dailyDeltaDisplay = UiText.dynamic("-3.2%"),
        dailyDeltaPercent = -3.2f,
        savingsDisplay = formatCurrencyValue(savings, currencyId, fmtPrefs),
        savingsDeltaDisplay = UiText.dynamic("+8.1%"),
        savingsDeltaPercent = 8.1f,
        incomeDisplay = formatCurrencyValue(income, currencyId, fmtPrefs),
        expenseDisplay = formatCurrencyValue(expense, currencyId, fmtPrefs),
        incomeFraction = split.incomeWeight,
        incomePercent = split.incomePercent,
        expensePercent = split.expensePercent,
        ratioDisplay = split.ratioDisplay,
        expenseChartPoints = expenseChartPoints,
        incomeChartPoints = incomeChartPoints,
        chartLabels = chartLabels,
        categoryBreakdown = allCategoryBreakdown.take(3),
        allCategoryBreakdown = allCategoryBreakdown,
        paymentTypeBreakdown = allPaymentBreakdown.take(3),
        allPaymentTypeBreakdown = allPaymentBreakdown,
        topTransactions = topTransactions,
        allTopTransactions = allTopTransactions,
        smartTip = SmartTipUi(
            resId = R.string.msg_spending_trend,
            flowChange = UiText.dynamic("+12.5%"),
            directionResId = R.string.label_up,
            topCategory = allCategoryBreakdown.firstOrNull()?.label ?: "",
            savingAmount = formatCurrencyValue(avgDailyExpense * 4, currencyId, fmtPrefs),
            hasSpendingData = true
        ),
        hasSpendingData = true
    )

    return AnalyticsScreenUiState(
        selectedPeriod = AnalyticsPeriod.MONTH,
        activeRange = febStart..febEnd,
        snapshot = snapshot
    )
}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//private fun AnalyticsScreenPreviewDark() {
//    ExpenseTrackerTheme(darkTheme = true) {
//        AnalyticsScreenContent(
//            uiState = buildPreviewAnalyticsUiState(),
//            isAdsEnabled = true,
//            transactions = transactionList,
//            categories = categoryMap.values.toList(),
//            paymentMethods = paymentTypeMap.values.toList(),
//            currencyId = DEFAULT_CURRENCY_ID,
//            amountFormatPreferences = defaultAmountFormatPreferences,
//            dateFormatPattern = "dd MMM yyyy",
//            onBackClick = {},
//            onDateRangeSelected = {},
//            onCustomRangeApplied = { _, _ -> },
//            onClearCustomRange = {}
//        )
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
//@Composable
//private fun AnalyticsScreenPreviewLight() {
//    ExpenseTrackerTheme(darkTheme = false) {
//        AnalyticsScreenContent(
//            uiState = buildPreviewAnalyticsUiState(),
//            isAdsEnabled = true,
//            transactions = transactionList,
//            categories = categoryMap.values.toList(),
//            paymentMethods = paymentTypeMap.values.toList(),
//            currencyId = DEFAULT_CURRENCY_ID,
//            amountFormatPreferences = defaultAmountFormatPreferences,
//            dateFormatPattern = "dd MMM yyyy",
//            onBackClick = {},
//            onDateRangeSelected = {},
//            onCustomRangeApplied = { _, _ -> },
//            onClearCustomRange = {}
//        )
//    }
//}

// ──────────────────────────────────────────────
// Smart AI Tip card preview (unlocked state)
// ──────────────────────────────────────────────

private fun buildPreviewSmartTip(): SmartTipUi {
    val currencyId = DEFAULT_CURRENCY_ID
    val fmtPrefs = defaultAmountFormatPreferences
    return SmartTipUi(
        resId = R.string.msg_spending_trend,
        flowChange = UiText.dynamic("+12.5%"),
        directionResId = R.string.label_up,
        topCategory = categoryMap.values.firstOrNull()?.name ?: "",
        savingAmount = formatCurrencyValue(420.0 * 4, currencyId, fmtPrefs),
        hasSpendingData = true
    )
}

@Composable
private fun SmartTipCardUnlockedPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding, vertical = 16.dp)
    ) {
        SmartTipCard(
            modifier = Modifier.fillMaxWidth(),
            tip = buildPreviewSmartTip()
        )
    }
}

@Preview(showBackground = true, name = "Smart AI Tip Card - Unlocked (Dark)")
@Composable
private fun SmartTipCardUnlockedPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        SmartTipCardUnlockedPreview()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO, name = "Smart AI Tip Card - Unlocked (Light)")
@Composable
private fun SmartTipCardUnlockedPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        SmartTipCardUnlockedPreview()
    }
}

@Composable
private fun CashFlowRatioCardPreviewBody() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding, vertical = 16.dp)
    ) {
        CashFlowCard(snapshot = buildPreviewAnalyticsUiState().snapshot)
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Cash Flow Ratio (Dark)")
@Composable
private fun CashFlowRatioCardPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        CashFlowRatioCardPreviewBody()
    }
}

@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO,
    name = "Cash Flow Ratio (Light)"
)
@Composable
private fun CashFlowRatioCardPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        CashFlowRatioCardPreviewBody()
    }
}

/**
 * The Avg Daily and Savings cards share [StatsRow], and they are the only pair on the screen laid
 * out side by side at half width each: the preview has to render both, at a real width, or the
 * amount-size fallback in [InsightStatCard] never gets the chance to fire the way it does on a
 * narrow phone.
 */
@Composable
private fun StatsRowPreviewBody() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ScreenPadding, vertical = 16.dp)
    ) {
        StatsRow(buildPreviewAnalyticsUiState().snapshot)
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Avg and Savings Cards (Dark)")
@Composable
private fun StatsRowPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        StatsRowPreviewBody()
    }
}

@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO,
    name = "Avg and Savings Cards (Light)"
)
@Composable
private fun StatsRowPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        StatsRowPreviewBody()
    }
}

// ──────────────────────────────────────────────
// Locked analytics card previews
// ──────────────────────────────────────────────

/**
 * The gated cards, all forced locked, composed through [GatedCardContent] plus
 * [PremiumLockedOverlay] so the veil matches the running screen.
 */
@Composable
private fun GatedPreviewCard(
    unlockText: String,
    icon: ImageVector = Icons.Filled.Lock,
    card: @Composable (Modifier) -> Unit
) {
    Box {
        GatedCardContent(isLocked = true) { gated -> card(gated) }
        PremiumLockedOverlay(
            displayText = unlockText,
            icon = icon,
            onClick = {}
        )
    }
}

/**
 * All four locked cards in one column, stacked on the app's own background so the veil and the
 * scrim are judged against the same surface they sit on in the running screen.
 */
@Composable
private fun LockedAnalyticsCardsPreviewBody() {
    val snapshot = buildPreviewAnalyticsUiState().snapshot
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Dimens.ScreenPadding, vertical = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            GatedPreviewCard(
                unlockText = stringResource(id = R.string.label_unlock_breakdown)
            ) { gated ->
                CategoryCard(
                    modifier = gated,
                    snapshot = snapshot,
                    onViewAllClick = {},
                    onShowTransactions = { _, _ -> }
                )
            }
            GatedPreviewCard(
                unlockText = stringResource(id = R.string.label_unlock_breakdown)
            ) { gated ->
                PaymentTypeCard(
                    modifier = gated,
                    snapshot = snapshot,
                    onViewAllClick = {},
                    onShowTransactions = { _, _ -> }
                )
            }
            GatedPreviewCard(
                unlockText = stringResource(id = R.string.label_unlock_top_spending)
            ) { gated ->
                TopSpendingCard(
                    modifier = gated,
                    topTransactions = snapshot.topTransactions,
                    dateFormatPattern = DEFAULT_DATE_FORMAT_PATTERN,
                    onViewAllClick = {}
                )
            }
            GatedPreviewCard(
                unlockText = stringResource(id = R.string.label_unlock_insights),
                icon = Icons.Filled.AutoAwesome
            ) { gated ->
                SmartTipCard(
                    modifier = gated,
                    tip = buildPreviewSmartTip()
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "Locked Analytics Cards (Dark)"
)
@Composable
private fun LockedAnalyticsCardsPreviewDark() {
    ExpenseTrackerTheme(darkTheme = true) {
        LockedAnalyticsCardsPreviewBody()
    }
}

@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO,
    name = "Locked Analytics Cards (Light)"
)
@Composable
private fun LockedAnalyticsCardsPreviewLight() {
    ExpenseTrackerTheme(darkTheme = false) {
        LockedAnalyticsCardsPreviewBody()
    }
}
