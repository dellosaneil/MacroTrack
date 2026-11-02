package org.thelazybattley.macrotrack.features.forecast.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.forecast_at_current_pace
import macrotrack.composeapp.generated.resources.forecast_basis_last_2_weeks
import macrotrack.composeapp.generated.resources.forecast_basis_last_4_weeks
import macrotrack.composeapp.generated.resources.forecast_basis_since_start
import macrotrack.composeapp.generated.resources.forecast_chart_description
import macrotrack.composeapp.generated.resources.forecast_chart_description_no_date
import macrotrack.composeapp.generated.resources.forecast_collecting_body
import macrotrack.composeapp.generated.resources.forecast_collecting_kicker
import macrotrack.composeapp.generated.resources.forecast_collecting_title
import macrotrack.composeapp.generated.resources.forecast_dash
import macrotrack.composeapp.generated.resources.forecast_deadline_title
import macrotrack.composeapp.generated.resources.forecast_flat_body
import macrotrack.composeapp.generated.resources.forecast_flat_note
import macrotrack.composeapp.generated.resources.forecast_flat_title
import macrotrack.composeapp.generated.resources.forecast_footnote
import macrotrack.composeapp.generated.resources.forecast_fraction
import macrotrack.composeapp.generated.resources.forecast_legend_range
import macrotrack.composeapp.generated.resources.forecast_legend_trend
import macrotrack.composeapp.generated.resources.forecast_link_deadline_sub
import macrotrack.composeapp.generated.resources.forecast_link_milestones_sub
import macrotrack.composeapp.generated.resources.forecast_link_milestones_sub_reached
import macrotrack.composeapp.generated.resources.forecast_link_what_if_sub
import macrotrack.composeapp.generated.resources.forecast_log_todays_weight
import macrotrack.composeapp.generated.resources.forecast_milestones_title
import macrotrack.composeapp.generated.resources.forecast_no_date_kicker
import macrotrack.composeapp.generated.resources.forecast_no_goal_body
import macrotrack.composeapp.generated.resources.forecast_no_weights_body
import macrotrack.composeapp.generated.resources.forecast_of
import macrotrack.composeapp.generated.resources.forecast_pace_based_on
import macrotrack.composeapp.generated.resources.forecast_pace_per_week
import macrotrack.composeapp.generated.resources.forecast_progress_goal
import macrotrack.composeapp.generated.resources.forecast_progress_now
import macrotrack.composeapp.generated.resources.forecast_progress_start
import macrotrack.composeapp.generated.resources.forecast_range
import macrotrack.composeapp.generated.resources.forecast_reach_around
import macrotrack.composeapp.generated.resources.forecast_reached_body
import macrotrack.composeapp.generated.resources.forecast_reached_kicker
import macrotrack.composeapp.generated.resources.forecast_reached_note
import macrotrack.composeapp.generated.resources.forecast_reached_title
import macrotrack.composeapp.generated.resources.forecast_ring_label
import macrotrack.composeapp.generated.resources.forecast_set_goal_weight
import macrotrack.composeapp.generated.resources.forecast_settings_description
import macrotrack.composeapp.generated.resources.forecast_share_card
import macrotrack.composeapp.generated.resources.forecast_stat_changed
import macrotrack.composeapp.generated.resources.forecast_stat_gained
import macrotrack.composeapp.generated.resources.forecast_stat_lost
import macrotrack.composeapp.generated.resources.forecast_stat_rough_guess
import macrotrack.composeapp.generated.resources.forecast_stat_to_go
import macrotrack.composeapp.generated.resources.forecast_stat_weeks
import macrotrack.composeapp.generated.resources.forecast_stat_weeks_left
import macrotrack.composeapp.generated.resources.forecast_stat_weigh_ins
import macrotrack.composeapp.generated.resources.forecast_approx
import macrotrack.composeapp.generated.resources.forecast_tip_check_in
import macrotrack.composeapp.generated.resources.forecast_tip_check_in_sub
import macrotrack.composeapp.generated.resources.forecast_tip_check_logging
import macrotrack.composeapp.generated.resources.forecast_tip_check_logging_sub
import macrotrack.composeapp.generated.resources.forecast_tip_log_food
import macrotrack.composeapp.generated.resources.forecast_tip_log_food_sub
import macrotrack.composeapp.generated.resources.forecast_tip_maintenance
import macrotrack.composeapp.generated.resources.forecast_tip_maintenance_sub
import macrotrack.composeapp.generated.resources.forecast_tip_new_goal
import macrotrack.composeapp.generated.resources.forecast_tip_new_goal_sub
import macrotrack.composeapp.generated.resources.forecast_tip_weigh_in
import macrotrack.composeapp.generated.resources.forecast_tip_weigh_in_sub
import macrotrack.composeapp.generated.resources.forecast_tip_what_if
import macrotrack.composeapp.generated.resources.forecast_tip_what_if_sub
import macrotrack.composeapp.generated.resources.forecast_tips_more_accurate
import macrotrack.composeapp.generated.resources.forecast_tips_often_help
import macrotrack.composeapp.generated.resources.forecast_title
import macrotrack.composeapp.generated.resources.forecast_trend_and_forecast
import macrotrack.composeapp.generated.resources.forecast_what_if_title
import macrotrack.composeapp.generated.resources.forecast_what_next
import macrotrack.composeapp.generated.resources.forecast_wrong_way_body
import macrotrack.composeapp.generated.resources.forecast_wrong_way_note
import macrotrack.composeapp.generated.resources.forecast_wrong_way_title_gain
import macrotrack.composeapp.generated.resources.forecast_wrong_way_title_lose
import macrotrack.composeapp.generated.resources.ic_calendar
import macrotrack.composeapp.generated.resources.ic_check_in
import macrotrack.composeapp.generated.resources.ic_flag
import macrotrack.composeapp.generated.resources.ic_log
import macrotrack.composeapp.generated.resources.ic_scale
import macrotrack.composeapp.generated.resources.ic_sliders
import macrotrack.composeapp.generated.resources.ic_sparkle
import macrotrack.composeapp.generated.resources.ic_target
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.FORECAST_MIN_WEIGH_INS
import org.thelazybattley.macrotrack.domain.model.ForecastBasis
import org.thelazybattley.macrotrack.domain.model.ForecastStatus
import org.thelazybattley.macrotrack.domain.model.GoalForecast
import org.thelazybattley.macrotrack.domain.model.MilestoneKind
import org.thelazybattley.macrotrack.domain.usecase.forecast.ForecastSnapshot
import org.thelazybattley.macrotrack.features.forecast.GoalForecastCallbacks
import org.thelazybattley.macrotrack.features.forecast.GoalForecastViewModel
import org.thelazybattley.macrotrack.features.forecast.GoalForecastViewState
import org.thelazybattley.macrotrack.features.profile.ui.TargetWeightKeypad
import org.thelazybattley.macrotrack.features.share.ShareCardKind
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

