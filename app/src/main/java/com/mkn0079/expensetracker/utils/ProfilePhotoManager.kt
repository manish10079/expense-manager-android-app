package com.mkn0079.expensetracker.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID

/**
 * Utility for managing profile photo storage and localization.
 * Ensures that network images (Google) and gallery picks are cached locally
 * for instant, offline-ready UI rendering.
 */
object ProfilePhotoManager {

    private const val PHOTO_DIR = "profile_photos"

    /**
     * Localizes a photo from any source (URL, Content URI, or local file)
     * into the app's internal private storage.
     *
     * @return The local file URI string if successful, or null.
     */
    suspend fun localizePhoto(context: Context, sourceUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val directory = File(context.filesDir, PHOTO_DIR).apply {
                    if (!exists()) mkdirs()
                }
                
                val targetFile = File(directory, "profile_${UUID.randomUUID()}.jpg")
                
                when (sourceUri.scheme) {
                    "http", "https" -> {
                        // Download from network
                        URL(sourceUri.toString()).openStream().use { input ->
                            targetFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    else -> {
                        // Copy from ContentProvider or File
                        context.contentResolver.openInputStream(sourceUri)?.use { input ->
                            targetFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        } ?: return@withContext null
                    }
                }
                
                Uri.fromFile(targetFile).toString()
            }.getOrNull()
        }
    }

    /**
     * Deletes a managed profile photo from internal storage.
     */
    fun deleteManagedPhoto(uriString: String?) {
        if (uriString == null) return
        
        runCatching {
            val uri = Uri.parse(uriString)
            if (uri.scheme != "file") return
            
            val file = uri.path?.let(::File) ?: return
            if (file.parentFile?.name == PHOTO_DIR && file.exists()) {
                file.delete()
            }
        }
    }
}
