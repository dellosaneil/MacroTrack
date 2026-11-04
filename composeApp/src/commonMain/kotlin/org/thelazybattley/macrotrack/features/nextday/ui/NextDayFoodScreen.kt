package org.thelazybattley.macrotrack.features.nextday.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.datetime.plus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_clock
import macrotrack.composeapp.generated.resources.next_day_amount_eaten
import macrotrack.composeapp.generated.resources.next_day_based_on
import macrotrack.composeapp.generated.resources.next_day_based_on_range
import macrotrack.composeapp.generated.resources.next_day_carbs
import macrotrack.composeapp.generated.resources.next_day_each_dinner
import macrotrack.composeapp.generated.resources.next_day_each_dinner_sub
import macrotrack.composeapp.generated.resources.next_day_energy
import macrotrack.composeapp.generated.resources.next_day_fade_aria
import macrotrack.composeapp.generated.resources.next_day_fade_day
import macrotrack.composeapp.generated.resources.next_day_fade_dinner
import macrotrack.composeapp.generated.resources.next_day_fat_max
import macrotrack.composeapp.generated.resources.next_day_food_history
import macrotrack.composeapp.generated.resources.next_day_food_none
import macrotrack.composeapp.generated.resources.next_day_food_sub
import macrotrack.composeapp.generated.resources.next_day_heads_up_toggle
import macrotrack.composeapp.generated.resources.next_day_heads_up_toggle_all_off
import macrotrack.composeapp.generated.resources.next_day_heads_up_toggle_sub
import macrotrack.composeapp.generated.resources.next_day_how_long
import macrotrack.composeapp.generated.resources.next_day_how_long_sub
import macrotrack.composeapp.generated.resources.next_day_less_unit
import macrotrack.composeapp.generated.resources.next_day_log_dinner
import macrotrack.composeapp.generated.resources.next_day_more_unit
import macrotrack.composeapp.generated.resources.next_day_morning_after
import macrotrack.composeapp.generated.resources.next_day_nights_progress
import macrotrack.composeapp.generated.resources.next_day_no_weigh_in
import macrotrack.composeapp.generated.resources.next_day_not_counted
import macrotrack.composeapp.generated.resources.next_day_not_enough
import macrotrack.composeapp.generated.resources.next_day_not_enough_body
import macrotrack.composeapp.generated.resources.next_day_note_at
import macrotrack.composeapp.generated.resources.next_day_note_estimated
import macrotrack.composeapp.generated.resources.next_day_note_usual
import macrotrack.composeapp.generated.resources.next_day_reminder
import macrotrack.composeapp.generated.resources.next_day_reminder_off
import macrotrack.composeapp.generated.resources.next_day_reminder_on
import macrotrack.composeapp.generated.resources.next_day_weigh_tip
import macrotrack.composeapp.generated.resources.next_day_why_title
import macrotrack.composeapp.generated.resources.next_day_why_title_up
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.core.toShortDayName
import org.thelazybattley.macrotrack.domain.model.DinnerEntry
import org.thelazybattley.macrotrack.domain.model.DinnerNight
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.NEXT_DAY_MIN_NIGHTS
import org.thelazybattley.macrotrack.domain.model.NextDayFood
import org.thelazybattley.macrotrack.domain.model.NextDaySettings
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.model.maxFatGain
import org.thelazybattley.macrotrack.domain.usecase.nextday.NextDayInputs
import org.thelazybattley.macrotrack.features.nextday.NextDayFoodCallbacks
import org.thelazybattley.macrotrack.features.nextday.NextDayFoodViewModel
import org.thelazybattley.macrotrack.features.nextday.NextDayFoodViewState
import org.thelazybattley.macrotrack.features.photos.ui.formatReminderTime
import org.thelazybattley.macrotrack.features.whatfits.ui.WhatFitsBottomButtons
import org.thelazybattley.macrotrack.features.whatfits.ui.WhatFitsScaffold
import org.thelazybattley.macrotrack.features.whatfits.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.CommonSwitch
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.roundToInt

