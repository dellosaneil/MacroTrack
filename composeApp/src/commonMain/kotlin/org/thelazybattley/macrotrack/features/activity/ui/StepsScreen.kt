package org.thelazybattley.macrotrack.features.activity.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_flame
import macrotrack.composeapp.generated.resources.ic_footprints
import macrotrack.composeapp.generated.resources.ic_minus
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_target
import macrotrack.composeapp.generated.resources.save
import macrotrack.composeapp.generated.resources.steps_active_time
import macrotrack.composeapp.generated.resources.steps_allow
import macrotrack.composeapp.generated.resources.steps_allow_body
import macrotrack.composeapp.generated.resources.steps_allow_settings
import macrotrack.composeapp.generated.resources.steps_allow_title
import macrotrack.composeapp.generated.resources.steps_burned
import macrotrack.composeapp.generated.resources.steps_burned_open
import macrotrack.composeapp.generated.resources.steps_busiest_hour
import macrotrack.composeapp.generated.resources.steps_chart_day
import macrotrack.composeapp.generated.resources.steps_chart_week
import macrotrack.composeapp.generated.resources.steps_daily_average
import macrotrack.composeapp.generated.resources.steps_distance
import macrotrack.composeapp.generated.resources.steps_goal_button
import macrotrack.composeapp.generated.resources.steps_goal_button_description
import macrotrack.composeapp.generated.resources.steps_goal_close_with_walk
import macrotrack.composeapp.generated.resources.steps_goal_days_hit
import macrotrack.composeapp.generated.resources.steps_goal_fewer
import macrotrack.composeapp.generated.resources.steps_goal_keep
import macrotrack.composeapp.generated.resources.steps_goal_last_days
import macrotrack.composeapp.generated.resources.steps_goal_more
import macrotrack.composeapp.generated.resources.steps_goal_note_average
import macrotrack.composeapp.generated.resources.steps_goal_note_big_jump
import macrotrack.composeapp.generated.resources.steps_goal_note_easy
import macrotrack.composeapp.generated.resources.steps_goal_note_near
import macrotrack.composeapp.generated.resources.steps_goal_nudge
import macrotrack.composeapp.generated.resources.steps_goal_nudge_body
import macrotrack.composeapp.generated.resources.steps_goal_over
import macrotrack.composeapp.generated.resources.steps_goal_reached
import macrotrack.composeapp.generated.resources.steps_goal_save
import macrotrack.composeapp.generated.resources.steps_goal_sheet_body
import macrotrack.composeapp.generated.resources.steps_goal_sheet_title
import macrotrack.composeapp.generated.resources.steps_goal_suggested
import macrotrack.composeapp.generated.resources.steps_goal_tap_to_type
import macrotrack.composeapp.generated.resources.steps_goal_to_go
import macrotrack.composeapp.generated.resources.steps_goal_value_description
import macrotrack.composeapp.generated.resources.steps_goal_would_hit
import macrotrack.composeapp.generated.resources.steps_history_body
import macrotrack.composeapp.generated.resources.steps_history_title
import macrotrack.composeapp.generated.resources.steps_hour_range
import macrotrack.composeapp.generated.resources.steps_hour_still_to_come
import macrotrack.composeapp.generated.resources.steps_info
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.steps_count
import macrotrack.composeapp.generated.resources.steps_history_body_count
import macrotrack.composeapp.generated.resources.steps_label_unit_steps
import macrotrack.composeapp.generated.resources.time_hour_am
import macrotrack.composeapp.generated.resources.time_hour_pm
import macrotrack.composeapp.generated.resources.steps_of_goal
import macrotrack.composeapp.generated.resources.steps_recent
import macrotrack.composeapp.generated.resources.steps_recent_empty
import macrotrack.composeapp.generated.resources.steps_record_heading
import macrotrack.composeapp.generated.resources.steps_recording_return
import macrotrack.composeapp.generated.resources.steps_see_all
import macrotrack.composeapp.generated.resources.steps_session_line
import macrotrack.composeapp.generated.resources.steps_title
import macrotrack.composeapp.generated.resources.steps_unavailable
import macrotrack.composeapp.generated.resources.time_12_am
import macrotrack.composeapp.generated.resources.time_12_pm
import macrotrack.composeapp.generated.resources.time_11_pm
import macrotrack.composeapp.generated.resources.time_6_am
import macrotrack.composeapp.generated.resources.time_6_pm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.KeypadField
import org.thelazybattley.macrotrack.core.platform.rememberMotionPermissionState
import org.thelazybattley.macrotrack.core.platform.rememberNotificationPermissionState
import org.thelazybattley.macrotrack.core.toDayMonthDay
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.ActivitySession
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.StepGoal
import org.thelazybattley.macrotrack.domain.model.STEPS_PER_WALKING_MINUTE
import org.thelazybattley.macrotrack.features.activity.ActivityRecorder
import org.thelazybattley.macrotrack.features.activity.GoalDraft
import org.thelazybattley.macrotrack.features.activity.Recording
import org.thelazybattley.macrotrack.features.activity.StepsCallbacks
import org.thelazybattley.macrotrack.features.activity.StepsRange
import org.thelazybattley.macrotrack.features.activity.StepsViewModel
import org.thelazybattley.macrotrack.features.activity.StepsViewState
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.features.pro.ProFeature
import org.thelazybattley.macrotrack.ui.common.CommonKeypadSheet
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.max

