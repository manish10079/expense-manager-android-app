package com.mkn0079.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import com.mkn0079.expensetracker.data.constants.DEFAULT_SORT_BY
import com.mkn0079.expensetracker.data.constants.DEFAULT_SORT_ORDER
import com.mkn0079.expensetracker.data.constants.DEFAULT_TRANSACTION_TYPE_FILTER_ID
import com.mkn0079.expensetracker.data.constants.categoryMap
import com.mkn0079.expensetracker.data.constants.paymentTypeMap
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.SortType
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.utils.getDefaultOrder
import com.mkn0079.expensetracker.utils.getOrderOptions
import com.mkn0079.expensetracker.monetization.Feature
import com.mkn0079.expensetracker.monetization.AccessStatus

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
    val orderOptions = getOrderOptions(selectedSort)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(54.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(colorScheme.onSurface.copy(alpha = 0.65f))
                )
            }
        }

        item {
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
                        contentDescription = "Close filters",
                        tint = colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sort & Filter",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Shape how your transactions are organized and displayed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                TextButton(onClick = onReset) {
                    Text(
                        text = "Reset",
                        color = colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            FilterSection(
                title = "Sort by",
                subtitle = "Choose the main attribute used to arrange the list.",
                icon = Icons.Default.Tune
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SortChip(
                        modifier = Modifier.weight(1f),
                        title = "Date",
                        icon = Icons.Default.DateRange,
                        selected = selectedSort == "Date"
                    ) {
                        onSortChange("Date")
                        onOrderChange(getDefaultOrder("Date"))
                    }

                    SortChip(
                        modifier = Modifier.weight(1f),
                        title = "Amount",
                        icon = Icons.Default.AttachMoney,
                        selected = selectedSort == "Amount"
                    ) {
                        onSortChange("Amount")
                        onOrderChange(getDefaultOrder("Amount"))
                    }

                    SortChip(
                        modifier = Modifier.weight(1f),
                        title = "Category",
                        icon = Icons.Default.GridView,
                        selected = selectedSort == "Category"
                    ) {
                        onSortChange("Category")
                        onOrderChange(getDefaultOrder("Category"))
                    }
                }
            }
        }

        item {
            FilterSection(
                title = "Order",
                subtitle = "Fine-tune how results are ranked inside the selected sort.",
                icon = Icons.AutoMirrored.Filled.Sort
            ) {
                orderOptions.forEachIndexed { index, option ->
                    OrderOption(
                        title = option.title,
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

        item {
            FilterSection(
                title = "Filters",
                subtitle = "Stack multiple filters to narrow the transaction list.",
                icon = Icons.Default.Tune
            ) {
                FilterGroupLabel(title = "Date")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    quickDateFilters.forEach { dateFilter ->
                        FilterOptionChip(
                            title = dateFilter,
                            icon = Icons.Default.DateRange,
                            selected = selectedDateRange == dateFilter,
                            onClick = {
                                onDateRangeChange(
                                    if (selectedDateRange == dateFilter) {
                                        null
                                    } else {
                                        dateFilter
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                FilterGroupLabel(title = "Transaction Type")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterOptionChip(
                        title = "Expense",
                        selected = selectedTransactionTypeIds.contains(FILTER_TYPE_EXPENSE),
                        onClick = { onTransactionTypeToggle(FILTER_TYPE_EXPENSE) }
                    )
                    FilterOptionChip(
                        title = "Income",
                        selected = selectedTransactionTypeIds.contains(FILTER_TYPE_INCOME),
                        onClick = { onTransactionTypeToggle(FILTER_TYPE_INCOME) }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                GatedAction(
                    feature = Feature.ADVANCED_SEARCH_SCOPE,
                    displayName = "Filter by Category",
                    onAction = { /* Handle inside content via onClick */ }
                ) { status, onClick ->
                    val isLocked = status !is AccessStatus.Granted
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilterGroupLabel(
                                title = when {
                                    selectedTransactionTypeIds.contains(FILTER_TYPE_EXPENSE) && selectedTransactionTypeIds.contains(FILTER_TYPE_INCOME) -> "All Categories"
                                    selectedTransactionTypeIds.contains(FILTER_TYPE_EXPENSE) -> "Expense Categories"
                                    selectedTransactionTypeIds.contains(FILTER_TYPE_INCOME) -> "Income Categories"
                                    else -> "Categories"
                                }
                            )
                            if (isLocked) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Locked",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp).padding(bottom = 8.dp)
                                )
                            }
                        }
                        
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            availableCategories.forEach { category ->
                                FilterOptionChip(
                                    title = category.name,
                                    icon = category.icon,
                                    selected = selectedCategoryIds.contains(category.id),
                                    onClick = { 
                                        if (isLocked) onClick() else onCategoryToggle(category.id)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                GatedAction(
                    feature = Feature.ADVANCED_SEARCH_SCOPE,
                    displayName = "Filter by Wallet/Payment Mode",
                    onAction = { /* Handle inside content */ }
                ) { status, onClick ->
                    val isLocked = status !is AccessStatus.Granted
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilterGroupLabel(title = "Payment Mode")
                            if (isLocked) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Locked",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp).padding(bottom = 8.dp)
                                )
                            }
                        }
                        
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            paymentModes.forEach { paymentType ->
                                FilterOptionChip(
                                    title = paymentType.name,
                                    icon = paymentType.icon,
                                    selected = selectedPaymentTypeIds.contains(paymentType.id),
                                    onClick = { 
                                        if (isLocked) onClick() else onPaymentModeToggle(paymentType.id)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                FilterGroupLabel(title = "Amount Range")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AmountFilterField(
                        modifier = Modifier.weight(1f),
                        value = minAmount,
                        placeholder = "Min",
                        onValueChange = onMinAmountChange
                    )
                    AmountFilterField(
                        modifier = Modifier.weight(1f),
                        value = maxAmount,
                        placeholder = "Max",
                        onValueChange = onMaxAmountChange
                    )
                }
            }
        }

        item {
            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Apply Filters",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FilterGroupLabel(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold
        )
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun FilterOptionChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    val selectedBrush = remember(colorScheme.primary, colorScheme.secondary) {
        Brush.horizontalGradient(
            colors = listOf(
                colorScheme.primary.copy(alpha = 0.26f),
                colorScheme.secondary.copy(alpha = 0.18f)
            )
        )
    }

    val unselectedBrush = remember(colorScheme.surface, colorScheme.surfaceVariant) {
        Brush.horizontalGradient(
            colors = listOf(
                colorScheme.surface.copy(alpha = 0.92f),
                colorScheme.surfaceVariant.copy(alpha = 0.72f)
            )
        )
    }

    val selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    val unselectedBorderColor = remember(colorScheme.onSurface) { colorScheme.onSurface.copy(alpha =  0.65f) }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = if (selected) selectedBrush else unselectedBrush
            )
            .border(
                width = 1.dp,
                color = if (selected) selectedBorderColor else unselectedBorderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else colorScheme.onSurfaceVariant,
                modifier = Modifier.width(16.dp)
            )
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
                color = colorScheme.onSurfaceVariant
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colorScheme.surface.copy(alpha = 0.92f),
            unfocusedContainerColor = colorScheme.surface.copy(alpha = 0.92f),
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
            unfocusedBorderColor = colorScheme.onSurface.copy(alpha =  0.65f),
            cursorColor = MaterialTheme.colorScheme.primary,
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
    content: @Composable ColumnScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = colorScheme.onSurface.copy(alpha = 0.65f),
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
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorScheme.primary.copy(alpha = 0.20f),
                                colorScheme.secondary.copy(alpha = 0.14f)
                            )
                        )
                    )
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

private fun orderDescription(sortType: SortType): String {
    return when (sortType) {
        SortType.NEWEST -> "Most recent transactions appear at the top."
        SortType.OLDEST -> "Earlier transactions show up first."
        SortType.HIGHEST -> "Larger amounts take priority in the list."
        SortType.LOWEST -> "Smaller amounts appear before bigger ones."
        SortType.INCOME_FIRST -> "Income transactions are grouped before expenses."
        SortType.EXPENSE_FIRST -> "Expense transactions are shown before income."
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
