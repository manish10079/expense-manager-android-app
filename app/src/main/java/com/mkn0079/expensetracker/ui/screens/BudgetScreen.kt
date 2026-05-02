@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.mkn0079.expensetracker.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.categoryMap
import com.mkn0079.expensetracker.data.constants.transactionList
import com.mkn0079.expensetracker.models.AmountFormatPreferences
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.RecurringTransactionRule
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.components.FeatureLockedOverlay
import com.mkn0079.expensetracker.ui.components.GatedAction
import com.mkn0079.expensetracker.ui.components.WheelDateTimePickerModal
import com.mkn0079.expensetracker.ui.components.WheelPickerMode
import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.expense
import com.mkn0079.expensetracker.ui.theme.featureGateLock
import com.mkn0079.expensetracker.ui.theme.income
import com.mkn0079.expensetracker.ui.viewmodels.BudgetCategoryBudgetUi
import com.mkn0079.expensetracker.ui.viewmodels.BudgetAccent
import com.mkn0079.expensetracker.ui.viewmodels.BudgetInsightUi
import com.mkn0079.expensetracker.ui.viewmodels.BudgetPeriodFilter
import com.mkn0079.expensetracker.ui.viewmodels.BudgetRecurringExpenseUi
import com.mkn0079.expensetracker.ui.viewmodels.BudgetSummaryUi
import com.mkn0079.expensetracker.ui.viewmodels.BudgetViewModel
import com.mkn0079.expensetracker.models.RecurringFrequency
import com.mkn0079.expensetracker.utils.defaultAmountFormatPreferences
import com.mkn0079.expensetracker.utils.datePickerSelectionToLocalDateTimestamp
import com.mkn0079.expensetracker.utils.formatCurrencyValue

