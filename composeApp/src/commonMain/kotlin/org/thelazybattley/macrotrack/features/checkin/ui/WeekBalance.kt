package org.thelazybattley.macrotrack.features.checkin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.check_in_review
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.week_balance
import macrotrack.composeapp.generated.resources.week_check_in
import macrotrack.composeapp.generated.resources.week_chip_on
import macrotrack.composeapp.generated.resources.week_chip_over
import macrotrack.composeapp.generated.resources.week_chip_under
import macrotrack.composeapp.generated.resources.week_day_counted
import macrotrack.composeapp.generated.resources.week_day_not_logged
import macrotrack.composeapp.generated.resources.week_day_today_left
import macrotrack.composeapp.generated.resources.week_day_today_over
import macrotrack.composeapp.generated.resources.week_day_upcoming
import macrotrack.composeapp.generated.resources.week_days_counted
import macrotrack.composeapp.generated.resources.week_even_over
import macrotrack.composeapp.generated.resources.week_even_under
import macrotrack.composeapp.generated.resources.week_kcal
import macrotrack.composeapp.generated.resources.week_last
import macrotrack.composeapp.generated.resources.week_legend_over
import macrotrack.composeapp.generated.resources.week_legend_under
import macrotrack.composeapp.generated.resources.week_new
import macrotrack.composeapp.generated.resources.week_next
import macrotrack.composeapp.generated.resources.week_not_logged_left_out
import macrotrack.composeapp.generated.resources.week_on_targets
import macrotrack.composeapp.generated.resources.week_over_targets
import macrotrack.composeapp.generated.resources.week_previous
import macrotrack.composeapp.generated.resources.week_range
import macrotrack.composeapp.generated.resources.week_short_even
import macrotrack.composeapp.generated.resources.week_short_over
import macrotrack.composeapp.generated.resources.week_short_under
import macrotrack.composeapp.generated.resources.week_starts_fresh
import macrotrack.composeapp.generated.resources.week_tap_day
import macrotrack.composeapp.generated.resources.week_this
import macrotrack.composeapp.generated.resources.week_tile_description
import macrotrack.composeapp.generated.resources.week_tile_description_new
import macrotrack.composeapp.generated.resources.week_today
import macrotrack.composeapp.generated.resources.week_today_counts
import macrotrack.composeapp.generated.resources.week_under_targets
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.WeekBalance
import org.thelazybattley.macrotrack.domain.model.WeekBalanceDay
import org.thelazybattley.macrotrack.domain.model.WeekDayStatus
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.dashedBorder
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.min

/** On the check-in, a day this far off target fills its bar; on the Food Log tile, this far. */
private const val CARD_BAR_SCALE_KCAL = 500
private const val TILE_BAR_SCALE_KCAL = 250