/** Bars on the week chart are scaled to at least this many steps, so a quiet week doesn't look full. */
private const val WEEK_CHART_MIN_SCALE = 12_000

@Composable
fun StepsScreen(
    onBack: () -> Unit,
    onRecord: (ActivityType) -> Unit,
    onOpenSession: (Long) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBurn: () -> Unit
) {
    val viewModel = koinViewModel<StepsViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val recorder = koinInject<ActivityRecorder>()
    val recording by recorder.state.collectAsStateWithLifecycle()
    StepsScreen(
        viewState = viewState,
        recording = recording,
        callbacks = viewModel,
        onBack = onBack,
        onRecord = onRecord,
        onOpenSession = onOpenSession,
        onOpenHistory = onOpenHistory,
        onOpenBurn = onOpenBurn
    )
}

@Composable
private fun StepsScreen(
    viewState: StepsViewState,
    recording: Recording?,
    callbacks: StepsCallbacks,
    onBack: () -> Unit,
    onRecord: (ActivityType) -> Unit,
    onOpenSession: (Long) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBurn: () -> Unit
) {
    val pro = LocalProAccess.current
    val motion = rememberMotionPermissionState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.background)
            .statusBarsPadding()
            .verticalScroll(state = rememberScrollState())
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        ActivityHeader(
            overline = viewState.today.toDayMonthDay(),
            title = stringResource(resource = Res.string.steps_title),
            onBack = onBack,
            trailing = { GoalButton(goal = viewState.goal, onClick = callbacks::onOpenGoal) }
        )
        when {
            !viewState.isSensorAvailable -> InfoNote(text = stringResource(resource = Res.string.steps_unavailable))
            !motion.isGranted -> AllowStepsCard(isPermanentlyDenied = motion.isPermanentlyDenied, onAllow = motion.request)
        }
        StepsHero(
            viewState = viewState,
            isPro = pro.isActive,
            onOpenBurn = onOpenBurn,
            onLockedBurn = { pro.openPaywall(ProFeature.CALORIES_BURNED) }
        )
        StepsChartCard(viewState = viewState, callbacks = callbacks)
        RecordButtons(recording = recording, onRecord = onRecord)
        RecentSessions(sessions = viewState.recentSessions, onOpenSession = onOpenSession, onSeeAll = onOpenHistory)
        HistoryCard(sessionsLast30 = viewState.sessionsLast30, sessionsPerDay = viewState.sessionsPerDay, onClick = onOpenHistory)
        InfoNote(text = stringResource(resource = Res.string.steps_info))
    }
    viewState.goalDraft?.let { draft ->
        GoalSheets(viewState = viewState, draft = draft, callbacks = callbacks)
    }
}

@Composable
private fun GoalButton(goal: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 12.dp)
    val description = stringResource(resource = Res.string.steps_goal_button_description, goal.withThousands())
    Row(
        modifier = Modifier
            .height(height = 40.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        Icon(painter = painterResource(resource = Res.drawable.ic_target), contentDescription = null, tint = colors.ink, modifier = Modifier.size(size = 15.dp))
        Text(
            text = stringResource(resource = Res.string.steps_goal_button, goal.withThousands()),
            style = typography.bold13,
            color = colors.ink,
            modifier = Modifier.clearAndSetSemantics {}
        )
    }
}

