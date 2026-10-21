package org.thelazybattley.macrotrack.features.mealplan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.c_value_gram
import macrotrack.composeapp.generated.resources.copy_from_another_day
import macrotrack.composeapp.generated.resources.f_value_gram
import macrotrack.composeapp.generated.resources.ic_calendar
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_copy
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_repeat
import macrotrack.composeapp.generated.resources.items_planned
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.meal_plan_add_to
import macrotrack.composeapp.generated.resources.meal_plan_back_to_today
import macrotrack.composeapp.generated.resources.meal_plan_day_empty
import macrotrack.composeapp.generated.resources.meal_plan_day_has_plan
import macrotrack.composeapp.generated.resources.meal_plan_left_kcal
import macrotrack.composeapp.generated.resources.meal_plan_left_to_plan
import macrotrack.composeapp.generated.resources.meal_plan_note
import macrotrack.composeapp.generated.resources.meal_plan_nothing_planned
import macrotrack.composeapp.generated.resources.meal_plan_of_goal
import macrotrack.composeapp.generated.resources.meal_plan_over
import macrotrack.composeapp.generated.resources.meal_plan_plan
import macrotrack.composeapp.generated.resources.meal_plan_planned
import macrotrack.composeapp.generated.resources.meal_plan_planning
import macrotrack.composeapp.generated.resources.meal_plan_remove
import macrotrack.composeapp.generated.resources.meal_plan_remove_description
import macrotrack.composeapp.generated.resources.meal_plan_repeating_active
import macrotrack.composeapp.generated.resources.meal_plan_repeating_none
import macrotrack.composeapp.generated.resources.meal_plan_skip_day
import macrotrack.composeapp.generated.resources.meal_plan_skip_description
import macrotrack.composeapp.generated.resources.meal_plan_tag_planned
import macrotrack.composeapp.generated.resources.meal_plan_tag_repeat
import macrotrack.composeapp.generated.resources.p_value_gram
import macrotrack.composeapp.generated.resources.repeating_meals_title
import macrotrack.composeapp.generated.resources.today
import macrotrack.composeapp.generated.resources.tomorrow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toDayMonthDay
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlanFood
import org.thelazybattley.macrotrack.domain.model.PlannedFood
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.mealplan.MealPlanCallbacks
import org.thelazybattley.macrotrack.features.mealplan.MealPlanViewModel
import org.thelazybattley.macrotrack.features.mealplan.MealPlanViewState
import org.thelazybattley.macrotrack.ui.common.dashedBorder
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun MealPlanScreen(
    onBack: () -> Unit,
    /** Opens adding food to [MealType] on the planned day. */
    onPlanFood: (MealType, LocalDate) -> Unit,
    onCopyFrom: (MealType, LocalDate) -> Unit,
    onOpenRepeatingMeals: () -> Unit
) {
    val viewModel = koinViewModel<MealPlanViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    MealPlanScreen(
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack,
        onPlanFood = { onPlanFood(it, viewState.selectedDate) },
        onCopyFrom = { onCopyFrom(it, viewState.selectedDate) },
        onOpenRepeatingMeals = onOpenRepeatingMeals
    )
}

@Composable
private fun MealPlanScreen(
    viewState: MealPlanViewState,
    callbacks: MealPlanCallbacks,
    onBack: () -> Unit,
    onPlanFood: (MealType) -> Unit,
    onCopyFrom: (MealType) -> Unit,
    onOpenRepeatingMeals: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.background)
    ) {
        PlanHeader(
            leadingIcon = Res.drawable.ic_chevron_left,
            leadingDescription = stringResource(resource = Res.string.back),
            onLeading = onBack,
            trailing = {
                SquareIconButton(
                    icon = Res.drawable.ic_repeat,
                    description = stringResource(resource = Res.string.repeating_meals_title),
                    onClick = onOpenRepeatingMeals
                )
            }
        ) {
            DayTitle(modifier = Modifier.weight(weight = 1f), date = viewState.selectedDate, today = viewState.today)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            DayStrip(viewState = viewState, onToday = onBack, onSelected = callbacks::onDaySelected)
            TallyCard(viewState = viewState)
            MealType.entries.forEach { mealType ->
                PlannedMealCard(
                    mealType = mealType,
                    viewState = viewState,
                    onPlanFood = { onPlanFood(mealType) },
                    onCopyFrom = { onCopyFrom(mealType) },
                    onRemove = callbacks::onRemove
                )
            }
            RepeatingMealsLink(activeCount = viewState.activeRepeatingCount, onClick = onOpenRepeatingMeals)
        }
    }
}

