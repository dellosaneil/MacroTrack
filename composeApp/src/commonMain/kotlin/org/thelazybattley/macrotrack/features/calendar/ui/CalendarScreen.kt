package org.thelazybattley.macrotrack.features.calendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_meals
import macrotrack.composeapp.generated.resources.avg_intake
import macrotrack.composeapp.generated.resources.calendar
import macrotrack.composeapp.generated.resources.day_streak
import macrotrack.composeapp.generated.resources.days_logged
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_flame
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.in_progress
import macrotrack.composeapp.generated.resources.kcal_unit
import macrotrack.composeapp.generated.resources.log_a_meal_today
import macrotrack.composeapp.generated.resources.meal_count
import macrotrack.composeapp.generated.resources.next_month
import macrotrack.composeapp.generated.resources.not_logged
import macrotrack.composeapp.generated.resources.nothing_logged
import macrotrack.composeapp.generated.resources.of_value
import macrotrack.composeapp.generated.resources.off_target
import macrotrack.composeapp.generated.resources.on_target
import macrotrack.composeapp.generated.resources.open_in_food_log
import macrotrack.composeapp.generated.resources.over_by_kcal
import macrotrack.composeapp.generated.resources.previous_month
import macrotrack.composeapp.generated.resources.slash_value_kcal
import macrotrack.composeapp.generated.resources.streak_at_risk
import macrotrack.composeapp.generated.resources.streak_ended
import macrotrack.composeapp.generated.resources.streak_logged_today_best
import macrotrack.composeapp.generated.resources.streak_logged_today_record
import macrotrack.composeapp.generated.resources.streak_none
import macrotrack.composeapp.generated.resources.under_by_kcal
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.features.calendar.CalendarCallbacks
import org.thelazybattley.macrotrack.features.calendar.CalendarViewModel
import org.thelazybattley.macrotrack.features.calendar.CalendarViewState
import org.thelazybattley.macrotrack.features.calendar.DayStatus
import org.thelazybattley.macrotrack.features.calendar.DaySummary
import org.thelazybattley.macrotrack.features.calendar.StreakState
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.common.dashedBorder
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit
) {
    val viewModel = koinViewModel<CalendarViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    CalendarScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = onPopBackStack,
        onOpenInFoodLog = { date ->
            viewModel.onOpenInFoodLog(date = date)
            onPopBackStack()
        }
    )
}

@Composable
private fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewState: CalendarViewState,
    callbacks: CalendarCallbacks,
    onBack: () -> Unit,
    onOpenInFoodLog: (LocalDate) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CommonScreenHeader(title = Res.string.calendar, onBack = onBack)
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
            StreakCard(
                viewState = viewState,
                onLogToday = { onOpenInFoodLog(viewState.today) }
            )
            MonthCard(viewState = viewState, callbacks = callbacks)
            MonthStatsRow(viewState = viewState)
            SelectedDayCard(viewState = viewState, onOpenInFoodLog = onOpenInFoodLog)
        }
    }
}

