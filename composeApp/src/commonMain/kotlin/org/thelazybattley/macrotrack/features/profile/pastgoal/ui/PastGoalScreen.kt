package org.thelazybattley.macrotrack.features.profile.pastgoal.ui

import androidx.compose.foundation.background
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.common.weightInputText
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.apply_to_range
import macrotrack.composeapp.generated.resources.change_past_goal
import macrotrack.composeapp.generated.resources.done
import macrotrack.composeapp.generated.resources.ends_on
import macrotrack.composeapp.generated.resources.from
import macrotrack.composeapp.generated.resources.from_value
import macrotrack.composeapp.generated.resources.history_after_saving_step
import macrotrack.composeapp.generated.resources.ic_calendar
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.next_month
import macrotrack.composeapp.generated.resources.no_past_days_hint
import macrotrack.composeapp.generated.resources.no_past_days_title
import macrotrack.composeapp.generated.resources.past_goal_impact
import macrotrack.composeapp.generated.resources.pick_exact_day_hint
import macrotrack.composeapp.generated.resources.pick_goal_step
import macrotrack.composeapp.generated.resources.pick_months_step
import macrotrack.composeapp.generated.resources.previous_month
import macrotrack.composeapp.generated.resources.starts_on
import macrotrack.composeapp.generated.resources.targets_from_weight
import macrotrack.composeapp.generated.resources.until
import macrotrack.composeapp.generated.resources.until_value
import macrotrack.composeapp.generated.resources.value_kcal
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.GoalPeriod
import org.thelazybattley.macrotrack.features.calendar.firstOfMonth
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.profile.pastgoal.DateSide
import org.thelazybattley.macrotrack.features.profile.pastgoal.PastGoalCallbacks
import org.thelazybattley.macrotrack.features.profile.pastgoal.PastGoalViewModel
import org.thelazybattley.macrotrack.features.profile.pastgoal.PastGoalViewState
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun PastGoalScreen(
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit
) {
    val viewModel = koinViewModel<PastGoalViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = viewState.isSaved) {
        if (viewState.isSaved) onPopBackStack()
    }

    PastGoalScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = onPopBackStack
    )
}

@Composable
private fun PastGoalScreen(
    modifier: Modifier = Modifier,
    viewState: PastGoalViewState,
    callbacks: PastGoalCallbacks,
    onBack: () -> Unit
) {
    val start = viewState.start
    val end = viewState.end
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CommonScreenHeader(title = Res.string.change_past_goal, onBack = onBack)
        },
        bottomBar = {
            if (start != null && end != null) {
                CommonPrimaryButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    text = stringResource(
                        resource = Res.string.apply_to_range,
                        start.toShortDate(today = viewState.today),
                        end.toShortDate(today = viewState.today)
                    ),
                    enabled = viewState.change != null,
                    onClick = callbacks::onApply
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(space = 18.dp)
        ) {
            if (!viewState.hasPastDays || start == null || end == null) {
                NoPastDays()
                return@Column
            }
            MonthsSection(viewState = viewState, start = start, end = end, callbacks = callbacks)
            GoalSection(viewState = viewState, start = start, onSelected = callbacks::onGoalSelected)
            Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                SectionLabel(text = stringResource(resource = Res.string.history_after_saving_step))
                val isEdited = { period: GoalPeriod -> period.start >= start && period.end <= end }
                TimelineBar(viewState = viewState, isEdited = isEdited)
                GoalPeriodList(periods = viewState.preview, today = viewState.today, isHighlighted = isEdited)
            }
            ImpactNote(viewState = viewState)
        }
    }
    viewState.sheet?.let { side ->
        DateSheet(viewState = viewState, side = side, callbacks = callbacks)
    }
}

@Composable
private fun MonthsSection(
    viewState: PastGoalViewState,
    start: LocalDate,
    end: LocalDate,
    callbacks: PastGoalCallbacks
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SectionLabel(text = stringResource(resource = Res.string.pick_months_step))
        viewState.months.chunked(size = 4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                row.forEach { month ->
                    val isEdge = month == start.firstOfMonth() || month == end.firstOfMonth()
                    val isInside = month > start.firstOfMonth() && month < end.firstOfMonth()
                    val shape = RoundedCornerShape(size = 12.dp)
                    Box(
                        modifier = Modifier
                            .weight(weight = 1f)
                            .height(height = 40.dp)
                            .clip(shape = shape)
                            .background(color = if (isEdge) colors.accent else if (isInside) colors.accentSoft else colors.surface)
                            .border(width = 1.5.dp, color = if (isEdge) colors.accent else if (isInside) Color.Transparent else colors.line, shape = shape)
                            .clickable(role = Role.Button) { callbacks.onMonthTapped(month = month) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = month.shortMonth(today = viewState.today),
                            style = typography.bold13,
                            color = if (isEdge) colors.onAccent else if (isInside) colors.accentInk else colors.ink
                        )
                    }
                }
                repeat(times = 4 - row.size) { Spacer(modifier = Modifier.weight(weight = 1f)) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            DateField(
                modifier = Modifier.weight(weight = 1f),
                label = Res.string.from,
                date = start,
                today = viewState.today,
                onClick = { callbacks.onOpenDateSheet(side = DateSide.FROM) }
            )
            DateField(
                modifier = Modifier.weight(weight = 1f),
                label = Res.string.until,
                date = end,
                today = viewState.today,
                onClick = { callbacks.onOpenDateSheet(side = DateSide.UNTIL) }
            )
        }
        Text(text = stringResource(resource = Res.string.pick_exact_day_hint), style = typography.regular11, color = colors.muted)
    }
}

