package com.mkn0079.expensetracker.domain.usecase

import com.mkn0079.expensetracker.domain.repository.AppPreferencesRepository

class GetAppPreferencesUseCase(
    private val repository: AppPreferencesRepository
) {
    operator fun invoke() = repository.observeAppSettings()
}