@Composable
private fun AllowStepsCard(isPermanentlyDenied: Boolean, onAllow: () -> Unit) {
    ActivityCard {
        Text(text = stringResource(resource = Res.string.steps_allow_title), style = typography.bold15, color = colors.ink)
        Text(text = stringResource(resource = Res.string.steps_allow_body), style = typography.regular13, color = colors.muted)
        CommonPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(resource = if (isPermanentlyDenied) Res.string.steps_allow_settings else Res.string.steps_allow),
            onClick = onAllow
        )
    }
}

@Composable
private fun StepsHero(
    viewState: StepsViewState,
    isPro: Boolean,
    onOpenBurn: () -> Unit,
    onLockedBurn: () -> Unit
) {
    val steps = viewState.todaySteps
    val goal = viewState.goal
    val profile = viewState.profile
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.hero)
            .padding(all = 18.dp),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 16.dp)) {
            Box(modifier = Modifier.size(size = 120.dp), contentAlignment = Alignment.Center) {
                ProgressRing(modifier = Modifier.fillMaxSize(), progress = steps / goal.toFloat(), strokeWidth = 11.dp)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_footprints),
                        contentDescription = null,
                        tint = colors.accentInk,
                        modifier = Modifier.size(size = 18.dp)
                    )
                    Text(text = steps.withThousands(), style = typography.bold24, color = colors.ink)
                    Text(text = stringResource(resource = Res.string.steps_of_goal, goal.withThousands()), style = typography.regular11, color = colors.muted)
                }
            }
            Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                val isReached = steps >= goal
                Text(
                    text = if (isReached) {
                        stringResource(resource = Res.string.steps_goal_reached)
                    } else {
                        stringResource(resource = Res.string.steps_goal_to_go, (goal - steps).withThousands())
                    },
                    style = typography.bold18,
                    color = colors.ink
                )
                Text(
                    text = if (isReached) {
                        stringResource(resource = Res.string.steps_goal_over, (steps - goal).withThousands())
                    } else {
                        stringResource(resource = Res.string.steps_goal_close_with_walk, (goal - steps + STEPS_PER_WALKING_MINUTE - 1) / STEPS_PER_WALKING_MINUTE)
                    },
                    style = typography.regular13,
                    color = colors.muted
                )
                Row(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(size = 8.dp))
                        .background(color = colors.warmSoft)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
                ) {
                    Icon(painter = painterResource(resource = Res.drawable.ic_flame), contentDescription = null, tint = colors.warmInk, modifier = Modifier.size(size = 13.dp))
                    Text(text = stringResource(resource = Res.string.steps_goal_days_hit, viewState.daysHit), style = typography.bold12, color = colors.warmInk)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            StatTile(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = Res.string.steps_distance),
                value = distanceValue(meters = viewState.day.distanceMeters(profile = profile)),
                unit = distanceUnit(),
                showBorder = false
            )
            StatTile(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = Res.string.steps_active_time),
                value = shortDuration(minutes = viewState.day.activeMinutes),
                showBorder = false
            )
            if (isPro && profile != null) {
                val kcal = viewState.day.activeKcal(profile = profile)
                val description = stringResource(resource = Res.string.steps_burned_open, kcal)
                StatTile(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .clip(shape = RoundedCornerShape(size = 14.dp))
                        .clickable(role = Role.Button, onClick = onOpenBurn)
                        .semantics(mergeDescendants = true) { contentDescription = description },
                    label = stringResource(resource = Res.string.steps_burned),
                    value = "≈" + kcal.withThousands(),
                    unit = stringResource(resource = Res.string.kcal_text),
                    showBorder = false
                )
            } else {
                StatTile(
                    modifier = Modifier.weight(weight = 1f),
                    label = stringResource(resource = Res.string.steps_burned),
                    value = "",
                    isLocked = !isPro,
                    onLockedClick = onLockedBurn,
                    showBorder = false
                )
            }
        }
    }
}

