package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.background
import org.thelazybattley.macrotrack.ui.common.inWeightUnit
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.change
import macrotrack.composeapp.generated.resources.daily_weigh_in
import macrotrack.composeapp.generated.resources.date
import macrotrack.composeapp.generated.resources.entries
import macrotrack.composeapp.generated.resources.ic_nav_progress
import macrotrack.composeapp.generated.resources.ic_trend_down
import macrotrack.composeapp.generated.resources.unit_avg
import macrotrack.composeapp.generated.resources.log
import macrotrack.composeapp.generated.resources.moving_average
import macrotrack.composeapp.generated.resources.moving_average_change_vs_week_ago
import macrotrack.composeapp.generated.resources.moving_average_chart_description
import macrotrack.composeapp.generated.resources.moving_average_chip
import macrotrack.composeapp.generated.resources.moving_average_days
import macrotrack.composeapp.generated.resources.moving_average_days_short
import macrotrack.composeapp.generated.resources.moving_average_on_date
import macrotrack.composeapp.generated.resources.moving_average_selected_range
import macrotrack.composeapp.generated.resources.moving_average_selecting
import macrotrack.composeapp.generated.resources.moving_average_today
import macrotrack.composeapp.generated.resources.moving_average_window
import macrotrack.composeapp.generated.resources.weight
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.ui.ProfilePillButton
import org.thelazybattley.macrotrack.features.profile.weighthistory.GoalPhase
import org.thelazybattley.macrotrack.features.profile.weighthistory.MovingAverageSummary
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightHistoryCallbacks
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightHistoryViewModel
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightHistoryViewState
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightPeriod
import org.thelazybattley.macrotrack.features.profile.weighthistory.changesFromPrevious
import org.thelazybattley.macrotrack.features.profile.weighthistory.movingAverage
import org.thelazybattley.macrotrack.features.profile.weighthistory.movingAverageSummary
import org.thelazybattley.macrotrack.features.tour.TourTarget
import org.thelazybattley.macrotrack.features.tour.tourTarget
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import org.thelazybattley.macrotrack.ui.common.formatWeight
import org.thelazybattley.macrotrack.ui.common.toSignedWeight

private const val SAME_THRESHOLD_KG = 0.005
private val WEIGHT_COLUMN_WIDTH = 60.dp
private val AVERAGE_COLUMN_WIDTH = 72.dp
private val CHANGE_COLUMN_WIDTH = 64.dp

@Composable
fun MovingAverageScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val viewModel = koinViewModel<WeightHistoryViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    MovingAverageScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = onPopBackStack,
        onLogWeight = { onNavigate(AppDestinations.Root.LogWeight.route) }
    )
}

