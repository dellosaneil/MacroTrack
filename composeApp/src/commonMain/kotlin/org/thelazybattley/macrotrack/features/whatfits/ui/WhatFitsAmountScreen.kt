package org.thelazybattley.macrotrack.features.whatfits.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.what_fits_calories
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.ic_minus
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.what_fits_amount_fill
import macrotrack.composeapp.generated.resources.what_fits_amount_for
import macrotrack.composeapp.generated.resources.what_fits_amount_less
import macrotrack.composeapp.generated.resources.what_fits_amount_more
import macrotrack.composeapp.generated.resources.what_fits_amount_same
import macrotrack.composeapp.generated.resources.what_fits_amount_usual
import macrotrack.composeapp.generated.resources.what_fits_chip_fill
import macrotrack.composeapp.generated.resources.what_fits_chip_last
import macrotrack.composeapp.generated.resources.what_fits_chip_usual
import macrotrack.composeapp.generated.resources.what_fits_in_use
import macrotrack.composeapp.generated.resources.what_fits_kcal_macros
import macrotrack.composeapp.generated.resources.what_fits_left_after_this
import macrotrack.composeapp.generated.resources.what_fits_less
import macrotrack.composeapp.generated.resources.what_fits_log_to
import macrotrack.composeapp.generated.resources.what_fits_more
import macrotrack.composeapp.generated.resources.what_fits_pick_amount
import macrotrack.composeapp.generated.resources.what_fits_source_my_food
import macrotrack.composeapp.generated.resources.what_fits_source_recent
import macrotrack.composeapp.generated.resources.what_fits_source_recipe
import macrotrack.composeapp.generated.resources.what_fits_start_from
import macrotrack.composeapp.generated.resources.what_fits_use
import macrotrack.composeapp.generated.resources.what_fits_use_amount
import macrotrack.composeapp.generated.resources.what_fits_usual_times
import macrotrack.composeapp.generated.resources.what_fits_uses_it_all
import macrotrack.composeapp.generated.resources.what_fits_your_last_times
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.FitFood
import org.thelazybattley.macrotrack.domain.model.FitSource
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.features.whatfits.WhatFitsCallbacks
import org.thelazybattley.macrotrack.features.whatfits.WhatFitsViewModel
import org.thelazybattley.macrotrack.features.whatfits.WhatFitsViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.roundToInt

/** The amount of one idea: from last time, the usual amount, what fills the protein left, or any past amount. */
@Composable
fun WhatFitsAmountScreen(
    viewModel: WhatFitsViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.isDone) {
        if (viewState.isDone) onDone()
    }
    WhatFitsAmountScreen(
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack,
        onPick = {
            viewModel.onPickDraft()
            onBack()
        }
    )
}

@Composable
private fun WhatFitsAmountScreen(
    viewState: WhatFitsViewState,
    callbacks: WhatFitsCallbacks,
    onBack: () -> Unit,
    onPick: () -> Unit
) {
    val food = viewState.amountFood ?: return
    val amount = viewState.draftAmount
    val mealName = stringResource(resource = viewState.mealType.title)
    val amountText = fitAmount(food = food, amount = amount)
    WhatFitsScaffold(
        title = food.name,
        subtitle = stringResource(resource = Res.string.what_fits_amount_for, sourceName(source = food.source), mealName),
        onBack = onBack,
        bottomBar = {
            WhatFitsBottomButtons(
                secondary = stringResource(resource = Res.string.what_fits_log_to, mealName),
                onSecondary = callbacks::onLogDraft,
                primary = stringResource(resource = Res.string.what_fits_pick_amount, amountText),
                onPrimary = onPick
            )
        }
    ) {
        AmountCard(viewState = viewState, food = food, amountText = amountText, callbacks = callbacks)
        Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            WhatFitsSectionLabel(text = stringResource(resource = Res.string.what_fits_left_after_this))
            LeftAfterCard(viewState = viewState)
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            WhatFitsSectionLabel(text = stringResource(resource = Res.string.what_fits_your_last_times))
            PastAmounts(food = food, amount = amount, onUse = callbacks::onDraftAmount)
        }
    }
}

@Composable
private fun sourceName(source: FitSource): String = stringResource(
    resource = when (source) {
        FitSource.RECIPE -> Res.string.what_fits_source_recipe
        FitSource.MY_FOOD -> Res.string.what_fits_source_my_food
        FitSource.RECENT -> Res.string.what_fits_source_recent
    }
)

