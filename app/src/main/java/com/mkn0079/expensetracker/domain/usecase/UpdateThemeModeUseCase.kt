package com.mkn0079.expensetracker.domain.usecase

import com.mkn0079.expensetracker.domain.repository.AppPreferencesRepository
import com.mkn0079.expensetracker.models.AppThemeMode

class UpdateThemeModeUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(themeMode: AppThemeMode) {
        repository.updateThemeMode(themeMode)
    }
}
