package org.thelazybattley.macrotrack.features.whatfits.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.ic_minus
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.what_fits
import macrotrack.composeapp.generated.resources.what_fits_after_macros
import macrotrack.composeapp.generated.resources.what_fits_after_these
import macrotrack.composeapp.generated.resources.what_fits_amount_same
import macrotrack.composeapp.generated.resources.what_fits_kcal_left
import macrotrack.composeapp.generated.resources.what_fits_kcal_over
import macrotrack.composeapp.generated.resources.what_fits_less_of
import macrotrack.composeapp.generated.resources.what_fits_less_than_last
import macrotrack.composeapp.generated.resources.what_fits_log_to
import macrotrack.composeapp.generated.resources.what_fits_macros_line
import macrotrack.composeapp.generated.resources.what_fits_more_of
import macrotrack.composeapp.generated.resources.what_fits_more_than_last
import macrotrack.composeapp.generated.resources.what_fits_of_goal
import macrotrack.composeapp.generated.resources.what_fits_of_today
import macrotrack.composeapp.generated.resources.what_fits_picked_count
import macrotrack.composeapp.generated.resources.what_fits_picked_label
import macrotrack.composeapp.generated.resources.what_fits_plan_later
import macrotrack.composeapp.generated.resources.what_fits_review_empty
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.whatfits.FitPick
import org.thelazybattley.macrotrack.features.whatfits.WhatFitsCallbacks
import org.thelazybattley.macrotrack.features.whatfits.WhatFitsViewModel
import org.thelazybattley.macrotrack.features.whatfits.WhatFitsViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

/** The picked ideas with their amounts, logged now or planned for later today. */
@Composable
fun WhatFitsReviewScreen(
    viewModel: WhatFitsViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.isDone) {
        if (viewState.isDone) onDone()
    }
    WhatFitsReviewScreen(viewState = viewState, callbacks = viewModel, onBack = onBack)
}

@Composable
private fun WhatFitsReviewScreen(
    viewState: WhatFitsViewState,
    callbacks: WhatFitsCallbacks,
    onBack: () -> Unit
) {
    val picks = viewState.picks
    val mealName = stringResource(resource = viewState.mealType.title)
    WhatFitsScaffold(
        title = stringResource(resource = Res.string.what_fits),
        subtitle = pluralStringResource(resource = Res.plurals.what_fits_picked_count, quantity = picks.size, picks.size),
        onBack = onBack,
        bottomBar = {
            WhatFitsBottomButtons(
                secondary = stringResource(resource = Res.string.what_fits_plan_later),
                onSecondary = callbacks::onPlanPicks,
                primary = stringResource(resource = Res.string.what_fits_log_to, mealName),
                onPrimary = callbacks::onLogPicks,
                isEnabled = picks.isNotEmpty()
            )
        }
    ) {
        AfterHero(viewState = viewState)
        WhatFitsMealTabs(options = MealType.entries, selected = viewState.mealType, onSelect = callbacks::onMealSelected)
        if (picks.isEmpty()) {
            Text(text = stringResource(resource = Res.string.what_fits_review_empty), style = typography.regular13, color = colors.muted)
            return@WhatFitsScaffold
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            WhatFitsSectionLabel(text = stringResource(resource = Res.string.what_fits_picked_label))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 18.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
            ) {
                picks.forEachIndexed { index, pick ->
                    if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
                    PickRow(pick = pick, mealType = viewState.mealType, onStep = { steps -> callbacks.onStepPick(key = pick.food.key, steps = steps) })
                }
            }
        }
        DayTotals(viewState = viewState)
    }
}

