package org.thelazybattley.macrotrack.features.foodlog.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.close
import macrotrack.composeapp.generated.resources.decrease_portion
import macrotrack.composeapp.generated.resources.delete
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_trash
import macrotrack.composeapp.generated.resources.increase_portion
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.logged_on
import macrotrack.composeapp.generated.resources.logged_today
import macrotrack.composeapp.generated.resources.meal
import macrotrack.composeapp.generated.resources.no_changes
import macrotrack.composeapp.generated.resources.percentage_eaten
import macrotrack.composeapp.generated.resources.portion_size
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.recipe
import macrotrack.composeapp.generated.resources.save_changes
import macrotrack.composeapp.generated.resources.unchanged
import macrotrack.composeapp.generated.resources.was_value_gram
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.dummyFoodLog
import org.thelazybattley.macrotrack.features.foodlog.EntryEdit
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditSheet(
    edit: EntryEdit,
    onAmountChanged: (Double) -> Unit,
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
                .padding(start = 20.dp, end = 20.dp, bottom = 22.dp),
            edit = edit,
            onAmountChanged = onAmountChanged,
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
    onAmountChanged: (Double) -> Unit,
    onMealChanged: (MealType) -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
        Header(edit = edit, onClose = onClose)
        AmountSection(edit = edit, onAmountChanged = onAmountChanged)
        TotalsCard(edit = edit)
        MealSection(selected = edit.mealType, onMealChanged = onMealChanged)
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            val shape = RoundedCornerShape(size = 16.dp)
            Row(
                modifier = Modifier
                    .height(height = 54.dp)
                    .clip(shape = shape)
                    .border(width = 1.dp, color = colors.line, shape = shape)
                    .clickable(role = Role.Button, onClick = onDelete)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_trash),
                    contentDescription = null,
                    tint = colors.fat,
                    modifier = Modifier.size(size = 16.dp)
                )
                Text(text = stringResource(resource = Res.string.delete), style = typography.bold14, color = colors.fat)
            }
            CommonPrimaryButton(
                modifier = Modifier.weight(weight = 1f),
                text = if (edit.hasChanges) Res.string.save_changes else Res.string.no_changes,
                enabled = edit.canSave,
                onClick = onSave
            )
        }
    }
}

@Composable
private fun Header(edit: EntryEdit, onClose: () -> Unit) {
    val date = edit.log.date
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(
                    text = edit.log.foodName,
                    style = typography.bold18,
                    color = colors.ink,
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
                text = if (date == getCurrentDate()) {
                    stringResource(resource = Res.string.logged_today)
                } else {
                    stringResource(resource = Res.string.logged_on, date.toAbbreviatedMonthDay())
                },
                style = typography.regular11,
                color = colors.muted
            )
        }
        val shape = RoundedCornerShape(size = 12.dp)
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
                modifier = Modifier.size(size = 16.dp)
            )
        }
    }
}

