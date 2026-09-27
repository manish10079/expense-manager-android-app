package com.mknlabs.expensetracker.feature.settings.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                // The **active** rows, seeded ones included — not the custom-only stream this used
                // to read alongside the seeded constants.
                //
                // Two things follow, and the second is why it changed. A user-created row still
                // arrives through the flow, so creating one still shows up immediately. And a
                // *seeded* row now arrives through it too, which is the only way a colour stored on
                // one can reach this grid: the constant map that used to supply the built-ins is
                // keyed by id and can never carry a value, so a recoloured built-in would have
                // looked uncoloured here until the screen was reopened. `observeAllCategories` is
                // deliberately not used — it has no `is_deleted` filter, so a deleted custom
                // category would come back.
                categoryRepository.observeActiveCategories(),
                paymentMethodRepository.observeActivePaymentMethods()
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

    /**
     * Sets, changes or clears the colour of one row, seeded or user-created.
     *
     * Takes the whole [item] rather than an id so the category-versus-payment decision stays in one
     * place: both are keyed from 1, so an id alone does not say which repository to write to. The
     * grid re-renders from the observed flow, so nothing is echoed into the UI state here — a local
     * guess at the new value would be a second source of truth that could disagree with the row.
     */
    fun updateColor(item: CategoryManagementItemUi, colorHex: String?) {
        viewModelScope.launch {
            if (item.isPaymentMethod) {
                paymentMethodRepository.updatePaymentMethodColor(item.id, colorHex)
            } else {
                categoryRepository.updateCategoryColor(item.id, colorHex)
            }
        }
    }

    private fun rebuildUiState(
        categories: List<CategoryType>,
        paymentTypes: List<PaymentType>
    ) {
        val incomeItems = buildCategoryManagementItems(
            categories = categories,
            transactionTypeId = TRANSACTION_TYPE_INCOME
        )
        val expenseItems = buildCategoryManagementItems(
            categories = categories,
            transactionTypeId = TRANSACTION_TYPE_EXPENSE
        )
        val paymentItems = buildPaymentManagementItems(paymentTypes)

        _uiState.update {
            it.copy(
                selectedTab = selectedTab,
                incomeItems = incomeItems,
                expenseItems = expenseItems,
                paymentItems = paymentItems
            )
        }
    }

    private companion object {
        /** `categoryTypeData.kt`'s own ids: 1 is income, 2 is expense. */
        const val TRANSACTION_TYPE_INCOME = 1
        const val TRANSACTION_TYPE_EXPENSE = 2
    }
}

private fun buildCategoryManagementItems(
    categories: List<CategoryType>,
    transactionTypeId: Int
): List<CategoryManagementItemUi> {
    // The user's own categories lead, newest first — a category they just added is the one they are
    // most likely looking for — with the built-ins after it in their fixed order. The grid renders
    // this list in order, so this is what puts a new card first.
    //
    // Both halves are the same rows now, split on `isSystem` rather than on "is it in the constant".
    // That is also the flag the DAO's delete guard uses (`is_system = 0`), so the 'x' the card draws
    // and the delete the database permits can no longer disagree.
    val customItems = categories
        .filter { it.transactionTypeId == transactionTypeId && !it.isSystem }
        .sortedByDescending { it.id }
    val builtinItems = categories
        .filter { it.transactionTypeId == transactionTypeId && it.isSystem }
        .sortedBy { it.id }

    return (customItems + builtinItems).map { category ->
        CategoryManagementItemUi(
            id = category.id,
            title = category.name,
            icon = category.icon,
            isUserCreated = !category.isSystem,
            colorHex = category.colorHex
        )
    }
}

private fun buildPaymentManagementItems(
    paymentTypes: List<PaymentType>
): List<CategoryManagementItemUi> {
    // Newest payment method first, for the same reason the categories lead with theirs.
    val customItems = paymentTypes.filter { !it.isSystem }.sortedByDescending { it.id }
    val builtinItems = paymentTypes.filter { it.isSystem }.sortedBy { it.id }

    return (customItems + builtinItems).map { paymentType ->
        CategoryManagementItemUi(
            id = paymentType.id,
            title = paymentType.name,
            icon = paymentType.icon,
            isUserCreated = !paymentType.isSystem,
            // Payment ids restart at 1, so this flag is what keeps UPI's card from drawing Food's
            // colour — and from writing to the categories table.
            colorHex = paymentType.colorHex,
            isPaymentMethod = true
        )
    }
}
