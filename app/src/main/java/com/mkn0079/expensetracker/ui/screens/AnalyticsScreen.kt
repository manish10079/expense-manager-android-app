package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.transactionList
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.components.GatedAction
import com.mkn0079.expensetracker.ui.components.WheelDateTimePickerModal
import com.mkn0079.expensetracker.ui.components.WheelPickerMode
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurplePrimary
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.ui.theme.income
import com.mkn0079.expensetracker.ui.theme.expense
import com.mkn0079.expensetracker.ui.viewmodels.AnalyticsPeriod
import com.mkn0079.expensetracker.ui.viewmodels.AnalyticsSnapshotUi
import com.mkn0079.expensetracker.ui.viewmodels.AnalyticsViewModel
import com.mkn0079.expensetracker.utils.defaultAmountFormatPreferences
import com.mkn0079.expensetracker.ui.viewmodels.CategoryBreakdownUi
import com.mkn0079.expensetracker.ui.viewmodels.PaymentTypeBreakdownUi
import com.mkn0079.expensetracker.ui.viewmodels.TopSpendingItemUi
import com.mkn0079.expensetracker.ui.viewmodels.buildCustomRangeHeadline
import com.mkn0079.expensetracker.ui.viewmodels.formatCustomRangeLabel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

enum class HeroDisplayMode(val label: String) {
    EXPENSE("Expense"),
    INCOME("Income"),
    BOTH("Both")
}

