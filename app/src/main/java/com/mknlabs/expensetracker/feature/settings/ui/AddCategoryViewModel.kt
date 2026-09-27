package com.mknlabs.expensetracker.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.CategoryRepository
import com.mknlabs.expensetracker.domain.repository.PaymentMethodRepository
import com.mknlabs.expensetracker.core.ui.models.CategoryManagementTab
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
    val isSaving: Boolean = false,
    /**
     * The colour the user chose, as `#RRGGBB`, or null for "no colour of its own".
     *
     * Null is the default and an ordinary answer rather than an absence: the row simply takes
     * the palette colour for its id. The picker therefore has to offer a way back to null, which
     * is why the swatch row leads with an explicit "default" option instead of treating a
     * selection as one-way.
     */
    val selectedColorHex: String? = null
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
            AddCategoryUiState(
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

    /**
     * Takes the hex the user tapped, or null for the default swatch.
     *
     * Stored exactly as offered rather than re-derived from an id: the swatch the user saw and
     * the value kept are the same colour by construction, so there is no lookup that could
     * disagree with the screen.
     */
    fun onColorSelected(colorHex: String?) {
        _uiState.update { it.copy(selectedColorHex = colorHex) }
    }

    fun saveCategory(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val name = currentState.name.trim()
        if (name.isBlank()) return

        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                when (currentState.targetTab) {
                    CategoryManagementTab.Income -> {
                        categoryRepository.createCustomCategory(
                            name, currentState.selectedIconId, 1, currentState.selectedColorHex
                        )
                    }
                    CategoryManagementTab.Expense -> {
                        categoryRepository.createCustomCategory(
                            name, currentState.selectedIconId, 2, currentState.selectedColorHex
                        )
                    }
                    CategoryManagementTab.Payment -> {
                        paymentMethodRepository.createCustomPaymentMethod(
                            name, currentState.selectedIconId, currentState.selectedColorHex
                        )
                    }
                }
                onSuccess()
                // Intentionally do NOT reset isSaving here.
                // isSaving = true blocks the duplicate-name error from ever
                // rendering while the screen is alive. Once onSuccess() navigates
                // away the composable is disposed, so the flag never matters.
            } catch (_: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun resetState() {
        val currentTab = _uiState.value.targetTab
        _uiState.update {
            AddCategoryUiState(
                targetTab = currentTab,
                selectedIconId = defaultIconIdFor(currentTab)
            )
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
