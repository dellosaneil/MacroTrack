package org.thelazybattley.macrotrack.features.whatfits.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.ic_bulb
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_restore
import macrotrack.composeapp.generated.resources.ic_shrink
import macrotrack.composeapp.generated.resources.ic_sliders
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.what_fits
import macrotrack.composeapp.generated.resources.what_fits_bars_aria
import macrotrack.composeapp.generated.resources.what_fits_change
import macrotrack.composeapp.generated.resources.what_fits_change_amount
import macrotrack.composeapp.generated.resources.what_fits_empty_body
import macrotrack.composeapp.generated.resources.what_fits_empty_title
import macrotrack.composeapp.generated.resources.what_fits_footer
import macrotrack.composeapp.generated.resources.what_fits_from_eaten
import macrotrack.composeapp.generated.resources.what_fits_full_body
import macrotrack.composeapp.generated.resources.what_fits_full_title
import macrotrack.composeapp.generated.resources.what_fits_hidden
import macrotrack.composeapp.generated.resources.what_fits_hidden_off
import macrotrack.composeapp.generated.resources.what_fits_keeping_spare
import macrotrack.composeapp.generated.resources.what_fits_left_today
import macrotrack.composeapp.generated.resources.what_fits_list_calories
import macrotrack.composeapp.generated.resources.what_fits_list_carbs
import macrotrack.composeapp.generated.resources.what_fits_list_protein
import macrotrack.composeapp.generated.resources.what_fits_macros_line
import macrotrack.composeapp.generated.resources.what_fits_none_body
import macrotrack.composeapp.generated.resources.what_fits_none_title
import macrotrack.composeapp.generated.resources.what_fits_nothing_spare
import macrotrack.composeapp.generated.resources.what_fits_pick
import macrotrack.composeapp.generated.resources.what_fits_picked_kcal
import macrotrack.composeapp.generated.resources.what_fits_review
import macrotrack.composeapp.generated.resources.what_fits_settings_open
import macrotrack.composeapp.generated.resources.what_fits_tag_last
import macrotrack.composeapp.generated.resources.what_fits_kcal_left_for
import macrotrack.composeapp.generated.resources.what_fits_kcal_over_for
import macrotrack.composeapp.generated.resources.what_fits_from_meal_history
import macrotrack.composeapp.generated.resources.what_fits_times_at
import macrotrack.composeapp.generated.resources.what_fits_not_at_yet
import macrotrack.composeapp.generated.resources.what_fits_tag_last_at
import macrotrack.composeapp.generated.resources.what_fits_tag_usual_at
import macrotrack.composeapp.generated.resources.what_fits_note_meal
import macrotrack.composeapp.generated.resources.what_fits_note_any
import macrotrack.composeapp.generated.resources.what_fits_note_last_at
import macrotrack.composeapp.generated.resources.what_fits_note_last
import macrotrack.composeapp.generated.resources.what_fits_note_usual
import macrotrack.composeapp.generated.resources.what_fits_note_serving
import macrotrack.composeapp.generated.resources.what_fits_note_cut
import macrotrack.composeapp.generated.resources.what_fits_show_other_meals
import macrotrack.composeapp.generated.resources.what_fits_only_meal
import macrotrack.composeapp.generated.resources.what_fits_none_at_meal
import macrotrack.composeapp.generated.resources.what_fits_never_at_meal
import macrotrack.composeapp.generated.resources.what_fits_tag_serving
import macrotrack.composeapp.generated.resources.what_fits_tag_set
import macrotrack.composeapp.generated.resources.what_fits_tag_shrunk
import macrotrack.composeapp.generated.resources.what_fits_tag_shrunk_start
import macrotrack.composeapp.generated.resources.what_fits_tag_usual
import macrotrack.composeapp.generated.resources.what_fits_tray_left
import macrotrack.composeapp.generated.resources.what_fits_tray_over
import macrotrack.composeapp.generated.resources.what_fits_unpick
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.FitAmountBasis
import org.thelazybattley.macrotrack.domain.model.FitFood
import org.thelazybattley.macrotrack.domain.model.FitIdea
import org.thelazybattley.macrotrack.domain.model.FitMacros
import org.thelazybattley.macrotrack.domain.model.FitPastAmount
import org.thelazybattley.macrotrack.domain.model.FitRanking
import org.thelazybattley.macrotrack.domain.model.FitSource
import org.thelazybattley.macrotrack.domain.model.FitTooBig
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.whatfits.WhatFitsCallbacks
import org.thelazybattley.macrotrack.features.whatfits.WhatFitsViewModel
import org.thelazybattley.macrotrack.features.whatfits.WhatFitsViewState
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

