package com.mknlabs.expensetracker.domain.usecase

import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository

class UpdateLanguageUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(languageCode: String) {
        repository.updateLanguage(languageCode)
    }
}
