package org.thelazybattley.macrotrack.features.mealplan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.plus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.copy_into_meal
import macrotrack.composeapp.generated.resources.copy_summary
import macrotrack.composeapp.generated.resources.day_off
import macrotrack.composeapp.generated.resources.day_on
import macrotrack.composeapp.generated.resources.days_custom
import macrotrack.composeapp.generated.resources.days_every_day
import macrotrack.composeapp.generated.resources.days_weekdays
import macrotrack.composeapp.generated.resources.days_weekends
import macrotrack.composeapp.generated.resources.ic_minus
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.items
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.repeat_cta
import macrotrack.composeapp.generated.resources.repeat_delete
import macrotrack.composeapp.generated.resources.repeat_each_time
import macrotrack.composeapp.generated.resources.repeat_edit_title
import macrotrack.composeapp.generated.resources.repeat_end_after
import macrotrack.composeapp.generated.resources.repeat_end_date
import macrotrack.composeapp.generated.resources.repeat_end_never
import macrotrack.composeapp.generated.resources.repeat_end_never_hint
import macrotrack.composeapp.generated.resources.repeat_ends
import macrotrack.composeapp.generated.resources.repeat_food
import macrotrack.composeapp.generated.resources.repeat_from
import macrotrack.composeapp.generated.resources.repeat_how
import macrotrack.composeapp.generated.resources.repeat_how_log
import macrotrack.composeapp.generated.resources.repeat_how_log_hint
import macrotrack.composeapp.generated.resources.repeat_how_plan
import macrotrack.composeapp.generated.resources.repeat_how_plan_hint
import macrotrack.composeapp.generated.resources.repeat_name
import macrotrack.composeapp.generated.resources.repeat_name_every_day
import macrotrack.composeapp.generated.resources.repeat_name_weekdays
import macrotrack.composeapp.generated.resources.repeat_name_weekends
import macrotrack.composeapp.generated.resources.repeat_next_up
import macrotrack.composeapp.generated.resources.repeat_no_end
import macrotrack.composeapp.generated.resources.repeat_nothing_recent
import macrotrack.composeapp.generated.resources.repeat_on
import macrotrack.composeapp.generated.resources.repeat_pick_day
import macrotrack.composeapp.generated.resources.repeat_pick_food
import macrotrack.composeapp.generated.resources.repeat_pick_meal
import macrotrack.composeapp.generated.resources.repeat_save
import macrotrack.composeapp.generated.resources.repeat_times
import macrotrack.composeapp.generated.resources.repeat_times_fewer
import macrotrack.composeapp.generated.resources.repeat_times_more
import macrotrack.composeapp.generated.resources.repeat_title
import macrotrack.composeapp.generated.resources.repeat_total_times
import macrotrack.composeapp.generated.resources.repeating_new
import macrotrack.composeapp.generated.resources.today
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.core.toDayMonthDay
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.core.toShortDayName
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlanFood
import org.thelazybattley.macrotrack.features.copy.PastMeal
import org.thelazybattley.macrotrack.features.copy.ui.CheckState
import org.thelazybattley.macrotrack.features.copy.ui.CopyCheckBox
import org.thelazybattley.macrotrack.features.copy.ui.CopyHeader
import org.thelazybattley.macrotrack.features.copy.ui.CopySectionLabel
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.mealplan.DayPattern
import org.thelazybattley.macrotrack.features.mealplan.RepeatEnd
import org.thelazybattley.macrotrack.features.mealplan.RepeatMealCallbacks
import org.thelazybattley.macrotrack.features.mealplan.RepeatMealViewModel
import org.thelazybattley.macrotrack.features.mealplan.RepeatMealViewState
import org.thelazybattley.macrotrack.ui.common.CommonDatePickerDialog
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun RepeatMealScreen(
    onClose: () -> Unit,
    onDone: () -> Unit
) {
    val viewModel = koinViewModel<RepeatMealViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = viewState.isDone) {
        if (viewState.isDone) onDone()
    }

    RepeatMealScreen(viewState = viewState, callbacks = viewModel, onClose = onClose)
}

