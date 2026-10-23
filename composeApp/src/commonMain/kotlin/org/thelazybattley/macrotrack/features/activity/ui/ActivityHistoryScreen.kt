package org.thelazybattley.macrotrack.features.activity.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.history_active_days
import macrotrack.composeapp.generated.resources.history_active_days_value
import macrotrack.composeapp.generated.resources.history_all_time
import macrotrack.composeapp.generated.resources.history_all_time_since
import macrotrack.composeapp.generated.resources.history_average_burned
import macrotrack.composeapp.generated.resources.history_average_line
import macrotrack.composeapp.generated.resources.history_average_pace
import macrotrack.composeapp.generated.resources.history_average_pace_runs
import macrotrack.composeapp.generated.resources.history_average_pace_walks
import macrotrack.composeapp.generated.resources.history_average_per_run
import macrotrack.composeapp.generated.resources.history_average_per_session
import macrotrack.composeapp.generated.resources.history_average_per_walk
import macrotrack.composeapp.generated.resources.history_avg_tag
import macrotrack.composeapp.generated.resources.history_bars_description
import macrotrack.composeapp.generated.resources.history_best_fastest_run
import macrotrack.composeapp.generated.resources.history_best_longest_run
import macrotrack.composeapp.generated.resources.history_best_longest_walk
import macrotrack.composeapp.generated.resources.history_best_most_burned
import macrotrack.composeapp.generated.resources.history_best_most_steps
import macrotrack.composeapp.generated.resources.history_bests
import macrotrack.composeapp.generated.resources.history_climbed
import macrotrack.composeapp.generated.resources.history_count_mixed
import macrotrack.composeapp.generated.resources.history_delta_down
import macrotrack.composeapp.generated.resources.history_delta_none
import macrotrack.composeapp.generated.resources.history_delta_same
import macrotrack.composeapp.generated.resources.history_delta_up
import macrotrack.composeapp.generated.resources.history_empty
import macrotrack.composeapp.generated.resources.history_filter_all
import macrotrack.composeapp.generated.resources.history_filter_runs
import macrotrack.composeapp.generated.resources.history_filter_walks
import macrotrack.composeapp.generated.resources.history_last_days
import macrotrack.composeapp.generated.resources.history_last_week
import macrotrack.composeapp.generated.resources.history_longest_streak
import macrotrack.composeapp.generated.resources.history_map_description
import macrotrack.composeapp.generated.resources.history_map_hint
import macrotrack.composeapp.generated.resources.history_map_title
import macrotrack.composeapp.generated.resources.history_metric_calories
import macrotrack.composeapp.generated.resources.history_metric_distance
import macrotrack.composeapp.generated.resources.history_metric_steps
import macrotrack.composeapp.generated.resources.history_metric_time
import macrotrack.composeapp.generated.resources.history_newest
import macrotrack.composeapp.generated.resources.history_nothing_recorded
import macrotrack.composeapp.generated.resources.history_note_day
import macrotrack.composeapp.generated.resources.history_note_month
import macrotrack.composeapp.generated.resources.history_note_week
import macrotrack.composeapp.generated.resources.history_oldest
import macrotrack.composeapp.generated.resources.history_overline
import macrotrack.composeapp.generated.resources.history_pace_description
import macrotrack.composeapp.generated.resources.history_pace_lower_faster
import macrotrack.composeapp.generated.resources.history_pace_none
import macrotrack.composeapp.generated.resources.history_pace_title_month
import macrotrack.composeapp.generated.resources.history_pace_title_run
import macrotrack.composeapp.generated.resources.history_pace_title_walk
import macrotrack.composeapp.generated.resources.history_pace_title_week
import macrotrack.composeapp.generated.resources.history_period
import macrotrack.composeapp.generated.resources.history_range_30
import macrotrack.composeapp.generated.resources.history_range_7
import macrotrack.composeapp.generated.resources.history_range_90
import macrotrack.composeapp.generated.resources.history_range_all
import macrotrack.composeapp.generated.resources.history_runs
import macrotrack.composeapp.generated.resources.history_sessions_count
import macrotrack.composeapp.generated.resources.history_show_all
import macrotrack.composeapp.generated.resources.history_show_fewer
import macrotrack.composeapp.generated.resources.history_streak_days
import macrotrack.composeapp.generated.resources.history_summary
import macrotrack.composeapp.generated.resources.history_this_week
import macrotrack.composeapp.generated.resources.history_title
import macrotrack.composeapp.generated.resources.history_total_kcal
import macrotrack.composeapp.generated.resources.history_total_label
import macrotrack.composeapp.generated.resources.history_total_label_all
import macrotrack.composeapp.generated.resources.history_total_steps
import macrotrack.composeapp.generated.resources.history_total_time
import macrotrack.composeapp.generated.resources.history_walks
import macrotrack.composeapp.generated.resources.history_walks_and_runs
import macrotrack.composeapp.generated.resources.history_week_summary
import macrotrack.composeapp.generated.resources.history_week_of
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.record_distance
import macrotrack.composeapp.generated.resources.steps_label_unit_steps
import macrotrack.composeapp.generated.resources.ic_trophy
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.ActivitySession
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.BucketSize
import org.thelazybattley.macrotrack.domain.model.HistoryFilter
import org.thelazybattley.macrotrack.domain.model.HistoryMetric
import org.thelazybattley.macrotrack.domain.model.HistoryRange
import org.thelazybattley.macrotrack.domain.model.HistoryTotals
import org.thelazybattley.macrotrack.domain.model.METERS_PER_KM
import org.thelazybattley.macrotrack.domain.model.averagePace
import org.thelazybattley.macrotrack.domain.model.percentChange
import org.thelazybattley.macrotrack.domain.model.weeksAgo
import org.thelazybattley.macrotrack.features.activity.ActivityHistoryCallbacks
import org.thelazybattley.macrotrack.features.activity.ActivityHistoryViewModel
import org.thelazybattley.macrotrack.features.activity.ActivityHistoryViewState
import org.thelazybattley.macrotrack.features.activity.HISTORY_LIST_PREVIEW
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.features.pro.ProFeature
import org.thelazybattley.macrotrack.features.pro.ui.ProBadge
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.sqrt