/** The Mon–Sun week balance at the top of the check-in, with ‹ › between this week and last. */
@Composable
fun WeekBalanceCard(
    modifier: Modifier = Modifier,
    balance: WeekBalance,
    selectedDay: Int?,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onDayPicked: (Int) -> Unit
) {
    val shape = RoundedCornerShape(size = 22.dp)
    val total = balance.total
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            WeekArrow(
                isBack = true,
                isEnabled = canGoBack,
                description = stringResource(resource = Res.string.week_previous),
                onClick = onPreviousWeek
            )
            Column(modifier = Modifier.weight(weight = 1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = balance.title(), style = typography.bold14, color = colors.ink)
                Text(
                    text = stringResource(resource = Res.string.week_range, balance.weekStart.shortDate(), balance.weekEnd.shortDate()),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
            WeekArrow(
                isBack = false,
                isEnabled = canGoForward,
                description = stringResource(resource = Res.string.week_next),
                onClick = onNextWeek
            )
        }
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Text(text = stringResource(resource = Res.string.week_balance), style = typography.regular11, color = colors.muted)
                BalanceChip(total = total)
            }
            Text(text = totalText(total = total), style = typography.bold32, color = totalColor(total = total))
            Text(text = totalWord(total = total), style = typography.regular13, color = colors.muted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            balance.days.forEachIndexed { index, day ->
                val description = dayReadout(balance = balance, index = index)
                Column(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .clip(shape = RoundedCornerShape(size = 8.dp))
                        .clickable(role = Role.Button) { onDayPicked(index) }
                        .clearAndSetSemantics {
                            contentDescription = description
                            selected = selectedDay == index
                        },
                    verticalArrangement = Arrangement.spacedBy(space = 4.dp)
                ) {
                    DayBars(day = day, isSelected = selectedDay == index, height = 44.dp, scale = CARD_BAR_SCALE_KCAL, todayMark = 18.dp)
                    Text(
                        text = when (day.status) {
                            WeekDayStatus.COUNTED -> signed(value = day.difference)
                            WeekDayStatus.TODAY -> stringResource(resource = Res.string.week_today)
                            WeekDayStatus.UPCOMING -> ""
                            WeekDayStatus.NOT_LOGGED -> "–"
                        },
                        style = typography.bold10,
                        color = if (day.status == WeekDayStatus.COUNTED) colors.ink else colors.muted,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = day.date.weekdayShort(),
                        style = typography.regular11,
                        color = colors.muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        Text(
            text = selectedDay?.let { dayReadout(balance = balance, index = it) } ?: stringResource(resource = Res.string.week_tap_day),
            style = typography.regular11,
            color = colors.ink,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .background(color = colors.background)
                .semantics { liveRegion = LiveRegionMode.Polite }
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
            LegendItem(color = colors.over, text = stringResource(resource = Res.string.week_legend_over))
            LegendItem(color = colors.under, text = stringResource(resource = Res.string.week_legend_under))
        }
        Text(text = countedNote(balance = balance), style = typography.regular11, color = colors.muted)
        evenTip(balance = balance)?.let { Text(text = it, style = typography.bold12, color = colors.ink) }
    }
}

/** Food Log: the week so far beside Steps, with mini bars, and "New" when a suggestion is waiting. */
@Composable
fun WeekBalanceTile(
    modifier: Modifier = Modifier,
    balance: WeekBalance,
    isNewSuggestion: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 18.dp)
    val description = tileDescription(balance = balance, isNewSuggestion = isNewSuggestion)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = description }
            .padding(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            Text(
                text = stringResource(resource = Res.string.week_this),
                style = typography.bold12,
                color = colors.muted,
                modifier = Modifier.weight(weight = 1f)
            )
            if (isNewSuggestion) NewDot()
            Icon(painter = painterResource(resource = Res.drawable.ic_chevron_right), contentDescription = null, tint = colors.muted, modifier = Modifier.size(size = 14.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(
                text = totalText(total = balance.total),
                style = typography.bold18,
                color = totalColor(total = balance.total),
                maxLines = 1,
                modifier = Modifier.alignByBaseline()
            )
            Text(text = shortWord(total = balance.total), style = typography.regular11, color = colors.muted, modifier = Modifier.alignByBaseline())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 3.dp)) {
            balance.days.forEach { day ->
                Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 1.dp)) {
                    DayBars(day = day, isSelected = false, height = 12.dp, scale = TILE_BAR_SCALE_KCAL, todayMark = 10.dp)
                    Text(
                        text = day.date.dayOfWeek.name.take(n = 1),
                        style = typography.regular8,
                        color = colors.muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/** While a walk or run records, the week shrinks to one line under the live card. */
@Composable
fun WeekBalanceLine(
    modifier: Modifier = Modifier,
    balance: WeekBalance,
    isNewSuggestion: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 16.dp)
    val description = tileDescription(balance = balance, isNewSuggestion = isNewSuggestion)
    Row(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = description }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(text = stringResource(resource = Res.string.week_this), style = typography.bold12, color = colors.muted)
        Row(modifier = Modifier.weight(weight = 1f), horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(
                text = totalText(total = balance.total),
                style = typography.bold14,
                color = totalColor(total = balance.total),
                modifier = Modifier.alignByBaseline()
            )
            Text(text = shortWord(total = balance.total), style = typography.regular11, color = colors.muted, modifier = Modifier.alignByBaseline())
        }
        Text(
            text = stringResource(resource = if (isNewSuggestion) Res.string.check_in_review else Res.string.week_check_in),
            style = typography.bold12,
            color = colors.accentInk
        )
        Icon(painter = painterResource(resource = Res.drawable.ic_chevron_right), contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(size = 14.dp))
    }
}

/** One day: ate more rises above the line, ate less hangs below it, and today is a dashed box until it ends. */
@Composable
private fun DayBars(day: WeekBalanceDay, isSelected: Boolean, height: Dp, scale: Int, todayMark: Dp) {
    val difference = day.difference
    val barHeight = height * (min(abs(difference), scale).toFloat() / scale)
    val bar = barHeight.coerceAtLeast(minimumValue = 3.dp)
    Column {
        Box(modifier = Modifier.fillMaxWidth().height(height = height), contentAlignment = Alignment.BottomCenter) {
            when {
                day.status == WeekDayStatus.TODAY -> Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.7f)
                        .height(height = todayMark)
                        .dashedBorder(color = colors.dashed, width = 1.5.dp, cornerRadius = 4.dp)
                )
                day.status == WeekDayStatus.COUNTED && difference > 0 -> Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.7f)
                        .height(height = bar)
                        .background(
                            color = if (isSelected) colors.ink else colors.over,
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
            }
        }
        HorizontalDivider(thickness = 1.5.dp, color = colors.dashed)
        Box(modifier = Modifier.fillMaxWidth().height(height = height), contentAlignment = Alignment.TopCenter) {
            if (day.status == WeekDayStatus.COUNTED && difference < 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.7f)
                        .height(height = bar)
                        .background(
                            color = if (isSelected) colors.ink else colors.under,
                            shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun WeekArrow(isBack: Boolean, isEnabled: Boolean, description: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 10.dp)
    Box(
        modifier = Modifier
            .size(size = 36.dp)
            .alpha(alpha = if (isEnabled) 1f else 0.35f)
            .clip(shape = shape)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .background(color = colors.surface)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = if (isBack) Res.drawable.ic_chevron_left else Res.drawable.ic_chevron_right),
            contentDescription = description,
            tint = colors.ink,
            modifier = Modifier.size(size = 16.dp)
        )
    }
}

@Composable
private fun BalanceChip(total: Int) {
    Text(
        text = stringResource(
            resource = when {
                total > 0 -> Res.string.week_chip_over
                total < 0 -> Res.string.week_chip_under
                else -> Res.string.week_chip_on
            }
        ),
        style = typography.bold11,
        color = if (total > 0) colors.warmInk else colors.accentInk,
        modifier = Modifier
            .background(color = if (total > 0) colors.warmSoft else colors.accentSoft, shape = RoundedCornerShape(size = 8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
        Box(modifier = Modifier.size(size = 10.dp).background(color = color, shape = RoundedCornerShape(size = 3.dp)))
        Text(text = text, style = typography.regular11, color = colors.muted)
    }
}

@Composable
private fun NewDot() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
        Text(text = stringResource(resource = Res.string.week_new), style = typography.bold11, color = colors.accentInk)
        Box(modifier = Modifier.size(size = 6.dp).background(color = colors.accent, shape = RoundedCornerShape(size = 3.dp)))
    }
}

@Composable
private fun WeekBalance.title(): String =
    stringResource(resource = if (isThisWeek) Res.string.week_this else Res.string.week_last)

/** "+100", "−1,240" or "0". */
private fun signed(value: Int): String = when {
    value > 0 -> "+" + value.withThousands()
    value < 0 -> "−" + abs(value).withThousands()
    else -> "0"
}

@Composable
private fun totalText(total: Int): String = stringResource(resource = Res.string.week_kcal, signed(value = total))

@Composable
private fun totalColor(total: Int): Color = when {
    total > 0 -> colors.overInk
    total < 0 -> colors.underInk
    else -> colors.ink
}

@Composable
private fun totalWord(total: Int): String = stringResource(
    resource = when {
        total > 0 -> Res.string.week_over_targets
        total < 0 -> Res.string.week_under_targets
        else -> Res.string.week_on_targets
    }
)

@Composable
private fun shortWord(total: Int): String = stringResource(
    resource = when {
        total > 0 -> Res.string.week_short_over
        total < 0 -> Res.string.week_short_under
        else -> Res.string.week_short_even
    }
)

@Composable
private fun tileDescription(balance: WeekBalance, isNewSuggestion: Boolean): String = stringResource(
    resource = if (isNewSuggestion) Res.string.week_tile_description_new else Res.string.week_tile_description,
    balance.title(),
    totalText(total = balance.total),
    totalWord(total = balance.total)
)

/** What tapping a day shows, and what a screen reader reads for it. */
@Composable
private fun dayReadout(balance: WeekBalance, index: Int): String {
    val day = balance.days[index]
    val date = day.date.shortDate()
    return when (day.status) {
        WeekDayStatus.COUNTED -> stringResource(
            resource = Res.string.week_day_counted,
            date,
            (day.eaten ?: 0).withThousands(),
            day.target.withThousands(),
            signed(value = day.difference),
            signed(value = balance.runningTotal(index = index))
        )
        WeekDayStatus.TODAY -> {
            val left = day.target - (day.eaten ?: 0)
            stringResource(
                resource = if (left >= 0) Res.string.week_day_today_left else Res.string.week_day_today_over,
                date,
                abs(left).withThousands()
            )
        }
        WeekDayStatus.UPCOMING -> stringResource(resource = Res.string.week_day_upcoming, date)
        WeekDayStatus.NOT_LOGGED -> stringResource(resource = Res.string.week_day_not_logged, date)
    }
}

/** "3 days counted · today counts when it ends · days not logged are left out. Starts fresh every Monday." */
@Composable
private fun countedNote(balance: WeekBalance): String {
    val counted = pluralStringResource(resource = Res.plurals.week_days_counted, quantity = balance.countedDays, balance.countedDays) +
        (if (balance.isThisWeek) stringResource(resource = Res.string.week_today_counts) else "") +
        (if (balance.hasNotLoggedDays) stringResource(resource = Res.string.week_not_logged_left_out) else "")
    return stringResource(resource = Res.string.week_starts_fresh, counted)
}

/** This week only: how far each day left can go the other way to finish even. */
@Composable
private fun evenTip(balance: WeekBalance): String? {
    if (!balance.isThisWeek) return null
    val perDay = balance.evenPerDay ?: return null
    return pluralStringResource(
        resource = if (balance.total > 0) Res.plurals.week_even_under else Res.plurals.week_even_over,
        quantity = balance.daysLeft,
        perDay.withThousands(),
        balance.daysLeft
    )
}

/** "Mon". */
internal fun kotlinx.datetime.LocalDate.weekdayShort(): String =
    dayOfWeek.name.take(n = 3).lowercase().replaceFirstChar { it.uppercase() }
