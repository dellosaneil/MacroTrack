package org.thelazybattley.macrotrack.features.quickadd.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.back_to_add_meal
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.g
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_pencil
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.log_without_a_food
import macrotrack.composeapp.generated.resources.meal
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.quick_add
import macrotrack.composeapp.generated.resources.quick_add_add_to_meal
import macrotrack.composeapp.generated.resources.quick_add_calories_description
import macrotrack.composeapp.generated.resources.quick_add_calories_from_macros
import macrotrack.composeapp.generated.resources.quick_add_calories_only
import macrotrack.composeapp.generated.resources.quick_add_enter_a_macro
import macrotrack.composeapp.generated.resources.quick_add_enter_calories
import macrotrack.composeapp.generated.resources.quick_add_grams_description
import macrotrack.composeapp.generated.resources.quick_add_kcal_left_after
import macrotrack.composeapp.generated.resources.quick_add_kcal_left_today
import macrotrack.composeapp.generated.resources.quick_add_kcal_over_after
import macrotrack.composeapp.generated.resources.quick_add_label_description
import macrotrack.composeapp.generated.resources.quick_add_label_placeholder
import macrotrack.composeapp.generated.resources.quick_add_macros
import macrotrack.composeapp.generated.resources.quick_add_what_to_enter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.ui.stripeColor
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.quickadd.QuickAddCallbacks
import org.thelazybattley.macrotrack.features.quickadd.QuickAddField
import org.thelazybattley.macrotrack.features.quickadd.QuickAddMode
import org.thelazybattley.macrotrack.features.quickadd.QuickAddViewModel
import org.thelazybattley.macrotrack.features.quickadd.QuickAddViewState
import org.thelazybattley.macrotrack.ui.common.CommonKeypad
import org.thelazybattley.macrotrack.ui.common.KeypadCaret
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs

@Composable
fun QuickAddScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val viewModel = koinViewModel<QuickAddViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.isSaved) {
        if (viewState.isSaved) onSaved()
    }
    QuickAddScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack
    )
}

@Composable
private fun QuickAddScreen(
    modifier: Modifier = Modifier,
    viewState: QuickAddViewState,
    callbacks: QuickAddCallbacks,
    onBack: () -> Unit
) {
    val defaultName = stringResource(resource = Res.string.quick_add)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Header(onBack = onBack)
        ModePicker(selected = viewState.mode, onSelected = callbacks::onModeSelected)
        Readout(viewState = viewState, onFieldSelected = callbacks::onFieldSelected)
        LabelField(label = viewState.label, onLabelChange = callbacks::onLabelChanged)
        MealPicker(selected = viewState.mealType, onSelected = callbacks::onMealSelected)
        CommonKeypad(
            modifier = Modifier.weight(weight = 1f).fillMaxWidth(),
            isDecimalEnabled = viewState.mode == QuickAddMode.MACROS,
            fillHeight = true,
            keyColor = colors.surface,
            onKey = callbacks::onKey
        )
        AddButton(viewState = viewState, onClick = { callbacks.onAdd(defaultName = defaultName) })
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        val shape = RoundedCornerShape(size = 14.dp)
        Box(
            modifier = Modifier
                .size(size = 44.dp)
                .clip(shape = shape)
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = shape)
                .clickable(role = Role.Button, onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_chevron_left),
                contentDescription = stringResource(resource = Res.string.back_to_add_meal),
                tint = colors.ink,
                modifier = Modifier.size(size = 18.dp)
            )
        }
        Column {
            Text(text = stringResource(resource = Res.string.log_without_a_food), style = typography.regular11, color = colors.muted)
            Text(text = stringResource(resource = Res.string.quick_add), style = typography.bold24, color = colors.ink)
        }
    }
}

@Composable
private fun ModePicker(selected: QuickAddMode, onSelected: (QuickAddMode) -> Unit) {
    val groupLabel = stringResource(resource = Res.string.quick_add_what_to_enter)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 14.dp))
            .background(color = colors.divider)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
            .semantics { contentDescription = groupLabel }
            .padding(all = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
    ) {
        listOf(
            QuickAddMode.CALORIES to Res.string.quick_add_calories_only,
            QuickAddMode.MACROS to Res.string.quick_add_macros
        ).forEach { (mode, label) ->
            val isSelected = mode == selected
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 40.dp)
                    .clip(shape = RoundedCornerShape(size = 10.dp))
                    .background(color = if (isSelected) colors.surface else Color.Transparent)
                    .clickable(role = Role.Tab) { onSelected(mode) }
                    .semantics { this.selected = isSelected },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = label),
                    style = typography.bold13,
                    color = if (isSelected) colors.ink else colors.muted
                )
            }
        }
    }
}

/** The calories being typed with what's left today, or the three macros with the calories they add up to. */
@Composable
private fun Readout(viewState: QuickAddViewState, onFieldSelected: (QuickAddField) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.hero)
            .padding(all = 14.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        when (viewState.mode) {
            QuickAddMode.CALORIES -> CaloriesReadout(viewState = viewState)
            QuickAddMode.MACROS -> MacrosReadout(viewState = viewState, onFieldSelected = onFieldSelected)
        }
    }
}

