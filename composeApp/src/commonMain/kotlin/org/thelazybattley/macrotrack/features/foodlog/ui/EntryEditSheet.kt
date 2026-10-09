package org.thelazybattley.macrotrack.features.foodlog.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.backspace
import macrotrack.composeapp.generated.resources.change_meal
import macrotrack.composeapp.generated.resources.close
import macrotrack.composeapp.generated.resources.decrease_portion
import macrotrack.composeapp.generated.resources.delete
import macrotrack.composeapp.generated.resources.edit_amount
import macrotrack.composeapp.generated.resources.enter_an_amount
import macrotrack.composeapp.generated.resources.ic_chevron_down
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_trash
import macrotrack.composeapp.generated.resources.increase_portion
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.logged_portion_kcal
import macrotrack.composeapp.generated.resources.no_changes
import macrotrack.composeapp.generated.resources.one_serving
import macrotrack.composeapp.generated.resources.one_serving_is_kcal
import macrotrack.composeapp.generated.resources.recipe
import macrotrack.composeapp.generated.resources.save_changes
import macrotrack.composeapp.generated.resources.servings_value
import macrotrack.composeapp.generated.resources.tap_to_type
import macrotrack.composeapp.generated.resources.typing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.dummyFoodLog
import org.thelazybattley.macrotrack.features.addmeal.FULL_RECIPE_PERCENT
import org.thelazybattley.macrotrack.features.foodlog.EntryEdit
import org.thelazybattley.macrotrack.features.foodlog.KEY_BACKSPACE
import org.thelazybattley.macrotrack.features.foodlog.KEY_DECIMAL
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

private val keypadKeys = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', KEY_DECIMAL, '0', KEY_BACKSPACE)

