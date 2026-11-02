package org.thelazybattley.macrotrack.features.whatfits.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.back
import macrotrack.composeapp.generated.resources.ic_book
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_package
import macrotrack.composeapp.generated.resources.ic_restore
import macrotrack.composeapp.generated.resources.what_fits_servings
import macrotrack.composeapp.generated.resources.what_fits_value_left
import macrotrack.composeapp.generated.resources.what_fits_value_over
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.FIT_FULL_RECIPE_PERCENT
import org.thelazybattley.macrotrack.domain.model.FitFood
import org.thelazybattley.macrotrack.domain.model.FitSource
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.ui.common.formatPortion
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.roundToInt

/** A screen of What fits?: a back button, a title with its subtitle, and the scrolling content below. */
@Composable
internal fun WhatFitsScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    backIcon: DrawableResource = Res.drawable.ic_chevron_left,
    backDescription: String = stringResource(resource = Res.string.back),
    trailing: (@Composable () -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colors.background)
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
            ) {
                WhatFitsIconButton(icon = backIcon, description = backDescription, onClick = onBack)
                Column(modifier = Modifier.weight(weight = 1f)) {
                    Text(text = title, style = typography.bold24, color = colors.ink, maxLines = 1)
                    Text(text = subtitle, style = typography.regular11, color = colors.muted, maxLines = 1)
                }
                trailing?.invoke()
            }
        },
        bottomBar = bottomBar
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp),
            content = content
        )
    }
}

@Composable
internal fun WhatFitsIconButton(icon: DrawableResource, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = description, tint = colors.ink, modifier = Modifier.size(size = 19.dp))
    }
}

/** The two buttons pinned to the bottom of a screen: a plain one and the main one. */
@Composable
internal fun WhatFitsBottomButtons(
    secondary: String,
    onSecondary: () -> Unit,
    primary: String,
    onPrimary: () -> Unit,
    isEnabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = colors.background)
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(weight = 1f)
                .height(height = 54.dp)
                .clip(shape = RoundedCornerShape(size = 16.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 16.dp))
                .clickable(enabled = isEnabled, role = Role.Button, onClick = onSecondary),
            contentAlignment = Alignment.Center
        ) {
            Text(text = secondary, style = typography.bold15, color = if (isEnabled) colors.ink else colors.faint)
        }
        Box(
            modifier = Modifier
                .weight(weight = 1f)
                .height(height = 54.dp)
                .clip(shape = RoundedCornerShape(size = 16.dp))
                .background(color = if (isEnabled) colors.accent else colors.line)
                .clickable(enabled = isEnabled, role = Role.Button, onClick = onPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(text = primary, style = typography.bold15, color = if (isEnabled) colors.onAccent else colors.faint, maxLines = 1)
        }
    }
}

/** Picks the meal ideas go to, from [options]. */
@Composable
internal fun WhatFitsMealTabs(
    options: List<MealType>,
    selected: MealType,
    onSelect: (MealType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.divider)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
            .padding(all = 4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        options.forEach { mealType ->
            val isSelected = mealType == selected
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 40.dp)
                    .then(if (isSelected) Modifier.shadow(elevation = 1.dp, shape = RoundedCornerShape(size = 10.dp)) else Modifier)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(mealType) }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = mealType.title),
                    style = typography.bold14,
                    color = if (isSelected) colors.ink else colors.muted,
                    maxLines = 1
                )
            }
        }
    }
}

/** A thin bar filled to [fraction] in [color]. */
@Composable
internal fun WhatFitsBar(modifier: Modifier = Modifier, fraction: Float, color: Color, height: Int = 5, isOutlined: Boolean = false) {
    Box(
        modifier = modifier
            .height(height = height.dp)
            .clip(shape = RoundedCornerShape(size = (height / 2f).dp))
            .background(color = colors.divider)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = fraction.coerceIn(minimumValue = 0f, maximumValue = 1f))
                .fillMaxHeight()
                .background(color = color, shape = RoundedCornerShape(size = (height / 2f).dp))
                .then(if (isOutlined) Modifier.border(width = 1.5.dp, color = colors.ink, shape = RoundedCornerShape(size = (height / 2f).dp)) else Modifier)
        )
    }
}

@Composable
internal fun WhatFitsSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text = text, style = typography.bold12, color = colors.muted, modifier = modifier)
}

/** A chip in a row of single-choice options. */
@Composable
internal fun WhatFitsChoiceChip(modifier: Modifier = Modifier, text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(height = 40.dp)
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = RoundedCornerShape(size = 20.dp))
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold13, color = if (isSelected) colors.accentInk else colors.ink, maxLines = 1)
    }
}

/** The tinted square that stands for an idea, by where it comes from. */
@Composable
internal fun FitFoodIcon(food: FitFood, size: Int = 40) {
    val (icon, background, tint) = when (food.source) {
        FitSource.RECIPE -> Triple(Res.drawable.ic_book, colors.nightSoft, colors.nightInk)
        FitSource.MY_FOOD -> Triple(Res.drawable.ic_package, colors.accentSoft, colors.accentInk)
        FitSource.RECENT -> Triple(Res.drawable.ic_restore, colors.warmSoft, colors.warmInk)
    }
    Box(
        modifier = Modifier
            .size(size = size.dp)
            .background(color = background, shape = RoundedCornerShape(size = 12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = tint, modifier = Modifier.size(size = (size * 0.47f).dp))
    }
}

/** The macro's color in the current palette. */
@Composable
internal fun MacroType.fitColor(): Color = when (this) {
    MacroType.PROTEIN -> colors.protein
    MacroType.CARBS -> colors.carbs
    MacroType.FAT -> colors.fat
}

/** An amount of [food]: "180 g" (or oz), or "1.5 servings" for a recipe. */
@Composable
internal fun fitAmount(food: FitFood, amount: Double): String = portionText(isRecipe = food.isRecipe, unit = food.unit, amount = amount)

/** A food amount in its stored [unit] as shown ("180 g", or oz), or a recipe percentage as servings ("1.5 servings"). */
@Composable
internal fun portionText(isRecipe: Boolean, unit: String, amount: Double): String = if (isRecipe) {
    val servings = (amount / FIT_FULL_RECIPE_PERCENT * 10).roundToInt() / 10.0
    pluralStringResource(resource = Res.plurals.what_fits_servings, quantity = if (servings == 1.0) 1 else 2, servings.toCleanString())
} else {
    formatPortion(amount = amount, storedUnit = unit)
}

/** "12 g left", or "4 g over" once it's below zero. */
@Composable
internal fun leftOrOver(value: Double, unit: String): String {
    val rounded = value.roundToInt()
    return stringResource(
        resource = if (rounded >= 0) Res.string.what_fits_value_left else Res.string.what_fits_value_over,
        abs(rounded).withThousands() + unit
    )
}

/** "1,050" */
internal fun Int.withThousands(): String {
    val digits = abs(this).toString()
    val grouped = digits.reversed().chunked(size = 3).joinToString(separator = ",").reversed()
    return if (this < 0) "−$grouped" else grouped
}

internal fun Double.kcal(): String = roundToInt().withThousands()
