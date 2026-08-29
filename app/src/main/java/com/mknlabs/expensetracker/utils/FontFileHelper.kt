package com.mknlabs.expensetracker.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Manages custom font files in internal storage.
 *
 * Fonts are stored in `filesDir/custom_fonts/` and survive app restarts.
 * The content URI permission is NOT required after import — the file is fully copied.
 */
object FontFileHelper {

    private const val TAG = "FontFileHelper"
    private const val FONTS_DIR = "custom_fonts"
    const val MAX_CUSTOM_FONTS = 5

    private fun getFontsDir(context: Context): File {
        return File(context.filesDir, FONTS_DIR).also { it.mkdirs() }
    }

    /**
     * Copy a font file from a content URI to internal storage.
     *
     * @return Result with the sanitized filename on success, or an exception on failure.
     */
    suspend fun importFont(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver

            // Get original filename from URI
            val originalName = getFileName(context, uri) ?: "font_${System.currentTimeMillis()}.ttf"
            val sanitizedName = sanitizeFileName(originalName)
            val targetFile = File(getFontsDir(context), sanitizedName)

            // Copy file
            contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Cannot open font file"))

            // Validate the font file
            val font = try {
                Font(file = targetFile, weight = FontWeight.Normal)
            } catch (e: Exception) {
                targetFile.delete()
                return@withContext Result.failure(Exception("Invalid font file: ${e.message}"))
            }

            Log.d(TAG, "Font imported: $sanitizedName (${targetFile.length()} bytes)")
            Result.success(sanitizedName)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import font", e)
            Result.failure(e)
        }
    }

    /**
     * Load a FontFamily from an internal storage filename.
     * Returns null if the file is missing or corrupt.
     */
    fun loadFontFamily(context: Context, fileName: String): FontFamily? {
        return try {
            val file = File(getFontsDir(context), fileName)
            if (!file.exists()) {
                Log.w(TAG, "Font file not found: $fileName")
                return null
            }

            FontFamily(
                Font(file = file, weight = FontWeight.Normal),
                Font(file = file, weight = FontWeight.Light),
                Font(file = file, weight = FontWeight.Medium),
                Font(file = file, weight = FontWeight.Bold)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load font: $fileName", e)
            null
        }
    }

    /**
     * Delete a font file from internal storage.
     */
    fun deleteFont(context: Context, fileName: String): Boolean {
        val file = File(getFontsDir(context), fileName)
        val deleted = file.delete()
        if (deleted) {
            Log.d(TAG, "Font deleted: $fileName")
        } else {
            Log.w(TAG, "Font file not found for deletion: $fileName")
        }
        return deleted
    }

    /**
     * Get a display-friendly name from a font filename.
     * e.g. "1693456789_MyHandwriting.ttf" → "MyHandwriting"
     */
    fun fontDisplayName(fileName: String): String {
        return fileName
            .replace(Regex("^\\d+_"), "") // remove timestamp prefix
            .replace(Regex("\\.(ttf|otf)$", RegexOption.IGNORE_CASE), "") // remove extension
    }

    /**
     * Check if the user can import more fonts.
     */
    fun canImport(importedCount: Int): Boolean = importedCount < MAX_CUSTOM_FONTS

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex)
                }
            }
        }
        if (name == null) {
            name = uri.path?.substringAfterLast('/')
        }
        return name
    }

    private fun sanitizeFileName(name: String): String {
        // Remove special characters, keep alphanumeric, dots, hyphens, underscores
        val sanitized = name.replace(Regex("[^a-zA-Z0-9.\\-_]"), "_")
        // Prefix with timestamp to avoid collisions
        return "${System.currentTimeMillis()}_$sanitized"
    }
}
