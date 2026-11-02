package org.thelazybattley.macrotrack.features.forecast.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.forecast_chart_goal
import macrotrack.composeapp.generated.resources.forecast_chart_today
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.FORECAST_RANGE_SPREAD
import org.thelazybattley.macrotrack.domain.model.ForecastStatus
import org.thelazybattley.macrotrack.domain.model.GoalForecast
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** The chart looks at most this far past today. */
private const val MAX_AHEAD_DAYS = 140
private const val MIN_AHEAD_DAYS = 30
private const val BAND_STEP_DAYS = 4
private val niceSteps = listOf(0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 50.0)

/** The days the chart spans, as epoch days: from the start to a little past the forecast date. */
internal fun GoalForecast.chartDays(): LongRange {
    val status = status as? ForecastStatus.OnTheWay
    val todayDay = today.toEpochDays()
    val endDay = if (status != null) {
        min(todayDay + MAX_AHEAD_DAYS, max(status.date.toEpochDays(), todayDay + MIN_AHEAD_DAYS) + 10)
    } else {
        todayDay + 7
    }
    return startDate.toEpochDays()..max(endDay, startDate.toEpochDays() + 1)
}

/** Where [day] sits across the chart's plot, from 0 at the left axis to 1 at the right edge. */
internal fun GoalForecast.chartFraction(day: Long): Float {
    val days = chartDays()
    return ((day - days.first).toFloat() / (days.last - days.first)).coerceIn(0f, 1f)
}

internal val CHART_LEFT = 30.dp
internal val CHART_RIGHT = 4.dp

/**
 * Weigh-ins as faint dots under the trend line, then, while on the way, a dotted projection to the goal, inside the
 * likely range when [showBand]. The goal is a dashed line. Tapping or dragging picks a day up to [lastSelectable]:
 * [selected] gets a line and a dot.
 */
