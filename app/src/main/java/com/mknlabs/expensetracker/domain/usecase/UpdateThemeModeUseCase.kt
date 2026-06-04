package com.mknlabs.expensetracker.domain.usecase

import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.models.AppThemeMode

class UpdateThemeModeUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(themeMode: AppThemeMode) {
        repository.updateThemeMode(themeMode)
    }
}
