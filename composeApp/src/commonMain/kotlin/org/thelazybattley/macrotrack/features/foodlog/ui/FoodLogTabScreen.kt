package org.thelazybattley.macrotrack.features.foodlog.ui

import androidx.compose.foundation.background
import org.thelazybattley.macrotrack.features.quickadd.ui.QuickAddTag
import org.thelazybattley.macrotrack.ui.common.formatPortion
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.edit_entry_value
import macrotrack.composeapp.generated.resources.tap_entry_hint
import macrotrack.composeapp.generated.resources.add
import macrotrack.composeapp.generated.resources.add_to_value
import macrotrack.composeapp.generated.resources.c_value_gram
import macrotrack.composeapp.generated.resources.f_value_gram
import macrotrack.composeapp.generated.resources.goal_not_recorded
import macrotrack.composeapp.generated.resources.ic_calendar
import macrotrack.composeapp.generated.resources.ic_flame
import macrotrack.composeapp.generated.resources.ic_meal_breakfast
import macrotrack.composeapp.generated.resources.ic_meal_dinner
import macrotrack.composeapp.generated.resources.ic_meal_lunch
import macrotrack.composeapp.generated.resources.ic_meal_snack
import macrotrack.composeapp.generated.resources.ic_nav_progress
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_trend_down
import macrotrack.composeapp.generated.resources.ic_trend_flat
import macrotrack.composeapp.generated.resources.items
import macrotrack.composeapp.generated.resources.items_kcal
import macrotrack.composeapp.generated.resources.kcal
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.meals
import macrotrack.composeapp.generated.resources.nothing_logged_yet
import macrotrack.composeapp.generated.resources.p_c_f
import macrotrack.composeapp.generated.resources.p_value_gram
import macrotrack.composeapp.generated.resources.percent_value
import macrotrack.composeapp.generated.resources.recipe
import macrotrack.composeapp.generated.resources.streak_open_calendar
import macrotrack.composeapp.generated.resources.today
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.rememberReviewPrompt
import kotlinx.coroutines.delay
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.features.addmeal.recipePercentEaten
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.dummyFoodLog
import org.thelazybattley.macrotrack.features.foodlog.DayGoal
import org.thelazybattley.macrotrack.features.foodlog.FoodLogCallbacks
import org.thelazybattley.macrotrack.features.foodlog.FoodLogFoodListByMealType
import org.thelazybattley.macrotrack.features.foodlog.FoodLogViewModel
import org.thelazybattley.macrotrack.features.foodlog.FoodLogViewState
import org.thelazybattley.macrotrack.features.pro.ui.ProTrialBanner
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.features.checkin.ui.CheckInCard
import org.thelazybattley.macrotrack.features.tour.TourTarget
import org.thelazybattley.macrotrack.features.tour.tourTarget
import org.thelazybattley.macrotrack.ui.common.CommonSwipeToDelete
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

@Composable
fun FoodLogTabScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    val viewModel = koinViewModel<FoodLogViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = Unit) {
        viewModel.onScreenShown()
    }

    val reviewPrompt = rememberReviewPrompt()
    LaunchedEffect(key1 = viewState.isReviewDue) {
        if (viewState.isReviewDue) {
            // Lets the updated streak land on screen first, so the ask follows the moment it rewards.
            delay(timeMillis = REVIEW_PROMPT_DELAY_MILLIS)
            viewModel.onReviewAsked()
            reviewPrompt.request()
        }
    }

    LaunchedEffect(key1 = viewState.navigateMealTypeParameter) {
        if (viewState.navigateMealTypeParameter != null) {
            onNavigate(
                AppDestinations.Root.AddMeal.createRoute(
                    mealType = viewState.navigateMealTypeParameter?.name ?: "",
                    logDate = viewState.selectedDate
                )
            )
            viewModel.resetNavigateMealTypeParameter()
        }
    }

    FoodLogTabScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onOpenCalendar = { onNavigate(AppDestinations.Root.Calendar.route) },
        onOpenCheckIn = { onNavigate(AppDestinations.Root.CheckIn.route) }
    )
}

private const val REVIEW_PROMPT_DELAY_MILLIS = 1_500L

