package com.mknlabs.expensetracker.ai

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.roundToInt

/**
 * Prepares a bitmap for ML Kit. Greyscale + contrast stretch only —
 * no Otsu / hard binarization (the model wants smooth gradients).
 */
object OcrBitmapPreprocessor {

    private const val UpscaleIfMinSideBelow = 960
    private const val MildUpscaleIfMinSideBelow = 1280
    private const val MaxLongestAfterScale = 2400

    fun prepare(source: Bitmap): Bitmap {
        val grey = toBt709Greyscale(source)
        stretchContrastInPlace(grey)
        return upscaleIfSmall(grey)
    }

    fun toBt709Greyscale(source: Bitmap): Bitmap {
        val w = source.width
        val h = source.height
        if (w <= 0 || h <= 0) return source

        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in pixels.indices) {
            val p = pixels[i]
            val y = (
                0.2126f * Color.red(p) +
                    0.7152f * Color.green(p) +
                    0.0722f * Color.blue(p)
                ).roundToInt().coerceIn(0, 255)
            pixels[i] = Color.rgb(y, y, y)
        }
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, w, 0, 0, w, h)
        return result
    }

    /** 1st–99th percentile linear stretch so thin ₹ bars stay as grey, not clipped. */
    private fun stretchContrastInPlace(bitmap: Bitmap) {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val hist = IntArray(256)
        val step = (pixels.size / 50_000).coerceAtLeast(1)
        var samples = 0
        var i = 0
        while (i < pixels.size) {
            hist[Color.red(pixels[i])]++
            samples++
            i += step
        }
        if (samples == 0) return
        val loCut = (samples * 0.01f).toInt()
        val hiCut = (samples * 0.99f).toInt().coerceAtLeast(loCut + 1)
        var lo = 0
        var hi = 255
        var acc = 0
        for (bin in 0..255) {
            acc += hist[bin]
            if (acc >= loCut) {
                lo = bin
                break
            }
        }
        acc = 0
        for (bin in 0..255) {
            acc += hist[bin]
            if (acc >= hiCut) {
                hi = bin
                break
            }
        }
        if (hi <= lo) return
        val span = (hi - lo).toFloat()
        for (idx in pixels.indices) {
            val y = ((Color.red(pixels[idx]) - lo) * 255f / span).roundToInt().coerceIn(0, 255)
            pixels[idx] = Color.rgb(y, y, y)
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
    }

    private fun upscaleIfSmall(source: Bitmap): Bitmap {
        val minSide = minOf(source.width, source.height)
        val scale = when {
            minSide < UpscaleIfMinSideBelow -> 2.0f
            minSide < MildUpscaleIfMinSideBelow -> 1.5f
            else -> return source
        }
        var w = (source.width * scale).roundToInt().coerceAtLeast(1)
        var h = (source.height * scale).roundToInt().coerceAtLeast(1)
        val longest = maxOf(w, h)
        if (longest > MaxLongestAfterScale) {
            val shrink = MaxLongestAfterScale.toFloat() / longest
            w = (w * shrink).roundToInt().coerceAtLeast(1)
            h = (h * shrink).roundToInt().coerceAtLeast(1)
        }
        if (w == source.width && h == source.height) return source
        val scaled = Bitmap.createScaledBitmap(source, w, h, true)
        if (scaled !== source && !source.isRecycled) source.recycle()
        return scaled
    }
}
