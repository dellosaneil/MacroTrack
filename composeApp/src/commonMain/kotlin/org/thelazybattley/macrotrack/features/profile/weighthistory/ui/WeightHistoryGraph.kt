package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightPeriod
import org.thelazybattley.macrotrack.features.profile.ui.formatKg
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

private const val AXIS_LABEL_WIDTH_DP = 36
private const val X_LABEL_HEIGHT_DP = 20
private const val TOP_PADDING_DP = 8
private const val TAP_RADIUS_DP = 24

@Composable
fun WeightHistoryGraph(
    modifier: Modifier = Modifier,
    weightList: List<Weight>,
    period: WeightPeriod
) {
    if (weightList.isEmpty()) return
    val textMeasurer = rememberTextMeasurer()
    val axisStyle = typography.regular10.copy(color = colors.muted)
    val tooltipStyle = typography.bold11.copy(color = colors.onAccent)
    val gridColor = colors.divider
    val lineColor = colors.accent
    val areaColor = colors.accentSoft
    val markerFill = colors.surface
    var selectedIndex by remember(key1 = weightList) { mutableIntStateOf(value = -1) }

    val weights = weightList.map { it.weight }
    val low = floor((weights.min() - 0.2) * 2) / 2
    val high = ceil((weights.max() + 0.2) * 2) / 2
    val spanDays = (period.end.toEpochDays() - period.start.toEpochDays()).coerceAtLeast(minimumValue = 1)
    val midDate = period.start.plus(period = DatePeriod(days = (spanDays / 2).toInt()))

    Canvas(
        modifier = modifier.pointerInput(key1 = weightList, key2 = period) {
            detectTapGestures { tap ->
                val points = pointsFor(
                    weightList = weightList,
                    period = period,
                    spanDays = spanDays,
                    low = low,
                    high = high,
                    size = Size(width = size.width.toFloat(), height = size.height.toFloat()),
                    density = this
                )
                val nearest = points.withIndex().minByOrNull { abs(it.value.x - tap.x) }
                selectedIndex = if (nearest != null && abs(nearest.value.x - tap.x) <= TAP_RADIUS_DP.dp.toPx()) {
                    if (nearest.index == selectedIndex) -1 else nearest.index
                } else {
                    -1
                }
            }
        }
    ) {
        val left = AXIS_LABEL_WIDTH_DP.dp.toPx()
        val top = TOP_PADDING_DP.dp.toPx()
        val bottom = size.height - X_LABEL_HEIGHT_DP.dp.toPx()

        listOf(high, (high + low) / 2, low).forEachIndexed { index, value ->
            val y = top + (bottom - top) * index / 2f
            drawLine(color = gridColor, start = Offset(x = left, y = y), end = Offset(x = size.width, y = y))
            val label = textMeasurer.measure(text = value.formatAxis(), style = axisStyle)
            drawText(textLayoutResult = label, topLeft = Offset(x = 0f, y = y - label.size.height / 2f))
        }

        listOf(period.start, midDate, period.end).forEachIndexed { index, date ->
            val label = textMeasurer.measure(text = date.toAbbreviatedMonthDay(), style = axisStyle)
            val x = when (index) {
                0 -> left
                1 -> left + (size.width - left) / 2f - label.size.width / 2f
                else -> size.width - label.size.width
            }
            drawText(textLayoutResult = label, topLeft = Offset(x = x, y = bottom + 4.dp.toPx()))
        }

        val points = pointsFor(
            weightList = weightList,
            period = period,
            spanDays = spanDays,
            low = low,
            high = high,
            size = size,
            density = this
        )

        val area = Path().apply {
            moveTo(x = points.first().x, y = bottom)
            points.forEach { lineTo(x = it.x, y = it.y) }
            lineTo(x = points.last().x, y = bottom)
            close()
        }
        drawPath(path = area, color = areaColor)
        val line = Path().apply {
            moveTo(x = points.first().x, y = points.first().y)
            points.drop(n = 1).forEach { lineTo(x = it.x, y = it.y) }
        }
        drawPath(
            path = line,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val marked = if (selectedIndex in points.indices) selectedIndex else points.lastIndex
        drawCircle(color = markerFill, radius = 5.dp.toPx(), center = points[marked])
        drawCircle(color = lineColor, radius = 5.dp.toPx(), center = points[marked], style = Stroke(width = 2.5.dp.toPx()))

        if (selectedIndex in points.indices) {
            val weight = weightList[selectedIndex]
            val tooltip = textMeasurer.measure(
                text = "${weight.weight.formatKg()} kg · ${weight.date.toAbbreviatedMonthDay()}",
                style = tooltipStyle
            )
            val padding = 6.dp.toPx()
            val boxWidth = tooltip.size.width + padding * 2
            val boxHeight = tooltip.size.height + padding * 2
            val point = points[selectedIndex]
            val boxLeft = (point.x - boxWidth / 2f).coerceIn(left, size.width - boxWidth)
            val boxTop = (point.y - boxHeight - 10.dp.toPx()).coerceAtLeast(minimumValue = 0f)
            drawRoundRect(
                color = lineColor,
                topLeft = Offset(x = boxLeft, y = boxTop),
                size = Size(width = boxWidth, height = boxHeight),
                cornerRadius = CornerRadius(x = 8.dp.toPx(), y = 8.dp.toPx())
            )
            drawText(textLayoutResult = tooltip, topLeft = Offset(x = boxLeft + padding, y = boxTop + padding))
        }
    }
}

private fun pointsFor(
    weightList: List<Weight>,
    period: WeightPeriod,
    spanDays: Long,
    low: Double,
    high: Double,
    size: Size,
    density: androidx.compose.ui.unit.Density
): List<Offset> = with(density) {
    val left = AXIS_LABEL_WIDTH_DP.dp.toPx()
    val top = TOP_PADDING_DP.dp.toPx()
    val bottom = size.height - X_LABEL_HEIGHT_DP.dp.toPx()
    val inset = 6.dp.toPx()
    weightList.map { weight ->
        val dayIndex = weight.date.toEpochDays() - period.start.toEpochDays()
        val x = left + inset + (size.width - left - inset * 2) * dayIndex / spanDays.toFloat()
        val y = bottom - (bottom - top) * ((weight.weight - low) / (high - low)).toFloat()
        Offset(x = x, y = y)
    }
}

private fun Double.formatAxis(): String {
    val tenths = kotlin.math.round(this * 10).toInt()
    return "${tenths / 10}.${abs(tenths % 10)}"
}

@Preview(showBackground = true)
@Composable
private fun PreviewWeightHistoryGraph() {
    MacroTrackTheme {
        WeightHistoryGraph(
            modifier = Modifier.fillMaxWidth().height(height = 170.dp).padding(all = 16.dp),
            weightList = listOf(
                Weight(date = LocalDate(2026, 10, 1), weight = 74.8),
                Weight(date = LocalDate(2026, 10, 3), weight = 74.6),
                Weight(date = LocalDate(2026, 10, 5), weight = 74.5),
                Weight(date = LocalDate(2026, 10, 8), weight = 74.2)
            ),
            period = WeightPeriod(
                range = WeightHistoryTimeRangeEnum.MONTH,
                start = LocalDate(2026, 10, 1),
                end = LocalDate(2026, 10, 31)
            )
        )
    }
}