/** One food's next-morning change: by amount, how long it lasts, why, and every dinner it was eaten at. */
@Composable
fun NextDayFoodScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val viewModel = koinViewModel<NextDayFoodViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    NextDayFoodScreen(
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack,
        onOpenHistory = { onNavigate(AppDestinations.Root.FoodHistory.createRoute(foodName = viewState.name, isRecipe = viewState.isRecipe)) },
        onLogDinner = { onNavigate(AppDestinations.Root.AddMeal.createRoute(mealType = MealType.DINNER.name)) },
        onOpenReminders = { onNavigate(AppDestinations.Root.Reminders.route) }
    )
}

@Composable
private fun NextDayFoodScreen(
    viewState: NextDayFoodViewState,
    callbacks: NextDayFoodCallbacks,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit,
    onLogDinner: () -> Unit,
    onOpenReminders: () -> Unit
) {
    val food = viewState.food
    val dinners = food?.nights?.size ?: 0
    WhatFitsScaffold(
        title = viewState.name,
        subtitle = pluralStringResource(resource = Res.plurals.next_day_food_sub, quantity = dinners, dinners, viewState.period.days),
        onBack = onBack,
        bottomBar = {
            if (food?.isReady == true) {
                WhatFitsBottomButtons(
                    secondary = stringResource(resource = Res.string.next_day_food_history),
                    onSecondary = onOpenHistory,
                    primary = stringResource(resource = Res.string.next_day_log_dinner),
                    onPrimary = onLogDinner
                )
            }
        }
    ) {
        if (!viewState.isLoaded) return@WhatFitsScaffold
        if (food == null) {
            Text(
                text = stringResource(resource = Res.string.next_day_food_none, viewState.name, viewState.period.days),
                style = typography.regular13,
                color = colors.muted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 18.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            )
            return@WhatFitsScaffold
        }
        if (food.isReady) {
            ReadyContent(food = food, viewState = viewState, callbacks = callbacks)
        } else {
            CollectingContent(food = food, viewState = viewState, onOpenReminders = onOpenReminders)
        }
    }
}

@Composable
private fun ReadyContent(food: NextDayFood, viewState: NextDayFoodViewState, callbacks: NextDayFoodCallbacks) {
    val amount = viewState.amount ?: return
    val unit = viewState.unit
    val amountText = nextDayAmount(isRecipe = food.isRecipe, unit = unit, amount = amount)
    val amountLong = nextDayAmount(isRecipe = food.isRecipe, unit = unit, amount = amount, isLong = true)
    val estimate = viewState.portion?.average ?: food.estimate(amount = amount) ?: return
    val isUp = estimate > 0
    // Hero: the change at the amount picked.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = if (isUp) colors.dangerSoft else colors.hero)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        val tint = if (isUp) colors.danger else colors.accentInk
        Text(text = stringResource(resource = Res.string.next_day_morning_after), style = typography.regular13, color = colors.ink)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Text(text = abs(estimate).tenths(), style = typography.bold36, color = tint)
            Text(
                text = stringResource(resource = if (estimate >= 0) Res.string.next_day_more_unit else Res.string.next_day_less_unit, weightUnitLabel()),
                style = typography.bold16,
                color = tint,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        val portion = viewState.portion
        val lead = stringResource(
            resource = if (amount == food.usual) Res.string.next_day_note_usual else Res.string.next_day_note_at,
            amountLong
        )
        val basis = when {
            portion == null -> stringResource(resource = Res.string.next_day_note_estimated)
            portion.nights.size > 1 && portion.min != portion.max -> stringResource(
                resource = Res.string.next_day_based_on_range,
                portion.nights.size,
                portion.min.signedTenths(),
                portion.max.signedTenths()
            )
            else -> pluralStringResource(resource = Res.plurals.next_day_based_on, quantity = portion.nights.size, portion.nights.size)
        }
        Text(text = "$lead $basis", style = typography.regular13, color = colors.ink)
        val amountLabel = stringResource(resource = Res.string.next_day_amount_eaten)
        Row(
            modifier = Modifier.semantics { contentDescription = amountLabel }.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            viewState.portions.forEach { option ->
                val isPicked = option.amount == amount
                Column(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 56.dp)
                        .clip(shape = RoundedCornerShape(size = 14.dp))
                        .background(color = if (isPicked) colors.surface else colors.surface.copy(alpha = 0.55f))
                        .border(width = 1.5.dp, color = if (isPicked) tint else Color.Transparent, shape = RoundedCornerShape(size = 14.dp))
                        .selectable(selected = isPicked, role = Role.RadioButton, onClick = { callbacks.onPortionSelected(amount = option.amount) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = nextDayAmount(isRecipe = food.isRecipe, unit = unit, amount = option.amount),
                        style = typography.bold16,
                        color = colors.ink,
                        maxLines = 1
                    )
                    Text(text = option.average.signedTenthsWithUnit(), style = typography.bold11, color = colors.muted, maxLines = 1)
                }
            }
        }
    }
    FadeCard(food = food, amount = amount, amountText = amountText)
    WhyCard(food = food, amount = amount, amountText = amountText, isUp = isUp)
    DinnerRows(food = food, unit = unit, highlight = amount)
    HeadsUpToggle(viewState = viewState, onToggle = callbacks::onHeadsUpToggled)
}

