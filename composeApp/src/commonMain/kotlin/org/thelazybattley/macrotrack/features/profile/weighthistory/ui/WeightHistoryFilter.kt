package org.thelazybattley.macrotrack.features.profile.weighthistory.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.all_time_since
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.next_period
import macrotrack.composeapp.generated.resources.previous_period
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.features.profile.weighthistory.WeightPeriod
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun WeightHistoryFilter(
    modifier: Modifier = Modifier,
    period: WeightPeriod,
    canGoBack: Boolean,
    canGoNext: Boolean,
    onBackButtonClicked: () -> Unit,
    onNextButtonClicked: () -> Unit
) {
    if (period.range == WeightHistoryTimeRangeEnum.ALL) {
        Text(
            text = stringResource(resource = Res.string.all_time_since, period.start.toFullDate()),
            style = typography.bold16,
            color = colors.ink,
            textAlign = TextAlign.Center,
            modifier = modifier.padding(vertical = 10.dp)
        )
        return
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        PeriodButton(
            enabled = canGoBack,
            contentDescription = stringResource(resource = Res.string.previous_period),
            rotation = 0f,
            onClick = onBackButtonClicked
        )
        Text(
            text = period.longLabel(),
            style = typography.bold16,
            color = colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(weight = 1f)
        )
        PeriodButton(
            enabled = canGoNext,
            contentDescription = stringResource(resource = Res.string.next_period),
            rotation = 180f,
            onClick = onNextButtonClicked
        )
    }
}

@Composable
private fun PeriodButton(
    enabled: Boolean,
    contentDescription: String,
    rotation: Float,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 12.dp)
    Box(
        modifier = Modifier
            .size(size = 40.dp)
            .clip(shape = shape)
            .background(color = if (enabled) colors.surface else Color.Transparent)
            .border(width = 1.dp, color = if (enabled) colors.line else colors.divider, shape = shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_left),
            contentDescription = contentDescription,
            tint = if (enabled) colors.ink else colors.faint,
            modifier = Modifier.size(size = 18.dp).rotate(degrees = rotation)
        )
    }
}

/** e.g. "Oct 5 – 11, 2026", "October 2026", "Aug – Oct 2026". */
fun WeightPeriod.longLabel(): String = when (range) {
    WeightHistoryTimeRangeEnum.WEEK -> {
        val endText = if (start.month == end.month) end.day.toString() else end.toAbbreviatedMonthDay()
        val startText = if (start.year == end.year) start.toAbbreviatedMonthDay() else start.toFullDate()
        "$startText – $endText, ${end.year}"
    }
    WeightHistoryTimeRangeEnum.MONTH -> "${start.monthName()} ${start.year}"
    WeightHistoryTimeRangeEnum.THREE_MONTHS -> if (start.year == end.year) {
        "${start.shortMonth()} – ${end.shortMonth()} ${end.year}"
    } else {
        "${start.shortMonth()} ${start.year} – ${end.shortMonth()} ${end.year}"
    }
    WeightHistoryTimeRangeEnum.ALL -> "${start.toFullDate()} – ${end.toFullDate()}"
}

/** e.g. "Oct 5 – 11", "October", "Aug – Oct", used when comparing two periods. */
fun WeightPeriod.shortLabel(): String = when (range) {
    WeightHistoryTimeRangeEnum.WEEK -> {
        val endText = if (start.month == end.month) end.day.toString() else end.toAbbreviatedMonthDay()
        "${start.toAbbreviatedMonthDay()} – $endText"
    }
    WeightHistoryTimeRangeEnum.MONTH -> start.monthName()
    WeightHistoryTimeRangeEnum.THREE_MONTHS -> "${start.shortMonth()} – ${end.shortMonth()}"
    WeightHistoryTimeRangeEnum.ALL -> longLabel()
}

internal fun LocalDate.toFullDate(): String = toAbbreviatedMonthDay() + ", " + year

private fun LocalDate.monthName(): String = month.name.lowercase().replaceFirstChar { it.uppercase() }

private fun LocalDate.shortMonth(): String = monthName().take(n = 3)

@Preview(showBackground = true)
@Composable
private fun PreviewWeightHistoryFilter() {
    MacroTrackTheme {
        WeightHistoryFilter(
            modifier = Modifier.fillMaxWidth().height(height = 56.dp).padding(horizontal = 16.dp),
            period = WeightPeriod(
                range = WeightHistoryTimeRangeEnum.WEEK,
                start = LocalDate(2026, 9, 28),
                end = LocalDate(2026, 10, 4)
            ),
            canGoBack = true,
            canGoNext = false,
            onBackButtonClicked = {},
            onNextButtonClicked = {}
        )
    }
}
