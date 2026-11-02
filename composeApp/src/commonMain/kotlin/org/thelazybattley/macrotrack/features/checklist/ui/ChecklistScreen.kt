package org.thelazybattley.macrotrack.features.checklist.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.checklist_about_days
import macrotrack.composeapp.generated.resources.checklist_all_done
import macrotrack.composeapp.generated.resources.checklist_all_done_body
import macrotrack.composeapp.generated.resources.checklist_all_done_today
import macrotrack.composeapp.generated.resources.checklist_done_at
import macrotrack.composeapp.generated.resources.checklist_done_meta
import macrotrack.composeapp.generated.resources.checklist_due
import macrotrack.composeapp.generated.resources.checklist_due_reminder
import macrotrack.composeapp.generated.resources.checklist_edit
import macrotrack.composeapp.generated.resources.checklist_edit_body
import macrotrack.composeapp.generated.resources.checklist_edit_habit_body
import macrotrack.composeapp.generated.resources.checklist_empty_body
import macrotrack.composeapp.generated.resources.checklist_empty_note
import macrotrack.composeapp.generated.resources.checklist_empty_title
import macrotrack.composeapp.generated.resources.checklist_footer
import macrotrack.composeapp.generated.resources.checklist_habit
import macrotrack.composeapp.generated.resources.checklist_history_open
import macrotrack.composeapp.generated.resources.checklist_left
import macrotrack.composeapp.generated.resources.checklist_low_body
import macrotrack.composeapp.generated.resources.checklist_low_title
import macrotrack.composeapp.generated.resources.checklist_manage_open
import macrotrack.composeapp.generated.resources.checklist_mark_done
import macrotrack.composeapp.generated.resources.checklist_mark_done_day
import macrotrack.composeapp.generated.resources.checklist_mark_done_now
import macrotrack.composeapp.generated.resources.checklist_missed_meta
import macrotrack.composeapp.generated.resources.checklist_more
import macrotrack.composeapp.generated.resources.checklist_n_of_m_done
import macrotrack.composeapp.generated.resources.checklist_next
import macrotrack.composeapp.generated.resources.checklist_next_day
import macrotrack.composeapp.generated.resources.checklist_nothing_due
import macrotrack.composeapp.generated.resources.checklist_part_count
import macrotrack.composeapp.generated.resources.checklist_past_open
import macrotrack.composeapp.generated.resources.checklist_previous_day
import macrotrack.composeapp.generated.resources.checklist_quick_start
import macrotrack.composeapp.generated.resources.checklist_refill
import macrotrack.composeapp.generated.resources.checklist_ring_aria
import macrotrack.composeapp.generated.resources.checklist_ring_done
import macrotrack.composeapp.generated.resources.checklist_see_history
import macrotrack.composeapp.generated.resources.checklist_see_history_body
import macrotrack.composeapp.generated.resources.checklist_skip_body
import macrotrack.composeapp.generated.resources.checklist_skip_day
import macrotrack.composeapp.generated.resources.checklist_skip_today
import macrotrack.composeapp.generated.resources.checklist_skipped_meta
import macrotrack.composeapp.generated.resources.checklist_snooze
import macrotrack.composeapp.generated.resources.checklist_snooze_body
import macrotrack.composeapp.generated.resources.checklist_streak_aria
import macrotrack.composeapp.generated.resources.checklist_subtitle
import macrotrack.composeapp.generated.resources.checklist_supplement
import macrotrack.composeapp.generated.resources.checklist_tap_once
import macrotrack.composeapp.generated.resources.checklist_tick
import macrotrack.composeapp.generated.resources.checklist_times_progress
import macrotrack.composeapp.generated.resources.checklist_title
import macrotrack.composeapp.generated.resources.checklist_today_date
import macrotrack.composeapp.generated.resources.checklist_undo_skip
import macrotrack.composeapp.generated.resources.checklist_undo_skip_body
import macrotrack.composeapp.generated.resources.checklist_untick
import macrotrack.composeapp.generated.resources.checklist_untick_action
import macrotrack.composeapp.generated.resources.checklist_untick_body
import macrotrack.composeapp.generated.resources.ic_bar_chart
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_checklist
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_clock
import macrotrack.composeapp.generated.resources.ic_flame
import macrotrack.composeapp.generated.resources.ic_minus
import macrotrack.composeapp.generated.resources.ic_more_vertical
import macrotrack.composeapp.generated.resources.ic_pencil
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_refill
import macrotrack.composeapp.generated.resources.ic_skip
import macrotrack.composeapp.generated.resources.ic_sliders
import macrotrack.composeapp.generated.resources.ic_undo
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.CHECKLIST_SNOOZE_MINUTES
import org.thelazybattley.macrotrack.domain.model.Checklist
import org.thelazybattley.macrotrack.domain.model.ChecklistEntry
import org.thelazybattley.macrotrack.domain.model.ChecklistIcon
import org.thelazybattley.macrotrack.domain.model.ChecklistItem
import org.thelazybattley.macrotrack.domain.model.ChecklistKind
import org.thelazybattley.macrotrack.domain.model.ChecklistSettings
import org.thelazybattley.macrotrack.domain.model.ChecklistStatus
import org.thelazybattley.macrotrack.domain.model.ChecklistTime
import org.thelazybattley.macrotrack.domain.model.DayMark
import org.thelazybattley.macrotrack.domain.model.DayPart
import org.thelazybattley.macrotrack.domain.model.SupplementUnit
import org.thelazybattley.macrotrack.domain.usecase.checklist.ChecklistAction
import org.thelazybattley.macrotrack.domain.usecase.checklist.phrase
import org.thelazybattley.macrotrack.features.checklist.ChecklistCallbacks
import org.thelazybattley.macrotrack.features.checklist.ChecklistPreset
import org.thelazybattley.macrotrack.features.checklist.ChecklistViewModel
import org.thelazybattley.macrotrack.features.checklist.ChecklistViewState
import org.thelazybattley.macrotrack.features.water.ui.clockText
import org.thelazybattley.macrotrack.ui.common.dashedBorder
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun ChecklistScreen(modifier: Modifier = Modifier, onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val viewModel = koinViewModel<ChecklistViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    ChecklistScreen(modifier = modifier, viewState = viewState, callbacks = viewModel, onBack = onBack, onNavigate = onNavigate)
}

