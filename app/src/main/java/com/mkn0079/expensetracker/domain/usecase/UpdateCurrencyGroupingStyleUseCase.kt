package com.mkn0079.expensetracker.domain.usecase

import com.mkn0079.expensetracker.domain.repository.AppPreferencesRepository
import com.mkn0079.expensetracker.models.CurrencyGroupingStyle

class UpdateCurrencyGroupingStyleUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(groupingStyle: CurrencyGroupingStyle) {
        repository.updateCurrencyGroupingStyle(groupingStyle)
    }
}