/**
 * A compact sheet for changing a logged entry. Amounts are typed on an in-sheet keypad instead of the
 * system keyboard, so nothing covers the readout or Save while typing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditSheet(
    edit: EntryEdit,
    onAmountChanged: (Double) -> Unit,
    onTypingStarted: () -> Unit,
    onKey: (Char) -> Unit,
    onMealChanged: (MealType) -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface
    ) {
        EntryEditContent(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 18.dp),
            edit = edit,
            onAmountChanged = onAmountChanged,
            onTypingStarted = onTypingStarted,
            onKey = onKey,
            onMealChanged = onMealChanged,
            onClose = onClose,
            onSave = onSave,
            onDelete = onDelete
        )
    }
}

@Composable
private fun EntryEditContent(
    modifier: Modifier = Modifier,
    edit: EntryEdit,
    isKeypadOpenInitially: Boolean = false,
    onAmountChanged: (Double) -> Unit,
    onTypingStarted: () -> Unit,
    onKey: (Char) -> Unit,
    onMealChanged: (MealType) -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    var isKeypadOpen by rememberSaveable(edit.log.id) { mutableStateOf(value = isKeypadOpenInitially) }
    var isMealPickerOpen by rememberSaveable(edit.log.id) { mutableStateOf(value = false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
        Header(
            edit = edit,
            onMealClick = { isMealPickerOpen = !isMealPickerOpen },
            onClose = onClose
        )
        AnimatedVisibility(visible = isMealPickerOpen) {
            MealPicker(
                selected = edit.mealType,
                onMealChanged = {
                    onMealChanged(it)
                    isMealPickerOpen = false
                }
            )
        }
        Readout(
            edit = edit,
            isTyping = isKeypadOpen,
            onClick = {
                if (!isKeypadOpen) onTypingStarted()
                isKeypadOpen = !isKeypadOpen
            }
        )
        QuickPicks(edit = edit, onAmountChanged = onAmountChanged)
        AnimatedVisibility(visible = isKeypadOpen) {
            Keypad(onKey = onKey)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            val shape = RoundedCornerShape(size = 14.dp)
            val deleteText = stringResource(resource = Res.string.delete)
            Box(
                modifier = Modifier
                    .size(size = 54.dp)
                    .clip(shape = shape)
                    .border(width = 1.dp, color = colors.line, shape = shape)
                    .clickable(role = Role.Button, onClick = onDelete)
                    .semantics { contentDescription = deleteText },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_trash),
                    contentDescription = null,
                    tint = colors.fat,
                    modifier = Modifier.size(size = 18.dp)
                )
            }
            CommonPrimaryButton(
                modifier = Modifier.weight(weight = 1f),
                text = when {
                    edit.amount <= 0 -> Res.string.enter_an_amount
                    edit.hasChanges -> Res.string.save_changes
                    else -> Res.string.no_changes
                },
                enabled = edit.canSave,
                onClick = onSave
            )
        }
    }
}

@Composable
private fun Header(edit: EntryEdit, onMealClick: () -> Unit, onClose: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(
                    text = edit.log.foodName,
                    style = typography.bold18,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(weight = 1f, fill = false)
                )
                if (edit.isRecipe) {
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
                text = if (edit.isRecipe) {
                    stringResource(resource = Res.string.one_serving_is_kcal, edit.baseCalories)
                } else {
                    stringResource(
                        resource = Res.string.logged_portion_kcal,
                        edit.originalAmount.toCleanString() + edit.unit.asSuffix(),
                        edit.log.calories
                    )
                },
                style = typography.regular11,
                color = colors.muted,
                maxLines = 1
            )
        }
        val shape = RoundedCornerShape(size = 10.dp)
        val changeMealText = stringResource(resource = Res.string.change_meal)
        Row(
            modifier = Modifier
                .height(height = 36.dp)
                .clip(shape = shape)
                .border(width = 1.dp, color = colors.line, shape = shape)
                .clickable(role = Role.Button, onClick = onMealClick)
                .semantics { contentDescription = changeMealText }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(size = 8.dp)
                    .background(color = edit.mealType.tint(), shape = RoundedCornerShape(size = 3.dp))
            )
            Text(text = stringResource(resource = edit.mealType.title), style = typography.bold13, color = colors.ink)
            Icon(
                painter = painterResource(resource = Res.drawable.ic_chevron_down),
                contentDescription = null,
                tint = colors.ink,
                modifier = Modifier.size(size = 12.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(size = 36.dp)
                .clip(shape = shape)
                .border(width = 1.dp, color = colors.line, shape = shape)
                .clickable(role = Role.Button, onClick = onClose),
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
}

@Composable
private fun MealPicker(selected: MealType, onMealChanged: (MealType) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.divider)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
            .padding(all = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        MealType.entries.forEach { mealType ->
            val isSelected = mealType == selected
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 34.dp)
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .clickable(role = Role.Tab) { onMealChanged(mealType) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = mealType.title),
                    style = typography.bold12,
                    color = if (isSelected) colors.ink else colors.muted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun Readout(edit: EntryEdit, isTyping: Boolean, onClick: () -> Unit) {
    val change = edit.caloriesChange
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
        val editAmountText = stringResource(resource = Res.string.edit_amount)
        Column(
            modifier = Modifier
                .widthIn(min = 124.dp)
                .clip(shape = shape)
                .background(color = colors.surface)
                .border(width = 1.5.dp, color = if (isTyping) colors.accent else colors.line, shape = shape)
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = editAmountText }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = edit.amountText.ifEmpty { "0" },
                    style = typography.bold32,
                    color = when {
                        edit.amountText.isEmpty() -> colors.faint
                        isTyping && edit.replaceOnType -> colors.accentInk
                        else -> colors.ink
                    }
                )
                if (isTyping) Caret()
                Text(
                    text = edit.unit,
                    style = typography.bold14,
                    color = colors.muted,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Text(
                text = when {
                    isTyping -> stringResource(resource = Res.string.typing)
                    edit.isRecipe -> edit.amount.servingsText()
                    else -> stringResource(resource = Res.string.tap_to_type)
                },
                style = typography.regular11,
                color = colors.muted
            )
        }
        Column(
            modifier = Modifier.weight(weight = 1f).padding(end = 6.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                Text(text = edit.calories.withThousands(), style = typography.bold24, color = colors.ink)
                Text(text = stringResource(resource = Res.string.kcal_text), style = typography.regular11, color = colors.muted)
                if (change != 0) {
                    Text(
                        text = if (change > 0) "+$change" else "−${abs(change)}",
                        style = typography.bold11,
                        color = if (change > 0) colors.warmInk else colors.accentInk,
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(size = 6.dp))
                            .background(color = colors.surface)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                MacroValue(color = colors.protein, grams = edit.protein)
                MacroValue(color = colors.carbs, grams = edit.carbs)
                MacroValue(color = colors.fat, grams = edit.fat)
            }
        }
    }
}

@Composable
private fun Caret() {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 500, easing = LinearEasing), repeatMode = RepeatMode.Reverse)
    )
    Box(
        modifier = Modifier
            .padding(start = 2.dp)
            .width(width = 2.dp)
            .height(height = 28.dp)
            .alpha(alpha = alpha)
            .background(color = colors.accent, shape = RoundedCornerShape(size = 1.dp))
    )
}

@Composable
private fun MacroValue(color: Color, grams: Double) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 3.dp)) {
        Box(modifier = Modifier.size(size = 6.dp).background(color = color, shape = RoundedCornerShape(size = 2.dp)))
        Text(text = "${grams.roundToInt()}g", style = typography.bold12, color = colors.ink)
    }
}

@Composable
private fun QuickPicks(edit: EntryEdit, onAmountChanged: (Double) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        StepButton(
            label = "−",
            description = Res.string.decrease_portion,
            enabled = edit.amount - edit.step > 0,
            onClick = { onAmountChanged(edit.stepped(up = false)) }
        )
        edit.presets.forEach { preset ->
            val isSelected = preset == edit.amount
            val shape = RoundedCornerShape(size = 10.dp)
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 40.dp)
                    .clip(shape = shape)
                    .background(color = if (isSelected) colors.accentSoft else colors.surface)
                    .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
                    .clickable(role = Role.Button) { onAmountChanged(preset) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = preset.toCleanString() + edit.unit.asSuffix(),
                    style = typography.bold13,
                    color = if (isSelected) colors.accentInk else colors.ink,
                    maxLines = 1
                )
            }
        }
        StepButton(
            label = "+",
            description = Res.string.increase_portion,
            enabled = true,
            onClick = { onAmountChanged(edit.stepped(up = true)) }
        )
    }
}

@Composable
private fun StepButton(
    label: String,
    description: StringResource,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 10.dp)
    val text = stringResource(resource = description)
    Box(
        modifier = Modifier
            .size(width = 42.dp, height = 40.dp)
            .clip(shape = shape)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = text },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = typography.bold18, color = if (enabled) colors.ink else colors.faint)
    }
}

@Composable
private fun Keypad(onKey: (Char) -> Unit) {
    val backspaceText = stringResource(resource = Res.string.backspace)
    Column(verticalArrangement = Arrangement.spacedBy(space = 5.dp)) {
        keypadKeys.chunked(size = 3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
                row.forEach { key ->
                    val isAction = key == KEY_DECIMAL || key == KEY_BACKSPACE
                    Box(
                        modifier = Modifier
                            .weight(weight = 1f)
                            .height(height = 46.dp)
                            .clip(shape = RoundedCornerShape(size = 10.dp))
                            .background(color = if (isAction) colors.divider else colors.background)
                            .clickable(role = Role.Button) { onKey(key) }
                            .semantics { if (key == KEY_BACKSPACE) contentDescription = backspaceText },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = key.toString(), style = typography.bold18, color = colors.ink)
                    }
                }
            }
        }
    }
}

@Composable
private fun Double.servingsText(): String {
    val servings = round(this / FULL_RECIPE_PERCENT * 100) / 100
    return if (servings == 1.0) {
        stringResource(resource = Res.string.one_serving)
    } else {
        stringResource(resource = Res.string.servings_value, servings.toCleanString())
    }
}

/** "g" and "%" sit right against the number; longer units get a space. */
private fun String.asSuffix(): String = if (length <= 2) this else " $this"

@Composable
private fun MealType.tint(): Color = when (this) {
    MealType.BREAKFAST, MealType.SNACK -> colors.warmInk
    MealType.LUNCH -> colors.accentInk
    MealType.DINNER -> colors.nightInk
}

@Preview(showBackground = true)
@Composable
private fun PreviewEntryEdit() {
    MacroTrackTheme {
        EntryEditContent(
            modifier = Modifier.background(color = colors.surface).padding(all = 16.dp),
            edit = EntryEdit(
                log = dummyFoodLog.copy(foodName = "Chicken rice bowl", calories = 620, protein = 45.0, carbs = 70.0, fat = 14.0, weight = 0.0),
                originalAmount = 100.0,
                unit = "%"
            ).withAmount(value = 75.0),
            isKeypadOpenInitially = true,
            onAmountChanged = {},
            onTypingStarted = {},
            onKey = {},
            onMealChanged = {},
            onClose = {},
            onSave = {},
            onDelete = {}
        )
    }
}