@Composable
fun GoalForecastScreen(
    onPopBackStack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val viewModel = koinViewModel<GoalForecastViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    GoalForecastScreen(
        viewState = viewState,
        callbacks = viewModel,
        onBack = onPopBackStack,
        onNavigate = onNavigate
    )
}

@Composable
private fun GoalForecastScreen(
    viewState: GoalForecastViewState,
    callbacks: GoalForecastCallbacks,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var showTargetKeypad by remember { mutableStateOf(value = false) }
    val snapshot = viewState.snapshot
    val forecast = snapshot?.forecast
    ForecastScaffold(
        title = Res.string.forecast_title,
        onBack = onBack,
        trailing = {
            if (forecast != null) {
                SettingsButton(onClick = { onNavigate(AppDestinations.Root.ForecastSettings.route) })
            }
        },
        overlay = {
            if (showTargetKeypad && snapshot != null) {
                TargetWeightKeypad(
                    currentTarget = snapshot.userDetails.targetWeight,
                    currentWeight = snapshot.userDetails.weight,
                    heightCm = snapshot.userDetails.height,
                    onSave = { target -> callbacks.onSaveTargetWeight(targetKg = target) },
                    onDismiss = { showTargetKeypad = false }
                )
            }
        }
    ) {
        if (!viewState.isLoaded || snapshot == null) return@ForecastScaffold
        if (forecast == null) {
            NoForecast(
                hasGoal = snapshot.userDetails.targetWeight != null,
                onSetGoal = { showTargetKeypad = true },
                onLogWeight = { onNavigate(AppDestinations.Root.LogWeight.createRoute()) }
            )
            return@ForecastScaffold
        }
        when (val status = forecast.status) {
            is ForecastStatus.OnTheWay -> OnTheWay(
                snapshot = snapshot,
                forecast = forecast,
                status = status,
                onBasisSelect = callbacks::onBasisSelect,
                onNavigate = onNavigate
            )
            is ForecastStatus.Collecting -> Collecting(forecast = forecast, status = status, onNavigate = onNavigate)
            ForecastStatus.Flat -> Flat(forecast = forecast, onNavigate = onNavigate)
            ForecastStatus.WrongWay -> WrongWay(forecast = forecast, onNavigate = onNavigate)
            is ForecastStatus.Reached -> Reached(
                snapshot = snapshot,
                forecast = forecast,
                status = status,
                onSwitchToMaintenance = callbacks::onSwitchToMaintenance,
                onNewGoal = { showTargetKeypad = true },
                onNavigate = onNavigate
            )
        }
    }
}