/** Faint routes stack up darker where they overlap; this is as faint as one gets. */
private const val MIN_ROUTE_ALPHA = 0.12f

@Composable
fun ActivityHistoryScreen(
    onBack: () -> Unit,
    onOpenSession: (Long) -> Unit
) {
    val viewModel = koinViewModel<ActivityHistoryViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    ActivityHistoryScreen(viewState = viewState, callbacks = viewModel, onBack = onBack, onOpenSession = onOpenSession)
}

@Composable
private fun ActivityHistoryScreen(
    viewState: ActivityHistoryViewState,
    callbacks: ActivityHistoryCallbacks,
    onBack: () -> Unit,
    onOpenSession: (Long) -> Unit
) {
    val pro = LocalProAccess.current
    val openPaywall = { pro.openPaywall(ProFeature.CALORIES_BURNED) }
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
            overline = stringResource(resource = Res.string.history_overline),
            title = stringResource(resource = Res.string.history_title),
            onBack = onBack
        )
        if (viewState.sessions.isEmpty()) {
            InfoNote(text = stringResource(resource = Res.string.history_empty))
            return@Column
        }
        SegmentedTabs(
            modifier = Modifier.fillMaxWidth(),
            options = listOf(
                HistoryRange.WEEK to stringResource(resource = Res.string.history_range_7),
                HistoryRange.MONTH to stringResource(resource = Res.string.history_range_30),
                HistoryRange.QUARTER to stringResource(resource = Res.string.history_range_90),
                HistoryRange.ALL to stringResource(resource = Res.string.history_range_all)
            ),
            selected = viewState.range,
            height = 36.dp,
            fillWidth = true,
            onSelect = callbacks::onRangeSelected
        )
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            listOf(
                Triple(HistoryFilter.ALL, Res.string.history_filter_all, null),
                Triple(HistoryFilter.WALKS, Res.string.history_filter_walks, ActivityType.WALK),
                Triple(HistoryFilter.RUNS, Res.string.history_filter_runs, ActivityType.RUN)
            ).forEach { (filter, label, type) ->
                FilterChip(
                    label = stringResource(resource = label),
                    type = type,
                    isSelected = viewState.filter == filter,
                    onClick = { callbacks.onFilterSelected(filter = filter) }
                )
            }
        }
        SummaryCard(viewState = viewState, isPro = pro.isActive, onLocked = openPaywall)
        BarsCard(viewState = viewState, callbacks = callbacks, isPro = pro.isActive, onLocked = openPaywall)
        PaceCard(viewState = viewState, callbacks = callbacks)
        WhereCard(viewState = viewState)
        BestsGrid(viewState = viewState, isPro = pro.isActive, onOpenSession = onOpenSession)
        SessionList(viewState = viewState, isPro = pro.isActive, callbacks = callbacks, onOpenSession = onOpenSession)
    }
}

