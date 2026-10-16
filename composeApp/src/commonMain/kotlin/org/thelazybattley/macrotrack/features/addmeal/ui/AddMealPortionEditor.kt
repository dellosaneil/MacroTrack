package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.background
import org.thelazybattley.macrotrack.ui.common.portionUnit
import org.thelazybattley.macrotrack.domain.model.PortionUnit
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.base_serving
import macrotrack.composeapp.generated.resources.decrease_portion
import macrotrack.composeapp.generated.resources.edit_amount
import macrotrack.composeapp.generated.resources.increase_portion
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.library_portion
import macrotrack.composeapp.generated.resources.percentage_eaten
import macrotrack.composeapp.generated.resources.portion_size
import macrotrack.composeapp.generated.resources.portion_last_time
import macrotrack.composeapp.generated.resources.recipe_full_serving
import macrotrack.composeapp.generated.resources.recipe_last_time
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.features.addmeal.SelectedMealItem
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.round

private const val RECIPE_STEP_PERCENT = 25.0
private const val OUNCE_STEP = 0.5

@Composable
fun AddMealPortionEditor(
    modifier: Modifier = Modifier,
    item: SelectedMealItem,
    onAmountChanged: (key: String, amount: Double) -> Unit
) {
    // Recipes are portioned in %, which no unit system changes.
    val portionUnit = if (item.isRecipe) PortionUnit(label = "%") else portionUnit(storedUnit = item.baseFood.unit)
    val shownAmount = portionUnit.fromStored(amount = item.amount)
    val step = when {
        item.isRecipe -> RECIPE_STEP_PERCENT
        portionUnit.isConverted -> OUNCE_STEP
        item.baseFood.weight > 10 -> 10.0
        else -> 0.5
    }
    val unit = portionUnit.label
    var isKeypadOpen by rememberSaveable(item.key) { mutableStateOf(value = false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            PortionMacro(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = Res.string.kcal_text),
                value = item.food.macros.calories.toString(),
                color = null
            )
            PortionMacro(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = MacroType.PROTEIN.text()),
                value = "${item.food.macros.protein.toInt()}g",
                color = MacroType.PROTEIN.stripeColor()
            )
            PortionMacro(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = MacroType.CARBS.text()),
                value = "${item.food.macros.carbs.toInt()}g",
                color = MacroType.CARBS.stripeColor()
            )
            PortionMacro(
                modifier = Modifier.weight(weight = 1f),
                label = stringResource(resource = MacroType.FAT.text()),
                value = "${item.food.macros.fat.toInt()}g",
                color = MacroType.FAT.stripeColor()
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Text(
                    text = stringResource(
                        resource = if (item.isRecipe) Res.string.percentage_eaten else Res.string.portion_size
                    ),
                    style = typography.bold13,
                    color = colors.ink
                )
                Text(
                    text = when {
                        item.isLibrary -> stringResource(
                            Res.string.library_portion,
                            portionUnit.format(amount = item.baseFood.weight),
                            unit
                        )
                        item.isRecipe && item.lastAmount != null ->
                            stringResource(Res.string.recipe_last_time, item.lastAmount.toCleanString())
                        item.isRecipe -> stringResource(resource = Res.string.recipe_full_serving)
                        item.lastAmount != null -> stringResource(
                            Res.string.portion_last_time,
                            portionUnit.format(amount = item.lastAmount),
                            unit,
                            portionUnit.format(amount = item.baseFood.weight)
                        )
                        else -> stringResource(
                            resource = Res.string.base_serving,
                            portionUnit.format(amount = item.baseFood.weight),
                            unit
                        )
                    },
                    style = typography.regular11,
                    color = colors.muted
                )
            }
            Row(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.divider)
                    .padding(all = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                StepperButton(
                    label = "−",
                    contentDescription = stringResource(resource = Res.string.decrease_portion),
                    enabled = shownAmount - step > 0,
                    onClick = { onAmountChanged(item.key, portionUnit.toStored(value = (shownAmount - step).roundTo(step = step))) }
                )
                val editAmountText = stringResource(resource = Res.string.edit_amount)
                Row(
                    modifier = Modifier
                        .widthIn(min = 64.dp)
                        .height(height = 40.dp)
                        .clip(shape = RoundedCornerShape(size = 9.dp))
                        .background(color = colors.surface)
                        .border(width = 1.5.dp, color = colors.accent, shape = RoundedCornerShape(size = 9.dp))
                        .clickable(role = Role.Button) { isKeypadOpen = true }
                        .semantics { contentDescription = editAmountText }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 3.dp, alignment = Alignment.CenterHorizontally)
                ) {
                    Text(text = portionUnit.format(amount = item.amount), style = typography.bold16, color = colors.ink, maxLines = 1)
                    Text(text = unit, style = typography.regular13, color = colors.muted, maxLines = 1)
                }
                StepperButton(
                    label = "+",
                    contentDescription = stringResource(resource = Res.string.increase_portion),
                    enabled = true,
                    onClick = { onAmountChanged(item.key, portionUnit.toStored(value = (shownAmount + step).roundTo(step = step))) }
                )
            }
        }
    }
    if (isKeypadOpen) {
        AddMealKeypadSheet(
            item = item,
            portionUnit = portionUnit,
            onAmountChanged = { amount -> onAmountChanged(item.key, amount) },
            onDismiss = { isKeypadOpen = false }
        )
    }
}

@Composable
private fun PortionMacro(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color?
) {
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            if (color != null) {
                Box(
                    modifier = Modifier
                        .size(size = 7.dp)
                        .background(color = color, shape = RoundedCornerShape(size = 2.dp))
                )
            }
            Text(
                text = label,
                style = typography.regular10,
                color = colors.muted,
                maxLines = 1
            )
        }
        Text(
            text = value,
            style = typography.bold14,
            color = colors.ink,
            maxLines = 1
        )
    }
}

@Composable
private fun StepperButton(
    label: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size = 40.dp)
            .clip(shape = RoundedCornerShape(size = 9.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 9.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = typography.bold18,
            color = if (enabled) colors.ink else colors.faint
        )
    }
}

private fun Double.roundTo(step: Double): Double = round(this / step) * step

@Preview(showBackground = true)
@Composable
private fun PreviewAddMealPortionEditor() {
    MacroTrackTheme {
        AddMealPortionEditor(
            modifier = Modifier.fillMaxWidth().padding(all = 16.dp),
            item = SelectedMealItem(
                key = "food:${dummyFood.name}",
                isRecipe = false,
                baseFood = dummyFood,
                amount = 150.0,
                food = dummyFood
            ),
            onAmountChanged = { _, _ -> }
        )
    }
}
