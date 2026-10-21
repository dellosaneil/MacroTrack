package org.thelazybattley.macrotrack.features.copy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.copy_add_to_it
import macrotrack.composeapp.generated.resources.copy_clear_all
import macrotrack.composeapp.generated.resources.copy_cta
import macrotrack.composeapp.generated.resources.copy_day_conflict_many
import macrotrack.composeapp.generated.resources.copy_day_conflict_one
import macrotrack.composeapp.generated.resources.copy_day_title
import macrotrack.composeapp.generated.resources.copy_from_subtitle
import macrotrack.composeapp.generated.resources.copy_into_meal
import macrotrack.composeapp.generated.resources.copy_meal_conflict_many
import macrotrack.composeapp.generated.resources.copy_meal_conflict_one
import macrotrack.composeapp.generated.resources.copy_meal_title
import macrotrack.composeapp.generated.resources.copy_no_future
import macrotrack.composeapp.generated.resources.copy_nothing_logged_day
import macrotrack.composeapp.generated.resources.copy_pick_another_date
import macrotrack.composeapp.generated.resources.copy_pick_day
import macrotrack.composeapp.generated.resources.copy_pick_days
import macrotrack.composeapp.generated.resources.copy_pick_item
import macrotrack.composeapp.generated.resources.copy_replace_cta
import macrotrack.composeapp.generated.resources.copy_replace_it
import macrotrack.composeapp.generated.resources.copy_same_portions
import macrotrack.composeapp.generated.resources.copy_select_all
import macrotrack.composeapp.generated.resources.copy_summary
import macrotrack.composeapp.generated.resources.copy_target_days
import macrotrack.composeapp.generated.resources.copy_target_today
import macrotrack.composeapp.generated.resources.copy_to
import macrotrack.composeapp.generated.resources.copy_what
import macrotrack.composeapp.generated.resources.ic_calendar
import macrotrack.composeapp.generated.resources.items
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.today
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toDayMonthDay
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.core.toShortDayName
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.copy.CopyToCallbacks
import org.thelazybattley.macrotrack.features.copy.CopyToViewModel
import org.thelazybattley.macrotrack.features.copy.CopyToViewState
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.CommonDatePickerDialog
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.dashedBorder
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CopyToScreen(
    onClose: () -> Unit,
    onCopied: () -> Unit
) {
    val viewModel = koinViewModel<CopyToViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = viewState.isDone) {
        if (viewState.isDone) onCopied()
    }

    CopyToScreen(viewState = viewState, callbacks = viewModel, onClose = onClose)
}

@Composable
private fun CopyToScreen(
    viewState: CopyToViewState,
    callbacks: CopyToCallbacks,
    onClose: () -> Unit
) {
    var isPickingDate by rememberSaveable { mutableStateOf(value = false) }
    val itemsText = pluralStringResource(resource = Res.plurals.items, quantity = viewState.sourceLogs.size, viewState.sourceLogs.size)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CopyHeader(
                title = viewState.mealType?.let {
                    stringResource(resource = Res.string.copy_meal_title, stringResource(resource = it.title).lowercase())
                } ?: stringResource(resource = Res.string.copy_day_title),
                subtitle = stringResource(
                    resource = Res.string.copy_from_subtitle,
                    viewState.sourceDate.toDayMonthDay(),
                    stringResource(
                        resource = Res.string.copy_summary,
                        itemsText,
                        viewState.sourceLogs.sumOf { it.calories }.withThousands()
                    )
                ),
                onClose = onClose
            )
        },
        bottomBar = {
            CommonPrimaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                text = ctaText(viewState = viewState),
                enabled = viewState.canCopy,
                onClick = callbacks::onCopy
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(space = 18.dp)
        ) {
            WhatToCopy(viewState = viewState, callbacks = callbacks)
            DayStrip(viewState = viewState, onToggled = callbacks::onDateToggled, onPickDate = { isPickingDate = true })
            if (viewState.isMeal) {
                MealPicker(selected = viewState.targetMeal, onSelected = callbacks::onTargetMealSelected)
            }
            if (viewState.hasConflict) {
                ConflictCard(viewState = viewState, onReplaceSelected = callbacks::onReplaceSelected)
            }
            Text(
                text = stringResource(resource = Res.string.copy_same_portions),
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
    if (isPickingDate) {
        CommonDatePickerDialog(
            selected = viewState.today,
            onDismiss = { isPickingDate = false },
            onConfirm = {
                isPickingDate = false
                callbacks.onDatePicked(date = it)
            }
        )
    }
}

@Composable
private fun WhatToCopy(viewState: CopyToViewState, callbacks: CopyToCallbacks) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CopySectionLabel(modifier = Modifier.weight(weight = 1f), text = stringResource(resource = Res.string.copy_what))
            Text(
                text = stringResource(resource = if (viewState.unchecked.isEmpty()) Res.string.copy_clear_all else Res.string.copy_select_all),
                style = typography.bold12,
                color = colors.accentInk,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .clickable(role = Role.Button, onClick = callbacks::onAllToggled)
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            )
        }
        val shape = RoundedCornerShape(size = 20.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = shape)
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = shape)
        ) {
            viewState.sourceLogs.groupBy { it.mealType }.forEach { (mealType, logs) ->
                if (!viewState.isMeal) {
                    val picked = logs.filter { it.id !in viewState.unchecked }
                    MealGroupRow(
                        mealType = mealType,
                        state = when (picked.size) {
                            logs.size -> CheckState.ON
                            0 -> CheckState.OFF
                            else -> CheckState.PARTIAL
                        },
                        calories = picked.sumOf { it.calories },
                        onClick = { callbacks.onMealToggled(mealType = mealType) }
                    )
                    HorizontalDivider(color = colors.divider)
                }
                logs.forEach { log ->
                    LogRow(
                        log = log,
                        amount = log.amountText(foodUnit = viewState.foodUnit),
                        isPicked = log.id !in viewState.unchecked,
                        onClick = { callbacks.onLogToggled(id = log.id) }
                    )
                    HorizontalDivider(color = colors.divider)
                }
            }
        }
    }
}

