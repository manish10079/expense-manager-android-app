package com.mknlabs.expensetracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.CategoryRepository
import com.mknlabs.expensetracker.domain.repository.PaymentMethodRepository
import com.mknlabs.expensetracker.ui.models.CategoryManagementTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddCategoryUiState(
    val name: String = "",
    val selectedIconId: String = "shopping_cart",
    val iconSearchQuery: String = "",
    val targetTab: CategoryManagementTab = CategoryManagementTab.Expense,
    val isDuplicateName: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class AddCategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCategoryUiState())
    val uiState: StateFlow<AddCategoryUiState> = _uiState.asStateFlow()

    fun setTargetTab(tab: CategoryManagementTab) {
        _uiState.update { 
            it.copy(
                targetTab = tab,
                selectedIconId = defaultIconIdFor(tab)
            ) 
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
        // Validation logic can go here or in a combined flow
    }

    fun onIconSearchQueryChange(query: String) {
        _uiState.update { it.copy(iconSearchQuery = query) }
    }

    fun onIconSelected(iconId: String) {
        _uiState.update { it.copy(selectedIconId = iconId) }
    }

    fun saveCategory(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val name = currentState.name.trim()
        if (name.isBlank()) return

        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            when (currentState.targetTab) {
                CategoryManagementTab.Income -> {
                    categoryRepository.createCustomCategory(name, currentState.selectedIconId, 1)
                }
                CategoryManagementTab.Expense -> {
                    categoryRepository.createCustomCategory(name, currentState.selectedIconId, 2)
                }
                CategoryManagementTab.Payment -> {
                    paymentMethodRepository.createCustomPaymentMethod(name, currentState.selectedIconId)
                }
            }
            onSuccess()
        }
    }

    private fun defaultIconIdFor(tab: CategoryManagementTab): String {
        return when (tab) {
            CategoryManagementTab.Income -> "attach_money"
            CategoryManagementTab.Expense -> "shopping_cart"
            CategoryManagementTab.Payment -> "wallet"
        }
    }
}