@Composable
private fun StreakCard(
    viewState: CalendarViewState,
    onLogToday: () -> Unit
) {
    val state = viewState.streakState
    val shape = RoundedCornerShape(size = 24.dp)
    val cardModifier = when (state) {
        StreakState.LOGGED_TODAY -> Modifier.background(color = colors.hero, shape = shape)
        StreakState.AT_RISK -> Modifier.background(color = colors.warmSoft, shape = shape)
        StreakState.ENDED -> Modifier
            .background(color = colors.surface, shape = shape)
            .border(width = 1.dp, color = colors.line, shape = shape)
    }
    val (badgeBackground, flameTint) = when (state) {
        StreakState.LOGGED_TODAY -> colors.surface to colors.fat
        StreakState.AT_RISK -> colors.surface to colors.carbs
        StreakState.ENDED -> colors.divider to colors.faint
    }
    val streak = viewState.currentStreak
    val best = viewState.bestStreak
    val subtitle = when (state) {
        StreakState.LOGGED_TODAY -> if (best == null || best == streak) {
            stringResource(resource = Res.string.streak_logged_today_record)
        } else {
            stringResource(
                resource = Res.string.streak_logged_today_best,
                best.length,
                best.start.toAbbreviatedMonthDay(),
                best.end.toAbbreviatedMonthDay()
            )
        }
        StreakState.AT_RISK -> stringResource(resource = Res.string.streak_at_risk, best?.length ?: 0)
        StreakState.ENDED -> viewState.lastBrokenStreak?.let { run ->
            stringResource(resource = Res.string.streak_ended, run.length, run.end.toAbbreviatedMonthDay())
        } ?: stringResource(resource = Res.string.streak_none)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(other = cardModifier)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(size = 52.dp)
                    .clip(shape = RoundedCornerShape(size = 16.dp))
                    .background(color = badgeBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_flame),
                    contentDescription = null,
                    tint = flameTint,
                    modifier = Modifier.size(size = 26.dp)
                )
            }
            Column(modifier = Modifier.weight(weight = 1f)) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                ) {
                    Text(
                        text = (streak?.length ?: 0).toString(),
                        style = typography.bold32,
                        color = colors.ink,
                        modifier = Modifier.alignByBaseline()
                    )
                    Text(
                        text = stringResource(resource = Res.string.day_streak),
                        style = typography.bold14,
                        color = colors.ink,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                Text(text = subtitle, style = typography.regular11, color = colors.muted)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            viewState.thisWeek.forEach { day ->
                Column(
                    modifier = Modifier.weight(weight = 1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(space = 5.dp)
                ) {
                    Text(text = day.date.dayLetter(), style = typography.medium11, color = colors.muted)
                    val dot = Modifier.size(size = 32.dp)
                    when {
                        day.isLogged -> Box(
                            modifier = dot.background(color = colors.accent, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(resource = Res.drawable.ic_check),
                                contentDescription = null,
                                tint = colors.onAccent,
                                modifier = Modifier.size(size = 14.dp)
                            )
                        }
                        day.isToday -> Box(
                            modifier = dot.dashedBorder(
                                color = if (state == StreakState.AT_RISK) colors.carbs else colors.accent,
                                width = 2.dp,
                                cornerRadius = 16.dp
                            )
                        )
                        day.isFuture -> Box(modifier = dot.border(width = 1.dp, color = colors.divider, shape = CircleShape))
                        else -> Box(modifier = dot.background(color = colors.divider, shape = CircleShape))
                    }
                }
            }
        }
        if (state == StreakState.AT_RISK) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 44.dp)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.accent)
                    .clickable(role = Role.Button, onClick = onLogToday),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_plus),
                    contentDescription = null,
                    tint = colors.onAccent,
                    modifier = Modifier.size(size = 16.dp)
                )
                Text(
                    text = stringResource(resource = Res.string.log_a_meal_today),
                    style = typography.bold14,
                    color = colors.onAccent
                )
            }
        }
    }
}

@Composable
private fun MonthCard(
    viewState: CalendarViewState,
    callbacks: CalendarCallbacks
) {
    val shape = RoundedCornerShape(size = 24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            MonthNavButton(
                icon = Res.drawable.ic_chevron_left,
                description = Res.string.previous_month,
                isEnabled = viewState.canGoBack,
                onClick = callbacks::onPreviousMonth
            )
            Text(
                text = viewState.visibleMonth.monthYear(),
                style = typography.bold16,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f),
                textAlign = TextAlign.Center
            )
            MonthNavButton(
                icon = Res.drawable.ic_chevron_right,
                description = Res.string.next_month,
                isEnabled = viewState.canGoForward,
                onClick = callbacks::onNextMonth
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                viewState.thisWeek.forEach { day ->
                    Text(
                        text = day.date.dayLetter(),
                        style = typography.bold11,
                        color = colors.faint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(weight = 1f)
                    )
                }
            }
            viewState.monthCells.chunked(size = 7).forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                    week.forEach { date ->
                        Box(modifier = Modifier.weight(weight = 1f).height(height = 44.dp)) {
                            if (date != null) {
                                DayCell(
                                    date = date,
                                    status = viewState.statusOf(date = date),
                                    isSelected = date == viewState.selectedDate,
                                    onClick = { callbacks.onSelectDate(date = date) }
                                )
                            }
                        }
                    }
                    repeat(times = 7 - week.size) { Spacer(modifier = Modifier.weight(weight = 1f)) }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
            LegendItem(
                text = Res.string.on_target,
                swatch = Modifier
                    .background(color = colors.accentSoft, shape = RoundedCornerShape(size = 3.dp))
                    .border(width = 1.dp, color = colors.accent, shape = RoundedCornerShape(size = 3.dp))
            )
            LegendItem(
                text = Res.string.off_target,
                swatch = Modifier
                    .background(color = colors.warmSoft, shape = RoundedCornerShape(size = 3.dp))
                    .border(width = 1.dp, color = colors.carbs, shape = RoundedCornerShape(size = 3.dp))
            )
            LegendItem(
                text = Res.string.not_logged,
                swatch = Modifier.dashedBorder(color = colors.dashed, width = 1.dp, cornerRadius = 3.dp)
            )
        }
    }
}

