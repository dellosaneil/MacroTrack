package org.thelazybattley.macrotrack.features.nextday.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_droplet
import macrotrack.composeapp.generated.resources.next_day_biggest_after
import macrotrack.composeapp.generated.resources.next_day_biggest_label
import macrotrack.composeapp.generated.resources.next_day_days
import macrotrack.composeapp.generated.resources.next_day_dinner_foods
import macrotrack.composeapp.generated.resources.next_day_empty
import macrotrack.composeapp.generated.resources.next_day_heads_up_all
import macrotrack.composeapp.generated.resources.next_day_heads_up_all_sub
import macrotrack.composeapp.generated.resources.next_day_higher
import macrotrack.composeapp.generated.resources.next_day_how_counted
import macrotrack.composeapp.generated.resources.next_day_lower
import macrotrack.composeapp.generated.resources.next_day_nights_counted
import macrotrack.composeapp.generated.resources.next_day_no_jump_body
import macrotrack.composeapp.generated.resources.next_day_no_jump_title
import macrotrack.composeapp.generated.resources.next_day_period
import macrotrack.composeapp.generated.resources.next_day_row_aria
import macrotrack.composeapp.generated.resources.next_day_row_aria_waiting
import macrotrack.composeapp.generated.resources.next_day_row_sub
import macrotrack.composeapp.generated.resources.next_day_row_waiting
import macrotrack.composeapp.generated.resources.next_day_seen_on
import macrotrack.composeapp.generated.resources.next_day_sort
import macrotrack.composeapp.generated.resources.next_day_sort_lowers
import macrotrack.composeapp.generated.resources.next_day_sort_raises
import macrotrack.composeapp.generated.resources.next_day_subtitle
import macrotrack.composeapp.generated.resources.next_day_title
import macrotrack.composeapp.generated.resources.next_day_usual_dinner
import macrotrack.composeapp.generated.resources.next_day_water_body
import macrotrack.composeapp.generated.resources.next_day_water_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.DinnerEntry
import org.thelazybattley.macrotrack.domain.model.NEXT_DAY_HEADS_UP_KG
import org.thelazybattley.macrotrack.domain.model.NEXT_DAY_MIN_NIGHTS
import org.thelazybattley.macrotrack.domain.model.NextDayFood
import org.thelazybattley.macrotrack.domain.model.NextDayOverview
import org.thelazybattley.macrotrack.domain.model.NextDayPeriod
import org.thelazybattley.macrotrack.domain.model.NextDaySettings
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.usecase.nextday.NextDayInputs
import org.thelazybattley.macrotrack.features.nextday.NextDayWeightCallbacks
import org.thelazybattley.macrotrack.features.nextday.NextDayWeightViewModel
import org.thelazybattley.macrotrack.features.nextday.NextDayWeightViewState
import org.thelazybattley.macrotrack.features.whatfits.ui.WhatFitsScaffold
import org.thelazybattley.macrotrack.ui.common.CommonSwitch
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs

/** A bar this long, in kg, fills its half of the row. */
private const val BAR_FULL_KG = 1.4

/** Dinner foods ranked by what they do to the next morning's weigh-in. */
@Composable
fun NextDayWeightScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val viewModel = koinViewModel<NextDayWeightViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    NextDayWeightScreen(
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack,
        onOpenFood = { food -> onNavigate(AppDestinations.Root.NextDayFood.createRoute(foodName = food.name, isRecipe = food.isRecipe)) }
    )
}

@Composable
private fun NextDayWeightScreen(
    viewState: NextDayWeightViewState,
    callbacks: NextDayWeightCallbacks,
    onBack: () -> Unit,
    onOpenFood: (NextDayFood) -> Unit
) {
    WhatFitsScaffold(
        title = stringResource(resource = Res.string.next_day_title),
        subtitle = stringResource(resource = Res.string.next_day_subtitle),
        onBack = onBack
    ) {
        val overview = viewState.overview ?: return@WhatFitsScaffold
        NextDaySegmented(
            label = stringResource(resource = Res.string.next_day_period),
            options = NextDayPeriod.entries.map { stringResource(resource = Res.string.next_day_days, it.days) },
            selected = NextDayPeriod.entries.indexOf(viewState.period),
            onSelect = { callbacks.onPeriodSelected(period = NextDayPeriod.entries[it]) }
        )
        BiggestJumpCard(overview = overview, viewState = viewState)
        if (overview.foods.isEmpty()) {
            Text(
                text = stringResource(resource = Res.string.next_day_empty, viewState.period.days),
                style = typography.regular13,
                color = colors.muted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 18.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(resource = Res.string.next_day_dinner_foods),
                    style = typography.bold15,
                    color = colors.ink,
                    modifier = Modifier.weight(weight = 1f)
                )
                NextDaySegmented(
                    modifier = Modifier.width(width = 190.dp),
                    label = stringResource(resource = Res.string.next_day_sort),
                    options = listOf(
                        stringResource(resource = Res.string.next_day_sort_raises),
                        stringResource(resource = Res.string.next_day_sort_lowers)
                    ),
                    selected = if (viewState.isRaisesFirst) 0 else 1,
                    height = 34,
                    onSelect = { callbacks.onSortSelected(isRaisesFirst = it == 0) }
                )
            }
            FoodList(foods = overview.sorted(raisesFirst = viewState.isRaisesFirst), viewState = viewState, onOpenFood = onOpenFood)
        }
        WaterNote()
        Text(
            text = stringResource(resource = Res.string.next_day_how_counted, NEXT_DAY_MIN_NIGHTS),
            style = typography.regular11,
            color = colors.muted,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        HeadsUpSetting(isOn = viewState.isHeadsUpOn, onToggle = callbacks::onHeadsUpToggled)
    }
}

