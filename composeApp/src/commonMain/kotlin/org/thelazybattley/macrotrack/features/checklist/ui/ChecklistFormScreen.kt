package org.thelazybattley.macrotrack.features.checklist.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DayOfWeek
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.checklist_add_cta
import macrotrack.composeapp.generated.resources.checklist_add_cta_empty
import macrotrack.composeapp.generated.resources.checklist_add_habit
import macrotrack.composeapp.generated.resources.checklist_add_supplement
import macrotrack.composeapp.generated.resources.checklist_amount
import macrotrack.composeapp.generated.resources.checklist_amount_aria
import macrotrack.composeapp.generated.resources.checklist_choose_icon
import macrotrack.composeapp.generated.resources.checklist_count_stock
import macrotrack.composeapp.generated.resources.checklist_count_stock_off
import macrotrack.composeapp.generated.resources.checklist_count_stock_on
import macrotrack.composeapp.generated.resources.checklist_day_off
import macrotrack.composeapp.generated.resources.checklist_day_on
import macrotrack.composeapp.generated.resources.checklist_days
import macrotrack.composeapp.generated.resources.checklist_days_custom
import macrotrack.composeapp.generated.resources.checklist_days_every
import macrotrack.composeapp.generated.resources.checklist_days_of_week
import macrotrack.composeapp.generated.resources.checklist_days_weekdays
import macrotrack.composeapp.generated.resources.checklist_delete
import macrotrack.composeapp.generated.resources.checklist_delete_body
import macrotrack.composeapp.generated.resources.checklist_delete_title
import macrotrack.composeapp.generated.resources.checklist_done_when
import macrotrack.composeapp.generated.resources.checklist_each_time
import macrotrack.composeapp.generated.resources.checklist_edit_title
import macrotrack.composeapp.generated.resources.checklist_form_sub_edit
import macrotrack.composeapp.generated.resources.checklist_form_sub_new
import macrotrack.composeapp.generated.resources.checklist_goal_minutes
import macrotrack.composeapp.generated.resources.checklist_goal_minutes_body
import macrotrack.composeapp.generated.resources.checklist_goal_tick
import macrotrack.composeapp.generated.resources.checklist_goal_tick_body
import macrotrack.composeapp.generated.resources.checklist_goal_times
import macrotrack.composeapp.generated.resources.checklist_goal_times_body
import macrotrack.composeapp.generated.resources.checklist_habit
import macrotrack.composeapp.generated.resources.checklist_left
import macrotrack.composeapp.generated.resources.checklist_minutes_short
import macrotrack.composeapp.generated.resources.checklist_name
import macrotrack.composeapp.generated.resources.checklist_name_hint_habit
import macrotrack.composeapp.generated.resources.checklist_name_hint_supplement
import macrotrack.composeapp.generated.resources.checklist_remind
import macrotrack.composeapp.generated.resources.checklist_remind_body
import macrotrack.composeapp.generated.resources.checklist_reminder_time
import macrotrack.composeapp.generated.resources.checklist_save
import macrotrack.composeapp.generated.resources.checklist_stock_field
import macrotrack.composeapp.generated.resources.checklist_supplement
import macrotrack.composeapp.generated.resources.checklist_times
import macrotrack.composeapp.generated.resources.checklist_type
import macrotrack.composeapp.generated.resources.checklist_unit
import macrotrack.composeapp.generated.resources.checklist_value_aria
import macrotrack.composeapp.generated.resources.checklist_warn_field
import macrotrack.composeapp.generated.resources.checklist_when
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.KeypadField
import org.thelazybattley.macrotrack.domain.model.CHECKLIST_AMOUNT_MAX
import org.thelazybattley.macrotrack.domain.model.CHECKLIST_GOAL_MAX
import org.thelazybattley.macrotrack.domain.model.CHECKLIST_STOCK_MAX
import org.thelazybattley.macrotrack.domain.model.ChecklistDays
import org.thelazybattley.macrotrack.domain.model.ChecklistIcon
import org.thelazybattley.macrotrack.domain.model.ChecklistKind
import org.thelazybattley.macrotrack.domain.model.ChecklistTime
import org.thelazybattley.macrotrack.domain.model.HabitGoal
import org.thelazybattley.macrotrack.domain.model.SupplementUnit
import org.thelazybattley.macrotrack.domain.usecase.checklist.formatChecklistAmount
import org.thelazybattley.macrotrack.domain.usecase.checklist.label
import org.thelazybattley.macrotrack.features.checklist.ChecklistDraft
import org.thelazybattley.macrotrack.features.checklist.ChecklistField
import org.thelazybattley.macrotrack.features.checklist.ChecklistFormCallbacks
import org.thelazybattley.macrotrack.features.checklist.ChecklistFormViewModel
import org.thelazybattley.macrotrack.features.checklist.ChecklistFormViewState
import org.thelazybattley.macrotrack.features.water.ui.clockText
import org.thelazybattley.macrotrack.ui.common.CommonKeypadSheet
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ChecklistFormScreen(modifier: Modifier = Modifier, onClose: () -> Unit) {
    val viewModel = koinViewModel<ChecklistFormViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.isClosed) {
        if (viewState.isClosed) onClose()
    }
    ChecklistFormScreen(modifier = modifier, viewState = viewState, callbacks = viewModel, onClose = onClose)
}