@Composable
private fun MealGroupRow(mealType: MealType, state: CheckState, calories: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .background(color = colors.background)
            .clickable(role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        CopyCheckBox(state = state)
        Text(
            text = stringResource(resource = mealType.title),
            style = typography.bold14,
            color = colors.ink,
            modifier = Modifier.weight(weight = 1f)
        )
        Text(
            text = calories.withThousands() + " " + stringResource(resource = Res.string.kcal_text),
            style = typography.regular11,
            color = colors.muted
        )
    }
}

@Composable
private fun LogRow(log: FoodLog, amount: String, isPicked: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Checkbox, onClick = onClick)
            .semantics { selected = isPicked }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        CopyCheckBox(state = if (isPicked) CheckState.ON else CheckState.OFF)
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = log.foodName, style = typography.medium13, color = colors.ink, maxLines = 1)
            Text(text = amount, style = typography.regular11, color = colors.muted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 3.dp)) {
            Text(text = log.calories.withThousands(), style = typography.bold13, color = colors.ink, modifier = Modifier.alignByBaseline())
            Text(
                text = stringResource(resource = Res.string.kcal_text),
                style = typography.regular11,
                color = colors.muted,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}

@Composable
private fun DayStrip(viewState: CopyToViewState, onToggled: (LocalDate) -> Unit, onPickDate: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CopySectionLabel(modifier = Modifier.weight(weight = 1f), text = stringResource(resource = Res.string.copy_to))
            Text(text = stringResource(resource = Res.string.copy_pick_days), style = typography.regular11, color = colors.muted)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(state = rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            viewState.stripDates.forEach { date ->
                DayChip(
                    date = date,
                    isToday = date == viewState.today,
                    isLogged = viewState.isLogged(date = date),
                    isPicked = date in viewState.targetDates,
                    onClick = { onToggled(date) }
                )
            }
            val description = stringResource(resource = Res.string.copy_pick_another_date)
            val shape = RoundedCornerShape(size = 14.dp)
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 60.dp)
                    .clip(shape = shape)
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = shape)
                    .clickable(role = Role.Button, onClick = onPickDate)
                    .semantics { contentDescription = description },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_calendar),
                    contentDescription = null,
                    tint = colors.ink,
                    modifier = Modifier.size(size = 20.dp)
                )
            }
        }
        Text(text = stringResource(resource = Res.string.copy_no_future), style = typography.regular11, color = colors.muted)
    }
}