@Composable
private fun FoodLogTabScreen(
    modifier: Modifier = Modifier,
    viewState: FoodLogViewState,
    callbacks: FoodLogCallbacks,
    onOpenCalendar: () -> Unit,
    onOpenCheckIn: () -> Unit
) {
    val isToday = viewState.selectedDate == getCurrentDate()
    val meals = listOf(viewState.breakfast, viewState.lunch, viewState.dinner, viewState.snack)

    Column(
        modifier = modifier
            .background(color = colors.background)
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FoodLogHeader(
                modifier = Modifier
                    .weight(weight = 1f)
                    .tourTarget(target = TourTarget.TODAY_HEADER),
                viewState = viewState,
                isToday = isToday
            )
            StreakCalendarButton(
                modifier = Modifier.tourTarget(target = TourTarget.TODAY_STREAK),
                streak = viewState.currentStreak,
                onClick = onOpenCalendar
            )
        }
        FoodLogDateChips(
            modifier = Modifier.fillMaxWidth(),
            dates = viewState.availableDates,
            dateSelected = viewState.selectedDate,
        ) { date ->
            callbacks.onDateFilterSelected(date = date)
        }
        FoodLogTotalMacros(
            modifier = Modifier
                .fillMaxWidth()
                .tourTarget(target = TourTarget.TODAY_REMAINING),
            viewState = viewState
        )
        viewState.checkIn?.takeIf { isToday && LocalProAccess.current.isActive }?.let { checkIn ->
            CheckInCard(modifier = Modifier.fillMaxWidth(), checkIn = checkIn, onClick = onOpenCheckIn)
        }
        ProTrialBanner(modifier = Modifier.fillMaxWidth())
        MealsHeader(
            modifier = Modifier.fillMaxWidth(),
            itemCount = meals.sumOf { it.foodList.size },
            calories = viewState.totalFoodMacros?.calories ?: 0
        )
        if (meals.any { it.foodList.isNotEmpty() }) {
            Text(
                text = stringResource(resource = Res.string.tap_entry_hint),
                style = typography.regular11,
                color = colors.muted
            )
        }
        meals.forEachIndexed { index, meal ->
            LoggedFoodMealType(
                modifier = Modifier.fillMaxWidth(),
                addButtonModifier = if (index == 0) Modifier.tourTarget(target = TourTarget.TODAY_ADD_MEAL) else Modifier,
                foodList = meal,
                foodUnit = viewState.foodUnit,
                recipeCalories = viewState.recipeCalories,
                onDeleteFoodLog = { id ->
                    callbacks.onDeleteFoodLog(id = id)
                },
                onOpenEntry = callbacks::onOpenEntry,
                onNavigate = {
                    callbacks.onNavigate(mealType = meal.mealType)
                }
            )
        }
    }
    viewState.entryEdit?.let { edit ->
        EntryEditSheet(
            edit = edit,
            onAmountChanged = callbacks::onEntryAmountChanged,
            onTypingStarted = callbacks::onEntryTypingStarted,
            onKey = callbacks::onEntryKey,
            onMealChanged = callbacks::onEntryMealChanged,
            onClose = callbacks::onCloseEntry,
            onSave = callbacks::onSaveEntry,
            onDelete = callbacks::onDeleteEntry
        )
    }
}

@Composable
private fun FoodLogHeader(
    modifier: Modifier = Modifier,
    viewState: FoodLogViewState,
    isToday: Boolean
) {
    val date = viewState.selectedDate
    val dayOfWeek = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val dateText = "$dayOfWeek, ${date.toAbbreviatedMonthDay()}"
    val dayGoal = viewState.dayGoal
    Column(modifier = modifier) {
        if (dayGoal == null) {
            Text(text = dateText, style = typography.medium13, color = colors.muted)
        } else {
            DayGoalLabel(dayGoal = dayGoal, dateText = dateText)
        }
        Text(
            text = if (isToday) stringResource(resource = Res.string.today) else dayOfWeek,
            style = typography.bold32,
            color = colors.ink
        )
        if (dayGoal?.isEstimated == true) {
            Text(
                text = stringResource(resource = Res.string.goal_not_recorded),
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

/** Today's logging streak; opens the calendar. */
@Composable
private fun StreakCalendarButton(
    modifier: Modifier = Modifier,
    streak: Int,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    val description = stringResource(resource = Res.string.streak_open_calendar, streak)
    Row(
        modifier = modifier
            .height(height = 44.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(start = 10.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_flame),
            contentDescription = null,
            tint = if (streak > 0) colors.fat else colors.faint,
            modifier = Modifier.size(size = 20.dp)
        )
        Text(
            text = streak.toString(),
            style = typography.bold18,
            color = colors.ink,
            modifier = Modifier.clearAndSetSemantics {}
        )
        Box(
            modifier = Modifier
                .width(width = 1.dp)
                .height(height = 20.dp)
                .background(color = colors.line)
        )
        Icon(
            painter = painterResource(resource = Res.drawable.ic_calendar),
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(size = 18.dp)
        )
    }
}

/** The label above the title: the selected day's nutrition goal, then its date. */
@Composable
private fun DayGoalLabel(
    dayGoal: DayGoal,
    dateText: String
) {
    val (icon, background, tint) = when (dayGoal.goal) {
        Goal.LOSE_WEIGHT -> Triple(Res.drawable.ic_trend_down, colors.accentSoft, colors.accentInk)
        Goal.MAINTAIN_WEIGHT -> Triple(Res.drawable.ic_trend_flat, colors.nightSoft, colors.nightInk)
        Goal.GAIN_WEIGHT -> Triple(Res.drawable.ic_nav_progress, colors.warmSoft, colors.warmInk)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(size = 8.dp))
                .background(color = background)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
        ) {
            Icon(
                painter = painterResource(resource = icon),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size = 13.dp)
            )
            Text(
                text = stringResource(resource = dayGoal.goal.title),
                style = typography.bold12,
                color = tint
            )
        }
        Text(text = dateText, style = typography.medium13, color = colors.muted)
    }
}

@Composable
private fun MealsHeader(
    modifier: Modifier = Modifier,
    itemCount: Int,
    calories: Int
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = stringResource(resource = Res.string.meals),
            style = typography.bold18,
            color = colors.ink,
            modifier = Modifier.weight(weight = 1f)
        )
        Text(
            text = stringResource(
                resource = Res.string.items_kcal,
                pluralStringResource(resource = Res.plurals.items, quantity = itemCount, itemCount),
                calories
            ),
            style = typography.regular11,
            color = colors.muted
        )
    }
}

