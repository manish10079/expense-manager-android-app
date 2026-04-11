package com.mkn0079.expensetracker.ui.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.mkn0079.expensetracker.data.constants.DEFAULT_CURRENCY_ID
import com.mkn0079.expensetracker.data.constants.DEFAULT_DATE_FORMAT_PATTERN
import com.mkn0079.expensetracker.data.constants.DEFAULT_SORT_BY
import com.mkn0079.expensetracker.data.constants.DEFAULT_SORT_ORDER
import com.mkn0079.expensetracker.data.constants.DEFAULT_TIME_FORMAT
import com.mkn0079.expensetracker.data.constants.DEFAULT_TRANSACTION_TYPE_FILTER_ID
import com.mkn0079.expensetracker.data.constants.categoryMap
import com.mkn0079.expensetracker.data.constants.paymentTypeMap
import com.mkn0079.expensetracker.domain.mapper.buildTransactionListItems
import com.mkn0079.expensetracker.domain.mapper.toTransactionCardItemUi
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.SortType
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.models.TransactionCardCustomizationSettings
import com.mkn0079.expensetracker.ui.components.FILTER_DATE_LAST_15_DAYS
import com.mkn0079.expensetracker.ui.components.FILTER_DATE_LAST_30_DAYS
import com.mkn0079.expensetracker.ui.components.FILTER_DATE_LAST_60_DAYS
import com.mkn0079.expensetracker.ui.components.FILTER_DATE_LAST_7_DAYS
import com.mkn0079.expensetracker.ui.components.TransactionPeriodFilter
import com.mkn0079.expensetracker.ui.models.TransactionListItemUi
import com.mkn0079.expensetracker.utils.formatDate
import com.mkn0079.expensetracker.utils.getDefaultOrder
import com.mkn0079.expensetracker.utils.sortTransactions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Immutable
data class TransactionsScreenUiState(
    val searchQuery: String = "",
    val selectedSort: String = DEFAULT_SORT_BY,
    val selectedOrder: SortType = DEFAULT_SORT_ORDER,
    val selectedDateRange: String? = null,
    val selectedTransactionTypeId: Int? = DEFAULT_TRANSACTION_TYPE_FILTER_ID,
    val selectedCategoryIds: Set<Int> = emptySet(),
    val selectedPaymentTypeIds: Set<Int> = emptySet(),
    val selectedMinAmount: String = "",
    val selectedMaxAmount: String = "",
    val selectedPeriodFilter: TransactionPeriodFilter = TransactionPeriodFilter.MONTHLY,
    val focusedPeriodTimestamp: Long = 0L,
    val canNavigateBackward: Boolean = false,
    val canNavigateForward: Boolean = false,
    val selectedPeriodLabel: String = "",
    val availableCategories: List<CategoryType> = emptyList(),
    val paymentModes: List<PaymentType> = emptyList(),
    val transactionItems: List<TransactionListItemUi> = emptyList(),
    val customizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings()
)

class TransactionsViewModel : ViewModel() {

    private var currentTransactions: List<Transaction> = emptyList()
    private var currentCurrencyId: Int = DEFAULT_CURRENCY_ID
    private var currentDateFormatPattern: String = DEFAULT_DATE_FORMAT_PATTERN
    private var currentTimeFormat: String = DEFAULT_TIME_FORMAT
    private var currentCustomizationSettings: TransactionCardCustomizationSettings = TransactionCardCustomizationSettings()
    private var latestTransactionTimestamp: Long = System.currentTimeMillis()

    private var searchQuery: String = ""
    private var selectedSort: String = DEFAULT_SORT_BY
    private var selectedOrder: SortType = DEFAULT_SORT_ORDER
    private var selectedDateRange: String? = null
    private var selectedTransactionTypeId: Int? = DEFAULT_TRANSACTION_TYPE_FILTER_ID
    private var selectedCategoryIds: Set<Int> = emptySet()
    private var selectedPaymentTypeIds: Set<Int> = emptySet()
    private var selectedMinAmount: String = ""
    private var selectedMaxAmount: String = ""

    private var appliedSortType: SortType = DEFAULT_SORT_ORDER
    private var appliedDateRange: String? = null
    private var appliedTransactionTypeId: Int? = null
    private var appliedCategoryIds: Set<Int> = emptySet()
    private var appliedPaymentTypeIds: Set<Int> = emptySet()
    private var appliedMinAmount: String = ""
    private var appliedMaxAmount: String = ""

