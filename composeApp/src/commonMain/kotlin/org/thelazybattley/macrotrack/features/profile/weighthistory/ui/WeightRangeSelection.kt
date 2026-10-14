package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.clear
import macrotrack.composeapp.generated.resources.clear_selected_range
import macrotrack.composeapp.generated.resources.compare_range_hint
import macrotrack.composeapp.generated.resources.day_count
import macrotrack.composeapp.generated.resources.difference
import macrotrack.composeapp.generated.resources.goal_legend
import macrotrack.composeapp.generated.resources.ic_compare_range
import macrotrack.composeapp.generated.resources.range_end
import macrotrack.composeapp.generated.resources.range_start
import macrotrack.composeapp.generated.resources.rate_per_week
import macrotrack.composeapp.generated.resources.value_kg_unit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.pastgoal.ui.tones
import org.thelazybattley.macrotrack.features.profile.ui.formatKg
import org.thelazybattley.macrotrack.features.profile.weighthistory.GoalPhase
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightPeriod
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val SAME_THRESHOLD_KG = 0.005
private const val BAND_ALPHA = 0.75f
private const val SELECTION_ALPHA = 0.12f

/** Two weigh-in dates picked on a chart; [anchor] is where the long-press started, so it can sit after [end]. */
data class WeightRange(val anchor: LocalDate, val end: LocalDate) {
    val first: LocalDate get() = minOf(anchor, end)
    val last: LocalDate get() = maxOf(anchor, end)
}

/** The x of [date] on a chart laid out like [pointsFor]. */
internal fun xForDate(date: LocalDate, period: WeightPeriod, spanDays: Long, width: Float, density: Density): Float =
    with(density) {
        val left = AXIS_LABEL_WIDTH_DP.dp.toPx()
        val inset = 6.dp.toPx()
        left + inset + (width - left - inset * 2) * (date.toEpochDays() - period.start.toEpochDays()) / spanDays.toFloat()
    }

/** Long-press then drag picks the weigh-ins in [weightList] nearest the finger. */
@Composable
internal fun Modifier.longPressRangeSelection(
    weightList: List<Weight>,
    period: WeightPeriod,
    spanDays: Long,
    onSelectionStart: (LocalDate) -> Unit,
    onSelectionDrag: (LocalDate) -> Unit,
    onSelectionEnd: () -> Unit
): Modifier {
    val haptics = LocalHapticFeedback.current
    val currentOnStart by rememberUpdatedState(newValue = onSelectionStart)
    val currentOnDrag by rememberUpdatedState(newValue = onSelectionDrag)
    val currentOnEnd by rememberUpdatedState(newValue = onSelectionEnd)
    return pointerInput(weightList, period) {
        fun dateAt(x: Float): LocalDate = weightList.minBy { weight ->
            abs(xForDate(date = weight.date, period = period, spanDays = spanDays, width = size.width.toFloat(), density = this) - x)
        }.date
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
}

/** A band per goal behind the plot, with a dashed line where a goal starts inside the period. */
internal fun DrawScope.drawGoalBands(
    goalPhases: List<GoalPhase>,
    period: WeightPeriod,
    spanDays: Long,
    bottom: Float,
    bandColors: Map<Goal, Color>,
    changeMarkColor: Color
) {
    val left = AXIS_LABEL_WIDTH_DP.dp.toPx()
    val dayWidth = xForDate(date = period.start.plusDays(1), period = period, spanDays = spanDays, width = size.width, density = this) -
        xForDate(date = period.start, period = period, spanDays = spanDays, width = size.width, density = this)
    fun x(date: LocalDate) = xForDate(date = date, period = period, spanDays = spanDays, width = size.width, density = this)
    val visible = goalPhases.filter { it.end >= period.start && it.start <= period.end }
    visible.forEach { phase ->
        val x0 = max(left, x(date = maxOf(phase.start, period.start)) - dayWidth / 2f)
        val x1 = min(size.width, x(date = minOf(phase.end, period.end)) + dayWidth / 2f)
        drawRect(
            color = bandColors.getValue(phase.goal).copy(alpha = BAND_ALPHA),
            topLeft = Offset(x = x0, y = 0f),
            size = Size(width = x1 - x0, height = bottom)
        )
    }
    visible.filter { it.start > period.start }.forEach { phase ->
        val x = max(left, x(date = phase.start) - dayWidth / 2f)
        drawLine(
            color = changeMarkColor,
            start = Offset(x = x, y = 0f),
            end = Offset(x = x, y = bottom),
            strokeWidth = 1.2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
        )
    }
}

/** The shaded span between the two ends of [selection], edged by a line at each end. */
internal fun DrawScope.drawRangeSelection(
    selection: WeightRange,
    period: WeightPeriod,
    spanDays: Long,
    bottom: Float,
    fillColor: Color,
    lineColor: Color
) {
    val xa = xForDate(date = selection.first, period = period, spanDays = spanDays, width = size.width, density = this)
    val xb = xForDate(date = selection.last, period = period, spanDays = spanDays, width = size.width, density = this)
    drawRect(
        color = fillColor.copy(alpha = SELECTION_ALPHA),
        topLeft = Offset(x = xa, y = 0f),
        size = Size(width = xb - xa, height = bottom)
    )
    listOf(xa, xb).forEach { x ->
        drawLine(color = lineColor, start = Offset(x = x, y = 0f), end = Offset(x = x, y = bottom), strokeWidth = 1.5.dp.toPx())
    }
}

/** A filled dot on the line at each end of [selection]; [points] lines up with [weightList]. */
internal fun DrawScope.drawRangeEnds(
    selection: WeightRange,
    weightList: List<Weight>,
    points: List<Offset>,
    fillColor: Color,
    outlineColor: Color
) {
    listOf(selection.first, selection.last).forEach { date ->
        val index = weightList.indexOfFirst { it.date == date }
        if (index < 0) return@forEach
        drawCircle(color = fillColor, radius = 5.5.dp.toPx(), center = points[index])
        drawCircle(color = outlineColor, radius = 5.5.dp.toPx(), center = points[index], style = Stroke(width = 2.dp.toPx()))
    }
}

private fun LocalDate.plusDays(days: Int): LocalDate = LocalDate.fromEpochDays(toEpochDays() + days)

@Composable
internal fun goalBandColors(): Map<Goal, Color> = Goal.entries.associateWith { it.tones().first }

/** "Goal:" followed by a chip in each goal's band color. */
@Composable
internal fun GoalLegend() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.goal_legend),
            style = typography.regular11,
            color = colors.muted
        )
        listOf(Goal.MAINTAIN_WEIGHT, Goal.LOSE_WEIGHT, Goal.GAIN_WEIGHT).forEach { goal ->
            val (background, ink) = goal.tones()
            Text(
                text = stringResource(resource = goal.title),
                style = typography.bold11,
                color = ink,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 6.dp))
                    .background(color = background)
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
internal fun CompareHint() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_compare_range),
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(size = 16.dp)
        )
        Text(
            text = stringResource(resource = Res.string.compare_range_hint),
            style = typography.regular11,
            color = colors.muted
        )
    }
}