@Composable
private fun AmountCard(
    viewState: WhatFitsViewState,
    food: FitFood,
    amountText: String,
    callbacks: WhatFitsCallbacks
) {
    val amount = viewState.draftAmount
    val macros = food.macrosFor(amount = amount)
    val fill = viewState.fillProteinAmount
    val tag = when (amount) {
        food.last.amount -> stringResource(resource = Res.string.what_fits_amount_same)
        food.usualAmount -> stringResource(resource = Res.string.what_fits_amount_usual)
        fill -> stringResource(resource = Res.string.what_fits_amount_fill)
        else -> stringResource(
            resource = if (amount > food.last.amount) Res.string.what_fits_amount_more else Res.string.what_fits_amount_less,
            fitAmount(food = food, amount = abs(amount - food.last.amount))
        )
    }
    val isPreset = amount == food.last.amount || amount == food.usualAmount || amount == fill
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 22.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 22.dp))
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
            StepButton(icon = Res.drawable.ic_minus, description = stringResource(resource = Res.string.what_fits_less)) { callbacks.onDraftStep(-1) }
            Column(modifier = Modifier.weight(weight = 1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = amountText, style = typography.bold32, color = colors.ink, textAlign = TextAlign.Center)
                Text(text = tag, style = typography.bold12, color = if (isPreset) colors.accentInk else colors.muted, textAlign = TextAlign.Center)
            }
            StepButton(icon = Res.drawable.ic_plus, description = stringResource(resource = Res.string.what_fits_more)) { callbacks.onDraftStep(1) }
        }
        Text(
            text = stringResource(
                resource = Res.string.what_fits_kcal_macros,
                macros.calories.kcal(),
                macros.protein.roundToInt(),
                macros.carbs.roundToInt(),
                macros.fat.roundToInt()
            ),
            style = typography.regular13,
            color = colors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            WhatFitsSectionLabel(text = stringResource(resource = Res.string.what_fits_start_from))
            Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                val chips = listOfNotNull(
                    Triple(stringResource(resource = Res.string.what_fits_chip_last), food.last.amount, food.last.date.toShortDayMonthDay()),
                    Triple(
                        stringResource(resource = Res.string.what_fits_chip_usual),
                        food.usualAmount,
                        pluralStringResource(resource = Res.plurals.what_fits_usual_times, quantity = food.history.size, food.usualCount, food.history.size)
                    ),
                    fill?.let { Triple(stringResource(resource = Res.string.what_fits_chip_fill), it, stringResource(resource = Res.string.what_fits_uses_it_all)) }
                )
                // Last time is picked when the amounts are the same.
                val selected = chips.indexOfFirst { it.second == amount }
                chips.forEachIndexed { index, (label, value, sub) ->
                    val isSelected = index == selected
                    Column(
                        modifier = Modifier
                            .weight(weight = 1f)
                            .clip(shape = RoundedCornerShape(size = 14.dp))
                            .background(color = if (isSelected) colors.accentSoft else colors.surface)
                            .border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) colors.accent else colors.line, shape = RoundedCornerShape(size = 14.dp))
                            .selectable(selected = isSelected, role = Role.RadioButton, onClick = { callbacks.onDraftAmount(value) })
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = label, style = typography.medium11, color = colors.muted, maxLines = 1)
                        Text(text = fitAmount(food = food, amount = value), style = typography.bold16, color = colors.ink, maxLines = 1)
                        Text(text = sub, style = typography.regular10, color = colors.muted, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepButton(icon: DrawableResource, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size = 52.dp)
            .clip(shape = RoundedCornerShape(size = 17.dp))
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 17.dp))
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = description, tint = colors.ink, modifier = Modifier.size(size = 20.dp))
    }
}

@Composable
private fun LeftAfterCard(viewState: WhatFitsViewState) {
    val left = viewState.left
    val after = viewState.leftAfterDraft
    val rows = listOf(
        AfterRow(label = stringResource(resource = Res.string.what_fits_calories), left = left.calories, after = after.calories, color = colors.accent, unit = " kcal"),
        AfterRow(label = stringResource(resource = Res.string.protein), left = left.protein, after = after.protein, color = MacroType.PROTEIN.fitColor(), unit = " g"),
        AfterRow(label = stringResource(resource = Res.string.carbs), left = left.carbs, after = after.carbs, color = MacroType.CARBS.fitColor(), unit = " g"),
        AfterRow(label = stringResource(resource = Res.string.fat), left = left.fat, after = after.fat, color = MacroType.FAT.fitColor(), unit = " g")
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        rows.forEachIndexed { index, row ->
            if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
            ) {
                Text(text = row.label, style = typography.medium13, color = colors.ink, modifier = Modifier.width(width = 64.dp), maxLines = 1)
                val used = if (row.left > 0) ((row.left - row.after) / row.left).toFloat() else 1f
                WhatFitsBar(modifier = Modifier.weight(weight = 1f), fraction = used, color = row.color, height = 8)
                Text(
                    text = leftOrOver(value = row.after, unit = row.unit),
                    style = typography.bold13,
                    color = if (row.after < 0) colors.warmInk else colors.ink,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(width = 96.dp),
                    maxLines = 1
                )
            }
        }
    }
}

private data class AfterRow(val label: String, val left: Double, val after: Double, val color: Color, val unit: String)

@Composable
private fun PastAmounts(food: FitFood, amount: Double, onUse: (Double) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
    ) {
        food.recentAmounts(count = 3).forEachIndexed { index, past ->
            if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
            val date = past.date.toShortDayMonthDay()
            val pastText = fitAmount(food = food, amount = past.amount)
            val isInUse = past.amount == amount
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 58.dp)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(resource = Res.string.what_fits_use_amount, pastText, date),
                        onClick = { onUse(past.amount) }
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(text = date, style = typography.medium13, color = colors.ink)
                    Text(text = stringResource(resource = past.mealType.title), style = typography.regular11, color = colors.muted)
                }
                Text(text = pastText, style = typography.bold14, color = colors.ink)
                Text(
                    text = stringResource(resource = if (isInUse) Res.string.what_fits_in_use else Res.string.what_fits_use),
                    style = typography.bold12,
                    color = colors.accentInk,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(width = 44.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewWhatFitsAmountScreen() {
    MacroTrackTheme {
        WhatFitsAmountScreen(
            viewState = previewWhatFitsState.copy(amountKey = "food:Chicken breast", draftAmount = 180.0),
            callbacks = WhatFitsCallbacks.default(),
            onBack = {},
            onPick = {}
        )
    }
}