@Composable
private fun RepeatMealScreen(
    viewState: RepeatMealViewState,
    callbacks: RepeatMealCallbacks,
    onClose: () -> Unit
) {
    var isPickingEndDate by rememberSaveable { mutableStateOf(value = false) }
    val mealName = stringResource(resource = viewState.mealType.title)
    val suggestedName = when (viewState.pattern) {
        DayPattern.WEEKDAYS -> stringResource(resource = Res.string.repeat_name_weekdays, mealName.lowercase())
        DayPattern.WEEKENDS -> stringResource(resource = Res.string.repeat_name_weekends, mealName.lowercase())
        DayPattern.EVERY_DAY -> stringResource(resource = Res.string.repeat_name_every_day, mealName.lowercase())
        null -> mealName
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CopyHeader(
                title = when {
                    viewState.isEditing -> stringResource(resource = Res.string.repeat_edit_title)
                    viewState.needsSource -> stringResource(resource = Res.string.repeating_new)
                    else -> stringResource(resource = Res.string.repeat_title, mealName.lowercase())
                },
                subtitle = headerSubtitle(viewState = viewState),
                onClose = onClose
            )
        },
        bottomBar = {
            if (!viewState.needsSource) {
                CommonPrimaryButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    text = ctaText(viewState = viewState),
                    enabled = viewState.canSave,
                    onClick = { callbacks.onSave(suggestedName = suggestedName) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(space = 18.dp)
        ) {
            if (viewState.needsSource) {
                if (viewState.isLoaded) SourcePicker(viewState = viewState, onPicked = callbacks::onSourcePicked)
                return@Column
            }
            Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                CopySectionLabel(text = stringResource(resource = Res.string.repeat_name))
                CommonTextField(
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = suggestedName,
                    textValue = viewState.name,
                    textStyle = typography.regular18,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    onValueChanged = callbacks::onNameChanged
                )
            }
            FoodList(viewState = viewState, onToggled = callbacks::onFoodToggled)
            MealPicker(selected = viewState.mealType, onSelected = callbacks::onMealSelected)
            DaysPicker(viewState = viewState, callbacks = callbacks)
            EndsPicker(viewState = viewState, callbacks = callbacks, onPickDate = { isPickingEndDate = true })
            HowPicker(isAutoLog = viewState.isAutoLog, onSelected = callbacks::onAutoLogSelected)
            NextUpCard(viewState = viewState)
            if (viewState.isEditing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 48.dp)
                        .clip(shape = RoundedCornerShape(size = 14.dp))
                        .clickable(role = Role.Button, onClick = callbacks::onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(resource = Res.string.repeat_delete), style = typography.bold14, color = colors.crimsonRed)
                }
            }
        }
    }
    if (isPickingEndDate) {
        val tomorrow = viewState.today.plus(period = DatePeriod(days = 1))
        CommonDatePickerDialog(
            selected = maxOf(viewState.endDate, tomorrow),
            minDate = tomorrow,
            maxDate = null,
            onDismiss = { isPickingEndDate = false },
            onConfirm = {
                isPickingEndDate = false
                callbacks.onEndDatePicked(date = it)
            }
        )
    }
}

@Composable
private fun headerSubtitle(viewState: RepeatMealViewState): String {
    val summary = stringResource(
        resource = Res.string.copy_summary,
        pluralStringResource(resource = Res.plurals.items, quantity = viewState.foods.size, viewState.foods.size),
        viewState.foods.sumOf { it.calories }.withThousands()
    )
    val source = viewState.sourceDate
    return when {
        viewState.isEditing -> summary
        source != null -> stringResource(resource = Res.string.repeat_from, source.toDayMonthDay(), summary)
        else -> stringResource(resource = Res.string.repeat_pick_meal)
    }
}