/**
 * The value in [values] at each end of [range], how far it moved, and the goals the range spanned.
 * [title] gets the range's length, e.g. "5 days"; [unitLabel] follows each end's value.
 */
@Composable
internal fun SelectedRangeCard(
    range: WeightRange,
    values: List<Weight>,
    goalPhases: List<GoalPhase>,
    unitLabel: String,
    title: @Composable (dayCount: String) -> String,
    onClear: () -> Unit
) {
    val startValue = values.firstOrNull { it.date == range.first }?.weight ?: return
    val endValue = values.firstOrNull { it.date == range.last }?.weight ?: return
    val difference = endValue - startValue
    val days = (range.last.toEpochDays() - range.first.toEpochDays()).toInt()
    val goals = goalPhases
        .filter { it.end >= range.first && it.start <= range.last }
        .map { stringResource(resource = it.goal.title) }
        .distinct()
    val shape = RoundedCornerShape(size = 18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
            .clip(shape = shape)
            .background(color = colors.background)
            .border(width = 1.5.dp, color = colors.accent, shape = shape)
            .padding(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = title(pluralStringResource(resource = Res.plurals.day_count, quantity = days, days)),
                    style = typography.bold11,
                    color = colors.muted
                )
                if (goals.isNotEmpty()) {
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(resource = Res.string.goal_legend) + " ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colors.ink)) {
                                append(goals.joinToString(separator = " → "))
                            }
                        },
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
            }
            val clearLabel = stringResource(resource = Res.string.clear_selected_range)
            val buttonShape = RoundedCornerShape(size = 12.dp)
            Box(
                modifier = Modifier
                    .height(height = 44.dp)
                    .clip(shape = buttonShape)
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = buttonShape)
                    .clickable(role = Role.Button, onClick = onClear)
                    .semantics { contentDescription = clearLabel }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = Res.string.clear),
                    style = typography.bold13,
                    color = colors.ink
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            RangeEnd(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = Res.string.range_start, range.first.toAbbreviatedMonthDay()),
                value = startValue,
                unitLabel = unitLabel
            )
            RangeEnd(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = Res.string.range_end, range.last.toAbbreviatedMonthDay()),
                value = endValue,
                unitLabel = unitLabel
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(resource = Res.string.difference),
                style = typography.regular11,
                color = colors.muted
            )
            Text(
                text = stringResource(resource = Res.string.value_kg_unit, difference.toSignedKg()),
                style = typography.bold24,
                color = when {
                    difference < -SAME_THRESHOLD_KG -> colors.accentInk
                    difference > SAME_THRESHOLD_KG -> colors.fat
                    else -> colors.ink
                }
            )
            if (days >= 2) {
                Text(
                    text = stringResource(resource = Res.string.rate_per_week, (difference / days * 7).toSignedKg()),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
    }
}

@Composable
private fun RangeEnd(
    modifier: Modifier = Modifier,
    label: String,
    value: Double,
    unitLabel: String
) {
    val shape = RoundedCornerShape(size = 12.dp)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(text = label, style = typography.regular11, color = colors.muted)
        Row(horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(
                text = value.formatKg(),
                style = typography.bold18,
                color = colors.ink,
                modifier = Modifier.alignByBaseline()
            )
            Text(
                text = unitLabel,
                style = typography.regular11,
                color = colors.muted,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}