@Composable
private fun StepsChartCard(viewState: StepsViewState, callbacks: StepsCallbacks) {
    ActivityCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            SegmentedTabs(
                options = listOf(
                    StepsRange.DAY to stringResource(resource = Res.string.steps_chart_day),
                    StepsRange.WEEK to stringResource(resource = Res.string.steps_chart_week)
                ),
                selected = viewState.range,
                onSelect = callbacks::onRangeSelected
            )
            Box(modifier = Modifier.weight(weight = 1f))
            val (value, label) = chartReadout(viewState = viewState)
            Column(horizontalAlignment = Alignment.End) {
                Text(text = value, style = typography.bold16, color = colors.ink)
                Text(text = label, style = typography.regular11, color = colors.muted, textAlign = TextAlign.End)
            }
        }
        when (viewState.range) {
            StepsRange.DAY -> DayChart(viewState = viewState, onHourSelected = callbacks::onHourSelected)
            StepsRange.WEEK -> WeekChart(viewState = viewState, onDaySelected = callbacks::onDaySelected)
        }
    }
}

@Composable
private fun chartReadout(viewState: StepsViewState): Pair<String, String> = when (viewState.range) {
    StepsRange.DAY -> {
        val selected = viewState.selectedHour
        when {
            selected == null -> viewState.busiestHour?.let {
                viewState.hours[it].withThousands() to stringResource(resource = Res.string.steps_busiest_hour, hourLabel(hour = it))
            } ?: ("–" to stringResource(resource = Res.string.steps_chart_day))
            selected > currentHour() -> "–" to stringResource(resource = Res.string.steps_hour_still_to_come, hourLabel(hour = selected))
            else -> viewState.hours[selected].withThousands() to
                stringResource(resource = Res.string.steps_hour_range, hourLabel(hour = selected), hourLabel(hour = (selected + 1) % 24))
        }
    }
    StepsRange.WEEK -> {
        val selected = viewState.selectedDay
        if (selected == null) {
            viewState.weekAverage.withThousands() to stringResource(resource = Res.string.steps_daily_average, viewState.goal.withThousands())
        } else {
            val day = viewState.week[selected]
            day.steps.withThousands() to relativeDay(date = day.date)
        }
    }
}

@Composable
private fun DayChart(viewState: StepsViewState, onHourSelected: (Int) -> Unit) {
    val now = currentHour()
    val peak = max(viewState.hours.maxOrNull() ?: 0, 1)
    val sessionHours = viewState.day.sessions.flatMap { (it.startTime.hour..it.endTime.hour).toList() }.toSet()
    Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(height = 128.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(space = 3.dp)
        ) {
            viewState.hours.forEachIndexed { hour, steps ->
                val isFuture = hour > now
                val isSelected = viewState.selectedHour == hour
                val label = if (isFuture) {
                    stringResource(resource = Res.string.steps_hour_still_to_come, hourLabel(hour = hour))
                } else {
                    hourLabel(hour = hour) + ", " + pluralStringResource(resource = Res.plurals.steps_count, quantity = steps, steps.withThousands())
                }
                BarButton(
                    modifier = Modifier.weight(weight = 1f),
                    fraction = if (isFuture) 0f else steps / peak.toFloat(),
                    maxHeight = 124.dp,
                    color = when {
                        isFuture -> colors.line
                        isSelected -> colors.ink
                        hour in sessionHours -> colors.accent
                        else -> colors.heroTrack
                    },
                    description = label,
                    onClick = { onHourSelected(hour) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(Res.string.time_12_am, Res.string.time_6_am, Res.string.time_12_pm, Res.string.time_6_pm, Res.string.time_11_pm).forEach {
                Text(text = stringResource(resource = it), style = typography.regular11, color = colors.muted)
            }
        }
    }
}

@Composable
private fun WeekChart(viewState: StepsViewState, onDaySelected: (Int) -> Unit) {
    val scale = maxOf(WEEK_CHART_MIN_SCALE, viewState.goal, viewState.week.maxOfOrNull { it.steps } ?: 0).toFloat()
    val goalFraction = viewState.goal / scale
    val line = colors.accent
    Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(height = 128.dp)) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
            ) {
                viewState.week.forEachIndexed { index, day ->
                    val isHit = day.steps >= viewState.goal
                    BarButton(
                        modifier = Modifier.weight(weight = 1f),
                        fraction = day.steps / scale,
                        maxHeight = 128.dp,
                        color = when {
                            viewState.selectedDay == index -> colors.ink
                            isHit -> colors.accent
                            else -> colors.heroTrack
                        },
                        cornerRadius = 6.dp,
                        description = relativeDay(date = day.date) + ", " +
                            pluralStringResource(resource = Res.plurals.steps_count, quantity = day.steps, day.steps.withThousands()),
                        onClick = { onDaySelected(index) }
                    )
                }
            }
            Canvas(modifier = Modifier.matchParentSize()) {
                val y = size.height * (1 - goalFraction)
                drawLine(
                    color = line,
                    start = Offset(x = 0f, y = y),
                    end = Offset(x = size.width, y = y),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
                )
            }
            Text(
                text = viewState.goal.withThousands(),
                style = typography.bold10,
                color = colors.accentInk,
                modifier = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .padding(top = (128.dp * (1 - goalFraction) - 16.dp).coerceAtLeast(minimumValue = 0.dp))
                    .background(color = colors.surface)
                    .padding(horizontal = 4.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            viewState.week.forEach { day ->
                Text(
                    text = day.date.dayOfWeek.name.take(n = 1),
                    style = typography.regular11,
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(weight = 1f)
                )
            }
        }
    }
}

