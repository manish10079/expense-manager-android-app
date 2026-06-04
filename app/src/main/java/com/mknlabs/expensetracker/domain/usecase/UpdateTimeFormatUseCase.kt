package com.mknlabs.expensetracker.domain.usecase

import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository

class UpdateTimeFormatUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(timeFormat: String) {
        repository.updateTimeFormat(timeFormat)
    }
}