/** The change on the dinner's day and each of the mornings after, at [amount]. */
@Composable
private fun FadeCard(food: NextDayFood, amount: Double, amountText: String) {
    val fade = food.fade(amount = amount)
    val counted = food.portion(amount = amount)?.nights?.size ?: food.counted.size
    val top = maxOf(fade.maxOf { abs(it ?: 0.0) }, 0.1)
    val noWeighIn = stringResource(resource = Res.string.next_day_no_weigh_in)
    val labels = fade.map { it?.signedTenthsWithUnit() ?: noWeighIn }
    val description = stringResource(resource = Res.string.next_day_fade_aria, labels[0], labels[1], labels[2])
    val barUp = colors.fat
    val barDown = colors.accent
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(resource = Res.string.next_day_how_long), style = typography.bold14, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            Text(
                text = pluralStringResource(resource = Res.plurals.next_day_how_long_sub, quantity = counted, amountText, counted),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 120.dp)
                .clearAndSetSemantics { contentDescription = description },
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            (listOf<Double?>(0.0) + fade).forEachIndexed { index, value ->
                Column(
                    modifier = Modifier.weight(weight = 1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(space = 4.dp, alignment = Alignment.Bottom)
                ) {
                    val text = when {
                        index == 0 -> "0"
                        value == null -> "–"
                        else -> value.signedTenths()
                    }
                    Text(
                        text = text,
                        style = typography.bold12,
                        color = when {
                            value == null || index == 0 -> colors.muted
                            value > 0 -> colors.danger
                            else -> colors.accentInk
                        }
                    )
                    val height = if (index == 0 || value == null) 4.dp else (80 * abs(value) / top).coerceAtLeast(minimumValue = 4.0).dp
                    val color = when {
                        index == 0 || value == null -> colors.dashed
                        value > 0 -> barUp.copy(alpha = (0.35 + 0.65 * abs(value) / top).toFloat().coerceIn(0.35f, 1f))
                        else -> barDown
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 44.dp)
                            .height(height = height)
                            .background(color = color, shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().clearAndSetSemantics {}, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            listOf(stringResource(resource = Res.string.next_day_fade_dinner)).plus((1..fade.size).map { stringResource(resource = Res.string.next_day_fade_day, it) })
                .forEach { label ->
                    Text(text = label, style = typography.bold11, color = colors.muted, textAlign = TextAlign.Center, modifier = Modifier.weight(weight = 1f))
                }
        }
    }
}

/** Carbs and calories at [amount], and how little of the jump could be fat. */
@Composable
private fun WhyCard(food: NextDayFood, amount: Double, amountText: String, isUp: Boolean) {
    val nights = food.portion(amount = amount)?.nights ?: food.counted
    // Other amounts scale the average night to the amount picked.
    val scale = if (food.portion(amount = amount) != null) 1.0 else nights.map { it.amount }.average().takeIf { it > 0 }?.let { amount / it } ?: 1.0
    val carbs = nights.map { it.carbs }.average() * scale
    val calories = (nights.map { it.calories }.average() * scale).roundToInt()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Text(
            text = stringResource(resource = if (isUp) Res.string.next_day_why_title_up else Res.string.next_day_why_title, amountText),
            style = typography.bold14,
            color = colors.ink
        )
        listOf(
            Triple(stringResource(resource = Res.string.next_day_carbs), "${carbs.roundToInt()} g", colors.carbs),
            Triple(stringResource(resource = Res.string.next_day_energy), "${calories.withThousands()} kcal", colors.fat)
        ).forEach { (label, value, dot) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
                Box(modifier = Modifier.size(size = 10.dp).background(color = dot, shape = RoundedCornerShape(size = 3.dp)))
                Text(text = label, style = typography.regular13, color = colors.ink, modifier = Modifier.weight(weight = 1f))
                Text(text = value, style = typography.bold14, color = colors.ink)
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        val fat = maxFatGain(calories = calories)
        val fatText = fat.hundredthsWithUnit()
        val line = stringResource(resource = Res.string.next_day_fat_max, calories.withThousands(), fatText)
        Text(text = line.withBold(fatText), style = typography.regular11, color = colors.muted)
    }
}

/** A weight in kg in the user's unit to two decimals with the unit: "0.08 kg". */
@Composable
private fun Double.hundredthsWithUnit(): String {
    val value = LocalUnitSystem.current.weightFromKg(kg = this)
    val hundredths = (abs(value) * 100).roundToInt()
    return "${hundredths / 100}.${(hundredths % 100).toString().padStart(length = 2, padChar = '0')} ${weightUnitLabel()}"
}

/** Every dinner [food] was eaten at, newest first, with the weigh-ins either side; [highlight] marks one amount. */
@Composable
private fun DinnerRows(food: NextDayFood, unit: String, highlight: Double?) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(resource = Res.string.next_day_each_dinner), style = typography.bold12, color = colors.muted, modifier = Modifier.weight(weight = 1f))
        Text(text = stringResource(resource = Res.string.next_day_each_dinner_sub), style = typography.medium11, color = colors.muted)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
    ) {
        food.nights.forEachIndexed { index, night ->
            if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
            DinnerRow(
                night = night,
                isRecipe = food.isRecipe,
                unit = unit,
                isLatest = index == 0 && highlight != null,
                isHighlighted = highlight != null && night.amount == highlight
            )
        }
    }
}