@Composable
private fun ChecklistScreen(
    modifier: Modifier = Modifier,
    viewState: ChecklistViewState,
    callbacks: ChecklistCallbacks,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val checklist = viewState.checklist
    val isEmpty = checklist != null && checklist.items.isEmpty()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            ChecklistHeader(
                title = stringResource(resource = Res.string.checklist_title),
                subtitle = stringResource(resource = Res.string.checklist_subtitle),
                onBack = onBack
            ) {
                if (checklist != null && !isEmpty) {
                    ChecklistIconButton(
                        icon = Res.drawable.ic_bar_chart,
                        description = stringResource(resource = Res.string.checklist_history_open),
                        onClick = { onNavigate(AppDestinations.Root.ChecklistHistory.route) }
                    )
                    ChecklistIconButton(
                        icon = Res.drawable.ic_sliders,
                        description = stringResource(resource = Res.string.checklist_manage_open),
                        onClick = { onNavigate(AppDestinations.Root.ChecklistManage.route) }
                    )
                }
            }
        }
    ) { innerPadding ->
        val date = viewState.selectedDate
        if (checklist == null || date == null) return@Scaffold
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            if (isEmpty) {
                EmptyContent(onNavigate = onNavigate)
            } else {
                TodayContent(
                    checklist = checklist,
                    date = date,
                    isToday = viewState.isToday,
                    callbacks = callbacks,
                    onNavigate = onNavigate
                )
            }
        }
    }
    viewState.menuItem?.let { item ->
        if (viewState.selectedDate != null && checklist != null) {
            ItemMenu(
                item = item,
                mark = checklist.markOn(item = item, date = viewState.selectedDate!!),
                isToday = viewState.isToday,
                callbacks = callbacks,
                onNavigate = onNavigate
            )
        }
    }
    viewState.refillItem?.let { item ->
        ChecklistRefillSheet(
            item = item,
            onRefill = { added, warnAt -> callbacks.onRefill(item = item, added = added, warnAt = warnAt) },
            onDismiss = callbacks::onCloseRefill
        )
    }
}

