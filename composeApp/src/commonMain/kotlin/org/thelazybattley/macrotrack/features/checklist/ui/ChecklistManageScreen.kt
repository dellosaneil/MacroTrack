package org.thelazybattley.macrotrack.features.checklist.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.checklist_day_start
import macrotrack.composeapp.generated.resources.checklist_day_start_body
import macrotrack.composeapp.generated.resources.checklist_drag
import macrotrack.composeapp.generated.resources.checklist_group_habits
import macrotrack.composeapp.generated.resources.checklist_group_paused
import macrotrack.composeapp.generated.resources.checklist_group_supplements
import macrotrack.composeapp.generated.resources.checklist_items
import macrotrack.composeapp.generated.resources.checklist_items_paused
import macrotrack.composeapp.generated.resources.checklist_left
import macrotrack.composeapp.generated.resources.checklist_manage_empty
import macrotrack.composeapp.generated.resources.checklist_manage_intro
import macrotrack.composeapp.generated.resources.checklist_manage_title
import macrotrack.composeapp.generated.resources.checklist_move_down
import macrotrack.composeapp.generated.resources.checklist_move_up
import macrotrack.composeapp.generated.resources.checklist_pause
import macrotrack.composeapp.generated.resources.checklist_paused_since
import macrotrack.composeapp.generated.resources.checklist_resume
import macrotrack.composeapp.generated.resources.checklist_show_on_today
import macrotrack.composeapp.generated.resources.checklist_show_on_today_body
import macrotrack.composeapp.generated.resources.checklist_value_aria
import macrotrack.composeapp.generated.resources.ic_drag
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.Checklist
import org.thelazybattley.macrotrack.domain.model.ChecklistIcon
import org.thelazybattley.macrotrack.domain.model.ChecklistItem
import org.thelazybattley.macrotrack.domain.model.ChecklistKind
import org.thelazybattley.macrotrack.domain.model.ChecklistSettings
import org.thelazybattley.macrotrack.domain.model.ChecklistTime
import org.thelazybattley.macrotrack.domain.usecase.checklist.phrase
import org.thelazybattley.macrotrack.features.checklist.ChecklistManageCallbacks
import org.thelazybattley.macrotrack.features.checklist.ChecklistManageViewModel
import org.thelazybattley.macrotrack.features.checklist.ChecklistManageViewState
import org.thelazybattley.macrotrack.features.water.ui.clockText
import org.thelazybattley.macrotrack.ui.common.CommonSwitch
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ChecklistManageScreen(modifier: Modifier = Modifier, onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val viewModel = koinViewModel<ChecklistManageViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    ChecklistManageScreen(modifier = modifier, viewState = viewState, callbacks = viewModel, onBack = onBack, onNavigate = onNavigate)
}

