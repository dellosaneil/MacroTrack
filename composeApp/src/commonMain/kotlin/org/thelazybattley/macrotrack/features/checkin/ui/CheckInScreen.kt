package org.thelazybattley.macrotrack.features.checkin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.check_in_apply
import macrotrack.composeapp.generated.resources.check_in_applied_body
import macrotrack.composeapp.generated.resources.check_in_applied_title
import macrotrack.composeapp.generated.resources.check_in_average_eaten
import macrotrack.composeapp.generated.resources.check_in_carbs_note
import macrotrack.composeapp.generated.resources.check_in_collecting_body
import macrotrack.composeapp.generated.resources.check_in_count_of
import macrotrack.composeapp.generated.resources.check_in_data_summary
import macrotrack.composeapp.generated.resources.check_in_days_logged
import macrotrack.composeapp.generated.resources.check_in_days_logged_of
import macrotrack.composeapp.generated.resources.check_in_days_logged_tip
import macrotrack.composeapp.generated.resources.check_in_done
import macrotrack.composeapp.generated.resources.check_in_energy_formula
import macrotrack.composeapp.generated.resources.check_in_first
import macrotrack.composeapp.generated.resources.check_in_footnote
import macrotrack.composeapp.generated.resources.check_in_how
import macrotrack.composeapp.generated.resources.check_in_kcal_a_day
import macrotrack.composeapp.generated.resources.check_in_keep
import macrotrack.composeapp.generated.resources.check_in_locked_body
import macrotrack.composeapp.generated.resources.check_in_locked_burn
import macrotrack.composeapp.generated.resources.check_in_locked_ready
import macrotrack.composeapp.generated.resources.check_in_locked_title
import macrotrack.composeapp.generated.resources.check_in_measured
import macrotrack.composeapp.generated.resources.check_in_measured_burn
import macrotrack.composeapp.generated.resources.check_in_measured_burn_row
import macrotrack.composeapp.generated.resources.check_in_missed_days
import macrotrack.composeapp.generated.resources.check_in_no_change
import macrotrack.composeapp.generated.resources.check_in_no_change_body
import macrotrack.composeapp.generated.resources.check_in_one_time
import macrotrack.composeapp.generated.resources.check_in_pace_gaining
import macrotrack.composeapp.generated.resources.check_in_pace_losing
import macrotrack.composeapp.generated.resources.check_in_pace_maintain
import macrotrack.composeapp.generated.resources.check_in_perk_burn_body
import macrotrack.composeapp.generated.resources.check_in_perk_burn_title
import macrotrack.composeapp.generated.resources.check_in_perk_target_body
import macrotrack.composeapp.generated.resources.check_in_perk_target_title
import macrotrack.composeapp.generated.resources.check_in_perk_trend_body
import macrotrack.composeapp.generated.resources.check_in_perk_trend_title
import macrotrack.composeapp.generated.resources.check_in_profile_estimate
import macrotrack.composeapp.generated.resources.check_in_suggestion
import macrotrack.composeapp.generated.resources.check_in_title
import macrotrack.composeapp.generated.resources.check_in_trend_weight
import macrotrack.composeapp.generated.resources.check_in_undo
import macrotrack.composeapp.generated.resources.check_in_unlock
import macrotrack.composeapp.generated.resources.check_in_weigh_ins
import macrotrack.composeapp.generated.resources.check_in_weigh_ins_tip
import macrotrack.composeapp.generated.resources.check_in_weight_gained_energy
import macrotrack.composeapp.generated.resources.check_in_weight_lost_energy
import macrotrack.composeapp.generated.resources.check_in_when_collecting
import macrotrack.composeapp.generated.resources.check_in_when_due
import macrotrack.composeapp.generated.resources.check_in_when_next
import macrotrack.composeapp.generated.resources.check_in_your_pace
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_info
import macrotrack.composeapp.generated.resources.ic_lock
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.CHECK_IN_INTERVAL_DAYS
import org.thelazybattley.macrotrack.domain.model.CHECK_IN_MIN_LOGGED_DAYS
import org.thelazybattley.macrotrack.domain.model.CHECK_IN_MIN_WEIGH_INS
import org.thelazybattley.macrotrack.domain.model.CHECK_IN_PACE_TOLERANCE_KG
import org.thelazybattley.macrotrack.domain.model.CHECK_IN_WINDOW_DAYS
import org.thelazybattley.macrotrack.domain.model.CheckInPace
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.WeeklyCheckIn
import org.thelazybattley.macrotrack.features.checkin.CheckInCallbacks
import org.thelazybattley.macrotrack.features.checkin.CheckInViewModel
import org.thelazybattley.macrotrack.features.checkin.CheckInViewState
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.features.pro.ProFeature
import org.thelazybattley.macrotrack.features.pro.ui.ProBadge
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.dashedBorder
import org.thelazybattley.macrotrack.ui.common.formatWeight
import org.thelazybattley.macrotrack.ui.common.formatWeightWithUnit
import org.thelazybattley.macrotrack.ui.common.toSignedWeight
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.max

