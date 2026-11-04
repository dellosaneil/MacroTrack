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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import macrotrack.composeapp.generated.resources.next_day_about_kcal
import macrotrack.composeapp.generated.resources.next_day_based_on
import macrotrack.composeapp.generated.resources.next_day_based_on_range
import macrotrack.composeapp.generated.resources.next_day_carbs_from
import macrotrack.composeapp.generated.resources.next_day_dinner_none
import macrotrack.composeapp.generated.resources.next_day_dinner_sub
import macrotrack.composeapp.generated.resources.next_day_dinner_sub_all
import macrotrack.composeapp.generated.resources.next_day_each_dinner_sub
import macrotrack.composeapp.generated.resources.next_day_each_time
import macrotrack.composeapp.generated.resources.next_day_fade_aria
import macrotrack.composeapp.generated.resources.next_day_fade_day
import macrotrack.composeapp.generated.resources.next_day_fade_dinner
import macrotrack.composeapp.generated.resources.next_day_fat_max
import macrotrack.composeapp.generated.resources.next_day_heads_up_toggle
import macrotrack.composeapp.generated.resources.next_day_heads_up_toggle_all_off
import macrotrack.composeapp.generated.resources.next_day_heads_up_toggle_sub
import macrotrack.composeapp.generated.resources.next_day_how_long
import macrotrack.composeapp.generated.resources.next_day_how_long_sub
import macrotrack.composeapp.generated.resources.next_day_in_3_months
import macrotrack.composeapp.generated.resources.next_day_in_year
import macrotrack.composeapp.generated.resources.next_day_kcal
import macrotrack.composeapp.generated.resources.next_day_less_unit
import macrotrack.composeapp.generated.resources.next_day_log_again
import macrotrack.composeapp.generated.resources.next_day_logged_again
import macrotrack.composeapp.generated.resources.next_day_macros
import macrotrack.composeapp.generated.resources.next_day_more_unit
import macrotrack.composeapp.generated.resources.next_day_morning_after
import macrotrack.composeapp.generated.resources.next_day_nights_progress
import macrotrack.composeapp.generated.resources.next_day_no_weigh_in
import macrotrack.composeapp.generated.resources.next_day_not_counted
import macrotrack.composeapp.generated.resources.next_day_not_enough
import macrotrack.composeapp.generated.resources.next_day_not_enough_body
import macrotrack.composeapp.generated.resources.next_day_note_big
import macrotrack.composeapp.generated.resources.next_day_note_light
import macrotrack.composeapp.generated.resources.next_day_note_usual
import macrotrack.composeapp.generated.resources.next_day_reminder
import macrotrack.composeapp.generated.resources.next_day_reminder_off
import macrotrack.composeapp.generated.resources.next_day_reminder_on
import macrotrack.composeapp.generated.resources.next_day_size_big
import macrotrack.composeapp.generated.resources.next_day_size_dinner
import macrotrack.composeapp.generated.resources.next_day_size_group
import macrotrack.composeapp.generated.resources.next_day_size_light
import macrotrack.composeapp.generated.resources.next_day_size_usual
import macrotrack.composeapp.generated.resources.next_day_weigh_tip
import macrotrack.composeapp.generated.resources.next_day_whats_in
import macrotrack.composeapp.generated.resources.next_day_whole_dinner
import macrotrack.composeapp.generated.resources.next_day_whole_macros
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.core.toShortDayName
import org.thelazybattley.macrotrack.domain.model.DinnerFood
import org.thelazybattley.macrotrack.domain.model.DinnerNight
import org.thelazybattley.macrotrack.domain.model.DinnerSize
import org.thelazybattley.macrotrack.domain.model.DinnerSizeGroup
import org.thelazybattley.macrotrack.domain.model.NEXT_DAY_MIN_NIGHTS
import org.thelazybattley.macrotrack.domain.model.NextDayDinner
import org.thelazybattley.macrotrack.domain.model.NextDayPeriod
import org.thelazybattley.macrotrack.domain.model.NextDaySettings
import org.thelazybattley.macrotrack.domain.model.dinnerKey
import org.thelazybattley.macrotrack.domain.model.maxFatGain
import org.thelazybattley.macrotrack.domain.usecase.nextday.NextDayInputs
import org.thelazybattley.macrotrack.features.nextday.NextDayDinnerCallbacks
import org.thelazybattley.macrotrack.features.nextday.NextDayDinnerViewModel
import org.thelazybattley.macrotrack.features.nextday.NextDayDinnerViewState
import org.thelazybattley.macrotrack.features.photos.ui.formatReminderTime
import org.thelazybattley.macrotrack.features.whatfits.ui.WhatFitsScaffold
import org.thelazybattley.macrotrack.features.whatfits.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonSwitch
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.roundToInt

