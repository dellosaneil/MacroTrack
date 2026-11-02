package org.thelazybattley.macrotrack.features.checklist.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.checklist_about_days
import macrotrack.composeapp.generated.resources.checklist_best
import macrotrack.composeapp.generated.resources.checklist_calendar_aria
import macrotrack.composeapp.generated.resources.checklist_dash
import macrotrack.composeapp.generated.resources.checklist_days_count
import macrotrack.composeapp.generated.resources.checklist_days_every
import macrotrack.composeapp.generated.resources.checklist_days_weekdays
import macrotrack.composeapp.generated.resources.checklist_edit
import macrotrack.composeapp.generated.resources.checklist_joined
import macrotrack.composeapp.generated.resources.checklist_left_stat
import macrotrack.composeapp.generated.resources.checklist_legend_done
import macrotrack.composeapp.generated.resources.checklist_legend_missed
import macrotrack.composeapp.generated.resources.checklist_legend_skipped
import macrotrack.composeapp.generated.resources.checklist_n_of_m_days
import macrotrack.composeapp.generated.resources.checklist_next_month
import macrotrack.composeapp.generated.resources.checklist_pattern_weekends_body
import macrotrack.composeapp.generated.resources.checklist_pattern_weekends_title
import macrotrack.composeapp.generated.resources.checklist_paused_since
import macrotrack.composeapp.generated.resources.checklist_percent
import macrotrack.composeapp.generated.resources.checklist_previous_month
import macrotrack.composeapp.generated.resources.checklist_refill
import macrotrack.composeapp.generated.resources.checklist_refills
import macrotrack.composeapp.generated.resources.checklist_reminder_off
import macrotrack.composeapp.generated.resources.checklist_row_dose
import macrotrack.composeapp.generated.resources.checklist_row_goal
import macrotrack.composeapp.generated.resources.checklist_row_reminder
import macrotrack.composeapp.generated.resources.checklist_row_since_start
import macrotrack.composeapp.generated.resources.checklist_row_started
import macrotrack.composeapp.generated.resources.checklist_row_status
import macrotrack.composeapp.generated.resources.checklist_row_when
import macrotrack.composeapp.generated.resources.checklist_since_start_stat
import macrotrack.composeapp.generated.resources.checklist_since_start_value
import macrotrack.composeapp.generated.resources.checklist_streak
import macrotrack.composeapp.generated.resources.checklist_this_month
import macrotrack.composeapp.generated.resources.checklist_goal_tick
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_pattern
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.core.toShortDayName
import org.thelazybattley.macrotrack.domain.model.Checklist
import org.thelazybattley.macrotrack.domain.model.ChecklistDays
import org.thelazybattley.macrotrack.domain.model.ChecklistItem
import org.thelazybattley.macrotrack.domain.model.ChecklistKind
import org.thelazybattley.macrotrack.domain.model.DayMark
import org.thelazybattley.macrotrack.features.checklist.ChecklistItemCallbacks
import org.thelazybattley.macrotrack.features.checklist.ChecklistItemViewModel
import org.thelazybattley.macrotrack.features.checklist.ChecklistItemViewState
import org.thelazybattley.macrotrack.features.water.ui.clockText
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

/** Misses are looked for over this many days for the pattern note. */
private const val PATTERN_DAYS = 30

@Composable
fun ChecklistItemScreen(modifier: Modifier = Modifier, onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val viewModel = koinViewModel<ChecklistItemViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    ChecklistItemScreen(modifier = modifier, viewState = viewState, callbacks = viewModel, onBack = onBack, onNavigate = onNavigate)
}

@Composable
private fun ChecklistItemScreen(
    modifier: Modifier = Modifier,
    viewState: ChecklistItemViewState,
    callbacks: ChecklistItemCallbacks,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val checklist = viewState.checklist
    val item = viewState.item
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            ChecklistHeader(title = item?.name.orEmpty(), subtitle = item?.let { checklistDoseAndTime(item = it) }, onBack = onBack)
        }
    ) { innerPadding ->
        val month = viewState.month
        if (checklist == null || item == null || month == null) return@Scaffold
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            Stats(checklist = checklist, item = item)
            MonthCalendar(
                checklist = checklist,
                item = item,
                month = month,
                canGoBack = viewState.canGoBack,
                canGoForward = viewState.canGoForward,
                onPrevious = callbacks::onPreviousMonth,
                onNext = callbacks::onNextMonth
            )
            val recent = checklist.completionOf(item = item, from = checklist.today.minus(period = DatePeriod(days = PATTERN_DAYS - 1)))
            if (recent.isMostlyWeekends) PatternNote(weekendMisses = recent.weekendMisses, misses = recent.missed)
            Details(checklist = checklist, item = item)
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                if (item.stock != null) {
                    ChecklistOutlineButton(
                        text = stringResource(resource = Res.string.checklist_refill),
                        modifier = Modifier.weight(weight = 1f),
                        onClick = { callbacks.onOpenRefill(isOpen = true) }
                    )
                }
                ChecklistOutlineButton(
                    text = stringResource(resource = Res.string.checklist_edit),
                    modifier = Modifier.weight(weight = 1f),
                    color = colors.accentInk,
                    onClick = { onNavigate(AppDestinations.Root.ChecklistForm.createRoute(itemId = item.id)) }
                )
            }
        }
    }
    if (viewState.isRefilling && item != null) {
        ChecklistRefillSheet(item = item, onRefill = callbacks::onRefill, onDismiss = { callbacks.onOpenRefill(isOpen = false) })
    }
}