@Composable
private fun TodayContent(
    checklist: Checklist,
    date: LocalDate,
    isToday: Boolean,
    callbacks: ChecklistCallbacks,
    onNavigate: (String) -> Unit
) {
    DayNavigation(date = date, isToday = isToday, onPrevious = callbacks::onPreviousDay, onNext = callbacks::onNextDay)
    val summary = checklist.summaryOn(date = date)
    SummaryCard(done = summary.done, total = summary.total, next = summary.next, isToday = isToday, isEmptyDay = summary.items.isEmpty())
    checklist.lowItem?.let { item -> LowStockBanner(item = item, onClick = { callbacks.onOpenRefill(item = item) }) }
    DayPart.entries.forEach { part ->
        val items = summary.items.filter { it.time.part == part }
        if (items.isEmpty()) return@forEach
        Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            Row {
                ChecklistLabel(text = stringResource(resource = part.label), modifier = Modifier.weight(weight = 1f))
                Text(
                    text = stringResource(
                        resource = Res.string.checklist_part_count,
                        items.count { checklist.markOn(item = it, date = date) == DayMark.DONE },
                        items.size
                    ),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
            ChecklistCard {
                items.forEachIndexed { index, item ->
                    if (index > 0) ChecklistDivider()
                    ItemRow(
                        item = item,
                        mark = checklist.markOn(item = item, date = date),
                        entry = checklist.entryOf(itemId = item.id, date = date),
                        streak = checklist.streakOf(item = item),
                        onTap = { callbacks.onTap(item = item) },
                        onOpen = { onNavigate(AppDestinations.Root.ChecklistItem.createRoute(itemId = item.id)) },
                        onMenu = { callbacks.onOpenMenu(item = item) }
                    )
                }
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        AddButton(
            text = stringResource(resource = Res.string.checklist_supplement),
            modifier = Modifier.weight(weight = 1f),
            onClick = { onNavigate(AppDestinations.Root.ChecklistForm.createRoute(kind = ChecklistKind.SUPPLEMENT.name)) }
        )
        AddButton(
            text = stringResource(resource = Res.string.checklist_habit),
            modifier = Modifier.weight(weight = 1f),
            onClick = { onNavigate(AppDestinations.Root.ChecklistForm.createRoute(kind = ChecklistKind.HABIT.name)) }
        )
    }
    Text(text = stringResource(resource = Res.string.checklist_footer), style = typography.regular11, color = colors.muted)
}

@Composable
private fun DayNavigation(date: LocalDate, isToday: Boolean, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        DayButton(icon = Res.drawable.ic_chevron_left, description = stringResource(resource = Res.string.checklist_previous_day), isEnabled = true, onClick = onPrevious)
        Text(
            text = if (isToday) stringResource(resource = Res.string.checklist_today_date, date.toShortDayMonthDay()) else date.toShortDayMonthDay(),
            style = typography.bold14,
            color = colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(weight = 1f)
        )
        DayButton(icon = Res.drawable.ic_chevron_right, description = stringResource(resource = Res.string.checklist_next_day), isEnabled = !isToday, onClick = onNext)
    }
}

@Composable
private fun DayButton(icon: DrawableResource, description: String, isEnabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 12.dp)
    Box(
        modifier = Modifier
            .size(size = 40.dp)
            .alpha(alpha = if (isEnabled) 1f else 0.35f)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = colors.ink, modifier = Modifier.size(size = 18.dp))
    }
}

