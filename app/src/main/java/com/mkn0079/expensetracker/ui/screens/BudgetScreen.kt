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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
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
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.RecurringTransactionRule
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurpleGlow
import com.mkn0079.expensetracker.ui.theme.PurplePrimary
import com.mkn0079.expensetracker.ui.viewmodels.BudgetCategoryBudgetUi
import com.mkn0079.expensetracker.ui.viewmodels.BudgetInsightUi
import com.mkn0079.expensetracker.ui.viewmodels.BudgetPeriodFilter
import com.mkn0079.expensetracker.ui.viewmodels.BudgetRecurringExpenseUi
import com.mkn0079.expensetracker.ui.viewmodels.BudgetSummaryUi
import com.mkn0079.expensetracker.ui.viewmodels.BudgetViewModel
import com.mkn0079.expensetracker.models.RecurringFrequency
import com.mkn0079.expensetracker.utils.datePickerSelectionToLocalDateTimestamp
import com.mkn0079.expensetracker.utils.formatCurrencyValue
import com.mkn0079.expensetracker.utils.localDateTimestampToDatePickerSelection

@Composable
fun BudgetScreen(
    currencyId: Int = DEFAULT_CURRENCY_ID,
    transactions: List<Transaction> = transactionList,
    availableCategories: List<CategoryType> = categoryMap.values.toList(),
    recurringRules: List<RecurringTransactionRule> = emptyList(),
    onDeleteRecurring: (String) -> Unit = {},
    onRecurringEnabledChange: (String, Boolean) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    val budgetViewModel: BudgetViewModel = viewModel()
    var isMonthPickerVisible by rememberSaveable { mutableStateOf(false) }
    var isBudgetEditorVisible by rememberSaveable { mutableStateOf(false) }
    var editingBudgetId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteBudgetId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteRecurringId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(transactions, availableCategories, currencyId, recurringRules) {
        budgetViewModel.updateInputs(
            transactions = transactions,
            categories = availableCategories,
            currencyId = currencyId,
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
                        BackgroundDark,
                        Color(0xFF050507),
                        BackgroundDark
                    )
                )
            )
    ) {
        BudgetGlow()

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
                BudgetTopBar(onBackClick = onBackClick)
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
                    BudgetPeriodRow(
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = { period ->
                            if (period == BudgetPeriodFilter.CustomMonth) {
                                isMonthPickerVisible = true
                            } else {
                                budgetViewModel.selectPeriod(period)
                            }
                        }
                    )
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
                        color = Color.White.copy(alpha = 0.06f)
                    )
                }

                item { SectionTitle(title = "Recurring Expenses") }

                if (uiState.recurringDueItems.isNotEmpty()) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(uiState.recurringDueItems, key = { it.id }) { item ->
                                RecurringDueChip(item = item)
                            }
                        }
                    }
                }

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
                            onDeleteClick = {
                                pendingDeleteRecurringId = expense.id
                            }
                        )
                    }
                }

                item { InsightCard(insight = uiState.insight) }
            }
        }
    }

    if (isMonthPickerVisible) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = localDateTimestampToDatePickerSelection(uiState.customMonthStart)
        )
        DatePickerDialog(
            onDismissRequest = { isMonthPickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis
                            ?.let(::datePickerSelectionToLocalDateTimestamp)
                            ?.let(budgetViewModel::selectCustomMonth)
                        isMonthPickerVisible = false
                    }
                ) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = { isMonthPickerVisible = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Select custom month",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                },
                headline = {
                    Text(
                        text = "Choose any date in the month you want to review.",
                        modifier = Modifier.padding(start = 24.dp, top = 8.dp, end = 24.dp)
                    )
                },
                showModeToggle = false
            )
        }
    }

    if (isBudgetEditorVisible) {
        BudgetEditorDialog(
            currencyId = currencyId,
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
                        PurplePrimary.copy(alpha = 0.26f),
                        PurpleGlow.copy(alpha = 0.12f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun BudgetTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = PurpleAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Budget & Recurring",
            color = PurplePrimary,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = 20.sp
            )
        )
    }
}