/** Energy in a kg, and the round per-pound figure shown to imperial users. */
private const val KCAL_PER_KG = 7700
private const val KCAL_PER_LB = 3500

@Composable
fun CheckInScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val viewModel = koinViewModel<CheckInViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.isClosed) {
        if (viewState.isClosed) onBack()
    }
    CheckInScreen(modifier = modifier, viewState = viewState, callbacks = viewModel, onBack = onBack)
}

@Composable
private fun CheckInScreen(
    modifier: Modifier = Modifier,
    viewState: CheckInViewState,
    callbacks: CheckInCallbacks,
    onBack: () -> Unit
) {
    val pro = LocalProAccess.current
    val snapshot = viewState.snapshot
    val checkIn = snapshot?.checkIn
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colors.background)
            .statusBarsPadding()
    ) {
        Header(
            subtitle = when {
                checkIn == null -> null
                checkIn is WeeklyCheckIn.Collecting -> stringResource(resource = Res.string.check_in_when_collecting)
                viewState.isDue -> stringResource(
                    resource = Res.string.check_in_when_due,
                    snapshot.today.shortDate(),
                    snapshot.today.weekdayName()
                )
                else -> viewState.nextCheckIn?.let { stringResource(resource = Res.string.check_in_when_next, it.shortDate()) }
            },
            onBack = onBack
        )
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            when {
                checkIn == null -> Unit
                !pro.isActive -> LockedContent(
                    checkIn = checkIn,
                    onUnlock = { pro.openPaywall(ProFeature.CHECK_IN) }
                )
                checkIn is WeeklyCheckIn.Collecting -> CollectingContent(checkIn = checkIn)
                checkIn is WeeklyCheckIn.Result -> ResultContent(
                    result = checkIn,
                    goal = snapshot.userDetails.goal,
                    goalPace = snapshot.userDetails.goalPace,
                    macroGoals = snapshot.macroGoals,
                    viewState = viewState,
                    callbacks = callbacks
                )
            }
        }
    }
}

@Composable
private fun Header(subtitle: String?, onBack: () -> Unit) {
    Row(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        val shape = RoundedCornerShape(size = 14.dp)
        Box(
            modifier = Modifier
                .size(size = 44.dp)
                .clip(shape = shape)
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = shape)
                .clickable(role = Role.Button, onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_chevron_left),
                contentDescription = stringResource(resource = Res.string.back),
                tint = colors.ink,
                modifier = Modifier.size(size = 18.dp)
            )
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
            ) {
                Text(text = stringResource(resource = Res.string.check_in_title), style = typography.bold24, color = colors.ink)
                ProBadge()
            }
            subtitle?.let { Text(text = it, style = typography.regular11, color = colors.muted) }
        }
    }
}