@Composable
private fun DayTitle(modifier: Modifier = Modifier, date: LocalDate, today: LocalDate) {
    val isTomorrow = date == today.plus(period = DatePeriod(days = 1))
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Row(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .background(color = colors.nightSoft)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_calendar),
                    contentDescription = null,
                    tint = colors.nightInk,
                    modifier = Modifier.size(size = 13.dp)
                )
                Text(text = stringResource(resource = Res.string.meal_plan_planning), style = typography.bold12, color = colors.nightInk)
            }
            Text(text = date.toDayMonthDay(), style = typography.medium13, color = colors.muted, maxLines = 1)
        }
        Text(
            text = if (isTomorrow) stringResource(resource = Res.string.tomorrow) else date.dayOfWeek.fullName(),
            style = typography.bold32,
            color = colors.ink
        )
    }
}

@Composable
private fun DayStrip(viewState: MealPlanViewState, onToday: () -> Unit, onSelected: (LocalDate) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(state = rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        val shape = RoundedCornerShape(size = 14.dp)
        val todayDescription = stringResource(resource = Res.string.meal_plan_back_to_today)
        Column(
            modifier = Modifier
                .size(width = 46.dp, height = 60.dp)
                .clip(shape = shape)
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = shape)
                .clickable(role = Role.Button, onClick = onToday)
                .semantics(mergeDescendants = true) { contentDescription = todayDescription },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 2.dp, alignment = Alignment.CenterVertically)
        ) {
            Text(
                text = stringResource(resource = Res.string.today).take(n = 1),
                style = typography.medium11,
                color = colors.ink.copy(alpha = 0.75f)
            )
            Text(text = viewState.today.day.toString(), style = typography.bold16, color = colors.ink)
        }
        viewState.days.forEach { date ->
            PlanDayChip(
                date = date,
                isSelected = date == viewState.selectedDate,
                hasPlan = viewState.hasPlan(date = date),
                onClick = { onSelected(date) }
            )
        }
    }
}

@Composable
private fun PlanDayChip(date: LocalDate, isSelected: Boolean, hasPlan: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    val name = date.toShortDayMonthDay()
    val description = stringResource(resource = if (hasPlan) Res.string.meal_plan_day_has_plan else Res.string.meal_plan_day_empty, name)
    val nightInk = colors.nightInk
    val textColor = if (isSelected) colors.surface else colors.ink
    Column(
        modifier = Modifier
            .size(width = 46.dp, height = 60.dp)
            .clip(shape = shape)
            .then(
                if (isSelected) {
                    Modifier.background(color = nightInk)
                } else {
                    Modifier.dashedBorder(color = nightInk, width = 1.5.dp, cornerRadius = 14.dp)
                }
            )
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics(mergeDescendants = true) {
                selected = isSelected
                contentDescription = description
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 2.dp, alignment = Alignment.CenterVertically)
    ) {
        Text(text = date.dayOfWeek.name.take(n = 1), style = typography.medium11, color = textColor.copy(alpha = 0.8f))
        Text(text = date.day.toString(), style = typography.bold16, color = textColor)
        Box(
            modifier = Modifier
                .size(size = 5.dp)
                .background(
                    color = when {
                        !hasPlan -> Color.Transparent
                        isSelected -> colors.surface
                        else -> nightInk
                    },
                    shape = CircleShape
                )
        )
    }
}

