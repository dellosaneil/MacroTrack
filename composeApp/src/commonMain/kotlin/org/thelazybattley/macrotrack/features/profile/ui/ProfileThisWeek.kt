package org.thelazybattley.macrotrack.features.profile.ui

import androidx.compose.foundation.background
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.plus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.avg_calories
import macrotrack.composeapp.generated.resources.avg_protein
import macrotrack.composeapp.generated.resources.best_value_days
import macrotrack.composeapp.generated.resources.day_logging_streak
import macrotrack.composeapp.generated.resources.days_on_target
import macrotrack.composeapp.generated.resources.ic_streak
import macrotrack.composeapp.generated.resources.of_value_logged
import macrotrack.composeapp.generated.resources.slash_value
import macrotrack.composeapp.generated.resources.this_week
import macrotrack.composeapp.generated.resources.weight_change
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.profile.WeekSummary
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import org.thelazybattley.macrotrack.ui.common.formatWeight

@Composable
fun ProfileThisWeek(
    modifier: Modifier = Modifier,
    weekSummary: WeekSummary,
    macroGoals: MacroGoals?
) {
    val weekEnd = weekSummary.weekStart.plus(period = DatePeriod(days = 6))
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(resource = Res.string.this_week),
                style = typography.bold18,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            Text(
                text = "${weekSummary.weekStart.toAbbreviatedMonthDay()} – ${weekEnd.toAbbreviatedMonthDay()}",
                style = typography.regular11,
                color = colors.muted
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
                .padding(all = 14.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            StreakRow(weekSummary = weekSummary)
            WeekDots(weekSummary = weekSummary)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 1.dp)
                    .background(color = colors.divider)
            )
            Column(verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
                    WeekStat(
                        modifier = Modifier.weight(weight = 1f),
                        label = stringResource(resource = Res.string.avg_calories),
                        value = weekSummary.averageCalories?.withThousands() ?: "–",
                        suffix = macroGoals?.let {
                            stringResource(resource = Res.string.slash_value, it.calories.withThousands())
                        }
                    )
                    WeekStat(
                        modifier = Modifier.weight(weight = 1f),
                        label = stringResource(resource = Res.string.avg_protein),
                        value = weekSummary.averageProtein?.let { "${it}g" } ?: "–",
                        suffix = macroGoals?.let {
                            stringResource(resource = Res.string.slash_value, "${it.protein}g")
                        }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
                    WeekStat(
                        modifier = Modifier.weight(weight = 1f),
                        label = stringResource(resource = Res.string.days_on_target),
                        value = weekSummary.daysOnTarget.toString(),
                        suffix = stringResource(resource = Res.string.of_value_logged, weekSummary.daysLogged)
                    )
                    val weightChange = weekSummary.weightChange
                    WeekStat(
                        modifier = Modifier.weight(weight = 1f),
                        label = stringResource(resource = Res.string.weight_change),
                        value = weightChange?.let { change ->
                            (if (change > 0) "+" else "") + change.formatWeight() + " " + weightUnitLabel()
                        } ?: "–",
                        valueColor = when {
                            weightChange == null || weightChange == 0.0 -> colors.ink
                            weightChange < 0 -> colors.accentInk
                            else -> colors.fat
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakRow(weekSummary: WeekSummary) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 40.dp)
                .background(color = colors.warmSoft, shape = RoundedCornerShape(size = 12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_streak),
                contentDescription = null,
                tint = colors.warmInk,
                modifier = Modifier.size(size = 20.dp)
            )
        }
        Column {
            Text(
                text = stringResource(resource = Res.string.day_logging_streak, weekSummary.currentStreak),
                style = typography.bold15,
                color = colors.ink
            )
            Text(
                text = stringResource(resource = Res.string.best_value_days, weekSummary.bestStreak),
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun WeekDots(weekSummary: WeekSummary) {
    Row(modifier = Modifier.fillMaxWidth()) {
        DayOfWeek.entries.forEachIndexed { index, dayOfWeek ->
            val isLogged = weekSummary.loggedDays.getOrElse(index = index) { false }
            val isToday = index == weekSummary.todayIndex
            val isFuture = index > weekSummary.todayIndex
            Column(
                modifier = Modifier.weight(weight = 1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(size = 32.dp)
                        .then(
                            if (isToday) {
                                Modifier.border(width = 2.dp, color = colors.accent, shape = CircleShape)
                            } else {
                                Modifier
                            }
                        )
                        .padding(all = 3.dp)
                        .clip(shape = CircleShape)
                        .background(
                            color = when {
                                isLogged -> colors.accent
                                isFuture -> Color.Transparent
                                else -> colors.divider
                            }
                        )
                        .then(
                            if (isFuture) {
                                Modifier.border(width = 1.5.dp, color = colors.dashed, shape = CircleShape)
                            } else {
                                Modifier
                            }
                        )
                )
                Text(
                    text = dayOfWeek.name.take(n = 1),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
    }
}

@Composable
private fun WeekStat(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    suffix: String? = null,
    valueColor: Color = colors.ink
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = typography.regular11,
            color = colors.muted
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            Text(
                text = value,
                style = typography.bold18,
                color = valueColor
            )
            if (suffix != null) {
                Text(
                    text = suffix,
                    style = typography.regular11,
                    color = colors.muted,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewProfileThisWeek() {
    MacroTrackTheme {
        ProfileThisWeek(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            weekSummary = WeekSummary(
                weekStart = getCurrentDate(),
                loggedDays = listOf(true, true, true, true, false, false, false),
                todayIndex = 3,
                currentStreak = 4,
                bestStreak = 12,
                averageCalories = 2210,
                averageProtein = 131,
                daysOnTarget = 3,
                daysLogged = 4,
                weightChange = -0.3
            ),
            macroGoals = MacroGoals(calories = 2350, protein = 148, carbs = 322, fat = 52)
        )
    }
}