@Composable
private fun ChecklistFormScreen(
    modifier: Modifier = Modifier,
    viewState: ChecklistFormViewState,
    callbacks: ChecklistFormCallbacks,
    onClose: () -> Unit
) {
    val draft = viewState.draft
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            ChecklistHeader(
                title = when {
                    draft == null -> ""
                    viewState.isEditing -> stringResource(resource = Res.string.checklist_edit_title, draft.original?.name ?: draft.name)
                    draft.kind == ChecklistKind.SUPPLEMENT -> stringResource(resource = Res.string.checklist_add_supplement)
                    else -> stringResource(resource = Res.string.checklist_add_habit)
                },
                subtitle = stringResource(resource = if (viewState.isEditing) Res.string.checklist_form_sub_edit else Res.string.checklist_form_sub_new),
                onBack = onClose,
                isClose = true
            )
        },
        bottomBar = {
            if (draft != null) {
                Column(
                    modifier = Modifier
                        .background(color = colors.background)
                        .navigationBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
                ) {
                    ChecklistPrimaryButton(
                        text = when {
                            viewState.isEditing -> stringResource(resource = Res.string.checklist_save)
                            draft.name.isBlank() -> stringResource(resource = Res.string.checklist_add_cta_empty)
                            else -> stringResource(resource = Res.string.checklist_add_cta, draft.name.trim())
                        },
                        isEnabled = draft.canSave,
                        onClick = callbacks::onSave
                    )
                }
            }
        }
    ) { innerPadding ->
        if (draft == null) return@Scaffold
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            if (!viewState.isEditing) {
                val kinds = listOf(ChecklistKind.SUPPLEMENT, ChecklistKind.HABIT)
                val typeLabel = stringResource(resource = Res.string.checklist_type)
                ChecklistSegmented(
                    modifier = Modifier.semantics { contentDescription = typeLabel },
                    labels = listOf(stringResource(resource = Res.string.checklist_supplement), stringResource(resource = Res.string.checklist_habit)),
                    selectedIndex = kinds.indexOf(draft.kind),
                    onSelect = { callbacks.onKind(kind = kinds[it]) }
                )
            }
            NameRow(draft = draft, callbacks = callbacks)
            if (draft.kind == ChecklistKind.SUPPLEMENT) DoseSection(draft = draft, callbacks = callbacks) else GoalSection(draft = draft, callbacks = callbacks)
            WhenSection(draft = draft, callbacks = callbacks)
            DaysSection(draft = draft, callbacks = callbacks)
            ChecklistCard {
                ChecklistSwitchRow(
                    title = stringResource(resource = Res.string.checklist_remind),
                    body = stringResource(resource = Res.string.checklist_remind_body),
                    isOn = draft.isReminderOn,
                    onToggle = callbacks::onReminderToggled,
                    value = clockText(minutes = draft.reminderMinutes),
                    valueDescription = stringResource(
                        resource = Res.string.checklist_value_aria,
                        stringResource(resource = Res.string.checklist_reminder_time),
                        clockText(minutes = draft.reminderMinutes)
                    ),
                    onValue = { callbacks.onPickTime(isOpen = true) }
                )
                if (draft.kind == ChecklistKind.SUPPLEMENT) {
                    ChecklistDivider()
                    val stockItem = draft.toItem().copy(stock = draft.stock)
                    val left = stringResource(resource = Res.string.checklist_left, checklistStock(item = stockItem, count = draft.stock))
                    ChecklistSwitchRow(
                        title = stringResource(resource = Res.string.checklist_count_stock),
                        body = if (draft.isStockOn) {
                            stringResource(resource = Res.string.checklist_count_stock_on, checklistStock(item = stockItem, count = draft.stock), draft.stockWarnAt)
                        } else {
                            stringResource(resource = Res.string.checklist_count_stock_off)
                        },
                        isOn = draft.isStockOn,
                        onToggle = callbacks::onStockToggled,
                        value = left,
                        valueDescription = stringResource(resource = Res.string.checklist_value_aria, stringResource(resource = Res.string.checklist_stock_field), left),
                        onValue = { callbacks.onOpenField(field = ChecklistField.STOCK) }
                    )
                }
            }
            if (viewState.isEditing) {
                Text(
                    text = stringResource(resource = Res.string.checklist_delete),
                    style = typography.bold14,
                    color = colors.danger,
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally)
                        .clip(shape = RoundedCornerShape(size = 10.dp))
                        .clickable(role = Role.Button) { callbacks.onConfirmDelete(isOpen = true) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
    if (draft != null) {
        viewState.openField?.let { field -> FieldKeypad(draft = draft, field = field, callbacks = callbacks) }
        if (viewState.isPickingTime) {
            ChecklistTimeDialog(
                minutes = draft.reminderMinutes,
                onDismiss = { callbacks.onPickTime(isOpen = false) },
                onPicked = callbacks::onReminderTime
            )
        }
        if (viewState.isPickingIcon) IconSheet(selected = draft.icon, onPick = callbacks::onIcon, onDismiss = { callbacks.onPickIcon(isOpen = false) })
        if (viewState.isConfirmingDelete) {
            AlertDialog(
                onDismissRequest = { callbacks.onConfirmDelete(isOpen = false) },
                containerColor = colors.surface,
                title = { Text(text = stringResource(resource = Res.string.checklist_delete_title, draft.original?.name ?: draft.name), style = typography.bold18, color = colors.ink) },
                text = { Text(text = stringResource(resource = Res.string.checklist_delete_body), style = typography.regular13, color = colors.muted) },
                confirmButton = {
                    TextButton(onClick = callbacks::onDelete) {
                        Text(text = stringResource(resource = Res.string.checklist_delete), style = typography.bold14, color = colors.danger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { callbacks.onConfirmDelete(isOpen = false) }) {
                        Text(text = stringResource(resource = Res.string.cancel), style = typography.bold14, color = colors.ink)
                    }
                }
            )
        }
    }
}

@Composable
private fun NameRow(draft: ChecklistDraft, callbacks: ChecklistFormCallbacks) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
        val chooseIcon = stringResource(resource = Res.string.checklist_choose_icon)
        ChecklistItemIcon(
            icon = draft.icon,
            size = 50.dp,
            modifier = Modifier
                .clip(shape = RoundedCornerShape(size = 14.dp))
                .clickable(role = Role.Button) { callbacks.onPickIcon(isOpen = true) }
                .semantics { contentDescription = chooseIcon }
        )
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            val label = stringResource(resource = Res.string.checklist_name)
            ChecklistLabel(text = label)
            val shape = RoundedCornerShape(size = 14.dp)
            BasicTextField(
                value = draft.name,
                onValueChange = callbacks::onName,
                singleLine = true,
                textStyle = typography.regular18.copy(color = colors.ink),
                cursorBrush = SolidColor(value = colors.accent),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 50.dp)
                    .semantics { contentDescription = label },
                decorationBox = { field ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shape = shape)
                            .background(color = colors.surface)
                            .border(width = 1.dp, color = colors.line, shape = shape)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (draft.name.isEmpty()) {
                            Text(
                                text = stringResource(
                                    resource = if (draft.kind == ChecklistKind.SUPPLEMENT) Res.string.checklist_name_hint_supplement else Res.string.checklist_name_hint_habit
                                ),
                                style = typography.regular18,
                                color = colors.faint
                            )
                        }
                        field()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DoseSection(draft: ChecklistDraft, callbacks: ChecklistFormCallbacks) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        ChecklistLabel(text = stringResource(resource = Res.string.checklist_each_time))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            val amount = formatChecklistAmount(amount = draft.amount)
            val description = stringResource(resource = Res.string.checklist_amount_aria, amount)
            val shape = RoundedCornerShape(size = 14.dp)
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 50.dp)
                    .clip(shape = shape)
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = shape)
                    .clickable(role = Role.Button) { callbacks.onOpenField(field = ChecklistField.AMOUNT) }
                    .clearAndSetSemantics { contentDescription = description },
                contentAlignment = Alignment.Center
            ) {
                Text(text = amount, style = typography.bold18, color = colors.ink)
            }
            val unitLabel = stringResource(resource = Res.string.checklist_unit)
            FlowRow(
                modifier = Modifier.weight(weight = 1f).selectableGroup().semantics { contentDescription = unitLabel },
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
                verticalArrangement = Arrangement.spacedBy(space = 6.dp)
            ) {
                SupplementUnit.entries.forEach { unit ->
                    ChecklistChip(label = stringResource(resource = unit.label), isSelected = draft.unit == unit, onClick = { callbacks.onUnit(unit = unit) }, height = 32.dp)
                }
            }
        }
    }
}