/** A tappable chart bar, its full height a target. */
@Composable
internal fun BarButton(
    modifier: Modifier = Modifier,
    fraction: Float,
    maxHeight: Dp,
    color: Color,
    description: String,
    cornerRadius: Dp = 3.dp,
    minHeight: Dp = 3.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height = maxHeight)
            .clickable(role = Role.Button, onClickLabel = null, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description },
        contentAlignment = Alignment.BottomCenter
    ) {
        ChartBar(modifier = Modifier.fillMaxWidth(), fraction = fraction, maxHeight = maxHeight, color = color, minHeight = minHeight, cornerRadius = cornerRadius)
    }
}

@Composable
private fun RecordButtons(recording: Recording?, onRecord: (ActivityType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(text = stringResource(resource = Res.string.steps_record_heading), style = typography.bold12, color = colors.muted)
        if (recording != null) {
            CommonPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(resource = Res.string.steps_recording_return, stringResource(resource = recording.type.title).lowercase()),
                onClick = { onRecord(recording.type) }
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
                ActivityType.entries.forEach { type ->
                    Row(
                        modifier = Modifier
                            .weight(weight = 1f)
                            .height(height = 56.dp)
                            .clip(shape = RoundedCornerShape(size = 16.dp))
                            .background(color = colors.accent)
                            .clickable(role = Role.Button) { onRecord(type) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
                    ) {
                        Icon(painter = painterResource(resource = type.icon), contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(size = 22.dp))
                        Text(text = stringResource(resource = type.title), style = typography.bold16, color = colors.onAccent)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentSessions(sessions: List<ActivitySession>, onOpenSession: (Long) -> Unit, onSeeAll: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(modifier = Modifier.weight(weight = 1f), text = stringResource(resource = Res.string.steps_recent))
            if (sessions.isNotEmpty()) {
                Text(
                    text = stringResource(resource = Res.string.steps_see_all),
                    style = typography.bold13,
                    color = colors.accentInk,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(size = 8.dp))
                        .clickable(role = Role.Button, onClick = onSeeAll)
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                )
            }
        }
        if (sessions.isEmpty()) {
            Text(
                text = stringResource(resource = Res.string.steps_recent_empty),
                style = typography.regular13,
                color = colors.muted,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 20.dp))
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
                    .padding(all = 16.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 20.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
            ) {
                sessions.forEachIndexed { index, session ->
                    SessionRow(session = session, thumbnailSize = 64.dp, onClick = { onOpenSession(session.id) })
                    if (index != sessions.lastIndex) HorizontalDivider(thickness = 1.dp, color = colors.divider)
                }
            }
        }
    }
}

/** A walk or run in a list: its route, name, when, and how far, how many steps and how long. */
@Composable
internal fun SessionRow(
    session: ActivitySession,
    thumbnailSize: Dp,
    detail: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        RouteSketch(
            modifier = Modifier
                .size(size = thumbnailSize)
                .clip(shape = RoundedCornerShape(size = 14.dp))
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp)),
            points = session.route,
            lineWidth = 3.dp,
            padding = 8.dp
        )
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                ActivityTypeBadge(type = session.type)
                Text(
                    text = session.displayName(),
                    style = typography.bold15,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(text = relativeDay(date = session.date) + " · " + clockTime(time = session.startTime), style = typography.regular11, color = colors.muted)
            Text(
                text = detail ?: stringResource(
                    resource = Res.string.steps_session_line,
                    distanceText(meters = session.distanceMeters),
                    session.steps.withThousands(),
                    shortDuration(minutes = session.movingSeconds / 60)
                ),
                style = typography.medium13,
                color = colors.ink
            )
        }
        Icon(painter = painterResource(resource = Res.drawable.ic_chevron_right), contentDescription = null, tint = colors.muted, modifier = Modifier.size(size = 16.dp))
    }
}

