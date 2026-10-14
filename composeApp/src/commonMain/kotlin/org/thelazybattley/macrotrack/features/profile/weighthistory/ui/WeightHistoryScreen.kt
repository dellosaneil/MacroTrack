package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.change_since_you_started
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_moving_average
import macrotrack.composeapp.generated.resources.kg
import macrotrack.composeapp.generated.resources.log
import macrotrack.composeapp.generated.resources.monthly_change
import macrotrack.composeapp.generated.resources.moving_average
import macrotrack.composeapp.generated.resources.moving_average_link_description
import macrotrack.composeapp.generated.resources.no_weight_logged_for_period
import macrotrack.composeapp.generated.resources.three_month_change
import macrotrack.composeapp.generated.resources.value_kg_unit
import macrotrack.composeapp.generated.resources.weekly_change
import macrotrack.composeapp.generated.resources.weight_entries
import macrotrack.composeapp.generated.resources.weight_history
import macrotrack.composeapp.generated.resources.weight_range_selected
import macrotrack.composeapp.generated.resources.weight_range_selecting
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.ui.ProfilePillButton
import org.thelazybattley.macrotrack.features.profile.weighthistory.GoalPhase
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightHistoryCallbacks
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightHistoryViewModel
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightHistoryViewState
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightPeriod
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun WeightHistoryScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val viewModel = koinViewModel<WeightHistoryViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    WeightHistoryScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = onPopBackStack,
        onLogWeight = { onNavigate(AppDestinations.Root.LogWeight.route) },
        onOpenMovingAverage = { onNavigate(AppDestinations.Root.MovingAverage.route) }
    )
}

@Composable
private fun WeightHistoryScreen(
    modifier: Modifier = Modifier,
    viewState: WeightHistoryViewState,
    callbacks: WeightHistoryCallbacks,
    onBack: () -> Unit,
    onLogWeight: () -> Unit,
    onOpenMovingAverage: () -> Unit
) {

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CommonScreenHeader(
                title = Res.string.weight_history,
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
            if (viewState.periodWeights.isEmpty()) {
                EmptyPeriod()
                return@Column
            }
            ChartCard(
                period = period,
                weightList = viewState.periodWeights,
                goalPhases = viewState.goalPhases,
                onOpenMovingAverage = onOpenMovingAverage
            )
            WeightHistoryAverageCard(modifier = Modifier.fillMaxWidth(), viewState = viewState)
            WeightHistoryStatistics(modifier = Modifier.fillMaxWidth(), weightList = viewState.periodWeights)
            WeightHistoryEntries(
                modifier = Modifier.fillMaxWidth(),
                weightList = viewState.periodWeights,
                allWeights = viewState.allWeights
            )
        }
    }
}

@Composable
private fun ChartCard(
    period: WeightPeriod,
    weightList: List<Weight>,
    goalPhases: List<GoalPhase>,
    onOpenMovingAverage: () -> Unit
) {
    var selection by remember(period) { mutableStateOf<WeightRange?>(value = null) }
    var isSelecting by remember(period) { mutableStateOf(value = false) }
    val change = weightList.last().weight - weightList.first().weight
    val changeLabel = when (period.range) {
        WeightHistoryTimeRangeEnum.WEEK -> Res.string.weekly_change
        WeightHistoryTimeRangeEnum.MONTH -> Res.string.monthly_change
        WeightHistoryTimeRangeEnum.THREE_MONTHS -> Res.string.three_month_change
        WeightHistoryTimeRangeEnum.ALL -> Res.string.change_since_you_started
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 24.dp))
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = stringResource(resource = changeLabel),
                    style = typography.regular11,
                    color = colors.muted
                )
                Text(
                    text = stringResource(resource = Res.string.value_kg_unit, change.toSignedKg()),
                    style = typography.bold32,
                    color = when {
                        change < -0.005 -> colors.accentInk
                        change > 0.005 -> colors.fat
                        else -> colors.ink
                    }
                )
            }
            Text(
                text = pluralStringResource(
                    resource = Res.plurals.weight_entries,
                    quantity = weightList.size,
                    weightList.size
                ),
                style = typography.regular11,
                color = colors.muted,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        WeightHistoryGraph(
            modifier = Modifier.fillMaxWidth().height(height = 170.dp),
            weightList = weightList,
            period = period,
            goalPhases = goalPhases,
            selection = selection,
            onSelectionStart = { date ->
                selection = WeightRange(anchor = date, end = date)
                isSelecting = true
            },
            onSelectionDrag = { date -> selection = selection?.copy(end = date) },
            onSelectionEnd = { isSelecting = false }
        )
        GoalLegend()
        val range = selection
        if (range == null) {
            CompareHint()
        } else {
            SelectedRangeCard(
                range = range,
                values = weightList,
                goalPhases = goalPhases,
                unitLabel = stringResource(resource = Res.string.kg),
                title = { dayCount ->
                    stringResource(
                        resource = if (isSelecting) Res.string.weight_range_selecting else Res.string.weight_range_selected,
                        dayCount
                    )
                },
                onClear = {
                    selection = null
                    isSelecting = false
                }
            )
        }
        MovingAverageLink(onClick = onOpenMovingAverage)
    }
}

@Composable
private fun MovingAverageLink(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.hero)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 36.dp)
                .background(color = colors.surface, shape = RoundedCornerShape(size = 10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_moving_average),
                contentDescription = null,
                tint = colors.accentInk,
                modifier = Modifier.size(size = 20.dp)
            )
        }
        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            Text(
                text = stringResource(resource = Res.string.moving_average),
                style = typography.bold14,
                color = colors.ink
            )
            Text(
                text = stringResource(resource = Res.string.moving_average_link_description),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.faint,
            modifier = Modifier.size(size = 16.dp)
        )
    }
}

@Composable
internal fun EmptyPeriod() {
    Text(
        text = stringResource(resource = Res.string.no_weight_logged_for_period),
        style = typography.regular13,
        color = colors.muted,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 20.dp))
            .padding(vertical = 32.dp, horizontal = 16.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewWeightHistoryScreen() {
    MacroTrackTheme {
        val weights = listOf(
            Weight(date = LocalDate(2026, 10, 1), weight = 74.8),
            Weight(date = LocalDate(2026, 10, 3), weight = 74.6),
            Weight(date = LocalDate(2026, 10, 8), weight = 74.2)
        )
        WeightHistoryScreen(
            viewState = WeightHistoryViewState(
                allWeights = weights,
                period = WeightPeriod(WeightHistoryTimeRangeEnum.MONTH, LocalDate(2026, 10, 1), LocalDate(2026, 10, 31)),
                periodWeights = weights,
                previousPeriod = WeightPeriod(WeightHistoryTimeRangeEnum.MONTH, LocalDate(2026, 9, 1), LocalDate(2026, 9, 30)),
                previousWeights = listOf(Weight(date = LocalDate(2026, 9, 7), weight = 76.0)),
                canGoBack = true
            ),
            callbacks = WeightHistoryCallbacks.default(),
            onBack = {},
            onLogWeight = {},
            onOpenMovingAverage = {}
        )
    }
}