    private var selectedPeriodFilter: TransactionPeriodFilter = TransactionPeriodFilter.MONTHLY
    private var focusedPeriodTimestamp: Long = latestTransactionTimestamp

    private val _uiState = MutableStateFlow(
        TransactionsScreenUiState(
            focusedPeriodTimestamp = focusedPeriodTimestamp,
            paymentModes = paymentTypeMap.values.toList()
        )
    )
    val uiState: StateFlow<TransactionsScreenUiState> = _uiState.asStateFlow()

    init {
        rebuildUiState()
    }

    fun updateInputs(
        transactions: List<Transaction>,
        currencyId: Int,
        dateFormatPattern: String,
        timeFormat: String,
        customizationSettings: TransactionCardCustomizationSettings
    ) {
        currentTransactions = transactions
        currentCurrencyId = currencyId
        currentDateFormatPattern = dateFormatPattern
        currentTimeFormat = timeFormat
        currentCustomizationSettings = customizationSettings
        latestTransactionTimestamp = transactions.maxOfOrNull { it.createdAt } ?: System.currentTimeMillis()
        if (focusedPeriodTimestamp == 0L) {
            focusedPeriodTimestamp = latestTransactionTimestamp
        }
        rebuildUiState()
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        rebuildUiState()
    }

    fun updateSort(sort: String) {
        selectedSort = sort
        selectedOrder = getDefaultOrder(sort)
        rebuildUiState()
    }

    fun updateOrder(order: SortType) {
        selectedOrder = order
        rebuildUiState()
    }

    fun updateDateRange(dateRange: String?) {
        selectedDateRange = dateRange
        rebuildUiState()
    }

    fun updateTransactionTypeFilter(transactionTypeId: Int?) {
        selectedTransactionTypeId = transactionTypeId
        selectedCategoryIds = emptySet()
        rebuildUiState()
    }

    fun toggleCategory(categoryId: Int) {
        selectedCategoryIds = selectedCategoryIds.toggle(categoryId)
        rebuildUiState()
    }

    fun togglePaymentMode(paymentTypeId: Int) {
        selectedPaymentTypeIds = selectedPaymentTypeIds.toggle(paymentTypeId)
        rebuildUiState()
    }

    fun updateMinAmount(amount: String) {
        selectedMinAmount = amount
        rebuildUiState()
    }

    fun updateMaxAmount(amount: String) {
        selectedMaxAmount = amount
        rebuildUiState()
    }

    fun applyFilters() {
        appliedSortType = selectedOrder
        appliedDateRange = selectedDateRange
        appliedTransactionTypeId = selectedTransactionTypeId
        appliedCategoryIds = selectedCategoryIds
        appliedPaymentTypeIds = selectedPaymentTypeIds
        appliedMinAmount = selectedMinAmount
        appliedMaxAmount = selectedMaxAmount
        rebuildUiState()
    }

    fun resetDraftFilters() {
        selectedSort = DEFAULT_SORT_BY
        selectedOrder = DEFAULT_SORT_ORDER
        selectedDateRange = null
        selectedTransactionTypeId = DEFAULT_TRANSACTION_TYPE_FILTER_ID
        selectedCategoryIds = emptySet()
        selectedPaymentTypeIds = emptySet()
        selectedMinAmount = ""
        selectedMaxAmount = ""
        rebuildUiState()
    }

    fun updatePeriodFilter(filter: TransactionPeriodFilter) {
        selectedPeriodFilter = filter
        focusedPeriodTimestamp = latestTransactionTimestamp
        rebuildUiState()
    }

    fun navigatePeriod(step: Int) {
        focusedPeriodTimestamp = shiftPeriod(
            timestamp = focusedPeriodTimestamp,
            filter = selectedPeriodFilter,
            step = step
        )
        rebuildUiState()
    }