@Composable
private fun SettingsButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_sliders),
            contentDescription = stringResource(resource = Res.string.forecast_settings_description),
            tint = colors.ink,
            modifier = Modifier.size(size = 19.dp)
        )
    }
}

@Composable
private fun OnTheWay(
    snapshot: ForecastSnapshot,
    forecast: GoalForecast,
    status: ForecastStatus.OnTheWay,
    onBasisSelect: (ForecastBasis) -> Unit,
    onNavigate: (String) -> Unit
) {
    val today = forecast.today
    val goalText = forecast.targetWeight.oneDecimalWithUnit()
    ForecastHeroCard(
        kicker = stringResource(resource = Res.string.forecast_reach_around, goalText.uppercase()),
        background = colors.hero,
        kickerColor = colors.accentInk
    ) {
        Text(text = status.date.forecastDate(today = today), style = typography.bold36, color = colors.ink)
        Text(
            text = if (snapshot.settings.isRangeShown) {
                stringResource(
                    resource = Res.string.forecast_range,
                    status.earliest.forecastDate(today = today),
                    status.latest.forecastDate(today = today)
                )
            } else {
                stringResource(resource = Res.string.forecast_at_current_pace)
            },
            style = typography.regular13,
            color = colors.muted
        )
        Box(modifier = Modifier.height(height = 10.dp))
        ForecastProgressBar(
            progress = forecast.progress,
            start = stringResource(resource = Res.string.forecast_progress_start, forecast.startWeight.oneDecimalWithUnit()),
            now = stringResource(
                resource = Res.string.forecast_progress_now,
                forecast.trendWeight.oneDecimalWithUnit(),
                (forecast.progress * 100).roundToInt()
            ),
            goal = stringResource(resource = Res.string.forecast_progress_goal, goalText)
        )
    }
    BasisPicker(forecast = forecast, onBasisSelect = onBasisSelect)
    ChartCard(forecast = forecast, status = status)
    ForecastStatTiles(
        stats = listOf(
            ForecastStat(label = stringResource(resource = Res.string.forecast_stat_to_go), value = forecast.remainingKg.oneDecimalWithUnit()),
            ForecastStat(
                label = stringResource(resource = Res.string.forecast_stat_weeks_left),
                value = stringResource(
                    resource = Res.string.forecast_approx,
                    ((status.date.toEpochDays() - today.toEpochDays()) / 7.0).roundToInt().toString()
                )
            ),
            ForecastStat(
                label = stringResource(resource = if (forecast.direction < 0) Res.string.forecast_stat_lost else Res.string.forecast_stat_gained),
                value = forecast.changedKg.oneDecimalWithUnit()
            )
        )
    )
    val reached = snapshot.milestones.count { it.isReached }
    val next = snapshot.milestones.firstOrNull { !it.isReached && it.kind != MilestoneKind.GOAL }
    ForecastLinksCard(
        links = listOf(
            ForecastLink(
                icon = Res.drawable.ic_sparkle,
                title = stringResource(resource = Res.string.forecast_what_if_title),
                subtitle = stringResource(resource = Res.string.forecast_link_what_if_sub),
                tint = colors.nightInk,
                background = colors.nightSoft,
                onClick = { onNavigate(AppDestinations.Root.ForecastWhatIf.route) }
            ),
            ForecastLink(
                icon = Res.drawable.ic_calendar,
                title = stringResource(resource = Res.string.forecast_deadline_title),
                subtitle = stringResource(resource = Res.string.forecast_link_deadline_sub),
                tint = colors.accentInk,
                background = colors.accentSoft,
                onClick = { onNavigate(AppDestinations.Root.ForecastDeadline.route) }
            ),
            ForecastLink(
                icon = Res.drawable.ic_flag,
                title = stringResource(resource = Res.string.forecast_milestones_title),
                subtitle = if (next?.expectedOn != null) {
                    stringResource(
                        resource = Res.string.forecast_link_milestones_sub,
                        reached,
                        next.label(direction = forecast.direction).lowercase(),
                        next.expectedOn.forecastDate(today = today)
                    )
                } else {
                    stringResource(resource = Res.string.forecast_link_milestones_sub_reached, reached)
                },
                tint = colors.warmInk,
                background = colors.warmSoft,
                onClick = { onNavigate(AppDestinations.Root.ForecastMilestones.route) }
            )
        )
    )
    ForecastFootnote(text = stringResource(resource = Res.string.forecast_footnote))
}