@Composable
private fun DayChip(date: LocalDate, isToday: Boolean, isLogged: Boolean, isPicked: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    val name = if (isToday) stringResource(resource = Res.string.today) + ", " + date.toShortDayMonthDay() else date.toShortDayMonthDay()
    val description = if (isLogged) name else stringResource(resource = Res.string.copy_nothing_logged_day, name)
    val dashed = colors.dashed
    Column(
        modifier = Modifier
            .size(width = 56.dp, height = 60.dp)
            .clip(shape = shape)
            .then(
                when {
                    isPicked -> Modifier.background(color = colors.accent)
                    isLogged -> Modifier.background(color = colors.surface).border(width = 1.dp, color = colors.line, shape = shape)
                    else -> Modifier.dashedChip(color = dashed)
                }
            )
            .clickable(role = Role.Checkbox, onClick = onClick)
            .semantics(mergeDescendants = true) {
                selected = isPicked
                contentDescription = description
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 2.dp, alignment = Alignment.CenterVertically)
    ) {
        val color = if (isPicked) colors.onAccent else colors.ink
        Text(
            text = if (isToday) stringResource(resource = Res.string.today) else date.toShortDayName(),
            style = typography.bold11,
            color = color.copy(alpha = 0.8f)
        )
        Text(text = date.day.toString(), style = typography.bold18, color = color)
    }
}

private fun Modifier.dashedChip(color: Color): Modifier = dashedBorder(color = color, width = 1.dp, cornerRadius = 14.dp)

@Composable
private fun MealPicker(selected: MealType?, onSelected: (MealType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        CopySectionLabel(text = stringResource(resource = Res.string.copy_into_meal))
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            MealType.entries.forEach { mealType ->
                val isSelected = mealType == selected
                val shape = RoundedCornerShape(size = 12.dp)
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 40.dp)
                        .clip(shape = shape)
                        .background(color = if (isSelected) colors.accentSoft else colors.surface)
                        .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
                        .clickable(role = Role.RadioButton) { onSelected(mealType) }
                        .semantics { this.selected = isSelected },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(resource = mealType.title),
                        style = typography.bold12,
                        color = if (isSelected) colors.accentInk else colors.ink,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** Some picked days already have food where the copies would go: add to it, or replace it. */
@Composable
private fun ConflictCard(viewState: CopyToViewState, onReplaceSelected: (Boolean) -> Unit) {
    val dates = viewState.conflictDates
    val one = dates.singleOrNull()
    val text = if (viewState.isMeal) {
        val meal = stringResource(resource = viewState.targetMeal!!.title).lowercase()
        if (one != null) {
            val count = viewState.existingOn(date = one).size
            stringResource(
                resource = Res.string.copy_meal_conflict_one,
                one.label(today = viewState.today),
                meal,
                pluralStringResource(resource = Res.plurals.items, quantity = count, count)
            )
        } else {
            stringResource(resource = Res.string.copy_meal_conflict_many, dates.size, meal)
        }
    } else if (one != null) {
        val count = viewState.existingOn(date = one).size
        stringResource(
            resource = Res.string.copy_day_conflict_one,
            one.label(today = viewState.today),
            pluralStringResource(resource = Res.plurals.items, quantity = count, count)
        )
    } else {
        stringResource(resource = Res.string.copy_day_conflict_many, dates.size)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.warmSoft)
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Text(text = text, style = typography.bold13, color = colors.warmInk)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(color = colors.surface)
                .padding(all = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            listOf(false to Res.string.copy_add_to_it, true to Res.string.copy_replace_it).forEach { (isReplace, label) ->
                val isSelected = isReplace == viewState.isReplace
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 36.dp)
                        .clip(shape = RoundedCornerShape(size = 9.dp))
                        .background(color = if (isSelected) colors.accentSoft else Color.Transparent)
                        .clickable(role = Role.RadioButton) { onReplaceSelected(isReplace) }
                        .semantics { selected = isSelected },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(resource = label),
                        style = typography.bold13,
                        color = if (isSelected) colors.accentInk else colors.muted
                    )
                }
            }
        }
    }
}

@Composable
private fun ctaText(viewState: CopyToViewState): String {
    val count = viewState.selectedLogs.size
    val dates = viewState.targetDates
    return when {
        count == 0 -> stringResource(resource = Res.string.copy_pick_item)
        dates.isEmpty() -> stringResource(resource = Res.string.copy_pick_day)
        else -> {
            val target = dates.singleOrNull()?.let {
                if (it == viewState.today) stringResource(resource = Res.string.copy_target_today) else it.toShortDayMonthDay()
            } ?: stringResource(resource = Res.string.copy_target_days, dates.size)
            stringResource(
                resource = if (viewState.hasConflict && viewState.isReplace) Res.string.copy_replace_cta else Res.string.copy_cta,
                pluralStringResource(resource = Res.plurals.items, quantity = count, count),
                viewState.selectedCalories.withThousands(),
                target
            )
        }
    }
}

@Composable
private fun LocalDate.label(today: LocalDate): String =
    if (this == today) stringResource(resource = Res.string.today) else toShortDayMonthDay()
