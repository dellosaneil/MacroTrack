package org.thelazybattley.macrotrack.features.forecast.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.forecast_add_milestone
import macrotrack.composeapp.generated.resources.forecast_behind_by
import macrotrack.composeapp.generated.resources.forecast_ms_behind_count
import macrotrack.composeapp.generated.resources.forecast_ms_empty_body
import macrotrack.composeapp.generated.resources.forecast_ms_empty_title
import macrotrack.composeapp.generated.resources.forecast_ms_set
import macrotrack.composeapp.generated.resources.forecast_ms_sub_by
import macrotrack.composeapp.generated.resources.forecast_ms_sub_no_date
import macrotrack.composeapp.generated.resources.forecast_ms_sub_reached
import macrotrack.composeapp.generated.resources.ic_flag
import androidx.compose.ui.text.style.TextAlign
import macrotrack.composeapp.generated.resources.forecast_add_your_own
import macrotrack.composeapp.generated.resources.forecast_approx
import macrotrack.composeapp.generated.resources.forecast_delete_milestone
import macrotrack.composeapp.generated.resources.forecast_milestones_title
import macrotrack.composeapp.generated.resources.forecast_ms_behind_preview
import macrotrack.composeapp.generated.resources.forecast_ms_by_optional
import macrotrack.composeapp.generated.resources.forecast_ms_footnote
import macrotrack.composeapp.generated.resources.forecast_ms_name
import macrotrack.composeapp.generated.resources.forecast_ms_name_placeholder
import macrotrack.composeapp.generated.resources.forecast_ms_no_date
import macrotrack.composeapp.generated.resources.forecast_ms_on_track_preview
import macrotrack.composeapp.generated.resources.forecast_ms_over
import macrotrack.composeapp.generated.resources.forecast_ms_reached
import macrotrack.composeapp.generated.resources.forecast_ms_under
import macrotrack.composeapp.generated.resources.forecast_ms_weight_label
import macrotrack.composeapp.generated.resources.forecast_new_milestone
import macrotrack.composeapp.generated.resources.forecast_on_track
import macrotrack.composeapp.generated.resources.forecast_share_card
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_trash
import macrotrack.composeapp.generated.resources.milestone_over
import macrotrack.composeapp.generated.resources.milestone_under
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.KeypadField
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.GoalForecast
import org.thelazybattley.macrotrack.domain.model.Milestone
import org.thelazybattley.macrotrack.domain.model.MilestoneTrack
import org.thelazybattley.macrotrack.features.forecast.GoalForecastCallbacks
import org.thelazybattley.macrotrack.features.forecast.GoalForecastViewModel
import org.thelazybattley.macrotrack.features.forecast.GoalForecastViewState
import org.thelazybattley.macrotrack.features.share.ShareCardKind
import org.thelazybattley.macrotrack.ui.common.CommonDatePickerDialog
import org.thelazybattley.macrotrack.ui.common.CommonKeypadSheet
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.weightInputText
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs

private const val WEIGHT_FIELD = "milestone_weight"

@Composable
fun ForecastMilestonesScreen(
    onPopBackStack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val viewModel = koinViewModel<GoalForecastViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    ForecastMilestonesScreen(viewState = viewState, callbacks = viewModel, onBack = onPopBackStack, onNavigate = onNavigate)
}

@Composable
private fun ForecastMilestonesScreen(
    viewState: GoalForecastViewState,
    callbacks: GoalForecastCallbacks,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var showAdd by remember { mutableStateOf(value = false) }
    val forecast = viewState.snapshot?.forecast
    val milestones = viewState.snapshot?.milestones.orEmpty()
    ForecastScaffold(
        title = Res.string.forecast_milestones_title,
        onBack = onBack,
        overlay = {
            if (showAdd && forecast != null) {
                AddMilestoneSheet(
                    forecast = forecast,
                    onAdd = { name, weight, byDate ->
                        callbacks.onAddMilestone(name = name, weightKg = weight, byDate = byDate)
                        showAdd = false
                    },
                    onDismiss = { showAdd = false }
                )
            }
        }
    ) {
        forecast ?: return@ForecastScaffold
        if (milestones.isEmpty()) {
            EmptyMilestones()
        } else {
            ForecastStatTiles(
                stats = listOf(
                    ForecastStat(label = stringResource(resource = Res.string.forecast_ms_set), value = milestones.size.toString()),
                    ForecastStat(label = stringResource(resource = Res.string.forecast_ms_reached), value = milestones.count { it.isReached }.toString()),
                    ForecastStat(
                        label = stringResource(resource = Res.string.forecast_ms_behind_count),
                        value = milestones.count { it.track == MilestoneTrack.BEHIND }.toString()
                    )
                )
            )
            Column {
                milestones.forEachIndexed { index, milestone ->
                    MilestoneRow(
                        forecast = forecast,
                        milestone = milestone,
                        isFirst = index == 0,
                        isLast = index == milestones.lastIndex,
                        isNextReached = milestones.getOrNull(index = index + 1)?.isReached == true,
                        onShare = { onNavigate(AppDestinations.Root.ShareCard.createRoute(kind = ShareCardKind.WEIGHT.name)) },
                        onDelete = { callbacks.onDeleteMilestone(id = milestone.custom.id) }
                    )
                }
            }
        }
        if (milestones.isEmpty()) {
            CommonPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = Res.string.forecast_add_your_own,
                onClick = { showAdd = true }
            )
        } else {
            ForecastDashedButton(
                text = stringResource(resource = Res.string.forecast_add_your_own),
                icon = Res.drawable.ic_plus,
                onClick = { showAdd = true }
            )
        }
        ForecastFootnote(text = stringResource(resource = Res.string.forecast_ms_footnote))
    }
}