/** Ideas from food already logged that fit what's left today. */
@Composable
fun WhatFitsScreen(
    viewModel: WhatFitsViewModel,
    onBack: () -> Unit,
    onOpenAmount: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenSettings: () -> Unit,
    onDone: () -> Unit
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.isDone) {
        if (viewState.isDone) onDone()
    }
    WhatFitsScreen(
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack,
        onOpenAmount = { key ->
            viewModel.onOpenAmount(key = key)
            onOpenAmount()
        },
        onOpenReview = onOpenReview,
        onOpenSettings = onOpenSettings
    )
}

@Composable
private fun WhatFitsScreen(
    viewState: WhatFitsViewState,
    callbacks: WhatFitsCallbacks,
    onBack: () -> Unit,
    onOpenAmount: (String) -> Unit,
    onOpenReview: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val left = viewState.left
    val leftCalories = left.calories.roundToInt()
    val mealName = stringResource(resource = viewState.mealType.title)
    val meal = mealName.lowercase()
    WhatFitsScaffold(
        title = stringResource(resource = Res.string.what_fits),
        subtitle = stringResource(
            resource = if (leftCalories >= 0) Res.string.what_fits_kcal_left_for else Res.string.what_fits_kcal_over_for,
            kotlin.math.abs(leftCalories).withThousands(),
            mealName
        ),
        onBack = onBack,
        trailing = {
            WhatFitsIconButton(
                icon = Res.drawable.ic_sliders,
                description = stringResource(resource = Res.string.what_fits_settings_open),
                onClick = onOpenSettings
            )
        },
        bottomBar = {
            if (viewState.picked.isNotEmpty()) PickedTray(viewState = viewState, onReview = onOpenReview)
        }
    ) {
        if (!viewState.isLoaded) return@WhatFitsScaffold
        LeftHero(left = left, spareCalories = viewState.settings.spareCalories)
        WhatFitsMealTabs(options = MealType.entries, selected = viewState.mealType, onSelect = callbacks::onMealSelected)
        val fitIdeas = viewState.ideas
        when {
            viewState.foods.isEmpty() -> EmptyNote(
                title = stringResource(resource = Res.string.what_fits_empty_title),
                body = stringResource(resource = Res.string.what_fits_empty_body)
            )
            leftCalories <= 0 -> EmptyNote(
                title = stringResource(resource = Res.string.what_fits_full_title),
                body = stringResource(resource = Res.string.what_fits_full_body)
            )
            else -> {
                BasisNote(viewState = viewState, meal = meal, onChange = onOpenSettings)
                if (fitIdeas.ideas.isEmpty() && viewState.settings.isMealOnly) {
                    NoneAtMeal(
                        text = stringResource(
                            resource = if (fitIdeas.hiddenCount > 0) Res.string.what_fits_none_at_meal else Res.string.what_fits_never_at_meal,
                            meal
                        )
                    )
                } else if (fitIdeas.ideas.isEmpty()) {
                    EmptyNote(
                        title = stringResource(resource = Res.string.what_fits_none_title),
                        body = stringResource(resource = Res.string.what_fits_none_body)
                    )
                } else {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(
                                resource = when (viewState.settings.ranking) {
                                    FitRanking.PROTEIN -> Res.string.what_fits_list_protein
                                    FitRanking.CALORIES -> Res.string.what_fits_list_calories
                                    FitRanking.FEWEST_CARBS -> Res.string.what_fits_list_carbs
                                }
                            ),
                            style = typography.bold12,
                            color = colors.muted,
                            modifier = Modifier.weight(weight = 1f)
                        )
                        Text(
                            text = if (viewState.settings.isMealOnly) {
                                stringResource(resource = Res.string.what_fits_from_meal_history, meal)
                            } else {
                                stringResource(resource = Res.string.what_fits_from_eaten)
                            },
                            style = typography.medium11,
                            color = colors.muted
                        )
                    }
                    IdeaList(
                        ideas = fitIdeas.ideas,
                        viewState = viewState,
                        meal = meal,
                        onOpen = onOpenAmount,
                        onTogglePick = callbacks::onTogglePick
                    )
                }
                if (fitIdeas.otherMealCount > 0 || !viewState.settings.isMealOnly) {
                    OtherMealsButton(
                        text = if (viewState.settings.isMealOnly) {
                            stringResource(resource = Res.string.what_fits_show_other_meals, fitIdeas.otherMealCount)
                        } else {
                            stringResource(resource = Res.string.what_fits_only_meal, meal)
                        },
                        onClick = callbacks::onToggleMealOnly
                    )
                }
                if (fitIdeas.hiddenCount > 0) {
                    Text(
                        text = pluralStringResource(
                            resource = if (viewState.settings.tooBig == FitTooBig.SHRINK) Res.plurals.what_fits_hidden else Res.plurals.what_fits_hidden_off,
                            quantity = fitIdeas.hiddenCount,
                            fitIdeas.hiddenCount
                        ),
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
                if (fitIdeas.ideas.isNotEmpty()) {
                    Text(text = stringResource(resource = Res.string.what_fits_footer), style = typography.regular11, color = colors.muted)
                }
            }
        }
    }
}

