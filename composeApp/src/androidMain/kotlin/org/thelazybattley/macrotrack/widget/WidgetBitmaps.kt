package org.thelazybattley.macrotrack.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import kotlin.math.roundToInt

/**
 * Shapes widgets can't draw themselves. Each is white so the widget tints it with a palette colour, which keeps
 * the light and dark values and the wallpaper colours working.
 */
internal object WidgetBitmaps {

    /** A ring [sizeDp] across, from the top going clockwise for [fraction] of the way round; 1 draws the whole ring. */
    fun ring(context: Context, sizeDp: Int, strokeDp: Float, fraction: Float): Bitmap {
        val density = context.resources.displayMetrics.density
        val size = (sizeDp * density).roundToInt()
        val stroke = strokeDp * density
        val bitmap = createBitmap(width = size, height = size)
        val sweep = 360f * fraction.coerceIn(0f, 1f)
        if (sweep <= 0f) return bitmap
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = if (sweep >= 360f) Paint.Cap.BUTT else Paint.Cap.ROUND
            color = android.graphics.Color.WHITE
        }
        val inset = stroke / 2
        Canvas(bitmap).drawArc(RectF(inset, inset, size - inset, size - inset), -90f, sweep, false, paint)
        return bitmap
    }

    /** The weight trend as a line, and the area under it, scaled to fill [widthDp] × [heightDp]. */
    fun sparkline(context: Context, values: List<Double>, widthDp: Int, heightDp: Int): Pair<Bitmap, Bitmap> {
        val density = context.resources.displayMetrics.density
        val width = (widthDp * density).roundToInt()
        val height = (heightDp * density).roundToInt()
        val line = createBitmap(width = width, height = height)
        val area = createBitmap(width = width, height = height)
        if (values.size < 2) return line to area
        val stroke = 2.5f * density
        val min = values.min()
        val range = (values.max() - min).takeIf { it > 0 } ?: 1.0
        val points = values.mapIndexed { index, value ->
            val x = stroke + index * (width - 2 * stroke) / (values.size - 1)
            val y = stroke + ((1 - (value - min) / range) * (height - 2 * stroke)).toFloat()
            x to y
        }
        val path = Path().apply {
            moveTo(points.first().first, points.first().second)
            points.drop(n = 1).forEach { (x, y) -> lineTo(x, y) }
        }
        val white = android.graphics.Color.WHITE
        Canvas(line).drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = white
        })
        val fill = Path(path).apply {
            lineTo(points.last().first, height.toFloat())
            lineTo(points.first().first, height.toFloat())
            close()
        }
        Canvas(area).drawPath(fill, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = white })
        return line to area
    }
}