@Composable
private fun MonthNavButton(
    icon: DrawableResource,
    description: StringResource,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 12.dp)
    Box(
        modifier = Modifier
            .size(size = 40.dp)
            .clip(shape = shape)
            .background(color = if (isEnabled) colors.surface else Color.Transparent)
            .border(width = 1.dp, color = if (isEnabled) colors.line else colors.divider, shape = shape)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = icon),
            contentDescription = stringResource(resource = description),
            tint = if (isEnabled) colors.ink else colors.faint,
            modifier = Modifier.size(size = 18.dp)
        )
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    status: DayStatus,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 12.dp)
    val (background, textColor) = when (status) {
        DayStatus.ON_TARGET -> colors.accentSoft to colors.accentInk
        DayStatus.OFF_TARGET -> colors.warmSoft to colors.warmInk
        DayStatus.LOGGED -> colors.divider to colors.ink
        DayStatus.TODAY -> colors.surface to colors.accentInk
        DayStatus.TODAY_EMPTY -> Color.Transparent to colors.ink
        DayStatus.NOT_LOGGED, DayStatus.FUTURE -> Color.Transparent to colors.faint
    }
    val outline = when (status) {
        DayStatus.TODAY -> Modifier.border(width = 2.dp, color = colors.accent, shape = shape)
        DayStatus.TODAY_EMPTY -> Modifier.dashedBorder(color = colors.accent, width = 2.dp, cornerRadius = 12.dp)
        DayStatus.NOT_LOGGED -> Modifier.dashedBorder(color = colors.dashed, width = 1.dp, cornerRadius = 12.dp)
        else -> Modifier
    }
    val selection = if (isSelected) {
        Modifier.border(width = 2.dp, color = colors.ink, shape = shape)
    } else {
        Modifier
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha = if (status == DayStatus.FUTURE) 0.45f else 1f)
            .clip(shape = shape)
            .background(color = background)
            .then(other = outline)
            .then(other = selection)
            .clickable(enabled = status != DayStatus.FUTURE, role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 3.dp, alignment = Alignment.CenterVertically)
    ) {
        Text(text = date.day.toString(), style = typography.bold14, color = textColor)
        if (status == DayStatus.TODAY) {
            Box(
                modifier = Modifier
                    .width(width = 14.dp)
                    .height(height = 3.dp)
                    .background(color = colors.accent, shape = RoundedCornerShape(size = 2.dp))
            )
        }
    }
}

@Composable
private fun LegendItem(text: StringResource, swatch: Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
    ) {
        Box(modifier = Modifier.size(size = 10.dp).then(other = swatch))
        Text(text = stringResource(resource = text), style = typography.regular11, color = colors.muted)
    }
}

@Composable
private fun MonthStatsRow(viewState: CalendarViewState) {
    val stats = viewState.monthStats
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        StatTile(
            modifier = Modifier.weight(weight = 1f),
            label = Res.string.days_logged,
            value = stats.daysLogged.toString(),
            suffix = stringResource(resource = Res.string.of_value, stats.daysSoFar)
        )
        StatTile(
            modifier = Modifier.weight(weight = 1f),
            label = Res.string.on_target,
            value = stats.daysOnTarget.toString(),
            suffix = stringResource(resource = Res.string.of_value, stats.daysWithGoal)
        )
        StatTile(
            modifier = Modifier.weight(weight = 1f),
            label = Res.string.avg_intake,
            value = stats.averageCalories?.withThousands() ?: "–",
            suffix = stringResource(resource = Res.string.kcal_unit)
        )
    }
}

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    label: StringResource,
    value: String,
    suffix: String
) {
    val shape = RoundedCornerShape(size = 16.dp)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(all = 10.dp),
        verticalArrangement = Arrangement.spacedBy(space = 2.dp)
    ) {
        Text(text = stringResource(resource = label), style = typography.regular11, color = colors.muted)
        Text(
            text = buildAnnotatedString {
                append(value)
                withStyle(style = SpanStyle(fontSize = typography.regular11.fontSize, color = colors.muted, fontWeight = typography.regular11.fontWeight)) {
                    append(" $suffix")
                }
            },
            style = typography.bold16,
            color = colors.ink
        )
    }
}

