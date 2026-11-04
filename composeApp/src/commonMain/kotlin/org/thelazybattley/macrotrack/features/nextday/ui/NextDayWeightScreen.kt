package org.thelazybattley.macrotrack.features.nextday.ui

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.ic_droplet
import macrotrack.composeapp.generated.resources.ic_search
import macrotrack.composeapp.generated.resources.next_day_biggest_body
import macrotrack.composeapp.generated.resources.next_day_biggest_label
import macrotrack.composeapp.generated.resources.next_day_empty
import macrotrack.composeapp.generated.resources.next_day_even
import macrotrack.composeapp.generated.resources.next_day_heads_up_all
import macrotrack.composeapp.generated.resources.next_day_heads_up_all_sub
import macrotrack.composeapp.generated.resources.next_day_how_counted
import macrotrack.composeapp.generated.resources.next_day_lowers
import macrotrack.composeapp.generated.resources.next_day_lowers_hint
import macrotrack.composeapp.generated.resources.next_day_lowers_most
import macrotrack.composeapp.generated.resources.next_day_nights_weighed
import macrotrack.composeapp.generated.resources.next_day_no_jump_body
import macrotrack.composeapp.generated.resources.next_day_no_jump_title
import macrotrack.composeapp.generated.resources.next_day_period
import macrotrack.composeapp.generated.resources.next_day_period_3_months
import macrotrack.composeapp.generated.resources.next_day_period_all
import macrotrack.composeapp.generated.resources.next_day_period_year
import macrotrack.composeapp.generated.resources.next_day_raises
import macrotrack.composeapp.generated.resources.next_day_raises_hint
import macrotrack.composeapp.generated.resources.next_day_raises_most
import macrotrack.composeapp.generated.resources.next_day_search
import macrotrack.composeapp.generated.resources.next_day_see_all
import macrotrack.composeapp.generated.resources.next_day_see_in_all
import macrotrack.composeapp.generated.resources.next_day_split_aria
import macrotrack.composeapp.generated.resources.next_day_subtitle
import macrotrack.composeapp.generated.resources.next_day_tile_aria
import macrotrack.composeapp.generated.resources.next_day_title
import macrotrack.composeapp.generated.resources.next_day_tracked
import macrotrack.composeapp.generated.resources.next_day_waiting
import macrotrack.composeapp.generated.resources.next_day_water_body
import macrotrack.composeapp.generated.resources.next_day_water_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.DinnerEffect
import org.thelazybattley.macrotrack.domain.model.DinnerItem
import org.thelazybattley.macrotrack.domain.model.NEXT_DAY_HEADS_UP_KG
import org.thelazybattley.macrotrack.domain.model.NEXT_DAY_LOWERS_KG
import org.thelazybattley.macrotrack.domain.model.NEXT_DAY_MIN_NIGHTS
import org.thelazybattley.macrotrack.domain.model.NEXT_DAY_RAISES_KG
import org.thelazybattley.macrotrack.domain.model.NextDayDinner
import org.thelazybattley.macrotrack.domain.model.NextDayOverview
import org.thelazybattley.macrotrack.domain.model.NextDayPeriod
import org.thelazybattley.macrotrack.domain.model.NextDaySettings
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.usecase.nextday.NextDayInputs
import org.thelazybattley.macrotrack.features.nextday.NextDayWeightCallbacks
import org.thelazybattley.macrotrack.features.nextday.NextDayWeightViewModel
import org.thelazybattley.macrotrack.features.nextday.NextDayWeightViewState
import org.thelazybattley.macrotrack.features.whatfits.ui.WhatFitsIconButton
import org.thelazybattley.macrotrack.features.whatfits.ui.WhatFitsScaffold
import org.thelazybattley.macrotrack.ui.common.CommonSwitch
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.thelazybattley.macrotrack.ui.common.weightUnitLabel
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Each list on the summary shows only this many; the rest are in All dinners. */
private const val TOP_COUNT = 3

/** The summary of what each dinner does to the next morning's weigh-in. */
@Composable
fun NextDayWeightScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val viewModel = koinViewModel<NextDayWeightViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    NextDayWeightScreen(
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack,
        onOpenDinner = { dinner -> onNavigate(AppDestinations.Root.NextDayDinner.createRoute(key = dinner.key, period = viewState.period.name)) },
        onOpenAll = { filter -> onNavigate(AppDestinations.Root.NextDayAll.createRoute(filter = filter?.name, period = viewState.period.name)) }
    )
}