    private fun rebuildUiState() {
        val availableCategories = categoryMap.values
            .filter { it.transactionTypeId == selectedTransactionTypeId }
            .sortedBy { it.name }

        val appliedMinAmountValue = appliedMinAmount.toDoubleOrNull()
        val appliedMaxAmountValue = appliedMaxAmount.toDoubleOrNull()
        val paymentTypeNames = paymentTypeMap.mapValues { it.value.name }
        val normalizedQuery = searchQuery.trim()

        val filteredTransactions = sortTransactions(
            currentTransactions.filter { transaction ->
                val paymentName = paymentTypeNames[transaction.paymentTypeId].orEmpty()
                val matchesSearchQuery = normalizedQuery.isBlank() ||
                    transaction.note.contains(normalizedQuery, ignoreCase = true) ||
                    transaction.amount.toString().contains(normalizedQuery, ignoreCase = true) ||
                    paymentName.contains(normalizedQuery, ignoreCase = true)

                matchesSelectedPeriod(
                    transactionTimestamp = transaction.createdAt,
                    focusedTimestamp = focusedPeriodTimestamp,
                    filter = selectedPeriodFilter
                ) &&
                    matchesSearchQuery &&
                    matchesQuickDateFilter(
                        transactionTimestamp = transaction.createdAt,
                        selectedDateRange = appliedDateRange,
                        anchorTimestamp = latestTransactionTimestamp
                    ) &&
                    matchesTransactionTypeFilter(
                        transactionTypeId = transaction.transactionTypeId,
                        selectedTransactionTypeId = appliedTransactionTypeId
                    ) &&
                    matchesCategoryFilter(
                        categoryId = transaction.categoryId,
                        selectedCategoryIds = appliedCategoryIds
                    ) &&
                    matchesPaymentModeFilter(
                        paymentTypeId = transaction.paymentTypeId,
                        selectedPaymentTypeIds = appliedPaymentTypeIds
                    ) &&
                    matchesAmountRangeFilter(
                        amount = transaction.amount,
                        minAmount = appliedMinAmountValue,
                        maxAmount = appliedMaxAmountValue
                    )
            },
            appliedSortType
        )

        val shouldGroupTransactions = currentCustomizationSettings.showDateSeparators &&
            selectedPeriodFilter != TransactionPeriodFilter.DAILY
        val transactionItems = buildTransactionListItems(
            transactions = filteredTransactions.map { transaction ->
                transaction.toTransactionCardItemUi(
                    currencyId = currentCurrencyId,
                    dateFormatPattern = currentDateFormatPattern,
                    timeFormat = currentTimeFormat,
                    paymentTypeName = paymentTypeNames[transaction.paymentTypeId].orEmpty()
                )
            },
            groupByDate = shouldGroupTransactions,
            sortType = appliedSortType
        )

        _uiState.update {
            it.copy(
                searchQuery = searchQuery,
                selectedSort = selectedSort,
                selectedOrder = selectedOrder,
                selectedDateRange = selectedDateRange,
                selectedTransactionTypeId = selectedTransactionTypeId,
                selectedCategoryIds = selectedCategoryIds,
                selectedPaymentTypeIds = selectedPaymentTypeIds,
                selectedMinAmount = selectedMinAmount,
                selectedMaxAmount = selectedMaxAmount,
                selectedPeriodFilter = selectedPeriodFilter,
                focusedPeriodTimestamp = focusedPeriodTimestamp,
                canNavigateBackward = canNavigateToPeriod(
                    transactions = currentTransactions,
                    focusedTimestamp = focusedPeriodTimestamp,
                    filter = selectedPeriodFilter,
                    direction = -1
                ),
                canNavigateForward = canNavigateToPeriod(
                    transactions = currentTransactions,
                    focusedTimestamp = focusedPeriodTimestamp,
                    filter = selectedPeriodFilter,
                    direction = 1
                ),
                selectedPeriodLabel = buildPeriodLabel(
                    timestamp = focusedPeriodTimestamp,
                    filter = selectedPeriodFilter,
                    dateFormatPattern = currentDateFormatPattern
                ),
                availableCategories = availableCategories,
                paymentModes = paymentTypeMap.values.toList(),
                transactionItems = transactionItems,
                customizationSettings = currentCustomizationSettings
            )
        }
    }
}

private fun matchesQuickDateFilter(
    transactionTimestamp: Long,
    selectedDateRange: String?,
    anchorTimestamp: Long
): Boolean {
    val rangeDays = when (selectedDateRange) {
        FILTER_DATE_LAST_7_DAYS -> 7
        FILTER_DATE_LAST_15_DAYS -> 15
        FILTER_DATE_LAST_30_DAYS -> 30
        FILTER_DATE_LAST_60_DAYS -> 60
        null -> return true
        else -> return true
    }

    val calendar = Calendar.getInstance().apply {
        timeInMillis = anchorTimestamp
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val endTimestamp = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_YEAR, -(rangeDays - 1))
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startTimestamp = calendar.timeInMillis

    return transactionTimestamp in startTimestamp..endTimestamp
}

