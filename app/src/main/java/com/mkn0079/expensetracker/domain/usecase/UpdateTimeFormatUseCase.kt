package com.mkn0079.expensetracker.domain.usecase

import com.mkn0079.expensetracker.domain.repository.AppPreferencesRepository

class UpdateTimeFormatUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(timeFormat: String) {
        repository.updateTimeFormat(timeFormat)
    }
}