/** One dinner: by size, what's in it, how long it lasts, and every night it was eaten. */
@Composable
fun NextDayDinnerScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val viewModel = koinViewModel<NextDayDinnerViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    NextDayDinnerScreen(
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack,
        onOpenReminders = { onNavigate(AppDestinations.Root.Reminders.route) }
    )
}

internal val DinnerSize.label: StringResource
    get() = when (this) {
        DinnerSize.LIGHT -> Res.string.next_day_size_light
        DinnerSize.USUAL -> Res.string.next_day_size_usual
        DinnerSize.BIG -> Res.string.next_day_size_big
    }

@Composable
private fun NextDayDinnerScreen(
    viewState: NextDayDinnerViewState,
    callbacks: NextDayDinnerCallbacks,
    onBack: () -> Unit,
    onOpenReminders: () -> Unit
) {
    val dinner = viewState.dinner
    val times = dinner?.nights?.size ?: 0
    WhatFitsScaffold(
        title = dinner?.name.orEmpty(),
        subtitle = when (viewState.period) {
            NextDayPeriod.ALL -> pluralStringResource(resource = Res.plurals.next_day_dinner_sub_all, quantity = times, times)
            NextDayPeriod.MONTHS_3 -> pluralStringResource(resource = Res.plurals.next_day_dinner_sub, quantity = times, times, stringResource(resource = Res.string.next_day_in_3_months))
            NextDayPeriod.YEAR -> pluralStringResource(resource = Res.plurals.next_day_dinner_sub, quantity = times, times, stringResource(resource = Res.string.next_day_in_year))
        },
        onBack = onBack,
        bottomBar = {
            if (dinner?.isReady == true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = colors.background)
                        .navigationBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
                ) {
                    CommonPrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(resource = if (viewState.isLoggedAgain) Res.string.next_day_logged_again else Res.string.next_day_log_again),
                        enabled = !viewState.isLoggedAgain,
                        onClick = callbacks::onLogAgain
                    )
                }
            }
        }
    ) {
        if (!viewState.isLoaded) return@WhatFitsScaffold
        if (dinner == null) {
            Text(
                text = stringResource(resource = Res.string.next_day_dinner_none),
                style = typography.regular13,
                color = colors.muted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 18.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            )
            return@WhatFitsScaffold
        }
        val unitOf = { name: String, isRecipe: Boolean -> viewState.unitOf(name = name, isRecipe = isRecipe) }
        val size = viewState.size
        if (dinner.isReady && size != null) {
            SizeHero(dinner = dinner, picked = size, onPick = callbacks::onSizeSelected)
            ContentsCard(foods = dinner.contents(size = size.size), size = size.size, unitOf = unitOf)
            FadeCard(dinner = dinner, size = size)
            NightRows(dinner = dinner, unitOf = unitOf, highlight = size.size)
            HeadsUpToggle(isOn = viewState.isHeadsUpOn, isAllOff = viewState.isHeadsUpAllOff, onToggle = callbacks::onHeadsUpToggled)
        } else {
            CollectingContent(dinner = dinner, viewState = viewState, unitOf = unitOf, onOpenReminders = onOpenReminders)
        }
    }
}

