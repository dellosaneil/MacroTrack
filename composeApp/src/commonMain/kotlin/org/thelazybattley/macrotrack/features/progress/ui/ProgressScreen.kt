package org.thelazybattley.macrotrack.features.progress.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.all_measurements
import macrotrack.composeapp.generated.resources.biceps_average
import macrotrack.composeapp.generated.resources.body_measurements
import macrotrack.composeapp.generated.resources.compared_with_value
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.kg
import macrotrack.composeapp.generated.resources.last_logged
import macrotrack.composeapp.generated.resources.log_measurements
import macrotrack.composeapp.generated.resources.log_weight
import macrotrack.composeapp.generated.resources.measured_on_value
import macrotrack.composeapp.generated.resources.no_measurements_prompt
import macrotrack.composeapp.generated.resources.no_measurements_yet
import macrotrack.composeapp.generated.resources.no_weight_logged_yet
import macrotrack.composeapp.generated.resources.progress
import macrotrack.composeapp.generated.resources.value_with_unit
import macrotrack.composeapp.generated.resources.weight
import macrotrack.composeapp.generated.resources.weight_history
import macrotrack.composeapp.generated.resources.weight_month_summary
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.MeasurementType
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.measurements.MeasurementSummary
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnit
import org.thelazybattley.macrotrack.features.measurements.formatChange
import org.thelazybattley.macrotrack.features.measurements.formatMeasure
import org.thelazybattley.macrotrack.features.profile.ui.WeightBottomSheetContent
import org.thelazybattley.macrotrack.features.profile.ui.formatKg
import org.thelazybattley.macrotrack.features.progress.ProgressViewModel
import org.thelazybattley.macrotrack.features.progress.ProgressViewState
import org.thelazybattley.macrotrack.ui.common.CommonBottomSheet
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    val viewModel = koinViewModel<ProgressViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    ProgressScreen(
        modifier = modifier,
        viewState = viewState,
        onSaveWeight = viewModel::onSaveWeight,
        onNavigate = onNavigate
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressScreen(
    modifier: Modifier = Modifier,
    viewState: ProgressViewState,
    onSaveWeight: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val showLogWeightSheet = remember { mutableStateOf(value = false) }
    Column(
        modifier = modifier.verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.progress),
            style = typography.bold32,
            color = colors.ink
        )
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            QuickAction(
                modifier = Modifier.weight(weight = 1f),
                text = Res.string.log_weight,
                isPrimary = true,
                onClick = { showLogWeightSheet.value = true }
            )
            QuickAction(
                modifier = Modifier.weight(weight = 1f),
                text = Res.string.log_measurements,
                isPrimary = false,
                onClick = { onNavigate(AppDestinations.Root.MeasurementsLog.route) }
            )
        }
        WeightCard(
            viewState = viewState,
            onOpenHistory = { onNavigate(AppDestinations.Profile.WeightHistory.route) }
        )
        MeasurementsCard(
            viewState = viewState,
            onOpenAll = { onNavigate(AppDestinations.Profile.BodyMeasurements.route) },
            onOpenType = { type -> onNavigate(AppDestinations.Root.MeasurementDetail.createRoute(type = type.name)) },
            onLog = { onNavigate(AppDestinations.Root.MeasurementsLog.route) }
        )
    }

    CommonBottomSheet(
        bottomSheetState = rememberModalBottomSheetState(),
        showBottomSheet = showLogWeightSheet
    ) { sheetModifier ->
        WeightBottomSheetContent(
            modifier = sheetModifier,
            previousWeight = (viewState.latestWeight?.weight ?: viewState.userDetails?.weight)?.toCleanString().orEmpty(),
            onSaveWeight = { weight ->
                showLogWeightSheet.value = false
                onSaveWeight(weight)
            }
        )
    }
}

@Composable
private fun QuickAction(
    modifier: Modifier = Modifier,
    text: StringResource,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    val content = if (isPrimary) colors.onAccent else colors.accentInk
    Row(
        modifier = modifier
            .height(height = 48.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = if (isPrimary) colors.accent else colors.accentSoft)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_plus),
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(size = 18.dp)
        )
        Text(text = stringResource(resource = text), style = typography.bold14, color = content)
    }
}

@Composable
private fun ProgressCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 24.dp))
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        content()
    }
}

@Composable
private fun WeightCard(
    viewState: ProgressViewState,
    onOpenHistory: () -> Unit
) {
    val weight = viewState.latestWeight?.weight ?: viewState.userDetails?.weight
    ProgressCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(text = stringResource(resource = Res.string.weight), style = typography.regular11, color = colors.muted)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                ) {
                    Text(
                        text = weight?.formatKg() ?: "–",
                        style = typography.bold32,
                        color = colors.ink
                    )
                    Text(
                        text = stringResource(resource = Res.string.kg),
                        style = typography.regular13,
                        color = colors.muted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                val change = viewState.monthChange
                val average = viewState.monthAverage
                if (change != null && average != null) {
                    Text(
                        text = stringResource(
                            resource = Res.string.weight_month_summary,
                            (if (change > 0) "+" else "") + change.formatKg(),
                            average.formatKg()
                        ),
                        style = typography.bold11,
                        color = if (change > 0) colors.fat else colors.accentInk
                    )
                } else if (viewState.latestWeight == null) {
                    Text(
                        text = stringResource(resource = Res.string.no_weight_logged_yet),
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
            }
            viewState.latestWeight?.let { latest ->
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = stringResource(resource = Res.string.last_logged), style = typography.regular11, color = colors.muted)
                    Text(text = latest.date.toAbbreviatedMonthDay(), style = typography.bold13, color = colors.ink)
                }
            }
        }
        if (viewState.sparkline.size > 1) {
            Sparkline(weights = viewState.sparkline)
        }
        LinkRow(text = stringResource(resource = Res.string.weight_history), onClick = onOpenHistory)
    }
}