@Composable
private fun CaloriesReadout(viewState: QuickAddViewState) {
    val calories = viewState.calories
    val description = stringResource(resource = Res.string.quick_add_calories_description, calories.ifEmpty { "0" })
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 2.dp)
    ) {
        Row(
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = description
                liveRegion = LiveRegionMode.Polite
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = calories.ifEmpty { "0" },
                style = typography.bold36.copy(fontSize = typography.bold36.fontSize * 4 / 3),
                color = if (calories.isEmpty()) colors.faint else colors.ink
            )
            KeypadCaret(height = 40.dp)
            Text(
                text = stringResource(resource = Res.string.kcal_text),
                style = typography.bold16,
                color = colors.muted,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        val remaining = viewState.remainingCalories
        val after = viewState.remainingAfterAdding
        if (remaining != null && after != null) {
            Text(
                text = when {
                    viewState.totalCalories <= 0 ->
                        stringResource(resource = Res.string.quick_add_kcal_left_today, remaining.withThousands())
                    after >= 0 -> stringResource(resource = Res.string.quick_add_kcal_left_after, after.withThousands())
                    else -> stringResource(resource = Res.string.quick_add_kcal_over_after, abs(after).withThousands())
                },
                style = typography.regular11,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun MacrosReadout(viewState: QuickAddViewState, onFieldSelected: (QuickAddField) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        QuickAddField.entries.forEach { field ->
            MacroField(
                modifier = Modifier.weight(weight = 1f),
                label = field.label(),
                text = viewState.grams[field].orEmpty(),
                color = field.macro.stripeColor(),
                isActive = field == viewState.activeField,
                onClick = { onFieldSelected(field) }
            )
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(resource = Res.string.quick_add_calories_from_macros),
            style = typography.regular11,
            color = colors.muted,
            modifier = Modifier.weight(weight = 1f)
        )
        Text(text = viewState.macroCalories.withThousands(), style = typography.bold18, color = colors.ink)
        Text(
            text = stringResource(resource = Res.string.kcal_text),
            style = typography.regular11,
            color = colors.muted,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun MacroField(
    modifier: Modifier = Modifier,
    label: StringResource,
    text: String,
    color: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    val labelText = stringResource(resource = label)
    val description = stringResource(resource = Res.string.quick_add_grams_description, labelText, text.ifEmpty { "0" })
    Column(
        modifier = modifier
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.5.dp, color = if (isActive) colors.accent else colors.line, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = description
                selected = isActive
            }
            .padding(all = 10.dp),
        verticalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 5.dp)
        ) {
            Box(modifier = Modifier.size(size = 7.dp).background(color = color, shape = RoundedCornerShape(size = 2.dp)))
            Text(text = labelText, style = typography.bold11, color = colors.muted)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text.ifEmpty { "0" },
                style = typography.bold24,
                color = if (text.isEmpty()) colors.faint else colors.ink,
                maxLines = 1
            )
            if (isActive) KeypadCaret(height = 22.dp)
            Text(
                text = stringResource(resource = Res.string.g),
                style = typography.bold12,
                color = colors.muted,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

@Composable
private fun LabelField(label: String, onLabelChange: (String) -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    val description = stringResource(resource = Res.string.quick_add_label_description)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 46.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_pencil),
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(size = 16.dp)
        )
        val placeholder = stringResource(resource = Res.string.quick_add_label_placeholder)
        BasicTextField(
            value = label,
            onValueChange = onLabelChange,
            singleLine = true,
            textStyle = typography.regular13.copy(color = colors.ink),
            cursorBrush = SolidColor(value = colors.accent),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            modifier = Modifier
                .weight(weight = 1f)
                .semantics { contentDescription = description },
            decorationBox = { innerTextField ->
                if (label.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = typography.regular13,
                        color = colors.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun MealPicker(selected: MealType, onSelected: (MealType) -> Unit) {
    val groupLabel = stringResource(resource = Res.string.meal)
    Row(
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = groupLabel },
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        MealType.entries.forEach { mealType ->
            val isSelected = mealType == selected
            val shape = RoundedCornerShape(size = 12.dp)
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 40.dp)
                    .clip(shape = shape)
                    .background(color = if (isSelected) colors.accentSoft else colors.surface)
                    .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
                    .clickable(role = Role.Tab) { onSelected(mealType) }
                    .semantics { this.selected = isSelected },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(resource = mealType.title),
                    style = typography.bold12,
                    color = if (isSelected) colors.accentInk else colors.ink,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun AddButton(viewState: QuickAddViewState, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 16.dp)
    val isEnabled = viewState.canAdd
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 54.dp)
            .clip(shape = shape)
            .background(color = if (isEnabled) colors.accent else colors.line)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        if (isEnabled) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_check),
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(size = 18.dp)
            )
        }
        Text(
            text = when {
                isEnabled -> stringResource(
                    resource = Res.string.quick_add_add_to_meal,
                    viewState.totalCalories.withThousands(),
                    stringResource(resource = viewState.mealType.title)
                )
                viewState.mode == QuickAddMode.CALORIES -> stringResource(resource = Res.string.quick_add_enter_calories)
                else -> stringResource(resource = Res.string.quick_add_enter_a_macro)
            },
            style = typography.bold16,
            color = if (isEnabled) colors.onAccent else colors.muted
        )
    }
}

private fun QuickAddField.label(): StringResource = when (this) {
    QuickAddField.PROTEIN -> Res.string.protein
    QuickAddField.CARBS -> Res.string.carbs
    QuickAddField.FAT -> Res.string.fat
}

@Preview(showBackground = true)
@Composable
private fun PreviewQuickAddScreen() {
    MacroTrackTheme {
        QuickAddScreen(
            viewState = QuickAddViewState(mealType = MealType.DINNER, calories = "450", replaceOnType = false, remainingCalories = 1050),
            callbacks = QuickAddCallbacks.default(),
            onBack = {}
        )
    }
}