@Composable
private fun MovingAverageScreen(
    modifier: Modifier = Modifier,
    viewState: WeightHistoryViewState,
    callbacks: WeightHistoryCallbacks,
    onBack: () -> Unit,
    onLogWeight: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CommonScreenHeader(
                title = Res.string.moving_average,
                onBack = onBack,
                trailing = {
                    ProfilePillButton(
                        text = stringResource(resource = Res.string.log),
                        onClick = onLogWeight
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            WeightHistoryTimeRange(
                modifier = Modifier.fillMaxWidth(),
                selectedTimeRange = viewState.timeRange,
                onSelectTimeRange = callbacks::onTimePeriodSelect
            )
            val period = viewState.period ?: return@Column
            WeightHistoryFilter(
                modifier = Modifier.fillMaxWidth(),
                period = period,
                canGoBack = viewState.canGoBack,
                canGoNext = viewState.canGoNext,
                onBackButtonClicked = callbacks::onPreviousPeriod,
                onNextButtonClicked = callbacks::onNextPeriod
            )
            val summary = viewState.movingAverageSummary
            if (viewState.periodWeights.isEmpty() || summary == null) {
                EmptyPeriod()
                return@Column
            }
            MovingAverageCard(
                period = period,
                viewState = viewState,
                summary = summary,
                onSelectWindow = callbacks::onMovingAverageWindowSelect
            )
            MovingAverageEntries(
                weightList = viewState.periodWeights,
                movingAverage = viewState.periodMovingAverage,
                averageChanges = viewState.periodMovingAverageChanges,
                window = viewState.movingAverageWindow
            )
        }
    }
}

@Composable
private fun MovingAverageCard(
    period: WeightPeriod,
    viewState: WeightHistoryViewState,
    summary: MovingAverageSummary,
    onSelectWindow: (MovingAverageWindow) -> Unit
) {
    val days = summary.window.days
    var selection by remember(period) { mutableStateOf<WeightRange?>(value = null) }
    var isSelecting by remember(period) { mutableStateOf(value = false) }
    val latestDate = viewState.periodWeights.last().date
    val chartDescription = stringResource(
        resource = Res.string.moving_average_chart_description,
        days,
        viewState.periodWeights.first().date.toAbbreviatedMonthDay(),
        latestDate.toAbbreviatedMonthDay(),
        summary.average.formatWeight(),
        weightUnitLabel()
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .tourTarget(target = TourTarget.MOVING_AVERAGE_CHART)
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 24.dp))
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = if (latestDate == getCurrentDate()) {
                        stringResource(resource = Res.string.moving_average_today, days)
                    } else {
                        stringResource(resource = Res.string.moving_average_on_date, days, latestDate.toAbbreviatedMonthDay())
                    },
                    style = typography.regular11,
                    color = colors.muted
                )
                Row(horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                    Text(
                        text = summary.average.formatWeight(),
                        style = typography.bold32,
                        color = colors.ink,
                        modifier = Modifier.alignByBaseline()
                    )
                    Text(
                        text = weightUnitLabel(),
                        style = typography.regular13,
                        color = colors.muted,
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }
            summary.change?.let { ChangeBadge(change = it) }
        }
        WindowChips(selectedWindow = summary.window, onSelectWindow = onSelectWindow)
        MovingAverageGraph(
            modifier = Modifier.fillMaxWidth().height(height = 170.dp),
            weightList = viewState.periodWeights.inWeightUnit(),
            movingAverage = viewState.periodMovingAverage.inWeightUnit(),
            period = period,
            goalPhases = viewState.goalPhases,
            selection = selection,
            onSelectionStart = { date ->
                selection = WeightRange(anchor = date, end = date)
                isSelecting = true
            },
            onSelectionDrag = { date -> selection = selection?.copy(end = date) },
            onSelectionEnd = { isSelecting = false },
            contentDescription = chartDescription
        )
        Legend(window = summary.window)
        val range = selection
        if (range == null) {
            CompareHint()
        } else {
            SelectedRangeCard(
                range = range,
                values = viewState.periodMovingAverage,
                goalPhases = viewState.goalPhases,
                unitLabel = stringResource(resource = Res.string.unit_avg, weightUnitLabel()),
                title = { dayCount ->
                    stringResource(
                        resource = if (isSelecting) Res.string.moving_average_selecting else Res.string.moving_average_selected_range,
                        dayCount,
                        days
                    )
                },
                onClear = {
                    selection = null
                    isSelecting = false
                }
            )
        }
    }
}

@Composable
private fun ChangeBadge(change: Double) {
    val color = change.changeColor()
    val shape = RoundedCornerShape(percent = 50)
    Row(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .height(height = 28.dp)
            .clip(shape = shape)
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        val icon = when {
            change < -SAME_THRESHOLD_KG -> Res.drawable.ic_trend_down
            change > SAME_THRESHOLD_KG -> Res.drawable.ic_nav_progress
            else -> null
        }
        icon?.let {
            Icon(
                painter = painterResource(resource = it),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(size = 14.dp)
            )
        }
        Text(
            text = stringResource(resource = Res.string.moving_average_change_vs_week_ago, change.toSignedWeight(), weightUnitLabel()),
            style = typography.bold11,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
private fun WindowChips(
    selectedWindow: MovingAverageWindow,
    onSelectWindow: (MovingAverageWindow) -> Unit
) {
    val groupLabel = stringResource(resource = Res.string.moving_average_window)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = groupLabel }
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        MovingAverageWindow.entries.forEach { window ->
            val isSelected = window == selectedWindow
            val shape = RoundedCornerShape(size = 12.dp)
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 44.dp)
                    .clip(shape = shape)
                    .background(color = if (isSelected) colors.accent else colors.surface)
                    .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
                    .selectable(selected = isSelected, role = Role.RadioButton) { onSelectWindow(window) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = Res.string.moving_average_chip, window.days),
                    style = typography.bold13,
                    color = if (isSelected) colors.onAccent else colors.ink
                )
            }
        }
    }
}

@Composable
private fun Legend(window: MovingAverageWindow) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(space = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(size = 7.dp)
                    .border(width = 1.2.dp, color = colors.muted, shape = CircleShape)
            )
            Text(
                text = stringResource(resource = Res.string.daily_weigh_in),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 16.dp, height = 3.dp)
                    .background(color = colors.accent, shape = RoundedCornerShape(percent = 50))
            )
            Text(
                text = stringResource(resource = Res.string.moving_average_days, window.days),
                style = typography.regular11,
                color = colors.muted
            )
        }
        GoalLegend()
    }
}

