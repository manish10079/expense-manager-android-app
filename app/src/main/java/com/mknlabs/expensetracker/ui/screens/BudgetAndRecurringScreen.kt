@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.mknlabs.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mknlabs.expensetracker.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mknlabs.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.transactionList
import com.mknlabs.expensetracker.models.AmountFormatPreferences
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.Transaction
import com.mknlabs.expensetracker.ui.components.AppHeader
import com.mknlabs.expensetracker.ui.components.GatedAction
import com.mknlabs.expensetracker.ui.components.WheelDateTimePickerModal
import com.mknlabs.expensetracker.ui.components.WheelPickerMode
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.Feature
import com.mknlabs.expensetracker.ui.theme.brandGradient
import com.mknlabs.expensetracker.ui.theme.standardCardGradient
import com.mknlabs.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mknlabs.expensetracker.ui.theme.expense
import com.mknlabs.expensetracker.ui.theme.featureGateLock
import com.mknlabs.expensetracker.ui.theme.Dimens
import com.mknlabs.expensetracker.ui.theme.income
import com.mknlabs.expensetracker.ui.viewmodels.BudgetAndRecurringViewModel
import com.mknlabs.expensetracker.ui.viewmodels.BudgetTab
import com.mknlabs.expensetracker.ui.viewmodels.BudgetPeriodFilter
import com.mknlabs.expensetracker.ui.viewmodels.BudgetAccent
import com.mknlabs.expensetracker.ui.viewmodels.BudgetSummaryUi
import com.mknlabs.expensetracker.ui.viewmodels.BudgetCategoryBudgetUi
import com.mknlabs.expensetracker.ui.viewmodels.BudgetRecurringExpenseUi
import com.mknlabs.expensetracker.models.RecurringFrequency
import com.mknlabs.expensetracker.utils.defaultAmountFormatPreferences
import com.mknlabs.expensetracker.utils.datePickerSelectionToLocalDateTimestamp
import com.mknlabs.expensetracker.utils.formatCurrencyValue

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mknlabs.expensetracker.ui.components.AdContainer
import com.mknlabs.expensetracker.ui.components.NativeAdCard
import com.mknlabs.expensetracker.monetization.AdPlacement

@Composable
fun BudgetAndRecurringScreen(
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    transactions: List<Transaction> = transactionList,
    availableCategories: List<CategoryType> = categoryMap.values.toList(),
    recurringRules: List<RecurringTransactionRule> = emptyList(),
    onDeleteRecurring: (String) -> Unit = {},
    onRecurringEnabledChange: (String, Boolean) -> Unit = { _, _ -> },
    onUpdateRecurringRule: (String, RecurringFrequency, Int) -> Unit = { _, _, _ -> },
    onBackClick: () -> Unit = {},
    isAdsEnabled: Boolean = false
) {
    val budgetViewModel: BudgetAndRecurringViewModel = hiltViewModel()

    LaunchedEffect(transactions, availableCategories, currencyId, amountFormatPreferences, recurringRules) {
        budgetViewModel.updateInputs(
            transactions = transactions,
            categories = availableCategories,
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            recurringRules = recurringRules
        )
    }

    val uiState by budgetViewModel.uiState.collectAsStateWithLifecycle()

    BudgetAndRecurringContent(
        uiState = uiState,
        isAdsEnabled = isAdsEnabled,
        currencyId = currencyId,
        amountFormatPreferences = amountFormatPreferences,
        availableCategories = availableCategories,
        transactions = transactions,
        onDeleteRecurring = onDeleteRecurring,
        onRecurringEnabledChange = onRecurringEnabledChange,
        onUpdateRecurringRule = onUpdateRecurringRule,
        onBackClick = onBackClick,
        onSelectTab = { budgetViewModel.selectTab(it) },
        onSelectPeriod = { budgetViewModel.selectPeriod(it) },
        onSelectCustomMonth = { budgetViewModel.selectCustomMonth(it) },
        onUpdateBudget = { budgetId, categoryId, limit -> budgetViewModel.updateBudget(budgetId, categoryId, limit) },
        onAddBudget = { categoryId, limit -> budgetViewModel.addBudget(categoryId, limit) },
        onDeleteBudget = { budgetId -> budgetViewModel.deleteBudget(budgetId) }
    )
}

