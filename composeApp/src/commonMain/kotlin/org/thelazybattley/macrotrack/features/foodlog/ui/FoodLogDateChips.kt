package org.thelazybattley.macrotrack.features.foodlog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

private const val MIN_DAYS_SHOWN = 7

@Composable
fun FoodLogDateChips(
    modifier: Modifier = Modifier,
    dates: List<LocalDate>,
    dateSelected: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    val today = getCurrentDate()
    val loggedDates = remember(key1 = dates) { dates.toSet() }
    val allDates = remember(key1 = dates, key2 = today) {
        val minimumStart = today.minus(period = DatePeriod(days = MIN_DAYS_SHOWN - 1))
        val start = minOf(dates.minOrNull() ?: minimumStart, minimumStart)
        generateSequence(seed = start) { it.plus(period = DatePeriod(days = 1)) }
            .takeWhile { it <= today }
            .toList()
    }
    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = (allDates.indexOf(dateSelected) - MIN_DAYS_SHOWN + 1)
            .coerceAtLeast(minimumValue = 0)
    )
    LaunchedEffect(key1 = allDates, key2 = dateSelected) {
        val index = allDates.indexOf(dateSelected)
        if (index < 0) return@LaunchedEffect
        val isFullyVisible = state.layoutInfo.visibleItemsInfo.any { item ->
            item.index == index &&
                item.offset >= state.layoutInfo.viewportStartOffset &&
                item.offset + item.size <= state.layoutInfo.viewportEndOffset
        }
        if (!isFullyVisible) {
            state.scrollToItem(index = (index - MIN_DAYS_SHOWN + 1).coerceAtLeast(minimumValue = 0))
        }
    }
    val visibleMonth by remember(key1 = allDates) {
        derivedStateOf {
            allDates.getOrNull(index = state.firstVisibleItemIndex)?.toMonthYear().orEmpty()
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = visibleMonth,
            style = typography.bold16,
            color = colors.ink
        )
        LazyRow(
            state = state,
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            items(items = allDates, key = { it.toEpochDays() }) { date ->
                DateChip(
                    date = date,
                    isSelected = date == dateSelected,
                    isEnabled = date in loggedDates || date == today,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

@Composable
private fun DateChip(
    modifier: Modifier = Modifier,
    date: LocalDate,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    val background = when {
        isSelected -> colors.accent
        isEnabled -> colors.surface
        else -> colors.divider
    }
    val borderColor = when {
        isSelected -> colors.accent
        isEnabled -> colors.line
        else -> colors.divider
    }
    val textColor = when {
        isSelected -> colors.onAccent
        isEnabled -> colors.ink
        else -> colors.faint
    }

    Column(
        modifier = modifier
            .width(width = 46.dp)
            .height(height = 56.dp)
            .clip(shape = shape)
            .background(color = background)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(enabled = isEnabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 2.dp, alignment = Alignment.CenterVertically)
    ) {
        Text(
            text = date.dayOfWeek.name.take(n = 1),
            style = typography.medium11,
            color = textColor.copy(alpha = 0.75f)
        )
        Text(
            text = date.day.toString(),
            style = typography.bold16,
            color = textColor
        )
    }
}

private fun LocalDate.toMonthYear(): String =
    month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + year

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun Preview() {
    MacroTrackTheme {
        FoodLogDateChips(
            modifier = Modifier.padding(all = 16.dp),
            dateSelected = getCurrentDate(),
            dates = listOf(
                getCurrentDate(),
                getCurrentDate().minus(period = DatePeriod(days = 1)),
                getCurrentDate().minus(period = DatePeriod(days = 3)),
            )
        ) {
        }
    }
}