@Composable
private fun NextDayWeightScreen(
    viewState: NextDayWeightViewState,
    callbacks: NextDayWeightCallbacks,
    onBack: () -> Unit,
    onOpenDinner: (NextDayDinner) -> Unit,
    onOpenAll: (DinnerEffect?) -> Unit
) {
    WhatFitsScaffold(
        title = stringResource(resource = Res.string.next_day_title),
        subtitle = stringResource(resource = Res.string.next_day_subtitle),
        onBack = onBack,
        trailing = {
            WhatFitsIconButton(
                icon = Res.drawable.ic_search,
                description = stringResource(resource = Res.string.next_day_search),
                onClick = { onOpenAll(null) }
            )
        }
    ) {
        val overview = viewState.overview ?: return@WhatFitsScaffold
        NextDaySegmented(
            label = stringResource(resource = Res.string.next_day_period),
            options = NextDayPeriod.entries.map { stringResource(resource = it.label) },
            selected = NextDayPeriod.entries.indexOf(viewState.period),
            onSelect = { callbacks.onPeriodSelected(period = NextDayPeriod.entries[it]) }
        )
        BiggestJumpCard(biggest = overview.biggest, viewState = viewState)
        if (overview.dinners.isEmpty()) {
            Text(
                text = stringResource(resource = Res.string.next_day_empty),
                style = typography.regular13,
                color = colors.muted,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 18.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            )
        } else {
            SplitCard(overview = overview, onOpenAll = onOpenAll)
            val unitOf = { name: String, isRecipe: Boolean -> viewState.unitOf(name = name, isRecipe = isRecipe) }
            TopSection(
                title = stringResource(resource = Res.string.next_day_raises_most),
                hint = stringResource(resource = Res.string.next_day_raises_hint, NEXT_DAY_RAISES_KG.tenths() + " " + weightUnitLabel()),
                dinners = overview.raises,
                unitOf = unitOf,
                onOpenDinner = onOpenDinner,
                onSeeAll = { onOpenAll(DinnerEffect.RAISES) }
            )
            TopSection(
                title = stringResource(resource = Res.string.next_day_lowers_most),
                hint = stringResource(resource = Res.string.next_day_lowers_hint, NEXT_DAY_LOWERS_KG.tenths() + " " + weightUnitLabel()),
                dinners = overview.lowers,
                unitOf = unitOf,
                onOpenDinner = onOpenDinner,
                onSeeAll = { onOpenAll(DinnerEffect.LOWERS) }
            )
        }
        WaterNote()
        Text(
            text = stringResource(
                resource = Res.string.next_day_how_counted,
                NEXT_DAY_RAISES_KG.tenths() + " " + weightUnitLabel(),
                NEXT_DAY_LOWERS_KG.tenths() + " " + weightUnitLabel()
            ),
            style = typography.regular11,
            color = colors.muted,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        HeadsUpSetting(isOn = viewState.isHeadsUpOn, onToggle = callbacks::onHeadsUpToggled)
    }
}

internal val NextDayPeriod.label: StringResource
    get() = when (this) {
        NextDayPeriod.MONTHS_3 -> Res.string.next_day_period_3_months
        NextDayPeriod.YEAR -> Res.string.next_day_period_year
        NextDayPeriod.ALL -> Res.string.next_day_period_all
    }

internal val DinnerEffect.label: StringResource
    get() = when (this) {
        DinnerEffect.RAISES -> Res.string.next_day_raises
        DinnerEffect.LOWERS -> Res.string.next_day_lowers
        DinnerEffect.EVEN -> Res.string.next_day_even
        DinnerEffect.WAITING -> Res.string.next_day_waiting
    }

/** The colour that marks dinners with this effect. */
@Composable
internal fun DinnerEffect.color(): Color = when (this) {
    DinnerEffect.RAISES -> colors.fat
    DinnerEffect.LOWERS -> colors.accent
    DinnerEffect.EVEN -> colors.faint
    DinnerEffect.WAITING -> colors.dashed
}

/** A small square in an effect's colour; Waiting is a dotted outline. */
@Composable
internal fun EffectDot(effect: DinnerEffect, size: Int = 10) {
    val shape = RoundedCornerShape(size = 3.dp)
    Box(
        modifier = Modifier
            .size(size = size.dp)
            .then(
                if (effect == DinnerEffect.WAITING) {
                    Modifier.border(width = 1.5.dp, color = colors.faint, shape = shape)
                } else {
                    Modifier.background(color = effect.color(), shape = shape)
                }
            )
    )
}

/**
 * Segmented buttons, one picked; [label] names the group for screen readers. [isCompact] sizes each button to its
 * text, with a little room either side, instead of sharing the full width.
 */
@Composable
internal fun NextDaySegmented(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    selected: Int,
    height: Int = 40,
    isCompact: Boolean = false,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = modifier
            .then(if (isCompact) Modifier else Modifier.fillMaxWidth())
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
                    .then(if (isCompact) Modifier else Modifier.weight(weight = 1f))
                    .height(height = height.dp)
                    .then(if (isSelected) Modifier.shadow(elevation = 1.dp, shape = RoundedCornerShape(size = 10.dp)) else Modifier)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(index) })
                    .padding(horizontal = if (isCompact) 10.dp else 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = option, style = typography.bold13, color = if (isSelected) colors.ink else colors.muted, maxLines = 1)
            }
        }
    }
}