@Composable
private fun EmptyMilestones() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 40.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Box(
            modifier = Modifier.size(size = 88.dp).background(color = colors.warmSoft, shape = RoundedCornerShape(size = 28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(resource = Res.drawable.ic_flag), contentDescription = null, tint = colors.warmInk, modifier = Modifier.size(size = 40.dp))
        }
        Text(text = stringResource(resource = Res.string.forecast_ms_empty_title), style = typography.bold24, color = colors.ink)
        Text(
            text = stringResource(resource = Res.string.forecast_ms_empty_body),
            style = typography.regular13,
            color = colors.muted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MilestoneRow(
    forecast: GoalForecast,
    milestone: Milestone,
    isFirst: Boolean,
    isLast: Boolean,
    isNextReached: Boolean,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val today = forecast.today
    val name = milestone.custom.name
    val isBehind = milestone.track == MilestoneTrack.BEHIND
    Row(modifier = Modifier.fillMaxWidth().height(intrinsicSize = IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
        Column(modifier = Modifier.width(width = 30.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(width = 2.dp)
                    .height(height = 14.dp)
                    .background(color = if (isFirst) Color.Transparent else if (milestone.isReached) colors.accent else colors.line)
            )
            MilestoneNode(isReached = milestone.isReached, isBehind = isBehind)
            Box(
                modifier = Modifier
                    .width(width = 2.dp)
                    .weight(weight = 1f)
                    .heightIn(min = 10.dp)
                    .background(color = if (isLast) Color.Transparent else if (isNextReached) colors.accent else colors.line)
            )
        }
        val shape = RoundedCornerShape(size = 14.dp)
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .padding(top = 4.dp, bottom = 10.dp)
                .clip(shape = shape)
                .background(color = colors.surface)
                .border(width = if (isBehind) 1.5.dp else 1.dp, color = if (isBehind) colors.carbs else colors.line, shape = shape)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = name, style = typography.bold14, color = colors.ink, modifier = Modifier.weight(weight = 1f))
                val whenText = milestone.reachedOn?.forecastDate(today = today)
                    ?: milestone.expectedOn?.let { stringResource(resource = Res.string.forecast_approx, it.forecastDate(today = today)) }
                if (whenText != null) {
                    Text(text = whenText, style = typography.bold13, color = if (milestone.isReached) colors.accentInk else colors.muted)
                }
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_trash),
                    contentDescription = stringResource(resource = Res.string.forecast_delete_milestone, name),
                    tint = colors.faint,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(size = 32.dp)
                        .clip(shape = CircleShape)
                        .clickable(role = Role.Button, onClick = onDelete)
                        .padding(all = 7.dp)
                )
            }
            val under = stringResource(
                resource = if (forecast.direction < 0) Res.string.milestone_under else Res.string.milestone_over,
                milestone.weight.oneDecimalWithUnit()
            )
            val toGo = abs(forecast.trendWeight - milestone.weight).oneDecimalWithUnit()
            val byDate = milestone.custom.byDate
            Text(
                text = when {
                    milestone.isReached -> stringResource(resource = Res.string.forecast_ms_sub_reached, under)
                    byDate != null -> stringResource(resource = Res.string.forecast_ms_sub_by, under, byDate.toShortDayMonthDay(), toGo)
                    else -> stringResource(resource = Res.string.forecast_ms_sub_no_date, under, toGo)
                },
                style = typography.regular11,
                color = colors.muted
            )
            milestone.track?.let { track -> TrackTag(track = track, behindByKg = milestone.behindByKg) }
            if (milestone.isReached) {
                Text(
                    text = stringResource(resource = Res.string.forecast_share_card),
                    style = typography.bold12,
                    color = colors.accentInk,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(shape = RoundedCornerShape(size = 6.dp))
                        .clickable(role = Role.Button, onClick = onShare)
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun MilestoneNode(isReached: Boolean, isBehind: Boolean) {
    val modifier = Modifier.size(size = 26.dp)
    if (isReached) {
        Box(modifier = modifier.background(color = colors.accent, shape = CircleShape), contentAlignment = Alignment.Center) {
            Icon(painter = painterResource(resource = Res.drawable.ic_check), contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(size = 14.dp))
        }
    } else {
        Box(
            modifier = modifier
                .background(color = colors.surface, shape = CircleShape)
                .border(width = 2.dp, color = if (isBehind) colors.carbs else colors.accent, shape = CircleShape)
        )
    }
}

@Composable
private fun TrackTag(track: MilestoneTrack, behindByKg: Double) {
    val isOnTrack = track == MilestoneTrack.ON_TRACK
    Text(
        text = if (isOnTrack) {
            stringResource(resource = Res.string.forecast_on_track)
        } else {
            stringResource(resource = Res.string.forecast_behind_by, behindByKg.oneDecimalWithUnit())
        },
        style = typography.bold11,
        color = if (isOnTrack) colors.accentInk else colors.warmInk,
        modifier = Modifier
            .padding(top = 4.dp)
            .clip(shape = RoundedCornerShape(size = 7.dp))
            .background(color = if (isOnTrack) colors.accentSoft else colors.warmSoft)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMilestoneSheet(
    forecast: GoalForecast,
    onAdd: (name: String, weightKg: Double, byDate: LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    val unitSystem = LocalUnitSystem.current
    val unit = weightUnitLabel()
    var name by remember { mutableStateOf(value = "") }
    // Starts halfway from today's trend to the goal.
    var weightKg by remember { mutableStateOf(value = (forecast.trendWeight + forecast.targetWeight) / 2) }
    var byDate by remember { mutableStateOf<LocalDate?>(value = null) }
    var showKeypad by remember { mutableStateOf(value = false) }
    var showDatePicker by remember { mutableStateOf(value = false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Text(text = stringResource(resource = Res.string.forecast_new_milestone), style = typography.bold18, color = colors.ink)
            Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(text = stringResource(resource = Res.string.forecast_ms_name), style = typography.medium11, color = colors.muted)
                CommonTextField(
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = stringResource(resource = Res.string.forecast_ms_name_placeholder),
                    textValue = name,
                    onValueChanged = { name = it.take(n = 40) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                SheetValueButton(
                    modifier = Modifier.weight(weight = 1f),
                    label = stringResource(resource = if (forecast.direction < 0) Res.string.forecast_ms_under else Res.string.forecast_ms_over),
                    value = weightKg.oneDecimalWithUnit(),
                    onClick = { showKeypad = true }
                )
                SheetValueButton(
                    modifier = Modifier.weight(weight = 1f),
                    label = stringResource(resource = Res.string.forecast_ms_by_optional),
                    value = byDate?.toShortDayMonthDay() ?: stringResource(resource = Res.string.forecast_ms_no_date),
                    onClick = { showDatePicker = true }
                )
            }
            val date = byDate
            val projected = date?.let { forecast.projectedOn(date = it) }
            if (date != null && projected != null) {
                val isOnTrack = (projected - weightKg) * forecast.direction >= 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(size = 14.dp))
                        .background(color = if (isOnTrack) colors.hero else colors.warmSoft)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
                ) {
                    Box(modifier = Modifier.size(size = 10.dp).background(color = if (isOnTrack) colors.accent else colors.carbs, shape = CircleShape))
                    Text(
                        text = stringResource(
                            resource = if (isOnTrack) Res.string.forecast_ms_on_track_preview else Res.string.forecast_ms_behind_preview,
                            date.forecastDate(today = forecast.today),
                            projected.oneDecimalWithUnit()
                        ),
                        style = typography.regular13,
                        color = colors.ink
                    )
                }
            }
            CommonPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = Res.string.forecast_add_milestone,
                enabled = name.isNotBlank(),
                onClick = { onAdd(name, weightKg, byDate) }
            )
        }
    }
    if (showKeypad) {
        CommonKeypadSheet(
            fields = listOf(
                KeypadField(
                    id = WEIGHT_FIELD,
                    label = stringResource(resource = Res.string.forecast_ms_weight_label),
                    unit = unit,
                    value = unitSystem.weightInputText(kg = weightKg),
                    wholeDigits = 3,
                    decimals = 1,
                    min = 1.0
                )
            ),
            openId = WEIGHT_FIELD,
            onOpen = {},
            onCommit = { _, text ->
                text.toDoubleOrNull()?.let { weightKg = unitSystem.weightToKg(value = it) }
                showKeypad = false
            },
            onDismiss = { showKeypad = false }
        )
    }
    if (showDatePicker) {
        CommonDatePickerDialog(
            selected = byDate ?: getCurrentDate().plus(period = DatePeriod(days = 30)),
            minDate = getCurrentDate().plus(period = DatePeriod(days = 1)),
            maxDate = null,
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                byDate = date
                showDatePicker = false
            }
        )
    }
}

@Composable
internal fun SheetValueButton(modifier: Modifier = Modifier, label: String, value: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = label, style = typography.medium11, color = colors.muted)
        Text(text = value, style = typography.bold18, color = colors.ink)
    }
}