@Composable
private fun Stats(checklist: Checklist, item: ChecklistItem) {
    val today = checklist.today
    val monthStart = LocalDate(year = today.year, month = today.month, day = 1)
    val month = checklist.completionOf(item = item, from = monthStart)
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        val streak = checklist.streakOf(item = item)
        StatTile(
            label = stringResource(resource = Res.string.checklist_streak),
            value = pluralStringResource(resource = Res.plurals.checklist_days_count, quantity = streak, streak),
            sub = stringResource(resource = Res.string.checklist_best, checklist.bestStreakOf(item = item))
        )
        StatTile(
            label = stringResource(resource = Res.string.checklist_this_month),
            value = if (month.counted == 0) stringResource(resource = Res.string.checklist_dash) else percent(share = month.share),
            sub = stringResource(resource = Res.string.checklist_n_of_m_days, month.done, month.counted)
        )
        val stock = item.stock
        if (stock != null) {
            val days = item.daysFor(stock = stock)
            StatTile(
                label = stringResource(resource = Res.string.checklist_left_stat),
                value = stock.toString(),
                sub = pluralStringResource(resource = Res.plurals.checklist_about_days, quantity = days, days).replaceFirstChar { it.uppercase() }
            )
        } else {
            val all = checklist.completionOf(item = item, from = item.createdOn)
            StatTile(
                label = stringResource(resource = Res.string.checklist_since_start_stat),
                value = if (all.counted == 0) stringResource(resource = Res.string.checklist_dash) else percent(share = all.share),
                sub = stringResource(resource = Res.string.checklist_n_of_m_days, all.done, all.counted)
            )
        }
    }
}

@Composable
private fun percent(share: Float): String = stringResource(resource = Res.string.checklist_percent, (share * 100).roundToInt())

@Composable
private fun androidx.compose.foundation.layout.RowScope.StatTile(label: String, value: String, sub: String) {
    val shape = RoundedCornerShape(size = 14.dp)
    Column(
        modifier = Modifier
            .weight(weight = 1f)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = label, style = typography.bold11, color = colors.muted, maxLines = 1)
        Text(text = value, style = typography.bold18, color = colors.ink, maxLines = 1)
        Text(text = sub, style = typography.regular11, color = colors.muted, maxLines = 1)
    }
}

@Composable
private fun MonthCalendar(
    checklist: Checklist,
    item: ChecklistItem,
    month: LocalDate,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val nextMonth = month.plus(period = DatePeriod(months = 1))
    val days = (0 until month.daysUntil(other = nextMonth)).map { month.plus(period = DatePeriod(days = it)) }
    val marks = days.associateWith { checklist.markOn(item = item, date = it) }
    val monthName = month.month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + month.year
    val shape = RoundedCornerShape(size = 20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            MonthButton(icon = Res.drawable.ic_chevron_left, description = stringResource(resource = Res.string.checklist_previous_month), isEnabled = canGoBack, onClick = onPrevious)
            Text(text = monthName, style = typography.bold14, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            MonthButton(icon = Res.drawable.ic_chevron_right, description = stringResource(resource = Res.string.checklist_next_month), isEnabled = canGoForward, onClick = onNext)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            LegendKey(label = stringResource(resource = Res.string.checklist_legend_done), fill = colors.accent, border = null)
            LegendKey(label = stringResource(resource = Res.string.checklist_legend_skipped), fill = colors.divider, border = colors.dashed)
            LegendKey(label = stringResource(resource = Res.string.checklist_legend_missed), fill = colors.warmSoft, border = colors.carbs)
        }
        Row(modifier = Modifier.clearAndSetSemantics { }, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
            DayOfWeek.entries.forEach { day ->
                Text(
                    text = day.name.take(n = 1),
                    style = typography.bold10,
                    color = colors.faint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(weight = 1f)
                )
            }
        }
        val description = stringResource(
            resource = Res.string.checklist_calendar_aria,
            monthName,
            marks.count { it.value == DayMark.DONE },
            marks.count { it.value == DayMark.SKIPPED },
            marks.count { it.value == DayMark.MISSED }
        )
        val cells = List(month.dayOfWeek.ordinal) { null } + days
        Column(modifier = Modifier.clearAndSetSemantics { contentDescription = description }, verticalArrangement = Arrangement.spacedBy(space = 5.dp)) {
            cells.chunked(size = 7).forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
                    (0 until 7).forEach { index ->
                        val date = week.getOrNull(index)
                        Box(modifier = Modifier.weight(weight = 1f).height(height = 34.dp)) {
                            if (date != null) DayCell(day = date.day, mark = marks.getValue(date))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, mark: DayMark) {
    val shape = RoundedCornerShape(size = 9.dp)
    val (fill, border, ink) = when (mark) {
        DayMark.DONE -> Triple(colors.accent, null, colors.onAccent)
        DayMark.SKIPPED -> Triple(colors.divider, colors.dashed, colors.muted)
        DayMark.MISSED -> Triple(colors.warmSoft, colors.carbs, colors.warmInk)
        DayMark.TO_DO -> Triple(colors.surface, colors.accent, colors.ink)
        DayMark.NOT_DUE, DayMark.FUTURE -> Triple(Color.Transparent, null, colors.faint)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = shape)
            .background(color = fill)
            .then(if (border != null) Modifier.border(width = if (mark == DayMark.TO_DO) 2.dp else 1.5.dp, color = border, shape = shape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(text = day.toString(), style = typography.bold12, color = ink)
    }
}

@Composable
private fun LegendKey(label: String, fill: Color, border: Color?) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
        val shape = RoundedCornerShape(size = 3.dp)
        Box(
            modifier = Modifier
                .size(size = 10.dp)
                .clip(shape = shape)
                .background(color = fill)
                .then(if (border != null) Modifier.border(width = 1.5.dp, color = border, shape = shape) else Modifier)
        )
        Text(text = label, style = typography.regular11, color = colors.muted)
    }
}

@Composable
private fun MonthButton(icon: DrawableResource, description: String, isEnabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size = 36.dp)
            .alpha(alpha = if (isEnabled) 1f else 0.3f)
            .clip(shape = RoundedCornerShape(size = 10.dp))
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = colors.ink, modifier = Modifier.size(size = 16.dp))
    }
}

