package org.thelazybattley.macrotrack.features.createfood.ui

import androidx.compose.foundation.background
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.auto_calculated
import macrotrack.composeapp.generated.resources.chicken_breast
import macrotrack.composeapp.generated.resources.create_food_barcode_linked
import macrotrack.composeapp.generated.resources.food_name
import macrotrack.composeapp.generated.resources.g
import macrotrack.composeapp.generated.resources.macros_per_serving
import macrotrack.composeapp.generated.resources.new_food_button
import macrotrack.composeapp.generated.resources.one_hundred
import macrotrack.composeapp.generated.resources.preview_per_value
import macrotrack.composeapp.generated.resources.save_food
import macrotrack.composeapp.generated.resources.serving_amount
import macrotrack.composeapp.generated.resources.this_food_is_already_saved
import macrotrack.composeapp.generated.resources.unit
import macrotrack.composeapp.generated.resources.update_food
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.features.addmeal.ui.stripeColor
import org.thelazybattley.macrotrack.features.createfood.CreateFoodCallbacks
import org.thelazybattley.macrotrack.features.createfood.CreateFoodViewModel
import org.thelazybattley.macrotrack.features.createfood.CreateFoodViewState
import org.thelazybattley.macrotrack.features.scanbarcode.formatBarcode
import org.thelazybattley.macrotrack.ui.common.CommonInputField
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CreateFoodScreen(
    modifier: Modifier = Modifier,
    popBackStack: () -> Unit
) {
    val viewModel = koinViewModel<CreateFoodViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.foodSaved) {
        if (viewState.foodSaved) popBackStack()
    }
    CreateFoodScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = popBackStack
    )
}

@Composable
private fun CreateFoodScreen(
    modifier: Modifier = Modifier,
    viewState: CreateFoodViewState,
    callbacks: CreateFoodCallbacks,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = {
            CommonScreenHeader(
                title = if (viewState.isUpdating) Res.string.update_food else Res.string.new_food_button,
                onBack = onBack
            )
        },
        bottomBar = {
            CommonPrimaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                text = if (viewState.isUpdating) Res.string.update_food else Res.string.save_food,
                enabled = viewState.buttonEnabled,
                onClick = callbacks::onSaveFood
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            FieldLabel(text = stringResource(resource = Res.string.food_name)) {
                CommonInputField(
                    modifier = Modifier.fillMaxWidth().height(height = 50.dp),
                    value = viewState.name,
                    onValueChange = { callbacks.onTextFieldUpdated(value = it, type = AddFoodTextFieldType.FOOD_NAME) },
                    placeholder = stringResource(resource = Res.string.chicken_breast),
                    textStyle = typography.medium13,
                    isError = viewState.duplicateFood,
                    enabled = !viewState.isUpdating
                )
                if (viewState.duplicateFood) {
                    Text(
                        text = stringResource(resource = Res.string.this_food_is_already_saved),
                        style = typography.regular11,
                        color = colors.fat
                    )
                }
                viewState.barcode?.let { barcode ->
                    Text(
                        text = stringResource(Res.string.create_food_barcode_linked, formatBarcode(digits = barcode)),
                        style = typography.regular11,
                        color = colors.muted
                    )
                }
            }
            ServingRow(viewState = viewState, callbacks = callbacks)
            MacroInputs(viewState = viewState, callbacks = callbacks)
            MacroPreviewCard(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(
                    resource = Res.string.preview_per_value,
                    viewState.weight.takeIf { it > 0 }?.toCleanString() ?: "–",
                    viewState.unit.ifBlank { if (viewState.unitSystem == UnitSystem.IMPERIAL) UnitSystem.OUNCE else stringResource(resource = Res.string.g) }
                ).uppercase(),
                calories = viewState.calories,
                protein = viewState.protein?.toDoubleOrNull() ?: 0.0,
                carbs = viewState.carbs?.toDoubleOrNull() ?: 0.0,
                fat = viewState.fat?.toDoubleOrNull() ?: 0.0,
                footnote = stringResource(resource = Res.string.auto_calculated)
            )
        }
    }
}

@Composable
private fun FieldLabel(
    modifier: Modifier = Modifier,
    text: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        Text(text = text, style = typography.bold12, color = colors.muted)
        content()
    }
}

