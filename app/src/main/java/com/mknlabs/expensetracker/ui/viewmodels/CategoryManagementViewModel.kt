package com.mknlabs.expensetracker.ui.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.ui.models.CategoryManagementItemUi
import com.mknlabs.expensetracker.ui.models.CategoryManagementTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.mknlabs.expensetracker.data.constants.categoryFallbackDescriptions
import com.mknlabs.expensetracker.data.constants.paymentFallbackDescriptions

@Immutable
data class CategoryManagementUiState(
    val selectedTab: CategoryManagementTab = CategoryManagementTab.Expense,
    val items: List<CategoryManagementItemUi> = emptyList(),
    val itemCount: Int = 0
)

class CategoryManagementViewModel : ViewModel() {

    private var customCategories: List<CategoryType> = emptyList()
    private var customPaymentTypes: List<PaymentType> = emptyList()
    private var selectedTab: CategoryManagementTab = CategoryManagementTab.Expense

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    init {
        rebuildUiState()
    }

    fun updateInputs(
        customCategories: List<CategoryType>,
        customPaymentTypes: List<PaymentType>
    ) {
        this.customCategories = customCategories
        this.customPaymentTypes = customPaymentTypes
        rebuildUiState()
    }

    fun selectTab(tab: CategoryManagementTab) {
        selectedTab = tab
        rebuildUiState()
    }

    private fun rebuildUiState() {
        val incomeItems = buildCategoryManagementItems(
            categories = customCategories,
            transactionTypeId = 1,
            fallbackSubtitleRes = R.string.title_custom_income_category
        )
        val expenseItems = buildCategoryManagementItems(
            categories = customCategories,
            transactionTypeId = 2,
            fallbackSubtitleRes = R.string.title_custom_expense_category
        )
        val paymentItems = buildPaymentManagementItems(customPaymentTypes)
        val items = when (selectedTab) {
            CategoryManagementTab.Income -> incomeItems
            CategoryManagementTab.Expense -> expenseItems
            CategoryManagementTab.Payment -> paymentItems
        }

        _uiState.update {
            it.copy(
                selectedTab = selectedTab,
                items = items,
                itemCount = items.size
            )
        }
    }
}


private fun buildCategoryManagementItems(
    categories: List<CategoryType>,
    transactionTypeId: Int,
    fallbackSubtitleRes: Int
): List<CategoryManagementItemUi> {
    val customItems = categories
        .filter { it.transactionTypeId == transactionTypeId }
        .sortedByDescending { it.id }
    val builtinItems = categoryMap.values
        .filter { it.transactionTypeId == transactionTypeId }
        .sortedBy { it.id }

    return (customItems + builtinItems).map { category ->
        CategoryManagementItemUi(
            id = category.id,
            title = category.name,
            subtitleRes = categoryFallbackDescriptions[category.id] ?: fallbackSubtitleRes,
            icon = category.icon,
            isUserCreated = category.id !in categoryMap
        )
    }
}

private fun buildPaymentManagementItems(
    paymentTypes: List<PaymentType>
): List<CategoryManagementItemUi> {
    val customItems = paymentTypes.sortedByDescending { it.id }
    val builtinItems = paymentTypeMap.values.sortedBy { it.id }

    return (customItems + builtinItems).map { paymentType ->
        CategoryManagementItemUi(
            id = paymentType.id,
            title = paymentType.name,
            subtitleRes = paymentFallbackDescriptions[paymentType.id] ?: R.string.label_custom_payment_method,
            icon = paymentType.icon,
            isUserCreated = paymentType.id !in paymentTypeMap
        )
    }
}


