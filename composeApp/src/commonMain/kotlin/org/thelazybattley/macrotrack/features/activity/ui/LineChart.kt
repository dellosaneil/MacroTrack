package org.thelazybattley.macrotrack.features.activity.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import kotlin.math.abs

/**
 * A line through [values] at [positions] (0 to 1 across), with its highest and lowest values marked on the left.
 * [lowerIsUp] puts small values at the top, for pace, where lower is faster. Tapping picks the nearest point.
 */
@Composable
internal fun LineChart(
    modifier: Modifier = Modifier,
    values: List<Double>,
    positions: List<Float>,
    description: String,
    label: (Double) -> String,
    selected: Int?,
    onSelect: (Int) -> Unit,
    lowerIsUp: Boolean = false,
    fillArea: Boolean = false,
    showDots: Boolean = false,
    gridLines: Int = 2
) {
    val line = colors.accent
    val area = colors.hero
    val grid = colors.line
    val ink = colors.ink
    val muted = colors.muted
    val surface = colors.surface
    val textMeasurer = rememberTextMeasurer()
    val latestPositions by rememberUpdatedState(newValue = positions)
    val latestOnSelect by rememberUpdatedState(newValue = onSelect)
    Canvas(
        modifier = modifier
            .semantics { contentDescription = description }
            .pointerInput(key1 = Unit) {
                detectTapGestures { tap ->
                    val left = LABEL_WIDTH.toPx()
                    val width = size.width - left
                    if (width <= 0 || latestPositions.isEmpty()) return@detectTapGestures
                    val fraction = (tap.x - left) / width
                    val nearest = latestPositions.indices.minByOrNull { abs(latestPositions[it] - fraction) } ?: return@detectTapGestures
                    latestOnSelect(nearest)
                }
            }
    ) {
        if (values.isEmpty()) return@Canvas
        val left = LABEL_WIDTH.toPx()
        val width = size.width - left
        val top = 8.dp.toPx()
        val height = size.height - 2 * top
        val low = values.min()
        val high = values.max()
        val pad = ((high - low) * 0.15).takeIf { it > 0 } ?: 1.0
        val min = low - pad
        val max = high + pad
        fun y(value: Double): Float {
            val share = ((value - min) / (max - min)).toFloat()
            return top + height * if (lowerIsUp) share else 1 - share
        }
        fun x(index: Int): Float = left + width * positions[index]
        val labelStyle = TextStyle(fontSize = 10.sp, color = muted)
        val marks = if (gridLines <= 2) listOf(high, low) else List(size = gridLines) { low + (high - low) * it / (gridLines - 1) }
        marks.forEach { value ->
            val gy = y(value = value)
            drawLine(color = grid, start = Offset(x = left, y = gy), end = Offset(x = size.width, y = gy), strokeWidth = 1.dp.toPx())
            val text = textMeasurer.measure(text = label(value), style = labelStyle)
            drawText(textLayoutResult = text, topLeft = Offset(x = left - text.size.width - 4.dp.toPx(), y = gy - text.size.height / 2f))
        }
        val path = Path().apply {
            values.indices.forEach { index ->
                if (index == 0) moveTo(x = x(index), y = y(value = values[index])) else lineTo(x = x(index), y = y(value = values[index]))
            }
        }
        if (fillArea && values.size > 1) {
            val fill = Path().apply {
                addPath(path = path)
                lineTo(x = x(values.lastIndex), y = size.height)
                lineTo(x = x(0), y = size.height)
                close()
            }
            drawPath(path = fill, color = area)
        }
        drawPath(path = path, color = line, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        if (showDots) {
            values.indices.forEach { index ->
                val isSelected = index == selected
                val center = Offset(x = x(index), y = y(value = values[index]))
                drawCircle(color = surface, radius = (if (isSelected) 8 else 6).dp.toPx(), center = center)
                drawCircle(color = if (isSelected) ink else line, radius = (if (isSelected) 6 else 4).dp.toPx(), center = center)
            }
        }
        selected?.takeIf { it in values.indices }?.let { index ->
            if (!showDots) {
                drawLine(color = ink, start = Offset(x = x(index), y = 0f), end = Offset(x = x(index), y = size.height), strokeWidth = 1.5.dp.toPx())
            }
        }
    }
}

private val LABEL_WIDTH = 34.dp