/** The usual change after this dinner at the size picked, and a button for each size it's been eaten at. */
@Composable
private fun SizeHero(dinner: NextDayDinner, picked: DinnerSizeGroup, onPick: (DinnerSize) -> Unit) {
    val average = picked.average
    val isUp = average > 0
    val tint = if (isUp) colors.danger else colors.accentInk
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = if (isUp) colors.dangerSoft else colors.hero)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Text(text = stringResource(resource = Res.string.next_day_morning_after), style = typography.regular13, color = colors.ink)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Text(text = abs(average).tenths(), style = typography.bold36, color = tint)
            Text(
                text = stringResource(resource = if (average >= 0) Res.string.next_day_more_unit else Res.string.next_day_less_unit, weightUnitLabel()),
                style = typography.bold16,
                color = tint,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        val kcal = picked.calories.withThousands()
        val lead = stringResource(
            resource = when (picked.size) {
                DinnerSize.LIGHT -> Res.string.next_day_note_light
                DinnerSize.USUAL -> Res.string.next_day_note_usual
                DinnerSize.BIG -> Res.string.next_day_note_big
            },
            kcal
        )
        val count = picked.nights.size
        val basis = if (count > 1 && picked.min != picked.max) {
            stringResource(resource = Res.string.next_day_based_on_range, count, picked.min.signedTenths(), picked.max.signedTenths())
        } else {
            pluralStringResource(resource = Res.plurals.next_day_based_on, quantity = count, count)
        }
        Text(text = "$lead $basis", style = typography.regular13, color = colors.ink)
        if (dinner.sizes.size > 1) {
            val groupLabel = stringResource(resource = Res.string.next_day_size_group)
            Row(
                modifier = Modifier.semantics { contentDescription = groupLabel }.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
            ) {
                dinner.sizes.forEach { option ->
                    val isPicked = option.size == picked.size
                    Column(
                        modifier = Modifier
                            .weight(weight = 1f)
                            .heightIn(min = 72.dp)
                            .clip(shape = RoundedCornerShape(size = 14.dp))
                            .background(color = if (isPicked) colors.surface else colors.surface.copy(alpha = 0.55f))
                            .border(width = 1.5.dp, color = if (isPicked) tint else Color.Transparent, shape = RoundedCornerShape(size = 14.dp))
                            .selectable(selected = isPicked, role = Role.RadioButton, onClick = { onPick(option.size) })
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(space = 1.dp, alignment = Alignment.CenterVertically)
                    ) {
                        Text(text = stringResource(resource = option.size.label), style = typography.bold13, color = colors.ink)
                        Text(
                            text = stringResource(resource = Res.string.next_day_about_kcal, option.calories.withThousands()),
                            style = typography.regular11,
                            color = colors.muted
                        )
                        Text(
                            text = option.average.signedTenths(),
                            style = typography.bold15,
                            color = if (option.average > 0) colors.danger else colors.accentInk
                        )
                    }
                }
            }
        }
    }
}

/** The colour of a food's stripe: whichever macro gives it most of its energy. */
@Composable
private fun DinnerFood.stripeColor(): Color {
    val fromProtein = protein * 4
    val fromCarbs = carbs * 4
    val fromFat = fat * 9
    return when {
        fromCarbs >= fromProtein && fromCarbs >= fromFat -> colors.carbs
        fromProtein >= fromFat -> colors.protein
        else -> colors.fat
    }
}

/** Each food at this size with its macros, the whole dinner, and the most it could be as fat. */
@Composable
private fun ContentsCard(foods: List<DinnerFood>, size: DinnerSize, unitOf: (String, Boolean) -> String) {
    val calories = foods.sumOf { it.calories }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
    ) {
        Row(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = stringResource(resource = Res.string.next_day_whats_in), style = typography.bold14, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            Text(
                text = stringResource(resource = Res.string.next_day_size_dinner, stringResource(resource = size.label)),
                style = typography.regular11,
                color = colors.muted
            )
        }
        foods.forEach { food ->
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
            ) {
                Box(modifier = Modifier.width(width = 4.dp).height(height = 34.dp).background(color = food.stripeColor(), shape = RoundedCornerShape(size = 2.dp)))
                Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
                    val amount = if (food.isQuickAdd) null else nextDayAmount(isRecipe = food.isRecipe, unit = unitOf(food.name, food.isRecipe), amount = food.amount.roundToInt().toDouble())
                    Text(
                        text = food.name + (amount?.let { " · $it" } ?: ""),
                        style = typography.bold14,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(resource = Res.string.next_day_macros, food.protein.roundToInt(), food.carbs.roundToInt(), food.fat.roundToInt()),
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = food.calories.withThousands(), style = typography.bold15, color = colors.ink)
                    Text(text = stringResource(resource = Res.string.next_day_kcal), style = typography.regular10, color = colors.muted)
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        Row(
            modifier = Modifier.fillMaxWidth().background(color = colors.background).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
        ) {
            Text(text = stringResource(resource = Res.string.next_day_whole_dinner), style = typography.bold14, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            Text(
                text = stringResource(
                    resource = Res.string.next_day_whole_macros,
                    foods.sumOf { it.protein }.roundToInt(),
                    foods.sumOf { it.carbs }.roundToInt(),
                    foods.sumOf { it.fat }.roundToInt()
                ),
                style = typography.regular11,
                color = colors.muted
            )
            Text(text = calories.withThousands(), style = typography.bold15, color = colors.ink)
        }
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        val fat = maxFatGain(calories = calories).hundredthsWithUnit()
        val carbsFrom = foods.takeIf { it.size > 1 }?.maxByOrNull { it.carbs }?.takeIf { it.carbs > 0 }
        val line = listOfNotNull(
            carbsFrom?.let { stringResource(resource = Res.string.next_day_carbs_from, it.name) },
            stringResource(resource = Res.string.next_day_fat_max, calories.withThousands(), fat)
        ).joinToString(separator = " ")
        Text(
            text = line.withBold(fat),
            style = typography.regular11,
            color = colors.muted,
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 14.dp)
        )
    }
}

