package com.mkn0079.expensetracker.domain.usecase

import com.mkn0079.expensetracker.domain.repository.AppPreferencesRepository

class UpdateCurrencyUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(currencyId: Int) {
        repository.updateCurrency(currencyId)
    }
}
