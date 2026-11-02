package org.thelazybattley.macrotrack.features.forecast.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.forecast_add_as_milestone
import macrotrack.composeapp.generated.resources.forecast_add_as_milestone_sub
import macrotrack.composeapp.generated.resources.forecast_dash
import macrotrack.composeapp.generated.resources.forecast_deadline_intro
import macrotrack.composeapp.generated.resources.forecast_deadline_kicker
import macrotrack.composeapp.generated.resources.forecast_deadline_less
import macrotrack.composeapp.generated.resources.forecast_deadline_milestone_name
import macrotrack.composeapp.generated.resources.forecast_deadline_more
import macrotrack.composeapp.generated.resources.forecast_deadline_same
import macrotrack.composeapp.generated.resources.forecast_deadline_title
import macrotrack.composeapp.generated.resources.forecast_deadline_too_soon_body
import macrotrack.composeapp.generated.resources.forecast_deadline_too_soon_kicker
import macrotrack.composeapp.generated.resources.forecast_is_the_earliest
import macrotrack.composeapp.generated.resources.forecast_kcal_a_day
import macrotrack.composeapp.generated.resources.forecast_per_week_short
import macrotrack.composeapp.generated.resources.forecast_pick_another_date
import macrotrack.composeapp.generated.resources.forecast_pick_later_date
import macrotrack.composeapp.generated.resources.forecast_stat_at_current
import macrotrack.composeapp.generated.resources.forecast_stat_pace
import macrotrack.composeapp.generated.resources.forecast_stat_weeks
import macrotrack.composeapp.generated.resources.forecast_too_soon
import macrotrack.composeapp.generated.resources.forecast_use_kcal_from_today
import macrotrack.composeapp.generated.resources.ic_calendar
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.DeadlinePlan
import org.thelazybattley.macrotrack.domain.model.GoalForecast
import org.thelazybattley.macrotrack.domain.model.carbsFor
import org.thelazybattley.macrotrack.domain.model.deadlinePlan
import org.thelazybattley.macrotrack.features.forecast.GoalForecastCallbacks
import org.thelazybattley.macrotrack.features.forecast.GoalForecastViewModel
import org.thelazybattley.macrotrack.features.forecast.GoalForecastViewState
import org.thelazybattley.macrotrack.ui.common.CommonDatePickerDialog
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs

@Composable
fun ForecastDeadlineScreen(onPopBackStack: () -> Unit) {
    val viewModel = koinViewModel<GoalForecastViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewState.isApplied) { if (viewState.isApplied) onPopBackStack() }

    ForecastDeadlineScreen(viewState = viewState, callbacks = viewModel, onBack = onPopBackStack)
}

@Composable
private fun ForecastDeadlineScreen(
    viewState: GoalForecastViewState,
    callbacks: GoalForecastCallbacks,
    onBack: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(value = false) }
    val snapshot = viewState.snapshot
    val forecast = snapshot?.forecast
    val plan = viewState.deadlinePlan
    val selected = viewState.shownDeadline
    ForecastScaffold(
        title = Res.string.forecast_deadline_title,
        onBack = onBack,
        overlay = {
            if (showDatePicker && forecast != null) {
                CommonDatePickerDialog(
                    selected = selected ?: forecast.today.plus(period = DatePeriod(days = 60)),
                    minDate = forecast.today.plus(period = DatePeriod(days = 7)),
                    maxDate = null,
                    onDismiss = { showDatePicker = false },
                    onConfirm = { date ->
                        showDatePicker = false
                        callbacks.onDeadlineSelect(date = date)
                    }
                )
            }
        }
    ) {
        if (snapshot == null || forecast == null || plan == null || selected == null) return@ForecastScaffold
        val today = forecast.today
        Text(
            text = stringResource(resource = Res.string.forecast_deadline_intro, forecast.targetWeight.oneDecimalWithUnit()),
            style = typography.regular13,
            color = colors.muted
        )
        val options = (viewState.deadlineOptions + selected).distinct()
        Column(modifier = Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            options.chunked(size = 2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    row.forEach { date ->
                        DeadlineOption(
                            modifier = Modifier.weight(weight = 1f),
                            forecast = forecast,
                            date = date,
                            isSelected = date == selected,
                            onClick = { callbacks.onDeadlineSelect(date = date) }
                        )
                    }
                    if (row.size == 1) androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(weight = 1f))
                }
            }
        }
        ForecastDashedButton(
            text = stringResource(resource = Res.string.forecast_pick_another_date),
            icon = Res.drawable.ic_calendar,
            onClick = { showDatePicker = true }
        )
        PlanCard(forecast = forecast, plan = plan, macroGoals = snapshot.macroGoals)
        ForecastStatTiles(
            stats = listOf(
                ForecastStat(
                    label = stringResource(resource = Res.string.forecast_stat_weeks),
                    value = ((plan.weeks * 10).toInt() / 10.0).toString()
                ),
                ForecastStat(
                    label = stringResource(resource = Res.string.forecast_stat_pace),
                    value = stringResource(
                        resource = Res.string.forecast_per_week_short,
                        plan.kgPerWeek.signedWeekly(direction = forecast.direction),
                        weightUnitLabel()
                    )
                ),
                ForecastStat(
                    label = stringResource(resource = Res.string.forecast_stat_at_current, snapshot.macroGoals.calories.withThousands()),
                    value = viewState.whatIfNow?.date?.forecastDate(today = today) ?: stringResource(resource = Res.string.forecast_dash)
                )
            )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
        ) {
            ForecastSwitchRow(
                label = stringResource(resource = Res.string.forecast_add_as_milestone),
                subtitle = stringResource(resource = Res.string.forecast_add_as_milestone_sub),
                isOn = viewState.isDeadlineMilestone,
                onToggle = callbacks::onDeadlineMilestoneToggle
            )
        }
        val milestoneName = stringResource(resource = Res.string.forecast_deadline_milestone_name, plan.date.forecastDate(today = today))
        CommonPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = if (plan.isTooSoon) {
                stringResource(resource = Res.string.forecast_pick_later_date)
            } else {
                stringResource(resource = Res.string.forecast_use_kcal_from_today, plan.kcal.withThousands())
            },
            enabled = !plan.isTooSoon,
            onClick = { callbacks.onApplyDeadline(milestoneName = milestoneName) }
        )
    }
}