@Composable
private fun BudgetAndRecurringContent(
    uiState: com.mknlabs.expensetracker.ui.viewmodels.BudgetAndRecurringScreenUiState,
    isAdsEnabled: Boolean,
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    availableCategories: List<CategoryType>,
    transactions: List<Transaction>,
    onDeleteRecurring: (String) -> Unit,
    onRecurringEnabledChange: (String, Boolean) -> Unit,
    onUpdateRecurringRule: (String, RecurringFrequency, Int) -> Unit,
    onBackClick: () -> Unit,
    onSelectTab: (BudgetTab) -> Unit,
    onSelectPeriod: (BudgetPeriodFilter) -> Unit,
    onSelectCustomMonth: (Long) -> Unit,
    onUpdateBudget: (String, Int, Double) -> Unit,
    onAddBudget: (Int, Double) -> Unit,
    onDeleteBudget: (String) -> Unit
) {
    var isMonthPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isBudgetEditorVisible by rememberSaveable { mutableStateOf(false) }
    var editingBudgetId by rememberSaveable { mutableStateOf<String?>(null) }
    var budgetEditorSessionKey by rememberSaveable { mutableStateOf(0L) }
    var pendingDeleteBudgetId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteRecurringId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingRecurringRule by remember { mutableStateOf<BudgetRecurringExpenseUi?>(null) }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    // Sync ViewModel tab state with PagerState
    LaunchedEffect(pagerState.currentPage) {
        val tab = if (pagerState.currentPage == 0) BudgetTab.Budgets else BudgetTab.Recurring
        if (uiState.selectedTab != tab) {
            onSelectTab(tab)
        }
    }

    // Sync PagerState with ViewModel tab state (for programmatic clicks)
    LaunchedEffect(uiState.selectedTab) {
        val page = if (uiState.selectedTab == BudgetTab.Budgets) 0 else 1
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
            .background(MaterialTheme.colorScheme.background)
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
            ) {
                AppHeader(title = stringResource(id = R.string.title_budget_recurring), onBackClick = onBackClick)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Row
            Box(modifier = Modifier.padding(horizontal = Dimens.ScreenPadding)) {
                BudgetTabRow(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = onSelectTab
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.Top
            ) { page ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(start = Dimens.ScreenPadding, top = 20.dp, end = Dimens.ScreenPadding, bottom = 126.dp),
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

                        item { BudgetSummaryCard(summary = uiState.summary) }
                        
                        item { SectionTitle(title = stringResource(id = R.string.title_category_budgets)) }

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
                                    }
                                )
                            }
                        }

                        item {
                            val canAdd = uiState.canAddBudget
                            BudgetActionButton(
                                title = if (uiState.isMonthLocked) stringResource(id = R.string.label_history_locked) else stringResource(id = R.string.title_add_new_budget),
                                icon = if (uiState.isMonthLocked) Icons.Filled.Lock else Icons.Filled.Add,
                                enabled = canAdd,
                                onClick = {
                                    editingBudgetId = null
                                    budgetEditorSessionKey = System.currentTimeMillis()
                                    isBudgetEditorVisible = true
                                }
                            )
                        }

                        item {
                            AdContainer(isAdsEnabled = isAdsEnabled) {
                                NativeAdCard(placement = AdPlacement.BUDGET_CALENDAR)
                            }
                        }
                    } else {
                        // TAB 2: RECURRING
                        item { SectionTitle(title = stringResource(id = R.string.title_recurring_expenses)) }

                        if (uiState.recurringExpenses.isEmpty()) {
                            item {
                                EmptySectionCard(
                                    message = uiState.emptyRecurringMessage?.asString()
                                        ?: stringResource(id = R.string.msg_no_recurring_items)
                                )
                            }
                        } else {
                            items(uiState.recurringExpenses, key = { it.id }) { expense ->
                                RecurringExpenseCard(
                                    expense = expense,
                                    onEnabledChange = { enabled ->
                                        onRecurringEnabledChange(expense.id, enabled)
                                    },
                                    onEditClick = { editingRecurringRule = expense },
                                    onDeleteClick = {
                                        pendingDeleteRecurringId = expense.id
                                    }
                                )
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
        BudgetEditorDialog(
            currencyId = currencyId,
            amountFormatPreferences = amountFormatPreferences,
            monthLabel = uiState.summary.monthLabel,
            expenseCategories = expenseCategories,
            existingBudget = editingBudget,
            sessionKey = budgetEditorSessionKey,
            onDismiss = {
                isBudgetEditorVisible = false
                editingBudgetId = null
            },
            onSave = { categoryId, limitAmount ->
                if (editingBudget != null) {
                    onUpdateBudget(editingBudget.id, categoryId, limitAmount)
                } else {
                    onAddBudget(categoryId, limitAmount)
                }
                isBudgetEditorVisible = false
                editingBudgetId = null
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
                onDeleteRecurring(pendingDeleteRecurring.id)
                pendingDeleteRecurringId = null
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
            }
        )
    }
}


@Composable
private fun budgetAccentColor(accent: BudgetAccent): Color {
    val colorScheme = MaterialTheme.colorScheme
    return when (accent) {
        BudgetAccent.Primary -> colorScheme.primary
        BudgetAccent.Warning -> colorScheme.tertiary
        BudgetAccent.Overspent -> colorScheme.error
        BudgetAccent.Disabled -> colorScheme.outline
        BudgetAccent.Daily -> colorScheme.income
        BudgetAccent.Yearly -> colorScheme.secondary
    }
}


@Composable
private fun BoxScope.BudgetGlow() {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 72.dp)
            .size(width = 260.dp, height = 180.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.26f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0f)
                    )
                )
            )
    )
}