@Composable
private fun SummaryCard(done: Int, total: Int, next: ChecklistItem?, isToday: Boolean, isEmptyDay: Boolean) {
    val isAllDone = total > 0 && done >= total
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 22.dp))
            .background(color = colors.hero)
            .padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        val ringDescription = stringResource(resource = Res.string.checklist_ring_aria, done, total)
        Box(modifier = Modifier.size(size = 96.dp).clearAndSetSemantics { contentDescription = ringDescription }, contentAlignment = Alignment.Center) {
            ProgressRing(fraction = if (total == 0) 0f else done.toFloat() / total)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$done/$total", style = typography.bold24, color = colors.ink)
                Text(text = stringResource(resource = Res.string.checklist_ring_done), style = typography.bold10, color = colors.muted)
            }
        }
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(
                text = when {
                    isAllDone && isToday -> stringResource(resource = Res.string.checklist_all_done_today)
                    isAllDone -> stringResource(resource = Res.string.checklist_all_done)
                    else -> stringResource(resource = Res.string.checklist_n_of_m_done, done, total)
                },
                style = typography.bold24,
                color = colors.ink
            )
            Text(
                text = when {
                    isEmptyDay -> stringResource(resource = Res.string.checklist_nothing_due)
                    isAllDone -> stringResource(resource = Res.string.checklist_all_done_body)
                    isToday && next != null -> stringResource(resource = Res.string.checklist_next, next.name, stringResource(resource = next.time.phrase))
                    else -> stringResource(resource = Res.string.checklist_past_open, total - done)
                },
                style = typography.regular13,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun ProgressRing(fraction: Float) {
    val track = colors.heroTrack
    val progress = colors.accent
    Canvas(modifier = Modifier.size(size = 96.dp)) {
        val stroke = 10.dp.toPx()
        val inset = stroke / 2 + 4.dp.toPx()
        val arcSize = Size(width = size.width - inset * 2, height = size.height - inset * 2)
        val topLeft = Offset(x = inset, y = inset)
        drawArc(color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width = stroke))
        if (fraction > 0f) {
            drawArc(
                color = progress,
                startAngle = -90f,
                sweepAngle = 360f * fraction.coerceAtMost(maximumValue = 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun LowStockBanner(item: ChecklistItem, onClick: () -> Unit) {
    val stock = item.stock ?: return
    val days = item.daysFor(stock = stock)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.warmSoft)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier.size(size = 34.dp).clip(shape = RoundedCornerShape(size = 10.dp)).background(color = colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(resource = Res.drawable.ic_refill), contentDescription = null, tint = colors.warmInk, modifier = Modifier.size(size = 18.dp))
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = stringResource(resource = Res.string.checklist_low_title, item.name), style = typography.bold14, color = colors.warmInk)
            Text(
                text = stringResource(
                    resource = Res.string.checklist_low_body,
                    stringResource(resource = Res.string.checklist_left, checklistStock(item = item, count = stock)),
                    pluralStringResource(resource = Res.plurals.checklist_about_days, quantity = days, days)
                ),
                style = typography.regular11,
                color = colors.warmInk
            )
        }
        Text(text = stringResource(resource = Res.string.checklist_refill), style = typography.bold13, color = colors.warmInk)
    }
}

@Composable
private fun ItemRow(
    item: ChecklistItem,
    mark: DayMark,
    entry: ChecklistEntry?,
    streak: Int,
    onTap: () -> Unit,
    onOpen: () -> Unit,
    onMenu: () -> Unit
) {
    val isDone = mark == DayMark.DONE
    val isSkipped = mark == DayMark.SKIPPED
    val partial = entry?.takeIf { it.status == ChecklistStatus.PARTIAL }?.count
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 66.dp)
            .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        CheckCircle(item = item, isDone = isDone, isSkipped = isSkipped, partial = partial, onTap = onTap)
        ChecklistItemIcon(icon = item.icon, isFaded = isDone || isSkipped)
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .clip(shape = RoundedCornerShape(size = 8.dp))
                .clickable(role = Role.Button, onClick = onOpen),
            verticalArrangement = Arrangement.spacedBy(space = 1.dp)
        ) {
            Text(
                text = item.name,
                style = typography.bold14,
                color = if (isDone) colors.muted else colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = checklistDoseAndTime(item = item), style = typography.regular11, color = colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = rowMeta(item = item, mark = mark, entry = entry, partial = partial),
                style = typography.bold11,
                color = when {
                    isDone -> colors.accentInk
                    isSkipped -> colors.faint
                    else -> colors.warmInk
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (streak > 1) {
            val streakDescription = pluralStringResource(resource = Res.plurals.checklist_streak_aria, quantity = streak, streak)
            Row(
                modifier = Modifier.clearAndSetSemantics { contentDescription = streakDescription },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                Icon(painter = painterResource(resource = Res.drawable.ic_flame), contentDescription = null, tint = colors.warmInk, modifier = Modifier.size(size = 13.dp))
                Text(text = streak.toString(), style = typography.bold12, color = colors.warmInk)
            }
        }
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 44.dp)
                .clip(shape = RoundedCornerShape(size = 8.dp))
                .clickable(role = Role.Button, onClick = onMenu),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_more_vertical),
                contentDescription = stringResource(resource = Res.string.checklist_more, item.name),
                tint = colors.muted,
                modifier = Modifier.size(size = 18.dp)
            )
        }
    }
}