/** With nothing to start from, the meals logged over the last week to pick one. */
@Composable
private fun SourcePicker(viewState: RepeatMealViewState, onPicked: (PastMeal) -> Unit) {
    if (viewState.recentMeals.isEmpty()) {
        Text(text = stringResource(resource = Res.string.repeat_nothing_recent), style = typography.regular13, color = colors.muted)
        return
    }
    val shape = RoundedCornerShape(size = 18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
    ) {
        viewState.recentMeals.forEachIndexed { index, meal ->
            val dayName = if (meal.date == viewState.today) stringResource(resource = Res.string.today) else meal.date.toShortDayMonthDay()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable(role = Role.Button) { onPicked(meal) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(
                        text = stringResource(resource = meal.mealType.title) + " · " + dayName,
                        style = typography.bold14,
                        color = colors.ink
                    )
                    Text(
                        text = meal.logs.joinToString(separator = ", ") { it.foodName },
                        style = typography.regular11,
                        color = colors.muted,
                        maxLines = 1
                    )
                }
                Text(
                    text = meal.calories.withThousands() + " " + stringResource(resource = Res.string.kcal_text),
                    style = typography.bold13,
                    color = colors.ink
                )
            }
            if (index != viewState.recentMeals.lastIndex) HorizontalDivider(color = colors.divider)
        }
    }
}

@Composable
private fun FoodList(viewState: RepeatMealViewState, onToggled: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        CopySectionLabel(text = stringResource(resource = Res.string.repeat_food))
        val shape = RoundedCornerShape(size = 18.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = shape)
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = shape)
        ) {
            viewState.foods.forEachIndexed { index, food ->
                FoodRow(
                    food = food,
                    amount = food.amountText(foodUnit = viewState.foodUnit),
                    isPicked = index !in viewState.unchecked,
                    onClick = { onToggled(index) }
                )
                if (index != viewState.foods.lastIndex) HorizontalDivider(color = colors.divider)
            }
        }
    }
}

@Composable
private fun FoodRow(food: PlanFood, amount: String, isPicked: Boolean, onClick: () -> Unit) {
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
            Text(text = food.foodName, style = typography.medium13, color = colors.ink, maxLines = 1)
            Text(text = amount, style = typography.regular11, color = colors.muted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 3.dp)) {
            Text(text = food.calories.withThousands(), style = typography.bold13, color = colors.ink, modifier = Modifier.alignByBaseline())
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
private fun MealPicker(selected: MealType, onSelected: (MealType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        CopySectionLabel(text = stringResource(resource = Res.string.copy_into_meal))
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            MealType.entries.forEach { mealType ->
                Choice(
                    modifier = Modifier.weight(weight = 1f),
                    text = stringResource(resource = mealType.title),
                    isSelected = mealType == selected,
                    onClick = { onSelected(mealType) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DaysPicker(viewState: RepeatMealViewState, callbacks: RepeatMealCallbacks) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        CopySectionLabel(text = stringResource(resource = Res.string.repeat_on))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(space = 6.dp), verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            listOf(
                DayPattern.EVERY_DAY to Res.string.days_every_day,
                DayPattern.WEEKDAYS to Res.string.days_weekdays,
                DayPattern.WEEKENDS to Res.string.days_weekends,
                null to Res.string.days_custom
            ).forEach { (pattern, label) ->
                Choice(
                    text = stringResource(resource = label),
                    isSelected = viewState.pattern == pattern,
                    onClick = { callbacks.onPatternSelected(pattern = pattern) },
                    horizontalPadding = 14.dp
                )
            }
        }
        // The week row starts on Monday.
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            DayOfWeek.entries.forEach { day ->
                val isOn = day in viewState.days
                val description = stringResource(resource = if (isOn) Res.string.day_on else Res.string.day_off, day.fullName())
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 40.dp)
                        .clip(shape = CircleShape)
                        .background(color = if (isOn) colors.accent else colors.surface)
                        .border(width = 1.dp, color = if (isOn) colors.accent else colors.line, shape = CircleShape)
                        .clickable(role = Role.Checkbox) { callbacks.onDayToggled(day = day) }
                        .semantics {
                            selected = isOn
                            contentDescription = description
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.fullName().take(n = 1),
                        style = typography.bold13,
                        color = if (isOn) colors.onAccent else colors.muted
                    )
                }
            }
        }
    }
}