@Composable
private fun HistoryCard(sessionsLast30: Int, sessionsPerDay: List<Int>, onClick: () -> Unit) {
    val heroTrack = colors.heroTrack
    val accent = colors.accent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.hero)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(all = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Row(
            modifier = Modifier.size(width = 64.dp, height = 44.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(space = 3.dp)
        ) {
            sessionsPerDay.forEach { count ->
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = if (count > 0) (14 + count * 14).coerceAtMost(maximumValue = 44).dp else 4.dp)
                        .clip(shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(color = if (count > 0) accent else heroTrack)
                )
            }
        }
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = stringResource(resource = Res.string.steps_history_title), style = typography.bold16, color = colors.ink)
            Text(
                text = pluralStringResource(resource = Res.plurals.steps_history_body_count, quantity = sessionsLast30, sessionsLast30) +
                    stringResource(resource = Res.string.steps_history_body),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Icon(painter = painterResource(resource = Res.drawable.ic_chevron_right), contentDescription = null, tint = colors.muted, modifier = Modifier.size(size = 16.dp))
    }
}

/** The goal sheet, or the keypad over it while a number is typed. */
@Composable
private fun GoalSheets(viewState: StepsViewState, draft: GoalDraft, callbacks: StepsCallbacks) {
    val notifications = rememberNotificationPermissionState()
    val save = {
        if (draft.isNudgeOn && viewState.isNudgeSupported && !notifications.isGranted) notifications.request()
        callbacks.onSaveGoal()
    }
    if (draft.isTyping) {
        val title = stringResource(resource = Res.string.steps_goal_sheet_title)
        CommonKeypadSheet(
            fields = listOf(
                KeypadField(
                    id = GOAL_FIELD,
                    label = title,
                    unit = stringResource(resource = Res.string.steps_label_unit_steps),
                    value = draft.goal.toString(),
                    wholeDigits = 5,
                    min = StepGoal.MIN.toDouble(),
                    max = StepGoal.MAX.toDouble()
                )
            ),
            openId = GOAL_FIELD,
            onOpen = {},
            onCommit = { _, text -> text.toIntOrNull()?.let(callbacks::onGoalPicked) },
            onDismiss = { callbacks.onGoalTypingChanged(isTyping = false) }
        )
    } else {
        GoalSheet(viewState = viewState, draft = draft, callbacks = callbacks, onSave = save)
    }
}

private const val GOAL_FIELD = "stepGoal"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GoalSheet(viewState: StepsViewState, draft: GoalDraft, callbacks: StepsCallbacks, onSave: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = callbacks::onCloseGoal,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            Column {
                Text(text = stringResource(resource = Res.string.steps_goal_sheet_title), style = typography.bold18, color = colors.ink)
                Text(text = stringResource(resource = Res.string.steps_goal_sheet_body), style = typography.regular11, color = colors.muted)
            }
            GoalStepper(draft = draft, callbacks = callbacks)
            val suggested = viewState.suggestedGoal
            FlowRow(horizontalArrangement = Arrangement.spacedBy(space = 6.dp), verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                suggested?.let {
                    ChoiceChip(
                        label = stringResource(resource = Res.string.steps_goal_suggested, it.withThousands()),
                        isSelected = draft.goal == it,
                        isSuggested = true,
                        onClick = { callbacks.onGoalPicked(goal = it) }
                    )
                }
                StepGoal.PRESETS.filter { it != suggested }.forEach { preset ->
                    ChoiceChip(
                        label = preset.withThousands(),
                        isSelected = draft.goal == preset,
                        onClick = { callbacks.onGoalPicked(goal = preset) }
                    )
                }
            }
            GoalPreview(viewState = viewState, goal = draft.goal)
            if (viewState.isNudgeSupported) {
                SwitchRow(
                    title = stringResource(resource = Res.string.steps_goal_nudge),
                    body = stringResource(resource = Res.string.steps_goal_nudge_body, StepGoal.NUDGE_MIN_SHORT.withThousands()),
                    isOn = draft.isNudgeOn,
                    onToggle = callbacks::onNudgeToggled
                )
            }
            CommonPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = if (draft.goal == viewState.goal && draft.isNudgeOn == viewState.isNudgeOn) {
                    stringResource(resource = Res.string.steps_goal_keep, viewState.goal.withThousands())
                } else if (draft.goal == viewState.goal) {
                    stringResource(resource = Res.string.save)
                } else {
                    stringResource(resource = Res.string.steps_goal_save, draft.goal.withThousands())
                },
                onClick = onSave
            )
        }
    }
}