@Composable
private fun rowMeta(item: ChecklistItem, mark: DayMark, entry: ChecklistEntry?, partial: Int?): String = when {
    mark == DayMark.DONE -> entry?.doneAtMillis?.let { stringResource(resource = Res.string.checklist_done_at, clockTextAt(millis = it)) }
        ?: stringResource(resource = Res.string.checklist_done_meta)
    mark == DayMark.SKIPPED -> stringResource(resource = Res.string.checklist_skipped_meta)
    partial != null -> stringResource(resource = Res.string.checklist_times_progress, partial, item.target)
    mark == DayMark.MISSED -> stringResource(resource = Res.string.checklist_missed_meta)
    else -> {
        val phrase = stringResource(resource = item.time.phrase)
        item.reminderMinutes?.let { stringResource(resource = Res.string.checklist_due_reminder, phrase, clockText(minutes = it)) }
            ?: stringResource(resource = Res.string.checklist_due, phrase)
    }
}

/** The tick: filled when done, a dash when skipped, the count so far for a times habit. */
@Composable
private fun CheckCircle(item: ChecklistItem, isDone: Boolean, isSkipped: Boolean, partial: Int?, onTap: () -> Unit) {
    val description = when {
        isDone -> stringResource(resource = Res.string.checklist_untick, item.name)
        item.target > 1 -> stringResource(resource = Res.string.checklist_tap_once, item.name, partial ?: 0, item.target)
        else -> stringResource(resource = Res.string.checklist_tick, item.name)
    }
    Box(
        modifier = Modifier
            .size(size = 32.dp)
            .clip(shape = CircleShape)
            .background(
                color = when {
                    isDone -> colors.accent
                    isSkipped -> colors.divider
                    else -> colors.surface
                }
            )
            .then(if (isDone || isSkipped) Modifier else Modifier.border(width = 2.dp, color = if (partial != null) colors.accent else colors.dashed, shape = CircleShape))
            .toggleable(value = isDone, role = Role.Checkbox, onValueChange = { onTap() })
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        when {
            isDone -> Icon(painter = painterResource(resource = Res.drawable.ic_check), contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(size = 16.dp))
            isSkipped -> Icon(painter = painterResource(resource = Res.drawable.ic_minus), contentDescription = null, tint = colors.muted, modifier = Modifier.size(size = 14.dp))
            partial != null -> Text(text = "$partial/${item.target}", style = typography.bold10, color = colors.accentInk)
        }
    }
}