@Composable
private fun DinnerRow(night: DinnerNight, isRecipe: Boolean, unit: String, isLatest: Boolean, isHighlighted: Boolean) {
    val change = night.change
    val before = night.before
    val after = night.nextMorning
    val weights = if (before != null && after != null) {
        "${before.tenths()} → ${after.tenths()} ${weightUnitLabel()}"
    } else {
        val missing = if (before == null) night.date else night.date.plus(period = DatePeriod(days = 1))
        stringResource(resource = Res.string.next_day_not_counted, missing.toShortDayName() + " " + missing.toAbbreviatedMonthDay())
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .background(color = if (isHighlighted) colors.background else Color.Transparent)
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .size(size = 42.dp)
                .background(color = if (isLatest) colors.accent else colors.nightSoft, shape = RoundedCornerShape(size = 12.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 1.dp, alignment = Alignment.CenterVertically)
        ) {
            val ink = if (isLatest) colors.onAccent else colors.nightInk
            Text(text = night.date.day.toString(), style = typography.bold16, color = ink)
            Text(text = night.date.month.name.take(n = 3), style = typography.bold9, color = ink)
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = nextDayAmount(isRecipe = isRecipe, unit = unit, amount = night.amount) + " · " + night.date.toShortDayName(),
                style = typography.bold14,
                color = colors.ink,
                maxLines = 1
            )
            Text(
                text = weights,
                style = if (change == null) typography.bold11 else typography.regular11,
                color = if (change == null) colors.warmInk else colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = change?.signedTenths() ?: "—",
            style = typography.bold14,
            color = when {
                change == null -> colors.muted
                change > 0 -> colors.danger
                else -> colors.accentInk
            },
            modifier = Modifier
                .background(
                    color = when {
                        change == null -> colors.divider
                        change > 0 -> colors.dangerSoft
                        else -> colors.accentSoft
                    },
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun HeadsUpToggle(viewState: NextDayFoodViewState, onToggle: () -> Unit) {
    val title = stringResource(resource = Res.string.next_day_heads_up_toggle)
    val isAllOff = viewState.inputs?.settings?.isHeadsUpOn == false
    val isOn = viewState.inputs?.settings?.mutedFoods?.contains(viewState.key) != true
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
            .clickable(role = Role.Switch, onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = title, style = typography.bold14, color = colors.ink)
            Text(
                text = stringResource(resource = if (isAllOff) Res.string.next_day_heads_up_toggle_all_off else Res.string.next_day_heads_up_toggle_sub),
                style = typography.regular11,
                color = colors.muted
            )
        }
        CommonSwitch(isOn = isOn, description = title, onToggle = onToggle)
    }
}

@Composable
private fun CollectingContent(food: NextDayFood, viewState: NextDayFoodViewState, onOpenReminders: () -> Unit) {
    val counted = food.counted.size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.surface)
            .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 24.dp))
            .padding(all = 18.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Text(text = stringResource(resource = Res.string.next_day_not_enough), style = typography.bold18, color = colors.ink)
        Text(
            text = stringResource(resource = Res.string.next_day_not_enough_body, NEXT_DAY_MIN_NIGHTS, food.name, counted),
            style = typography.regular13,
            color = colors.muted
        )
        val progress = stringResource(resource = Res.string.next_day_nights_progress, counted, NEXT_DAY_MIN_NIGHTS)
        Row(
            modifier = Modifier.clearAndSetSemantics { contentDescription = progress },
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            repeat(times = NEXT_DAY_MIN_NIGHTS) { index ->
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 10.dp)
                        .background(color = if (index < counted) colors.accent else colors.divider, shape = RoundedCornerShape(size = 5.dp))
                )
            }
        }
        Text(text = progress, style = typography.bold12, color = colors.ink)
    }
    DinnerRows(food = food, unit = viewState.unit, highlight = null)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.nightSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_clock),
            contentDescription = null,
            tint = colors.nightInk,
            modifier = Modifier.padding(top = 1.dp).size(size = 18.dp)
        )
        Text(text = stringResource(resource = Res.string.next_day_weigh_tip), style = typography.regular13, color = colors.ink)
    }
    val reminder = viewState.weighInReminder
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
            .clickable(role = Role.Button, onClick = onOpenReminders)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = stringResource(resource = Res.string.next_day_reminder), style = typography.bold14, color = colors.ink)
            Text(
                text = if (reminder?.isOn == true) {
                    stringResource(resource = Res.string.next_day_reminder_on, formatReminderTime(minutes = reminder.minutes))
                } else {
                    stringResource(resource = Res.string.next_day_reminder_off)
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
private fun PreviewNextDayFoodScreen() {
    val today = LocalDate(year = 2026, month = 10, day = 8)
    val nights = listOf(1 to 70.0, 10 to 70.0, 19 to 100.0, 26 to 50.0, 35 to 70.0, 42 to 70.0)
    val entries = nights.map { (back, amount) ->
        DinnerEntry(name = "Carbonara", isRecipe = true, date = LocalDate.fromEpochDays(epochDays = today.toEpochDays() - back), amount = amount, calories = (920 * amount / 100).roundToInt(), carbs = 96 * amount / 100)
    }
    val weights = nights.flatMap { (back, amount) ->
        val day = today.toEpochDays() - back
        listOf(Weight(date = LocalDate.fromEpochDays(epochDays = day), weight = 75.0), Weight(date = LocalDate.fromEpochDays(epochDays = day + 1), weight = 75.0 + amount / 70))
    }.distinctBy { it.date }
    MacroTrackTheme {
        NextDayFoodScreen(
            viewState = NextDayFoodViewState(
                name = "Carbonara",
                isRecipe = true,
                today = today,
                inputs = NextDayInputs(entries = entries, weights = weights, settings = NextDaySettings())
            ),
            callbacks = NextDayFoodCallbacks.default(),
            onBack = {},
            onOpenHistory = {},
            onLogDinner = {},
            onOpenReminders = {}
        )
    }
}