@Composable
private fun Sparkline(weights: List<Weight>) {
    val lineColor = colors.accent
    val areaColor = colors.accentSoft
    val markerColor = colors.surface
    Canvas(modifier = Modifier.fillMaxWidth().height(height = 64.dp)) {
        val values = weights.map { it.weight }
        val low = values.min()
        val high = values.max()
        val range = (high - low).takeIf { it > 0 } ?: 1.0
        val firstDay = weights.first().date.toEpochDays()
        val span = (weights.last().date.toEpochDays() - firstDay).coerceAtLeast(minimumValue = 1)
        val inset = 6.dp.toPx()
        val points = weights.map { weight ->
            Offset(
                x = inset + (size.width - inset * 2) * (weight.date.toEpochDays() - firstDay) / span.toFloat(),
                y = inset + (size.height - inset * 2) * ((high - weight.weight) / range).toFloat()
            )
        }
        val area = Path().apply {
            moveTo(x = points.first().x, y = size.height)
            points.forEach { lineTo(x = it.x, y = it.y) }
            lineTo(x = points.last().x, y = size.height)
            close()
        }
        drawPath(path = area, color = areaColor)
        val line = Path().apply {
            moveTo(x = points.first().x, y = points.first().y)
            points.drop(n = 1).forEach { lineTo(x = it.x, y = it.y) }
        }
        drawPath(path = line, color = lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color = markerColor, radius = 4.5.dp.toPx(), center = points.last())
        drawCircle(color = lineColor, radius = 4.5.dp.toPx(), center = points.last(), style = Stroke(width = 2.5.dp.toPx()))
    }
}

@Composable
private fun MeasurementsCard(
    viewState: ProgressViewState,
    onOpenAll: () -> Unit,
    onOpenType: (MeasurementType) -> Unit,
    onLog: () -> Unit
) {
    ProgressCard {
        Text(text = stringResource(resource = Res.string.body_measurements), style = typography.regular11, color = colors.muted)
        val lastMeasured = viewState.lastMeasured
        if (lastMeasured == null) {
            Text(text = stringResource(resource = Res.string.no_measurements_yet), style = typography.bold18, color = colors.ink)
            Text(text = stringResource(resource = Res.string.no_measurements_prompt), style = typography.regular13, color = colors.muted)
            LinkRow(text = stringResource(resource = Res.string.log_measurements), onClick = onLog)
            return@ProgressCard
        }
        Column {
            Text(
                text = stringResource(resource = Res.string.measured_on_value, lastMeasured.toAbbreviatedMonthDay()),
                style = typography.bold18,
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
        if (viewState.highlights.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                viewState.highlights.forEach { (type, summary) ->
                    MeasurementTile(
                        modifier = Modifier.weight(weight = 1f),
                        label = if (type == MeasurementType.LEFT_BICEP) {
                            stringResource(resource = Res.string.biceps_average)
                        } else {
                            stringResource(resource = type.title)
                        },
                        summary = summary,
                        unit = viewState.unit,
                        onClick = { onOpenType(type) }
                    )
                }
            }
        }
        LinkRow(text = stringResource(resource = Res.string.all_measurements), onClick = onOpenAll)
    }
}

@Composable
private fun MeasurementTile(
    modifier: Modifier = Modifier,
    label: String,
    summary: MeasurementSummary,
    unit: MeasurementUnit,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.background)
            .clickable(onClick = onClick)
            .padding(all = 10.dp)
    ) {
        Text(text = label, style = typography.regular11, color = colors.muted, maxLines = 1)
        Text(
            text = stringResource(
                resource = Res.string.value_with_unit,
                unit.fromCm(cm = summary.latest.valueCm).formatMeasure(),
                stringResource(resource = unit.label)
            ),
            style = typography.bold14,
            color = colors.ink,
            maxLines = 1
        )
        Text(
            text = summary.changeCm?.let { unit.fromCm(cm = it).formatChange() } ?: "—",
            style = typography.bold11,
            color = colors.muted
        )
    }
}

@Composable
private fun LinkRow(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 44.dp)
            .clip(shape = shape)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = typography.bold13,
            color = colors.ink,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(weight = 1f)
        )
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_left),
            contentDescription = null,
            tint = colors.faint,
            modifier = Modifier.size(size = 16.dp).rotate(degrees = 180f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewProgressScreen() {
    MacroTrackTheme {
        ProgressScreen(
            viewState = ProgressViewState(),
            onSaveWeight = {},
            onNavigate = {}
        )
    }
}
