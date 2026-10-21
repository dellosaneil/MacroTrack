package org.thelazybattley.macrotrack.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_calendar
import macrotrack.composeapp.generated.resources.tomorrow
import macrotrack.composeapp.generated.resources.yesterday
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/**
 * Names the past day a log screen adds to, so food doesn't land on the wrong day by mistake. A later day is
 * being planned, so it gets the meal plan's colors. Shows nothing for today.
 */
@Composable
fun CommonLogDateBadge(
    modifier: Modifier = Modifier,
    date: LocalDate
) {
    val today = getCurrentDate()
    if (date == today) return
    val isPlanning = date > today
    val tint = if (isPlanning) colors.nightInk else colors.warmInk
    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 8.dp))
            .background(color = if (isPlanning) colors.nightSoft else colors.warmSoft)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_calendar),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size = 13.dp)
        )
        Text(text = logDateLabel(date = date), style = typography.bold12, color = tint)
    }
}

/** "Yesterday", "Tomorrow", or the short day and date, like "Sun, Oct 4". */
@Composable
fun logDateLabel(date: LocalDate): String {
    if (date == getCurrentDate().minus(period = DatePeriod(days = 1))) return stringResource(resource = Res.string.yesterday)
    if (date == getCurrentDate().plus(period = DatePeriod(days = 1))) return stringResource(resource = Res.string.tomorrow)
    val day = date.dayOfWeek.name.take(n = 3).lowercase().replaceFirstChar { it.uppercase() }
    return "$day, ${date.toAbbreviatedMonthDay()}"
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewCommonLogDateBadge() {
    MacroTrackTheme {
        CommonLogDateBadge(
            modifier = Modifier.padding(all = 16.dp),
            date = getCurrentDate().minus(period = DatePeriod(days = 3))
        )
    }
}
