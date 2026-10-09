package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.average_weight
import macrotrack.composeapp.generated.resources.change
import macrotrack.composeapp.generated.resources.entries
import macrotrack.composeapp.generated.resources.higher_than_value
import macrotrack.composeapp.generated.resources.ic_nav_progress
import macrotrack.composeapp.generated.resources.ic_trend_down
import macrotrack.composeapp.generated.resources.ic_trend_flat
import macrotrack.composeapp.generated.resources.latest
import macrotrack.composeapp.generated.resources.lower_than_value
import macrotrack.composeapp.generated.resources.no_earlier_period_to_compare
import macrotrack.composeapp.generated.resources.no_entries_to_compare
import macrotrack.composeapp.generated.resources.same_as_value
import macrotrack.composeapp.generated.resources.start
import macrotrack.composeapp.generated.resources.value_kg_unit
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.features.profile.ui.formatKg
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightHistoryViewState
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightPeriod
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs

private const val SAME_THRESHOLD_KG = 0.005

@Composable
fun WeightHistoryAverageCard(
    modifier: Modifier = Modifier,
    viewState: WeightHistoryViewState
) {
    val period = viewState.period ?: return
    val average = viewState.average ?: return
    val previousPeriod = viewState.previousPeriod
    val previousAverage = viewState.previousAverage

    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.hero)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.average_weight),
            style = typography.bold12,
            color = colors.muted
        )
        Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            AverageTile(
                modifier = Modifier.weight(weight = 1f),
                label = period.shortLabel(),
                value = average
            )
            if (previousPeriod != null && previousAverage != null) {
                AverageTile(
                    modifier = Modifier.weight(weight = 1f),
                    label = previousPeriod.shortLabel(),
                    value = previousAverage
                )
            }
        }
        when {
            previousPeriod == null -> Text(
                text = stringResource(resource = Res.string.no_earlier_period_to_compare),
                style = typography.regular11,
                color = colors.muted
            )

            previousAverage == null -> Text(
                text = stringResource(resource = Res.string.no_entries_to_compare, previousPeriod.shortLabel()),
                style = typography.regular11,
                color = colors.muted
            )

            else -> AverageDifference(
                difference = average - previousAverage,
                previousLabel = previousPeriod.shortLabel()
            )
        }
    }
}

@Composable
private fun AverageTile(
    modifier: Modifier = Modifier,
    label: String,
    value: Double
) {
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = typography.regular11,
            color = colors.muted
        )
        Text(
            text = stringResource(resource = Res.string.value_kg_unit, value.formatKg()),
            style = typography.bold18,
            color = colors.ink
        )
    }
}

@Composable
private fun AverageDifference(
    difference: Double,
    previousLabel: String
) {
    val (icon, color, note) = when {
        difference < -SAME_THRESHOLD_KG -> Triple(Res.drawable.ic_trend_down, colors.accentInk, Res.string.lower_than_value)
        difference > SAME_THRESHOLD_KG -> Triple(Res.drawable.ic_nav_progress, colors.fat, Res.string.higher_than_value)
        else -> Triple(Res.drawable.ic_trend_flat, colors.ink, Res.string.same_as_value)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 36.dp)
                .background(color = colors.surface, shape = RoundedCornerShape(size = 12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(size = 18.dp)
            )
        }
        Text(
            text = stringResource(resource = Res.string.value_kg_unit, difference.toSignedKg()),
            style = typography.bold18,
            color = color
        )
        Text(
            text = stringResource(resource = note, previousLabel),
            style = typography.regular13,
            color = colors.muted,
            modifier = Modifier.weight(weight = 1f)
        )
    }
}

