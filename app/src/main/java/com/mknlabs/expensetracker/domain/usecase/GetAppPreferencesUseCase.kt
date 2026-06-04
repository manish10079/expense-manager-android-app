package com.mknlabs.expensetracker.domain.usecase

import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository

class GetAppPreferencesUseCase(
    private val repository: AppPreferencesRepository
) {
    operator fun invoke() = repository.observeAppSettings()
}