@Composable
private fun GoalSection(draft: ChecklistDraft, callbacks: ChecklistFormCallbacks) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        ChecklistLabel(text = stringResource(resource = Res.string.checklist_done_when))
        Column(modifier = Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            listOf(
                Triple(HabitGoal.TICK, Res.string.checklist_goal_tick, Res.string.checklist_goal_tick_body),
                Triple(HabitGoal.MINUTES, Res.string.checklist_goal_minutes, Res.string.checklist_goal_minutes_body),
                Triple(HabitGoal.TIMES, Res.string.checklist_goal_times, Res.string.checklist_goal_times_body)
            ).forEach { (goal, title, body) ->
                val isOn = draft.goal == goal
                val shape = RoundedCornerShape(size = 14.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = shape)
                        .background(color = if (isOn) colors.accentSoft else colors.surface)
                        .border(width = if (isOn) 1.5.dp else 1.dp, color = if (isOn) colors.accent else colors.line, shape = shape)
                        .selectable(selected = isOn, role = Role.RadioButton) { callbacks.onGoal(goal = goal) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(size = 20.dp)
                            .clip(shape = CircleShape)
                            .background(color = colors.surface)
                            .border(width = if (isOn) 6.dp else 2.dp, color = if (isOn) colors.accent else colors.dashed, shape = CircleShape)
                    )
                    Column(modifier = Modifier.weight(weight = 1f)) {
                        Text(text = stringResource(resource = title), style = typography.bold14, color = colors.ink)
                        Text(text = stringResource(resource = body), style = typography.regular11, color = colors.muted)
                    }
                    if (isOn && goal != HabitGoal.TICK) {
                        val value = if (goal == HabitGoal.MINUTES) {
                            stringResource(resource = Res.string.checklist_minutes_short, draft.minutes)
                        } else {
                            pluralStringResource(resource = Res.plurals.checklist_times, quantity = draft.times, draft.times)
                        }
                        ChecklistValueButton(
                            text = value,
                            description = stringResource(resource = Res.string.checklist_value_aria, stringResource(resource = title), value),
                            onClick = { callbacks.onOpenField(field = if (goal == HabitGoal.MINUTES) ChecklistField.MINUTES else ChecklistField.TIMES) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WhenSection(draft: ChecklistDraft, callbacks: ChecklistFormCallbacks) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        val label = stringResource(resource = Res.string.checklist_when)
        ChecklistLabel(text = label)
        FlowRow(
            modifier = Modifier.selectableGroup().semantics { contentDescription = label },
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
            verticalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            ChecklistTime.entries.forEach { time ->
                ChecklistChip(label = stringResource(resource = time.label), isSelected = draft.time == time, onClick = { callbacks.onTime(time = time) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DaysSection(draft: ChecklistDraft, callbacks: ChecklistFormCallbacks) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        val label = stringResource(resource = Res.string.checklist_days)
        ChecklistLabel(text = label)
        FlowRow(
            modifier = Modifier.selectableGroup().semantics { contentDescription = label },
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
            verticalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            listOf(
                ChecklistDays.EVERY_DAY to Res.string.checklist_days_every,
                ChecklistDays.WEEKDAYS to Res.string.checklist_days_weekdays,
                ChecklistDays.CUSTOM to Res.string.checklist_days_custom
            ).forEach { (choice, text) ->
                ChecklistChip(label = stringResource(resource = text), isSelected = draft.dayChoice == choice, onClick = { callbacks.onDayChoice(choice = choice) })
            }
        }
        val weekLabel = stringResource(resource = Res.string.checklist_days_of_week)
        Row(modifier = Modifier.semantics { contentDescription = weekLabel }, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            DayOfWeek.entries.forEach { day ->
                val isOn = day in draft.days
                val name = day.name.lowercase().replaceFirstChar { it.uppercase() }
                val description = stringResource(resource = if (isOn) Res.string.checklist_day_on else Res.string.checklist_day_off, name)
                val shape = RoundedCornerShape(size = 19.dp)
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 38.dp)
                        .clip(shape = shape)
                        .background(color = if (isOn) colors.accent else colors.surface)
                        .border(width = 1.dp, color = if (isOn) colors.accent else colors.line, shape = shape)
                        .toggleable(value = isOn, role = Role.Checkbox) { callbacks.onDayToggled(day = day) }
                        .clearAndSetSemantics { contentDescription = description },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = name.take(n = 1), style = typography.bold13, color = if (isOn) colors.onAccent else colors.muted)
                }
            }
        }
    }
}

@Composable
private fun FieldKeypad(draft: ChecklistDraft, field: ChecklistField, callbacks: ChecklistFormCallbacks) {
    val stockItem = draft.toItem()
    val keypadField = when (field) {
        ChecklistField.AMOUNT -> KeypadField(
            id = field.name,
            label = stringResource(resource = Res.string.checklist_amount),
            unit = stringResource(resource = draft.unit.label),
            value = formatChecklistAmount(amount = draft.amount).replace(",", ""),
            wholeDigits = 6,
            decimals = 2,
            max = CHECKLIST_AMOUNT_MAX
        )
        ChecklistField.MINUTES -> KeypadField(
            id = field.name,
            label = stringResource(resource = Res.string.checklist_goal_minutes),
            value = draft.minutes.toString(),
            wholeDigits = 3,
            max = CHECKLIST_GOAL_MAX.toDouble()
        )
        ChecklistField.TIMES -> KeypadField(
            id = field.name,
            label = stringResource(resource = Res.string.checklist_goal_times),
            value = draft.times.toString(),
            wholeDigits = 3,
            max = CHECKLIST_GOAL_MAX.toDouble()
        )
        ChecklistField.STOCK, ChecklistField.WARN_AT -> null
    }
    val fields = keypadField?.let { listOf(it) } ?: listOf(
        KeypadField(
            id = ChecklistField.STOCK.name,
            label = stringResource(resource = Res.string.checklist_stock_field),
            shortLabel = stringResource(resource = Res.string.checklist_stock_field),
            unit = checklistStock(item = stockItem, count = draft.stock).substringAfter(delimiter = " "),
            value = draft.stock.toString(),
            wholeDigits = 4,
            allowsZero = true,
            max = CHECKLIST_STOCK_MAX.toDouble()
        ),
        KeypadField(
            id = ChecklistField.WARN_AT.name,
            label = stringResource(resource = Res.string.checklist_warn_field),
            shortLabel = stringResource(resource = Res.string.checklist_warn_field),
            value = draft.stockWarnAt.toString(),
            wholeDigits = 3,
            allowsZero = true,
            max = 999.0
        )
    )
    CommonKeypadSheet(
        fields = fields,
        openId = field.name,
        onOpen = { id -> ChecklistField.entries.find { it.name == id }?.let { callbacks.onOpenField(field = it) } },
        onCommit = { id, text ->
            val committed = ChecklistField.entries.find { it.name == id }
            val value = text.removeSuffix(suffix = ".").toDoubleOrNull()
            if (committed != null && value != null) callbacks.onFieldValue(field = committed, value = value)
        },
        onDismiss = { callbacks.onOpenField(field = null) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun IconSheet(selected: ChecklistIcon, onPick: (ChecklistIcon) -> Unit, onDismiss: () -> Unit) {
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
            ChecklistSheetTitle(title = stringResource(resource = Res.string.checklist_choose_icon), subtitle = null)
            FlowRow(
                modifier = Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
                verticalArrangement = Arrangement.spacedBy(space = 10.dp),
                maxItemsInEachRow = 4
            ) {
                ChecklistIcon.entries.forEach { icon ->
                    val isOn = icon == selected
                    Box(
                        modifier = Modifier
                            .size(size = 64.dp)
                            .clip(shape = RoundedCornerShape(size = 16.dp))
                            .border(width = if (isOn) 2.dp else 1.dp, color = if (isOn) colors.accent else colors.line, shape = RoundedCornerShape(size = 16.dp))
                            .selectable(selected = isOn, role = Role.RadioButton) { onPick(icon) }
                            .semantics { contentDescription = icon.name.lowercase() },
                        contentAlignment = Alignment.Center
                    ) {
                        ChecklistItemIcon(icon = icon, size = 44.dp)
                    }
                }
            }
            Box(modifier = Modifier.width(width = 1.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewChecklistFormScreen() {
    MacroTrackTheme {
        ChecklistFormScreen(
            viewState = ChecklistFormViewState(draft = ChecklistDraft(name = "Magnesium", amount = 200.0, unit = SupplementUnit.MG, time = ChecklistTime.BEDTIME)),
            callbacks = ChecklistFormCallbacks.default(),
            onClose = {}
        )
    }
}