@Composable
private fun EndsPicker(viewState: RepeatMealViewState, callbacks: RepeatMealCallbacks, onPickDate: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        CopySectionLabel(text = stringResource(resource = Res.string.repeat_ends))
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            EndCard(
                modifier = Modifier.weight(weight = 1f),
                title = stringResource(resource = Res.string.repeat_end_never),
                subtitle = stringResource(resource = Res.string.repeat_end_never_hint),
                isSelected = viewState.end == RepeatEnd.NEVER,
                onClick = { callbacks.onEndSelected(end = RepeatEnd.NEVER) }
            )
            EndCard(
                modifier = Modifier.weight(weight = 1f),
                title = stringResource(resource = Res.string.repeat_end_date),
                subtitle = viewState.endDate.toAbbreviatedMonthDay(),
                isSelected = viewState.end == RepeatEnd.ON_DATE,
                // Picking the day is the point of this choice, so it always opens the calendar.
                onClick = onPickDate
            )
            EndCard(
                modifier = Modifier.weight(weight = 1f),
                title = stringResource(resource = Res.string.repeat_end_after),
                subtitle = pluralStringResource(resource = Res.plurals.repeat_times, quantity = viewState.times, viewState.times),
                isSelected = viewState.end == RepeatEnd.AFTER_TIMES,
                onClick = { callbacks.onEndSelected(end = RepeatEnd.AFTER_TIMES) }
            )
        }
        if (viewState.end == RepeatEnd.AFTER_TIMES) {
            TimesStepper(times = viewState.times, onChanged = callbacks::onTimesChanged)
        }
    }
}

@Composable
private fun EndCard(modifier: Modifier = Modifier, title: String, subtitle: String, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 12.dp)
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = if (isSelected) 1.5.dp else 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics(mergeDescendants = true) { selected = isSelected }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(text = title, style = typography.bold13, color = colors.ink, maxLines = 1)
        Text(text = subtitle, style = typography.regular11, color = colors.muted, maxLines = 1)
    }
}

@Composable
private fun TimesStepper(times: Int, onChanged: (Int) -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(all = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepButton(
            icon = Res.drawable.ic_minus,
            description = stringResource(resource = Res.string.repeat_times_fewer),
            enabled = times > 1,
            onClick = { onChanged(times - 1) }
        )
        Text(
            text = pluralStringResource(resource = Res.plurals.repeat_times, quantity = times, times),
            style = typography.bold16,
            color = colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(weight = 1f)
        )
        StepButton(
            icon = Res.drawable.ic_plus,
            description = stringResource(resource = Res.string.repeat_times_more),
            enabled = times < RepeatMealViewState.MAX_TIMES,
            onClick = { onChanged(times + 1) }
        )
    }
}

@Composable
private fun StepButton(icon: DrawableResource, description: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clip(shape = RoundedCornerShape(size = 10.dp))
            .background(color = colors.background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = icon),
            contentDescription = description,
            tint = if (enabled) colors.ink else colors.faint,
            modifier = Modifier.size(size = 18.dp)
        )
    }
}

