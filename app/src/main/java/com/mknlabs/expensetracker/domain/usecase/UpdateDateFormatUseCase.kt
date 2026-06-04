package com.mknlabs.expensetracker.domain.usecase

import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository

class UpdateDateFormatUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(dateFormatPattern: String) {
        repository.updateDateFormat(dateFormatPattern)
    }
}