@Composable
private fun BudgetPeriodRow(
    selectedPeriod: BudgetPeriodFilter,
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
            .background(Color(0xFF17171A))
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
                            colors = listOf(PurplePrimary, Color(0xFFB89AF7))
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF24114C) else Color(0xFFD9D0E8),
        label = "budget_text_color"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
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
                ambientColor = Color.Black.copy(alpha = 0.34f),
                spotColor = PurpleGlow.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF211E24),
                        Color(0xFF171518)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
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
                color = Color(0xFF9C96AA),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp
                )
            )

            Text(
                text = summary.monthLabel,
                color = Color(0xFFD5CCEA),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = summary.totalBudgetLabel,
                color = Color(0xFFF6F2FC),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    fontSize = 34.sp
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "/ month",
                color = Color(0xFFD3CED9),
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
                valueColor = Color(0xFFFFB8B2)
            )

            BudgetMetricCard(
                modifier = Modifier.weight(1f),
                title = if (summary.remainingAmount >= 0.0) "REMAINING" else "OVER",
                value = summary.remainingLabel,
                valueColor = if (summary.remainingAmount >= 0.0) Color(0xFFD6C8FF) else Color(0xFFFFB3B3)
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
                    color = Color(0xFFE1D6FF),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 0.9.sp
                    )
                )

                Text(
                    text = summary.limitLabel,
                    color = Color(0xFFA8A1B4),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 0.7.sp
                    )
                )
            }

            BudgetProgressBar(
                progress = summary.usageFraction,
                accent = Brush.horizontalGradient(
                    colors = listOf(PurplePrimary, Color(0xFFBAA3FF))
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
            .background(Color(0xFF101013))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFF9B95A8),
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
        color = Color(0xFFF2EDF8),
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
            .background(Color(0xFF1B1A1E))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Text(
            text = message,
            color = Color(0xFFC8C0D7),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
private fun BudgetEditorDialog(
    currencyId: Int,
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
        containerColor = Color(0xFF18171C),
        titleContentColor = Color(0xFFF4EFFA),
        textContentColor = Color(0xFFD0C8DD),
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
                    color = PurplePrimary,
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
                        color = Color(0xFFEDE7F9),
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
                    placeholder = { Text(formatCurrencyValue(5000.0, currencyId)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                        focusedLabelColor = PurplePrimary,
                        unfocusedLabelColor = Color(0xFFAAA2B8),
                        cursorColor = PurplePrimary
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
        containerColor = Color(0xFF18171C),
        titleContentColor = Color(0xFFF4EFFA),
        textContentColor = Color(0xFFD0C8DD),
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
                color = Color(0xFFEDE7F9),
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
                    .background(Color(0xFF111115))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(enabled = enabled, onClick = onClick)
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    color = if (enabled) Color(0xFFF4EFFA) else Color(0xFFA9A1B6),
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = actionLabel,
                    color = if (enabled) PurplePrimary else Color(0xFF7D768B),
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
        containerColor = Color(0xFF141416),
        scrimColor = Color.Black.copy(alpha = 0.62f)
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
                    color = Color(0xFFF0EBF7),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                )
            }

            item {
                Text(
                    text = "Choose from all expense categories, including preloaded and custom ones.",
                    color = Color(0xFF968EA8),
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
                    PurplePrimary.copy(alpha = 0.18f)
                } else {
                    Color(0xFF1A1A1E)
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
                        PurpleAccent.copy(alpha = 0.18f)
                    } else {
                        Color(0xFF232326)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = if (isSelected) PurpleAccent else Color(0xFFD7D1E4),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name,
                color = Color(0xFFF0EBF7),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Expense category",
                color = Color(0xFF9B93AE),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isSelected) {
            Text(
                text = "Selected",
                color = PurpleAccent,
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
        containerColor = Color(0xFF18171C),
        titleContentColor = Color(0xFFF4EFFA),
        textContentColor = Color(0xFFD0C8DD),
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
            colors = listOf(Color(0xFF2C2527), Color(0xFF1F1A1B))
        )

        budget.progressFraction >= 0.85f -> Brush.verticalGradient(
            colors = listOf(Color(0xFF402108), Color(0xFF251409))
        )

        else -> Brush.verticalGradient(
            colors = listOf(Color(0xFF252326), Color(0xFF1C1A1D))
        )
    }
    val iconContainer = when {
        budget.spentAmount > budget.limitAmount -> Color(0xFF4A373A)
        budget.progressFraction >= 0.85f -> Color(0xFF5A3922)
        else -> Color(0xFF343138)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(containerBrush)
            .border(
                width = 1.dp,
                color = budget.accent.copy(alpha = 0.24f),
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
                    tint = budget.accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = budget.title,
                    color = Color(0xFFF4EFFA),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = budget.statusValueLabel,
                    color = budget.accent,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = budget.summaryLabel,
                    color = Color(0xFFF4EFFA),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = budget.totalCaption,
                    color = Color(0xFFB1ABBB),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        letterSpacing = 0.7.sp
                    )
                )
            }
        }

        Text(
            text = budget.statusCaption,
            color = budget.accent,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        )

        BudgetProgressBar(
            progress = budget.progressFraction,
            accent = Brush.horizontalGradient(
                colors = listOf(budget.accent, budget.accent.copy(alpha = 0.8f))
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
                accent = Color(0xFFFFB3B3),
                onClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun BudgetCardAction(
    label: String,
    accent: Color = PurplePrimary,
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
        Text(
            text = label,
            color = accent,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                letterSpacing = 0.8.sp
            )
        )
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
            .background(Color(0xFF08080A))
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
                    colors = listOf(PurplePrimary, Color(0xFF8C64FF))
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
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
        )
    }
}

@Composable
private fun RecurringDueChip(item: BudgetRecurringExpenseUi) {
    val containerColor = when {
        item.dueLabel.contains("TODAY") -> Color(0xFF230C0D)
        item.dueLabel.contains("TOMORROW") || item.dueLabel.contains("DUE IN") -> Color(0xFF2A1B08)
        else -> Color(0xFF161025)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .border(
                width = 1.dp,
                color = item.accent.copy(alpha = 0.34f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.24f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.accent,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = item.dueLabel,
                color = item.accent,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${item.title} (${item.dueAmountLabel})",
                color = Color(0xFFF6F1FB),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun RecurringExpenseCard(
    expense: BudgetRecurringExpenseUi,
    onEnabledChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1F1D20))
            .border(
                width = 1.dp,
                color = expense.accent.copy(alpha = 0.18f),
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
                    .background(Color(0xFF0F0F12)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = expense.icon,
                    contentDescription = expense.title,
                    tint = expense.accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    color = Color(0xFFF5F0FB),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = expense.amountLabel,
                    color = Color(0xFFBDB6C9),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                )
            }

            Switch(
                checked = expense.isEnabled,
                onCheckedChange = onEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF24114C),
                    checkedTrackColor = PurpleAccent,
                    uncheckedThumbColor = Color(0xFFDDD6EC),
                    uncheckedTrackColor = Color(0xFF3B3548),
                    uncheckedBorderColor = Color(0xFF3B3548)
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RecurringMetaChip(
                label = expense.categoryLabel,
                accent = expense.accent
            )
            RecurringMetaChip(
                label = expense.frequencyLabel,
                accent = expense.accent
            )
            RecurringMetaChip(
                label = "${expense.repeatCount}X",
                accent = expense.accent
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = expense.dueLabel,
                    color = expense.accent,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = expense.sourceDateLabel,
                    color = Color(0xFFAAA2B8),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = expense.dueAmountLabel,
                color = Color(0xFFF5F0FB),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            BudgetCardAction(
                label = "DELETE",
                accent = Color(0xFFFFB3B3),
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
private fun InsightCard(insight: BudgetInsightUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF201B26), Color(0xFF17141B))
                )
            )
            .border(
                width = 1.dp,
                color = insight.accent.copy(alpha = 0.24f),
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
                    .background(insight.accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = insight.title,
                    tint = insight.accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = insight.title,
                    color = Color(0xFFF4EFFA),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = insight.supportingLabel,
                    color = insight.accent,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                )
            }
        }

        Text(
            text = insight.body,
            color = Color(0xFFD3CCDF),
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
    backgroundColor = 0xFF0A0A0A,
    device = "spec:width=412dp,height=915dp,dpi=420"
)
@Composable
private fun BudgetScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        BudgetScreen()
    }
}
