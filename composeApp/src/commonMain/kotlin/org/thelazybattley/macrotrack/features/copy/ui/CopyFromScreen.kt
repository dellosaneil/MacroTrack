package org.thelazybattley.macrotrack.features.copy.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.copy_filter_any
import macrotrack.composeapp.generated.resources.copy_filter_breakfasts
import macrotrack.composeapp.generated.resources.copy_filter_dinners
import macrotrack.composeapp.generated.resources.copy_filter_lunches
import macrotrack.composeapp.generated.resources.copy_filter_snacks
import macrotrack.composeapp.generated.resources.copy_into_cta
import macrotrack.composeapp.generated.resources.copy_into_subtitle
import macrotrack.composeapp.generated.resources.copy_into_subtitle_over
import macrotrack.composeapp.generated.resources.copy_into_title
import macrotrack.composeapp.generated.resources.copy_kcal_left
import macrotrack.composeapp.generated.resources.copy_kcal_over
import macrotrack.composeapp.generated.resources.copy_last_seven_days
import macrotrack.composeapp.generated.resources.copy_left_after
import macrotrack.composeapp.generated.resources.copy_left_after_today
import macrotrack.composeapp.generated.resources.copy_meal_count
import macrotrack.composeapp.generated.resources.copy_nothing_recent
import macrotrack.composeapp.generated.resources.copy_pick_item
import macrotrack.composeapp.generated.resources.copy_yesterday_date
import macrotrack.composeapp.generated.resources.items
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.today
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toDayMonthDay
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.copy.CopyFromCallbacks
import org.thelazybattley.macrotrack.features.copy.CopyFromViewModel
import org.thelazybattley.macrotrack.features.copy.CopyFromViewState
import org.thelazybattley.macrotrack.features.copy.PastMeal
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs

@Composable
fun CopyFromScreen(
    onClose: () -> Unit,
    onCopied: () -> Unit
) {
    val viewModel = koinViewModel<CopyFromViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = viewState.isDone) {
        if (viewState.isDone) onCopied()
    }

    CopyFromScreen(viewState = viewState, callbacks = viewModel, onClose = onClose)
}

@Composable
private fun CopyFromScreen(
    viewState: CopyFromViewState,
    callbacks: CopyFromCallbacks,
    onClose: () -> Unit
) {
    val isToday = viewState.targetDate == getCurrentDate()
    val mealName = stringResource(resource = viewState.mealType.title).lowercase()
    val selected = viewState.selected
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            val day = (if (isToday) stringResource(resource = Res.string.today) + ", " else "") + viewState.targetDate.toDayMonthDay()
            val remaining = viewState.remainingCalories
            CopyHeader(
                title = stringResource(resource = Res.string.copy_into_title, mealName),
                subtitle = when {
                    remaining == null -> day
                    remaining < 0 -> stringResource(resource = Res.string.copy_into_subtitle_over, day, abs(remaining).withThousands())
                    else -> stringResource(resource = Res.string.copy_into_subtitle, day, remaining.withThousands())
                },
                onClose = onClose
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                viewState.remainingAfter?.takeIf { selected != null }?.let { left ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(resource = if (isToday) Res.string.copy_left_after_today else Res.string.copy_left_after),
                            style = typography.regular13,
                            color = colors.muted,
                            modifier = Modifier.weight(weight = 1f)
                        )
                        Text(
                            text = if (left < 0) {
                                stringResource(resource = Res.string.copy_kcal_over, abs(left).withThousands())
                            } else {
                                stringResource(resource = Res.string.copy_kcal_left, left.withThousands())
                            },
                            style = typography.bold13,
                            color = if (left < 0) colors.danger else colors.accentInk
                        )
                    }
                }
                CommonPrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = selected?.let {
                        stringResource(
                            resource = Res.string.copy_into_cta,
                            pluralStringResource(resource = Res.plurals.items, quantity = it.logs.size, it.logs.size),
                            it.calories.withThousands(),
                            mealName
                        )
                    } ?: stringResource(resource = Res.string.copy_pick_item),
                    enabled = selected != null,
                    onClick = callbacks::onCopy
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            MealFilter(mealType = viewState.mealType, isAnyMeal = viewState.isAnyMeal, onSelected = callbacks::onAnyMealSelected)
            val meals = viewState.pastMeals
            if (meals.isEmpty()) {
                Text(text = stringResource(resource = Res.string.copy_nothing_recent), style = typography.regular13, color = colors.muted)
            } else {
                meals.forEach { meal ->
                    PastMealCard(
                        meal = meal,
                        viewState = viewState,
                        isSelected = meal == selected,
                        onClick = { callbacks.onPicked(meal = meal) }
                    )
                }
                Text(text = stringResource(resource = Res.string.copy_last_seven_days), style = typography.regular11, color = colors.muted)
            }
        }
    }
}

