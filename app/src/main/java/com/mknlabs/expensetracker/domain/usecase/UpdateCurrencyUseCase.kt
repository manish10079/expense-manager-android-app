package com.mknlabs.expensetracker.domain.usecase

import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository

class UpdateCurrencyUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(currencyId: Int) {
        repository.updateCurrency(currencyId)
    }
}