@Composable
private fun BasisPicker(forecast: GoalForecast, onBasisSelect: (ForecastBasis) -> Unit) {
    val pace = forecast.pace ?: 0.0
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(resource = Res.string.forecast_pace_based_on),
                style = typography.medium11,
                color = colors.muted,
                modifier = Modifier.weight(weight = 1f)
            )
            Text(
                text = stringResource(
                    resource = Res.string.forecast_pace_per_week,
                    pace.signedWeekly(direction = if (pace < 0) -1 else 1),
                    weightUnitLabel()
                ),
                style = typography.bold12,
                color = colors.accentInk
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 14.dp))
                .background(color = colors.divider)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
                .padding(all = 4.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            listOf(
                ForecastBasis.LAST_2_WEEKS to Res.string.forecast_basis_last_2_weeks,
                ForecastBasis.LAST_4_WEEKS to Res.string.forecast_basis_last_4_weeks,
                ForecastBasis.SINCE_START to Res.string.forecast_basis_since_start
            ).forEach { (basis, label) ->
                val isSelected = forecast.basis == basis
                val isEnabled = forecast.paceByBasis[basis] != null
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 40.dp)
                        .clip(shape = RoundedCornerShape(size = 10.dp))
                        .background(color = if (isSelected) colors.surface else colors.divider)
                        .selectable(selected = isSelected, enabled = isEnabled, role = Role.Tab, onClick = { onBasisSelect(basis) }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(resource = label),
                        style = typography.medium13,
                        color = when {
                            isSelected -> colors.ink
                            isEnabled -> colors.muted
                            else -> colors.faint
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartCard(forecast: GoalForecast, status: ForecastStatus.OnTheWay?) {
    val start = forecast.startWeight.oneDecimalWithUnit()
    val now = forecast.trendWeight.oneDecimalWithUnit()
    val description = if (status != null) {
        stringResource(
            resource = Res.string.forecast_chart_description,
            start,
            now,
            forecast.targetWeight.oneDecimalWithUnit(),
            status.date.forecastDate(today = forecast.today)
        )
    } else {
        stringResource(resource = Res.string.forecast_chart_description_no_date, start, now)
    }
    ForecastCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(resource = Res.string.forecast_trend_and_forecast),
                style = typography.bold14,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            LegendSwatch(height = 3, color = colors.accent, label = stringResource(resource = Res.string.forecast_legend_trend))
            if (status != null) {
                Box(modifier = Modifier.width(width = 10.dp))
                LegendSwatch(height = 8, color = colors.accentSoft, label = stringResource(resource = Res.string.forecast_legend_range))
            }
        }
        ForecastChart(
            modifier = Modifier.fillMaxWidth().height(height = 190.dp),
            forecast = forecast,
            contentDescription = description
        )
    }
}

@Composable
private fun LegendSwatch(height: Int, color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
        Box(modifier = Modifier.size(width = 12.dp, height = height.dp).background(color = color, shape = RoundedCornerShape(size = 2.dp)))
        Text(text = label, style = typography.regular11, color = colors.muted)
    }
}

