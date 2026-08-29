package com.mknlabs.expensetracker.domain.usecase

import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.models.FontMode
import javax.inject.Inject

class SetActiveFontUseCase @Inject constructor(
    private val repository: AppPreferencesRepository
) {
    /**
     * Set the active font mode.
     * For CUSTOM mode, also set the active custom font filename.
     */
    suspend operator fun invoke(fontMode: FontMode, customFontFileName: String? = null) {
        when (fontMode) {
            FontMode.APP -> {
                repository.updateFontMode(FontMode.APP)
                repository.setActiveCustomFont(null)
            }
            FontMode.SYSTEM -> {
                repository.updateFontMode(FontMode.SYSTEM)
                repository.setActiveCustomFont(null)
            }
            FontMode.CUSTOM -> {
                if (customFontFileName != null) {
                    repository.setActiveCustomFont(customFontFileName)
                }
            }
        }
    }
}
