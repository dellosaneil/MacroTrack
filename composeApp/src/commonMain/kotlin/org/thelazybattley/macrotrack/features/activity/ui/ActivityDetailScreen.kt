package org.thelazybattley.macrotrack.features.activity.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.detail_all_sessions
import macrotrack.composeapp.generated.resources.detail_along
import macrotrack.composeapp.generated.resources.detail_at_distance
import macrotrack.composeapp.generated.resources.detail_avg_pace
import macrotrack.composeapp.generated.resources.detail_burned_body
import macrotrack.composeapp.generated.resources.detail_burned_locked_body
import macrotrack.composeapp.generated.resources.detail_burned_locked_title
import macrotrack.composeapp.generated.resources.detail_burned_title
import macrotrack.composeapp.generated.resources.detail_cadence
import macrotrack.composeapp.generated.resources.detail_cadence_average
import macrotrack.composeapp.generated.resources.detail_cadence_unit
import macrotrack.composeapp.generated.resources.detail_chart_description
import macrotrack.composeapp.generated.resources.detail_climb
import macrotrack.composeapp.generated.resources.detail_climbed_total
import macrotrack.composeapp.generated.resources.detail_delete
import macrotrack.composeapp.generated.resources.detail_delete_body
import macrotrack.composeapp.generated.resources.detail_delete_cancel
import macrotrack.composeapp.generated.resources.detail_delete_title
import macrotrack.composeapp.generated.resources.detail_elevation
import macrotrack.composeapp.generated.resources.detail_finish
import macrotrack.composeapp.generated.resources.detail_map_description
import macrotrack.composeapp.generated.resources.detail_pace
import macrotrack.composeapp.generated.resources.detail_pace_average
import macrotrack.composeapp.generated.resources.detail_rename
import macrotrack.composeapp.generated.resources.detail_rename_cancel
import macrotrack.composeapp.generated.resources.detail_rename_save
import macrotrack.composeapp.generated.resources.detail_rename_title
import macrotrack.composeapp.generated.resources.detail_rename_type
import macrotrack.composeapp.generated.resources.detail_splits
import macrotrack.composeapp.generated.resources.detail_splits_per
import macrotrack.composeapp.generated.resources.detail_start
import macrotrack.composeapp.generated.resources.detail_steps
import macrotrack.composeapp.generated.resources.detail_tap_chart
import macrotrack.composeapp.generated.resources.detail_time
import macrotrack.composeapp.generated.resources.detail_unit_km
import macrotrack.composeapp.generated.resources.detail_unit_mile
import macrotrack.composeapp.generated.resources.detail_when
import macrotrack.composeapp.generated.resources.detail_zoom_in
import macrotrack.composeapp.generated.resources.detail_zoom_out
import macrotrack.composeapp.generated.resources.ic_bar_chart
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_fit
import macrotrack.composeapp.generated.resources.ic_flame
import macrotrack.composeapp.generated.resources.ic_minus
import macrotrack.composeapp.generated.resources.ic_pencil
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.record_burned
import macrotrack.composeapp.generated.resources.record_distance
import macrotrack.composeapp.generated.resources.record_fit_route
import macrotrack.composeapp.generated.resources.save_summary
import macrotrack.composeapp.generated.resources.split_partial
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.ActivitySession
import org.thelazybattley.macrotrack.domain.model.FEET_PER_METER
import org.thelazybattley.macrotrack.domain.model.METERS_PER_KM
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.splits
import org.thelazybattley.macrotrack.features.activity.ActivityDetailCallbacks
import org.thelazybattley.macrotrack.features.activity.ActivityDetailViewModel
import org.thelazybattley.macrotrack.features.activity.ActivityDetailViewState
import org.thelazybattley.macrotrack.features.activity.RouteChart
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.features.pro.ProFeature
import org.thelazybattley.macrotrack.features.pro.ui.ProBadge
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.formatWeightOneDecimalWithUnit
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

/** Markers clutter the map past this many, so a long route shows none. */
private const val MAX_MARKERS = 30

@Composable
fun ActivityDetailScreen(
    onBack: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBurn: () -> Unit
) {
    val viewModel = koinViewModel<ActivityDetailViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.isDeleted) {
        if (viewState.isDeleted) onBack()
    }
    ActivityDetailScreen(viewState = viewState, callbacks = viewModel, onBack = onBack, onOpenHistory = onOpenHistory, onOpenBurn = onOpenBurn)
}