@Composable
private fun DateField(
    modifier: Modifier = Modifier,
    label: StringResource,
    date: LocalDate,
    today: LocalDate,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = stringResource(resource = label), style = typography.regular11, color = colors.muted)
            Text(text = date.toAbbreviatedMonthDay() + ", " + date.year, style = typography.bold14, color = colors.ink)
        }
        Icon(
            painter = painterResource(resource = Res.drawable.ic_calendar),
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(size = 16.dp)
        )
    }
}

@Composable
private fun GoalSection(
    viewState: PastGoalViewState,
    start: LocalDate,
    onSelected: (Goal) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SectionLabel(text = stringResource(resource = Res.string.pick_goal_step))
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Goal.entries.forEach { goal ->
                val isSelected = goal == viewState.selectedGoal
                val shape = RoundedCornerShape(size = 16.dp)
                Column(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .clip(shape = shape)
                        .background(color = if (isSelected) colors.accentSoft else colors.surface)
                        .border(width = 1.5.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
                        .clickable(role = Role.RadioButton) { onSelected(goal) }
                        .padding(all = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 2.dp)
                ) {
                    Text(text = stringResource(resource = goal.title), style = typography.bold14, color = colors.ink)
                    viewState.targets[goal]?.let { targets ->
                        Text(
                            text = stringResource(resource = Res.string.value_kcal, targets.calories.withThousands()),
                            style = typography.regular11,
                            color = colors.muted
                        )
                    }
                }
            }
        }
        viewState.weightAtStart?.let { weight ->
            Text(
                text = stringResource(
                    resource = Res.string.targets_from_weight,
                    LocalUnitSystem.current.weightInputText(kg = weight),
                    start.toShortDate(today = viewState.today),
                    weightUnitLabel()
                ),
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun TimelineBar(
    viewState: PastGoalViewState,
    isEdited: (GoalPeriod) -> Boolean
) {
    val periods = viewState.preview.reversed()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 14.dp)
            .clip(shape = RoundedCornerShape(size = 7.dp)),
        horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
    ) {
        periods.forEach { period ->
            val days = (period.end.toEpochDays() - period.start.toEpochDays() + 1).toFloat()
            val (_, ink) = period.goal.tones()
            Box(
                modifier = Modifier
                    .weight(weight = days)
                    .fillMaxSize()
                    .alpha(alpha = if (isEdited(period)) 1f else 0.35f)
                    .background(color = if (period.goal == Goal.LOSE_WEIGHT) colors.accent else ink)
            )
        }
    }
}

@Composable
private fun ImpactNote(viewState: PastGoalViewState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.hero)
            .padding(all = 14.dp)
    ) {
        Text(
            text = stringResource(
                resource = Res.string.past_goal_impact,
                viewState.dayCount,
                stringResource(resource = viewState.selectedGoal.title),
                viewState.loggedDayCount
            ),
            style = typography.regular13,
            color = colors.ink
        )
    }
}