@Composable
private fun Collecting(forecast: GoalForecast, status: ForecastStatus.Collecting, onNavigate: (String) -> Unit) {
    val left = (FORECAST_MIN_WEIGH_INS - forecast.weighIns).coerceAtLeast(minimumValue = 1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 22.dp))
            .background(color = colors.nightSoft)
            .padding(all = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ForecastRing(
            fraction = forecast.weighIns / FORECAST_MIN_WEIGH_INS.toFloat(),
            value = stringResource(resource = Res.string.forecast_fraction, forecast.weighIns, FORECAST_MIN_WEIGH_INS),
            label = stringResource(resource = Res.string.forecast_ring_label)
        )
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(
                text = pluralStringResource(resource = Res.plurals.forecast_collecting_kicker, quantity = left, left),
                style = typography.bold11,
                color = colors.nightInk
            )
            Text(text = stringResource(resource = Res.string.forecast_collecting_title), style = typography.bold24, color = colors.ink)
            Text(text = stringResource(resource = Res.string.forecast_collecting_body), style = typography.regular13, color = colors.muted)
        }
    }
    ChartCard(forecast = forecast, status = null)
    ForecastStatTiles(
        stats = listOf(
            ForecastStat(
                label = stringResource(resource = Res.string.forecast_stat_weigh_ins),
                value = stringResource(resource = Res.string.forecast_of, forecast.weighIns, FORECAST_MIN_WEIGH_INS)
            ),
            ForecastStat(
                label = stringResource(resource = Res.string.forecast_stat_rough_guess),
                value = status.roughDate?.forecastDate(today = forecast.today) ?: stringResource(resource = Res.string.forecast_dash)
            )
        )
    )
    ForecastSectionLabel(text = stringResource(resource = Res.string.forecast_tips_more_accurate))
    ForecastLinksCard(
        links = listOf(
            ForecastLink(
                icon = Res.drawable.ic_scale,
                title = stringResource(resource = Res.string.forecast_tip_weigh_in),
                subtitle = stringResource(resource = Res.string.forecast_tip_weigh_in_sub),
                tint = colors.accentInk,
                background = colors.accentSoft,
                onClick = { onNavigate(AppDestinations.Root.LogWeight.createRoute()) }
            ),
            ForecastLink(
                icon = Res.drawable.ic_log,
                title = stringResource(resource = Res.string.forecast_tip_log_food),
                subtitle = stringResource(resource = Res.string.forecast_tip_log_food_sub),
                tint = colors.warmInk,
                background = colors.warmSoft,
                onClick = null
            )
        )
    )
    CommonPrimaryButton(
        modifier = Modifier.fillMaxWidth(),
        text = Res.string.forecast_log_todays_weight,
        onClick = { onNavigate(AppDestinations.Root.LogWeight.createRoute()) }
    )
}

@Composable
private fun NoDateHero(title: String, body: String) {
    ForecastHeroCard(
        kicker = stringResource(resource = Res.string.forecast_no_date_kicker),
        background = colors.warmSoft,
        kickerColor = colors.warmInk
    ) {
        Text(text = title, style = typography.bold24, color = colors.ink)
        Text(text = body, style = typography.regular13, color = colors.muted)
    }
}

@Composable
private fun NoDateTips(onNavigate: (String) -> Unit) {
    ForecastSectionLabel(text = stringResource(resource = Res.string.forecast_tips_often_help))
    ForecastLinksCard(
        links = listOf(
            ForecastLink(
                icon = Res.drawable.ic_log,
                title = stringResource(resource = Res.string.forecast_tip_check_logging),
                subtitle = stringResource(resource = Res.string.forecast_tip_check_logging_sub),
                tint = colors.warmInk,
                background = colors.warmSoft,
                onClick = null
            ),
            ForecastLink(
                icon = Res.drawable.ic_sparkle,
                title = stringResource(resource = Res.string.forecast_tip_what_if),
                subtitle = stringResource(resource = Res.string.forecast_tip_what_if_sub),
                tint = colors.nightInk,
                background = colors.nightSoft,
                onClick = { onNavigate(AppDestinations.Root.ForecastWhatIf.route) }
            ),
            ForecastLink(
                icon = Res.drawable.ic_check_in,
                title = stringResource(resource = Res.string.forecast_tip_check_in),
                subtitle = stringResource(resource = Res.string.forecast_tip_check_in_sub),
                tint = colors.accentInk,
                background = colors.accentSoft,
                onClick = { onNavigate(AppDestinations.Root.CheckIn.route) }
            )
        )
    )
}

@Composable
private fun Flat(forecast: GoalForecast, onNavigate: (String) -> Unit) {
    NoDateHero(
        title = stringResource(resource = Res.string.forecast_flat_title),
        body = stringResource(resource = Res.string.forecast_flat_body, forecast.trendWeight.oneDecimalWithUnit())
    )
    ChartCard(forecast = forecast, status = null)
    NoDateTips(onNavigate = onNavigate)
    ForecastNote(text = stringResource(resource = Res.string.forecast_flat_note))
}

@Composable
private fun WrongWay(forecast: GoalForecast, onNavigate: (String) -> Unit) {
    NoDateHero(
        title = stringResource(
            resource = if (forecast.direction < 0) Res.string.forecast_wrong_way_title_lose else Res.string.forecast_wrong_way_title_gain
        ),
        body = stringResource(resource = Res.string.forecast_wrong_way_body, forecast.targetWeight.oneDecimalWithUnit())
    )
    ChartCard(forecast = forecast, status = null)
    NoDateTips(onNavigate = onNavigate)
    ForecastNote(text = stringResource(resource = Res.string.forecast_wrong_way_note))
}