private fun matchesTransactionTypeFilter(
    transactionTypeId: Int,
    selectedTransactionTypeId: Int?
): Boolean {
    return selectedTransactionTypeId == null || transactionTypeId == selectedTransactionTypeId
}

private fun matchesCategoryFilter(
    categoryId: Int,
    selectedCategoryIds: Set<Int>
): Boolean {
    return selectedCategoryIds.isEmpty() || selectedCategoryIds.contains(categoryId)
}

private fun matchesPaymentModeFilter(
    paymentTypeId: Int,
    selectedPaymentTypeIds: Set<Int>
): Boolean {
    return selectedPaymentTypeIds.isEmpty() || selectedPaymentTypeIds.contains(paymentTypeId)
}

private fun matchesAmountRangeFilter(
    amount: Double,
    minAmount: Double?,
    maxAmount: Double?
): Boolean {
    val matchesMin = minAmount == null || amount >= minAmount
    val matchesMax = maxAmount == null || amount <= maxAmount
    return matchesMin && matchesMax
}

private fun matchesSelectedPeriod(
    transactionTimestamp: Long,
    focusedTimestamp: Long,
    filter: TransactionPeriodFilter
): Boolean {
    if (filter == TransactionPeriodFilter.ALL) {
        return true
    }

    val transactionCalendar = Calendar.getInstance().apply { timeInMillis = transactionTimestamp }
    val focusedCalendar = Calendar.getInstance().apply { timeInMillis = focusedTimestamp }

    return when (filter) {
        TransactionPeriodFilter.ALL -> true
        TransactionPeriodFilter.DAILY -> {
            transactionCalendar.get(Calendar.YEAR) == focusedCalendar.get(Calendar.YEAR) &&
                transactionCalendar.get(Calendar.DAY_OF_YEAR) == focusedCalendar.get(Calendar.DAY_OF_YEAR)
        }

        TransactionPeriodFilter.MONTHLY -> {
            transactionCalendar.get(Calendar.YEAR) == focusedCalendar.get(Calendar.YEAR) &&
                transactionCalendar.get(Calendar.MONTH) == focusedCalendar.get(Calendar.MONTH)
        }

        TransactionPeriodFilter.YEARLY -> {
            transactionCalendar.get(Calendar.YEAR) == focusedCalendar.get(Calendar.YEAR)
        }
    }
}

private fun canNavigateToPeriod(
    transactions: List<Transaction>,
    focusedTimestamp: Long,
    filter: TransactionPeriodFilter,
    direction: Int
): Boolean {
    if (filter == TransactionPeriodFilter.ALL) {
        return false
    }

    val candidateTimestamp = shiftPeriod(
        timestamp = focusedTimestamp,
        filter = filter,
        step = direction
    )

    return transactions.any { transaction ->
        matchesSelectedPeriod(
            transactionTimestamp = transaction.createdAt,
            focusedTimestamp = candidateTimestamp,
            filter = filter
        )
    }
}

private fun shiftPeriod(
    timestamp: Long,
    filter: TransactionPeriodFilter,
    step: Int
): Long {
    if (filter == TransactionPeriodFilter.ALL) {
        return timestamp
    }

    return Calendar.getInstance().apply {
        timeInMillis = timestamp
        when (filter) {
            TransactionPeriodFilter.ALL -> Unit
            TransactionPeriodFilter.DAILY -> add(Calendar.DAY_OF_YEAR, step)
            TransactionPeriodFilter.MONTHLY -> add(Calendar.MONTH, step)
            TransactionPeriodFilter.YEARLY -> add(Calendar.YEAR, step)
        }
    }.timeInMillis
}

private fun buildPeriodLabel(
    timestamp: Long,
    filter: TransactionPeriodFilter,
    dateFormatPattern: String
): String {
    val date = Date(timestamp)

    return when (filter) {
        TransactionPeriodFilter.ALL -> "All Records"
        TransactionPeriodFilter.DAILY -> formatDate(timestamp, dateFormatPattern)
        TransactionPeriodFilter.MONTHLY -> SimpleDateFormat("MMMM, yyyy", Locale.getDefault()).format(date)
        TransactionPeriodFilter.YEARLY -> SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
    }
}

private fun Set<Int>.toggle(id: Int): Set<Int> {
    return if (contains(id)) this - id else this + id
}