@Composable
fun WeightHistoryStatistics(
    modifier: Modifier = Modifier,
    weightList: List<Weight>
) {
    val first = weightList.firstOrNull() ?: return
    val last = weightList.last()
    val change = last.weight - first.weight
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        StatTile(
            modifier = Modifier.weight(weight = 1f),
            label = stringResource(resource = Res.string.start),
            value = first.weight.formatKg()
        )
        StatTile(
            modifier = Modifier.weight(weight = 1f),
            label = stringResource(resource = Res.string.latest),
            value = last.weight.formatKg()
        )
        StatTile(
            modifier = Modifier.weight(weight = 1f),
            label = stringResource(resource = Res.string.change),
            value = change.toSignedKg(),
            valueColor = change.changeColor()
        )
    }
}

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color = colors.ink
) {
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
            .padding(all = 10.dp)
    ) {
        Text(
            text = label,
            style = typography.regular11,
            color = colors.muted
        )
        Text(
            text = value,
            style = typography.bold16,
            color = valueColor
        )
    }
}

@Composable
fun WeightHistoryEntries(
    modifier: Modifier = Modifier,
    weightList: List<Weight>,
    allWeights: List<Weight>
) {
    if (weightList.isEmpty()) return
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
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
            val entries = weightList.sortedByDescending { it.date }
            entries.forEachIndexed { index, weight ->
                val previous = allWeights.lastOrNull { it.date < weight.date }
                EntryRow(weight = weight, previousWeight = previous?.weight)
                if (index != entries.lastIndex) {
                    HorizontalDivider(thickness = 1.dp, color = colors.divider)
                }
            }
        }
    }
}

@Composable
private fun EntryRow(
    weight: Weight,
    previousWeight: Double?
) {
    val difference = previousWeight?.let { weight.weight - it }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = weight.date.toEntryDate(),
            style = typography.regular13,
            color = colors.muted,
            modifier = Modifier.weight(weight = 1f)
        )
        Text(
            text = stringResource(resource = Res.string.value_kg_unit, weight.weight.formatKg()),
            style = typography.bold13,
            color = colors.ink,
            modifier = Modifier.width(width = 84.dp)
        )
        Text(
            text = when {
                difference == null -> "—"
                difference < -SAME_THRESHOLD_KG -> "↓ " + abs(difference).formatKg()
                difference > SAME_THRESHOLD_KG -> "↑ " + difference.formatKg()
                else -> "0.00"
            },
            style = typography.bold13,
            color = difference?.changeColor() ?: colors.muted,
            textAlign = TextAlign.End,
            modifier = Modifier.width(width = 64.dp)
        )
    }
}

@Composable
private fun Double.changeColor(): Color = when {
    this < -SAME_THRESHOLD_KG -> colors.accentInk
    this > SAME_THRESHOLD_KG -> colors.fat
    else -> colors.muted
}

internal fun Double.toSignedKg(): String = when {
    this > SAME_THRESHOLD_KG -> "+" + formatKg()
    this < -SAME_THRESHOLD_KG -> formatKg()
    else -> "0.00"
}

private fun LocalDate.toEntryDate(): String {
    val dayName = dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(n = 3)
    return "$dayName, ${toFullDate()}"
}

@Preview(showBackground = true)
@Composable
private fun PreviewWeightHistoryStatistics() {
    MacroTrackTheme {
        val weights = listOf(
            Weight(date = LocalDate(2026, 10, 1), weight = 74.8),
            Weight(date = LocalDate(2026, 10, 8), weight = 74.2)
        )
        Column(
            modifier = Modifier.padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            WeightHistoryAverageCard(
                modifier = Modifier.fillMaxWidth(),
                viewState = WeightHistoryViewState(
                    period = WeightPeriod(WeightHistoryTimeRangeEnum.MONTH, LocalDate(2026, 10, 1), LocalDate(2026, 10, 31)),
                    periodWeights = weights,
                    previousPeriod = WeightPeriod(WeightHistoryTimeRangeEnum.MONTH, LocalDate(2026, 9, 1), LocalDate(2026, 9, 30)),
                    previousWeights = listOf(Weight(date = LocalDate(2026, 9, 7), weight = 76.0))
                )
            )
            WeightHistoryStatistics(modifier = Modifier.fillMaxWidth(), weightList = weights)
            Box(modifier = Modifier.height(height = 4.dp))
            WeightHistoryEntries(modifier = Modifier.fillMaxWidth(), weightList = weights, allWeights = weights)
        }
    }
}
