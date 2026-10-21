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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import org.thelazybattley.macrotrack.ui.common.CommonKeypadField
import org.thelazybattley.macrotrack.ui.common.CommonKeypadSheet
import org.thelazybattley.macrotrack.ui.common.KeypadPreview
import org.thelazybattley.macrotrack.core.KeypadField
import macrotrack.composeapp.generated.resources.keypad_per_serving
import macrotrack.composeapp.generated.resources.keypad_serving
import macrotrack.composeapp.generated.resources.keypad_serving_subtitle
import macrotrack.composeapp.generated.resources.keypad_per_amount
import macrotrack.composeapp.generated.resources.kcal
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.fat
import kotlin.math.roundToInt
import androidx.compose.runtime.remember

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
    var openField by remember { mutableStateOf<AddFoodTextFieldType?>(value = null) }
    val servingUnit = viewState.unit.ifBlank {
        if (viewState.unitSystem == UnitSystem.IMPERIAL) UnitSystem.OUNCE else stringResource(resource = Res.string.g)
    }
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
            ServingRow(viewState = viewState, callbacks = callbacks, openField = openField, onOpen = { openField = it })
            MacroInputs(viewState = viewState, openField = openField, onOpen = { openField = it })
            MacroPreviewCard(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(
                    resource = Res.string.preview_per_value,
                    viewState.weight.takeIf { it > 0 }?.toCleanString() ?: "–",
                    servingUnit
                ).uppercase(),
                calories = viewState.calories,
                protein = viewState.protein?.toDoubleOrNull() ?: 0.0,
                carbs = viewState.carbs?.toDoubleOrNull() ?: 0.0,
                fat = viewState.fat?.toDoubleOrNull() ?: 0.0,
                footnote = stringResource(resource = Res.string.auto_calculated)
            )
        }
    }

    openField?.let { open ->
        CreateFoodKeypad(
            viewState = viewState,
            servingUnit = servingUnit,
            openField = open,
            onOpen = { openField = it },
            onCommit = { type, text -> callbacks.onTextFieldUpdated(value = text, type = type) },
            onDismiss = { openField = null }
        )
    }
}

/** The serving amount and each macro use the keypad, with calories worked out as they are typed. */
@Composable
private fun CreateFoodKeypad(
    viewState: CreateFoodViewState,
    servingUnit: String,
    openField: AddFoodTextFieldType,
    onOpen: (AddFoodTextFieldType) -> Unit,
    onCommit: (AddFoodTextFieldType, String) -> Unit,
    onDismiss: () -> Unit
) {
    val perServing = stringResource(resource = Res.string.keypad_per_serving)
    val grams = stringResource(resource = Res.string.g)
    fun macroField(type: AddFoodTextFieldType, label: String, value: String?) = KeypadField(
        id = type.name,
        label = label,
        subtitle = perServing,
        unit = grams,
        value = value.orEmpty().removeSuffix(suffix = ".0"),
        wholeDigits = 3,
        decimals = 1,
        allowsZero = true
    )
    CommonKeypadSheet(
        fields = listOf(
            KeypadField(
                id = AddFoodTextFieldType.AMOUNT.name,
                label = stringResource(resource = Res.string.serving_amount),
                shortLabel = stringResource(resource = Res.string.keypad_serving),
                subtitle = stringResource(resource = Res.string.keypad_serving_subtitle),
                unit = servingUnit,
                value = viewState.weight.takeIf { it > 0 }?.toCleanString().orEmpty(),
                wholeDigits = 4,
                decimals = 1
            ),
            macroField(type = AddFoodTextFieldType.PROTEIN, label = stringResource(resource = Res.string.protein), value = viewState.protein),
            macroField(type = AddFoodTextFieldType.CARBS, label = stringResource(resource = Res.string.carbs), value = viewState.carbs),
            macroField(type = AddFoodTextFieldType.FATS, label = stringResource(resource = Res.string.fat), value = viewState.fat)
        ),
        openId = openField.name,
        onOpen = { onOpen(AddFoodTextFieldType.valueOf(value = it)) },
        onCommit = { id, text -> onCommit(AddFoodTextFieldType.valueOf(value = id), text) },
        onDismiss = onDismiss,
        preview = { _, live ->
            fun grams(type: AddFoodTextFieldType) = live(type.name).toDoubleOrNull() ?: 0.0
            val calories = grams(type = AddFoodTextFieldType.PROTEIN) * MacroType.PROTEIN.calories +
                grams(type = AddFoodTextFieldType.CARBS) * MacroType.CARBS.calories +
                grams(type = AddFoodTextFieldType.FATS) * MacroType.FAT.calories
            KeypadPreview(
                value = stringResource(resource = Res.string.kcal, calories.roundToInt()),
                caption = stringResource(
                    resource = Res.string.keypad_per_amount,
                    live(AddFoodTextFieldType.AMOUNT.name).ifEmpty { "0" },
                    servingUnit
                )
            )
        }
    )
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
    callbacks: CreateFoodCallbacks,
    openField: AddFoodTextFieldType?,
    onOpen: (AddFoodTextFieldType) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
        FieldLabel(
            modifier = Modifier.weight(weight = 1f),
            text = stringResource(resource = Res.string.serving_amount)
        ) {
            CommonKeypadField(
                modifier = Modifier.fillMaxWidth().height(height = 50.dp),
                value = viewState.weight.takeIf { it > 0 }?.toCleanString().orEmpty(),
                onClick = { onOpen(AddFoodTextFieldType.AMOUNT) },
                placeholder = stringResource(resource = Res.string.one_hundred),
                textStyle = typography.bold15,
                isOpen = openField == AddFoodTextFieldType.AMOUNT,
                contentDescription = stringResource(resource = Res.string.serving_amount)
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
    openField: AddFoodTextFieldType?,
    onOpen: (AddFoodTextFieldType) -> Unit
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
                    CommonKeypadField(
                        modifier = Modifier.width(width = 76.dp).height(height = 40.dp),
                        value = value.orEmpty().removeSuffix(".0"),
                        onClick = { onOpen(fieldType) },
                        placeholder = "0",
                        textAlign = TextAlign.End,
                        textStyle = typography.bold15,
                        isOpen = openField == fieldType,
                        isHighlighted = !value.isNullOrEmpty(),
                        cornerRadius = 10.dp,
                        horizontalPadding = 10.dp,
                        contentDescription = stringResource(resource = type.text())
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