@Composable
private fun ActivityDetailScreen(
    viewState: ActivityDetailViewState,
    callbacks: ActivityDetailCallbacks,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBurn: () -> Unit
) {
    val session = viewState.session ?: run {
        Box(modifier = Modifier.fillMaxSize().background(color = colors.background))
        return
    }
    val pro = LocalProAccess.current
    val unitMeters = distanceUnitMeters()
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
            overline = stringResource(
                resource = Res.string.detail_when,
                session.date.toShortDayMonthDay(),
                clockTime(time = session.startTime),
                clockTime(time = session.endTime)
            ),
            title = session.displayName(),
            onBack = onBack,
            titleLeading = { TypeChip(session = session) },
            titleTrailing = {
                SquareButton(
                    icon = Res.drawable.ic_pencil,
                    description = stringResource(resource = Res.string.detail_rename_type, stringResource(resource = session.type.title).lowercase()),
                    size = 32.dp,
                    onClick = callbacks::onOpenRename
                )
            }
        )
        RouteCard(session = session, zoom = viewState.zoom, unitMeters = unitMeters, onZoom = callbacks::onZoom)
        StatsGrid(viewState = viewState, session = session, isPro = pro.isActive, onLocked = { pro.openPaywall(ProFeature.CALORIES_BURNED) })
        AlongCard(viewState = viewState, session = session, callbacks = callbacks)
        SplitsCard(session = session, unitMeters = unitMeters)
        BurnCard(viewState = viewState, session = session, isPro = pro.isActive, onOpenBurn = onOpenBurn, onLocked = { pro.openPaywall(ProFeature.CALORIES_BURNED) })
        OutlinedRowButton(icon = Res.drawable.ic_bar_chart, text = stringResource(resource = Res.string.detail_all_sessions), onClick = onOpenHistory)
        Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            SheetActionButton(
                modifier = Modifier.weight(weight = 1f),
                text = stringResource(resource = Res.string.detail_rename),
                style = SheetButtonStyle.QUIET,
                onClick = callbacks::onOpenRename
            )
            SheetActionButton(
                modifier = Modifier.weight(weight = 1f),
                text = stringResource(resource = Res.string.detail_delete, stringResource(resource = session.type.title).lowercase()),
                style = SheetButtonStyle.DESTRUCTIVE,
                onClick = callbacks::onDelete
            )
        }
    }
    viewState.renameDraft?.let { draft ->
        val typeName = stringResource(resource = session.type.title)
        ActivityNameSheet(
            title = stringResource(resource = Res.string.detail_rename_title, typeName.lowercase()),
            summary = stringResource(
                resource = Res.string.save_summary,
                distanceText(meters = session.distanceMeters),
                clockDuration(seconds = session.movingSeconds.toLong()),
                session.steps.withThousands()
            ),
            name = draft,
            type = session.type,
            fallbackName = typeName + " · " + session.date.toAbbreviatedMonthDay(),
            usedNames = viewState.usedNames,
            onNameChange = callbacks::onRenameChanged,
            onTypeChange = null,
            onDismiss = callbacks::onCloseRename
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
                SheetActionButton(text = stringResource(resource = Res.string.detail_rename_cancel), style = SheetButtonStyle.QUIET, onClick = callbacks::onCloseRename)
                SheetActionButton(
                    modifier = Modifier.weight(weight = 1f),
                    text = stringResource(resource = Res.string.detail_rename_save),
                    style = SheetButtonStyle.PRIMARY,
                    onClick = callbacks::onSaveRename
                )
            }
        }
    }
    if (viewState.isConfirmingDelete) {
        DeleteSheet(session = session, onCancel = callbacks::onCancelDelete, onConfirm = callbacks::onConfirmDelete)
    }
}

