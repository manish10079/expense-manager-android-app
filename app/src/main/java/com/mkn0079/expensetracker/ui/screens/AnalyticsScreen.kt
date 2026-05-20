package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.mkn0079.expensetracker.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.transactionList
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.utils.formatCurrencyValue
import com.mkn0079.expensetracker.ui.components.AnimatedTabSwitcher
import com.mkn0079.expensetracker.ui.models.TabItem
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.components.GatedAction
import com.mkn0079.expensetracker.ui.components.WheelDateTimePickerModal
import com.mkn0079.expensetracker.ui.components.WheelPickerMode
import com.mkn0079.expensetracker.utils.defaultAmountFormatPreferences
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.ui.theme.income
import com.mkn0079.expensetracker.ui.theme.expense
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.Dimens
import com.mkn0079.expensetracker.ui.theme.brandGradient
import com.mkn0079.expensetracker.ui.theme.standardCardGradient
import com.mkn0079.expensetracker.ui.theme.featureGateLock
import com.mkn0079.expensetracker.ui.viewmodels.AnalyticsPeriod
import com.mkn0079.expensetracker.ui.viewmodels.PaymentTypeBreakdownUi
import com.mkn0079.expensetracker.ui.viewmodels.TopSpendingItemUi
import com.mkn0079.expensetracker.ui.viewmodels.formatCustomRangeLabel
import com.mkn0079.expensetracker.ui.viewmodels.AnalyticsViewModel
import com.mkn0079.expensetracker.ui.viewmodels.AnalyticsSnapshotUi
import com.mkn0079.expensetracker.ui.viewmodels.CategoryBreakdownUi
import com.mkn0079.expensetracker.ui.viewmodels.SummaryLabelUi
import com.mkn0079.expensetracker.ui.viewmodels.SmartTipUi
import com.mkn0079.expensetracker.ui.viewmodels.ChartLabelUi
import com.mkn0079.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mkn0079.expensetracker.utils.formatDate
import com.mkn0079.expensetracker.ui.components.TransactionCard
import androidx.hilt.navigation.compose.hiltViewModel
import com.mkn0079.expensetracker.ui.viewmodels.MonetizationViewModel
import com.mkn0079.expensetracker.ui.components.AdContainer
import com.mkn0079.expensetracker.ui.components.BannerAdView
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

