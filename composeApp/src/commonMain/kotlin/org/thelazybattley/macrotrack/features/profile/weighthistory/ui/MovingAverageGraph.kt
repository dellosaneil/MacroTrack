package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
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
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.pastgoal.ui.tones
import org.thelazybattley.macrotrack.features.profile.weighthistory.GoalPhase
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightPeriod
import org.thelazybattley.macrotrack.features.profile.weighthistory.movingAverage
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/** Periods up to this many days get full-size weigh-in dots; longer ones shrink them so they don't crowd. */
private const val LARGE_DOT_MAX_DAYS = 31

private const val BAND_ALPHA = 0.75f
private const val SELECTION_ALPHA = 0.12f

/** Two weigh-in dates picked on the chart; [anchor] is where the long-press started, so it can sit after [end]. */
data class AverageRange(val anchor: LocalDate, val end: LocalDate) {
    val first: LocalDate get() = minOf(anchor, end)
    val last: LocalDate get() = maxOf(anchor, end)
}

/**
 * Daily weigh-ins as dots under the moving average line, over bands for each goal the user had.
 * Long-press then drag picks a [selection] between two weigh-ins.
 */
@Composable
fun MovingAverageGraph(
    modifier: Modifier = Modifier,
    weightList: List<Weight>,
    movingAverage: List<Weight>,
    period: WeightPeriod,
    goalPhases: List<GoalPhase>,
    selection: AverageRange?,
    onSelectionStart: (LocalDate) -> Unit,
    onSelectionDrag: (LocalDate) -> Unit,
    onSelectionEnd: () -> Unit,
    contentDescription: String
) {
    if (weightList.isEmpty() || movingAverage.isEmpty()) return
    val textMeasurer = rememberTextMeasurer()
    val haptics = LocalHapticFeedback.current
    val currentOnStart by rememberUpdatedState(newValue = onSelectionStart)
    val currentOnDrag by rememberUpdatedState(newValue = onSelectionDrag)
    val currentOnEnd by rememberUpdatedState(newValue = onSelectionEnd)
    val axisStyle = typography.regular10.copy(color = colors.muted)
    val gridColor = colors.divider
    val lineColor = colors.accent
    val selectionLineColor = colors.accentInk
    val changeMarkColor = colors.ink.copy(alpha = 0.45f)
    val dotStroke = colors.muted
    val markerFill = colors.surface

    // The average can sit outside the period's weigh-ins when it draws on days before the period.
    val values = weightList.map { it.weight } + movingAverage.map { it.weight }
    val low = floor((values.min() - 0.2) * 2) / 2
    val high = ceil((values.max() + 0.2) * 2) / 2
    val spanDays = (period.end.toEpochDays() - period.start.toEpochDays()).coerceAtLeast(minimumValue = 1)
    val midDate = period.start.plus(period = DatePeriod(days = (spanDays / 2).toInt()))
    val dotRadiusDp = if (spanDays <= LARGE_DOT_MAX_DAYS) 3.dp else 1.6.dp

    val visiblePhases = goalPhases.filter { it.end >= period.start && it.start <= period.end }
    val phaseTones = Goal.entries.associateWith { it.tones() }

    Canvas(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .pointerInput(movingAverage, period, low, high) {
                fun dateAt(x: Float): LocalDate {
                    val points = pointsFor(
                        weightList = movingAverage,
                        period = period,
                        spanDays = spanDays,
                        low = low,
                        high = high,
                        size = Size(width = size.width.toFloat(), height = size.height.toFloat()),
                        density = this
                    )
                    return movingAverage[points.indices.minBy { abs(points[it].x - x) }].date
                }
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        haptics.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                        currentOnStart(dateAt(x = offset.x))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentOnDrag(dateAt(x = change.position.x))
                    },
                    onDragEnd = { currentOnEnd() },
                    onDragCancel = { currentOnEnd() }
                )
            }
    ) {
        val left = AXIS_LABEL_WIDTH_DP.dp.toPx()
        val top = TOP_PADDING_DP.dp.toPx()
        val bottom = size.height - X_LABEL_HEIGHT_DP.dp.toPx()
        val inset = 6.dp.toPx()
        val dayWidth = (size.width - left - inset * 2) / spanDays.toFloat()
        fun xFor(date: LocalDate): Float = left + inset + dayWidth * (date.toEpochDays() - period.start.toEpochDays())

        visiblePhases.forEach { phase ->
            val x0 = max(left, xFor(date = maxOf(phase.start, period.start)) - dayWidth / 2f)
            val x1 = min(size.width, xFor(date = minOf(phase.end, period.end)) + dayWidth / 2f)
            drawRect(
                color = phaseTones.getValue(phase.goal).first.copy(alpha = BAND_ALPHA),
                topLeft = Offset(x = x0, y = 0f),
                size = Size(width = x1 - x0, height = bottom)
            )
        }

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

        visiblePhases.filter { it.start > period.start }.forEach { phase ->
            val x = max(left, xFor(date = phase.start) - dayWidth / 2f)
            drawLine(
                color = changeMarkColor,
                start = Offset(x = x, y = 0f),
                end = Offset(x = x, y = bottom),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
            )
        }

        if (selection != null) {
            val xa = xFor(date = selection.first)
            val xb = xFor(date = selection.last)
            drawRect(
                color = lineColor.copy(alpha = SELECTION_ALPHA),
                topLeft = Offset(x = xa, y = 0f),
                size = Size(width = xb - xa, height = bottom)
            )
            listOf(xa, xb).forEach { x ->
                drawLine(
                    color = selectionLineColor,
                    start = Offset(x = x, y = 0f),
                    end = Offset(x = x, y = bottom),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
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

        if (selection != null) {
            listOf(selection.first, selection.last).forEach { date ->
                val index = movingAverage.indexOfFirst { it.date == date }
                if (index < 0) return@forEach
                drawCircle(color = lineColor, radius = 5.5.dp.toPx(), center = averagePoints[index])
                drawCircle(
                    color = markerFill,
                    radius = 5.5.dp.toPx(),
                    center = averagePoints[index],
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
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
            goalPhases = listOf(
                GoalPhase(start = LocalDate(2026, 3, 2), end = LocalDate(2026, 10, 4), goal = Goal.MAINTAIN_WEIGHT),
                GoalPhase(start = LocalDate(2026, 10, 5), end = LocalDate(2026, 10, 8), goal = Goal.LOSE_WEIGHT)
            ),
            selection = AverageRange(anchor = LocalDate(2026, 10, 2), end = LocalDate(2026, 10, 6)),
            onSelectionStart = {},
            onSelectionDrag = {},
            onSelectionEnd = {},
            contentDescription = ""
        )
    }
}