@Composable
private fun HowPicker(isAutoLog: Boolean, onSelected: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        CopySectionLabel(text = stringResource(resource = Res.string.repeat_how))
        listOf(
            Triple(false, Res.string.repeat_how_plan, Res.string.repeat_how_plan_hint),
            Triple(true, Res.string.repeat_how_log, Res.string.repeat_how_log_hint)
        ).forEach { (value, title, hint) ->
            val isSelected = value == isAutoLog
            val shape = RoundedCornerShape(size = 14.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = shape)
                    .background(color = if (isSelected) colors.accentSoft else colors.surface)
                    .border(width = if (isSelected) 1.5.dp else 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
                    .clickable(role = Role.RadioButton) { onSelected(value) }
                    .semantics(mergeDescendants = true) { selected = isSelected }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(size = 20.dp)
                        .background(color = colors.surface, shape = CircleShape)
                        .border(width = if (isSelected) 6.dp else 2.dp, color = if (isSelected) colors.accent else colors.dashed, shape = CircleShape)
                )
                Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
                    Text(text = stringResource(resource = title), style = typography.bold14, color = colors.ink)
                    Text(text = stringResource(resource = hint), style = typography.regular11, color = colors.muted)
                }
            }
        }
    }
}

@Composable
private fun NextUpCard(viewState: RepeatMealViewState) {
    val upcoming = viewState.upcoming
    val total = viewState.draft.totalTimes
    val eachTime = stringResource(
        resource = Res.string.repeat_each_time,
        stringResource(resource = viewState.mealType.title),
        viewState.selectedCalories.withThousands()
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.nightSoft)
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        Text(text = stringResource(resource = Res.string.repeat_next_up), style = typography.bold12, color = colors.nightInk)
        Text(
            text = if (upcoming.isEmpty()) {
                stringResource(resource = Res.string.repeat_pick_day)
            } else {
                upcoming.joinToString(separator = " · ") { it.toShortDayName() + " " + it.day } +
                    if (upcoming.size == RepeatMealViewState.PREVIEW_DAYS) " …" else ""
            },
            style = typography.bold14,
            color = colors.ink
        )
        Text(
            text = if (total != null) {
                stringResource(resource = Res.string.repeat_total_times, eachTime, total)
            } else {
                stringResource(resource = Res.string.repeat_no_end, eachTime)
            },
            style = typography.regular11,
            color = colors.muted
        )
    }
}

@Composable
private fun Choice(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    horizontalPadding: Dp = 0.dp
) {
    val shape = RoundedCornerShape(size = 12.dp)
    Box(
        modifier = modifier
            .height(height = 40.dp)
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { selected = isSelected }
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = typography.bold12,
            color = if (isSelected) colors.accentInk else colors.ink,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun ctaText(viewState: RepeatMealViewState): String = when {
    viewState.selectedFoods.isEmpty() -> stringResource(resource = Res.string.repeat_pick_food)
    viewState.days.isEmpty() -> stringResource(resource = Res.string.repeat_pick_day)
    viewState.isEditing -> stringResource(resource = Res.string.repeat_save)
    else -> stringResource(
        resource = Res.string.repeat_cta,
        daysLabel(days = viewState.days).let { if (viewState.pattern != null) it.lowercase() else it }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewRepeatMealScreen() {
    val today = getCurrentDate()
    MacroTrackTheme {
        RepeatMealScreen(
            viewState = RepeatMealViewState(
                today = today,
                sourceDate = today,
                foods = listOf(
                    PlanFood(
                        foodName = "Greek yogurt",
                        calories = 190,
                        protein = 20.0,
                        carbs = 8.0,
                        fat = 10.0,
                        weight = 200.0,
                        dominantMacro = MacroType.PROTEIN
                    ),
                    PlanFood(
                        foodName = "Rolled oats",
                        calories = 230,
                        protein = 8.0,
                        carbs = 40.0,
                        fat = 4.0,
                        weight = 60.0,
                        dominantMacro = MacroType.CARBS
                    )
                ),
                isLoaded = true
            ),
            callbacks = RepeatMealCallbacks.default(),
            onClose = {}
        )
    }
}
