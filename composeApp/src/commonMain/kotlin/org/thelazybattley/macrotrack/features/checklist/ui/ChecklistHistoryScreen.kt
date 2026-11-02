package org.thelazybattley.macrotrack.features.checklist.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.checklist_average
import macrotrack.composeapp.generated.resources.checklist_by_item
import macrotrack.composeapp.generated.resources.checklist_done_each_day
import macrotrack.composeapp.generated.resources.checklist_history_bar_aria
import macrotrack.composeapp.generated.resources.checklist_history_title
import macrotrack.composeapp.generated.resources.checklist_last_days
import macrotrack.composeapp.generated.resources.checklist_missed_days
import macrotrack.composeapp.generated.resources.checklist_mostly_weekends
import macrotrack.composeapp.generated.resources.checklist_never_missed
import macrotrack.composeapp.generated.resources.checklist_nothing_due_yet
import macrotrack.composeapp.generated.resources.checklist_percent
import macrotrack.composeapp.generated.resources.checklist_period
import macrotrack.composeapp.generated.resources.checklist_period_aria
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.Checklist
import org.thelazybattley.macrotrack.domain.model.ChecklistDayTotal
import org.thelazybattley.macrotrack.domain.model.ChecklistEntry
import org.thelazybattley.macrotrack.domain.model.ChecklistIcon
import org.thelazybattley.macrotrack.domain.model.ChecklistItem
import org.thelazybattley.macrotrack.domain.model.ChecklistKind
import org.thelazybattley.macrotrack.domain.model.ChecklistSettings
import org.thelazybattley.macrotrack.domain.model.ChecklistStatus
import org.thelazybattley.macrotrack.domain.model.ChecklistTime
import org.thelazybattley.macrotrack.features.checklist.CHECKLIST_HISTORY_PERIODS
import org.thelazybattley.macrotrack.features.checklist.ChecklistHistoryCallbacks
import org.thelazybattley.macrotrack.features.checklist.ChecklistHistoryViewModel
import org.thelazybattley.macrotrack.features.checklist.ChecklistHistoryViewState
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

private const val BAR_MAX_HEIGHT = 96

@Composable
fun ChecklistHistoryScreen(modifier: Modifier = Modifier, onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val viewModel = koinViewModel<ChecklistHistoryViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    ChecklistHistoryScreen(modifier = modifier, viewState = viewState, callbacks = viewModel, onBack = onBack, onNavigate = onNavigate)
}

