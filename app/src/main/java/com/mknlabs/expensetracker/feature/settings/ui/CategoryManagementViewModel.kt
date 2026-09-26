package com.mknlabs.expensetracker.feature.settings.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.data.constants.categoryMap
import com.mknlabs.expensetracker.data.constants.paymentTypeMap
import com.mknlabs.expensetracker.domain.repository.CategoryRepository
import com.mknlabs.expensetracker.domain.repository.PaymentMethodRepository
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.core.ui.models.CategoryManagementItemUi
import com.mknlabs.expensetracker.core.ui.models.CategoryManagementTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Immutable
data class CategoryManagementUiState(
    val selectedTab: CategoryManagementTab = CategoryManagementTab.Expense,
    val incomeItems: List<CategoryManagementItemUi> = emptyList(),
    val expenseItems: List<CategoryManagementItemUi> = emptyList(),
    val paymentItems: List<CategoryManagementItemUi> = emptyList()
) {
    val items: List<CategoryManagementItemUi>
        get() = when (selectedTab) {
            CategoryManagementTab.Income -> incomeItems
            CategoryManagementTab.Expense -> expenseItems
            CategoryManagementTab.Payment -> paymentItems
        }
    val itemCount: Int get() = items.size
}

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
                categoryRepository.observeActiveCustomCategories(),
                paymentMethodRepository.observeActiveCustomPaymentMethods()
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
            transactionTypeId = 1
        )
        val expenseItems = buildCategoryManagementItems(
            categories = customCategories,
            transactionTypeId = 2
        )
        val paymentItems = buildPaymentManagementItems(customPaymentTypes)

        _uiState.update {
            it.copy(
                selectedTab = selectedTab,
                incomeItems = incomeItems,
                expenseItems = expenseItems,
                paymentItems = paymentItems
            )
        }
    }
}


private fun buildCategoryManagementItems(
    categories: List<CategoryType>,
    transactionTypeId: Int
): List<CategoryManagementItemUi> {
    // The user's own categories lead, newest first — a category they just added is the
    // one they are most likely looking for — with the built-ins after it in their fixed
    // order. The grid renders this list in order, so this is what puts a new card first.
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
            icon = category.icon,
            isUserCreated = category.id !in categoryMap
        )
    }
}

private fun buildPaymentManagementItems(
    paymentTypes: List<PaymentType>
): List<CategoryManagementItemUi> {
    // Newest payment method first, for the same reason the categories lead with theirs.
    val customItems = paymentTypes.sortedByDescending { it.id }
    val builtinItems = paymentTypeMap.values.sortedBy { it.id }

    return (customItems + builtinItems).map { paymentType ->
        CategoryManagementItemUi(
            id = paymentType.id,
            title = paymentType.name,
            icon = paymentType.icon,
            isUserCreated = paymentType.id !in paymentTypeMap
        )
    }
}