@Composable
private fun AddButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .height(height = 50.dp)
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .dashedBorder(color = colors.dashed, width = 1.5.dp, cornerRadius = 16.dp)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(painter = painterResource(resource = Res.drawable.ic_plus), contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(size = 17.dp))
        Spacer(modifier = Modifier.width(width = 6.dp))
        Text(text = text, style = typography.bold14, color = colors.accentInk)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun ItemMenu(item: ChecklistItem, mark: DayMark, isToday: Boolean, callbacks: ChecklistCallbacks, onNavigate: (String) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = callbacks::onCloseMenu,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            Box(modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)) {
                ChecklistSheetTitle(title = item.name, subtitle = checklistDoseAndTime(item = item))
            }
            if (mark == DayMark.DONE) {
                MenuAction(
                    icon = Res.drawable.ic_undo,
                    tone = colors.nightSoft to colors.nightInk,
                    title = stringResource(resource = Res.string.checklist_untick_action),
                    body = stringResource(resource = Res.string.checklist_untick_body),
                    onClick = { callbacks.onMenuAction(item = item, action = ChecklistAction.CLEAR) }
                )
            } else {
                val now = clockTextAt(millis = Clock.System.now().toEpochMilliseconds())
                MenuAction(
                    icon = Res.drawable.ic_check,
                    tone = colors.accentSoft to colors.accentInk,
                    title = stringResource(resource = Res.string.checklist_mark_done),
                    body = if (isToday) stringResource(resource = Res.string.checklist_mark_done_now, now) else stringResource(resource = Res.string.checklist_mark_done_day),
                    onClick = { callbacks.onMenuAction(item = item, action = ChecklistAction.DONE) }
                )
            }
            if (mark == DayMark.SKIPPED) {
                MenuAction(
                    icon = Res.drawable.ic_undo,
                    tone = colors.nightSoft to colors.nightInk,
                    title = stringResource(resource = Res.string.checklist_undo_skip),
                    body = stringResource(resource = Res.string.checklist_undo_skip_body),
                    onClick = { callbacks.onMenuAction(item = item, action = ChecklistAction.CLEAR) }
                )
            } else {
                MenuAction(
                    icon = Res.drawable.ic_skip,
                    tone = colors.nightSoft to colors.nightInk,
                    title = stringResource(resource = if (isToday) Res.string.checklist_skip_today else Res.string.checklist_skip_day),
                    body = stringResource(resource = Res.string.checklist_skip_body),
                    onClick = { callbacks.onMenuAction(item = item, action = ChecklistAction.SKIP) }
                )
            }
            if (isToday && mark != DayMark.DONE && mark != DayMark.SKIPPED) {
                val later = clockTextAt(millis = Clock.System.now().toEpochMilliseconds() + CHECKLIST_SNOOZE_MINUTES * 60_000L)
                MenuAction(
                    icon = Res.drawable.ic_clock,
                    tone = colors.warmSoft to colors.warmInk,
                    title = stringResource(resource = Res.string.checklist_snooze),
                    body = stringResource(resource = Res.string.checklist_snooze_body, later),
                    onClick = { callbacks.onSnooze(item = item) }
                )
            }
            MenuAction(
                icon = Res.drawable.ic_bar_chart,
                tone = colors.divider to colors.protein,
                title = stringResource(resource = Res.string.checklist_see_history),
                body = stringResource(resource = Res.string.checklist_see_history_body),
                onClick = {
                    callbacks.onCloseMenu()
                    onNavigate(AppDestinations.Root.ChecklistItem.createRoute(itemId = item.id))
                }
            )
            MenuAction(
                icon = Res.drawable.ic_pencil,
                tone = colors.accentSoft to colors.accentInk,
                title = stringResource(resource = Res.string.checklist_edit),
                body = stringResource(resource = if (item.kind == ChecklistKind.SUPPLEMENT) Res.string.checklist_edit_body else Res.string.checklist_edit_habit_body),
                onClick = {
                    callbacks.onCloseMenu()
                    onNavigate(AppDestinations.Root.ChecklistForm.createRoute(itemId = item.id))
                }
            )
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .height(height = 48.dp)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .background(color = colors.divider)
                    .clickable(role = Role.Button, onClick = callbacks::onCloseMenu),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(resource = Res.string.cancel), style = typography.bold14, color = colors.ink)
            }
        }
    }
}

@Composable
private fun MenuAction(
    icon: DrawableResource,
    tone: Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color>,
    title: String,
    body: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Box(
            modifier = Modifier.size(size = 38.dp).clip(shape = RoundedCornerShape(size = 11.dp)).background(color = tone.first),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(resource = icon), contentDescription = null, tint = tone.second, modifier = Modifier.size(size = 18.dp))
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = title, style = typography.bold14, color = colors.ink)
            Text(text = body, style = typography.regular11, color = colors.muted)
        }
    }
}