@Composable
private fun DeadlineOption(
    modifier: Modifier = Modifier,
    forecast: GoalForecast,
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val plan = forecast.deadlinePlan(date = date)
    val shape = RoundedCornerShape(size = 14.dp)
    val accent = if (plan.isTooSoon) colors.fat else colors.accent
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = if (!isSelected) colors.surface else if (plan.isTooSoon) colors.warmSoft else colors.accentSoft)
            .border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) accent else colors.line, shape = shape)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = date.forecastDate(today = forecast.today), style = typography.bold18, color = colors.ink)
        Text(
            text = if (plan.isTooSoon) {
                stringResource(resource = Res.string.forecast_too_soon)
            } else {
                stringResource(
                    resource = Res.string.forecast_per_week_short,
                    plan.kgPerWeek.signedWeekly(direction = forecast.direction),
                    weightUnitLabel()
                )
            },
            style = typography.regular11,
            color = colors.muted
        )
    }
}

@Composable
private fun PlanCard(forecast: GoalForecast, plan: DeadlinePlan, macroGoals: MacroGoals) {
    val today = forecast.today
    val dateText = plan.date.forecastDate(today = today)
    ForecastHeroCard(
        kicker = if (plan.isTooSoon) {
            stringResource(resource = Res.string.forecast_deadline_too_soon_kicker)
        } else {
            stringResource(resource = Res.string.forecast_deadline_kicker, forecast.targetWeight.oneDecimalWithUnit().uppercase(), dateText.uppercase())
        },
        background = if (plan.isTooSoon) colors.warmSoft else colors.hero,
        kickerColor = if (plan.isTooSoon) colors.fat else colors.accentInk
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            Text(
                text = if (plan.isTooSoon) {
                    plan.earliestSafeDate?.forecastDate(today = today) ?: stringResource(resource = Res.string.forecast_dash)
                } else {
                    plan.kcal.withThousands()
                },
                style = typography.bold36,
                color = colors.ink
            )
            Text(
                text = stringResource(resource = if (plan.isTooSoon) Res.string.forecast_is_the_earliest else Res.string.forecast_kcal_a_day),
                style = typography.bold14,
                color = colors.muted,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        val carbs = carbsFor(calories = plan.kcal, protein = macroGoals.protein, fat = macroGoals.fat)
        val difference = plan.kcal - macroGoals.calories
        Text(
            text = when {
                plan.isTooSoon -> stringResource(
                    resource = Res.string.forecast_deadline_too_soon_body,
                    plan.kgPerWeek.signedWeekly(direction = forecast.direction),
                    weightUnitLabel(),
                    plan.realisticWeight.oneDecimalWithUnit(),
                    dateText
                )
                difference == 0 -> stringResource(resource = Res.string.forecast_deadline_same)
                else -> stringResource(
                    resource = if (difference < 0) Res.string.forecast_deadline_less else Res.string.forecast_deadline_more,
                    abs(difference).withThousands(),
                    macroGoals.protein,
                    carbs
                )
            },
            style = typography.regular13,
            color = colors.muted
        )
    }
}
