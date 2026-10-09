package org.thelazybattley.macrotrack.features.measurements.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.biceps_average
import macrotrack.composeapp.generated.resources.body_measurements
import macrotrack.composeapp.generated.resources.change_since_value
import macrotrack.composeapp.generated.resources.compared_with_value
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.last_measured
import macrotrack.composeapp.generated.resources.log
import macrotrack.composeapp.generated.resources.log_measurements
import macrotrack.composeapp.generated.resources.no_measurements_prompt
import macrotrack.composeapp.generated.resources.no_measurements_yet
import macrotrack.composeapp.generated.resources.value_with_unit
import macrotrack.composeapp.generated.resources.waist
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.MeasurementGroup
import org.thelazybattley.macrotrack.domain.model.MeasurementType
import org.thelazybattley.macrotrack.features.measurements.MeasurementSummary
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnit
import org.thelazybattley.macrotrack.features.measurements.bicepAverage
import org.thelazybattley.macrotrack.features.measurements.formatChange
import org.thelazybattley.macrotrack.features.measurements.formatMeasure
import org.thelazybattley.macrotrack.features.measurements.ui.MeasurementUnitToggle
import org.thelazybattley.macrotrack.features.measurements.ui.toMeasurementDate
import org.thelazybattley.macrotrack.features.profile.ui.ProfilePillButton
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun MeasurementsScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val viewModel = koinViewModel<MeasurementsViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    MeasurementsScreen(
        modifier = modifier,
        viewState = viewState,
        onBack = onPopBackStack,
        onLog = { onNavigate(AppDestinations.Root.MeasurementsLog.route) },
        onOpenType = { type -> onNavigate(AppDestinations.Root.MeasurementDetail.createRoute(type = type.name)) },
        onUnitSelected = viewModel::onUnitSelected
    )
}

@Composable
private fun MeasurementsScreen(
    modifier: Modifier = Modifier,
    viewState: MeasurementsViewState,
    onBack: () -> Unit,
    onLog: () -> Unit,
    onOpenType: (MeasurementType) -> Unit,
    onUnitSelected: (MeasurementUnit) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CommonScreenHeader(
                title = Res.string.body_measurements,
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
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            if (viewState.summaries.isEmpty()) {
                EmptyMeasurements(onLog = onLog)
                return@Column
            }
            SummaryCard(viewState = viewState, onUnitSelected = onUnitSelected)
            MeasurementGroup.entries.forEach { group ->
                val rows = MeasurementType.entries.filter { it.group == group }.mapNotNull { viewState.summaries[it] }
                if (rows.isEmpty()) return@forEach
                MeasurementGroupList(
                    group = group,
                    rows = rows,
                    unit = viewState.unit,
                    onOpenType = onOpenType
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    viewState: MeasurementsViewState,
    onUnitSelected: (MeasurementUnit) -> Unit
) {
    val unitLabel = stringResource(resource = viewState.unit.label)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.hero)
            .padding(all = 18.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = stringResource(resource = Res.string.last_measured),
                    style = typography.regular11,
                    color = colors.muted
                )
                Text(
                    text = viewState.lastMeasured?.toMeasurementDate().orEmpty(),
                    style = typography.bold24,
                    color = colors.ink
                )
                viewState.previousMeasured?.let { previous ->
                    Text(
                        text = stringResource(resource = Res.string.compared_with_value, previous.toAbbreviatedMonthDay()),
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
            }
            MeasurementUnitToggle(selected = viewState.unit, onSelected = onUnitSelected)
        }
        val highlights = listOfNotNull(
            viewState.summaries[MeasurementType.WAIST]?.let { stringResource(resource = Res.string.waist) to it },
            viewState.summaries.bicepAverage()?.let { stringResource(resource = Res.string.biceps_average) to it }
        )
        if (highlights.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                highlights.forEach { (label, summary) ->
                    HighlightTile(
                        modifier = Modifier.weight(weight = 1f),
                        label = label,
                        summary = summary,
                        unit = viewState.unit,
                        unitLabel = unitLabel
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightTile(
    modifier: Modifier = Modifier,
    label: String,
    summary: MeasurementSummary,
    unit: MeasurementUnit,
    unitLabel: String
) {
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = label, style = typography.regular11, color = colors.muted)
        Text(
            text = stringResource(
                resource = Res.string.value_with_unit,
                unit.fromCm(cm = summary.latest.valueCm).formatMeasure(),
                unitLabel
            ),
            style = typography.bold18,
            color = colors.ink
        )
        val change = summary.changeCm
        val previous = summary.previous
        if (change != null && previous != null) {
            Text(
                text = stringResource(
                    resource = Res.string.change_since_value,
                    unit.fromCm(cm = change).formatChange(),
                    previous.date.toAbbreviatedMonthDay()
                ),
                style = typography.bold11,
                color = colors.accentInk
            )
        }
    }
}

@Composable
private fun MeasurementGroupList(
    group: MeasurementGroup,
    rows: List<MeasurementSummary>,
    unit: MeasurementUnit,
    onOpenType: (MeasurementType) -> Unit
) {
    val unitLabel = stringResource(resource = unit.label)
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(
            text = stringResource(resource = group.title),
            style = typography.bold12,
            color = colors.muted
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
        ) {
            rows.forEachIndexed { index, summary ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable { onOpenType(summary.type) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
                ) {
                    Column(modifier = Modifier.weight(weight = 1f)) {
                        Text(
                            text = stringResource(resource = summary.type.title),
                            style = typography.bold14,
                            color = colors.ink
                        )
                        Text(
                            text = summary.latest.date.toAbbreviatedMonthDay(),
                            style = typography.regular11,
                            color = colors.muted
                        )
                    }
                    Text(
                        text = stringResource(
                            resource = Res.string.value_with_unit,
                            unit.fromCm(cm = summary.latest.valueCm).formatMeasure(),
                            unitLabel
                        ),
                        style = typography.bold16,
                        color = colors.ink
                    )
                    Text(
                        text = summary.changeCm?.let { unit.fromCm(cm = it).formatChange() } ?: "—",
                        style = typography.bold11,
                        color = if (summary.changeCm == null || summary.changeCm == 0.0) colors.muted else colors.ink,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(width = 56.dp)
                    )
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_chevron_left),
                        contentDescription = null,
                        tint = colors.faint,
                        modifier = Modifier.size(size = 16.dp).rotate(degrees = 180f)
                    )
                }
                if (index != rows.lastIndex) {
                    HorizontalDivider(thickness = 1.dp, color = colors.divider)
                }
            }
        }
    }
}

@Composable
private fun EmptyMeasurements(onLog: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.hero)
            .padding(all = 20.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.no_measurements_yet),
            style = typography.bold18,
            color = colors.ink
        )
        Text(
            text = stringResource(resource = Res.string.no_measurements_prompt),
            style = typography.regular13,
            color = colors.muted
        )
        CommonPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = Res.string.log_measurements,
            onClick = onLog
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMeasurementsScreen() {
    MacroTrackTheme {
        MeasurementsScreen(
            viewState = MeasurementsViewState(),
            onBack = {},
            onLog = {},
            onOpenType = {},
            onUnitSelected = {}
        )
    }
}