@Composable
private fun LoggedFoodMealType(
    modifier: Modifier = Modifier,
    addButtonModifier: Modifier = Modifier,
    foodList: FoodLogFoodListByMealType,
    foodUnit: Map<String, String>,
    recipeCalories: Map<String, Int>,
    onDeleteFoodLog: (Long) -> Unit,
    onOpenEntry: (FoodLog) -> Unit,
    onNavigate: () -> Unit
) {
    val shape = RoundedCornerShape(size = 20.dp)
    val isEmpty = foodList.foodList.isEmpty()
    val dashedColor = colors.dashed

    val cardModifier = if (isEmpty) {
        modifier.drawBehind {
            drawRoundRect(
                color = dashedColor,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(6.dp.toPx(), 4.dp.toPx())
                    )
                ),
                cornerRadius = CornerRadius(x = 20.dp.toPx(), y = 20.dp.toPx())
            )
        }
    } else {
        modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
    }

    Column(modifier = cardModifier) {
        MealTypeHeader(
            modifier = Modifier.fillMaxWidth().padding(all = 14.dp),
            addButtonModifier = addButtonModifier,
            foodList = foodList,
            onNavigate = onNavigate
        )
        if (isEmpty) return@Column
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        foodList.foodList.forEachIndexed { index, food ->
            key(food.id) {
                CommonSwipeToDelete(
                    modifier = Modifier.fillMaxWidth(),
                    onDelete = {
                        onDeleteFoodLog(food.id)
                    }
                ) {
                    FoodLogItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = colors.surface)
                            .clickable(
                                onClickLabel = stringResource(resource = Res.string.edit_entry_value, food.foodName),
                                role = Role.Button
                            ) { onOpenEntry(food) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        food = food,
                        unit = foodUnit[food.foodName] ?: "",
                        recipePercent = recipeCalories[food.foodName]?.let {
                            recipePercentEaten(loggedCalories = food.calories, fullCalories = it)
                        }
                    )
                }
            }
            if (index == foodList.foodList.lastIndex) return@forEachIndexed
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
        }
    }
}

@Composable
private fun MealTypeHeader(
    modifier: Modifier = Modifier,
    addButtonModifier: Modifier = Modifier,
    foodList: FoodLogFoodListByMealType,
    onNavigate: () -> Unit
) {
    val isEmpty = foodList.foodList.isEmpty()
    val style = foodList.mealType.toMealStyle()
    val mealName = stringResource(resource = foodList.mealType.title)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 36.dp)
                .background(color = style.background, shape = RoundedCornerShape(size = 12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = style.icon),
                contentDescription = null,
                tint = style.tint,
                modifier = Modifier.size(size = 18.dp)
            )
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = mealName,
                style = typography.bold15,
                color = colors.ink
            )
            Text(
                text = if (isEmpty) {
                    stringResource(resource = Res.string.nothing_logged_yet)
                } else {
                    stringResource(
                        resource = Res.string.p_c_f,
                        foodList.protein.toInt(),
                        foodList.carbs.toInt(),
                        foodList.fat.toInt()
                    )
                },
                style = typography.regular11,
                color = colors.muted
            )
        }
        if (!isEmpty) {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 3.dp)) {
                Text(
                    text = foodList.calories.withThousands(),
                    style = typography.bold16,
                    color = colors.ink,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = stringResource(resource = Res.string.kcal_text),
                    style = typography.regular11,
                    color = colors.muted,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
        val addContentDescription = stringResource(resource = Res.string.add_to_value, mealName)
        if (isEmpty) {
            Box(
                modifier = addButtonModifier
                    .height(height = 44.dp)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.accent)
                    .clickable(onClick = onNavigate)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = Res.string.add),
                    style = typography.bold14,
                    color = colors.onAccent
                )
            }
        } else {
            Box(
                modifier = addButtonModifier
                    .size(size = 44.dp)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.accentSoft)
                    .clickable(onClick = onNavigate),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_plus),
                    contentDescription = addContentDescription,
                    tint = colors.accentInk,
                    modifier = Modifier.size(size = 18.dp)
                )
            }
        }
    }
}