@Composable
private fun BudgetPeriodRow(
    selectedPeriod: BudgetPeriodFilter,
    isCustomMonthLocked: Boolean,
    onPeriodSelected: (BudgetPeriodFilter) -> Unit
) {
    val periods = remember { BudgetPeriodFilter.entries }
    val selectedIndex = periods.indexOf(selectedPeriod).coerceAtLeast(0)
    
    val density = LocalDensity.current
    var containerWidthPx by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .onSizeChanged { containerWidthPx = it.width }
            .clip(RoundedCornerShape(26.dp))
            .background(standardCardGradient())
            .padding(4.dp)
    ) {
        val tabWidth = with(density) { (containerWidthPx.toDp() - 8.dp) / periods.size }
        
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "budget_indicator_offset"
        )

        // Sliding indicator (Pill)
        if (containerWidthPx > 0) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(brandGradient())
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            periods.forEach { period ->
                BudgetPeriodChip(
                    label = when (period) {
                        BudgetPeriodFilter.ThisMonth -> stringResource(id = R.string.label_this_month_caps)
                        BudgetPeriodFilter.LastMonth -> stringResource(id = R.string.label_last_month)
                        BudgetPeriodFilter.CustomMonth -> stringResource(id = R.string.label_custom_month_caps)
                    },
                    selected = period == selectedPeriod,
                    isLocked = period == BudgetPeriodFilter.CustomMonth && isCustomMonthLocked,
                    onClick = { onPeriodSelected(period) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BudgetPeriodChip(
    label: String,
    selected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "budget_text_color"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = animatedColor,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 15.sp,
                    letterSpacing = 0.sp,
                    fontSize = 11.sp
                )
            )

            if (isLocked) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = stringResource(id = R.string.content_desc_locked_formatted, label),
                    tint = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.featureGateLock
                    },
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun BudgetSummaryCard(summary: BudgetSummaryUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 26.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.34f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(standardCardGradient())
            .border(

                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.label_monthly_budget),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp
                )
            )

            Text(
                text = summary.monthLabel,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = summary.totalBudgetLabel,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    fontSize = 34.sp
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(id = R.string.label_month),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BudgetMetricCard(
                modifier = Modifier.weight(1f),
                title = stringResource(id = R.string.title_spent),
                value = summary.spentLabel,
                valueColor = MaterialTheme.colorScheme.expense
            )

            BudgetMetricCard(
                modifier = Modifier.weight(1f),
                title = if (summary.remainingAmount >= 0.0) stringResource(id = R.string.label_remaining) else stringResource(id = R.string.label_over),
                value = summary.remainingLabel.asString(),
                valueColor = if (summary.remainingAmount >= 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.usageLabel.asString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 0.9.sp
                    )
                )

                if (summary.dailyAllowanceLabel != null) {
                    Text(
                        text = summary.dailyAllowanceLabel.asString(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Text(
                    text = summary.limitLabel.asString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 0.7.sp
                    )
                )
            }

            BudgetProgressBar(
                progress = summary.usageFraction,
                accent = brandGradient()
            )
        }
    }
}

@Composable
private fun BudgetMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    valueColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                letterSpacing = 0.9.sp
            )
        )

        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                fontSize = 18.sp
            )
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
        )
    )
}