/** Segmented buttons, one picked; [label] names the group for screen readers. */
@Composable
internal fun NextDaySegmented(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    selected: Int,
    height: Int = 40,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.divider)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
            .padding(all = 4.dp)
            .semantics { contentDescription = label }
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = height.dp)
                    .then(if (isSelected) Modifier.shadow(elevation = 1.dp, shape = RoundedCornerShape(size = 10.dp)) else Modifier)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center
            ) {
                Text(text = option, style = typography.bold13, color = if (isSelected) colors.ink else colors.muted, maxLines = 1)
            }
        }
    }
}

/** The food that raises the next morning's weight most, with the usual change after any dinner. */
@Composable
private fun BiggestJumpCard(overview: NextDayOverview, viewState: NextDayWeightViewState) {
    val biggest = overview.biggest
    val shape = RoundedCornerShape(size = 24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = if (biggest != null) colors.dangerSoft else colors.surface)
            .then(if (biggest == null) Modifier.border(width = 1.5.dp, color = colors.dashed, shape = shape) else Modifier)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        val average = biggest?.average
        if (biggest != null && average != null) {
            Text(text = stringResource(resource = Res.string.next_day_biggest_label), style = typography.bold11, color = colors.danger)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                Text(text = average.signedTenths(), style = typography.bold36, color = colors.danger)
                Text(
                    text = weightUnitLabel(),
                    style = typography.bold16,
                    color = colors.danger,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            val usual = biggest.usual ?: 0.0
            val amount = nextDayAmount(
                isRecipe = biggest.isRecipe,
                unit = viewState.unitOf(name = biggest.name, isRecipe = biggest.isRecipe),
                amount = usual,
                isLong = true
            )
            val portion = biggest.portion(amount = usual)
            val seen = (portion?.changes ?: biggest.counted.mapNotNull { it.change }).count { it > 0 }
            val total = portion?.nights?.size ?: biggest.counted.size
            val sentence = stringResource(resource = Res.string.next_day_biggest_after, biggest.name, amount) + " " +
                pluralStringResource(resource = Res.plurals.next_day_seen_on, quantity = total, seen, total)
            Text(text = sentence.withBold(biggest.name, amount), style = typography.regular13, color = colors.ink)
        } else {
            Text(text = stringResource(resource = Res.string.next_day_no_jump_title), style = typography.bold18, color = colors.ink)
            Text(
                text = stringResource(resource = Res.string.next_day_no_jump_body, NEXT_DAY_MIN_NIGHTS),
                style = typography.regular13,
                color = colors.muted
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            StatTile(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = Res.string.next_day_usual_dinner),
                value = overview.usual?.signedTenthsWithUnit() ?: "–",
                background = if (biggest != null) colors.surface else colors.background
            )
            StatTile(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = Res.string.next_day_nights_counted),
                value = overview.nights.toString(),
                background = if (biggest != null) colors.surface else colors.background
            )
        }
    }
}

@Composable
internal fun StatTile(modifier: Modifier = Modifier, label: String, value: String, background: Color = colors.surface) {
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = background)
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(text = label, style = typography.bold11, color = colors.muted, maxLines = 1)
        Text(text = value, style = typography.bold16, color = colors.ink, maxLines = 1)
    }
}

@Composable
private fun FoodList(foods: List<NextDayFood>, viewState: NextDayWeightViewState, onOpenFood: (NextDayFood) -> Unit) {
    // The longest bar sets the scale, so a big jump never runs off the row.
    val full = maxOf(BAR_FULL_KG, foods.maxOf { abs(it.average ?: 0.0) })
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics {}
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 6.dp)
        ) {
            Box(modifier = Modifier.weight(weight = 1f))
            Row(modifier = Modifier.width(width = 120.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(resource = Res.string.next_day_lower), style = typography.bold10, color = colors.muted)
                Text(text = stringResource(resource = Res.string.next_day_higher), style = typography.bold10, color = colors.muted)
            }
            Box(modifier = Modifier.width(width = 76.dp))
        }
        foods.forEachIndexed { index, food ->
            if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
            FoodRow(food = food, unit = viewState.unitOf(name = food.name, isRecipe = food.isRecipe), full = full, onClick = { onOpenFood(food) })
        }
    }
}