@Composable
private fun ServingRow(
    viewState: CreateFoodViewState,
    callbacks: CreateFoodCallbacks
) {
    var amount by rememberSaveable { mutableStateOf(value = "") }
    LaunchedEffect(key1 = viewState.weight) {
        if ((amount.toDoubleOrNull() ?: 0.0) != viewState.weight) {
            amount = viewState.weight.takeIf { it > 0 }?.toCleanString().orEmpty()
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
        FieldLabel(
            modifier = Modifier.weight(weight = 1f),
            text = stringResource(resource = Res.string.serving_amount)
        ) {
            CommonInputField(
                modifier = Modifier.fillMaxWidth().height(height = 50.dp),
                value = amount,
                onValueChange = { value ->
                    if (value.isDecimalInput()) {
                        amount = value
                        callbacks.onTextFieldUpdated(value = value, type = AddFoodTextFieldType.AMOUNT)
                    }
                },
                placeholder = stringResource(resource = Res.string.one_hundred),
                keyboardType = KeyboardType.Decimal,
                textStyle = typography.bold15
            )
        }
        FieldLabel(
            modifier = Modifier.weight(weight = 1f),
            text = stringResource(resource = Res.string.unit)
        ) {
            CommonInputField(
                modifier = Modifier.fillMaxWidth().height(height = 50.dp),
                value = viewState.unit,
                onValueChange = { callbacks.onTextFieldUpdated(value = it, type = AddFoodTextFieldType.UNIT) },
                placeholder = stringResource(resource = Res.string.g),
                textStyle = typography.bold15
            )
        }
    }
}

@Composable
private fun MacroInputs(
    viewState: CreateFoodViewState,
    callbacks: CreateFoodCallbacks
) {
    val rows = listOf(
        Triple(MacroType.PROTEIN, viewState.protein, AddFoodTextFieldType.PROTEIN),
        Triple(MacroType.CARBS, viewState.carbs, AddFoodTextFieldType.CARBS),
        Triple(MacroType.FAT, viewState.fat, AddFoodTextFieldType.FATS)
    )
    FieldLabel(text = stringResource(resource = Res.string.macros_per_serving)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
        ) {
            rows.forEachIndexed { index, (type, value, fieldType) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 58.dp)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(size = 10.dp)
                            .background(color = type.stripeColor(), shape = RoundedCornerShape(size = 3.dp))
                    )
                    Text(
                        text = stringResource(resource = type.text()),
                        style = typography.medium13,
                        color = colors.ink,
                        modifier = Modifier.weight(weight = 1f)
                    )
                    CommonInputField(
                        modifier = Modifier.width(width = 76.dp).height(height = 40.dp),
                        value = value.orEmpty().removeSuffix(".0"),
                        onValueChange = { input ->
                            if (input.isDecimalInput()) callbacks.onTextFieldUpdated(value = input, type = fieldType)
                        },
                        placeholder = "0",
                        keyboardType = KeyboardType.Decimal,
                        textAlign = TextAlign.End,
                        textStyle = typography.bold15,
                        isHighlighted = !value.isNullOrEmpty(),
                        cornerRadius = 10.dp,
                        horizontalPadding = 10.dp
                    )
                    Text(
                        text = stringResource(resource = Res.string.g),
                        style = typography.regular13,
                        color = colors.muted,
                        modifier = Modifier.width(width = 12.dp)
                    )
                }
                if (index != rows.lastIndex) {
                    HorizontalDivider(thickness = 1.dp, color = colors.divider)
                }
            }
        }
    }
}

private fun String.isDecimalInput(): Boolean =
    length <= 7 && count { it == '.' } <= 1 && all { it.isDigit() || it == '.' }

enum class AddFoodTextFieldType {
    FOOD_NAME,
    AMOUNT,
    FATS,
    PROTEIN,
    CARBS,
    UNIT
}

@Preview(showBackground = true)
@Composable
private fun PreviewCreateFoodScreen() {
    MacroTrackTheme {
        CreateFoodScreen(
            viewState = CreateFoodViewState(name = "Chicken breast", protein = "31", carbs = "0", fat = "4", calories = 160, weight = 100.0, unit = "g"),
            callbacks = CreateFoodCallbacks.default(),
            onBack = {}
        )
    }
}