/** A weight in kg in the user's unit to two decimals with the unit: "0.15 kg". */
@Composable
private fun Double.hundredthsWithUnit(): String {
    val value = LocalUnitSystem.current.weightFromKg(kg = this)
    val hundredths = (abs(value) * 100).roundToInt()
    return "${hundredths / 100}.${(hundredths % 100).toString().padStart(length = 2, padChar = '0')} ${weightUnitLabel()}"
}

/** The change on the dinner's day and each of the mornings after, at the size picked. */
@Composable
private fun FadeCard(dinner: NextDayDinner, size: DinnerSizeGroup) {
    val fade = dinner.fade(size = size.size)
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
                text = pluralStringResource(resource = Res.plurals.next_day_how_long_sub, quantity = size.nights.size, stringResource(resource = size.size.label), size.nights.size),
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
                    Text(
                        text = when {
                            index == 0 -> "0"
                            value == null -> "–"
                            else -> value.signedTenths()
                        },
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
            (listOf(stringResource(resource = Res.string.next_day_fade_dinner)) + (1..fade.size).map { stringResource(resource = Res.string.next_day_fade_day, it) })
                .forEach { label ->
                    Text(text = label, style = typography.bold11, color = colors.muted, textAlign = TextAlign.Center, modifier = Modifier.weight(weight = 1f))
                }
        }
    }
}

/** Every night this dinner was eaten, newest first, with the weigh-ins either side; [highlight] marks one size. */
@Composable
private fun NightRows(dinner: NextDayDinner, unitOf: (String, Boolean) -> String, highlight: DinnerSize?) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(resource = Res.string.next_day_each_time), style = typography.bold12, color = colors.muted, modifier = Modifier.weight(weight = 1f))
        Text(text = stringResource(resource = Res.string.next_day_each_dinner_sub), style = typography.medium11, color = colors.muted)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
    ) {
        dinner.nights.forEachIndexed { index, night ->
            if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
            NightRow(
                night = night,
                unitOf = unitOf,
                isLatest = index == 0 && highlight != null,
                isHighlighted = highlight != null && night.isCounted && dinner.sizeOf(calories = night.calories) == highlight
            )
        }
    }
}

@Composable
private fun NightRow(night: DinnerNight, unitOf: (String, Boolean) -> String, isLatest: Boolean, isHighlighted: Boolean) {
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
                text = nightAmounts(items = night.items, unitOf = unitOf),
                style = typography.bold14,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = night.date.toShortDayName() + " · " + weights,
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
private fun HeadsUpToggle(isOn: Boolean, isAllOff: Boolean, onToggle: () -> Unit) {
    val title = stringResource(resource = Res.string.next_day_heads_up_toggle)
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
private fun CollectingContent(
    dinner: NextDayDinner,
    viewState: NextDayDinnerViewState,
    unitOf: (String, Boolean) -> String,
    onOpenReminders: () -> Unit
) {
    val counted = dinner.counted.size
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
            text = stringResource(resource = Res.string.next_day_not_enough_body, NEXT_DAY_MIN_NIGHTS, counted),
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
    NightRows(dinner = dinner, unitOf = unitOf, highlight = null)
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
private fun PreviewNextDayDinnerScreen() {
    val today = LocalDate(year = 2026, month = 10, day = 8)
    val (items, weights) = previewDinners(today = today, foods = listOf("Chicken wings" to 720, "Rice" to 325), daysAgo = listOf(2, 9, 16, 23), change = 1.1)
    MacroTrackTheme {
        NextDayDinnerScreen(
            viewState = NextDayDinnerViewState(
                key = dinnerKey(items = items.filter { it.date == items.first().date }),
                today = today,
                inputs = NextDayInputs(items = items, weights = weights, settings = NextDaySettings())
            ),
            callbacks = NextDayDinnerCallbacks.default(),
            onBack = {},
            onOpenReminders = {}
        )
    }
}