@Composable
private fun Reached(
    snapshot: ForecastSnapshot,
    forecast: GoalForecast,
    status: ForecastStatus.Reached,
    onSwitchToMaintenance: () -> Unit,
    onNewGoal: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val today = forecast.today
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 136.dp)
                .background(color = colors.hero, shape = RoundedCornerShape(size = 48.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(size = 116.dp).background(color = colors.accentSoft, shape = RoundedCornerShape(size = 38.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_target),
                    contentDescription = null,
                    tint = colors.accentInk,
                    modifier = Modifier.size(size = 56.dp)
                )
            }
        }
        Text(
            text = stringResource(resource = Res.string.forecast_reached_kicker, status.date.forecastDate(today = today).uppercase()),
            style = typography.bold12,
            color = colors.accentInk
        )
        Text(
            text = stringResource(resource = Res.string.forecast_reached_title, forecast.targetWeight.oneDecimalWithUnit()),
            style = typography.bold32,
            color = colors.ink,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(
                resource = Res.string.forecast_reached_body,
                forecast.startWeight.oneDecimalWithUnit(),
                forecast.startDate.forecastDate(today = today)
            ),
            style = typography.regular13,
            color = colors.muted,
            textAlign = TextAlign.Center
        )
    }
    ForecastStatTiles(
        stats = listOf(
            ForecastStat(
                label = stringResource(resource = Res.string.forecast_stat_changed),
                value = kotlin.math.abs(forecast.targetWeight - forecast.startWeight).oneDecimalWithUnit()
            ),
            ForecastStat(
                label = stringResource(resource = Res.string.forecast_stat_weeks),
                value = ((status.date.toEpochDays() - forecast.startDate.toEpochDays()) / 7).toString()
            ),
            ForecastStat(label = stringResource(resource = Res.string.forecast_stat_weigh_ins), value = forecast.weighIns.toString())
        )
    )
    ForecastSectionLabel(text = stringResource(resource = Res.string.forecast_what_next))
    ForecastLinksCard(
        links = listOf(
            ForecastLink(
                icon = Res.drawable.ic_scale,
                title = stringResource(resource = Res.string.forecast_tip_maintenance),
                subtitle = stringResource(resource = Res.string.forecast_tip_maintenance_sub, snapshot.maintenance.withThousands()),
                tint = colors.accentInk,
                background = colors.accentSoft,
                onClick = onSwitchToMaintenance
            ),
            ForecastLink(
                icon = Res.drawable.ic_target,
                title = stringResource(resource = Res.string.forecast_tip_new_goal),
                subtitle = stringResource(resource = Res.string.forecast_tip_new_goal_sub),
                tint = colors.nightInk,
                background = colors.nightSoft,
                onClick = onNewGoal
            )
        )
    )
    ForecastNote(text = stringResource(resource = Res.string.forecast_reached_note), background = colors.hero, color = colors.accentInk)
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        ForecastSecondaryButton(
            modifier = Modifier.weight(weight = 1f),
            text = stringResource(resource = Res.string.forecast_share_card),
            onClick = { onNavigate(AppDestinations.Root.ShareCard.createRoute(kind = ShareCardKind.WEIGHT.name)) }
        )
        CommonPrimaryButton(
            modifier = Modifier.weight(weight = 1f),
            text = Res.string.forecast_tip_maintenance,
            onClick = onSwitchToMaintenance
        )
    }
}

@Composable
private fun NoForecast(hasGoal: Boolean, onSetGoal: () -> Unit, onLogWeight: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 22.dp))
            .background(color = colors.hero)
            .padding(all = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Icon(
            painter = painterResource(resource = if (hasGoal) Res.drawable.ic_scale else Res.drawable.ic_target),
            contentDescription = null,
            tint = colors.accentInk,
            modifier = Modifier.size(size = 40.dp)
        )
        Text(
            text = stringResource(resource = if (hasGoal) Res.string.forecast_no_weights_body else Res.string.forecast_no_goal_body),
            style = typography.regular13,
            color = colors.muted,
            textAlign = TextAlign.Center
        )
        CommonPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = if (hasGoal) Res.string.forecast_log_todays_weight else Res.string.forecast_set_goal_weight,
            onClick = if (hasGoal) onLogWeight else onSetGoal
        )
    }
}
