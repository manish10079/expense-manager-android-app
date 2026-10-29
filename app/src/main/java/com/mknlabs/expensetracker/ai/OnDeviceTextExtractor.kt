package com.mknlabs.expensetracker.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.media.ExifInterface
import android.net.Uri
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.mlkit.vision.common.InputImage
import android.util.Log
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class OnDeviceTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val devanagariRecognizer by lazy {
        TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
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
        val loaded = loadBitmap(uri) ?: return ""
        return recognizePrepared(loaded)
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
                        val text = recognizePrepared(bitmap)
                        if (text.isNotEmpty()) {
                            if (isNotEmpty()) append("\n")
                            append(text)
                        }
                    }
                }
            }.trim()
        }
    }

    private suspend fun recognizePrepared(source: Bitmap): String {
        val grey = OcrBitmapPreprocessor.prepare(source)
        try {
            val immutable = if (grey.isMutable) {
                grey.copy(Bitmap.Config.ARGB_8888, false) ?: grey
            } else {
                grey
            }
            val text = devanagariRecognizer
                .process(InputImage.fromBitmap(immutable, 0))
                .await()
                .text
                .trim()
            if (immutable !== grey && !immutable.isRecycled) immutable.recycle()
            Log.i(TAG, "RAW OCR:\n$text")
            return text
        } finally {
            if (grey !== source && !grey.isRecycled) grey.recycle()
            if (!source.isRecycled) source.recycle()
        }
    }

    private fun loadBitmap(uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        val longest = max(bounds.outWidth, bounds.outHeight).coerceAtLeast(1)
        val sample = max(1, longest / MAX_PAGE_PX)
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return null
        return applyExifRotation(uri, decoded)
    }

    private fun applyExifRotation(uri: Uri, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated !== bitmap) bitmap.recycle()
        return rotated
    }

    companion object {
        private const val TAG = "OnDeviceTextExtractor"
        private const val MAX_PDF_PAGES = 4
        private const val MAX_PAGE_PX = 1600
    }
}