@Composable
private fun EmptySectionCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(standardCardGradient())
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
private fun BudgetEditorDialog(
    currencyId: Int,
    amountFormatPreferences: AmountFormatPreferences,
    monthLabel: String,
    expenseCategories: List<CategoryType>,
    existingBudget: BudgetCategoryBudgetUi?,
    sessionKey: Any?,
    onDismiss: () -> Unit,
    onSave: (Int, Double) -> Unit
) {
    var isCategoryPickerVisible by rememberSaveable(existingBudget?.id, monthLabel, sessionKey) { mutableStateOf(false) }
    val categoryPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategoryId by rememberSaveable(existingBudget?.id, monthLabel, sessionKey) {
        mutableStateOf(existingBudget?.categoryId ?: expenseCategories.firstOrNull()?.id)
    }
    var amountInput by rememberSaveable(existingBudget?.id, monthLabel, sessionKey) {
        mutableStateOf(
            existingBudget?.limitAmount
                ?.takeIf { it > 0.0 }
                ?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                .orEmpty()
        )
    }
    val selectedCategory = expenseCategories.firstOrNull { it.id == selectedCategoryId }
    val limitAmount = amountInput.toDoubleOrNull()
    val isSaveEnabled = selectedCategory != null && limitAmount != null && limitAmount > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (existingBudget == null) stringResource(id = R.string.title_add_budget) else stringResource(id = R.string.title_edit_budget),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                )
                Text(
                    text = monthLabel,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = stringResource(id = R.string.label_budgets_are_no_longer_preloade),
                    style = MaterialTheme.typography.bodyMedium
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(id = R.string.label_category_1),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    )

                    SelectionDialogField(
                        label = "",
                        value = selectedCategory?.name ?: stringResource(id = R.string.msg_no_expense_categories_available),
                        actionLabel = stringResource(id = R.string.label_change_caps),
                        enabled = expenseCategories.isNotEmpty(),
                        onClick = { isCategoryPickerVisible = true },
                        dropdownContent = {}
                    )
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { updatedValue ->
                        amountInput = updatedValue.filter { character ->
                            character.isDigit() || character == '.'
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(id = R.string.label_monthly_limit)) },
                    placeholder = {
                        Text(formatCurrencyValue(5000.0, currencyId, amountFormatPreferences))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedCategoryId != null && limitAmount != null) {
                        onSave(selectedCategoryId!!, limitAmount)
                    }
                },
                enabled = isSaveEnabled
            ) {
                Text(if (existingBudget == null) stringResource(id = R.string.label_save_1) else stringResource(id = R.string.label_update_1))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.label_cancel_1))
            }
        }
    )

    if (isCategoryPickerVisible) {
        BudgetCategoryPickerSheet(
            categories = expenseCategories,
            selectedCategoryId = selectedCategoryId,
            sheetState = categoryPickerSheetState,
            onDismiss = { isCategoryPickerVisible = false },
            onCategorySelected = { categoryId ->
                selectedCategoryId = categoryId
                isCategoryPickerVisible = false
            }
        )
    }
}

@Composable
private fun DeleteBudgetDialog(
    budgetName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = stringResource(id = R.string.label_delete_budget),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                )
            )
        },
        text = {
            Text(stringResource(id = R.string.label_remove_the_budget_for_val_this, budgetName))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(id = R.string.label_delete_1))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.label_cancel_1))
            }
        }
    )
}

@Composable
private fun SelectionDialogField(
    label: String,
    value: String,
    actionLabel: String,
    enabled: Boolean,
    onClick: () -> Unit,
    dropdownContent: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        }

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(standardCardGradient())
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(enabled = enabled, onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = actionLabel,
                    color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha =  0.65f),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        letterSpacing = 0.7.sp
                    )
                )
            }

            dropdownContent()
        }
    }
}

@Composable
private fun BudgetCategoryPickerSheet(
    categories: List<CategoryType>,
    selectedCategoryId: Int?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onCategorySelected: (Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.62f)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.label_select_category),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }

            item {
                Text(
                    text = stringResource(id = R.string.label_choose_from_all_expense_catego),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            items(
                items = categories,
                key = { category -> category.id }
            ) { category ->
                BudgetCategoryPickerRow(
                    category = category,
                    isSelected = category.id == selectedCategoryId,
                    onClick = { onCategorySelected(category.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun BudgetCategoryPickerRow(
    category: CategoryType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(id = R.string.label_expense_category),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isSelected) {
            Text(
                text = stringResource(id = R.string.label_selected),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun DeleteRecurringDialog(
    recurringName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = stringResource(id = R.string.label_delete_recurring),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                )
            )
        },
        text = {
            Text(stringResource(id = R.string.label_remove_val_from_recurring_trac, recurringName))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(id = R.string.label_delete_1))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.label_cancel_1))
            }
        }
    )
}



