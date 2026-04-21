package com.mkn0079.expensetracker.domain.usecase

import com.mkn0079.expensetracker.domain.repository.AppPreferencesRepository

class UpdateDateFormatUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(dateFormatPattern: String) {
        repository.updateDateFormat(dateFormatPattern)
    }
}
