package org.thelazybattley.macrotrack.features.profile.pastgoal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.period_dates
import macrotrack.composeapp.generated.resources.period_to_today
import macrotrack.composeapp.generated.resources.value_kcal
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.GoalPeriod
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Goal periods, one row each, with [isHighlighted] periods shaded. */
@Composable
fun GoalPeriodList(
    modifier: Modifier = Modifier,
    periods: List<GoalPeriod>,
    today: LocalDate,
    isHighlighted: (GoalPeriod) -> Boolean = { false },
    footer: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(size = 18.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
    ) {
        periods.forEachIndexed { index, period ->
            if (index > 0) HorizontalDivider(color = colors.divider)
            GoalPeriodRow(period = period, today = today, isHighlighted = isHighlighted(period))
        }
        if (footer != null) {
            if (periods.isNotEmpty()) HorizontalDivider(color = colors.divider)
            footer()
        }
    }
}

@Composable
private fun GoalPeriodRow(
    period: GoalPeriod,
    today: LocalDate,
    isHighlighted: Boolean
) {
    val dates = if (period.end == today) {
        stringResource(resource = Res.string.period_to_today, period.start.toShortDate(today = today))
    } else {
        stringResource(resource = Res.string.period_dates, period.start.toShortDate(today = today), period.end.toShortDate(today = today))
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 50.dp)
            .background(color = if (isHighlighted) colors.hero else Color.Transparent)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        GoalChip(goal = period.goal)
        Text(text = dates, style = typography.regular13, color = colors.ink, modifier = Modifier.weight(weight = 1f))
        Text(
            text = stringResource(resource = Res.string.value_kcal, period.calories.withThousands()),
            style = typography.bold13,
            color = colors.ink
        )
    }
}

@Composable
fun GoalChip(goal: Goal) {
    val (background, ink) = goal.tones()
    Text(
        text = stringResource(resource = goal.title),
        style = typography.bold12,
        color = ink,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .width(width = 70.dp)
            .clip(shape = RoundedCornerShape(size = 8.dp))
            .background(color = background)
            .padding(vertical = 3.dp)
    )
}

@Composable
fun Goal.tones(): Pair<Color, Color> = when (this) {
    Goal.LOSE_WEIGHT -> colors.accentSoft to colors.accentInk
    Goal.MAINTAIN_WEIGHT -> colors.nightSoft to colors.nightInk
    Goal.GAIN_WEIGHT -> colors.warmSoft to colors.warmInk
}

/** "Oct 5", or "Oct 5, 2025" outside the current year. */
fun LocalDate.toShortDate(today: LocalDate): String =
    toAbbreviatedMonthDay() + if (year != today.year) ", $year" else ""