@Composable
private fun FilterChip(label: String, type: ActivityType?, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 18.dp)
    Row(
        modifier = Modifier
            .height(height = 36.dp)
            .clip(shape = shape)
            .background(color = if (isSelected) colors.ink else colors.surface)
            .border(width = 1.dp, color = if (isSelected) colors.ink else colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        val content = if (isSelected) colors.background else colors.ink
        type?.let {
            Icon(painter = painterResource(resource = it.icon), contentDescription = null, tint = content, modifier = Modifier.size(size = 15.dp))
        }
        Text(text = label, style = typography.bold13, color = content)
    }
}

@Composable
private fun SummaryCard(viewState: ActivityHistoryViewState, isPro: Boolean, onLocked: () -> Unit) {
    val totals = viewState.totals
    val previous = viewState.previousTotals
    val period = viewState.period
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 22.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 22.dp))
    ) {
        Row(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(modifier = Modifier.weight(weight = 1f), text = stringResource(resource = Res.string.history_summary))
            Text(
                text = if (viewState.range == HistoryRange.ALL) {
                    stringResource(resource = Res.string.history_all_time_since, period.start.toAbbreviatedMonthDay())
                } else {
                    stringResource(resource = Res.string.history_period, period.days, period.start.toAbbreviatedMonthDay(), period.end.toAbbreviatedMonthDay())
                },
                style = typography.regular11,
                color = colors.muted,
                textAlign = TextAlign.End
            )
        }
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        Row {
            BigTile(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = Res.string.history_total_steps),
                value = totals.steps.withThousands(),
                unit = "",
                metric = HistoryMetric.STEPS,
                totals = totals,
                previous = previous
            )
            Box(modifier = Modifier.width(width = 1.dp).height(height = 78.dp).background(color = colors.divider))
            BigTile(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = Res.string.history_total_kcal),
                value = (totals.kcal ?: 0).withThousands(),
                unit = stringResource(resource = Res.string.kcal_text),
                metric = HistoryMetric.KCAL,
                totals = totals,
                previous = previous,
                isLocked = !isPro,
                onLocked = onLocked
            )
        }
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        Row {
            BigTile(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = Res.string.history_total_time),
                value = shortDuration(minutes = totals.movingSeconds / 60),
                unit = "",
                metric = HistoryMetric.TIME,
                totals = totals,
                previous = previous
            )
            Box(modifier = Modifier.width(width = 1.dp).height(height = 78.dp).background(color = colors.divider))
            BigTile(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = Res.string.record_distance),
                value = distanceValue(meters = totals.meters, decimals = 1),
                unit = distanceUnit(),
                metric = HistoryMetric.DISTANCE,
                totals = totals,
                previous = previous
            )
        }
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
            val inRange = viewState.inRange
            val countValue = when (viewState.filter) {
                HistoryFilter.ALL -> stringResource(resource = Res.string.history_count_mixed, totals.count, totals.walks, totals.runs)
                else -> totals.count.toString()
            }
            val perLabel = when (viewState.filter) {
                HistoryFilter.ALL -> Res.string.history_average_per_session
                HistoryFilter.WALKS -> Res.string.history_average_per_walk
                HistoryFilter.RUNS -> Res.string.history_average_per_run
            }
            val walkPace = averagePace(sessions = inRange.filter { it.type == ActivityType.WALK })
            val runPace = averagePace(sessions = inRange.filter { it.type == ActivityType.RUN })
            val paces = listOfNotNull(
                walkPace?.takeIf { viewState.filter != HistoryFilter.RUNS }?.let { stringResource(resource = Res.string.history_average_pace_walks, paceValue(secondsPerKm = it)) },
                runPace?.takeIf { viewState.filter != HistoryFilter.WALKS }?.let { stringResource(resource = Res.string.history_average_pace_runs, paceValue(secondsPerKm = it)) }
            )
            val count = totals.count
            val rows = listOf(
                Triple(
                    stringResource(
                        resource = when (viewState.filter) {
                            HistoryFilter.ALL -> Res.string.history_walks_and_runs
                            HistoryFilter.WALKS -> Res.string.history_walks
                            HistoryFilter.RUNS -> Res.string.history_runs
                        }
                    ),
                    countValue,
                    false
                ),
                Triple(stringResource(resource = Res.string.history_active_days), stringResource(resource = Res.string.history_active_days_value, viewState.activeDays, viewState.period.days), false),
                Triple(
                    stringResource(resource = Res.string.history_longest_streak),
                    pluralStringResource(resource = Res.plurals.history_streak_days, quantity = viewState.longestStreak, viewState.longestStreak),
                    false
                ),
                Triple(stringResource(resource = Res.string.history_climbed), climbValue(meters = totals.climbMeters) + " " + climbUnit(), false),
                Triple(
                    stringResource(resource = perLabel),
                    if (count == 0) {
                        "–"
                    } else {
                        stringResource(
                            resource = Res.string.history_average_line,
                            distanceText(meters = totals.meters / count, decimals = 1),
                            shortDuration(minutes = totals.movingSeconds / count / 60),
                            (totals.steps / count).withThousands()
                        )
                    },
                    false
                ),
                Triple(
                    stringResource(resource = Res.string.history_average_burned),
                    if (count == 0 || totals.kcal == null) "–" else "≈" + (totals.kcal / count).withThousands() + " " + stringResource(resource = Res.string.kcal_text),
                    true
                ),
                Triple(stringResource(resource = Res.string.history_average_pace), if (paces.isEmpty()) "–" else paces.joinToString(separator = " · ") + " " + paceUnit(), false)
            )
            rows.forEachIndexed { index, (label, value, isKcal) ->
                Row(modifier = Modifier.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = label, style = typography.regular13, color = colors.muted, modifier = Modifier.weight(weight = 1f))
                    if (isKcal && !isPro) {
                        ProBadge(modifier = Modifier.clickable(role = Role.Button, onClick = onLocked))
                    } else {
                        Text(text = value, style = typography.bold13, color = colors.ink, textAlign = TextAlign.End)
                    }
                }
                if (index != rows.lastIndex) HorizontalDivider(thickness = 1.dp, color = colors.divider)
            }
        }
    }
}