/** What's left today, kept spare calories aside. */
@Composable
private fun LeftHero(left: FitMacros, spareCalories: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 22.dp))
            .background(color = colors.hero)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(text = stringResource(resource = Res.string.what_fits_left_today), style = typography.bold11, color = colors.accentInk)
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
                    Text(text = left.calories.coerceAtLeast(minimumValue = 0.0).kcal(), style = typography.bold32, color = colors.ink)
                    Text(
                        text = stringResource(resource = Res.string.kcal_text),
                        style = typography.regular13,
                        color = colors.muted,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
            }
            Text(
                text = if (spareCalories > 0) {
                    stringResource(resource = Res.string.what_fits_keeping_spare, spareCalories)
                } else {
                    stringResource(resource = Res.string.what_fits_nothing_spare)
                },
                style = typography.regular11,
                color = colors.muted,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            listOf(
                MacroType.PROTEIN to Res.string.protein,
                MacroType.CARBS to Res.string.carbs,
                MacroType.FAT to Res.string.fat
            ).forEach { (macro, label) ->
                Column(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .background(color = colors.surface, shape = RoundedCornerShape(size = 12.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
                        Box(modifier = Modifier.size(size = 7.dp).background(color = macro.fitColor(), shape = RoundedCornerShape(size = 2.dp)))
                        Text(text = stringResource(resource = label), style = typography.medium11, color = colors.muted)
                    }
                    Text(text = "${left[macro].coerceAtLeast(minimumValue = 0.0).roundToInt()} g", style = typography.bold16, color = colors.ink)
                }
            }
        }
    }
}

