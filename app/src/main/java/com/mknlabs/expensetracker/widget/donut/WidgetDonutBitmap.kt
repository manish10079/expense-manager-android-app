package com.mknlabs.expensetracker.widget.donut

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.mknlabs.expensetracker.widget.model.WidgetBreakdownSlice

internal object WidgetDonutBitmap {
    fun draw(
        slices: List<WidgetBreakdownSlice>,
        sizePx: Int,
        trackColor: Int
    ): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val ring = sizePx * 0.20f
        val outerInset = 2f
        val innerInset = outerInset + ring
        val outer = RectF(outerInset, outerInset, sizePx - outerInset, sizePx - outerInset)
        val inner = RectF(innerInset, innerInset, sizePx - innerInset, sizePx - innerInset)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        val track = Path()
        track.addOval(outer, Path.Direction.CW)
        track.addOval(inner, Path.Direction.CCW)
        fill.color = trackColor
        canvas.drawPath(track, fill)

        val usable = slices.filter { it.percent > 0 }.take(4)
        val total = usable.sumOf { it.percent }.coerceAtLeast(1)
        val gap = 12f
        var start = -90f
        usable.forEach { slice ->
            val sweep = (slice.percent / total.toFloat()) * (360f - gap * usable.size)
            val sliceSweep = sweep.coerceAtLeast(4f)
            val path = Path()
            path.arcTo(outer, start, sliceSweep, false)
            path.arcTo(inner, start + sliceSweep, -sliceSweep, false)
            path.close()
            fill.color = slice.colorArgb
            canvas.drawPath(path, fill)
            start += sliceSweep + gap
        }
        return bmp
    }

    fun dot(colorArgb: Int, sizePx: Int): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = colorArgb
        }
        val r = sizePx / 2f
        canvas.drawCircle(r, r, r - 0.5f, paint)
        return bmp
    }
}