/** What's planned against the calorie goal. Planned food isn't counted until it's eaten. */
@Composable
private fun TallyCard(viewState: MealPlanViewState) {
    val planned = viewState.plannedCalories
    val goal = viewState.calorieGoal
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.nightSoft)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(text = stringResource(resource = Res.string.meal_plan_planned), style = typography.regular11, color = colors.muted)
                Row(horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
                    Text(
                        text = planned.withThousands(),
                        style = typography.bold24,
                        color = colors.ink,
                        modifier = Modifier.alignByBaseline()
                    )
                    if (goal != null) {
                        Text(
                            text = stringResource(resource = Res.string.meal_plan_of_goal, goal.withThousands()),
                            style = typography.regular13,
                            color = colors.muted,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }
            }
            if (goal != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = stringResource(resource = Res.string.meal_plan_left_to_plan), style = typography.regular11, color = colors.muted)
                    Text(
                        text = if (planned > goal) {
                            stringResource(resource = Res.string.meal_plan_over, (planned - goal).withThousands())
                        } else {
                            stringResource(resource = Res.string.meal_plan_left_kcal, (goal - planned).withThousands())
                        },
                        style = typography.bold18,
                        color = colors.ink
                    )
                }
            }
        }
        if (goal != null && goal > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 8.dp)
                    .clip(shape = RoundedCornerShape(size = 4.dp))
                    .background(color = colors.nightInk.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = (planned.toFloat() / goal).coerceIn(minimumValue = 0f, maximumValue = 1f))
                        .clip(shape = RoundedCornerShape(size = 4.dp))
                        .background(color = colors.nightInk)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 14.dp)) {
            MacroDot(color = colors.protein, text = stringResource(resource = Res.string.p_value_gram, viewState.plannedProtein.toInt()))
            MacroDot(color = colors.carbs, text = stringResource(resource = Res.string.c_value_gram, viewState.plannedCarbs.toInt()))
            MacroDot(color = colors.fat, text = stringResource(resource = Res.string.f_value_gram, viewState.plannedFat.toInt()))
        }
        Text(text = stringResource(resource = Res.string.meal_plan_note), style = typography.regular11, color = colors.nightInk)
    }
}

@Composable
private fun MacroDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
        Box(modifier = Modifier.size(size = 7.dp).background(color = color, shape = RoundedCornerShape(size = 2.dp)))
        Text(text = text, style = typography.regular11, color = colors.muted)
    }
}

@Composable
private fun PlannedMealCard(
    mealType: MealType,
    viewState: MealPlanViewState,
    onPlanFood: () -> Unit,
    onCopyFrom: () -> Unit,
    onRemove: (PlannedFood) -> Unit
) {
    val items = viewState.mealPlan(mealType = mealType)
    val hasItems = items.isNotEmpty()
    val mealName = stringResource(resource = mealType.title)
    val shape = RoundedCornerShape(size = 20.dp)
    val dashed = colors.dashed
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (hasItems) {
                    Modifier
                        .clip(shape = shape)
                        .background(color = colors.surface)
                        .border(width = 1.dp, color = colors.line, shape = shape)
                } else {
                    Modifier.dashedBorder(color = dashed, width = 1.5.dp, cornerRadius = 20.dp)
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(text = mealName, style = typography.bold15, color = colors.ink)
                Text(
                    text = if (hasItems) {
                        pluralStringResource(resource = Res.plurals.items_planned, quantity = items.size, items.size)
                    } else {
                        stringResource(resource = Res.string.meal_plan_nothing_planned)
                    },
                    style = typography.regular11,
                    color = colors.muted
                )
                if (!hasItems) {
                    Row(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(shape = RoundedCornerShape(size = 8.dp))
                            .clickable(role = Role.Button, onClick = onCopyFrom)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
                    ) {
                        Icon(
                            painter = painterResource(resource = Res.drawable.ic_copy),
                            contentDescription = null,
                            tint = colors.accentInk,
                            modifier = Modifier.size(size = 14.dp)
                        )
                        Text(
                            text = stringResource(resource = Res.string.copy_from_another_day),
                            style = typography.bold12,
                            color = colors.accentInk
                        )
                    }
                }
            }
            if (hasItems) {
                Text(text = items.sumOf { it.food.calories }.withThousands(), style = typography.bold16, color = colors.ink)
            }
            val addDescription = stringResource(resource = Res.string.meal_plan_add_to, mealName.lowercase())
            if (hasItems) {
                Box(
                    modifier = Modifier
                        .size(size = 44.dp)
                        .clip(shape = RoundedCornerShape(size = 12.dp))
                        .background(color = colors.nightSoft)
                        .clickable(role = Role.Button, onClick = onPlanFood),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_plus),
                        contentDescription = addDescription,
                        tint = colors.nightInk,
                        modifier = Modifier.size(size = 18.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .height(height = 44.dp)
                        .clip(shape = RoundedCornerShape(size = 12.dp))
                        .background(color = colors.nightInk)
                        .clickable(role = Role.Button, onClick = onPlanFood)
                        .semantics { contentDescription = addDescription }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(resource = Res.string.meal_plan_plan), style = typography.bold14, color = colors.surface)
                }
            }
        }
        if (!hasItems) return@Column
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        items.forEachIndexed { index, planned ->
            PlannedRow(planned = planned, viewState = viewState, onRemove = { onRemove(planned) })
            if (index != items.lastIndex) HorizontalDivider(thickness = 1.dp, color = colors.divider)
        }
    }
}