@Composable
private fun NoPastDays() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        Text(text = stringResource(resource = Res.string.no_past_days_title), style = typography.bold16, color = colors.ink)
        Text(
            text = stringResource(resource = Res.string.no_past_days_hint),
            style = typography.regular13,
            color = colors.muted,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSheet(
    viewState: PastGoalViewState,
    side: DateSide,
    callbacks: PastGoalCallbacks
) {
    val month = viewState.sheetMonth ?: return
    val firstMonth = viewState.months.firstOrNull() ?: return
    val lastMonth = viewState.months.last()
    val today = viewState.today
    ModalBottomSheet(
        onDismissRequest = callbacks::onCloseDateSheet,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = stringResource(resource = if (side == DateSide.FROM) Res.string.starts_on else Res.string.ends_on),
                    style = typography.bold18,
                    color = colors.ink,
                    modifier = Modifier.weight(weight = 1f)
                )
                val other = if (side == DateSide.FROM) viewState.end else viewState.start
                other?.let {
                    Text(
                        text = stringResource(
                            resource = if (side == DateSide.FROM) Res.string.until_value else Res.string.from_value,
                            it.toShortDate(today = today)
                        ),
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                SheetNavButton(
                    icon = Res.drawable.ic_chevron_left,
                    description = Res.string.previous_month,
                    isEnabled = month > firstMonth,
                    onClick = { callbacks.onSheetMonthChanged(month = month.minus(period = DatePeriod(months = 1))) }
                )
                Text(
                    text = month.month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + month.year,
                    style = typography.bold16,
                    color = colors.ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(weight = 1f)
                )
                SheetNavButton(
                    icon = Res.drawable.ic_chevron_right,
                    description = Res.string.next_month,
                    isEnabled = month < lastMonth,
                    onClick = { callbacks.onSheetMonthChanged(month = month.plus(period = DatePeriod(months = 1))) }
                )
            }
            val next = month.plus(period = DatePeriod(months = 1))
            val days = generateSequence(seed = month) { it.plus(period = DatePeriod(days = 1)) }.takeWhile { it < next }.toList()
            val cells = List<LocalDate?>(size = month.dayOfWeek.isoDayNumber - 1) { null } + days
            Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { letter ->
                        Text(
                            text = letter,
                            style = typography.bold11,
                            color = colors.faint,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(weight = 1f)
                        )
                    }
                }
                cells.chunked(size = 7).forEach { week ->
                    Row(horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                        week.forEach { date ->
                            Box(modifier = Modifier.weight(weight = 1f).height(height = 40.dp)) {
                                if (date != null) {
                                    SheetDay(
                                        date = date,
                                        viewState = viewState,
                                        isEnabled = viewState.isSelectable(date = date, side = side),
                                        onClick = { callbacks.onDatePicked(date = date) }
                                    )
                                }
                            }
                        }
                        repeat(times = 7 - week.size) { Spacer(modifier = Modifier.weight(weight = 1f)) }
                    }
                }
            }
            CommonPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = Res.string.done,
                onClick = callbacks::onCloseDateSheet
            )
        }
    }
}

@Composable
private fun SheetDay(
    date: LocalDate,
    viewState: PastGoalViewState,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val start = viewState.start
    val end = viewState.end
    val isEdge = date == start || date == end
    val isInside = start != null && end != null && date > start && date < end
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha = if (isEnabled || isEdge) 1f else 0.4f)
            .clip(shape = RoundedCornerShape(size = 10.dp))
            .background(color = if (isEdge) colors.accent else if (isInside) colors.accentSoft else Color.Transparent)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.day.toString(),
            style = typography.bold14,
            color = when {
                isEdge -> colors.onAccent
                isInside -> colors.accentInk
                isEnabled -> colors.ink
                else -> colors.faint
            }
        )
    }
}

@Composable
private fun SheetNavButton(
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
private fun SectionLabel(text: String) {
    Text(text = text, style = typography.bold12, color = colors.muted)
}

private fun LocalDate.shortMonth(today: LocalDate): String =
    month.name.take(n = 3).lowercase().replaceFirstChar { it.uppercase() } +
        if (year != today.year) " '" + (year % 100).toString().padStart(length = 2, padChar = '0') else ""

@Preview(showBackground = true)
@Composable
private fun PreviewPastGoalScreen() {
    val today = getCurrentDate()
    val maintain = MacroGoals(calories = 2650, protein = 148, carbs = 397, fat = 52)
    val deficit = MacroGoals(calories = 2350, protein = 148, carbs = 322, fat = 52)
    val records = listOf(
        DailyGoal(date = today.minus(period = DatePeriod(months = 6)), goal = Goal.MAINTAIN_WEIGHT, macroGoals = maintain)
    )
    MacroTrackTheme {
        PastGoalScreen(
            viewState = PastGoalViewState(
                today = today,
                records = records,
                current = DailyGoal(date = today, goal = Goal.LOSE_WEIGHT, macroGoals = deficit),
                earliestDate = records.first().date,
                start = today.minus(period = DatePeriod(months = 4)).firstOfMonth(),
                end = today.minus(period = DatePeriod(months = 2)).firstOfMonth(),
                selectedGoal = Goal.LOSE_WEIGHT,
                targets = mapOf(Goal.LOSE_WEIGHT to deficit, Goal.MAINTAIN_WEIGHT to maintain, Goal.GAIN_WEIGHT to maintain),
                weightAtStart = 78.4
            ),
            callbacks = PastGoalCallbacks.default(),
            onBack = {}
        )
    }
}