@Composable
private fun AmountSection(edit: EntryEdit, onAmountChanged: (Double) -> Unit) {
    var text by remember(key1 = edit.log.id) { mutableStateOf(value = edit.amount.toCleanString()) }
    LaunchedEffect(key1 = edit.amount) {
        if (text.toDoubleOrNull() != edit.amount) text = edit.amount.toCleanString()
    }
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(
            text = stringResource(resource = if (edit.isRecipe) Res.string.percentage_eaten else Res.string.portion_size),
            style = typography.bold12,
            color = colors.muted
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            StepButton(
                label = "−",
                description = Res.string.decrease_portion,
                enabled = edit.amount - edit.step > 0,
                onClick = { onAmountChanged(edit.stepped(up = false)) }
            )
            Row(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 48.dp)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .border(width = 1.5.dp, color = colors.accent, shape = RoundedCornerShape(size = 14.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 4.dp, alignment = Alignment.CenterHorizontally)
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { value ->
                        if (value.length > 6 || value.count { it == '.' } > 1 || value.any { !it.isDigit() && it != '.' }) {
                            return@BasicTextField
                        }
                        text = value
                        value.toDoubleOrNull()?.takeIf { it > 0 }?.let(onAmountChanged)
                    },
                    singleLine = true,
                    textStyle = typography.bold24.copy(color = colors.ink, textAlign = TextAlign.End),
                    cursorBrush = SolidColor(value = colors.accent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(width = 80.dp)
                )
                Text(text = edit.unit, style = typography.regular13, color = colors.muted, modifier = Modifier.width(width = 40.dp))
            }
            StepButton(
                label = "+",
                description = Res.string.increase_portion,
                enabled = true,
                onClick = { onAmountChanged(edit.stepped(up = true)) }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            edit.presets.forEach { preset ->
                val isSelected = preset == edit.amount
                val shape = RoundedCornerShape(size = 10.dp)
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 34.dp)
                        .clip(shape = shape)
                        .background(color = if (isSelected) colors.accentSoft else colors.surface)
                        .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
                        .clickable(role = Role.Button) { onAmountChanged(preset) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset.toCleanString() + if (edit.isRecipe || edit.unit.length <= 2) edit.unit else " ${edit.unit}",
                        style = typography.bold13,
                        color = if (isSelected) colors.accentInk else colors.ink,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun StepButton(
    label: String,
    description: StringResource,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    val text = stringResource(resource = description)
    Box(
        modifier = Modifier
            .size(size = 48.dp)
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
private fun TotalsCard(edit: EntryEdit) {
    val change = edit.caloriesChange
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.hero)
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = edit.calories.withThousands(), style = typography.bold32, color = colors.ink, modifier = Modifier.alignByBaseline())
            Text(
                text = " " + stringResource(resource = Res.string.kcal_text),
                style = typography.regular13,
                color = colors.muted,
                modifier = Modifier.alignByBaseline().weight(weight = 1f)
            )
            Text(
                text = when {
                    change == 0 -> stringResource(resource = Res.string.unchanged)
                    change > 0 -> "+$change " + stringResource(resource = Res.string.kcal_text)
                    else -> "−${abs(change)} " + stringResource(resource = Res.string.kcal_text)
                },
                style = typography.bold12,
                color = when {
                    change == 0 -> colors.muted
                    change > 0 -> colors.warmInk
                    else -> colors.accentInk
                },
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .background(color = colors.surface)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            MacroTile(Modifier.weight(weight = 1f), Res.string.protein, colors.protein, edit.protein, edit.log.protein)
            MacroTile(Modifier.weight(weight = 1f), Res.string.carbs, colors.carbs, edit.carbs, edit.log.carbs)
            MacroTile(Modifier.weight(weight = 1f), Res.string.fat, colors.fat, edit.fat, edit.log.fat)
        }
    }
}

@Composable
private fun MacroTile(
    modifier: Modifier,
    label: StringResource,
    color: Color,
    grams: Double,
    wasGrams: Double
) {
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.surface)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
            Box(modifier = Modifier.size(size = 7.dp).background(color = color, shape = RoundedCornerShape(size = 2.dp)))
            Text(text = stringResource(resource = label), style = typography.regular11, color = colors.muted)
        }
        Text(text = "${grams.roundToInt()}g", style = typography.bold16, color = colors.ink)
        Text(
            text = if (grams.roundToInt() == wasGrams.roundToInt()) " " else stringResource(resource = Res.string.was_value_gram, wasGrams.roundToInt()),
            style = typography.regular11,
            color = colors.faint
        )
    }
}

@Composable
private fun MealSection(selected: MealType, onMealChanged: (MealType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(text = stringResource(resource = Res.string.meal), style = typography.bold12, color = colors.muted)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 14.dp))
                .background(color = colors.divider)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
                .padding(all = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            MealType.entries.forEach { mealType ->
                val isSelected = mealType == selected
                Box(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(height = 38.dp)
                        .clip(shape = RoundedCornerShape(size = 10.dp))
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
}

@Preview(showBackground = true)
@Composable
private fun PreviewEntryEdit() {
    MacroTrackTheme {
        EntryEditContent(
            modifier = Modifier.background(color = colors.surface).padding(all = 20.dp),
            edit = EntryEdit(
                log = dummyFoodLog.copy(foodName = "Chicken rice bowl", calories = 620, protein = 45.0, carbs = 70.0, fat = 14.0, weight = 0.0),
                originalAmount = 100.0,
                amount = 75.0,
                unit = "%"
            ),
            onAmountChanged = {},
            onMealChanged = {},
            onClose = {},
            onSave = {},
            onDelete = {}
        )
    }
}