/** How the amounts were picked, with a way to change it. */
@Composable
private fun BasisNote(viewState: WhatFitsViewState, meal: String, onChange: () -> Unit) {
    val settings = viewState.settings
    val amount = when (settings.amountBasis) {
        FitAmountBasis.LAST_TIME -> if (settings.isMealOnly) {
            stringResource(resource = Res.string.what_fits_note_last_at, meal)
        } else {
            stringResource(resource = Res.string.what_fits_note_last)
        }
        FitAmountBasis.USUAL -> stringResource(resource = Res.string.what_fits_note_usual)
        FitAmountBasis.SERVING -> stringResource(resource = Res.string.what_fits_note_serving)
    }
    val note = if (settings.isMealOnly) {
        stringResource(resource = Res.string.what_fits_note_meal, meal, amount)
    } else {
        stringResource(resource = Res.string.what_fits_note_any, amount)
    }
    val text = if (viewState.ideas.ideas.any { it.isShrunk }) stringResource(resource = Res.string.what_fits_note_cut, note) else note
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.nightSoft)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_restore),
            contentDescription = null,
            tint = colors.nightInk,
            modifier = Modifier.padding(top = 1.dp).size(size = 18.dp)
        )
        Text(text = text, style = typography.regular13, color = colors.ink, modifier = Modifier.weight(weight = 1f))
        Text(
            text = stringResource(resource = Res.string.what_fits_change),
            style = typography.bold13,
            color = colors.nightInk,
            modifier = Modifier
                .clip(shape = RoundedCornerShape(size = 8.dp))
                .clickable(role = Role.Button, onClick = onChange)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun IdeaList(
    ideas: List<FitIdea>,
    viewState: WhatFitsViewState,
    meal: String,
    onOpen: (String) -> Unit,
    onTogglePick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
    ) {
        ideas.forEachIndexed { index, idea ->
            if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
            IdeaRow(
                idea = idea,
                isPicked = idea.food.key in viewState.picked,
                isSetByHand = viewState.amounts[idea.food.key]?.let { it != idea.startAmount } == true,
                basis = viewState.settings.amountBasis,
                mealType = viewState.mealType,
                meal = meal,
                onOpen = { onOpen(idea.food.key) },
                onTogglePick = { onTogglePick(idea.food.key) }
            )
        }
    }
}