@Composable
private fun BigTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    metric: HistoryMetric,
    totals: HistoryTotals,
    previous: HistoryTotals?,
    isLocked: Boolean = false,
    onLocked: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .then(if (isLocked) Modifier.clickable(role = Role.Button, onClick = onLocked) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(space = 2.dp)
    ) {
        Text(text = label, style = typography.medium11, color = colors.muted)
        if (isLocked) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(text = "– – –", style = typography.bold18, color = colors.line)
                ProBadge()
            }
        } else {
            ValueWithUnit(value = value, unit = unit, valueStyle = typography.bold24)
            previous?.let {
                val change = percentChange(current = totals.value(metric = metric), previous = it.value(metric = metric))
                val (text, color) = when {
                    change == null -> stringResource(resource = Res.string.history_delta_none) to colors.muted
                    change == 0 -> stringResource(resource = Res.string.history_delta_same) to colors.muted
                    change > 0 -> stringResource(resource = Res.string.history_delta_up, change) to colors.accentInk
                    else -> stringResource(resource = Res.string.history_delta_down, abs(change)) to colors.warmInk
                }
                Text(text = text, style = typography.bold11, color = color)
            }
        }
    }
}

@Composable
private fun BarsCard(viewState: ActivityHistoryViewState, callbacks: ActivityHistoryCallbacks, isPro: Boolean, onLocked: () -> Unit) {
    val metric = if (!isPro && viewState.metric == HistoryMetric.KCAL) HistoryMetric.STEPS else viewState.metric
    val profile = viewState.profile
    val buckets = viewState.buckets
    val values = buckets.map { HistoryTotals.of(sessions = it.sessions, profile = profile).value(metric = metric) }
    val active = values.filterIndexed { index, _ -> buckets[index].sessions.isNotEmpty() }
    val average = if (active.isEmpty()) 0.0 else active.average()
    val max = values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val top = max * 1.1
    val format: @Composable (Double) -> String = { value ->
        when (metric) {
            HistoryMetric.STEPS, HistoryMetric.KCAL -> value.toInt().withThousands()
            HistoryMetric.TIME -> shortDuration(minutes = (value / 60).toInt())
            HistoryMetric.DISTANCE -> distanceValue(meters = value, decimals = 1)
        }
    }
    val unit = when (metric) {
        HistoryMetric.STEPS -> stringResource(resource = Res.string.steps_label_unit_steps)
        HistoryMetric.KCAL -> stringResource(resource = Res.string.kcal_text)
        HistoryMetric.TIME -> ""
        HistoryMetric.DISTANCE -> distanceUnit()
    }
    val selected = viewState.selectedBar?.takeIf { it in buckets.indices }
    ActivityCard {
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            listOf(
                HistoryMetric.STEPS to Res.string.history_metric_steps,
                HistoryMetric.KCAL to Res.string.history_metric_calories,
                HistoryMetric.TIME to Res.string.history_metric_time,
                HistoryMetric.DISTANCE to Res.string.history_metric_distance
            ).forEach { (option, label) ->
                val isLocked = option == HistoryMetric.KCAL && !isPro
                MetricChip(
                    label = stringResource(resource = label),
                    isSelected = metric == option,
                    isLocked = isLocked,
                    onClick = { if (isLocked) onLocked() else callbacks.onMetricSelected(metric = option) }
                )
            }
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = selected?.let { bucketLabel(start = buckets[it].start, end = buckets[it].end, size = viewState.bucketSize) }
                        ?: stringResource(
                            resource = if (viewState.range == HistoryRange.ALL) Res.string.history_total_label_all else Res.string.history_total_label,
                            viewState.period.days
                        ),
                    style = typography.regular11,
                    color = colors.muted
                )
                val isEmptyBar = selected != null && buckets[selected].sessions.isEmpty()
                ValueWithUnit(
                    value = when {
                        selected == null -> format(viewState.totals.value(metric = metric))
                        isEmptyBar -> "–"
                        else -> format(values[selected])
                    },
                    unit = if (isEmptyBar) stringResource(resource = Res.string.history_nothing_recorded) else unit,
                    valueStyle = typography.bold24
                )
            }
            Text(
                text = stringResource(
                    resource = when (viewState.bucketSize) {
                        BucketSize.DAY -> Res.string.history_note_day
                        BucketSize.WEEK -> Res.string.history_note_week
                        BucketSize.MONTH -> Res.string.history_note_month
                    }
                ),
                style = typography.regular11,
                color = colors.muted,
                textAlign = TextAlign.End
            )
        }
        val ink = colors.ink
        val averageText = stringResource(resource = Res.string.history_avg_tag, format(average))
        Box(modifier = Modifier.fillMaxWidth().height(height = 150.dp)) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(space = if (buckets.size > 20) 2.dp else 6.dp)
            ) {
                buckets.forEachIndexed { index, bucket ->
                    val count = bucket.sessions.size
                    BarButton(
                        modifier = Modifier.weight(weight = 1f),
                        fraction = (values[index] / top).toFloat(),
                        maxHeight = 150.dp,
                        color = if (selected == null || selected == index) colors.accent else colors.heroTrack,
                        cornerRadius = 4.dp,
                        // Days with nothing recorded stay empty rather than showing a stub.
                        minHeight = if (count == 0) 0.dp else 4.dp,
                        description = stringResource(
                            resource = Res.string.history_bars_description,
                            bucketLabel(start = bucket.start, end = bucket.end, size = viewState.bucketSize),
                            if (count == 0) stringResource(resource = Res.string.history_nothing_recorded) else format(values[index]) + " " + unit,
                            pluralStringResource(resource = Res.plurals.history_sessions_count, quantity = count, count)
                        ),
                        onClick = { callbacks.onBarSelected(index = index) }
                    )
                }
            }
            if (average > 0) {
                val fraction = (average / top).toFloat()
                Canvas(modifier = Modifier.matchParentSize()) {
                    val y = size.height * (1 - fraction)
                    drawLine(
                        color = ink.copy(alpha = 0.55f),
                        start = Offset(x = 0f, y = y),
                        end = Offset(x = size.width, y = y),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
                    )
                }
                Text(
                    text = averageText,
                    style = typography.bold10,
                    color = colors.ink,
                    modifier = Modifier
                        .padding(top = (150.dp * (1 - fraction) - 16.dp).coerceAtLeast(minimumValue = 0.dp))
                        .clip(shape = RoundedCornerShape(size = 4.dp))
                        .background(color = colors.surface)
                        .padding(horizontal = 4.dp)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val first = buckets.firstOrNull()
            val middle = buckets.getOrNull(index = buckets.size / 2)
            listOfNotNull(first, middle, buckets.lastOrNull()).forEach { bucket ->
                Text(text = axisLabel(date = bucket.start, size = viewState.bucketSize), style = typography.regular11, color = colors.muted)
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, isSelected: Boolean, isLocked: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 10.dp)
    Row(
        modifier = Modifier
            .height(height = 34.dp)
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
    ) {
        Text(text = label, style = typography.bold12, color = if (isSelected) colors.accentInk else colors.muted)
        if (isLocked) ProBadge()
    }
}

/** A bar's label: the day, the week it covers, or the month. */
@Composable
private fun bucketLabel(start: LocalDate, end: LocalDate, size: BucketSize): String = when (size) {
    BucketSize.DAY -> relativeDay(date = start)
    BucketSize.WEEK -> stringResource(resource = Res.string.history_week_of, start.toAbbreviatedMonthDay(), end.toAbbreviatedMonthDay())
    BucketSize.MONTH -> start.monthName()
}

@Composable
private fun axisLabel(date: LocalDate, size: BucketSize): String = when (size) {
    BucketSize.MONTH -> date.monthName().take(n = 3)
    else -> date.toAbbreviatedMonthDay()
}

private fun LocalDate.monthName(): String = month.name.lowercase().replaceFirstChar { it.uppercase() }

@Composable
private fun PaceCard(viewState: ActivityHistoryViewState, callbacks: ActivityHistoryCallbacks) {
    val points = viewState.pacePoints
    val type = viewState.paceTypeShown
    val unitMeters = distanceUnitMeters()
    val selected = viewState.selectedPoint?.takeIf { it in points.indices }
    ActivityCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = stringResource(
                        resource = when (viewState.bucketSize) {
                            BucketSize.DAY -> if (type == ActivityType.RUN) Res.string.history_pace_title_run else Res.string.history_pace_title_walk
                            BucketSize.WEEK -> Res.string.history_pace_title_week
                            BucketSize.MONTH -> Res.string.history_pace_title_month
                        },
                        stringResource(resource = type.title)
                    ),
                    style = typography.bold16,
                    color = colors.ink
                )
                Text(text = stringResource(resource = Res.string.history_pace_lower_faster), style = typography.regular11, color = colors.muted)
            }
            if (viewState.filter == HistoryFilter.ALL) {
                SegmentedTabs(
                    options = ActivityType.entries.map { it to stringResource(resource = it.title) },
                    selected = type,
                    height = 30.dp,
                    onSelect = callbacks::onPaceTypeSelected
                )
            }
        }
        if (points.isEmpty()) {
            Text(
                text = stringResource(resource = Res.string.history_pace_none, stringResource(resource = type.title).lowercase()),
                style = typography.regular13,
                color = colors.muted
            )
            return@ActivityCard
        }
        val average = averagePace(sessions = viewState.inRange.filter { it.type == type })
        Column {
            ValueWithUnit(
                value = selected?.let { paceValue(secondsPerKm = points[it].secondsPerKm) } ?: average?.let { paceValue(secondsPerKm = it) } ?: "–",
                unit = paceUnit(),
                valueStyle = typography.bold18
            )
            Text(
                text = selected?.let { index ->
                    val point = points[index]
                    point.session?.let { relativeDay(date = it.date) + " · " + it.displayName() }
                        ?: bucketLabel(start = point.start, end = point.end, size = viewState.bucketSize)
                } ?: stringResource(
                    resource = if (viewState.range == HistoryRange.ALL) Res.string.history_all_time else Res.string.history_last_days,
                    viewState.period.days
                ),
                style = typography.regular11,
                color = colors.muted
            )
        }
        LineChart(
            modifier = Modifier.fillMaxWidth().height(height = 140.dp),
            values = points.map { it.secondsPerKm * unitMeters / METERS_PER_KM },
            positions = if (points.size == 1) listOf(0.5f) else points.indices.map { it / (points.size - 1f) },
            description = stringResource(resource = Res.string.history_pace_description),
            label = { minutesSeconds(seconds = it) },
            selected = selected,
            onSelect = callbacks::onPointSelected,
            lowerIsUp = true,
            showDots = true,
            gridLines = 3
        )
        Row(modifier = Modifier.fillMaxWidth().padding(start = 34.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(resource = Res.string.history_oldest), style = typography.regular11, color = colors.muted)
            Text(text = stringResource(resource = Res.string.history_newest), style = typography.regular11, color = colors.muted)
        }
    }
}