@Composable
private fun FoodRow(food: NextDayFood, unit: String, full: Double, onClick: () -> Unit) {
    val average = food.average
    val usual = food.usual
    val amount = usual?.let { nextDayAmount(isRecipe = food.isRecipe, unit = unit, amount = it, isLong = true) }.orEmpty()
    val counted = food.counted.size
    val description = if (average != null) {
        stringResource(resource = Res.string.next_day_row_aria, food.name, amount, average.signedTenthsWithUnit(), counted)
    } else {
        stringResource(resource = Res.string.next_day_row_aria_waiting, food.name, counted, NEXT_DAY_MIN_NIGHTS)
    }
    val upColor = colors.fat
    val downColor = colors.accent
    val lineColor = colors.dashed
    val waitingColor = colors.faint
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = description }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
            Text(text = food.name, style = typography.bold14, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (average != null) {
                Text(
                    text = pluralStringResource(resource = Res.plurals.next_day_row_sub, quantity = counted, amount, counted),
                    style = typography.regular11,
                    color = colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = stringResource(resource = Res.string.next_day_row_waiting, counted, NEXT_DAY_MIN_NIGHTS),
                    style = typography.bold11,
                    color = colors.warmInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Canvas(modifier = Modifier.width(width = 120.dp).height(height = 22.dp)) {
            val center = size.width / 2
            drawLine(color = lineColor, start = Offset(x = center, y = 0f), end = Offset(x = center, y = size.height), strokeWidth = 1.5.dp.toPx())
            if (average == null) {
                drawLine(
                    color = waitingColor,
                    start = Offset(x = size.width * 0.3f, y = size.height / 2),
                    end = Offset(x = size.width * 0.7f, y = size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 3.dp.toPx()))
                )
            } else {
                val width = (center * (abs(average) / full).toFloat()).coerceIn(minimumValue = 2.dp.toPx(), maximumValue = center)
                val barHeight = 12.dp.toPx()
                drawRoundRect(
                    color = if (average > 0) upColor else downColor,
                    topLeft = Offset(x = if (average > 0) center else center - width, y = (size.height - barHeight) / 2),
                    size = Size(width = width, height = barHeight),
                    cornerRadius = CornerRadius(x = 4.dp.toPx())
                )
            }
        }
        Text(
            text = average?.signedTenthsWithUnit() ?: "—",
            style = typography.bold15,
            color = when {
                average == null -> colors.faint
                average > 0 -> colors.danger
                else -> colors.accentInk
            },
            maxLines = 1,
            modifier = Modifier.width(width = 66.dp),
            textAlign = TextAlign.End
        )
    }
}

/** Why a jump the morning after isn't fat: in kg or lb, by the user's units. */
@Composable
internal fun WaterNote() {
    val isImperial = LocalUnitSystem.current == UnitSystem.IMPERIAL
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.nightSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_droplet),
            contentDescription = null,
            tint = colors.nightInk,
            modifier = Modifier.padding(top = 1.dp).size(size = 18.dp)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(stringResource(resource = Res.string.next_day_water_title)) }
                append(" ")
                append(stringResource(resource = Res.string.next_day_water_body, weightUnitLabel(), if (isImperial) "3,500" else "7,700"))
            },
            style = typography.regular13,
            color = colors.ink
        )
    }
}

@Composable
private fun HeadsUpSetting(isOn: Boolean, onToggle: () -> Unit) {
    val title = stringResource(resource = Res.string.next_day_heads_up_all)
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
                text = stringResource(resource = Res.string.next_day_heads_up_all_sub, NEXT_DAY_HEADS_UP_KG.tenths() + " " + weightUnitLabel()),
                style = typography.regular11,
                color = colors.muted
            )
        }
        CommonSwitch(isOn = isOn, description = title, onToggle = onToggle)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewNextDayWeightScreen() {
    val today = LocalDate(year = 2026, month = 10, day = 8)
    val weights = (0..40).map { Weight(date = LocalDate.fromEpochDays(epochDays = today.toEpochDays() - it), weight = 74.2 + it * 0.04 + if (it % 7 == 0) -0.9 else 0.0) }
    val entries = (0..5).map { DinnerEntry(name = "Carbonara", isRecipe = true, date = LocalDate.fromEpochDays(epochDays = today.toEpochDays() - 1 - it * 7), amount = 70.0, calories = 644, carbs = 67.0) } +
        (0..3).map { DinnerEntry(name = "Chicken breast", isRecipe = false, date = LocalDate.fromEpochDays(epochDays = today.toEpochDays() - 3 - it * 5), amount = 150.0, calories = 248, carbs = 0.0) }
    MacroTrackTheme {
        NextDayWeightScreen(
            viewState = NextDayWeightViewState(today = today, inputs = NextDayInputs(entries = entries, weights = weights, settings = NextDaySettings())),
            callbacks = NextDayWeightCallbacks.default(),
            onBack = {},
            onOpenFood = {}
        )
    }
}