@Composable
private fun TypeChip(session: ActivitySession) {
    val (background, tint) = session.type.badgeColors()
    Text(
        text = stringResource(resource = session.type.title),
        style = typography.bold12,
        color = tint,
        modifier = Modifier
            .clip(shape = RoundedCornerShape(size = 8.dp))
            .background(color = background)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun RouteCard(session: ActivitySession, zoom: Float, unitMeters: Double, onZoom: (Float) -> Unit) {
    val shape = RoundedCornerShape(size = 24.dp)
    val markers = unitMeters.takeIf { session.distanceMeters / it <= MAX_MARKERS }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 330.dp)
            .clip(shape = shape)
            .border(width = 1.dp, color = colors.line, shape = shape)
    ) {
        RouteMap(
            modifier = Modifier.fillMaxSize(),
            routes = listOf(MapRoute(points = session.route)),
            description = stringResource(resource = Res.string.detail_map_description, distanceText(meters = session.distanceMeters)),
            zoom = zoom,
            markers = markers
        )
        Column(
            modifier = Modifier
                .align(alignment = Alignment.TopEnd)
                .padding(all = 10.dp),
            verticalArrangement = Arrangement.spacedBy(space = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
            ) {
                MapControl(icon = Res.drawable.ic_plus, description = stringResource(resource = Res.string.detail_zoom_in)) { onZoom(zoom.zoomedBy(step = 0.4f)) }
                HorizontalDivider(modifier = Modifier.size(width = 44.dp, height = 1.dp), color = colors.line)
                MapControl(icon = Res.drawable.ic_minus, description = stringResource(resource = Res.string.detail_zoom_out)) { onZoom(zoom.zoomedBy(step = -0.4f)) }
            }
            Box(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
            ) {
                MapControl(icon = Res.drawable.ic_fit, description = stringResource(resource = Res.string.record_fit_route)) { onZoom(1f) }
            }
        }
        Row(
            modifier = Modifier
                .align(alignment = Alignment.BottomStart)
                .padding(all = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            LegendChip(text = stringResource(resource = Res.string.detail_start), isFinish = false)
            LegendChip(text = stringResource(resource = Res.string.detail_finish), isFinish = true)
        }
    }
}

@Composable
private fun MapControl(icon: DrawableResource, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = description, tint = colors.ink, modifier = Modifier.size(size = 18.dp))
    }
}

@Composable
private fun LegendChip(text: String, isFinish: Boolean) {
    Row(
        modifier = Modifier
            .height(height = 26.dp)
            .clip(shape = RoundedCornerShape(size = 8.dp))
            .background(color = colors.surface)
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 9.dp)
                .clip(shape = CircleShape)
                .then(
                    if (isFinish) Modifier.border(width = 2.5.dp, color = colors.ink, shape = CircleShape) else Modifier.background(color = colors.accent)
                )
        )
        Text(text = text, style = typography.bold11, color = colors.ink)
    }
}

@Composable
private fun StatsGrid(viewState: ActivityDetailViewState, session: ActivitySession, isPro: Boolean, onLocked: () -> Unit) {
    val profile = viewState.profile
    val tiles = listOf(
        Triple(stringResource(resource = Res.string.record_distance), distanceValue(meters = session.distanceMeters), distanceUnit()),
        Triple(stringResource(resource = Res.string.detail_steps), session.steps.withThousands(), ""),
        Triple(stringResource(resource = Res.string.detail_time), clockDuration(seconds = session.movingSeconds.toLong()), ""),
        Triple(stringResource(resource = Res.string.detail_avg_pace), session.paceSecondsPerKm?.let { paceValue(secondsPerKm = it) } ?: "–", paceUnit()),
        Triple(stringResource(resource = Res.string.detail_climb), climbValue(meters = session.climbMeters), climbUnit()),
        Triple(
            stringResource(resource = Res.string.record_burned),
            profile?.let { "≈" + it.kcal(session = session).withThousands() } ?: "–",
            stringResource(resource = Res.string.kcal_text)
        )
    )
    val burnedIndex = tiles.lastIndex
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        tiles.withIndex().chunked(size = 3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                row.forEach { (index, tile) ->
                    StatTile(
                        modifier = Modifier.weight(weight = 1f),
                        label = tile.first,
                        value = tile.second,
                        unit = tile.third,
                        isLocked = index == burnedIndex && !isPro,
                        onLockedClick = onLocked
                    )
                }
            }
        }
    }
}