@Composable
private fun WhereCard(viewState: ActivityHistoryViewState) {
    val sessions = viewState.inRange.filter { it.route.size > 1 }
    if (sessions.isEmpty()) return
    val names = sessions.groupBy { it.name }.filterKeys { it != null }.mapValues { it.value.size }
    val top = names.entries.sortedByDescending { it.value }.take(n = 3)
    val routes = sessions.map { session ->
        val count = session.name?.let { names[it] } ?: 1
        MapRoute(points = session.route, alpha = maxOf(MIN_ROUTE_ALPHA, 0.9f / sqrt(x = count.toFloat())))
    }
    Column(verticalArrangement = Arrangement.spacedBy(space = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(modifier = Modifier.weight(weight = 1f), text = stringResource(resource = Res.string.history_map_title))
            Text(text = stringResource(resource = Res.string.history_map_hint), style = typography.regular11, color = colors.muted)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 230.dp)
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
        ) {
            RouteMap(
                modifier = Modifier.fillMaxSize(),
                routes = routes,
                description = stringResource(resource = Res.string.history_map_description, sessions.size),
                showEnds = false,
                interactive = false
            )
            if (top.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .align(alignment = Alignment.BottomStart)
                        .padding(all = 10.dp)
                        .clip(shape = RoundedCornerShape(size = 12.dp))
                        .background(color = colors.surface)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 4.dp)
                ) {
                    top.forEachIndexed { index, (name, count) ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(width = 16.dp, height = 4.dp)
                                    .clip(shape = RoundedCornerShape(size = 2.dp))
                                    .background(color = colors.accent.copy(alpha = listOf(1f, 0.6f, 0.35f)[index]))
                            )
                            Text(text = name.orEmpty(), style = typography.bold11, color = colors.ink)
                            Text(text = "$count×", style = typography.regular11, color = colors.muted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BestsGrid(viewState: ActivityHistoryViewState, isPro: Boolean, onOpenSession: (Long) -> Unit) {
    val bests = viewState.bests
    val profile = viewState.profile
    val cards = listOfNotNull(
        bests.longestWalk?.let { Triple(Res.string.history_best_longest_walk, it, distanceValue(meters = it.distanceMeters) to distanceUnit()) },
        bests.longestRun?.let { Triple(Res.string.history_best_longest_run, it, distanceValue(meters = it.distanceMeters) to distanceUnit()) },
        bests.fastestRun?.let { run -> run.paceSecondsPerKm?.let { Triple(Res.string.history_best_fastest_run, run, paceValue(secondsPerKm = it) to paceUnit()) } },
        if (isPro && profile != null) {
            bests.mostBurned?.let { Triple(Res.string.history_best_most_burned, it, profile.kcal(session = it).withThousands() to stringResource(resource = Res.string.kcal_text)) }
        } else {
            bests.mostSteps?.let { Triple(Res.string.history_best_most_steps, it, it.steps.withThousands() to stringResource(resource = Res.string.steps_label_unit_steps)) }
        }
    )
    if (cards.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(space = 10.dp)) {
        SectionTitle(text = stringResource(resource = Res.string.history_bests))
        cards.chunked(size = 2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                row.forEach { (label, session, value) ->
                    BestCard(
                        modifier = Modifier.weight(weight = 1f),
                        label = stringResource(resource = label),
                        value = value.first,
                        unit = value.second,
                        session = session,
                        onClick = { onOpenSession(session.id) }
                    )
                }
                if (row.size == 1) Box(modifier = Modifier.weight(weight = 1f))
            }
        }
    }
}

@Composable
private fun BestCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String,
    session: ActivitySession,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 16.dp)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            Icon(painter = painterResource(resource = Res.drawable.ic_trophy), contentDescription = null, tint = colors.warmInk, modifier = Modifier.size(size = 14.dp))
            Text(text = label, style = typography.bold12, color = colors.warmInk)
        }
        ValueWithUnit(value = value, unit = unit, valueStyle = typography.bold18)
        Text(text = relativeDay(date = session.date) + " · " + session.displayName(), style = typography.regular11, color = colors.muted, maxLines = 1)
    }
}