@Composable
fun BudgetScreen(
    currencyId: Int = DEFAULT_CURRENCY_ID,
    amountFormatPreferences: AmountFormatPreferences = defaultAmountFormatPreferences,
    transactions: List<Transaction> = transactionList,
    availableCategories: List<CategoryType> = categoryMap.values.toList(),
    recurringRules: List<RecurringTransactionRule> = emptyList(),
    onDeleteRecurring: (String) -> Unit = {},
    onRecurringEnabledChange: (String, Boolean) -> Unit = { _, _ -> },
    onUpdateRecurringRule: (String, RecurringFrequency, Int) -> Unit = { _, _, _ -> },
    onBackClick: () -> Unit = {}
) {
    val budgetViewModel: BudgetViewModel = viewModel()
    var isMonthPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isBudgetEditorVisible by rememberSaveable { mutableStateOf(false) }
    var editingBudgetId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteBudgetId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteRecurringId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingRecurringRule by remember { mutableStateOf<BudgetRecurringExpenseUi?>(null) }

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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, top = 22.dp, end = 14.dp)
            ) {
                AppHeader(title = "Budget & Recurring", onBackClick = onBackClick)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(start = 14.dp, top = 18.dp, end = 14.dp, bottom = 126.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
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
                                    budgetViewModel.selectPeriod(period)
                                }
                            }
                        )
                    }
                }

                item { BudgetSummaryCard(summary = uiState.summary) }
                item { SectionTitle(title = "Category Budgets") }

                if (uiState.categoryBudgets.isEmpty()) {
                    item {
                        EmptySectionCard(
                            message = uiState.emptyCategoryMessage
                                ?: "No category budget data is available for this month yet."
                        )
                    }
                } else {
                    items(uiState.categoryBudgets, key = { it.id }) { budget ->
                        CategoryBudgetCard(
                            budget = budget,
                            onEditClick = {
                                editingBudgetId = budget.id
                                isBudgetEditorVisible = true
                            },
                            onDeleteClick = {
                                pendingDeleteBudgetId = budget.id
                            }
                        )
                    }
                }

                item {
                    BudgetActionButton(
                        title = "ADD NEW BUDGET",
                        icon = Icons.Filled.Add,
                        onClick = {
                            editingBudgetId = null
                            isBudgetEditorVisible = true
                        }
                    )
                }

                item {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha =  0.65f)
                    )
                }

                item { SectionTitle(title = "Recurring Expenses") }

                if (uiState.recurringExpenses.isEmpty()) {
                    item {
                        EmptySectionCard(
                            message = uiState.emptyRecurringMessage
                                ?: "No recurring items added yet."
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

                item {
                    GatedAction(
                        feature = Feature.BUDGET_INSIGHTS,
                        displayName = "Budget Insights",
                        onAction = {}
                    ) { status, onClick ->
                        val isLocked = status !is AccessStatus.Granted
                        Box {
                            InsightCard(
                                insight = uiState.insight,
                                modifier = if (isLocked) Modifier.blur(16.dp) else Modifier
                            )
                            if (isLocked) {
                                FeatureLockedOverlay(
                                    displayText = "Unlock Insights",
                                    onClick = onClick
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
            mode = WheelPickerMode.SINGLE_DATE,
            initialStartMillis = uiState.customMonthStart,
            onDismissRequest = { isMonthPickerVisible = false },
            onConfirm = { pickedDateMillis, _ ->
                val adjustedTimestamp = datePickerSelectionToLocalDateTimestamp(
                    selectedDateMillis = pickedDateMillis,
                    referenceTimestamp = uiState.customMonthStart
                )
                budgetViewModel.selectCustomMonth(adjustedTimestamp)
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
            onDismiss = {
                isBudgetEditorVisible = false
                editingBudgetId = null
            },
            onSave = { categoryId, limitAmount ->
                if (editingBudget != null) {
                    budgetViewModel.updateBudget(
                        budgetId = editingBudget.id,
                        categoryId = categoryId,
                        limitAmount = limitAmount
                    )
                } else {
                    budgetViewModel.addBudget(
                        categoryId = categoryId,
                        limitAmount = limitAmount
                    )
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
                budgetViewModel.deleteBudget(pendingDeleteBudget.id)
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
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
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
                        BudgetPeriodFilter.ThisMonth -> "THIS MONTH"
                        BudgetPeriodFilter.LastMonth -> "LAST MONTH"
                        BudgetPeriodFilter.CustomMonth -> "CUSTOM MONTH"
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
                    contentDescription = "$label locked",
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
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
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
                text = "MONTHLY BUDGET",
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
                text = "/ month",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BudgetMetricCard(
                modifier = Modifier.weight(1f),
                title = "SPENT",
                value = summary.spentLabel,
                valueColor = MaterialTheme.colorScheme.expense
            )

            BudgetMetricCard(
                modifier = Modifier.weight(1f),
                title = if (summary.remainingAmount >= 0.0) "REMAINING" else "OVER",
                value = summary.remainingLabel,
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
                    text = summary.usageLabel,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 0.9.sp
                    )
                )

                Text(
                    text = summary.limitLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 0.7.sp
                    )
                )
            }

            BudgetProgressBar(
                progress = summary.usageFraction,
                accent = Brush.horizontalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
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
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
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
    onDismiss: () -> Unit,
    onSave: (Int, Double) -> Unit
) {
    var isCategoryPickerVisible by rememberSaveable(existingBudget?.id, monthLabel) { mutableStateOf(false) }
    val categoryPickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategoryId by rememberSaveable(existingBudget?.id, monthLabel) {
        mutableStateOf(existingBudget?.categoryId ?: expenseCategories.firstOrNull()?.id)
    }
    var amountInput by rememberSaveable(existingBudget?.id, monthLabel) {
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
                    text = if (existingBudget == null) "Add Budget" else "Edit Budget",
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
                    text = "Budgets are no longer preloaded. Add the category cap you want to track for this month.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Category",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    )

                    SelectionDialogField(
                        label = "",
                        value = selectedCategory?.name ?: "No expense categories available",
                        actionLabel = "CHANGE",
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
                    label = { Text("Monthly limit") },
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
                Text(if (existingBudget == null) "Save" else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
                text = "Delete Budget",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                )
            )
        },
        text = {
            Text("Remove the budget for $budgetName? This only deletes the budget cap, not your transactions.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                    text = "Select Category",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }

            item {
                Text(
                    text = "Choose from all expense categories, including preloaded and custom ones.",
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
                text = "Expense category",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isSelected) {
            Text(
                text = "Selected",
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
                text = "Delete Recurring",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                )
            )
        },
        text = {
            Text("Remove $recurringName from recurring tracking?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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

        else -> Brush.verticalGradient(
            colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
        )
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
                    text = budget.statusValueLabel,
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
                    text = budget.totalCaption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 0.7.sp
                    )
                )
            }
        }

        Text(
            text = budget.statusCaption,
            color = budgetAccentColor(budget.accent),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        )

        BudgetProgressBar(
            progress = budget.progressFraction,
            accent = Brush.horizontalGradient(
                colors = listOf(budgetAccentColor(budget.accent), budgetAccentColor(budget.accent).copy(alpha = 0.8f))
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            BudgetCardAction(
                label = "EDIT",
                onClick = onEditClick
            )

            Spacer(modifier = Modifier.width(8.dp))

            BudgetCardAction(
                label = "DELETE",
                accent = MaterialTheme.colorScheme.error,
                onClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun BudgetCardAction(
    label: String,
    accent: Color = MaterialTheme.colorScheme.primary,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.22f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                color = accent,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            )

            if (isLocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "$label locked",
                    tint = MaterialTheme.colorScheme.featureGateLock,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )
            )
            .clickable(onClick = onClick)
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
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                        )
                    )
                    
                    if (isUrgent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Upcoming",
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
                label = "${expense.currentInstallment} OF ${expense.totalInstallments}",
                accent = budgetAccentColor(expense.accent)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = expense.dueLabel,
                        color = budgetAccentColor(expense.accent),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                    )
                    
                    if (expense.dueLabel.contains("TODAY")) {
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
                    text = expense.sourceDateLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = expense.dueAmountLabel,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GatedAction(
                feature = Feature.RECURRING_RULE_EDIT,
                displayName = "Edit Recurring Rule",
                onAction = onEditClick
            ) { status, onClick ->
                val isLocked = status !is AccessStatus.Granted
                BudgetCardAction(
                    label = "EDIT",
                    accent = MaterialTheme.colorScheme.primary,
                    isLocked = isLocked,
                    onClick = if (isLocked) onClick else onEditClick
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            BudgetCardAction(
                label = "DELETE",
                accent = MaterialTheme.colorScheme.error,
                onClick = onDeleteClick
            )
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

@Composable
private fun InsightCard(
    insight: BudgetInsightUi,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)
                )
            )
            .border(
                width = 1.dp,
                color = budgetAccentColor(insight.accent).copy(alpha = 0.24f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(budgetAccentColor(insight.accent).copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = insight.title,
                    tint = budgetAccentColor(insight.accent),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = insight.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = insight.supportingLabel,
                    color = budgetAccentColor(insight.accent),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                )
            }
        }

        Text(
            text = insight.body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 20.sp
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
private fun BudgetScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        BudgetScreen()
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
                text = "Edit Commitment",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Frequency",
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
                    text = "Total Installments",
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
                    Text("CANCEL", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("SAVE CHANGES", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