@Composable
private fun AlongCard(viewState: ActivityDetailViewState, session: ActivitySession, callbacks: ActivityDetailCallbacks) {
    val samples = viewState.samples
    if (samples.size < 2) return
    val unitSystem = LocalUnitSystem.current
    val isImperial = unitSystem == UnitSystem.IMPERIAL
    val unitMeters = distanceUnitMeters()
    val stepLength = session.stepLengthMeters
    val chart = viewState.chart
    // Elevation needs altitude on the fixes, cadence needs counted steps; tabs without data are left out.
    val tabs = buildList {
        add(RouteChart.PACE to stringResource(resource = Res.string.detail_pace))
        if (samples.any { it.altitude != null }) add(RouteChart.ELEVATION to stringResource(resource = Res.string.detail_elevation))
        if (stepLength != null) add(RouteChart.CADENCE to stringResource(resource = Res.string.detail_cadence))
    }
    val points = samples.mapIndexedNotNull { index, sample ->
        val value = when (chart) {
            RouteChart.PACE -> sample.paceSecondsPerKm?.let { it * unitMeters / METERS_PER_KM }
            RouteChart.ELEVATION -> sample.altitude?.let { if (isImperial) it * FEET_PER_METER else it }
            RouteChart.CADENCE -> sample.paceSecondsPerKm?.takeIf { it > 0 && stepLength != null }?.let { METERS_PER_KM / it * 60 / stepLength!! }
        }
        value?.let { Triple(index, sample.meters, it) }
    }
    val total = session.distanceMeters.takeIf { it > 0 } ?: 1.0
    val selected = viewState.selectedSample?.let { selectedIndex -> points.indexOfFirst { it.first == selectedIndex }.takeIf { it >= 0 } }
    val format: (Double) -> String = when (chart) {
        RouteChart.PACE -> { value -> minutesSeconds(seconds = value) }
        else -> { value -> value.roundToInt().toString() }
    }
    val unit = when (chart) {
        RouteChart.PACE -> paceUnit()
        RouteChart.ELEVATION -> climbUnit()
        RouteChart.CADENCE -> stringResource(resource = Res.string.detail_cadence_unit)
    }
    ActivityCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Text(
                text = stringResource(resource = Res.string.detail_along, stringResource(resource = session.type.title).lowercase()),
                style = typography.bold16,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            SegmentedTabs(options = tabs, selected = chart, height = 32.dp, onSelect = callbacks::onChartSelected)
        }
        val (readValue, readLabel) = when {
            selected != null -> format(points[selected].third) to
                stringResource(resource = Res.string.detail_at_distance, distanceText(meters = points[selected].second))
            chart == RouteChart.PACE -> (session.paceSecondsPerKm?.let { paceValue(secondsPerKm = it) } ?: "–") to
                stringResource(resource = Res.string.detail_pace_average)
            chart == RouteChart.ELEVATION -> climbValue(meters = session.climbMeters) to stringResource(resource = Res.string.detail_climbed_total)
            else -> (points.map { it.third }.average().takeIf { !it.isNaN() }?.roundToInt()?.toString() ?: "–") to
                stringResource(resource = Res.string.detail_cadence_average)
        }
        Column {
            ValueWithUnit(value = readValue, unit = if (chart == RouteChart.ELEVATION && selected == null) climbUnit() else unit, valueStyle = typography.bold24)
            Text(text = readLabel + " · " + stringResource(resource = Res.string.detail_tap_chart), style = typography.regular11, color = colors.muted)
        }
        LineChart(
            modifier = Modifier.fillMaxWidth().height(height = 120.dp),
            values = points.map { it.third },
            positions = points.map { (it.second / total).toFloat() },
            description = stringResource(resource = Res.string.detail_chart_description),
            label = format,
            selected = selected,
            onSelect = { callbacks.onSampleSelected(index = points[it].first) },
            lowerIsUp = chart == RouteChart.PACE,
            fillArea = chart == RouteChart.ELEVATION
        )
        Row(modifier = Modifier.fillMaxWidth().padding(start = 34.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(resource = Res.string.detail_start), style = typography.regular11, color = colors.muted)
            Text(text = distanceText(meters = session.distanceMeters / 2), style = typography.regular11, color = colors.muted)
            Text(text = distanceText(meters = session.distanceMeters), style = typography.regular11, color = colors.muted)
        }
    }
}

