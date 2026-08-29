package com.mknlabs.expensetracker.domain.usecase

import android.content.Context
import android.net.Uri
import com.mknlabs.expensetracker.domain.repository.AppPreferencesRepository
import com.mknlabs.expensetracker.utils.FontFileHelper
import kotlinx.coroutines.flow.first
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ImportCustomFontUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AppPreferencesRepository
) {
    /**
     * Import a font file from a URI and add it to the user's font library.
     *
     * @return Result with the display name on success.
     */
    suspend operator fun invoke(uri: Uri): Result<String> {
        // Check max limit
        val currentFonts = repository.observeAppSettings().first().importedFontFileNames
        if (!FontFileHelper.canImport(currentFonts.size)) {
            return Result.failure(Exception("Maximum ${FontFileHelper.MAX_CUSTOM_FONTS} custom fonts allowed"))
        }

        // Import the file
        val importResult = FontFileHelper.importFont(context, uri)
        return importResult.fold(
            onSuccess = { fileName ->
                // Atomic single DataStore write: add to library + set as active
                repository.addAndActivateFont(fileName)
                Result.success(FontFileHelper.fontDisplayName(fileName))
            },
            onFailure = { e ->
                Result.failure(e)
            }
        )
    }
}
