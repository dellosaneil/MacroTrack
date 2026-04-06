package org.thelazybattley.macrotrack.features.createfood.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.amount
import macrotrack.composeapp.generated.resources.carbs_g
import macrotrack.composeapp.generated.resources.chicken_breast
import macrotrack.composeapp.generated.resources.fat_g
import macrotrack.composeapp.generated.resources.food_name
import macrotrack.composeapp.generated.resources.g
import macrotrack.composeapp.generated.resources.macros_per_serving
import macrotrack.composeapp.generated.resources.new_food
import macrotrack.composeapp.generated.resources.one_hundred
import macrotrack.composeapp.generated.resources.placeholder_carbs
import macrotrack.composeapp.generated.resources.placeholder_fats
import macrotrack.composeapp.generated.resources.placeholder_protein
import macrotrack.composeapp.generated.resources.protein_g
import macrotrack.composeapp.generated.resources.save_food
import macrotrack.composeapp.generated.resources.this_food_is_already_saved
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toColor
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.features.createfood.CreateFoodCallbacks
import org.thelazybattley.macrotrack.features.createfood.CreateFoodViewModel
import org.thelazybattley.macrotrack.features.createfood.CreateFoodViewState
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.ui.common.CommonButton
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.common.CommonTopBar
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddIngredientScreen(
    modifier: Modifier = Modifier,
    popBackStack: () -> Unit
) {
    val viewModel = koinViewModel<CreateFoodViewModel>()
    val viewState by viewModel.state.collectAsState()
    LaunchedEffect(key1 = viewState.foodSaved) {
        if (viewState.foodSaved) {
            popBackStack()
        }
    }
    Scaffold(
        containerColor = colors.white,
        topBar = {
            CommonTopBar(
                modifier = Modifier
                    .padding(paddingValues = AppPadding)
                    .fillMaxWidth()
                    .statusBarsPadding(),
                stringResource = Res.string.new_food
            ) {
                popBackStack()
            }
        }
    ) { innerPadding ->
        AddIngredientScreen(
            modifier = modifier
                .padding(paddingValues = innerPadding)
                .padding(paddingValues = AppPadding),
            viewState = viewState,
            callbacks = viewModel
        )
    }
}

@Composable
fun AddIngredientScreen(
    modifier: Modifier = Modifier,
    viewState: CreateFoodViewState,
    callbacks: CreateFoodCallbacks,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Spacer(modifier = Modifier)
        val textColor = if (viewState.duplicateFood) {
            colors.crimsonRed
        } else {
            colors.gray
        }
        AddIngredientTextField(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.food_name,
            titleTextColor = textColor,
            borderColor = colors.deepBlue,
            placeholder = Res.string.chicken_breast,
            isError = viewState.duplicateFood
        ) {
            callbacks.onTextFieldUpdated(
                value = it,
                type = AddFoodTextFieldType.FOOD_NAME
            )
        }
        if (viewState.duplicateFood) {
            Text(
                text = stringResource(resource = Res.string.this_food_is_already_saved),
                style = typography.regular10,
                color = colors.crimsonRed
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            Text(
                text = stringResource(resource = Res.string.amount),
                color = colors.gray,
                style = typography.medium11
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val textFieldHeight = remember { mutableStateOf(0) }
                val height = with(receiver = LocalDensity.current) {
                    textFieldHeight.value.toDp()
                }
                CommonTextField(
                    modifier = Modifier
                        .weight(weight = 0.85f)
                        .onSizeChanged { size ->
                            textFieldHeight.value = size.height
                        },
                    placeholder = Res.string.one_hundred,
                    borderColor = colors.deepBlue,
                    onValueChanged = { value ->
                        callbacks.onTextFieldUpdated(
                            value = value,
                            type = AddFoodTextFieldType.AMOUNT_IN_GRAMS
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp, color = colors.deepBlue,
                            shape = RoundedCornerShape(size = 12.dp)
                        )
                        .height(height = height)
                        .weight(weight = 0.15f),
                ) {
                    Text(
                        text = stringResource(resource = Res.string.g),
                        style = typography.bold12,
                        color = colors.deepBlue,
                        modifier = Modifier.align(alignment = Alignment.Center)
                    )
                }
            }
        }

        Text(
            text = stringResource(
                resource = Res.string.macros_per_serving,
            ),
            style = typography.bold12,
            color = colors.black
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AddIngredientTextField(
                modifier = Modifier.weight(weight = 1f),
                title = Res.string.protein_g,
                titleTextColor = MacroType.PROTEIN.toColor(),
                borderColor = MacroType.PROTEIN.toColor(),
                placeholder = Res.string.placeholder_protein,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            ) {
                callbacks.onTextFieldUpdated(value = it, type = AddFoodTextFieldType.PROTEIN)
            }
            AddIngredientTextField(
                modifier = Modifier.weight(weight = 1f),
                title = Res.string.carbs_g,
                titleTextColor = MacroType.CARBS.toColor(),
                borderColor = MacroType.CARBS.toColor(),
                placeholder = Res.string.placeholder_carbs,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            ) {
                callbacks.onTextFieldUpdated(value = it, type = AddFoodTextFieldType.CARBS)
            }
            AddIngredientTextField(
                modifier = Modifier.weight(weight = 1f),
                title = Res.string.fat_g,
                titleTextColor = MacroType.FAT.toColor(),
                borderColor = MacroType.FAT.toColor(),
                placeholder = Res.string.placeholder_fats,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                )
            ) {
                callbacks.onTextFieldUpdated(value = it, type = AddFoodTextFieldType.FATS)
            }
        }

        AddIngredientPreviewCalories(
            modifier = Modifier.fillMaxWidth(),
            calories = viewState.calories,
            protein = viewState.protein ?: 0.0,
            fats = viewState.fat ?: 0.0,
            carbs = viewState.carbs ?: 0.0
        )

        CreateFoodMacroTracker(
            modifier = Modifier.fillMaxWidth(),
            proteinPercentage = viewState.proteinPercentage,
            carbsPercentage = viewState.carbsPercentage,
            fatsPercentage = viewState.fatPercentage
        )

        Spacer(modifier = Modifier.weight(weight = 1f))
        CommonButton(
            buttonText = stringResource(resource = Res.string.save_food),
            isEnabled = viewState.buttonEnabled
        ) {
            callbacks.onSaveFood()
        }
    }
}

@Composable
private fun AddIngredientTextField(
    modifier: Modifier = Modifier.height(height = 32.dp),
    title: StringResource,
    placeholder: StringResource,
    titleTextColor: Color,
    borderColor: Color,
    isEnabled: Boolean = true,
    onTextFieldSize: (IntSize) -> Unit = {},
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onValueChanged: (String) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(resource = title),
            color = titleTextColor,
            style = typography.medium11
        )
        CommonTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { size ->
                    onTextFieldSize(size)
                },
            placeholder = placeholder,
            borderColor = borderColor,
            onValueChanged = onValueChanged,
            isEnabled = isEnabled,
            isError = isError,
            keyboardOptions = keyboardOptions
        )
    }
}

enum class AddFoodTextFieldType {
    FOOD_NAME,
    AMOUNT_IN_GRAMS,
    FATS,
    PROTEIN,
    CARBS
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewAddIngredientScreen() {
    MacroTrackTheme {
        AddIngredientScreen(
            modifier = Modifier.fillMaxSize().padding(all = 16.dp),
            viewState = CreateFoodViewState(),
            callbacks = CreateFoodCallbacks.default()
        )
    }
}

