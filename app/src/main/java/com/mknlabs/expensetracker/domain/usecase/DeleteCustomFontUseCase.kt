package com.mknlabs.expensetracker.domain.usecase

import android.content.Context
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.utils.FontFileHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeleteCustomFontUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AppPreferencesRepository
) {
    /**
     * Delete a custom font file and remove it from the user's font library.
     * If it was the active font, reverts to APP font.
     */
    suspend operator fun invoke(fileName: String) {
        // Delete the file from internal storage
        FontFileHelper.deleteFont(context, fileName)
        // Remove from library (also handles active font revert if needed)
        repository.removeImportedFont(fileName)
    }
}