/** How much of today the day comes to with the picks, and what's left after them. */
@Composable
private fun AfterHero(viewState: WhatFitsViewState) {
    val goal = viewState.goalMacros
    val dayFraction = if (goal.calories > 0) ((viewState.eaten.calories + viewState.pickedMacros.calories) / goal.calories).toFloat() else 0f
    val after = viewState.leftAfterPicks
    val track = colors.heroTrack
    val fill = if (dayFraction > 1f) colors.fat else colors.accent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 22.dp))
            .background(color = colors.hero)
            .padding(all = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        Box(modifier = Modifier.size(size = 92.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(size = 82.dp)) {
                val stroke = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
                drawArc(color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = stroke)
                drawArc(color = fill, startAngle = -90f, sweepAngle = 360f * dayFraction.coerceIn(minimumValue = 0f, maximumValue = 1f), useCenter = false, style = stroke)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${(dayFraction * 100).roundToInt()}%", style = typography.bold18, color = colors.ink)
                Text(text = stringResource(resource = Res.string.what_fits_of_today), style = typography.bold9, color = colors.muted)
            }
        }
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
            Text(text = stringResource(resource = Res.string.what_fits_after_these), style = typography.bold11, color = colors.accentInk)
            Text(
                text = if (after.calories >= 0) {
                    stringResource(resource = Res.string.what_fits_kcal_left, after.calories.kcal())
                } else {
                    stringResource(resource = Res.string.what_fits_kcal_over, (-after.calories).kcal())
                },
                style = typography.bold24,
                color = colors.ink
            )
            Text(
                text = stringResource(
                    resource = Res.string.what_fits_after_macros,
                    leftOrOver(value = after.protein, unit = " g"),
                    leftOrOver(value = after.carbs, unit = " g"),
                    leftOrOver(value = after.fat, unit = " g")
                ),
                style = typography.regular13,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun PickRow(pick: FitPick, mealType: MealType, onStep: (Int) -> Unit) {
    val food = pick.food
    val macros = food.macrosFor(amount = pick.amount)
    val last = food.lastFor(mealType = mealType).amount
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        FitFoodIcon(food = food, size = 38)
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 1.dp)) {
            Text(text = food.name, style = typography.bold14, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = "${macros.calories.kcal()} kcal · " + stringResource(
                    resource = Res.string.what_fits_macros_line,
                    macros.protein.roundToInt(),
                    macros.carbs.roundToInt(),
                    macros.fat.roundToInt()
                ),
                style = typography.regular11,
                color = colors.muted,
                maxLines = 1
            )
            Text(
                text = when {
                    pick.amount == last -> stringResource(resource = Res.string.what_fits_amount_same)
                    pick.amount > last -> stringResource(resource = Res.string.what_fits_more_than_last, fitAmount(food = food, amount = last))
                    else -> stringResource(resource = Res.string.what_fits_less_than_last, fitAmount(food = food, amount = last))
                },
                style = typography.bold11,
                color = if (pick.amount == last) colors.accentInk else colors.warmInk,
                maxLines = 1
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            SmallStep(icon = Res.drawable.ic_minus, description = stringResource(resource = Res.string.what_fits_less_of, food.name)) { onStep(-1) }
            Text(
                text = fitAmount(food = food, amount = pick.amount),
                style = typography.bold14,
                color = colors.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 64.dp)
            )
            SmallStep(icon = Res.drawable.ic_plus, description = stringResource(resource = Res.string.what_fits_more_of, food.name)) { onStep(1) }
        }
    }
}

@Composable
private fun SmallStep(icon: DrawableResource, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.background)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = description, tint = colors.ink, modifier = Modifier.size(size = 16.dp))
    }
}

/** Each macro for the whole day with the picks, against its goal. */
@Composable
private fun DayTotals(viewState: WhatFitsViewState) {
    val goal = viewState.goalMacros
    val total = viewState.eaten + viewState.pickedMacros
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        listOf(
            MacroType.PROTEIN to Res.string.protein,
            MacroType.CARBS to Res.string.carbs,
            MacroType.FAT to Res.string.fat
        ).forEach { (macro, label) ->
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
                    Box(modifier = Modifier.size(size = 7.dp).background(color = macro.fitColor(), shape = RoundedCornerShape(size = 2.dp)))
                    Text(text = stringResource(resource = label), style = typography.medium11, color = colors.muted)
                }
                Text(
                    text = stringResource(resource = Res.string.what_fits_of_goal, total[macro].roundToInt(), goal[macro].roundToInt()),
                    style = typography.bold16,
                    color = colors.ink,
                    maxLines = 1
                )
                WhatFitsBar(
                    modifier = Modifier.fillMaxWidth(),
                    fraction = if (goal[macro] > 0) (total[macro] / goal[macro]).toFloat() else 0f,
                    color = macro.fitColor()
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewWhatFitsReviewScreen() {
    MacroTrackTheme {
        WhatFitsReviewScreen(
            viewState = previewWhatFitsState.copy(
                picked = listOf("recipe:Salmon, rice & broccoli", "food:Cottage cheese"),
                amounts = mapOf("recipe:Salmon, rice & broccoli" to 100.0, "food:Cottage cheese" to 200.0)
            ),
            callbacks = WhatFitsCallbacks.default(),
            onBack = {}
        )
    }
}
