package com.mknlabs.expensetracker.domain.usecase

import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository

class UpdateCurrencyDecimalPlacesUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(decimalPlaces: Int) {
        repository.updateCurrencyDecimalPlaces(decimalPlaces)
    }
}
