package com.mknlabs.expensetracker.domain.usecase

import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.models.CurrencyGroupingStyle

class UpdateCurrencyGroupingStyleUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(groupingStyle: CurrencyGroupingStyle) {
        repository.updateCurrencyGroupingStyle(groupingStyle)
    }
}