@Composable
private fun LockedContent(checkIn: WeeklyCheckIn, onUnlock: () -> Unit) {
    HeroCard {
        IconTile(icon = Res.drawable.ic_lock)
        Text(text = stringResource(resource = Res.string.check_in_locked_title), style = typography.bold24, color = colors.ink)
        Text(text = stringResource(resource = Res.string.check_in_locked_body), style = typography.regular13, color = colors.muted)
    }
    SurfaceCard {
        listOf(
            Res.string.check_in_perk_burn_title to Res.string.check_in_perk_burn_body,
            Res.string.check_in_perk_target_title to Res.string.check_in_perk_target_body,
            Res.string.check_in_perk_trend_title to Res.string.check_in_perk_trend_body
        ).forEach { (title, body) ->
            Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_check),
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.padding(top = 2.dp).size(size = 18.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
                    Text(text = stringResource(resource = title), style = typography.bold14, color = colors.ink)
                    Text(text = stringResource(resource = body), style = typography.regular13, color = colors.muted)
                }
            }
        }
    }
    // Only the counts show: the burn itself is the Pro part.
    val (loggedDays, weighIns) = when (checkIn) {
        is WeeklyCheckIn.Result -> checkIn.loggedDays to checkIn.weighIns
        is WeeklyCheckIn.Collecting -> checkIn.loggedDays to checkIn.weighIns
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(color = colors.dashed, width = 1.dp, cornerRadius = 16.dp)
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        Text(text = stringResource(resource = Res.string.check_in_locked_ready), style = typography.bold12, color = colors.muted)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            Text(text = "– – – –", style = typography.bold24, color = colors.faint, modifier = Modifier.clearAndSetSemantics { })
            Text(text = stringResource(resource = Res.string.check_in_locked_burn), style = typography.regular13, color = colors.muted)
        }
        Text(
            text = stringResource(resource = Res.string.check_in_data_summary, loggedDays, CHECK_IN_WINDOW_DAYS, weighIns),
            style = typography.regular11,
            color = colors.muted
        )
    }
    PrimaryAction(text = stringResource(resource = Res.string.check_in_unlock), onClick = onUnlock)
    Text(
        text = stringResource(resource = Res.string.check_in_one_time),
        style = typography.regular11,
        color = colors.muted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CollectingContent(checkIn: WeeklyCheckIn.Collecting) {
    HeroCard {
        Text(text = stringResource(resource = Res.string.check_in_first), style = typography.regular11, color = colors.muted)
        Text(text = checkIn.firstCheckIn.shortDate(), style = typography.bold24, color = colors.ink)
        Text(text = stringResource(resource = Res.string.check_in_collecting_body), style = typography.regular13, color = colors.muted)
    }
    SurfaceCard {
        NeedRow(
            label = Res.string.check_in_days_logged,
            count = checkIn.loggedDays,
            needed = CHECK_IN_MIN_LOGGED_DAYS,
            tip = Res.string.check_in_days_logged_tip
        )
        NeedRow(
            label = Res.string.check_in_weigh_ins,
            count = checkIn.weighIns,
            needed = CHECK_IN_MIN_WEIGH_INS,
            tip = Res.string.check_in_weigh_ins_tip
        )
    }
    Text(text = stringResource(resource = Res.string.check_in_missed_days), style = typography.regular11, color = colors.muted)
}

@Composable
private fun NeedRow(label: StringResource, count: Int, needed: Int, tip: StringResource) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = stringResource(resource = label), style = typography.bold14, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            Text(
                text = stringResource(resource = Res.string.check_in_count_of, count.coerceAtMost(maximumValue = needed), needed),
                style = typography.regular13,
                color = colors.muted
            )
        }
        Bar(fraction = count.toFloat() / needed, color = colors.accent, track = colors.divider, height = 8)
        Text(text = stringResource(resource = tip), style = typography.regular11, color = colors.muted)
    }
}

@Composable
private fun ResultContent(
    result: WeeklyCheckIn.Result,
    goal: Goal,
    goalPace: Double,
    macroGoals: MacroGoals,
    viewState: CheckInViewState,
    callbacks: CheckInCallbacks
) {
    HeroCard {
        Column {
            Text(text = stringResource(resource = Res.string.check_in_measured_burn), style = typography.regular11, color = colors.muted)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(text = result.measuredBurn.withThousands(), style = typography.bold36, color = colors.ink)
                Text(
                    text = stringResource(resource = Res.string.check_in_kcal_a_day),
                    style = typography.regular13,
                    color = colors.muted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
        val most = max(result.measuredBurn, result.estimatedBurn).coerceAtLeast(minimumValue = 1).toFloat()
        BurnBar(label = Res.string.check_in_measured, value = result.measuredBurn, fraction = result.measuredBurn / most, color = colors.accent)
        BurnBar(label = Res.string.check_in_profile_estimate, value = result.estimatedBurn, fraction = result.estimatedBurn / most, color = colors.faint)
        Text(text = result.burnSummary(), style = typography.bold13, color = colors.ink)
    }

    Workings(result = result)

    SurfaceCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(resource = Res.string.check_in_your_pace),
                style = typography.bold14,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            PaceTag(pace = result.pace)
        }
        val monthly = abs(result.monthlyChange).formatWeightWithUnit()
        Text(
            text = when {
                goal == Goal.MAINTAIN_WEIGHT -> stringResource(resource = Res.string.check_in_pace_maintain, monthly)
                result.monthlyChange < 0 -> stringResource(resource = Res.string.check_in_pace_losing, monthly, goalPace.formatWeightWithUnit())
                else -> stringResource(resource = Res.string.check_in_pace_gaining, monthly, goalPace.formatWeightWithUnit())
            },
            style = typography.regular13,
            color = colors.muted
        )
    }

    when {
        !viewState.isDue -> Unit
        viewState.isApplied -> Applied(result = result, onUndo = callbacks::onUndo, onDone = callbacks::onKeep)
        result.isNoChange -> NoChange(result = result, onDone = callbacks::onKeep)
        else -> Suggestion(result = result, macroGoals = macroGoals, onApply = callbacks::onApply, onKeep = callbacks::onKeep)
    }

    val next = if (viewState.isDue) {
        viewState.snapshot?.today?.plus(period = DatePeriod(days = CHECK_IN_INTERVAL_DAYS))
    } else {
        viewState.nextCheckIn
    }
    Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_info),
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.padding(top = 1.dp).size(size = 16.dp)
        )
        Text(
            text = stringResource(
                resource = Res.string.check_in_footnote,
                result.loggedDays,
                result.weighIns,
                next?.shortDate().orEmpty()
            ),
            style = typography.regular11,
            color = colors.muted
        )
    }
}

