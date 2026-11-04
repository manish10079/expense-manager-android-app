package com.mknlabs.expensetracker.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
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
        val local = copyUriOnce(uri) ?: return@withContext DocumentOcrResult.Text("")
        try {
            val mime = context.contentResolver.getType(uri).orEmpty()
            val isPdf = mime.equals("application/pdf", ignoreCase = true) ||
                uri.toString().lowercase().endsWith(".pdf") ||
                local.name.lowercase().endsWith(".pdf")
            val text = if (isPdf) extractPdf(local) else extractImage(local)
            DocumentOcrResult.Text(text)
        } catch (t: Throwable) {
            Log.w(TAG, "OCR failed for $uri", t)
            if (!isPlayServicesAvailable()) {
                DocumentOcrResult.PlayServicesUnavailable
            } else {
                DocumentOcrResult.Text("")
            }
        } finally {
            local.delete()
            ocrCacheDir().delete()
        }
    }

    private fun ocrCacheDir(): File = File(context.cacheDir, OCR_CACHE_DIR)

    private fun copyUriOnce(uri: Uri): File? {
        val dir = ocrCacheDir()
        if (!dir.exists() && !dir.mkdirs()) {
            Log.w(TAG, "Could not create OCR cache dir")
            return null
        }
        dir.listFiles()?.forEach { it.delete() }
        val dest = File(dir, "in_${System.nanoTime()}")
        val ok = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().buffered().use { output -> input.copyTo(output) }
            }
        }.onFailure { Log.w(TAG, "Copy failed for $uri", it) }.getOrNull()
        if (ok == null || !dest.isFile || dest.length() == 0L) {
            dest.delete()
            dir.delete()
            return null
        }
        return dest
    }

    private suspend fun extractImage(file: File): String {
        val loaded = loadBitmap(file) ?: return ""
        return recognizePrepared(loaded)
    }

    private suspend fun extractPdf(file: File): String {
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
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

    private fun loadBitmap(file: File): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        val longest = max(bounds.outWidth, bounds.outHeight)
        val decoded = if (longest > 0) {
            val sample = max(1, longest / MAX_PAGE_PX)
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(file.absolutePath, opts)
        } else {
            null
        } ?: decodeWithImageDecoder(file) ?: return null
        return applyExifRotation(file, decoded)
    }

    private fun decodeWithImageDecoder(file: File): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return runCatching {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val longest = max(info.size.width, info.size.height).coerceAtLeast(1)
                if (longest > MAX_PAGE_PX) {
                    val scale = MAX_PAGE_PX.toFloat() / longest
                    decoder.setTargetSize(
                        (info.size.width * scale).toInt().coerceAtLeast(1),
                        (info.size.height * scale).toInt().coerceAtLeast(1),
                    )
                }
            }
        }.onFailure { Log.w(TAG, "ImageDecoder failed", it) }.getOrNull()
    }

    private fun applyExifRotation(file: File, bitmap: Bitmap): Bitmap {
        val orientation = runCatching {
            ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
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
        private const val OCR_CACHE_DIR = "ocr_in"
        private const val MAX_PDF_PAGES = 4
        private const val MAX_PAGE_PX = 1600
    }
}