@Composable
private fun MealFilter(mealType: MealType, isAnyMeal: Boolean, onSelected: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.line)
            .padding(all = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
    ) {
        listOf(false to mealType.pluralTitle, true to Res.string.copy_filter_any).forEach { (any, label) ->
            val isSelected = any == isAnyMeal
            Box(
                modifier = Modifier
                    .height(height = 34.dp)
                    .clip(shape = RoundedCornerShape(size = 9.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .clickable(role = Role.RadioButton) { onSelected(any) }
                    .semantics { selected = isSelected }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = label),
                    style = typography.bold13,
                    color = if (isSelected) colors.ink else colors.muted
                )
            }
        }
    }
}

private val MealType.pluralTitle: StringResource
    get() = when (this) {
        MealType.BREAKFAST -> Res.string.copy_filter_breakfasts
        MealType.LUNCH -> Res.string.copy_filter_lunches
        MealType.DINNER -> Res.string.copy_filter_dinners
        MealType.SNACK -> Res.string.copy_filter_snacks
    }

@Composable
private fun PastMealCard(meal: PastMeal, viewState: CopyFromViewState, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 18.dp)
    val isYesterday = meal.date == viewState.targetDate.minus(period = DatePeriod(days = 1))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = if (isSelected) 1.5.dp else 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { selected = isSelected }
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
            Box(
                modifier = Modifier
                    .size(size = 20.dp)
                    .clip(shape = CircleShape)
                    .background(color = colors.surface)
                    .border(width = if (isSelected) 6.dp else 2.dp, color = if (isSelected) colors.accent else colors.dashed, shape = CircleShape)
            )
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = if (isYesterday) {
                        stringResource(resource = Res.string.copy_yesterday_date, meal.date.toShortDayMonthDay())
                    } else {
                        meal.date.toShortDayMonthDay()
                    },
                    style = typography.bold14,
                    color = colors.ink
                )
                Text(
                    text = stringResource(
                        resource = Res.string.copy_meal_count,
                        stringResource(resource = meal.mealType.title),
                        pluralStringResource(resource = Res.plurals.items, quantity = meal.logs.size, meal.logs.size)
                    ),
                    style = typography.regular11,
                    color = colors.muted
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(space = 3.dp)) {
                Text(text = meal.calories.withThousands(), style = typography.bold16, color = colors.ink, modifier = Modifier.alignByBaseline())
                Text(
                    text = stringResource(resource = Res.string.kcal_text),
                    style = typography.regular11,
                    color = colors.muted,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
        Column(modifier = Modifier.padding(start = 30.dp), verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            meal.logs.forEach { log ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    Text(
                        text = log.foodName + " · " + log.amountText(foodUnit = viewState.foodUnit),
                        style = typography.regular13,
                        color = colors.muted,
                        maxLines = 1,
                        modifier = Modifier.weight(weight = 1f)
                    )
                    Text(text = log.calories.withThousands(), style = typography.regular13, color = colors.muted)
                }
            }
        }
    }
}