/** The sum behind the burn: average eaten plus the trend change as energy. */
@Composable
private fun Workings(result: WeeklyCheckIn.Result) {
    val isImperial = LocalUnitSystem.current == UnitSystem.IMPERIAL
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(
            text = stringResource(
                resource = Res.string.check_in_how,
                result.windowStart.shortDate().substringAfter(delimiter = ", ") + " – " +
                    result.windowEnd.shortDate().substringAfter(delimiter = ", ")
            ),
            style = typography.bold12,
            color = colors.muted
        )
        val shape = RoundedCornerShape(size = 20.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = shape)
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = shape)
        ) {
            WorkingRow(
                label = stringResource(resource = Res.string.check_in_average_eaten),
                sub = stringResource(resource = Res.string.check_in_days_logged_of, result.loggedDays, CHECK_IN_WINDOW_DAYS),
                value = result.averageIntake.withThousands() + " kcal"
            )
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            WorkingRow(
                label = stringResource(resource = Res.string.check_in_trend_weight),
                sub = result.startWeight.formatWeight() + " → " + result.endWeight.formatWeightWithUnit(),
                value = result.weightChange.toSignedWeight() + " " + weightUnitLabel()
            )
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            WorkingRow(
                label = stringResource(
                    resource = if (result.energyFromWeight >= 0) Res.string.check_in_weight_lost_energy else Res.string.check_in_weight_gained_energy
                ),
                sub = stringResource(
                    resource = Res.string.check_in_energy_formula,
                    abs(result.weightChange).formatWeightWithUnit(),
                    (if (isImperial) KCAL_PER_LB else KCAL_PER_KG).withThousands(),
                    CHECK_IN_WINDOW_DAYS
                ),
                value = (if (result.energyFromWeight >= 0) "+" else "−") + abs(result.energyFromWeight).withThousands() + " kcal"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colors.background)
                    .heightIn(min = 52.dp)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(resource = Res.string.check_in_measured_burn_row),
                    style = typography.bold14,
                    color = colors.ink,
                    modifier = Modifier.weight(weight = 1f)
                )
                Text(text = result.measuredBurn.withThousands() + " kcal", style = typography.bold16, color = colors.ink)
            }
        }
    }
}

@Composable
private fun WorkingRow(label: String, sub: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = label, style = typography.medium13, color = colors.ink)
            Text(text = sub, style = typography.regular11, color = colors.muted)
        }
        Text(text = value, style = typography.bold15, color = colors.ink)
    }
}

@Composable
private fun Suggestion(result: WeeklyCheckIn.Result, macroGoals: MacroGoals, onApply: () -> Unit, onKeep: () -> Unit) {
    AccentCard {
        Text(text = stringResource(resource = Res.string.check_in_suggestion), style = typography.bold12, color = colors.accentInk)
        Text(text = result.suggestionTitle(), style = typography.bold18, color = colors.ink)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            Text(
                text = result.currentTarget.withThousands(),
                style = typography.bold18.copy(textDecoration = TextDecoration.LineThrough),
                color = colors.muted
            )
            Text(text = "→", style = typography.bold18, color = colors.accentInk)
            Text(text = result.suggestedTarget.withThousands(), style = typography.bold24, color = colors.ink)
            Text(text = stringResource(resource = Res.string.check_in_kcal_a_day), style = typography.regular13, color = colors.muted)
        }
        // Protein and fat come from body weight, so carbs take up the whole change.
        Text(
            text = stringResource(
                resource = Res.string.check_in_carbs_note,
                macroGoals.carbs,
                (macroGoals.carbs + result.targetChange / 4).coerceAtLeast(minimumValue = 0)
            ),
            style = typography.regular11,
            color = colors.muted
        )
    }
    PrimaryAction(text = stringResource(resource = Res.string.check_in_apply), onClick = onApply)
    TextAction(text = stringResource(resource = Res.string.check_in_keep, result.currentTarget.withThousands()), onClick = onKeep)
}