@Composable
private fun PlannedRow(planned: PlannedFood, viewState: MealPlanViewState, onRemove: () -> Unit) {
    val repeatingMeal = viewState.repeatingMealOf(planned = planned)
    val isRepeat = planned.repeatingMealId != null
    val dayName = planned.date.toShortDayMonthDay()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                Text(
                    text = planned.food.foodName,
                    style = typography.medium13,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(weight = 1f, fill = false)
                )
                Text(
                    text = "· " + planned.food.amountText(foodUnit = viewState.foodUnit),
                    style = typography.regular13,
                    color = colors.muted,
                    maxLines = 1
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                PlanTag(
                    text = if (repeatingMeal != null) {
                        stringResource(resource = Res.string.meal_plan_tag_repeat, daysLabel(days = repeatingMeal.days))
                    } else {
                        stringResource(resource = Res.string.meal_plan_tag_planned)
                    }
                )
                Text(
                    text = planned.food.calories.withThousands() + " " + stringResource(resource = Res.string.kcal_text),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
        }
        val label = stringResource(resource = if (isRepeat) Res.string.meal_plan_skip_day else Res.string.meal_plan_remove)
        val description = stringResource(
            resource = if (isRepeat) Res.string.meal_plan_skip_description else Res.string.meal_plan_remove_description,
            planned.food.foodName,
            dayName
        )
        val shape = RoundedCornerShape(size = 10.dp)
        Box(
            modifier = Modifier
                .height(height = 34.dp)
                .clip(shape = shape)
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = shape)
                .clickable(role = Role.Button, onClick = onRemove)
                .semantics { contentDescription = description }
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = label, style = typography.bold12, color = colors.muted)
        }
    }
}

@Composable
private fun RepeatingMealsLink(activeCount: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 36.dp)
                .background(color = colors.nightSoft, shape = RoundedCornerShape(size = 11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_repeat),
                contentDescription = null,
                tint = colors.nightInk,
                modifier = Modifier.size(size = 18.dp)
            )
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = stringResource(resource = Res.string.repeating_meals_title), style = typography.bold14, color = colors.ink)
            Text(
                text = if (activeCount > 0) {
                    stringResource(resource = Res.string.meal_plan_repeating_active, activeCount)
                } else {
                    stringResource(resource = Res.string.meal_plan_repeating_none)
                },
                style = typography.regular11,
                color = colors.muted
            )
        }
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.faint,
            modifier = Modifier.size(size = 16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewMealPlanScreen() {
    val today = getCurrentDate()
    val tomorrow = today.plus(period = DatePeriod(days = 1))
    MacroTrackTheme {
        MealPlanScreen(
            viewState = MealPlanViewState(
                today = today,
                selectedDate = tomorrow,
                calorieGoal = 2150,
                planned = listOf(
                    PlannedFood(
                        id = 1,
                        date = tomorrow,
                        mealType = MealType.DINNER,
                        food = PlanFood(
                            foodName = "Salmon fillet",
                            calories = 310,
                            protein = 31.0,
                            carbs = 0.0,
                            fat = 20.0,
                            weight = 150.0,
                            dominantMacro = MacroType.PROTEIN
                        )
                    )
                )
            ),
            callbacks = MealPlanCallbacks.default(),
            onBack = {},
            onPlanFood = {},
            onCopyFrom = {},
            onOpenRepeatingMeals = {}
        )
    }
}
