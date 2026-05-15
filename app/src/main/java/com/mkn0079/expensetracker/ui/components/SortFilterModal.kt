package com.mkn0079.expensetracker.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.data.constants.DEFAULT_SORT_BY
import com.mkn0079.expensetracker.data.constants.DEFAULT_SORT_ORDER
import com.mkn0079.expensetracker.data.constants.categoryMap
import com.mkn0079.expensetracker.data.constants.paymentTypeMap
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.SortType
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.Dimens
import com.mkn0079.expensetracker.ui.theme.brandGradient
import com.mkn0079.expensetracker.ui.theme.surfaceGradient
import com.mkn0079.expensetracker.ui.theme.standardCardGradient
import com.mkn0079.expensetracker.ui.theme.subtlePrimaryGradient
import com.mkn0079.expensetracker.utils.getDefaultOrder
import com.mkn0079.expensetracker.utils.getOrderOptions
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.monetization.AccessStatus
import com.mkn0079.expensetracker.ui.theme.featureGateLock

const val FILTER_DATE_LAST_7_DAYS = "Last 7 Days"
const val FILTER_DATE_LAST_15_DAYS = "Last 15 Days"
const val FILTER_DATE_LAST_30_DAYS = "Last 30 Days"
const val FILTER_DATE_LAST_60_DAYS = "Last 60 Days"

private const val FILTER_TYPE_INCOME = 1
private const val FILTER_TYPE_EXPENSE = 2