@Composable
private fun IdeaRow(
    idea: FitIdea,
    isPicked: Boolean,
    isSetByHand: Boolean,
    basis: FitAmountBasis,
    mealType: MealType,
    meal: String,
    onOpen: () -> Unit,
    onTogglePick: () -> Unit
) {
    val food = idea.food
    val timesAtMeal = food.timesAt(mealType = mealType)
    val tag = when {
        isSetByHand -> stringResource(resource = Res.string.what_fits_tag_set)
        idea.isShrunk && basis == FitAmountBasis.LAST_TIME ->
            stringResource(resource = Res.string.what_fits_tag_shrunk, fitAmount(food = food, amount = idea.startAmount))
        idea.isShrunk -> stringResource(resource = Res.string.what_fits_tag_shrunk_start, fitAmount(food = food, amount = idea.startAmount))
        basis == FitAmountBasis.USUAL && timesAtMeal > 0 -> stringResource(resource = Res.string.what_fits_tag_usual_at, meal)
        basis == FitAmountBasis.USUAL -> stringResource(resource = Res.string.what_fits_tag_usual)
        basis == FitAmountBasis.SERVING -> stringResource(resource = Res.string.what_fits_tag_serving)
        timesAtMeal > 0 -> stringResource(
            resource = Res.string.what_fits_tag_last_at,
            meal,
            food.lastFor(mealType = mealType).date.toShortDayMonthDay()
        )
        else -> stringResource(resource = Res.string.what_fits_tag_last, food.last.date.toShortDayMonthDay())
    }
    val shares = listOf(MacroType.PROTEIN, MacroType.CARBS, MacroType.FAT).map { (idea.shares.getValue(it) * 100).roundToInt() }
    val barsDescription = stringResource(resource = Res.string.what_fits_bars_aria, shares[0], shares[1], shares[2])
    val changeLabel = stringResource(resource = Res.string.what_fits_change_amount, food.name)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = if (isPicked) colors.background else colors.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        FitFoodIcon(food = food)
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .clip(shape = RoundedCornerShape(size = 10.dp))
                .clickable(role = Role.Button, onClickLabel = changeLabel, onClick = onOpen),
            verticalArrangement = Arrangement.spacedBy(space = 3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(
                    text = food.name,
                    style = typography.bold14,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(weight = 1f, fill = false)
                )
                Text(
                    text = if (timesAtMeal > 0) {
                        stringResource(resource = Res.string.what_fits_times_at, timesAtMeal, meal)
                    } else {
                        stringResource(resource = Res.string.what_fits_not_at_yet, meal)
                    },
                    style = typography.regular11,
                    color = colors.muted,
                    maxLines = 1
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                val tint = if (idea.isShrunk) colors.warmInk else colors.accentInk
                Icon(
                    painter = painterResource(resource = if (idea.isShrunk) Res.drawable.ic_shrink else Res.drawable.ic_restore),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(size = 12.dp)
                )
                Text(
                    text = fitAmount(food = food, amount = idea.amount) + " · " + tag,
                    style = typography.bold12,
                    color = tint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "${idea.macros.calories.kcal()} kcal · " + stringResource(
                    resource = Res.string.what_fits_macros_line,
                    idea.macros.protein.roundToInt(),
                    idea.macros.carbs.roundToInt(),
                    idea.macros.fat.roundToInt()
                ),
                style = typography.regular11,
                color = colors.muted
            )
            Row(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clearAndSetSemantics { contentDescription = barsDescription },
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
            ) {
                listOf(MacroType.PROTEIN, MacroType.CARBS, MacroType.FAT).forEach { macro ->
                    Row(
                        modifier = Modifier.weight(weight = 1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
                    ) {
                        Text(text = macro.name.take(n = 1), style = typography.bold10, color = colors.muted, modifier = Modifier.width(width = 8.dp))
                        WhatFitsBar(
                            modifier = Modifier.weight(weight = 1f),
                            fraction = idea.shares.getValue(macro).toFloat(),
                            color = macro.fitColor(),
                            isOutlined = macro in idea.over
                        )
                    }
                }
            }
        }
        PickButton(name = food.name, isPicked = isPicked, onClick = onTogglePick)
    }
}

@Composable
private fun PickButton(name: String, isPicked: Boolean, onClick: () -> Unit) {
    val description = stringResource(resource = if (isPicked) Res.string.what_fits_unpick else Res.string.what_fits_pick, name)
    val shape = RoundedCornerShape(size = 14.dp)
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clip(shape = shape)
            .background(color = if (isPicked) colors.accent else colors.surface)
            .then(if (isPicked) Modifier else Modifier.border(width = 1.5.dp, color = colors.accent, shape = shape))
            .toggleable(value = isPicked, role = Role.Checkbox, onValueChange = { onClick() })
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = if (isPicked) Res.drawable.ic_check else Res.drawable.ic_plus),
            contentDescription = null,
            tint = if (isPicked) colors.onAccent else colors.accentInk,
            modifier = Modifier.size(size = 18.dp)
        )
    }
}

/** The picked ideas so far, and what's left after them. */
@Composable
private fun PickedTray(viewState: WhatFitsViewState, onReview: () -> Unit) {
    val after = viewState.leftAfterPicks
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(size = 20.dp))
                .clip(shape = RoundedCornerShape(size = 20.dp))
                .background(color = colors.ink)
                .padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = pluralStringResource(
                        resource = Res.plurals.what_fits_picked_kcal,
                        quantity = viewState.picked.size,
                        viewState.picked.size,
                        viewState.pickedMacros.calories.kcal()
                    ),
                    style = typography.bold14,
                    color = colors.background
                )
                Text(
                    text = if (after.calories >= 0) {
                        stringResource(resource = Res.string.what_fits_tray_left, after.calories.kcal(), leftOrOver(value = after.protein, unit = " g"))
                    } else {
                        stringResource(resource = Res.string.what_fits_tray_over, (-after.calories).kcal())
                    },
                    style = typography.regular11,
                    color = colors.background.copy(alpha = 0.8f)
                )
            }
            Box(
                modifier = Modifier
                    .height(height = 44.dp)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .background(color = colors.accent)
                    .clickable(role = Role.Button, onClick = onReview)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(resource = Res.string.what_fits_review), style = typography.bold14, color = colors.onAccent)
            }
        }
    }
}