@Composable
private fun Applied(result: WeeklyCheckIn.Result, onUndo: () -> Unit, onDone: () -> Unit) {
    SurfaceCard {
        Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp), verticalAlignment = Alignment.Top) {
            IconTile(icon = Res.drawable.ic_check, size = 40, background = colors.accentSoft)
            Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
                Text(
                    text = stringResource(resource = Res.string.check_in_applied_title, result.suggestedTarget.withThousands()),
                    style = typography.bold15,
                    color = colors.ink
                )
                Text(text = stringResource(resource = Res.string.check_in_applied_body), style = typography.regular13, color = colors.muted)
            }
            Text(
                text = stringResource(resource = Res.string.check_in_undo),
                style = typography.bold13,
                color = colors.accentInk,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .clickable(role = Role.Button, onClick = onUndo)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
    PrimaryAction(text = stringResource(resource = Res.string.check_in_done), onClick = onDone)
}

@Composable
private fun NoChange(result: WeeklyCheckIn.Result, onDone: () -> Unit) {
    AccentCard {
        Text(text = stringResource(resource = Res.string.check_in_no_change), style = typography.bold12, color = colors.accentInk)
        Text(text = result.suggestionTitle(), style = typography.bold18, color = colors.ink)
        Text(
            text = stringResource(resource = Res.string.check_in_no_change_body, CHECK_IN_PACE_TOLERANCE_KG.formatWeightWithUnit()),
            style = typography.regular13,
            color = colors.muted
        )
    }
    PrimaryAction(text = stringResource(resource = Res.string.check_in_done), onClick = onDone)
}

@Composable
private fun BurnBar(label: StringResource, value: Int, fraction: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
        Text(text = stringResource(resource = label), style = typography.regular11, color = colors.muted, modifier = Modifier.width(width = 96.dp))
        Box(modifier = Modifier.weight(weight = 1f)) {
            Bar(fraction = fraction, color = color, track = colors.heroTrack, height = 10)
        }
        Text(
            text = value.withThousands(),
            style = typography.bold12,
            color = colors.ink,
            textAlign = TextAlign.End,
            modifier = Modifier.width(width = 44.dp)
        )
    }
}

@Composable
private fun Bar(fraction: Float, color: Color, track: Color, height: Int) {
    val shape = RoundedCornerShape(size = (height / 2).dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = height.dp)
            .clip(shape = shape)
            .background(color = track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = fraction.coerceIn(minimumValue = 0f, maximumValue = 1f))
                .fillMaxHeight()
                .clip(shape = shape)
                .background(color = color)
        )
    }
}

@Composable
private fun HeroCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.hero)
            .padding(all = 18.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) { content() }
}

@Composable
private fun SurfaceCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(size = 20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) { content() }
}

@Composable
private fun AccentCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(size = 20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.accentSoft)
            .border(width = 1.5.dp, color = colors.accent, shape = shape)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) { content() }
}

@Composable
private fun IconTile(
    icon: DrawableResource,
    size: Int = 44,
    background: Color = colors.surface
) {
    Box(
        modifier = Modifier
            .size(size = size.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = background),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(size = 20.dp))
    }
}

@Composable
private fun PrimaryAction(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 54.dp)
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.accent)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold16, color = colors.onAccent)
    }
}

@Composable
private fun TextAction(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 44.dp)
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold14, color = colors.muted)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCheckInScreen() {
    MacroTrackTheme {
        Column(modifier = Modifier.padding(all = 20.dp), verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
            ResultContent(
                result = WeeklyCheckIn.Result(
                    windowStart = LocalDate(year = 2026, month = 9, day = 17),
                    windowEnd = LocalDate(year = 2026, month = 10, day = 7),
                    loggedDays = 19,
                    weighIns = 9,
                    averageIntake = 2110,
                    startWeight = 75.0,
                    endWeight = 74.4,
                    energyFromWeight = 220,
                    measuredBurn = 2330,
                    estimatedBurn = 2450,
                    currentTarget = 2150,
                    suggestedTarget = 2030,
                    monthlyChange = -0.87,
                    pace = CheckInPace.SLOWER
                ),
                goal = Goal.LOSE_WEIGHT,
                goalPace = 1.2,
                macroGoals = MacroGoals(calories = 2150, protein = 148, carbs = 220, fat = 52),
                viewState = CheckInViewState(),
                callbacks = CheckInCallbacks.default()
            )
        }
    }
}