enum class HeroDisplayMode(val labelRes: Int) {
    EXPENSE(R.string.label_expense),
    INCOME(R.string.label_income),
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
    onBackClick: () -> Unit = {},
    analyticsViewModel: AnalyticsViewModel = viewModel()
) {
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val isAdsEnabled by monetizationViewModel.isAdsEnabled.collectAsStateWithLifecycle()

    var isCustomRangePickerVisible by rememberSaveable { mutableStateOf(false) }
    var isCategorySheetVisible by rememberSaveable { mutableStateOf(false) }
    var isPaymentSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isTopSpendingSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isTransactionSheetVisible by rememberSaveable { mutableStateOf(false) }
    var heroDisplayMode by rememberSaveable { mutableStateOf(HeroDisplayMode.EXPENSE) }

    var selectedFilterId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedFilterLabel by rememberSaveable { mutableStateOf("") }
    var filterByPayment by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(transactions, categories, paymentMethods, currencyId, amountFormatPreferences) {
        analyticsViewModel.updateInputs(
            transactions = transactions,
            categories = categories,
            paymentTypes = paymentMethods,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences
        )
    }
    val uiState by analyticsViewModel.uiState.collectAsStateWithLifecycle()
    val filteredTransactions = remember(selectedFilterId, uiState.activeRange, filterByPayment, transactions) {
        if (selectedFilterId == null) emptyList()
        else {
            transactions.filter {
                it.createdAt in uiState.activeRange &&
                        it.transactionTypeId == 2 &&
                        (if (filterByPayment) it.paymentTypeId == selectedFilterId else it.categoryId == selectedFilterId)
            }.sortedByDescending { it.createdAt }
        }
    }
    val customRange = uiState.customRange
    val snapshot = uiState.snapshot

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Dimens.ScreenPadding, top = Dimens.HeaderSpacing, end = Dimens.ScreenPadding)
        ) {
            AppHeader(title = stringResource(id = R.string.title_analytics), onBackClick = onBackClick)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = Dimens.ScreenPadding, top = 18.dp, end = Dimens.ScreenPadding, bottom = 142.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                GatedAction(
                    feature = Feature.ANALYTICS_PERIOD_YEAR,
                    displayName = stringResource(id = R.string.title_yearly_analytics),
                    onAction = { analyticsViewModel.selectPeriod(AnalyticsPeriod.YEAR) }
                ) { status, onLockedClick ->
                    val isYearLocked = status !is AccessStatus.Granted

                    AnimatedTabSwitcher(
                        items = AnalyticsPeriod.entries.filter { it != AnalyticsPeriod.CUSTOM }.map { period ->
                            TabItem(
                                id = period,
                                label = stringResource(id = period.labelRes),
                                isLocked = period == AnalyticsPeriod.YEAR && isYearLocked,
                                onLockedClick = { if (period == AnalyticsPeriod.YEAR && isYearLocked) onLockedClick() }
                            )
                        },
                        selectedItemId = uiState.selectedPeriod,
                        onItemSelected = { period ->
                            analyticsViewModel.selectPeriod(period)
                        }
                    )
                }
            }
            item {
                CustomRangeSelector(
                    selectedPeriod = uiState.selectedPeriod,
                    customRange = customRange,
                    onClick = {
                        isCustomRangePickerVisible = true
                    },
                    onClear = analyticsViewModel::clearCustomRange
                )
            }
            item { 
                HeroAnalyticsSection(
                    snapshot = snapshot,
                    displayMode = heroDisplayMode,
                    onDisplayModeChange = { heroDisplayMode = it }
                ) 
            }
            item { StatsRow(snapshot) }
            item { CashFlowCard(snapshot) }
            item {
                GatedAction(
                    feature = Feature.ANALYTICS_CATEGORY_BREAKDOWN,
                    displayName = stringResource(id = R.string.title_full_category_breakdown),
                    onAction = {}
                ) { status, onClick ->
                    val isLocked = status !is AccessStatus.Granted
                    Box {
                        CategoryCard(
                            modifier = if (isLocked) Modifier.blur(16.dp) else Modifier,
                            snapshot = snapshot,
                            onViewAllClick = { isCategorySheetVisible = true },
                            onShowTransactions = { id, label ->
                                selectedFilterId = id
                                selectedFilterLabel = label
                                filterByPayment = false
                                isTransactionSheetVisible = true
                            }
                        )
                        if (isLocked) {
                            PremiumLockedOverlay(
                                displayText = stringResource(id = R.string.label_unlock_breakdown),
                                onClick = onClick
                            )
                        }
                    }
                }
            }
            item {
                GatedAction(
                    feature = Feature.ANALYTICS_PAYMENT_BREAKDOWN,
                    displayName = stringResource(id = R.string.title_payment_mode_breakdown),
                    onAction = {}
                ) { status, onClick ->
                    val isLocked = status !is AccessStatus.Granted
                    Box {
                        PaymentTypeCard(
                            modifier = if (isLocked) Modifier.blur(16.dp) else Modifier,
                            snapshot = snapshot,
                            onViewAllClick = { isPaymentSheetVisible = true },
                            onShowTransactions = { id, label ->
                                selectedFilterId = id
                                selectedFilterLabel = label
                                filterByPayment = true
                                isTransactionSheetVisible = true
                            }
                        )
                        if (isLocked) {
                            PremiumLockedOverlay(
                                displayText = stringResource(id = R.string.label_unlock_breakdown),
                                onClick = onClick
                            )
                        }
                    }
                }
            }
            item {
                // Inline Banner Ad after Payment Mode Breakdown
                AdContainer(isAdsEnabled = isAdsEnabled) {
                    BannerAdView()
                }
            }
            item {
                GatedAction(
                    feature = Feature.ANALYTICS_TOP_SPENDING,
                    displayName = stringResource(id = R.string.title_top_spending_details),
                    onAction = {}
                ) { status, onClick ->
                    Box {
                        val isLocked = status !is AccessStatus.Granted
                        TopSpendingCard(
                            modifier = if (isLocked) Modifier.blur(16.dp) else Modifier,
                            topTransactions = snapshot.topTransactions,
                            dateFormatPattern = dateFormatPattern,
                            onViewAllClick = { isTopSpendingSheetVisible = true }
                        )
                        if (isLocked) {
                            PremiumLockedOverlay(
                                displayText = stringResource(id = R.string.label_unlock_top_spending),
                                onClick = onClick
                            )
                        }
                    }
                }
            }
            item {
                GatedAction(
                    feature = Feature.ANALYTICS_SMART_TIPS,
                    displayName = stringResource(id = R.string.title_spending_insights),
                    onAction = {}
                ) { status, onClick ->
                    Box {
                        val isLocked = status !is AccessStatus.Granted
                        SmartTipCard(
                            modifier = if (isLocked) Modifier.blur(16.dp) else Modifier,
                            tip = snapshot.smartTip
                        )
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
        }
    }

    if (isCustomRangePickerVisible) {
        WheelDateTimePickerModal(
            mode = WheelPickerMode.DATE_RANGE,
            initialStartMillis = uiState.customRangeStart ?: System.currentTimeMillis(),
            initialEndMillis = uiState.customRangeEnd,
            onDismissRequest = { isCustomRangePickerVisible = false },
            onConfirm = { pickedStart, pickedEnd ->
                analyticsViewModel.applyCustomRange(
                    startMillis = pickedStart,
                    endMillis = pickedEnd ?: pickedStart
                )
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

    if (isTransactionSheetVisible) {
        FilteredTransactionsBottomSheet(
            label = selectedFilterLabel,
            transactions = filteredTransactions,
            categories = categories,
            paymentTypes = paymentMethods,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            dateFormatPattern = dateFormatPattern,
            onDismiss = {
                isTransactionSheetVisible = false
                selectedFilterId = null
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

@Composable
private fun CustomRangeSelector(
    selectedPeriod: AnalyticsPeriod,
    customRange: LongRange?,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (selectedPeriod == AnalyticsPeriod.CUSTOM) {
                        Brush.horizontalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                        )
                    } else {
                        standardCardGradient()
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (selectedPeriod == AnalyticsPeriod.CUSTOM) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            GatedAction(
                feature = Feature.ANALYTICS_CUSTOM_RANGE,
                displayName = stringResource(id = R.string.desc_custom_range),
                onAction = onClick
            ) { status, gatedOnClick ->
                val isLocked = status !is AccessStatus.Granted
                
                Row(
                    modifier = Modifier
                        .clickable { if (isLocked) gatedOnClick() else onClick() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = stringResource(id = R.string.desc_custom_range),
                        tint = if (selectedPeriod == AnalyticsPeriod.CUSTOM) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = customRange?.let { formatCustomRangeLabel(it) } ?: stringResource(id = R.string.desc_custom_range),
                        color = if (selectedPeriod == AnalyticsPeriod.CUSTOM) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = stringResource(id = R.string.desc_locked),
                            tint = MaterialTheme.colorScheme.featureGateLock,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        if (selectedPeriod == AnalyticsPeriod.CUSTOM && customRange != null) {
            Text(
                text = stringResource(id = R.string.label_clear),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.clickable(onClick = onClear)
            )
        }
    }
}

@Composable
private fun HeroAnalyticsSection(
    snapshot: AnalyticsSnapshotUi,
    displayMode: HeroDisplayMode,
    onDisplayModeChange: (HeroDisplayMode) -> Unit
) {
    val title = when (displayMode) {
        HeroDisplayMode.EXPENSE -> stringResource(id = R.string.label_total_spending)
        HeroDisplayMode.INCOME -> stringResource(id = R.string.label_total_income)
        HeroDisplayMode.BOTH -> stringResource(id = R.string.label_net_savings)
    }
    
    val amount = when (displayMode) {
        HeroDisplayMode.EXPENSE -> snapshot.expenseDisplay
        HeroDisplayMode.INCOME -> snapshot.incomeDisplay
        HeroDisplayMode.BOTH -> snapshot.savingsDisplay
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            AnimatedTabSwitcher(
                items = HeroDisplayMode.entries.map { TabItem(it, stringResource(id = it.labelRes)) },
                selectedItemId = displayMode,
                onItemSelected = onDisplayModeChange,
                modifier = Modifier.width(180.dp),
                compact = true
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = resolveSummaryLabel(snapshot.summaryLabel),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = amount,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp
                )
            )
            Text(
                text = snapshot.changeDisplay,
                color = if (snapshot.changePercent >= 0) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        AnalyticsLineChart(
            expensePoints = snapshot.expenseChartPoints,
            incomePoints = snapshot.incomeChartPoints,
            labels = snapshot.chartLabels,
            displayMode = displayMode
        )
    }
}

@Composable
private fun AnalyticsLineChart(
    expensePoints: List<Float>,
    incomePoints: List<Float>,
    labels: List<ChartLabelUi>,
    displayMode: HeroDisplayMode
) {
    val expenseColor = MaterialTheme.colorScheme.expense
    val incomeColor = MaterialTheme.colorScheme.income
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val showExpense = displayMode == HeroDisplayMode.EXPENSE || displayMode == HeroDisplayMode.BOTH
    val showIncome = displayMode == HeroDisplayMode.INCOME || displayMode == HeroDisplayMode.BOTH
    
    Column(modifier = Modifier.fillMaxWidth()) {
        val maxExpense = if (showExpense && expensePoints.isNotEmpty()) expensePoints.maxOrNull() ?: 0f else 0f
        val maxIncome = if (showIncome && incomePoints.isNotEmpty()) incomePoints.maxOrNull() ?: 0f else 0f
        val maxValue = maxOf(maxExpense, maxIncome).coerceAtLeast(1f)

        Row(modifier = Modifier.fillMaxWidth().height(170.dp)) {
            Column(
                modifier = Modifier
                    .width(34.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatYAxisAmount(maxValue),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(
                    text = formatYAxisAmount(maxValue / 2),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = stringResource(id = R.string.label_zero),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 28.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    if (!showExpense && !showIncome) return@Canvas
                    
                    val chartHeight = size.height * 0.82f
                    
                    val gridLines = listOf(16.dp.toPx(), (chartHeight + 16.dp.toPx()) / 2)
                    gridLines.forEach { y ->
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    if (showExpense && expensePoints.isNotEmpty()) {
                        val stepX = if (expensePoints.size > 1) size.width / (expensePoints.size - 1) else size.width
                        val normalized = expensePoints.mapIndexed { index, value ->
                            Offset(
                                x = stepX * index,
                                y = chartHeight - ((value / maxValue) * (chartHeight - 16.dp.toPx()))
                            )
                        }
                        
                        val linePath = Path().apply {
                            moveTo(normalized.first().x, normalized.first().y)
                            for (index in 1 until normalized.size) {
                                val previous = normalized[index - 1]
                                val current = normalized[index]
                                val controlX = (previous.x + current.x) / 2f
                                cubicTo(controlX, previous.y, controlX, current.y, current.x, current.y)
                            }
                        }
                        
                        if (displayMode == HeroDisplayMode.EXPENSE) {
                            val fillPath = Path().apply {
                                addPath(linePath)
                                lineTo(normalized.last().x, chartHeight)
                                lineTo(normalized.first().x, chartHeight)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.6f),
                                        primaryColor.copy(alpha = 0.2f),
                                        backgroundColor.copy(alpha = 0.1f)
                                    ),
                                    endY = chartHeight
                                )
                            )
                        }
                        
                        drawPath(
                            path = linePath,
                            color = expenseColor,
                            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    
                    if (showIncome && incomePoints.isNotEmpty()) {
                        val stepX = if (incomePoints.size > 1) size.width / (incomePoints.size - 1) else size.width
                        val normalized = incomePoints.mapIndexed { index, value ->
                            Offset(
                                x = stepX * index,
                                y = chartHeight - ((value / maxValue) * (chartHeight - 16.dp.toPx()))
                            )
                        }
                        
                        val linePath = Path().apply {
                            moveTo(normalized.first().x, normalized.first().y)
                            for (index in 1 until normalized.size) {
                                val previous = normalized[index - 1]
                                val current = normalized[index]
                                val controlX = (previous.x + current.x) / 2f
                                cubicTo(controlX, previous.y, controlX, current.y, current.x, current.y)
                            }
                        }
                        
                        if (displayMode == HeroDisplayMode.INCOME) {
                            val fillPath = Path().apply {
                                addPath(linePath)
                                lineTo(normalized.last().x, chartHeight)
                                lineTo(normalized.first().x, chartHeight)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        incomeColor.copy(alpha = 0.6f),
                                        incomeColor.copy(alpha = 0.2f),
                                        backgroundColor.copy(alpha = 0.1f)
                                    ),
                                    endY = chartHeight
                                )
                            )
                        }
                        
                        drawPath(
                            path = linePath,
                            color = incomeColor,
                            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    drawLine(
                        color = gridColor,
                        start = Offset(0f, chartHeight),
                        end = Offset(size.width, chartHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            Spacer(modifier = Modifier.width(42.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 42.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { labelUi ->
                Text(
                    text = resolveChartLabel(labelUi),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
        
        if (displayMode == HeroDisplayMode.BOTH) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(expenseColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(id = R.string.label_expense), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(20.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(incomeColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(id = R.string.label_income), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun formatYAxisAmount(amount: Float): String {
    return when {
        amount >= 1_000_000f -> stringResource(id = R.string.format_millions, amount / 1_000_000f)
        amount >= 1_000f -> stringResource(id = R.string.format_thousands, amount / 1_000f)
        else -> amount.toInt().toString()
    }
}

@Composable
private fun StatsRow(snapshot: AnalyticsSnapshotUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InsightStatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(id = R.string.title_avg_daily),
            value = snapshot.avgDailyDisplay,
            delta = snapshot.dailyDeltaDisplay,
            deltaColor = if (snapshot.dailyDeltaPercent >= 0) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense,
            deltaBackground = (if (snapshot.dailyDeltaPercent >= 0) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense).copy(alpha = 0.15f),
            icon = Icons.Filled.Wallet,
            iconTint = MaterialTheme.colorScheme.primary
        )
        InsightStatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(id = R.string.title_savings),
            value = snapshot.savingsDisplay,
            delta = snapshot.savingsDeltaDisplay,
            deltaColor = if (snapshot.savingsDeltaPercent >= 0) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense,
            deltaBackground = (if (snapshot.savingsDeltaPercent >= 0) MaterialTheme.colorScheme.income else MaterialTheme.colorScheme.expense).copy(alpha = 0.15f),
            icon = Icons.Filled.ArrowOutward,
            iconTint = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun InsightStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    delta: String,
    deltaColor: Color,
    deltaBackground: Color,
    icon: ImageVector,
    iconTint: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(standardCardGradient())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(standardCardGradient()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(16.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(deltaBackground)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = delta,
                        color = deltaColor,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}

@Composable
private fun CashFlowCard(snapshot: AnalyticsSnapshotUi) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(standardCardGradient())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.label_cash_flow_ratio),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    LegendDot(stringResource(id = R.string.label_income).uppercase(), MaterialTheme.colorScheme.income)
                    LegendDot(stringResource(id = R.string.label_expense).uppercase(), MaterialTheme.colorScheme.expense)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            CashFlowBar(snapshot.incomeFraction, MaterialTheme.colorScheme.income, MaterialTheme.colorScheme.expense)
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = snapshot.incomeDisplay,
                    color = MaterialTheme.colorScheme.income,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = snapshot.expenseDisplay,
                    color = MaterialTheme.colorScheme.expense,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun categoryBreakdownColor(index: Int): Color {
    val colorScheme = MaterialTheme.colorScheme
    return when (index % 3) {
        0 -> colorScheme.primary
        1 -> colorScheme.secondary
        else -> colorScheme.tertiary
    }
}

@Composable
private fun paymentBreakdownColor(index: Int): Color {
    val colorScheme = MaterialTheme.colorScheme
    return when (index % 3) {
        0 -> colorScheme.income
        1 -> colorScheme.primary
        else -> colorScheme.secondary
    }
}

@Composable
private fun CashFlowBar(incomeFraction: Float, incomeColor: Color, expenseColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val incomeWidth = size.width * incomeFraction.coerceIn(0f, 1f)
            drawRoundRect(
                color = incomeColor,
                size = Size(incomeWidth, size.height),
                cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
            )
            drawRoundRect(
                color = expenseColor,
                topLeft = Offset(incomeWidth, 0f),
                size = Size(size.width - incomeWidth, size.height),
                cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
            )
        }
    }
}

@Composable
private fun CategoryCard(
    modifier: Modifier = Modifier,
    snapshot: AnalyticsSnapshotUi,
    onViewAllClick: () -> Unit,
    onShowTransactions: (Int, String) -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(standardCardGradient())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.label_top_spending_by_category),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                         fontSize = 16.sp,
                        lineHeight = 28.sp
                    )
                )
                if (snapshot.allCategoryBreakdown.isNotEmpty()) {
                    Text(
                        text = stringResource(id = R.string.label_view_all),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp
                        ),
                        modifier = Modifier.clickable(onClick = onViewAllClick)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            SpendingDonutChart(snapshot.categoryBreakdown, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(20.dp))
            if (snapshot.categoryBreakdown.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.label_no_category_spending_found_in),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    snapshot.categoryBreakdown.forEach { category ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(categoryBreakdownColor(category.colorIndex))
                                )
                                Text(
                                    text = if (category.isOther) stringResource(id = R.string.label_other) else category.label,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.format_percentage, category.percentLabel),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = stringResource(id = R.string.desc_show_transactions),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { onShowTransactions(category.id, category.label) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryBreakdownBottomSheet(
    categories: List<CategoryBreakdownUi>,
    onDismiss: () -> Unit,
    onShowTransactions: (Int, String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.label_all_categories),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.label_ranked_by_spending_for_the_sel),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (categories.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.label_no_category_spending_found_in),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(categories.size) { index ->
                        CategoryBreakdownRow(
                            rank = index + 1,
                            category = categories[index],
                            onShowTransactions = onShowTransactions
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(
    rank: Int,
    category: CategoryBreakdownUi,
    onShowTransactions: (Int, String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(standardCardGradient()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rank.toString(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(categoryBreakdownColor(category.colorIndex))
                    )
                    Text(
                        text = if (category.isOther) stringResource(id = R.string.label_other) else category.label,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.format_percentage, category.percentLabel),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = stringResource(id = R.string.desc_show_transactions),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onShowTransactions(category.id, category.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(category.fraction.coerceIn(0f, 1f))
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(categoryBreakdownColor(category.colorIndex))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = category.amountDisplay,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SpendingDonutChart(breakdown: List<CategoryBreakdownUi>, modifier: Modifier = Modifier) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val segmentColors = breakdown.map { categoryBreakdownColor(it.colorIndex) }

    Box(modifier = modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            var startAngle = -180f
            val gap = 5f
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            breakdown.forEachIndexed { index, segment ->
                val sweep = (segment.fraction * 360f) - gap
                drawArc(
                    color = segmentColors[index],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweep + gap
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.label_top_val, (breakdown.firstOrNull()?.let { if (it.isOther) stringResource(id = R.string.label_other) else it.label } ?: stringResource(id = R.string.label_not_available))),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TopSpendingCard(
    modifier: Modifier = Modifier,
    topTransactions: List<TopSpendingItemUi>,
    dateFormatPattern: String,
    onViewAllClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(standardCardGradient())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.label_top_spending),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                if (topTransactions.isNotEmpty()) {
                    Text(
                        text = stringResource(id = R.string.label_view_all),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp
                        ),
                        modifier = Modifier.clickable(onClick = onViewAllClick)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            if (topTransactions.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.label_no_spending_transactions_in_th),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    topTransactions.forEach { transaction ->
                        TopSpendingRow(transaction = transaction, dateFormatPattern = dateFormatPattern)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopSpendingRow(
    transaction: TopSpendingItemUi,
    dateFormatPattern: String
) {
    val truncatedNote = if (transaction.note.length > 10) {
        stringResource(id = R.string.format_ellipsis, transaction.note.take(10))
    } else {
        transaction.note
    }
    val categoryLabel = if (transaction.isGeneral) stringResource(id = R.string.label_general) else transaction.categoryLabel

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(standardCardGradient()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transaction.icon,
                contentDescription = transaction.note,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = truncatedNote,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = categoryLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.separator_bullet),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatDate(transaction.createdAt, dateFormatPattern),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            text = transaction.amountDisplay,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun SmartTipCard(
    modifier: Modifier = Modifier,
    tip: SmartTipUi
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(standardCardGradient())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(brandGradient()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = stringResource(id = R.string.desc_ai_tip),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.label_smart_ai_tip),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = resolveSmartTip(tip),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 23.sp)
                )
            }
            SparkleCluster()
        }
    }
}

@Composable
private fun SparkleCluster() {
    val sparkleColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(
        modifier = Modifier
            .size(34.dp)
            .aspectRatio(1f)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = min(size.width, size.height) / 2.5f
        val innerRadius = outerRadius / 2.2f
        val path = Path()
        for (index in 0 until 8) {
            val angle = (PI / 4 * index) - PI / 2
            val radius = if (index % 2 == 0) outerRadius else innerRadius
            val x = center.x + (cos(angle) * radius).toFloat()
            val y = center.y + (sin(angle) * radius).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path, color = sparkleColor)
    }
}

@Composable
private fun PaymentTypeCard(
    modifier: Modifier = Modifier,
    snapshot: AnalyticsSnapshotUi,
    onViewAllClick: () -> Unit,
    onShowTransactions: (Int, String) -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(standardCardGradient())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.label_top_spending_by_payment_mode),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                         fontSize = 16.sp,
                        lineHeight = 28.sp
                    )
                )
                if (snapshot.allPaymentTypeBreakdown.isNotEmpty()) {
                    Text(
                        text = stringResource(id = R.string.label_view_all),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp
                        ),
                        modifier = Modifier.clickable(onClick = onViewAllClick)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            PaymentDonutChart(snapshot.paymentTypeBreakdown, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(20.dp))
            if (snapshot.paymentTypeBreakdown.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.label_no_payment_data_found_in_this),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    snapshot.paymentTypeBreakdown.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(paymentBreakdownColor(item.colorIndex))
                                )
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (item.isOther) stringResource(id = R.string.label_wallet) else item.label,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.format_percentage, item.percentLabel),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = stringResource(id = R.string.desc_show_transactions),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { onShowTransactions(item.id, item.label) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentDonutChart(breakdown: List<PaymentTypeBreakdownUi>, modifier: Modifier = Modifier) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val segmentColors = breakdown.map { paymentBreakdownColor(it.colorIndex) }

    Box(modifier = modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            var startAngle = -180f
            val gap = 5f
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            breakdown.forEachIndexed { index, segment ->
                val sweep = (segment.fraction * 360f) - gap
                drawArc(
                    color = segmentColors[index],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweep + gap
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = breakdown.firstOrNull()?.icon ?: Icons.Filled.Wallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = breakdown.firstOrNull()?.let { if (it.isOther) stringResource(id = R.string.label_wallet) else it.label } ?: stringResource(id = R.string.label_not_available),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentTypeBreakdownBottomSheet(
    categories: List<PaymentTypeBreakdownUi>,
    onDismiss: () -> Unit,
    onShowTransactions: (Int, String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            val filteredCategories = remember(categories) {
                categories.filter { it.fraction > 0f }
            }
            
            Text(
                text = stringResource(id = R.string.label_spending_by_payment_mode),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.label_spending_by_payment_mode_breakdown),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (filteredCategories.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.label_no_payment_data_found_in_this),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredCategories.size) { index ->
                        PaymentBreakdownRow(
                            rank = index + 1,
                            item = filteredCategories[index],
                            onShowTransactions = onShowTransactions
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentBreakdownRow(
    rank: Int,
    item: PaymentTypeBreakdownUi,
    onShowTransactions: (Int, String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(standardCardGradient()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rank.toString(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (item.isOther) stringResource(id = R.string.label_wallet) else item.label,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.format_percentage, item.percentLabel),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = stringResource(id = R.string.desc_show_transactions),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onShowTransactions(item.id, item.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.fraction.coerceIn(0f, 1f))
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(paymentBreakdownColor(item.colorIndex))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.amountDisplay,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilteredTransactionsBottomSheet(
    label: String,
    transactions: List<Transaction>,
    categories: List<CategoryType>,
    paymentTypes: List<PaymentType>,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    dateFormatPattern: String,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.label_val_transactions, label),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (transactions.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.label_no_transactions_found_in_this),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                val dateShortFormat = stringResource(id = R.string.format_date_short)
                LazyColumn(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(transactions.size) { index ->
                        val transaction = transactions[index]
                        val category = categories.find { it.id == transaction.categoryId }
                        val payment = paymentTypes.find { it.id == transaction.paymentTypeId }
                        
                        TransactionCard(
                            note = transaction.note,
                            transactionDate = formatDate(transaction.createdAt, dateShortFormat),
                            transactionTime = com.mkn0079.expensetracker.utils.formatTime(transaction.createdAt, "24-hour"),
                            amount = formatCurrencyValue(transaction.amount, currencyId, amountFormatPreferences),
                            transactionTypeId = transaction.transactionTypeId,
                            icon = category?.icon ?: Icons.Filled.QuestionMark,
                            paymentType = payment?.name ?: stringResource(id = R.string.label_unknown),
                            categoryLabel = category?.name ?: stringResource(id = R.string.label_other)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.PremiumLockedOverlay(
    modifier: Modifier = Modifier,
    displayText: String,
    icon: ImageVector = Icons.Filled.Lock,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .matchParentSize()
            .clip(RoundedCornerShape(30.dp))
            .clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.featureGateLock,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = displayText,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopSpendingBottomSheet(
    transactions: List<TopSpendingItemUi>,
    dateFormatPattern: String,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.label_top_spending),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.label_your_highest_expenses_in_the_s),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (transactions.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.label_no_spending_data_found),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(transactions.size) { index ->
                        TopSpendingRow(transaction = transactions[index], dateFormatPattern = dateFormatPattern)
                    }
                }
            }
        }
    }
}

@Composable
private fun resolveSummaryLabel(summary: SummaryLabelUi): String {
    return when {
        summary.resId != null -> stringResource(id = summary.resId)
        summary.datePattern != null && summary.timestamp != null -> {
            SimpleDateFormat(summary.datePattern, Locale.getDefault()).format(Date(summary.timestamp))
        }
        summary.customRange != null -> summary.customRange
        else -> ""
    }
}

@Composable
private fun resolveSmartTip(tip: SmartTipUi): String {
    return if (tip.resId == R.string.msg_spending_trend) {
        stringResource(
            id = tip.resId,
            tip.flowChange ?: "",
            tip.directionResId?.let { stringResource(id = it) } ?: "",
            tip.topCategory ?: stringResource(id = R.string.label_spending),
            tip.savingAmount ?: ""
        )
    } else {
        stringResource(id = tip.resId)
    }
}

@Composable
private fun resolveChartLabel(label: ChartLabelUi): String {
    return when {
        label.resId != null && label.index != null -> {
            val array = stringArrayResource(id = label.resId)
            if (label.index in array.indices) array[label.index] else ""
        }
        label.label != null -> label.label
        else -> ""
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyticsScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        AnalyticsScreen(dateFormatPattern = "dd MMM yyyy")
    }
}
