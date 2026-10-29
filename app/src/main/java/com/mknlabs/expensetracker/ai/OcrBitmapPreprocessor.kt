package com.mknlabs.expensetracker.ai

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.roundToInt

/** ITU-R BT.709 greyscale before Latin OCR. */
object OcrBitmapPreprocessor {

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
}