@Composable
private fun PatternNote(weekendMisses: Int, misses: Int) {
    val title = stringResource(resource = Res.string.checklist_pattern_weekends_title)
    val body = stringResource(resource = Res.string.checklist_pattern_weekends_body, weekendMisses, misses)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.nightSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(painter = painterResource(resource = Res.drawable.ic_pattern), contentDescription = null, tint = colors.nightInk, modifier = Modifier.padding(top = 1.dp).size(size = 18.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(text = title) }
                append(text = " $body")
            },
            style = typography.regular13,
            color = colors.ink
        )
    }
}

@Composable
private fun Details(checklist: Checklist, item: ChecklistItem) {
    val since = checklist.completionOf(item = item, from = item.createdOn)
    val started = item.createdOn.daysUntil(other = checklist.today) + 1
    val rows = buildList {
        add(
            (if (item.kind == ChecklistKind.SUPPLEMENT) Res.string.checklist_row_dose else Res.string.checklist_row_goal) to
                checklistDose(item = item).ifEmpty { stringResource(resource = Res.string.checklist_goal_tick) }
        )
        add(Res.string.checklist_row_when to stringResource(resource = Res.string.checklist_joined, stringResource(resource = item.time.label), daysText(item = item)))
        add(Res.string.checklist_row_reminder to (item.reminderMinutes?.let { clockText(minutes = it) } ?: stringResource(resource = Res.string.checklist_reminder_off)))
        add(
            Res.string.checklist_row_started to stringResource(
                resource = Res.string.checklist_joined,
                item.createdOn.toAbbreviatedMonthDay(),
                pluralStringResource(resource = Res.plurals.checklist_days_count, quantity = started, started)
            )
        )
        val done = stringResource(resource = Res.string.checklist_since_start_value, (since.share * 100).roundToInt())
        add(
            Res.string.checklist_row_since_start to if (item.stock != null || item.refills > 0) {
                stringResource(resource = Res.string.checklist_joined, done, pluralStringResource(resource = Res.plurals.checklist_refills, quantity = item.refills, item.refills))
            } else {
                done
            }
        )
        item.pausedOn?.let { add(Res.string.checklist_row_status to stringResource(resource = Res.string.checklist_paused_since, it.toAbbreviatedMonthDay())) }
    }
    ChecklistCard {
        rows.forEachIndexed { index, (label, value) ->
            if (index > 0) ChecklistDivider()
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                Text(text = stringResource(resource = label), style = typography.regular13, color = colors.muted, modifier = Modifier.weight(weight = 1f))
                Text(text = value, style = typography.bold13, color = colors.ink, textAlign = TextAlign.End)
            }
        }
    }
}

/** "Every day", "Weekdays" or "Mon, Wed, Fri". */
@Composable
fun daysText(item: ChecklistItem): String = when (ChecklistDays.of(days = item.days)) {
    ChecklistDays.EVERY_DAY -> stringResource(resource = Res.string.checklist_days_every)
    ChecklistDays.WEEKDAYS -> stringResource(resource = Res.string.checklist_days_weekdays)
    ChecklistDays.CUSTOM -> DayOfWeek.entries.filter { it in item.days }.joinToString(separator = ", ") { day ->
        LocalDate(year = 2024, month = 1, day = 1 + day.ordinal).toShortDayName()
    }
}