@Composable
private fun SessionList(
    viewState: ActivityHistoryViewState,
    isPro: Boolean,
    callbacks: ActivityHistoryCallbacks,
    onOpenSession: (Long) -> Unit
) {
    val sessions = viewState.inRange
    val shown = if (viewState.isListExpanded) sessions else sessions.take(n = HISTORY_LIST_PREVIEW)
    val profile = viewState.profile
    Column(verticalArrangement = Arrangement.spacedBy(space = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(
                modifier = Modifier.weight(weight = 1f),
                text = stringResource(
                    resource = when (viewState.filter) {
                        HistoryFilter.ALL -> Res.string.history_walks_and_runs
                        HistoryFilter.WALKS -> Res.string.history_walks
                        HistoryFilter.RUNS -> Res.string.history_runs
                    }
                )
            )
            Text(
                text = pluralStringResource(resource = Res.plurals.history_sessions_count, quantity = sessions.size, sessions.size),
                style = typography.regular11,
                color = colors.muted
            )
        }
        if (shown.isEmpty()) {
            InfoNote(text = stringResource(resource = Res.string.history_nothing_recorded))
            return@Column
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
        ) {
            var lastWeek = -1
            shown.forEach { session ->
                val week = weeksAgo(date = session.date, today = viewState.today)
                if (week != lastWeek) {
                    lastWeek = week
                    val inWeek = sessions.filter { weeksAgo(date = it.date, today = viewState.today) == week }
                    WeekHeader(week = week, sessions = inWeek, today = viewState.today)
                }
                val kcal = profile?.takeIf { isPro }?.let { " · ≈" + it.kcal(session = session).withThousands() + " " + stringResource(resource = Res.string.kcal_text) }.orEmpty()
                SessionRow(
                    session = session,
                    thumbnailSize = 52.dp,
                    detail = distanceText(meters = session.distanceMeters) + " · " + shortDuration(minutes = session.movingSeconds / 60) +
                        (session.paceSecondsPerKm?.let { " · " + paceValue(secondsPerKm = it) + " " + paceUnit() } ?: "") + kcal,
                    onClick = { onOpenSession(session.id) }
                )
                HorizontalDivider(thickness = 1.dp, color = colors.divider)
            }
            if (sessions.size > HISTORY_LIST_PREVIEW) {
                SheetActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (viewState.isListExpanded) {
                        stringResource(resource = Res.string.history_show_fewer)
                    } else {
                        stringResource(resource = Res.string.history_show_all, sessions.size)
                    },
                    style = SheetButtonStyle.TEXT,
                    onClick = callbacks::onToggleList
                )
            }
        }
    }
}

@Composable
private fun WeekHeader(week: Int, sessions: List<ActivitySession>, today: LocalDate) {
    val label = when (week) {
        0 -> stringResource(resource = Res.string.history_this_week)
        1 -> stringResource(resource = Res.string.history_last_week)
        else -> {
            val end = today.minus(days = week * 7)
            stringResource(resource = Res.string.history_week_of, end.minus(days = 6).toAbbreviatedMonthDay(), end.toAbbreviatedMonthDay())
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.background)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = typography.bold12, color = colors.muted, modifier = Modifier.weight(weight = 1f))
        Text(
            text = stringResource(
                resource = Res.string.history_week_summary,
                pluralStringResource(resource = Res.plurals.history_sessions_count, quantity = sessions.size, sessions.size),
                distanceText(meters = sessions.sumOf { it.distanceMeters }, decimals = 1)
            ),
            style = typography.bold12,
            color = colors.muted
        )
    }
}

private fun LocalDate.minus(days: Int): LocalDate = minus(period = DatePeriod(days = days))