@Composable
fun AnalyticsScreen(
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    transactions: List<Transaction> = transactionList,
    categories: List<CategoryType> = emptyList(),
    paymentMethods: List<PaymentType> = emptyList(),
    onBackClick: () -> Unit = {},
    analyticsViewModel: AnalyticsViewModel = viewModel()
) {
    var isCustomRangePickerVisible by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var isCategorySheetVisible by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var isPaymentSheetVisible by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var isTopSpendingSheetVisible by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var heroDisplayMode by rememberSaveable { mutableStateOf(HeroDisplayMode.EXPENSE) }


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
    val customRange = uiState.customRange
    val snapshot = uiState.snapshot

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, top = 20.dp, end = 22.dp)
        ) {
            AppHeader(title = "Analytics", onBackClick = onBackClick)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 22.dp, top = 18.dp, end = 22.dp, bottom = 142.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                PeriodTabs(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelected = { period ->
                        if (period == AnalyticsPeriod.YEAR) {
                            // Handled via GatedAction wrapper in PeriodTabs or here
                        }
                        analyticsViewModel.selectPeriod(period)
                    }
                )
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
                    displayName = "Category Breakdown",
                    onAction = {}
                ) { status, onClick ->
                    val isLocked = status !is AccessStatus.Granted
                    Box {
                        CategoryCard(
                            modifier = if (isLocked) Modifier.blur(16.dp) else Modifier,
                            snapshot = snapshot,
                            onViewAllClick = { isCategorySheetVisible = true }
                        )
                        if (isLocked) {
                            PremiumLockedOverlay(
                                displayText = "Unlock Breakdown",
                                onClick = onClick
                            )
                        }
                    }
                }
            }
            item {
                GatedAction(
                    feature = Feature.ANALYTICS_PAYMENT_BREAKDOWN,
                    displayName = "Payment Mode Breakdown",
                    onAction = {}
                ) { status, onClick ->
                    val isLocked = status !is AccessStatus.Granted
                    Box {
                        PaymentTypeCard(
                            modifier = if (isLocked) Modifier.blur(16.dp) else Modifier,
                            snapshot = snapshot,
                            onViewAllClick = { isPaymentSheetVisible = true }
                        )
                        if (isLocked) {
                            PremiumLockedOverlay(
                                displayText = "Unlock Breakdown",
                                onClick = onClick
                            )
                        }
                    }
                }
            }
            item {
                GatedAction(
                    feature = Feature.ANALYTICS_TOP_SPENDING,
                    displayName = "Top Spending Details",
                    onAction = {}
                ) { status, onClick ->
                    Box {
                        val isLocked = status !is AccessStatus.Granted
                        TopSpendingCard(
                            modifier = if (isLocked) Modifier.blur(16.dp) else Modifier,
                            topTransactions = snapshot.topTransactions,
                            onViewAllClick = { isTopSpendingSheetVisible = true }
                        )
                        if (isLocked) {
                            PremiumLockedOverlay(
                                displayText = "Unlock Top Spending",
                                onClick = onClick
                            )
                        }
                    }
                }
            }
            item {
                GatedAction(
                    feature = Feature.ANALYTICS_SMART_TIPS,
                    displayName = "Spending Insights",
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
                                displayText = "Unlock Insights",
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
            displayName = "Full Category Breakdown",
            onAction = { isCategorySheetVisible = true }
        ) { status, onClick ->
            if (status is AccessStatus.Granted) {
                CategoryBreakdownBottomSheet(
                    categories = snapshot.allCategoryBreakdown,
                    onDismiss = { isCategorySheetVisible = false }
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
            displayName = "Full Payment Breakdown",
            onAction = { isPaymentSheetVisible = true }
        ) { status, onClick ->
            if (status is AccessStatus.Granted) {
                PaymentTypeBreakdownBottomSheet(
                    categories = snapshot.allPaymentTypeBreakdown,
                    onDismiss = { isPaymentSheetVisible = false }
                )
            } else {
                LaunchedEffect(Unit) { 
                    isPaymentSheetVisible = false
                    onClick()
                }
            }
        }
    }

    if (isTopSpendingSheetVisible) {
        TopSpendingBottomSheet(
            transactions = snapshot.allTopTransactions,
            onDismiss = { isTopSpendingSheetVisible = false }
        )
    }
}


@Composable
private fun PeriodTabs(
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit
) {
    val periods = remember { AnalyticsPeriod.entries.filter { it != AnalyticsPeriod.CUSTOM } }
    val selectedIndex = periods.indexOf(selectedPeriod)
    val isTabSelected = selectedIndex != -1

    val density = androidx.compose.ui.platform.LocalDensity.current
    var containerWidthPx by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .onSizeChanged { containerWidthPx = it.width }
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF17171A))
            .padding(4.dp)
    ) {
        val tabWidth = with(density) { (containerWidthPx.toDp() - 8.dp) / periods.size }
        
        val indicatorOffset by animateDpAsState(
            targetValue = if (isTabSelected) tabWidth * selectedIndex else 0.dp,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "period_indicator_offset"
        )

        val indicatorAlpha by animateFloatAsState(
            targetValue = if (isTabSelected) 1f else 0f,
            label = "period_indicator_alpha"
        )

        // Sliding indicator (Pill)
        if (indicatorAlpha > 0f && containerWidthPx > 0) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .graphicsLayer { alpha = indicatorAlpha }
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PurplePrimary, Color(0xFFB89AF7))
                        )
                    )
            )
        }

        // Tab Content
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            periods.forEach { period ->
                val selected = period == selectedPeriod
                
                if (period == AnalyticsPeriod.YEAR) {
                    GatedAction(
                        feature = Feature.ANALYTICS_PERIOD_YEAR,
                        displayName = "Yearly Analytics",
                        onAction = { onPeriodSelected(period) }
                    ) { status, onClick ->
                        val isLocked = status !is AccessStatus.Granted
                        PeriodTabItem(
                            period = period,
                            selected = selected,
                            isLocked = isLocked,
                            onClick = { if (isLocked) onClick() else onPeriodSelected(period) }
                        )
                    }
                } else {
                    PeriodTabItem(
                        period = period,
                        selected = selected,
                        isLocked = false,
                        onClick = { onPeriodSelected(period) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.PeriodTabItem(
    period: AnalyticsPeriod,
    selected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = when {
            selected -> Color(0xFF24114C)
            isLocked -> Color(0xFF7B748A)
            else -> Color(0xFFD9D0E8)
        },
        label = "period_text_color"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = period.label,
                color = animatedColor,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            if (isLocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = animatedColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
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
                            colors = listOf(Color(0xFF271B42), Color(0xFF43306C))
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF161519), Color(0xFF161519))
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (selectedPeriod == AnalyticsPeriod.CUSTOM) {
                        Color(0xFF6C50BE)
                    } else {
                        Color(0xFF26242A)
                    },
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            GatedAction(
                feature = Feature.ANALYTICS_CUSTOM_RANGE,
                displayName = "Custom Range Analytics",
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
                        contentDescription = "Custom range",
                        tint = if (selectedPeriod == AnalyticsPeriod.CUSTOM) Color(0xFFF0E9FF) else PurpleAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = customRange?.let { formatCustomRangeLabel(it) } ?: "Custom Range",
                        color = if (selectedPeriod == AnalyticsPeriod.CUSTOM) Color(0xFFF0E9FF) else Color(0xFFD1CADF),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Locked",
                            tint = if (selectedPeriod == AnalyticsPeriod.CUSTOM) Color(0xFFF0E9FF) else Color(0xFFD1CADF),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        if (selectedPeriod == AnalyticsPeriod.CUSTOM && customRange != null) {
            Text(
                text = "Clear",
                color = PurpleAccent,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.clickable(onClick = onClear)
            )
        }
    }
}

@Composable
private fun HeroToggle(
    selectedMode: HeroDisplayMode,
    onModeSelected: (HeroDisplayMode) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1F1928))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        HeroDisplayMode.values().forEach { mode ->
            val isSelected = mode == selectedMode
            val backgroundColor by animateColorAsState(
                if (isSelected) PurplePrimary else Color.Transparent,
                label = "bg"
            )
            val textColor by animateColorAsState(
                if (isSelected) Color.White else Color(0xFFA49CB4),
                label = "text"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.label,
                    color = textColor,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
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
        HeroDisplayMode.EXPENSE -> "TOTAL SPENT"
        HeroDisplayMode.INCOME -> "TOTAL INCOME"
        HeroDisplayMode.BOTH -> "NET BALANCE"
    }
    
    val amount = when (displayMode) {
        HeroDisplayMode.EXPENSE -> snapshot.expenseDisplay
        HeroDisplayMode.INCOME -> snapshot.incomeDisplay
        HeroDisplayMode.BOTH -> snapshot.savingsDisplay
    }
    
    val changePercent = when (displayMode) {
        HeroDisplayMode.EXPENSE -> -snapshot.changePercent // Inverting because lower expense is good? No, let's keep it simple.
        HeroDisplayMode.INCOME -> snapshot.changePercent // Need income delta? ViewModel doesn't have separate delta yet.
        HeroDisplayMode.BOTH -> snapshot.changePercent // Using flow delta for now
    }
    
    // Actually, snapshot.changeDisplay is flow delta. We should probably just use it or simplify.
    // For now, let's use the provided changeDisplay but handle color logic.

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color(0xFF7B748A),
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            HeroToggle(selectedMode = displayMode, onModeSelected = onDisplayModeChange)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = snapshot.summaryLabel,
            color = Color(0xFFA49CB4),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = amount,
                color = Color(0xFFF1EDF7),
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
    labels: List<String>,
    displayMode: HeroDisplayMode
) {
    val expenseColor = MaterialTheme.colorScheme.expense
    val incomeColor = MaterialTheme.colorScheme.income
    val showExpense = displayMode == HeroDisplayMode.EXPENSE || displayMode == HeroDisplayMode.BOTH
    val showIncome = displayMode == HeroDisplayMode.INCOME || displayMode == HeroDisplayMode.BOTH
    
    Column(modifier = Modifier.fillMaxWidth()) {
        val maxExpense = if (showExpense && expensePoints.isNotEmpty()) expensePoints.maxOrNull() ?: 0f else 0f
        val maxIncome = if (showIncome && incomePoints.isNotEmpty()) incomePoints.maxOrNull() ?: 0f else 0f
        val maxValue = maxOf(maxExpense, maxIncome).coerceAtLeast(1f)

        Row(modifier = Modifier.fillMaxWidth().height(170.dp)) {
            // Y-Axis Labels
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
                    text = "0",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 28.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
            if (!showExpense && !showIncome) return@Canvas
            
            val chartHeight = size.height * 0.82f
            
            // Draw horizontal grid lines
            val gridLines = listOf(16.dp.toPx(), (chartHeight + 16.dp.toPx()) / 2)
            gridLines.forEach { y ->
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Draw Expense Line
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
                            colors = listOf(Color(0x994B2E8D), Color(0x661E1336), Color(0x22100C1A)),
                            endY = chartHeight
                        )
                    )
                }
                
                drawPath(
                    path = linePath,
                    color = expenseColor,
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                )
                
                if (displayMode == HeroDisplayMode.EXPENSE) {
                    normalized.maxByOrNull { it.y * -1 }?.let { peak ->
                        drawCircle(color = Color(0x33DECFFF), radius = 9.dp.toPx(), center = peak)
                        drawCircle(color = Color(0xFFDECFFF), radius = 4.dp.toPx(), center = peak)
                    }
                }
            }
            
            // Draw Income Line
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
                            colors = listOf(Color(0x992E8D4B), Color(0x6613361E), Color(0x220C1A10)),
                            endY = chartHeight
                        )
                    )
                }
                
                drawPath(
                    path = linePath,
                    color = incomeColor,
                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                )
                
                if (displayMode == HeroDisplayMode.INCOME) {
                    normalized.maxByOrNull { it.y * -1 }?.let { peak ->
                        drawCircle(color = Color(0x33CFFFDE), radius = 9.dp.toPx(), center = peak)
                        drawCircle(color = Color(0xFFCFFFDE), radius = 4.dp.toPx(), center = peak)
                    }
                }
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
            labels.forEach { label ->
                Text(
                    text = label,
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
                Text("Expense", color = Color(0xFFA49CB4), style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(20.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(incomeColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Income", color = Color(0xFFA49CB4), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun formatYAxisAmount(amount: Float): String {
    return when {
        amount >= 1_000_000f -> String.format(java.util.Locale.US, "%.1fM", amount / 1_000_000f)
        amount >= 1_000f -> String.format(java.util.Locale.US, "%.1fk", amount / 1_000f)
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
            title = "AVG DAILY",
            value = snapshot.avgDailyDisplay,
            delta = snapshot.dailyDeltaDisplay,
            deltaColor = Color(0xFFFF9B90),
            deltaBackground = Color(0xFF4B1E20),
            icon = Icons.Filled.Wallet,
            iconTint = Color(0xFFE0CEFF)
        )
        InsightStatCard(
            modifier = Modifier.weight(1f),
            title = "SAVINGS",
            value = snapshot.savingsDisplay,
            delta = snapshot.savingsDeltaDisplay,
            deltaColor = Color(0xFFDCCEFF),
            deltaBackground = Color(0xFF38275A),
            icon = Icons.Filled.ArrowOutward,
            iconTint = Color(0xFFFFC59A)
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
    Surface(modifier = modifier, shape = RoundedCornerShape(30.dp), color = Color(0xFF1C1B1E)) {
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
                        .size(28.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF26202E)),
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
                color = Color(0xFFADA5BC),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color(0xFFF0EBF8),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}

@Composable
private fun CashFlowCard(snapshot: AnalyticsSnapshotUi) {
    Surface(shape = RoundedCornerShape(30.dp), color = Color(0xFF1C1B1E)) {
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
                    text = "Cash Flow Ratio",
                    color = Color(0xFFF0EBF8),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    LegendDot("INCOME", MaterialTheme.colorScheme.income)
                    LegendDot("EXPENSE", MaterialTheme.colorScheme.expense)
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
        Text(text = label, color = Color(0xFFCFC8DB), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun CashFlowBar(incomeFraction: Float, incomeColor: Color, expenseColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(Color(0xFF2B2830))
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
    onViewAllClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        color = Color(0xFF1C1B1E)
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
                    text = "Top Spending by\nCategory",
                    color = Color(0xFFF0EBF8),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 28.sp
                    )
                )
                if (snapshot.allCategoryBreakdown.isNotEmpty()) {
                    Text(
                        text = "VIEW ALL",
                        color = PurpleAccent,
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
                    text = "No category spending found in this range.",
                    color = Color(0xFFA49CB4),
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(category.color)
                                )
                                Text(
                                    text = category.label,
                                    color = Color(0xFFD7D2E1),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Text(
                                text = "${category.percentLabel}%",
                                color = Color(0xFFF0EBF8),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
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
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF17161A),
        contentColor = Color(0xFFF0EBF8),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            Text(
                text = "All Categories",
                color = Color(0xFFF0EBF8),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ranked by spending for the selected analytics range.",
                color = Color(0xFFA49CB4),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (categories.isEmpty()) {
                Text(
                    text = "No category spending found in this range.",
                    color = Color(0xFFA49CB4),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                Column(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    categories.forEachIndexed { index, category ->
                        CategoryBreakdownRow(
                            rank = index + 1,
                            category = category
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
    category: CategoryBreakdownUi
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1F1D23)
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF26232C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rank.toString(),
                            color = Color(0xFFD9D2E8),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(category.color)
                    )
                    Text(
                        text = category.label,
                        color = Color(0xFFF0EBF8),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Text(
                    text = "${category.percentLabel}%",
                    color = Color(0xFFF0EBF8),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2B2830))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(category.fraction.coerceIn(0f, 1f))
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(category.color)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = category.amountDisplay,
                color = Color(0xFFA49CB4),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SpendingDonutChart(breakdown: List<CategoryBreakdownUi>, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            var startAngle = -180f
            val gap = 5f
            drawArc(
                color = Color(0xFF302E33),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            breakdown.forEach { segment ->
                val sweep = (segment.fraction * 360f) - gap
                drawArc(
                    color = segment.color,
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
                text = "Top: ${breakdown.firstOrNull()?.label ?: "N/A"}",
                color = Color(0xFFF0EBF8),
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
    onViewAllClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        color = Color(0xFF1C1B1E)
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
                    text = "Top Spending",
                    color = Color(0xFFF0EBF8),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                if (topTransactions.isNotEmpty()) {
                    Text(
                        text = "VIEW ALL",
                        color = PurpleAccent,
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
                    text = "No spending transactions in the selected range.",
                    color = Color(0xFFA49CB4),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    topTransactions.forEach { transaction ->
                        TopSpendingRow(transaction = transaction)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopSpendingRow(
    transaction: TopSpendingItemUi
) {
    val truncatedNote = transaction.note.truncateWithEllipsis(maxCharacters = 10)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF2A2830)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transaction.icon,
                contentDescription = transaction.note,
                tint = PurpleAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = truncatedNote,
                color = Color(0xFFF0EBF8),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = transaction.categoryLabel,
                color = Color(0xFF9C95AB),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = transaction.amountDisplay,
            color = Color(0xFFF0EBF8),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

private fun String.truncateWithEllipsis(maxCharacters: Int): String {
    if (length <= maxCharacters) return this
    return take(maxCharacters) + "..."
}

@Composable
private fun SmartTipCard(
    modifier: Modifier = Modifier,
    tip: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        color = Color(0xFF161518)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF201A29), Color(0xFF111013))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(colors = listOf(PurplePrimary, Color(0xFF8C63FF)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "AI Tip",
                    tint = Color(0xFF24114C)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Smart AI Tip",
                    color = Color(0xFFF0EBF8),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tip,
                    color = Color(0xFFD8D1E6),
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 23.sp)
                )
            }
            SparkleCluster()
        }
    }
}

@Composable
private fun SparkleCluster() {
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
        drawPath(path, color = Color(0xFF2E2737))
    }
}

@Composable
private fun PaymentTypeCard(
    modifier: Modifier = Modifier,
    snapshot: AnalyticsSnapshotUi,
    onViewAllClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        color = Color(0xFF1C1B1E)
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
                    text = "Top Spending by\nPayment Mode",
                    color = Color(0xFFF0EBF8),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 28.sp
                    )
                )
                if (snapshot.allPaymentTypeBreakdown.isNotEmpty()) {
                    Text(
                        text = "VIEW ALL",
                        color = PurpleAccent,
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
                    text = "No payment data found in this range.",
                    color = Color(0xFFA49CB4),
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(item.color)
                                )
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = Color(0xFFA49CB4),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = item.label,
                                    color = Color(0xFFD7D2E1),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Text(
                                text = "${item.percentLabel}%",
                                color = Color(0xFFF0EBF8),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentDonutChart(breakdown: List<PaymentTypeBreakdownUi>, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            var startAngle = -180f
            val gap = 5f
            drawArc(
                color = Color(0xFF302E33),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            breakdown.forEach { segment ->
                val sweep = (segment.fraction * 360f) - gap
                drawArc(
                    color = segment.color,
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
                tint = Color(0xFFF0EBF8),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = breakdown.firstOrNull()?.label ?: "N/A",
                color = Color(0xFFA49CB4),
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
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF17161A),
        contentColor = Color(0xFFF0EBF8),
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
                text = "Spending by Payment Mode",
                color = Color(0xFFF0EBF8),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Breakdown of expenses based on the wallet or payment method used.",
                color = Color(0xFFA49CB4),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (filteredCategories.isEmpty()) {
                Text(
                    text = "No payment data found in this range.",
                    color = Color(0xFFA49CB4),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                Column(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    filteredCategories.forEachIndexed { index, category ->
                        PaymentBreakdownRow(
                            rank = index + 1,
                            item = category
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
    item: PaymentTypeBreakdownUi
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1F1D23)
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF26232C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rank.toString(),
                            color = Color(0xFFD9D2E8),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = Color(0xFFA49CB4),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = item.label,
                        color = Color(0xFFF0EBF8),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Text(
                    text = "${item.percentLabel}%",
                    color = Color(0xFFF0EBF8),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2B2830))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.fraction.coerceIn(0f, 1f))
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(item.color)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.amountDisplay,
                color = Color(0xFFA49CB4),
                style = MaterialTheme.typography.bodyMedium
            )
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
            color = Color.Black.copy(alpha = 0.45f)
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
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = displayText,
                    color = Color.White,
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
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF17161A),
        contentColor = Color(0xFFF0EBF8),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Top Spending",
                color = Color(0xFFF0EBF8),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your highest expenses in the selected period.",
                color = Color(0xFFA49CB4),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (transactions.isEmpty()) {
                Text(
                    text = "No spending data found.",
                    color = Color(0xFFA49CB4),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                Column(
                    modifier = Modifier.padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    transactions.forEach { transaction ->
                        TopSpendingRow(transaction = transaction)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun AnalyticsScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        AnalyticsScreen()
    }
}