@Composable
internal fun ForecastChart(
    modifier: Modifier = Modifier,
    forecast: GoalForecast,
    showBand: Boolean,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
    contentDescription: String
) {
    val unitSystem = LocalUnitSystem.current
    val textMeasurer = rememberTextMeasurer()
    val axisStyle = typography.regular10.copy(color = colors.faint)
    val monthStyle = typography.regular10.copy(color = colors.muted)
    val todayStyle = typography.bold9.copy(color = colors.muted)
    val goalStyle = typography.bold10.copy(color = colors.nightInk)
    val gridColor = colors.divider
    val bandColor = colors.accentSoft
    val goalColor = colors.nightInk
    val lineColor = colors.accent
    val dotColor = colors.faint
    val todayColor = colors.dashed
    val surface = colors.surface
    val selectionColor = colors.ink.copy(alpha = 0.5f)
    val futureColor = colors.nightInk
    val goalLabel = stringResource(resource = Res.string.forecast_chart_goal, forecast.targetWeight.oneDecimalWithUnit())
    val todayLabel = stringResource(resource = Res.string.forecast_chart_today)

    val status = forecast.status as? ForecastStatus.OnTheWay
    val chartDays = forecast.chartDays()
    val startDay = chartDays.first
    val todayDay = forecast.today.toEpochDays()
    val endDay = chartDays.last
    // Without a forecast there's nothing to show after today.
    val lastSelectable = if (status != null) endDay else todayDay
    val spanDays = max(1L, endDay - startDay).toFloat()
    fun shown(kg: Double) = unitSystem.weightFromKg(kg = kg)
    val values = forecast.weights.map { shown(it.weight) } + forecast.trend.map { shown(it.weight) } + shown(forecast.targetWeight)
    val low = values.min() - 0.6
    val high = values.max() + 0.4
    val step = niceSteps.firstOrNull { (high - low) / it <= 5 } ?: niceSteps.last()
    val gridValues = generateSequence(seed = ceil(low / step) * step) { it + step }.takeWhile { it <= high }.toList()

    fun projected(day: Long, multiplier: Double): Double {
        val pace = forecast.pace ?: 0.0
        val value = forecast.trendWeight + pace * multiplier * (day - todayDay) / 7.0
        return if (forecast.direction < 0) max(forecast.targetWeight, value) else min(forecast.targetWeight, value)
    }

    fun dayAt(x: Float, width: Float, density: Float): LocalDate {
        val left = CHART_LEFT.value * density
        val right = width - CHART_RIGHT.value * density
        val day = startDay + ((x - left) / (right - left) * spanDays).roundToInt()
        return LocalDate.fromEpochDays(day.coerceIn(startDay, lastSelectable))
    }

    Canvas(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .pointerInput(forecast) {
                detectTapGestures(onTap = { onSelect(dayAt(x = it.x, width = size.width.toFloat(), density = density)) })
            }
            .pointerInput(forecast) {
                detectHorizontalDragGestures(
                    onDragStart = { onSelect(dayAt(x = it.x, width = size.width.toFloat(), density = density)) },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        onSelect(dayAt(x = change.position.x, width = size.width.toFloat(), density = density))
                    }
                )
            }
    ) {
        val left = CHART_LEFT.toPx()
        val top = 14.dp.toPx()
        val bottom = size.height - 18.dp.toPx()
        val right = size.width - CHART_RIGHT.toPx()
        fun x(day: Long) = left + (day - startDay) / spanDays * (right - left)
        fun y(value: Double) = (top + (high - value) / (high - low) * (bottom - top)).toFloat()

        gridValues.forEach { value ->
            drawLine(color = gridColor, start = Offset(left, y(value)), end = Offset(right, y(value)), strokeWidth = 1.dp.toPx())
            val label = textMeasurer.measure(text = value.roundToInt().toString(), style = axisStyle)
            drawText(textLayoutResult = label, topLeft = Offset(0f, y(value) - label.size.height / 2f))
        }

        if (status != null && showBand) {
            val band = Path()
            val days = (todayDay..endDay step BAND_STEP_DAYS.toLong()).toList() + endDay
            days.forEachIndexed { index, day ->
                val point = Offset(x(day), y(shown(projected(day = day, multiplier = 1 - FORECAST_RANGE_SPREAD))))
                if (index == 0) band.moveTo(point.x, point.y) else band.lineTo(point.x, point.y)
            }
            days.reversed().forEach { day -> band.lineTo(x(day), y(shown(projected(day = day, multiplier = 1 + FORECAST_RANGE_SPREAD)))) }
            band.close()
            drawPath(path = band, color = bandColor)
        }

        val goalY = y(shown(forecast.targetWeight))
        drawLine(
            color = goalColor,
            start = Offset(left, goalY),
            end = Offset(right, goalY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
        )
        val goalText = textMeasurer.measure(text = goalLabel, style = goalStyle)
        val goalTextY = if (forecast.direction < 0) goalY + 3.dp.toPx() else goalY - goalText.size.height - 3.dp.toPx()
        drawText(textLayoutResult = goalText, topLeft = Offset(right - goalText.size.width, goalTextY))

        forecast.weights.forEach { weight ->
            drawCircle(
                color = dotColor.copy(alpha = 0.6f),
                radius = 1.8.dp.toPx(),
                center = Offset(x(weight.date.toEpochDays()), y(shown(weight.weight)))
            )
        }

        val trendPath = Path()
        forecast.trend.forEachIndexed { index, weight ->
            val point = Offset(x(weight.date.toEpochDays()), y(shown(weight.weight)))
            if (index == 0) trendPath.moveTo(point.x, point.y) else trendPath.lineTo(point.x, point.y)
        }
        drawPath(
            path = trendPath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val nowPoint = Offset(x(todayDay), y(shown(forecast.trendWeight)))
        drawLine(
            color = todayColor,
            start = Offset(nowPoint.x, top),
            end = Offset(nowPoint.x, bottom),
            strokeWidth = 1.dp.toPx()
        )
        val todayText = textMeasurer.measure(text = todayLabel, style = todayStyle)
        drawText(
            textLayoutResult = todayText,
            topLeft = Offset((nowPoint.x - todayText.size.width / 2f).coerceIn(0f, size.width - todayText.size.width), 0f)
        )

        if (status != null) {
            val endProjection = min(status.date.toEpochDays(), endDay)
            val end = Offset(x(endProjection), y(shown(projected(day = endProjection, multiplier = 1.0))))
            drawLine(
                color = lineColor,
                start = nowPoint,
                end = end,
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(1.dp.toPx(), 6.dp.toPx()))
            )
            if (status.date.toEpochDays() <= endDay) {
                drawCircle(color = surface, radius = 5.5.dp.toPx(), center = Offset(end.x, goalY))
                drawCircle(color = goalColor, radius = 5.5.dp.toPx(), center = Offset(end.x, goalY), style = Stroke(width = 2.5.dp.toPx()))
            }
        }
        drawCircle(color = surface, radius = 7.dp.toPx(), center = nowPoint)
        drawCircle(color = lineColor, radius = 5.dp.toPx(), center = nowPoint)

        val months = monthStarts(from = forecast.startDate, toDay = endDay)
        val every = max(1, ceil(months.size / 6.0).toInt())
        months.filterIndexed { index, _ -> index % every == 0 }.forEach { date ->
            val day = date.toEpochDays()
            if (day - startDay < 4 || endDay - day < 6) return@forEach
            val label = textMeasurer.measure(text = date.month.shortName(), style = monthStyle)
            drawText(textLayoutResult = label, topLeft = Offset(x(day) - label.size.width / 2f, size.height - label.size.height))
        }

        if (selected != null) {
            val day = selected.toEpochDays().coerceIn(startDay, lastSelectable)
            val isPast = day <= todayDay
            val value = if (isPast) forecast.trendOn(date = selected) else forecast.projectedOn(date = selected)
            val selectedX = x(day)
            drawLine(color = selectionColor, start = Offset(selectedX, top - 4.dp.toPx()), end = Offset(selectedX, bottom), strokeWidth = 1.2.dp.toPx())
            if (value != null) {
                val point = Offset(selectedX, y(shown(value)))
                drawCircle(color = surface, radius = 8.5.dp.toPx(), center = point)
                drawCircle(color = if (isPast) lineColor else futureColor, radius = 6.dp.toPx(), center = point)
            }
        }
    }
}

private fun monthStarts(from: LocalDate, toDay: Long): List<LocalDate> {
    var year = from.year
    var month = from.month.ordinal + 1
    val starts = mutableListOf<LocalDate>()
    while (true) {
        month++
        if (month > 12) {
            month = 1
            year++
        }
        val date = LocalDate(year = year, month = month, day = 1)
        if (date.toEpochDays() > toDay) return starts
        starts += date
    }
}

private fun Month.shortName(): String = name.lowercase().replaceFirstChar { it.uppercase() }.take(n = 3)