@Composable
private fun GoalStepper(draft: GoalDraft, callbacks: StepsCallbacks) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.hero)
            .padding(all = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        StepperButton(
            icon = Res.drawable.ic_minus,
            description = stringResource(resource = Res.string.steps_goal_fewer, StepGoal.STEP),
            isEnabled = draft.goal > StepGoal.MIN,
            onClick = callbacks::onGoalStepDown
        )
        val description = stringResource(resource = Res.string.steps_goal_value_description, draft.goal.withThousands())
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .clip(shape = RoundedCornerShape(size = 14.dp))
                .background(color = colors.surface)
                .clickable(role = Role.Button) { callbacks.onGoalTypingChanged(isTyping = true) }
                .semantics(mergeDescendants = true) { contentDescription = description }
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = draft.goal.withThousands(), style = typography.bold32, color = colors.ink)
            Text(text = stringResource(resource = Res.string.steps_goal_tap_to_type), style = typography.regular11, color = colors.muted)
        }
        StepperButton(
            icon = Res.drawable.ic_plus,
            description = stringResource(resource = Res.string.steps_goal_more, StepGoal.STEP),
            isEnabled = draft.goal < StepGoal.MAX,
            onClick = callbacks::onGoalStepUp
        )
    }
}

@Composable
private fun StepperButton(
    icon: org.jetbrains.compose.resources.DrawableResource,
    description: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size = 52.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.surface)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = icon),
            contentDescription = description,
            tint = if (isEnabled) colors.ink else colors.faint,
            modifier = Modifier.size(size = 20.dp)
        )
    }
}

/** The last 14 days coloured against the goal being picked. */
@Composable
private fun GoalPreview(viewState: StepsViewState, goal: Int) {
    val days = viewState.lookback
    val hits = StepGoal.daysHit(dailySteps = days.map { it.steps }, goal = goal)
    val average = viewState.lookbackAverage
    val ink = colors.ink
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
            .padding(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(resource = Res.string.steps_goal_would_hit, hits, days.size),
                style = typography.bold14,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            Text(text = stringResource(resource = Res.string.steps_goal_last_days, days.size), style = typography.regular11, color = colors.muted)
        }
        val scale = maxOf(WEEK_CHART_MIN_SCALE, goal, days.maxOfOrNull { it.steps } ?: 0).toFloat()
        Box(modifier = Modifier.fillMaxWidth().height(height = 72.dp)) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                days.forEach { day ->
                    ChartBar(
                        modifier = Modifier
                            .weight(weight = 1f)
                            .semantics {
                                contentDescription = day.date.toShortDayMonthDay() + ", " + day.steps.withThousands()
                            },
                        fraction = day.steps / scale,
                        maxHeight = 72.dp,
                        color = if (day.steps >= goal) colors.accent else colors.heroTrack
                    )
                }
            }
            Canvas(modifier = Modifier.matchParentSize()) {
                val y = size.height * (1 - goal / scale)
                drawLine(
                    color = ink.copy(alpha = 0.6f),
                    start = Offset(x = 0f, y = y),
                    end = Offset(x = size.width, y = y),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
                )
            }
        }
        if (average > 0) {
            val advice = when {
                goal > average * StepGoal.BIG_JUMP_SHARE -> stringResource(resource = Res.string.steps_goal_note_big_jump, StepGoal.STEP)
                goal < average * StepGoal.EASY_SHARE -> stringResource(resource = Res.string.steps_goal_note_easy)
                else -> stringResource(resource = Res.string.steps_goal_note_near)
            }
            Text(
                text = stringResource(resource = Res.string.steps_goal_note_average, average.withThousands()) + " " + advice,
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

/** "7 AM". */
@Composable
internal fun hourLabel(hour: Int): String = stringResource(
    resource = if (hour < 12) Res.string.time_hour_am else Res.string.time_hour_pm,
    if (hour % 12 == 0) 12 else hour % 12
)