/** The first time: what the checklist is for, and quick starts that only fill in the form. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmptyContent(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier.size(size = 96.dp).clip(shape = RoundedCornerShape(size = 30.dp)).background(color = colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(resource = Res.drawable.ic_checklist), contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(size = 44.dp))
        }
        Text(text = stringResource(resource = Res.string.checklist_empty_title), style = typography.bold24, color = colors.ink, textAlign = TextAlign.Center)
        Text(text = stringResource(resource = Res.string.checklist_empty_body), style = typography.regular13, color = colors.muted, textAlign = TextAlign.Center)
        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            listOf(ChecklistKind.SUPPLEMENT to Res.string.checklist_supplement, ChecklistKind.HABIT to Res.string.checklist_habit).forEach { (kind, label) ->
                val isMain = kind == ChecklistKind.SUPPLEMENT
                Row(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 54.dp)
                        .clip(shape = RoundedCornerShape(size = 16.dp))
                        .background(color = if (isMain) colors.accent else colors.accentSoft)
                        .clickable(role = Role.Button) { onNavigate(AppDestinations.Root.ChecklistForm.createRoute(kind = kind.name)) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val ink = if (isMain) colors.onAccent else colors.accentInk
                    Icon(painter = painterResource(resource = Res.drawable.ic_plus), contentDescription = null, tint = ink, modifier = Modifier.size(size = 17.dp))
                    Spacer(modifier = Modifier.width(width = 6.dp))
                    Text(text = stringResource(resource = label), style = typography.bold14, color = ink)
                }
            }
        }
        Column(modifier = Modifier.fillMaxWidth().padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            ChecklistLabel(text = stringResource(resource = Res.string.checklist_quick_start))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(space = 6.dp), verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                ChecklistPreset.entries.forEach { preset ->
                    val shape = RoundedCornerShape(size = 18.dp)
                    Row(
                        modifier = Modifier
                            .height(height = 36.dp)
                            .clip(shape = shape)
                            .background(color = colors.surface)
                            .border(width = 1.dp, color = colors.line, shape = shape)
                            .clickable(role = Role.Button) { onNavigate(AppDestinations.Root.ChecklistForm.createRoute(preset = preset.name)) }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                    ) {
                        Icon(painter = painterResource(resource = Res.drawable.ic_plus), contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(size = 14.dp))
                        Text(text = stringResource(resource = preset.label), style = typography.bold13, color = colors.ink)
                    }
                }
            }
        }
        Text(
            text = stringResource(resource = Res.string.checklist_empty_note),
            style = typography.regular11,
            color = colors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewChecklistScreen() {
    val today = LocalDate(year = 2026, month = 10, day = 8)
    val start = LocalDate(year = 2026, month = 9, day = 1)
    MacroTrackTheme {
        ChecklistScreen(
            viewState = ChecklistViewState(
                checklist = Checklist(
                    today = today,
                    items = listOf(
                        ChecklistItem(id = 1, kind = ChecklistKind.SUPPLEMENT, name = "Creatine", icon = ChecklistIcon.PILL, amount = 5.0, unit = SupplementUnit.G, time = ChecklistTime.MORNING, createdOn = start),
                        ChecklistItem(id = 2, kind = ChecklistKind.SUPPLEMENT, name = "Fish oil", icon = ChecklistIcon.FISH, amount = 2.0, time = ChecklistTime.DINNER, reminderMinutes = 19 * 60 + 30, stock = 6, createdOn = start),
                        ChecklistItem(id = 3, kind = ChecklistKind.HABIT, name = "Stretch before bed", icon = ChecklistIcon.MOON, time = ChecklistTime.BEDTIME, days = DayOfWeek.entries.toSet(), createdOn = start)
                    ),
                    entries = listOf(ChecklistEntry(itemId = 1, date = today, status = ChecklistStatus.DONE)),
                    settings = ChecklistSettings()
                )
            ),
            callbacks = ChecklistCallbacks.default(),
            onBack = {},
            onNavigate = {}
        )
    }
}