@Composable
private fun ChecklistManageScreen(
    modifier: Modifier = Modifier,
    viewState: ChecklistManageViewState,
    callbacks: ChecklistManageCallbacks,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val checklist = viewState.checklist
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            val count = checklist?.items?.size ?: 0
            val paused = checklist?.items?.count { it.isPaused } ?: 0
            val items = pluralStringResource(resource = Res.plurals.checklist_items, quantity = count, count)
            ChecklistHeader(
                title = stringResource(resource = Res.string.checklist_manage_title),
                subtitle = if (paused > 0) stringResource(resource = Res.string.checklist_items_paused, items, paused) else items,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        if (checklist == null) return@Scaffold
        val ordered = checklist.items.sortedWith(compareBy<ChecklistItem> { it.sortOrder }.thenBy { it.id })
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            Text(text = stringResource(resource = Res.string.checklist_manage_intro), style = typography.regular13, color = colors.muted)
            if (ordered.isEmpty()) {
                Text(text = stringResource(resource = Res.string.checklist_manage_empty), style = typography.bold14, color = colors.muted)
            }
            listOf(
                Res.string.checklist_group_supplements to ordered.filter { !it.isPaused && it.kind == ChecklistKind.SUPPLEMENT },
                Res.string.checklist_group_habits to ordered.filter { !it.isPaused && it.kind == ChecklistKind.HABIT },
                Res.string.checklist_group_paused to ordered.filter { it.isPaused }
            ).forEach { (name, items) ->
                if (items.isNotEmpty()) {
                    Group(
                        name = name,
                        items = items,
                        onReorder = callbacks::onReorder,
                        onPausedToggled = callbacks::onPausedToggled,
                        onOpen = { onNavigate(AppDestinations.Root.ChecklistForm.createRoute(itemId = it.id)) }
                    )
                }
            }
            ChecklistCard {
                ChecklistSwitchRow(
                    title = stringResource(resource = Res.string.checklist_show_on_today),
                    body = stringResource(resource = Res.string.checklist_show_on_today_body),
                    isOn = checklist.settings.isShownOnToday,
                    onToggle = callbacks::onShowOnTodayToggled
                )
                ChecklistDivider()
                val title = stringResource(resource = Res.string.checklist_day_start)
                val time = clockText(minutes = checklist.settings.dayStartMinutes)
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
                        Text(text = title, style = typography.bold14, color = colors.ink)
                        Text(text = stringResource(resource = Res.string.checklist_day_start_body), style = typography.regular11, color = colors.muted)
                    }
                    ChecklistValueButton(
                        text = time,
                        description = stringResource(resource = Res.string.checklist_value_aria, title, time),
                        onClick = { callbacks.onPickDayStart(isOpen = true) }
                    )
                }
            }
        }
    }
    if (viewState.isPickingDayStart && checklist != null) {
        DayStartSheet(selected = checklist.settings.dayStartMinutes, onPick = callbacks::onDayStart, onDismiss = { callbacks.onPickDayStart(isOpen = false) })
    }
}

/**
 * A group's rows, reordered by dragging the handle; each row also offers Move up and Move down to screen readers.
 * The new order is saved when the drag ends.
 */
@Composable
private fun Group(
    name: StringResource,
    items: List<ChecklistItem>,
    onReorder: (List<ChecklistItem>) -> Unit,
    onPausedToggled: (ChecklistItem) -> Unit,
    onOpen: (ChecklistItem) -> Unit
) {
    var order by remember(items) { mutableStateOf(value = items) }
    var draggingId by remember { mutableStateOf<Long?>(value = null) }
    var dragOffset by remember { mutableFloatStateOf(value = 0f) }
    val heights = remember { mutableStateMapOf<Long, Int>() }
    val latestOrder by rememberUpdatedState(newValue = order)

    fun move(from: Int, to: Int) {
        if (to !in order.indices) return
        order = order.toMutableList().apply { add(index = to, element = removeAt(index = from)) }
    }

    fun drag(id: Long, delta: Float) {
        dragOffset += delta
        val index = latestOrder.indexOfFirst { it.id == id }
        val next = latestOrder.getOrNull(index = index + 1)
        val previous = latestOrder.getOrNull(index = index - 1)
        if (next != null && dragOffset > (heights[next.id] ?: 0) / 2f) {
            dragOffset -= heights[next.id] ?: 0
            move(from = index, to = index + 1)
        } else if (previous != null && dragOffset < -(heights[previous.id] ?: 0) / 2f) {
            dragOffset += heights[previous.id] ?: 0
            move(from = index, to = index - 1)
        }
    }

    fun endDrag() {
        draggingId = null
        dragOffset = 0f
        if (latestOrder.map { it.id } != items.map { it.id }) onReorder(latestOrder)
    }

    Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        ChecklistLabel(text = stringResource(resource = name))
        ChecklistCard {
            order.forEachIndexed { index, item ->
                if (index > 0) ChecklistDivider()
                val isDragging = draggingId == item.id
                val moveUp = stringResource(resource = Res.string.checklist_move_up)
                val moveDown = stringResource(resource = Res.string.checklist_move_down)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { heights[item.id] = it.height }
                        .zIndex(zIndex = if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            translationY = if (isDragging) dragOffset else 0f
                            shadowElevation = if (isDragging) 8.dp.toPx() else 0f
                        }
                        .background(color = colors.surface)
                        .heightIn(min = 60.dp)
                        .semantics {
                            customActions = buildList {
                                if (index > 0) add(CustomAccessibilityAction(label = moveUp) { move(from = index, to = index - 1); onReorder(order); true })
                                if (index < order.lastIndex) add(CustomAccessibilityAction(label = moveDown) { move(from = index, to = index + 1); onReorder(order); true })
                            }
                        }
                        .padding(start = 8.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
                ) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_drag),
                        contentDescription = stringResource(resource = Res.string.checklist_drag, item.name),
                        tint = colors.faint,
                        modifier = Modifier
                            .size(size = 32.dp)
                            .padding(all = 8.dp)
                            .pointerInput(item.id) {
                                detectVerticalDragGestures(
                                    onDragStart = { draggingId = item.id },
                                    onVerticalDrag = { change, amount ->
                                        change.consume()
                                        drag(id = item.id, delta = amount)
                                    },
                                    onDragEnd = ::endDrag,
                                    onDragCancel = ::endDrag
                                )
                            }
                    )
                    ChecklistItemIcon(icon = item.icon, size = 36.dp, isFaded = item.isPaused)
                    Column(
                        modifier = Modifier
                            .weight(weight = 1f)
                            .clip(shape = RoundedCornerShape(size = 8.dp))
                            .clickable(role = Role.Button) { onOpen(item) }
                    ) {
                        Text(text = item.name, style = typography.bold14, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = manageLine(item = item), style = typography.regular11, color = colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    CommonSwitch(
                        isOn = !item.isPaused,
                        description = stringResource(resource = if (item.isPaused) Res.string.checklist_resume else Res.string.checklist_pause, item.name),
                        onToggle = { onPausedToggled(item) }
                    )
                }
            }
        }
    }
}

