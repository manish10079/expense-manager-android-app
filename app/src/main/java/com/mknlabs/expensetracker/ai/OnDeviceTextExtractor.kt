package com.mknlabs.expensetracker.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnDeviceTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    fun isPlayServicesAvailable(): Boolean {
        val code = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context)
        return code == ConnectionResult.SUCCESS
    }

    suspend fun extract(uri: Uri): DocumentOcrResult = withContext(Dispatchers.IO) {
        if (!isPlayServicesAvailable()) {
            return@withContext DocumentOcrResult.PlayServicesUnavailable
        }
        runCatching {
            val mime = context.contentResolver.getType(uri).orEmpty()
            val text = if (mime.equals("application/pdf", ignoreCase = true) ||
                uri.toString().lowercase().endsWith(".pdf")
            ) {
                extractPdf(uri)
            } else {
                extractImage(uri)
            }
            DocumentOcrResult.Text(text)
        }.getOrElse {
            if (!isPlayServicesAvailable()) {
                DocumentOcrResult.PlayServicesUnavailable
            } else {
                DocumentOcrResult.Text("")
            }
        }
    }

    private suspend fun extractImage(uri: Uri): String {
        val image = InputImage.fromFilePath(context, uri)
        return recognizer.process(image).await().text.trim()
    }

    private suspend fun extractPdf(uri: Uri): String {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return ""
        return PdfRenderer(pfd).use { renderer ->
            val pages = minOf(renderer.pageCount, MAX_PDF_PAGES)
            buildString {
                for (i in 0 until pages) {
                    renderer.openPage(i).use { page ->
                        val scale = minOf(
                            MAX_PAGE_PX.toFloat() / page.width,
                            MAX_PAGE_PX.toFloat() / page.height,
                            1f,
                        )
                        val w = (page.width * scale).toInt().coerceAtLeast(1)
                        val h = (page.height * scale).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        val text = recognizer.process(InputImage.fromBitmap(bitmap, 0))
                            .await()
                            .text
                            .trim()
                        bitmap.recycle()
                        if (text.isNotEmpty()) {
                            if (isNotEmpty()) append("\n")
                            append(text)
                        }
                    }
                }
            }.trim()
        }
    }

    companion object {
        private const val MAX_PDF_PAGES = 4
        private const val MAX_PAGE_PX = 1600
    }
}