@Composable
private fun SelectedDayCard(
    viewState: CalendarViewState,
    onOpenInFoodLog: (LocalDate) -> Unit
) {
    val date = viewState.selectedDate
    val day = viewState.selectedDay
    val dateText = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() } + ", " + date.toAbbreviatedMonthDay()
    val shape = RoundedCornerShape(size = 20.dp)
    if (day == null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .dashedBorder(color = colors.dashed, width = 1.5.dp, cornerRadius = 20.dp)
                .padding(all = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(text = dateText, style = typography.bold14, color = colors.ink)
                Text(text = stringResource(resource = Res.string.nothing_logged), style = typography.regular11, color = colors.muted)
            }
            if (viewState.canOpenSelected) {
                Text(
                    text = stringResource(resource = Res.string.add_meals),
                    style = typography.bold13,
                    color = colors.accentInk,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(size = 12.dp))
                        .background(color = colors.accentSoft)
                        .clickable(role = Role.Button) { onOpenInFoodLog(date) }
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                )
            }
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = dateText, style = typography.bold14, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            StatusPill(status = viewState.statusOf(date = date), day = day)
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            Text(
                text = day.calories.withThousands(),
                style = typography.bold24,
                color = colors.ink,
                modifier = Modifier.alignByBaseline()
            )
            day.goalCalories?.let { goal ->
                Text(
                    text = stringResource(resource = Res.string.slash_value_kcal, goal.withThousands()),
                    style = typography.regular13,
                    color = colors.muted,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
        day.goalCalories?.takeIf { it > 0 }?.let { goal ->
            val isOff = viewState.statusOf(date = date) == DayStatus.OFF_TARGET
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 6.dp)
                    .clip(shape = RoundedCornerShape(size = 3.dp))
                    .background(color = colors.divider)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (day.calories.toFloat() / goal).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(shape = RoundedCornerShape(size = 3.dp))
                        .background(color = if (isOff) colors.carbs else colors.accent)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 14.dp)) {
            MacroAmount(letter = "P", grams = day.protein, color = colors.protein)
            MacroAmount(letter = "C", grams = day.carbs, color = colors.carbs)
            MacroAmount(letter = "F", grams = day.fat, color = colors.fat)
            Spacer(modifier = Modifier.weight(weight = 1f))
            Text(
                text = pluralStringResource(resource = Res.plurals.meal_count, quantity = day.mealCount, day.mealCount),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 44.dp)
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
                .clickable(role = Role.Button) { onOpenInFoodLog(date) }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(resource = Res.string.open_in_food_log),
                style = typography.bold14,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            Icon(
                painter = painterResource(resource = Res.drawable.ic_chevron_right),
                contentDescription = null,
                tint = colors.faint,
                modifier = Modifier.size(size = 16.dp)
            )
        }
    }
}

@Composable
private fun StatusPill(status: DayStatus, day: DaySummary) {
    val goal = day.goalCalories
    val (background, textColor, text) = when (status) {
        DayStatus.ON_TARGET -> Triple(colors.accentSoft, colors.accentInk, stringResource(resource = Res.string.on_target))
        DayStatus.OFF_TARGET -> {
            val difference = abs(day.calories - (goal ?: 0)).withThousands()
            val resource = if (goal != null && day.calories > goal) Res.string.over_by_kcal else Res.string.under_by_kcal
            Triple(colors.warmSoft, colors.warmInk, stringResource(resource = resource, difference))
        }
        DayStatus.TODAY -> Triple(colors.divider, colors.muted, stringResource(resource = Res.string.in_progress))
        else -> return
    }
    Text(
        text = text,
        style = typography.bold12,
        color = textColor,
        modifier = Modifier
            .clip(shape = RoundedCornerShape(size = 10.dp))
            .background(color = background)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun MacroAmount(letter: String, grams: Double, color: Color) {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(color = color, fontWeight = typography.bold11.fontWeight)) { append(letter) }
            append(" ${grams.roundToInt()}g")
        },
        style = typography.regular11,
        color = colors.muted
    )
}

private fun LocalDate.dayLetter(): String = dayOfWeek.name.take(n = 1)

private fun LocalDate.monthYear(): String = month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + year

@Preview(showBackground = true)
@Composable
private fun PreviewCalendarScreen() {
    val today = getCurrentDate()
    val days = (0..20).filter { it != 4 && it != 5 }.associate { offset ->
        val date = today.minus(period = DatePeriod(days = offset))
        date to DaySummary(
            date = date,
            calories = if (offset % 6 == 2) 2860 else 2150,
            protein = 160.0,
            carbs = 230.0,
            fat = 70.0,
            mealCount = 4,
            goalCalories = 2150
        )
    }
    MacroTrackTheme {
        CalendarScreen(
            viewState = CalendarViewState(today = today, days = days),
            callbacks = CalendarCallbacks.default(),
            onBack = {},
            onOpenInFoodLog = {}
        )
    }
}