@Composable
private fun CategoryBudgetCard(
    budget: BudgetCategoryBudgetUi,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val containerBrush = when {
        budget.spentAmount > budget.limitAmount -> Brush.verticalGradient(
            colors = listOf(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
        )

        budget.progressFraction >= 0.85f -> Brush.verticalGradient(
            colors = listOf(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f), MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f))
        )

        else -> standardCardGradient()
    }
    val iconContainer = when {
        budget.spentAmount > budget.limitAmount -> MaterialTheme.colorScheme.errorContainer
        budget.progressFraction >= 0.85f -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(containerBrush)
            .border(
                width = 1.dp,
                color = budgetAccentColor(budget.accent).copy(alpha = 0.24f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = budget.icon,
                    contentDescription = budget.title,
                    tint = budgetAccentColor(budget.accent),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = budget.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = budget.statusValueLabel.asString(),
                    color = budgetAccentColor(budget.accent),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = budget.summaryLabel,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = budget.totalCaption.asString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 0.7.sp
                    )
                )
            }
        }

        if (budget.remainingEdits != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (budget.remainingEdits == 0) stringResource(id = R.string.label_history_locked) else stringResource(id = R.string.label_edits_left_formatted, budget.remainingEdits),
                    color = if (budget.remainingEdits == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }

        BudgetProgressBar(
            progress = budget.progressFraction,
            accent = Brush.horizontalGradient(
                colors = listOf(budgetAccentColor(budget.accent), budgetAccentColor(budget.accent).copy(alpha = 0.8f))
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = budget.statusCaption.asString(),
                color = budgetAccentColor(budget.accent),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                BudgetCardAction(
                    icon = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.label_edit),
                    accent = MaterialTheme.colorScheme.primary,
                    isLocked = !budget.canEdit,
                    onClick = { if (budget.canEdit) onEditClick() }
                )

                Spacer(modifier = Modifier.width(8.dp))

                BudgetCardAction(
                    icon = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.label_delete),
                    accent = MaterialTheme.colorScheme.error,
                    isLocked = !budget.canEdit,
                    onClick = { if (budget.canEdit) onDeleteClick() }
                )
            }
        }
    }
}

@Composable
private fun BudgetCardAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String? = null,
    contentDescription: String? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    isLocked: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val finalAccent = if (isLocked) MaterialTheme.colorScheme.outline else accent
    val backgroundAlpha = if (isLocked || !enabled) 0.16f else 0.12f
    val borderAlpha = if (isLocked || !enabled) 0.35f else 0.22f

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(finalAccent.copy(alpha = backgroundAlpha))
            .border(
                width = 1.dp,
                color = finalAccent.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = if (label != null) 12.dp else 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription ?: label,
                    tint = if (isLocked || !enabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) else finalAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (label != null) {
                if (icon != null) Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = if (isLocked || !enabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) else finalAccent,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                )
            }

            if (isLocked) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = stringResource(R.string.content_desc_locked_formatted, contentDescription ?: label ?: ""),
                    tint = finalAccent.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun BudgetProgressBar(
    progress: Float,
    accent: Brush
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "budget_progress_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(12.dp)
                .clip(CircleShape)
                .background(accent)
        )
    }
}

@Composable
private fun BudgetActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val backgroundBrush = if (enabled) {
        brandGradient()
    } else {
        Brush.horizontalGradient(
            colors = listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outlineVariant)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundBrush)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
        )
    }
}