@Composable
private fun SplitsCard(session: ActivitySession, unitMeters: Double) {
    val splits = remember(session.route, unitMeters) { session.route.splits(unitMeters = unitMeters) }
    if (splits.isEmpty()) return
    val paces = splits.map { it.pace(unitMeters = unitMeters) }
    val fastest = paces.min()
    val slowest = paces.max()
    val isImperial = LocalUnitSystem.current == UnitSystem.IMPERIAL
    val unitName = stringResource(resource = if (isImperial) Res.string.detail_unit_mile else Res.string.detail_unit_km)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
    ) {
        Row(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(resource = Res.string.detail_splits), style = typography.bold16, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            Text(text = stringResource(resource = Res.string.detail_splits_per, unitName), style = typography.regular11, color = colors.muted)
        }
        splits.forEachIndexed { index, split ->
            val pace = paces[index]
            val isFull = split.meters >= unitMeters
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                Text(
                    text = if (isFull) unitName.replaceFirstChar { it.uppercase() } + " " + split.index else stringResource(resource = Res.string.split_partial, distanceValue(meters = split.meters)),
                    style = typography.regular13,
                    color = colors.muted,
                    modifier = Modifier.size(width = 64.dp, height = 18.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 8.dp)
                        .clip(shape = CircleShape)
                        .background(color = colors.hero)
                ) {
                    // Longer bar = faster.
                    val share = if (slowest > fastest) 0.55f + 0.45f * ((slowest - pace) / (slowest - fastest)).toFloat() else 1f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = share)
                            .height(height = 8.dp)
                            .clip(shape = CircleShape)
                            .background(color = if (pace == fastest) colors.accent else colors.heroTrack)
                    )
                }
                Text(text = minutesSeconds(seconds = pace), style = typography.bold13, color = colors.ink)
            }
        }
    }
}

@Composable
private fun BurnCard(
    viewState: ActivityDetailViewState,
    session: ActivitySession,
    isPro: Boolean,
    onOpenBurn: () -> Unit,
    onLocked: () -> Unit
) {
    val profile = viewState.profile
    val noun = stringResource(resource = session.type.title).lowercase()
    val shape = RoundedCornerShape(size = 20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = if (isPro) colors.hero else colors.surface)
            .then(if (isPro) Modifier else Modifier.border(width = 1.dp, color = colors.line, shape = shape))
            .clickable(role = Role.Button, onClick = if (isPro) onOpenBurn else onLocked)
            .padding(all = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 40.dp)
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(color = if (isPro) colors.surface else colors.warmSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_flame),
                contentDescription = null,
                tint = if (isPro) colors.accentInk else colors.warmInk,
                modifier = Modifier.size(size = 20.dp)
            )
        }
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            if (isPro && profile != null) {
                Text(
                    text = stringResource(resource = Res.string.detail_burned_title, profile.kcal(session = session).withThousands(), noun),
                    style = typography.bold14,
                    color = colors.ink
                )
                Text(
                    text = stringResource(
                        resource = Res.string.detail_burned_body,
                        profile.weightKg.formatWeightOneDecimalWithUnit(),
                        speedText(meters = session.distanceMeters, seconds = session.movingSeconds)
                    ),
                    style = typography.regular11,
                    color = colors.muted
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                    Text(text = stringResource(resource = Res.string.detail_burned_locked_title), style = typography.bold14, color = colors.ink)
                    ProBadge()
                }
                Text(text = stringResource(resource = Res.string.detail_burned_locked_body, noun), style = typography.regular11, color = colors.muted)
            }
        }
        Icon(painter = painterResource(resource = Res.drawable.ic_chevron_right), contentDescription = null, tint = colors.muted, modifier = Modifier.size(size = 16.dp))
    }
}

@Composable
internal fun OutlinedRowButton(icon: DrawableResource, text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 48.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = colors.ink, modifier = Modifier.size(size = 16.dp))
        Text(text = text, style = typography.bold14, color = colors.ink)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteSheet(session: ActivitySession, onCancel: () -> Unit, onConfirm: () -> Unit) {
    val noun = stringResource(resource = session.type.title).lowercase()
    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            Text(text = stringResource(resource = Res.string.detail_delete_title, session.displayName()), style = typography.bold18, color = colors.ink)
            Text(text = stringResource(resource = Res.string.detail_delete_body, noun), style = typography.regular13, color = colors.muted)
            Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
                SheetActionButton(
                    modifier = Modifier.weight(weight = 1f),
                    text = stringResource(resource = Res.string.detail_delete_cancel),
                    style = SheetButtonStyle.QUIET,
                    onClick = onCancel
                )
                SheetActionButton(
                    modifier = Modifier.weight(weight = 1f),
                    text = stringResource(resource = Res.string.detail_delete, noun),
                    style = SheetButtonStyle.DESTRUCTIVE,
                    onClick = onConfirm
                )
            }
        }
    }
}
