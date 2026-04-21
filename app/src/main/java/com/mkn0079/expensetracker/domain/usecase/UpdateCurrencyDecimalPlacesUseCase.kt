package com.mkn0079.expensetracker.domain.usecase

import com.mkn0079.expensetracker.domain.repository.AppPreferencesRepository

class UpdateCurrencyDecimalPlacesUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(decimalPlaces: Int) {
        repository.updateCurrencyDecimalPlaces(decimalPlaces)
    }
}