/** The dinner that raises the next morning's weight most, or what it takes to find one. */
@Composable
private fun BiggestJumpCard(biggest: NextDayDinner?, viewState: NextDayWeightViewState) {
    val average = biggest?.average
    if (biggest == null || average == null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 24.dp))
                .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 24.dp))
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Text(text = stringResource(resource = Res.string.next_day_no_jump_title), style = typography.bold18, color = colors.ink)
            Text(text = stringResource(resource = Res.string.next_day_no_jump_body, NEXT_DAY_MIN_NIGHTS), style = typography.regular13, color = colors.muted)
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.dangerSoft)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(text = stringResource(resource = Res.string.next_day_biggest_label), style = typography.bold11, color = colors.danger)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Text(text = average.signedTenths(), style = typography.bold36, color = colors.danger)
            Text(text = weightUnitLabel(), style = typography.bold16, color = colors.danger, modifier = Modifier.padding(bottom = 6.dp))
        }
        val amounts = foodAmounts(foods = biggest.usualFoods, unitOf = { name, isRecipe -> viewState.unitOf(name = name, isRecipe = isRecipe) })
        val nights = biggest.counted.size
        val sentence = pluralStringResource(resource = Res.plurals.next_day_biggest_body, quantity = nights, biggest.name, amounts, nights)
        Text(text = sentence.withBold(biggest.name), style = typography.regular13, color = colors.ink)
    }
}

/** How many dinners there are and how they split by effect, with a tile to open each group. */
@Composable
private fun SplitCard(overview: NextDayOverview, onOpenAll: (DinnerEffect) -> Unit) {
    val order = listOf(DinnerEffect.RAISES, DinnerEffect.EVEN, DinnerEffect.LOWERS, DinnerEffect.WAITING)
    val splitDescription = stringResource(
        resource = Res.string.next_day_split_aria,
        overview.count(effect = DinnerEffect.RAISES),
        overview.count(effect = DinnerEffect.EVEN),
        overview.count(effect = DinnerEffect.LOWERS),
        overview.count(effect = DinnerEffect.WAITING)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = pluralStringResource(resource = Res.plurals.next_day_tracked, quantity = overview.dinners.size, overview.dinners.size),
                style = typography.bold14,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            Text(
                text = pluralStringResource(resource = Res.plurals.next_day_nights_weighed, quantity = overview.nights, overview.nights),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 12.dp)
                .clip(shape = RoundedCornerShape(size = 6.dp))
                .clearAndSetSemantics { contentDescription = splitDescription },
            horizontalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            order.forEach { effect ->
                val count = overview.count(effect = effect)
                if (count > 0) {
                    Box(
                        modifier = Modifier
                            .weight(weight = count.toFloat())
                            .widthIn(min = 6.dp)
                            .height(height = 12.dp)
                            .background(color = effect.color())
                    )
                }
            }
        }
        listOf(DinnerEffect.RAISES, DinnerEffect.LOWERS, DinnerEffect.EVEN, DinnerEffect.WAITING).chunked(size = 2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                pair.forEach { effect ->
                    val label = stringResource(resource = effect.label)
                    val count = overview.count(effect = effect)
                    val description = stringResource(resource = Res.string.next_day_tile_aria, label, count)
                    Row(
                        modifier = Modifier
                            .weight(weight = 1f)
                            .heightIn(min = 52.dp)
                            .clip(shape = RoundedCornerShape(size = 12.dp))
                            .background(color = colors.background)
                            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
                            .clickable(role = Role.Button, onClick = { onOpenAll(effect) })
                            .clearAndSetSemantics { contentDescription = description }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                    ) {
                        EffectDot(effect = effect)
                        Column(modifier = Modifier.weight(weight = 1f)) {
                            Text(text = label, style = typography.bold11, color = colors.muted, maxLines = 1)
                            Text(text = count.toString(), style = typography.bold18, color = colors.ink)
                        }
                        Icon(
                            painter = painterResource(resource = Res.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint = colors.faint,
                            modifier = Modifier.size(size = 14.dp)
                        )
                    }
                }
            }
        }
    }
}

