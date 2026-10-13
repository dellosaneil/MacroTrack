package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import org.thelazybattley.macrotrack.features.profile.weighthistory.movingAverage
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.ceil
import kotlin.math.floor

/** Periods up to this many days get full-size weigh-in dots; longer ones shrink them so they don't crowd. */
private const val LARGE_DOT_MAX_DAYS = 31

/** Daily weigh-ins as dots under the moving average line, which carries the area fill and the latest marker. */
@Composable
fun MovingAverageGraph(
    modifier: Modifier = Modifier,
    weightList: List<Weight>,
    movingAverage: List<Weight>,
    period: WeightPeriod,
    contentDescription: String
) {
    if (weightList.isEmpty() || movingAverage.isEmpty()) return
    val textMeasurer = rememberTextMeasurer()
    val axisStyle = typography.regular10.copy(color = colors.muted)
    val gridColor = colors.divider
    val lineColor = colors.accent
    val areaColor = colors.accentSoft
    val dotStroke = colors.muted
    val markerFill = colors.surface

    // The average can sit outside the period's weigh-ins when it draws on days before the period.
    val values = weightList.map { it.weight } + movingAverage.map { it.weight }
    val low = floor((values.min() - 0.2) * 2) / 2
    val high = ceil((values.max() + 0.2) * 2) / 2
    val spanDays = (period.end.toEpochDays() - period.start.toEpochDays()).coerceAtLeast(minimumValue = 1)
    val midDate = period.start.plus(period = DatePeriod(days = (spanDays / 2).toInt()))
    val dotRadiusDp = if (spanDays <= LARGE_DOT_MAX_DAYS) 3.dp else 1.6.dp

    Canvas(modifier = modifier.semantics { this.contentDescription = contentDescription }) {
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

        val averagePoints = pointsFor(
            weightList = movingAverage,
            period = period,
            spanDays = spanDays,
            low = low,
            high = high,
            size = size,
            density = this
        )
        val area = Path().apply {
            moveTo(x = averagePoints.first().x, y = bottom)
            averagePoints.forEach { lineTo(x = it.x, y = it.y) }
            lineTo(x = averagePoints.last().x, y = bottom)
            close()
        }
        drawPath(path = area, color = areaColor)

        val dotRadius = dotRadiusDp.toPx()
        pointsFor(
            weightList = weightList,
            period = period,
            spanDays = spanDays,
            low = low,
            high = high,
            size = size,
            density = this
        ).forEach { point ->
            drawCircle(color = markerFill, radius = dotRadius, center = point)
            drawCircle(color = dotStroke, radius = dotRadius, center = point, style = Stroke(width = 1.2.dp.toPx()))
        }

        val line = Path().apply {
            moveTo(x = averagePoints.first().x, y = averagePoints.first().y)
            averagePoints.drop(n = 1).forEach { lineTo(x = it.x, y = it.y) }
        }
        drawPath(
            path = line,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val latest = averagePoints.last()
        drawCircle(color = markerFill, radius = 5.dp.toPx(), center = latest)
        drawCircle(color = lineColor, radius = 5.dp.toPx(), center = latest, style = Stroke(width = 2.5.dp.toPx()))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMovingAverageGraph() {
    MacroTrackTheme {
        val weights = listOf(
            Weight(date = LocalDate(2026, 10, 1), weight = 74.8),
            Weight(date = LocalDate(2026, 10, 2), weight = 75.1),
            Weight(date = LocalDate(2026, 10, 3), weight = 74.6),
            Weight(date = LocalDate(2026, 10, 5), weight = 74.9),
            Weight(date = LocalDate(2026, 10, 6), weight = 74.4),
            Weight(date = LocalDate(2026, 10, 8), weight = 74.2)
        )
        MovingAverageGraph(
            modifier = Modifier.fillMaxWidth().height(height = 170.dp).padding(all = 16.dp),
            weightList = weights,
            movingAverage = weights.movingAverage(windowDays = 7),
            period = WeightPeriod(
                range = WeightHistoryTimeRangeEnum.MONTH,
                start = LocalDate(2026, 10, 1),
                end = LocalDate(2026, 10, 31)
            ),
            contentDescription = ""
        )
    }
}
