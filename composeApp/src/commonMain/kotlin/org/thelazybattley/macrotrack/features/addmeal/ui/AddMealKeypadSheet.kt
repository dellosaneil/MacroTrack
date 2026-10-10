package org.thelazybattley.macrotrack.features.addmeal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.base_serving
import macrotrack.composeapp.generated.resources.close
import macrotrack.composeapp.generated.resources.done
import macrotrack.composeapp.generated.resources.enter_an_amount
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.recipe
import macrotrack.composeapp.generated.resources.recipe_full_serving
import macrotrack.composeapp.generated.resources.typing
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.AmountInput
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.features.addmeal.SelectedMealItem
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.CommonKeypad
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.KeypadCaret
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.roundToInt

/**
 * Types a portion for a picked item on the in-app keypad. Each valid amount is applied as it is
 * typed, so the list and the Add total follow along.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealKeypadSheet(
    item: SelectedMealItem,
    unit: String,
    onAmountChanged: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        AddMealKeypadContent(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp),
            item = item,
            unit = unit,
            onAmountChanged = onAmountChanged,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun AddMealKeypadContent(
    modifier: Modifier = Modifier,
    item: SelectedMealItem,
    unit: String,
    onAmountChanged: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember(item.key) { mutableStateOf(value = AmountInput.of(value = item.amount)) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            Column(modifier = Modifier.weight(weight = 1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                    Text(
                        text = item.baseFood.name,
                        style = typography.bold18,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(weight = 1f, fill = false)
                    )
                    if (item.isRecipe) {
                        Text(
                            text = stringResource(resource = Res.string.recipe).uppercase(),
                            style = typography.bold10,
                            color = colors.accentInk,
                            modifier = Modifier
                                .background(color = colors.accentSoft, shape = RoundedCornerShape(size = 6.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = if (item.isRecipe) {
                        stringResource(resource = Res.string.recipe_full_serving)
                    } else {
                        stringResource(resource = Res.string.base_serving, item.baseFood.weight.toCleanString(), item.baseFood.unit)
                    },
                    style = typography.regular11,
                    color = colors.muted
                )
            }
            val shape = RoundedCornerShape(size = 10.dp)
            Box(
                modifier = Modifier
                    .size(size = 36.dp)
                    .clip(shape = shape)
                    .border(width = 1.dp, color = colors.line, shape = shape)
                    .clickable(role = Role.Button, onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_close),
                    contentDescription = stringResource(resource = Res.string.close),
                    tint = colors.ink,
                    modifier = Modifier.size(size = 15.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.hero)
                .padding(all = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            val shape = RoundedCornerShape(size = 12.dp)
            Column(
                modifier = Modifier
                    .widthIn(min = 124.dp)
                    .clip(shape = shape)
                    .background(color = colors.surface)
                    .border(width = 1.5.dp, color = colors.accent, shape = shape)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = input.text.ifEmpty { "0" },
                        style = typography.bold32,
                        color = when {
                            input.text.isEmpty() -> colors.faint
                            input.replaceOnType -> colors.accentInk
                            else -> colors.ink
                        }
                    )
                    KeypadCaret()
                    Text(
                        text = unit,
                        style = typography.bold14,
                        color = colors.muted,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Text(text = stringResource(resource = Res.string.typing), style = typography.regular11, color = colors.muted)
            }
            // Shows the macros of the last valid amount, which is what will be logged.
            val macros = item.food.macros
            Column(
                modifier = Modifier.weight(weight = 1f).padding(end = 6.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                    Text(text = macros.calories.withThousands(), style = typography.bold24, color = colors.ink)
                    Text(text = stringResource(resource = Res.string.kcal_text), style = typography.regular11, color = colors.muted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    MacroGrams(color = colors.protein, grams = macros.protein)
                    MacroGrams(color = colors.carbs, grams = macros.carbs)
                    MacroGrams(color = colors.fat, grams = macros.fat)
                }
            }
        }
        CommonKeypad(
            onKey = { key ->
                input = input.typed(key = key)
                input.value.takeIf { it > 0 }?.let(onAmountChanged)
            }
        )
        CommonPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = if (input.value > 0) Res.string.done else Res.string.enter_an_amount,
            enabled = input.value > 0,
            onClick = onDismiss
        )
    }
}

@Composable
private fun MacroGrams(color: Color, grams: Double) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 3.dp)) {
        Box(modifier = Modifier.size(size = 6.dp).background(color = color, shape = RoundedCornerShape(size = 2.dp)))
        Text(text = "${grams.roundToInt()}g", style = typography.bold12, color = colors.ink)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAddMealKeypadSheet() {
    MacroTrackTheme {
        AddMealKeypadContent(
            modifier = Modifier.background(color = colors.surface).padding(all = 16.dp),
            item = SelectedMealItem(
                key = "food:${dummyFood.name}",
                isRecipe = false,
                baseFood = dummyFood,
                amount = 150.0,
                food = dummyFood
            ),
            unit = "g",
            onAmountChanged = {},
            onDismiss = {}
        )
    }
}