@Composable
private fun ChecklistHistoryScreen(
    modifier: Modifier = Modifier,
    viewState: ChecklistHistoryViewState,
    callbacks: ChecklistHistoryCallbacks,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            ChecklistHeader(
                title = stringResource(resource = Res.string.checklist_history_title),
                subtitle = pluralStringResource(resource = Res.plurals.checklist_last_days, quantity = viewState.days, viewState.days),
                onBack = onBack
            )
        }
    ) { innerPadding ->
        val checklist = viewState.checklist ?: return@Scaffold
        val from = checklist.today.minus(period = DatePeriod(days = viewState.days - 1))
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            val periodLabel = stringResource(resource = Res.string.checklist_period_aria)
            ChecklistSegmented(
                modifier = Modifier.semantics { contentDescription = periodLabel },
                labels = CHECKLIST_HISTORY_PERIODS.map { pluralStringResource(resource = Res.plurals.checklist_period, quantity = it, it) },
                selectedIndex = CHECKLIST_HISTORY_PERIODS.indexOf(viewState.days),
                onSelect = { callbacks.onPeriod(days = CHECKLIST_HISTORY_PERIODS[it]) }
            )
            DoneEachDay(totals = checklist.dayTotals(days = viewState.days), today = checklist.today)
            Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                ChecklistLabel(text = stringResource(resource = Res.string.checklist_by_item))
                val rows = checklist.items
                    .map { it to checklist.completionOf(item = it, from = from) }
                    .filter { (item, completion) -> !item.isPaused || completion.counted > 0 }
                    .sortedByDescending { (_, completion) -> if (completion.counted == 0) -1f else completion.share }
                ChecklistCard {
                    rows.forEachIndexed { index, (item, completion) ->
                        if (index > 0) ChecklistDivider()
                        ItemShare(
                            item = item,
                            share = completion.share,
                            hasDays = completion.counted > 0,
                            sub = when {
                                completion.counted == 0 -> stringResource(resource = Res.string.checklist_nothing_due_yet)
                                completion.isMostlyWeekends -> stringResource(resource = Res.string.checklist_mostly_weekends)
                                completion.missed == 0 -> stringResource(resource = Res.string.checklist_never_missed)
                                else -> pluralStringResource(resource = Res.plurals.checklist_missed_days, quantity = completion.missed, completion.missed)
                            },
                            onClick = { onNavigate(AppDestinations.Root.ChecklistItem.createRoute(itemId = item.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DoneEachDay(totals: List<ChecklistDayTotal>, today: LocalDate) {
    val counted = totals.filter { it.due > 0 }
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
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = stringResource(resource = Res.string.checklist_done_each_day), style = typography.bold14, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            if (counted.isNotEmpty()) {
                Text(
                    text = stringResource(
                        resource = Res.string.checklist_average,
                        oneDecimal(value = counted.sumOf { it.done }.toDouble() / counted.size),
                        oneDecimal(value = counted.sumOf { it.due }.toDouble() / counted.size)
                    ),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
        val gap = if (totals.size > 14) 2.dp else 5.dp
        Row(modifier = Modifier.fillMaxWidth().height(height = (BAR_MAX_HEIGHT + 18).dp), horizontalArrangement = Arrangement.spacedBy(space = gap)) {
            totals.forEachIndexed { index, total ->
                val share = if (total.due == 0) 0f else total.done.toFloat() / total.due
                val description = stringResource(resource = Res.string.checklist_history_bar_aria, total.date.toShortDayMonthDay(), total.done, total.due)
                val isToday = total.date == today
                Column(
                    modifier = Modifier.weight(weight = 1f).fillMaxHeight().clearAndSetSemantics { contentDescription = description },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(space = 4.dp, alignment = Alignment.Bottom)
                ) {
                    val barShape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height = (BAR_MAX_HEIGHT * share).roundToInt().coerceAtLeast(minimumValue = 4).dp)
                            .clip(shape = barShape)
                            .background(
                                color = when {
                                    isToday -> colors.accentSoft
                                    total.due > 0 && total.done >= total.due -> colors.accent
                                    total.done == 0 -> colors.divider
                                    else -> colors.accent.copy(alpha = 0.4f + 0.5f * share)
                                }
                            )
                            .then(if (isToday) Modifier.border(width = 1.5.dp, color = colors.accent, shape = barShape) else Modifier)
                    )
                    val label = when {
                        totals.size <= 14 -> total.date.dayOfWeek.name.take(n = 1)
                        index % 5 == 0 || index == totals.lastIndex -> total.date.day.toString()
                        else -> ""
                    }
                    Text(text = label, style = typography.regular10, color = colors.muted, maxLines = 1, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

private fun oneDecimal(value: Double): String {
    val tenths = (value * 10).roundToInt()
    return if (tenths % 10 == 0) (tenths / 10).toString() else "${tenths / 10}.${tenths % 10}"
}

@Composable
private fun ItemShare(item: ChecklistItem, share: Float, hasDays: Boolean, sub: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 66.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        ChecklistItemIcon(icon = item.icon, isFaded = item.isPaused)
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 5.dp)) {
            Row {
                Text(text = item.name, style = typography.bold14, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(weight = 1f))
                if (hasDays) Text(text = stringResource(resource = Res.string.checklist_percent, (share * 100).roundToInt()), style = typography.bold14, color = colors.ink)
            }
            Box(modifier = Modifier.fillMaxWidth().height(height = 6.dp).clip(shape = RoundedCornerShape(size = 3.dp)).background(color = colors.divider)) {
                if (hasDays && share > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = share)
                            .fillMaxHeight()
                            .clip(shape = RoundedCornerShape(size = 3.dp))
                            .background(
                                color = when {
                                    share >= 0.8f -> colors.accent
                                    share >= 0.6f -> colors.carbs
                                    else -> colors.fat
                                }
                            )
                    )
                }
            }
            Text(text = sub, style = typography.regular11, color = colors.muted)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewChecklistHistoryScreen() {
    val today = LocalDate(year = 2026, month = 10, day = 8)
    val start = LocalDate(year = 2026, month = 9, day = 1)
    MacroTrackTheme {
        ChecklistHistoryScreen(
            viewState = ChecklistHistoryViewState(
                checklist = Checklist(
                    today = today,
                    items = listOf(
                        ChecklistItem(id = 1, kind = ChecklistKind.SUPPLEMENT, name = "Creatine", icon = ChecklistIcon.PILL, time = ChecklistTime.MORNING, createdOn = start)
                    ),
                    entries = (0 until 10).map { ChecklistEntry(itemId = 1, date = today.minus(period = DatePeriod(days = it * 2)), status = ChecklistStatus.DONE) },
                    settings = ChecklistSettings()
                )
            ),
            callbacks = ChecklistHistoryCallbacks.default(),
            onBack = {},
            onNavigate = {}
        )
    }
}