@Composable
private fun FoodLogItem(
    modifier: Modifier = Modifier,
    food: FoodLog,
    unit: String,
    /** The share of a full recipe serving eaten; null for a food, or a recipe that no longer exists. */
    recipePercent: Double? = null
) {
    val isRecipe = food.isRecipe
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
            ) {
                Text(
                    text = food.foodName,
                    style = typography.medium13,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(weight = 1f, fill = false)
                )
                if (isRecipe) {
                    Text(
                        text = stringResource(resource = Res.string.recipe).uppercase(),
                        style = typography.bold10,
                        color = colors.accentInk,
                        modifier = Modifier
                            .background(color = colors.accentSoft, shape = RoundedCornerShape(size = 6.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
                if (food.isQuickAdd) QuickAddTag()
                if (isRecipe && recipePercent != null) {
                    Text(
                        text = "· " + stringResource(resource = Res.string.percent_value, recipePercent.roundToInt()),
                        style = typography.regular13,
                        color = colors.muted
                    )
                } else if (!isRecipe && !food.isQuickAdd) {
                    Text(
                        text = "· " + formatPortion(amount = food.weight, storedUnit = unit),
                        style = typography.regular13,
                        color = colors.muted
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
                MacroValue(
                    color = colors.protein,
                    text = stringResource(resource = Res.string.p_value_gram, food.protein.toInt())
                )
                MacroValue(
                    color = colors.carbs,
                    text = stringResource(resource = Res.string.c_value_gram, food.carbs.toInt())
                )
                MacroValue(
                    color = colors.fat,
                    text = stringResource(resource = Res.string.f_value_gram, food.fat.toInt())
                )
            }
        }
        Text(
            text = stringResource(resource = Res.string.kcal, food.calories),
            style = typography.bold13,
            color = colors.ink
        )
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.faint,
            modifier = Modifier.size(size = 14.dp)
        )
    }
}

@Composable
private fun MacroValue(
    color: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 7.dp)
                .background(color = color, shape = RoundedCornerShape(size = 2.dp))
        )
        Text(
            text = text,
            style = typography.regular11,
            color = colors.muted
        )
    }
}

private data class MealStyle(
    val icon: DrawableResource,
    val background: Color,
    val tint: Color
)

@Composable
private fun MealType.toMealStyle(): MealStyle = when (this) {
    MealType.BREAKFAST -> MealStyle(Res.drawable.ic_meal_breakfast, colors.warmSoft, colors.warmInk)
    MealType.LUNCH -> MealStyle(Res.drawable.ic_meal_lunch, colors.accentSoft, colors.accentInk)
    MealType.DINNER -> MealStyle(Res.drawable.ic_meal_dinner, colors.nightSoft, colors.nightInk)
    MealType.SNACK -> MealStyle(Res.drawable.ic_meal_snack, colors.warmSoft, colors.warmInk)
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewFoodLogTabScreen() {
    MacroTrackTheme {
        FoodLogTabScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp),
            viewState = FoodLogViewState(
                breakfast = FoodLogFoodListByMealType(
                    mealType = MealType.BREAKFAST,
                    foodList = listOf(
                        dummyFoodLog.copy(
                            weight = 0.0
                        ),
                        dummyFoodLog.copy(id = 2),
                        dummyFoodLog.copy(id = 3),
                    ),
                    calories = 369,
                    protein = 63.9,
                    carbs = 36.9,
                    fat = 36.9
                ),
                availableDates = listOf(getCurrentDate())
            ),
            callbacks = FoodLogCallbacks.default(),
            onOpenCalendar = {},
            onOpenCheckIn = {}
        )
    }
}
