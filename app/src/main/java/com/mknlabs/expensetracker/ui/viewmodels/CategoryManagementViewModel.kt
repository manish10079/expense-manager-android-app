package com.mknlabs.expensetracker.ui.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.domain.repository.CategoryRepository
import com.mknlabs.expensetracker.domain.repository.PaymentMethodRepository
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.ui.models.CategoryManagementItemUi
import com.mknlabs.expensetracker.ui.models.CategoryManagementTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import com.mknlabs.expensetracker.data.constants.categoryFallbackDescriptions
import com.mknlabs.expensetracker.data.constants.paymentFallbackDescriptions
import javax.inject.Inject

@Immutable
data class CategoryManagementUiState(
    val selectedTab: CategoryManagementTab = CategoryManagementTab.Expense,
    val items: List<CategoryManagementItemUi> = emptyList(),
    val itemCount: Int = 0
)

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private var selectedTab: CategoryManagementTab = CategoryManagementTab.Expense
    private var latestCategories: List<CategoryType> = emptyList()
    private var latestPaymentTypes: List<PaymentType> = emptyList()

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                categoryRepository.observeAllCategories(),
                paymentMethodRepository.observeAllPaymentMethods()
            ) { categories, paymentMethods ->
                categories to paymentMethods
            }.collect { (categories, paymentMethods) ->
                latestCategories = categories
                latestPaymentTypes = paymentMethods
                rebuildUiState(categories, paymentMethods)
            }
        }
    }

    fun selectTab(tab: CategoryManagementTab) {
        selectedTab = tab
        rebuildUiState(latestCategories, latestPaymentTypes)
    }

    private fun rebuildUiState(
        customCategories: List<CategoryType>,
        customPaymentTypes: List<PaymentType>
    ) {
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
