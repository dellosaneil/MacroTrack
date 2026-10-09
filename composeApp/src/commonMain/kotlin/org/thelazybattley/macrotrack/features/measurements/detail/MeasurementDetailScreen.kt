package org.thelazybattley.macrotrack.features.measurements.detail

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.change
import macrotrack.composeapp.generated.resources.entries
import macrotrack.composeapp.generated.resources.latest
import macrotrack.composeapp.generated.resources.log
import macrotrack.composeapp.generated.resources.no_entries_for_period
import macrotrack.composeapp.generated.resources.start
import macrotrack.composeapp.generated.resources.value_with_unit
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.measurements.formatChange
import org.thelazybattley.macrotrack.features.measurements.formatMeasure
import org.thelazybattley.macrotrack.features.measurements.ui.toMeasurementDate
import org.thelazybattley.macrotrack.features.profile.ui.ProfilePillButton
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightPeriod
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryGraph
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryTimeRangeEnum
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun MeasurementDetailScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val viewModel = koinViewModel<MeasurementDetailViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    MeasurementDetailScreen(
        modifier = modifier,
        viewState = viewState,
        onBack = onPopBackStack,
        onLog = { onNavigate(AppDestinations.Root.MeasurementsLog.route) },
        onRangeSelected = viewModel::onRangeSelected
    )
}

@Composable
private fun MeasurementDetailScreen(
    modifier: Modifier = Modifier,
    viewState: MeasurementDetailViewState,
    onBack: () -> Unit,
    onLog: () -> Unit,
    onRangeSelected: (MeasurementRange) -> Unit
) {
    val unitLabel = stringResource(resource = viewState.unit.label)
    val values = viewState.entries.map { viewState.unit.fromCm(cm = it.valueCm) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CommonScreenHeader(
                title = viewState.type.title,
                onBack = onBack,
                trailing = {
                    ProfilePillButton(text = stringResource(resource = Res.string.log), onClick = onLog)
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
            RangeTabs(selected = viewState.range, onSelected = onRangeSelected)
            if (values.isEmpty()) {
                Text(
                    text = stringResource(resource = Res.string.no_entries_for_period),
                    style = typography.regular13,
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = RoundedCornerShape(size = 20.dp))
                        .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 20.dp))
                        .padding(vertical = 32.dp, horizontal = 16.dp)
                )
                return@Column
            }
            val change = values.last() - values.first()
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
                            text = stringResource(resource = viewState.range.changeLabel),
                            style = typography.regular11,
                            color = colors.muted
                        )
                        Text(
                            text = stringResource(resource = Res.string.value_with_unit, change.signed(), unitLabel),
                            style = typography.bold32,
                            color = colors.ink
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(resource = Res.string.latest),
                            style = typography.regular11,
                            color = colors.muted
                        )
                        Text(
                            text = stringResource(resource = Res.string.value_with_unit, values.last().formatMeasure(), unitLabel),
                            style = typography.bold16,
                            color = colors.ink
                        )
                    }
                }
                WeightHistoryGraph(
                    modifier = Modifier.fillMaxWidth().height(height = 170.dp),
                    weightList = viewState.entries.mapIndexed { index, entry ->
                        Weight(date = entry.date, weight = values[index])
                    },
                    period = WeightPeriod(
                        range = WeightHistoryTimeRangeEnum.ALL,
                        start = viewState.start,
                        end = viewState.end
                    ),
                    unitLabel = unitLabel
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                StatTile(Modifier.weight(weight = 1f), stringResource(resource = Res.string.start), values.first().formatMeasure())
                StatTile(Modifier.weight(weight = 1f), stringResource(resource = Res.string.latest), values.last().formatMeasure())
                StatTile(Modifier.weight(weight = 1f), stringResource(resource = Res.string.change), change.signed())
            }
            EntriesList(viewState = viewState, unitLabel = unitLabel)
        }
    }
}

@Composable
private fun RangeTabs(
    selected: MeasurementRange,
    onSelected: (MeasurementRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.divider)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
            .padding(all = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        MeasurementRange.entries.forEach { range ->
            val isSelected = range == selected
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 38.dp)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .clickable { onSelected(range) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = range.label),
                    style = typography.bold13,
                    color = if (isSelected) colors.ink else colors.muted
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    modifier: Modifier,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
            .padding(all = 10.dp)
    ) {
        Text(text = label, style = typography.regular11, color = colors.muted)
        Text(text = value, style = typography.bold16, color = colors.ink)
    }
}

@Composable
private fun EntriesList(
    viewState: MeasurementDetailViewState,
    unitLabel: String
) {
    val entries = viewState.entries.sortedByDescending { it.date }
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
            entries.forEachIndexed { index, entry ->
                val previous = viewState.allEntries.lastOrNull { it.date < entry.date }
                val difference = previous?.let { viewState.unit.fromCm(cm = entry.valueCm - it.valueCm) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.date.toMeasurementDate(),
                        style = typography.regular13,
                        color = colors.muted,
                        modifier = Modifier.weight(weight = 1f)
                    )
                    Text(
                        text = stringResource(
                            resource = Res.string.value_with_unit,
                            viewState.unit.fromCm(cm = entry.valueCm).formatMeasure(),
                            unitLabel
                        ),
                        style = typography.bold13,
                        color = colors.ink,
                        modifier = Modifier.width(width = 90.dp)
                    )
                    Text(
                        text = difference?.formatChange() ?: "—",
                        style = typography.bold13,
                        color = if (difference == null || difference == 0.0) colors.muted else colors.ink,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(width = 60.dp)
                    )
                }
                if (index != entries.lastIndex) {
                    HorizontalDivider(thickness = 1.dp, color = colors.divider)
                }
            }
        }
    }
}

private fun Double.signed(): String = when {
    this >= 0.005 -> "+" + formatMeasure()
    this <= -0.005 -> formatMeasure()
    else -> "0.00"
}

@Preview(showBackground = true)
@Composable
private fun PreviewMeasurementDetailScreen() {
    MacroTrackTheme {
        MeasurementDetailScreen(
            viewState = MeasurementDetailViewState(),
            onBack = {},
            onLog = {},
            onRangeSelected = {}
        )
    }
}