@Composable
private fun MovingAverageEntries(
    weightList: List<Weight>,
    movingAverage: List<Weight>,
    averageChanges: Map<LocalDate, Double>,
    window: MovingAverageWindow
) {
    val averageByDate = movingAverage.associate { it.date to it.weight }
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(
            text = stringResource(resource = Res.string.entries),
            style = typography.bold18,
            color = colors.ink
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(height = 36.dp).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(resource = Res.string.date).uppercase(),
                    style = typography.bold11,
                    color = colors.muted,
                    modifier = Modifier.weight(weight = 1f)
                )
                Text(
                    text = stringResource(resource = Res.string.weight).uppercase(),
                    style = typography.bold11,
                    color = colors.muted,
                    modifier = Modifier.width(width = WEIGHT_COLUMN_WIDTH)
                )
                Text(
                    text = stringResource(resource = Res.string.moving_average_days_short, window.days).uppercase(),
                    style = typography.bold11,
                    color = colors.muted,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(width = AVERAGE_COLUMN_WIDTH)
                )
                Text(
                    text = stringResource(resource = Res.string.change).uppercase(),
                    style = typography.bold11,
                    color = colors.muted,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(width = CHANGE_COLUMN_WIDTH)
                )
            }
            weightList.sortedByDescending { it.date }.forEach { weight ->
                HorizontalDivider(thickness = 1.dp, color = colors.divider)
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = weight.date.toEntryDate(),
                        style = typography.regular13,
                        color = colors.muted,
                        modifier = Modifier.weight(weight = 1f)
                    )
                    Text(
                        text = weight.weight.formatWeight(),
                        style = typography.bold13,
                        color = colors.ink,
                        modifier = Modifier.width(width = WEIGHT_COLUMN_WIDTH)
                    )
                    Text(
                        text = averageByDate[weight.date]?.formatWeight() ?: "—",
                        style = typography.bold13,
                        color = colors.accentInk,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(width = AVERAGE_COLUMN_WIDTH)
                    )
                    val change = averageChanges[weight.date]
                    Text(
                        text = when {
                            change == null -> "—"
                            change < -SAME_THRESHOLD_KG -> "↓ " + abs(change).formatWeight()
                            change > SAME_THRESHOLD_KG -> "↑ " + change.formatWeight()
                            else -> "0.00"
                        },
                        style = typography.bold13,
                        color = change?.changeColor() ?: colors.muted,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(width = CHANGE_COLUMN_WIDTH)
                    )
                }
            }
        }
    }
}

@Composable
private fun Double.changeColor(): Color = when {
    this < -SAME_THRESHOLD_KG -> colors.accentInk
    this > SAME_THRESHOLD_KG -> colors.fat
    else -> colors.muted
}

@Preview(showBackground = true)
@Composable
private fun PreviewMovingAverageScreen() {
    MacroTrackTheme {
        val weights = listOf(
            Weight(date = LocalDate(2026, 9, 28), weight = 75.2),
            Weight(date = LocalDate(2026, 10, 1), weight = 74.8),
            Weight(date = LocalDate(2026, 10, 2), weight = 75.1),
            Weight(date = LocalDate(2026, 10, 3), weight = 74.6),
            Weight(date = LocalDate(2026, 10, 5), weight = 74.9),
            Weight(date = LocalDate(2026, 10, 6), weight = 74.4),
            Weight(date = LocalDate(2026, 10, 8), weight = 74.2)
        )
        val period = WeightPeriod(WeightHistoryTimeRangeEnum.MONTH, LocalDate(2026, 10, 1), LocalDate(2026, 10, 31))
        MovingAverageScreen(
            viewState = WeightHistoryViewState(
                allWeights = weights,
                goalPhases = listOf(
                    GoalPhase(start = LocalDate(2026, 3, 2), end = LocalDate(2026, 10, 4), goal = Goal.MAINTAIN_WEIGHT),
                    GoalPhase(start = LocalDate(2026, 10, 5), end = LocalDate(2026, 10, 8), goal = Goal.LOSE_WEIGHT)
                ),
                period = period,
                periodWeights = weights.drop(n = 1),
                periodMovingAverage = weights.movingAverage(windowDays = 7).drop(n = 1),
                periodMovingAverageChanges = weights.movingAverage(windowDays = 7).changesFromPrevious(),
                movingAverageSummary = weights.movingAverageSummary(
                    date = LocalDate(2026, 10, 8),
                    window = MovingAverageWindow.SEVEN
                ),
                canGoBack = true
            ),
            callbacks = WeightHistoryCallbacks.default(),
            onBack = {},
            onLogWeight = {}
        )
    }
}
