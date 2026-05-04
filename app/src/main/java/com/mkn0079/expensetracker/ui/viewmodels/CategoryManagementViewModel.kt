package com.mkn0079.expensetracker.ui.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.mkn0079.expensetracker.data.constants.categoryMap
import com.mkn0079.expensetracker.data.constants.paymentTypeMap
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.ui.models.CategoryManagementItemUi
import com.mkn0079.expensetracker.ui.models.CategoryManagementTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.mkn0079.expensetracker.data.constants.categoryFallbackDescriptions
import com.mkn0079.expensetracker.data.constants.paymentFallbackDescriptions

@Immutable
data class CategoryManagementUiState(
    val selectedTab: CategoryManagementTab = CategoryManagementTab.Expense,
    val items: List<CategoryManagementItemUi> = emptyList(),
    val categoryCountLabel: String = "0 expense categories"
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
            fallbackSubtitle = "Custom income category"
        )
        val expenseItems = buildCategoryManagementItems(
            categories = customCategories,
            transactionTypeId = 2,
            fallbackSubtitle = "Custom expense category"
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
                categoryCountLabel = buildCategoryCountLabel(selectedTab, items.size)
            )
        }
    }
}


private fun buildCategoryManagementItems(
    categories: List<CategoryType>,
    transactionTypeId: Int,
    fallbackSubtitle: String
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
            subtitle = categoryFallbackDescriptions[category.id] ?: fallbackSubtitle,
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
            subtitle = paymentFallbackDescriptions[paymentType.id] ?: "Custom payment method",
            icon = paymentType.icon,
            isUserCreated = paymentType.id !in paymentTypeMap
        )
    }
}

private fun buildCategoryCountLabel(
    tab: CategoryManagementTab,
    count: Int
): String {
    return when (tab) {
        CategoryManagementTab.Income -> "$count income categories"
        CategoryManagementTab.Expense -> "$count expense categories"
        CategoryManagementTab.Payment -> "$count payment methods"
    }
}