private val quickDateFilters = listOf(
    FILTER_DATE_LAST_7_DAYS,
    FILTER_DATE_LAST_15_DAYS,
    FILTER_DATE_LAST_30_DAYS,
    FILTER_DATE_LAST_60_DAYS
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    selectedSort: String,
    selectedOrder: SortType,
    selectedDateRange: String?,
    selectedCustomStartDate: Long?,
    selectedCustomEndDate: Long?,
    selectedTransactionTypeIds: Set<Int>,
    availableCategories: List<CategoryType>,
    selectedCategoryIds: Set<Int>,
    paymentModes: List<PaymentType>,
    selectedPaymentTypeIds: Set<Int>,
    minAmount: String,
    maxAmount: String,
    onSortChange: (String) -> Unit,
    onOrderChange: (SortType) -> Unit,
    onDateRangeChange: (String?) -> Unit,
    onCustomDateRangeChange: (Long?, Long?) -> Unit,
    onTransactionTypeToggle: (Int) -> Unit,
    onCategoryToggle: (Int) -> Unit,
    onPaymentModeToggle: (Int) -> Unit,
    onMinAmountChange: (String) -> Unit,
    onMaxAmountChange: (String) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onClose: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val orderOptions = remember(selectedSort) { getOrderOptions(selectedSort) }

    // PERFORMANCE: Cache gradients to prevent per-frame allocation
    val brandBrush = brandGradient()
    val cardBrush = standardCardGradient()
    val chipSelectedBrush = brandGradient(alpha = 0.2f)
    val chipUnselectedBrush = subtlePrimaryGradient()

    // UI-only expansion states to hide categories by default
    var isExpenseExpanded by remember { mutableStateOf(false) }
    var isIncomeExpanded by remember { mutableStateOf(false) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var tempFromMillis by remember { mutableStateOf<Long?>(selectedCustomStartDate) }
    var tempToMillis by remember { mutableStateOf<Long?>(selectedCustomEndDate) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item(key = "drag_handle") {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(colorScheme.onSurface.copy(alpha = 0.25f))
                )
            }
        }

        item(key = "header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.55f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.desc_close_filters),
                        tint = colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.label_sort_filter),
                        style = MaterialTheme.typography.headlineSmall,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.label_sort_filter_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                TextButton(onClick = {
                    isExpenseExpanded = false
                    isIncomeExpanded = false
                    onReset()
                }) {
                    Text(
                        text = stringResource(R.string.label_reset),
                        color = colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item(key = "sort_section") {
            FilterSection(
                title = stringResource(R.string.label_sort_by),
                subtitle = stringResource(R.string.desc_sort_subtitle),
                icon = Icons.Default.Tune,
                backgroundBrush = cardBrush
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val sortOptions = listOf(
                        "Date" to (stringResource(R.string.title_date) to Icons.Default.DateRange),
                        "Amount" to (stringResource(R.string.title_amount) to Icons.Default.AttachMoney),
                        "Category" to (stringResource(R.string.title_category) to Icons.Default.GridView)
                    )
                    sortOptions.forEach { (key, pair) ->
                        val (label, icon) = pair
                        FilterChip(
                            title = label,
                            icon = icon,
                            selected = selectedSort == key,
                            selectedBrush = chipSelectedBrush,
                            unselectedBrush = chipUnselectedBrush,
                            onClick = {
                                onSortChange(key)
                                onOrderChange(getDefaultOrder(key))
                            }
                        )
                    }
                }
            }
        }

        item(key = "order_section") {
            FilterSection(
                title = stringResource(R.string.label_order),
                subtitle = stringResource(R.string.desc_order_subtitle),
                icon = Icons.AutoMirrored.Filled.Sort,
                backgroundBrush = cardBrush
            ) {
                orderOptions.forEachIndexed { index, option ->
                    OrderOption(
                        titleResId = option.titleResId,
                        subtitle = orderDescription(option.value),
                        value = option.value,
                        selectedOrder = selectedOrder
                    ) {
                        onOrderChange(option.value)
                    }

                    if (index != orderOptions.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        item(key = "filters_section") {
            FilterSection(
                title = stringResource(R.string.label_filters),
                subtitle = stringResource(R.string.desc_filters_subtitle),
                icon = Icons.Default.Tune,
                backgroundBrush = cardBrush
            ) {
                FilterGroup(title = stringResource(R.string.title_date)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        quickDateFilters.forEach { dateFilter ->
                            val label = when (dateFilter) {
                                FILTER_DATE_LAST_7_DAYS -> stringResource(R.string.label_last_7_days)
                                FILTER_DATE_LAST_15_DAYS -> stringResource(R.string.label_last_15_days)
                                FILTER_DATE_LAST_30_DAYS -> stringResource(R.string.label_last_30_days)
                                FILTER_DATE_LAST_60_DAYS -> stringResource(R.string.label_last_60_days)
                                else -> dateFilter
                            }
                            FilterChip(
                                title = label,
                                icon = Icons.Default.DateRange,
                                selected = selectedDateRange == dateFilter,
                                selectedBrush = chipSelectedBrush,
                                unselectedBrush = chipUnselectedBrush,
                                onClick = {
                                    onDateRangeChange(if (selectedDateRange == dateFilter) null else dateFilter)
                                }
                            )
                        }
                        
                        GatedAction(
                            feature = Feature.ANALYTICS_CUSTOM_RANGE,
                            displayName = stringResource(R.string.label_custom_range_filter),
                            onAction = { showDatePicker = true }
                        ) { status, gatedOnClick ->
                            val isLocked = status !is AccessStatus.Granted
                            Box {
                                val customRangeText = if (selectedDateRange == "Custom Range" && selectedCustomStartDate != null && selectedCustomEndDate != null) {
                                    com.mkn0079.expensetracker.ui.viewmodels.formatCustomRangeLabel(
                                        selectedCustomStartDate..selectedCustomEndDate
                                    )
                                } else {
                                    stringResource(R.string.label_custom_range)
                                }
                                FilterChip(
                                    title = customRangeText,
                                    icon = Icons.Default.DateRange,
                                    selected = selectedDateRange == "Custom Range",
                                    selectedBrush = chipSelectedBrush,
                                    unselectedBrush = chipUnselectedBrush,
                                    onClick = { if (isLocked) gatedOnClick() else showDatePicker = true }
                                )
                                if (isLocked) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = stringResource(R.string.desc_locked),
                                        tint = colorScheme.featureGateLock,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-4).dp, y = 4.dp)
                                            .size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    if (showDatePicker) {
                        WheelDateTimePickerModal(
                            mode = WheelPickerMode.DATE_RANGE,
                            initialStartMillis = tempFromMillis ?: System.currentTimeMillis(),
                            initialEndMillis = tempToMillis,
                            onDismissRequest = { showDatePicker = false },
                            onConfirm = { start, end ->
                                tempFromMillis = start
                                tempToMillis = end
                                onCustomDateRangeChange(start, end)
                                showDatePicker = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                FilterGroup(title = stringResource(R.string.title_transaction_type_caps)) {
                    val isAllTypesActive = selectedTransactionTypeIds.size == 2 || selectedTransactionTypeIds.isEmpty()
                    
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            title = stringResource(R.string.title_expense),
                            selected = !isAllTypesActive && selectedTransactionTypeIds.contains(FILTER_TYPE_EXPENSE),
                            selectedBrush = chipSelectedBrush,
                            unselectedBrush = chipUnselectedBrush,
                            onClick = { 
                                if (isAllTypesActive) {
                                    onTransactionTypeToggle(FILTER_TYPE_INCOME)
                                    isExpenseExpanded = true
                                    isIncomeExpanded = false
                                } else {
                                    onTransactionTypeToggle(FILTER_TYPE_EXPENSE)
                                    isExpenseExpanded = !isExpenseExpanded
                                }
                            }
                        )
                        FilterChip(
                            title = stringResource(R.string.title_income),
                            selected = !isAllTypesActive && selectedTransactionTypeIds.contains(FILTER_TYPE_INCOME),
                            selectedBrush = chipSelectedBrush,
                            unselectedBrush = chipUnselectedBrush,
                            onClick = { 
                                if (isAllTypesActive) {
                                    onTransactionTypeToggle(FILTER_TYPE_EXPENSE)
                                    isIncomeExpanded = true
                                    isExpenseExpanded = false
                                } else {
                                    onTransactionTypeToggle(FILTER_TYPE_INCOME)
                                    isIncomeExpanded = !isIncomeExpanded
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                GatedFilterGroup(
                    feature = Feature.ADVANCED_SEARCH_SCOPE,
                    displayName = stringResource(R.string.label_categories)
                ) { isLocked, onClick ->
                    val expenseCategories = remember(availableCategories) {
                        availableCategories.filter { it.transactionTypeId == FILTER_TYPE_EXPENSE }
                    }
                    val incomeCategories = remember(availableCategories) {
                        availableCategories.filter { it.transactionTypeId == FILTER_TYPE_INCOME }
                    }

                    Column {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isExpenseExpanded && expenseCategories.isNotEmpty(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                FilterGroup(title = stringResource(R.string.label_expense_categories)) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        expenseCategories.forEach { category ->
                                            FilterChip(
                                                title = category.name,
                                                icon = category.icon,
                                                selected = selectedCategoryIds.contains(category.id),
                                                selectedBrush = chipSelectedBrush,
                                                unselectedBrush = chipUnselectedBrush,
                                                onClick = { if (isLocked) onClick() else onCategoryToggle(category.id) }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = isIncomeExpanded && incomeCategories.isNotEmpty(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                FilterGroup(title = stringResource(R.string.label_income_categories)) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        incomeCategories.forEach { category ->
                                            FilterChip(
                                                title = category.name,
                                                icon = category.icon,
                                                selected = selectedCategoryIds.contains(category.id),
                                                selectedBrush = chipSelectedBrush,
                                                unselectedBrush = chipUnselectedBrush,
                                                onClick = { if (isLocked) onClick() else onCategoryToggle(category.id) }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        if ((isExpenseExpanded && expenseCategories.isEmpty()) || (isIncomeExpanded && incomeCategories.isEmpty())) {
                            if (expenseCategories.isEmpty() && incomeCategories.isEmpty()) {
                                FilterGroup(title = stringResource(R.string.label_categories)) {
                                    Text(
                                        text = stringResource(R.string.desc_no_categories),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                GatedFilterGroup(
                    feature = Feature.ADVANCED_SEARCH_SCOPE,
                    displayName = stringResource(R.string.label_payment_mode)
                ) { isLocked, onClick ->
                    FilterGroup(title = stringResource(R.string.label_payment_mode)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            paymentModes.forEach { paymentType ->
                                FilterChip(
                                    title = paymentType.name,
                                    icon = paymentType.icon,
                                    selected = selectedPaymentTypeIds.contains(paymentType.id),
                                    selectedBrush = chipSelectedBrush,
                                    unselectedBrush = chipUnselectedBrush,
                                    onClick = { if (isLocked) onClick() else onPaymentModeToggle(paymentType.id) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                FilterGroup(title = stringResource(R.string.label_amount_range)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AmountFilterField(
                            modifier = Modifier.weight(1f),
                            value = minAmount,
                            placeholder = stringResource(R.string.label_min),
                            onValueChange = onMinAmountChange
                        )
                        AmountFilterField(
                            modifier = Modifier.weight(1f),
                            value = maxAmount,
                            placeholder = stringResource(R.string.label_max),
                            onValueChange = onMaxAmountChange
                        )
                    }
                }
            }
        }

        item(key = "apply_button") {
            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(
                        elevation = 4.dp, // Reduced for performance
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = colorScheme.primary.copy(alpha = 0.15f),
                        spotColor = colorScheme.primary.copy(alpha = 0.15f)
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brandBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.label_apply_filters),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun GatedFilterGroup(
    feature: Feature,
    displayName: String,
    content: @Composable (Boolean, () -> Unit) -> Unit
) {
    GatedAction(
        feature = feature,
        displayName = displayName,
        onAction = { /* Handled via content onClick */ }
    ) { status, onClick ->
        val isLocked = status !is AccessStatus.Granted
        Box {
            content(isLocked, onClick)
            if (isLocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = stringResource(R.string.desc_locked),
                    tint = MaterialTheme.colorScheme.featureGateLock,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = (displayName.length * 7).dp) // Rough estimate for title width
                        .offset(x = 12.dp, y = 2.dp)
                        .size(14.dp)
                )
            }
        }
    }
}

/**
 * Enhanced GatedFilterGroup with better Lock positioning
 */
@Composable
private fun GatedFilterGroup(
    feature: Feature,
    displayName: String,
    title: String,
    content: @Composable (Boolean, () -> Unit) -> Unit
) {
    GatedAction(
        feature = feature,
        displayName = displayName,
        onAction = { /* Handled via content onClick */ }
    ) { status, onClick ->
        val isLocked = status !is AccessStatus.Granted
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                if (isLocked) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = stringResource(R.string.desc_locked),
                        tint = MaterialTheme.colorScheme.featureGateLock,
                        modifier = Modifier.size(14.dp).padding(bottom = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            content(isLocked, onClick)
        }
    }
}

@Composable
private fun FilterChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selectedBrush: Brush = brandGradient(alpha = 0.2f),
    unselectedBrush: Brush = subtlePrimaryGradient()
) {
    val colorScheme = MaterialTheme.colorScheme

    val selectedBorderColor = colorScheme.primary.copy(alpha = 0.35f)
    val unselectedBorderColor = colorScheme.onSurface.copy(alpha = 0.15f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) selectedBrush else unselectedBrush)
            .border(
                width = 1.dp,
                color = if (selected) selectedBorderColor else unselectedBorderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) colorScheme.primary else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) colorScheme.onSurface else colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AmountFilterField(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(sanitizeAmountRangeInput(it)) },
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surface.copy(alpha = 0.5f),
            unfocusedContainerColor = colorScheme.surface.copy(alpha = 0.3f),
            focusedBorderColor = colorScheme.primary.copy(alpha = 0.35f),
            unfocusedBorderColor = colorScheme.onSurface.copy(alpha = 0.15f),
            cursorColor = colorScheme.primary,
            focusedTextColor = colorScheme.onSurface,
            unfocusedTextColor = colorScheme.onSurface
        )
    )
}

@Composable
private fun FilterSection(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundBrush: Brush = standardCardGradient(),
    content: @Composable ColumnScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundBrush)
            .border(
                width = 1.dp,
                color = colorScheme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorScheme.primary.copy(alpha = 0.15f))
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.primary
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

private fun sanitizeAmountRangeInput(input: String): String {
    val filtered = input.filterIndexed { index, char ->
        char.isDigit() || (char == '.' && index == input.indexOf('.'))
    }
    val decimalIndex = filtered.indexOf('.')

    return if (decimalIndex >= 0) {
        val whole = filtered.substring(0, decimalIndex + 1)
        val decimals = filtered.substring(decimalIndex + 1).take(2)
        whole + decimals
    } else {
        filtered
    }
}

@Composable
private fun OrderOption(
    titleResId: Int,
    subtitle: String,
    value: SortType,
    selectedOrder: SortType,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val selected = value == selectedOrder

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) brandGradient(alpha = 0.12f) else androidx.compose.ui.graphics.SolidColor(Color.Transparent))
            .border(
                width = 1.dp,
                color = if (selected) colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (selected) brandGradient() else androidx.compose.ui.graphics.SolidColor(colorScheme.surfaceVariant.copy(alpha = 0.4f))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (value) {
                    SortType.NEWEST -> Icons.Default.VerticalAlignTop
                    SortType.OLDEST -> Icons.Default.VerticalAlignBottom
                    SortType.HIGHEST -> Icons.Default.TrendingUp
                    SortType.LOWEST -> Icons.Default.TrendingDown
                    SortType.INCOME_FIRST -> Icons.Default.ArrowUpward
                    SortType.EXPENSE_FIRST -> Icons.Default.ArrowDownward
                },
                contentDescription = null,
                tint = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(titleResId),
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }

        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = colorScheme.primary,
                unselectedColor = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
private fun orderDescription(sortType: SortType): String {
    return when (sortType) {
        SortType.NEWEST -> stringResource(R.string.desc_newest_order)
        SortType.OLDEST -> stringResource(R.string.desc_oldest_order)
        SortType.HIGHEST -> stringResource(R.string.desc_highest_order)
        SortType.LOWEST -> stringResource(R.string.desc_lowest_order)
        SortType.INCOME_FIRST -> stringResource(R.string.desc_income_first_order)
        SortType.EXPENSE_FIRST -> stringResource(R.string.desc_expense_first_order)
    }
}

@Preview(showBackground = true)
@Composable
fun FilterBottomSheetPreview() {
    var selectedSort by remember { mutableStateOf(DEFAULT_SORT_BY) }
    var selectedOrder by remember { mutableStateOf(DEFAULT_SORT_ORDER) }
    var selectedDateRange by remember { mutableStateOf<String?>(FILTER_DATE_LAST_30_DAYS) }
    var selectedTransactionTypeIds by remember { mutableStateOf(setOf(1, 2)) }
    var selectedCategoryIds by remember { mutableStateOf(setOf(5, 1)) }
    var selectedPaymentTypeIds by remember { mutableStateOf(setOf(2, 1)) }
    var minAmount by remember { mutableStateOf("") }
    var maxAmount by remember { mutableStateOf("") }
    val availableCategories = remember(selectedTransactionTypeIds) {
        categoryMap.values
            .filter { selectedTransactionTypeIds.contains(it.transactionTypeId) }
            .sortedBy { it.name }
    }

    ExpenseTrackerTheme(darkTheme = true) {
        FilterBottomSheet(
            selectedSort = selectedSort,
            selectedOrder = selectedOrder,
            selectedDateRange = selectedDateRange,
            selectedCustomStartDate = null,
            selectedCustomEndDate = null,
            selectedTransactionTypeIds = selectedTransactionTypeIds,
            availableCategories = availableCategories,
            selectedCategoryIds = selectedCategoryIds,
            paymentModes = paymentTypeMap.values.toList(),
            selectedPaymentTypeIds = selectedPaymentTypeIds,
            minAmount = minAmount,
            maxAmount = maxAmount,
            onSortChange = { selectedSort = it },
            onOrderChange = { selectedOrder = it },
            onDateRangeChange = { selectedDateRange = it },
            onCustomDateRangeChange = { start, end -> /* Dummy */ },
            onTransactionTypeToggle = {
                selectedTransactionTypeIds = selectedTransactionTypeIds.toggle(it)
                selectedCategoryIds = emptySet()
            },
            onCategoryToggle = { categoryId ->
                selectedCategoryIds = selectedCategoryIds.toggle(categoryId)
            },
            onPaymentModeToggle = { paymentTypeId ->
                selectedPaymentTypeIds = selectedPaymentTypeIds.toggle(paymentTypeId)
            },
            onMinAmountChange = { minAmount = it },
            onMaxAmountChange = { maxAmount = it },
            onApply = {},
            onReset = {
                selectedSort = DEFAULT_SORT_BY
                selectedOrder = DEFAULT_SORT_ORDER
                selectedDateRange = null
                selectedTransactionTypeIds = setOf(1, 2)
                selectedCategoryIds = emptySet()
                selectedPaymentTypeIds = emptySet()
                minAmount = ""
                maxAmount = ""
            },
            onClose = {}
        )
    }
}

private fun Set<Int>.toggle(id: Int): Set<Int> {
    return if (contains(id)) this - id else this + id
}