/** The first [TOP_COUNT] of [dinners], and a button to see them all; nothing when there are none. */
@Composable
private fun TopSection(
    title: String,
    hint: String,
    dinners: List<NextDayDinner>,
    unitOf: (String, Boolean) -> String,
    onOpenDinner: (NextDayDinner) -> Unit,
    onSeeAll: () -> Unit
) {
    if (dinners.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, style = typography.bold15, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            Text(text = hint, style = typography.regular11, color = colors.muted)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
        ) {
            dinners.take(n = TOP_COUNT).forEachIndexed { index, dinner ->
                if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
                DinnerRow(dinner = dinner, unitOf = unitOf, onClick = { onOpenDinner(dinner) })
            }
            HorizontalDivider(thickness = 1.dp, color = colors.divider)
            MoreButton(
                text = if (dinners.size > TOP_COUNT) {
                    stringResource(resource = Res.string.next_day_see_all, dinners.size)
                } else {
                    stringResource(resource = Res.string.next_day_see_in_all)
                },
                onClick = onSeeAll
            )
        }
    }
}

/** The full-width button at the foot of a list. */
@Composable
internal fun MoreButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(color = colors.background)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp, alignment = Alignment.CenterHorizontally)
    ) {
        Text(text = text, style = typography.bold13, color = colors.accentInk)
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.accentInk,
            modifier = Modifier.size(size = 14.dp)
        )
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

/** Sample dinners for previews: [foods] (name to kcal) on each of [daysAgo], with weigh-ins that move by [change] overnight. */
internal fun previewDinners(today: LocalDate, foods: List<Pair<String, Int>>, daysAgo: List<Int>, change: Double): Pair<List<DinnerItem>, List<Weight>> {
    val day = { back: Int -> LocalDate.fromEpochDays(epochDays = today.toEpochDays() - back) }
    val items = daysAgo.flatMap { back ->
        foods.map { (name, kcal) ->
            DinnerItem(name = name, isRecipe = false, isQuickAdd = false, date = day(back), amount = 200.0, calories = kcal, protein = 20.0, carbs = 40.0, fat = 10.0)
        }
    }
    val weights = daysAgo.flatMap { back -> listOf(Weight(date = day(back), weight = 75.0), Weight(date = day(back - 1), weight = 75.0 + change)) }
    return items to weights
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewNextDayWeightScreen() {
    val today = LocalDate(year = 2026, month = 10, day = 8)
    val (wings, wingWeights) = previewDinners(today = today, foods = listOf("Chicken wings" to 720, "Rice" to 325), daysAgo = listOf(2, 9, 16, 23), change = 1.1)
    val (salad, saladWeights) = previewDinners(today = today, foods = listOf("Grilled chicken" to 300, "Salad" to 120), daysAgo = listOf(4, 11, 18), change = -0.3)
    MacroTrackTheme {
        NextDayWeightScreen(
            viewState = NextDayWeightViewState(
                today = today,
                inputs = NextDayInputs(items = wings + salad, weights = (wingWeights + saladWeights).distinctBy { it.date }, settings = NextDaySettings())
            ),
            callbacks = NextDayWeightCallbacks.default(),
            onBack = {},
            onOpenDinner = {},
            onOpenAll = {}
        )
    }
}