/** "1,000 IU · in the morning · 41 left", or when it was paused. */
@Composable
private fun manageLine(item: ChecklistItem): String {
    item.pausedOn?.let { return stringResource(resource = Res.string.checklist_paused_since, it.toAbbreviatedMonthDay()) }
    return listOfNotNull(
        checklistDose(item = item).takeIf { it.isNotEmpty() },
        stringResource(resource = item.time.phrase),
        item.stock?.let { stringResource(resource = Res.string.checklist_left, checklistStock(item = item, count = it)) } ?: daysText(item = item)
    ).joinToString(separator = " · ")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun DayStartSheet(selected: Int, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            ChecklistSheetTitle(
                title = stringResource(resource = Res.string.checklist_day_start),
                subtitle = stringResource(resource = Res.string.checklist_day_start_body)
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(space = 8.dp), verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                ChecklistSettings.DAY_START_OPTIONS.forEach { minutes ->
                    ChecklistChip(label = clockText(minutes = minutes), isSelected = minutes == selected, onClick = { onPick(minutes) })
                }
            }
            Box(modifier = Modifier.size(size = 1.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewChecklistManageScreen() {
    val start = LocalDate(year = 2026, month = 9, day = 1)
    MacroTrackTheme {
        ChecklistManageScreen(
            viewState = ChecklistManageViewState(
                checklist = Checklist(
                    today = LocalDate(year = 2026, month = 10, day = 8),
                    items = listOf(
                        ChecklistItem(id = 1, kind = ChecklistKind.SUPPLEMENT, name = "Creatine", icon = ChecklistIcon.PILL, time = ChecklistTime.MORNING, createdOn = start),
                        ChecklistItem(id = 2, kind = ChecklistKind.HABIT, name = "Walk after lunch", icon = ChecklistIcon.RUN, time = ChecklistTime.LUNCH, sortOrder = 1, createdOn = start),
                        ChecklistItem(id = 3, kind = ChecklistKind.SUPPLEMENT, name = "Magnesium", icon = ChecklistIcon.LEAF, time = ChecklistTime.BEDTIME, sortOrder = 2, pausedOn = start, createdOn = start)
                    ),
                    entries = emptyList(),
                    settings = ChecklistSettings()
                )
            ),
            callbacks = ChecklistManageCallbacks.default(),
            onBack = {},
            onNavigate = {}
        )
    }
}