@Composable
private fun RecurringExpenseCard(
    expense: BudgetRecurringExpenseUi,
    onEnabledChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isUrgent = expense.isEnabled && (expense.accent == BudgetAccent.Overspent || expense.accent == BudgetAccent.Warning)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = budgetAccentColor(expense.accent).copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = expense.icon,
                    contentDescription = expense.title,
                    tint = budgetAccentColor(expense.accent),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = expense.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall
                            .copy(
                           fontWeight = FontWeight.Medium
                        )
                    )
                    
                    if (isUrgent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = stringResource(id = R.string.content_desc_upcoming),
                            tint = budgetAccentColor(expense.accent),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = expense.amountLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                )
            }

            Switch(
                checked = expense.isEnabled,
                onCheckedChange = onEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RecurringMetaChip(
                label = expense.categoryLabel,
                accent = budgetAccentColor(expense.accent)
            )
            RecurringMetaChip(
                label = expense.frequencyLabel,
                accent = budgetAccentColor(expense.accent)
            )
            RecurringMetaChip(
                label = stringResource(id = R.string.label_installments_formatted, expense.currentInstallment, expense.totalInstallments),
                accent = budgetAccentColor(expense.accent)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = expense.dueLabel.asString(),
                        color = budgetAccentColor(expense.accent),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                    )
                    
                    if (expense.dueLabel.asString().contains("TODAY")) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = expense.sourceDateLabel.asString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GatedAction(
                    feature = Feature.RECURRING_RULE_EDIT,
                    displayName = stringResource(id = R.string.label_edit_recurring_rule),
                    onAction = onEditClick
                ) { status, onClick ->
                    BudgetCardAction(
                        icon = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.label_edit),
                        accent = MaterialTheme.colorScheme.primary,
                        isLocked = status !is AccessStatus.Granted,
                        onClick = onClick
                    )
                }

                BudgetCardAction(
                    icon = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.label_delete),
                    accent = MaterialTheme.colorScheme.error,
                    onClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
private fun RecurringMetaChip(
    label: String,
    accent: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.14f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.26f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = accent,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                letterSpacing = 0.7.sp
            )
        )
    }
}

@Preview(
    name = "Budget Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun BudgetAndRecurringScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        BudgetAndRecurringContent(
            uiState = com.mknlabs.expensetracker.ui.viewmodels.BudgetAndRecurringScreenUiState(),
            isAdsEnabled = true,
            currencyId = DEFAULT_CURRENCY_ID,
            amountFormatPreferences = defaultAmountFormatPreferences,
            availableCategories = emptyList(),
            transactions = emptyList(),
            onDeleteRecurring = {},
            onRecurringEnabledChange = { _, _ -> },
            onUpdateRecurringRule = { _, _, _ -> },
            onBackClick = {},
            onSelectTab = {},
            onSelectPeriod = {},
            onSelectCustomMonth = {},
            onUpdateBudget = { _, _, _ -> },
            onAddBudget = { _, _ -> },
            onDeleteBudget = {}
        )
    }
}

@Composable
private fun BudgetTabRow(
    selectedTab: BudgetTab,
    onTabSelected: (BudgetTab) -> Unit
) {
    val tabs = remember { BudgetTab.entries }
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
    
    val density = LocalDensity.current
    var containerWidthPx by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .onSizeChanged { containerWidthPx = it.width }
            .clip(RoundedCornerShape(26.dp))
            .background(standardCardGradient())
            .padding(4.dp)
    ) {
        val tabWidth = with(density) { (containerWidthPx.toDp() - 8.dp) / tabs.size }
        
        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "tab_indicator_offset"
        )

        // Sliding indicator (Pill)
        if (containerWidthPx > 0) {
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(brandGradient())
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                BudgetTabChip(
                    label = when (tab) {
                        BudgetTab.Budgets -> stringResource(id = R.string.label_tab_budgets)
                        BudgetTab.Recurring -> stringResource(id = R.string.label_tab_recurring)
                    },
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BudgetTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tab_text_color"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            color = animatedColor,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.1.sp,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
private fun RecurringRuleEditorModal(
    rule: BudgetRecurringExpenseUi,
    onDismiss: () -> Unit,
    onSave: (RecurringFrequency, Int) -> Unit
) {
    var selectedFrequency by remember { mutableStateOf(rule.frequency) }
    var installmentsInput by remember { mutableStateOf(rule.totalInstallments.toString()) }
    var isFrequencyDropdownExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.65f),
        dragHandle = {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Text(
                text = stringResource(id = R.string.title_edit_recurring),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.label_frequency_capitalized),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelLarge
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedFrequency.label,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth(),
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { isFrequencyDropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = isFrequencyDropdownExpanded,
                        onDismissRequest = { isFrequencyDropdownExpanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        RecurringFrequency.entries.forEach { frequency ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = frequency.label,
                                        color = if (frequency == selectedFrequency) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedFrequency = frequency
                                    isFrequencyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.label_total_installments),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelLarge
                )
                
                OutlinedTextField(
                    value = installmentsInput,
                    onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() }) installmentsInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(id = R.string.label_cancel_caps), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                TextButton(
                    onClick = {
                        val count = installmentsInput.toIntOrNull() ?: rule.totalInstallments
                        if (count > 0) {
                            onSave(selectedFrequency, count)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                ) {
                    Text(stringResource(id = R.string.label_save_changes_caps), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