/** Shown in place of the list when nothing eaten at the meal fits, or nothing was ever eaten at it. */
@Composable
private fun NoneAtMeal(text: String) {
    Text(
        text = text,
        style = typography.regular13,
        color = colors.muted,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.5.dp, color = colors.dashed, shape = RoundedCornerShape(size = 18.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp)
    )
}

/** Turns the meal filter off to add food from other meals, or back on. */
@Composable
private fun OtherMealsButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp, alignment = Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(resource = Res.drawable.ic_sliders), contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(size = 16.dp))
        Text(text = text, style = typography.bold14, color = colors.accentInk)
    }
}

@Composable
private fun EmptyNote(title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
            .padding(all = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier.size(size = 36.dp).background(color = colors.accentSoft, shape = RoundedCornerShape(size = 11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(resource = Res.drawable.ic_bulb), contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(size = 18.dp))
        }
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
            Text(text = title, style = typography.bold14, color = colors.ink)
            Text(text = body, style = typography.regular13, color = colors.muted)
        }
    }
}

internal val previewFitFoods = listOf(
    FitFood(
        name = "Chicken breast",
        source = FitSource.MY_FOOD,
        unit = "g",
        baseAmount = 100.0,
        baseMacros = FitMacros(calories = 165.0, protein = 31.0, carbs = 0.0, fat = 3.6),
        dominantMacro = MacroType.PROTEIN,
        history = listOf(
            FitPastAmount(date = LocalDate(year = 2026, month = 10, day = 5), mealType = MealType.DINNER, amount = 180.0),
            FitPastAmount(date = LocalDate(year = 2026, month = 9, day = 29), mealType = MealType.DINNER, amount = 150.0),
            FitPastAmount(date = LocalDate(year = 2026, month = 9, day = 24), mealType = MealType.LUNCH, amount = 150.0)
        )
    ),
    FitFood(
        name = "Salmon, rice & broccoli",
        source = FitSource.RECIPE,
        unit = "",
        baseAmount = 100.0,
        baseMacros = FitMacros(calories = 575.0, protein = 38.0, carbs = 58.0, fat = 20.0),
        dominantMacro = MacroType.CARBS,
        history = listOf(FitPastAmount(date = LocalDate(year = 2026, month = 10, day = 6), mealType = MealType.DINNER, amount = 100.0))
    ),
    FitFood(
        name = "Cottage cheese",
        source = FitSource.RECENT,
        unit = "g",
        baseAmount = 100.0,
        baseMacros = FitMacros(calories = 98.0, protein = 11.0, carbs = 3.4, fat = 4.3),
        dominantMacro = MacroType.PROTEIN,
        history = listOf(FitPastAmount(date = LocalDate(year = 2026, month = 10, day = 4), mealType = MealType.SNACK, amount = 250.0))
    )
)

internal val previewWhatFitsState = WhatFitsViewState(
    isLoaded = true,
    foods = previewFitFoods,
    goal = MacroGoals(calories = 2150, protein = 160, carbs = 220, fat = 70),
    eaten = FitMacros(calories = 1100.0, protein = 74.0, carbs = 140.0, fat = 30.0),
    loggedMeals = setOf(MealType.BREAKFAST, MealType.LUNCH),
    mealType = MealType.DINNER
)

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewWhatFitsScreen() {
    MacroTrackTheme {
        WhatFitsScreen(
            viewState = previewWhatFitsState.copy(picked = listOf("recipe:Salmon, rice & broccoli"), amounts = mapOf("recipe:Salmon, rice & broccoli" to 100.0)),
            callbacks = WhatFitsCallbacks.default(),
            onBack = {},
            onOpenAmount = {},
            onOpenReview = {},
            onOpenSettings = {}
        )
    }
}
