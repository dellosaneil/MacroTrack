package org.thelazybattley.macrotrack.features.addingredient.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.amount
import macrotrack.composeapp.generated.resources.calories_kcal
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.carbs_g
import macrotrack.composeapp.generated.resources.chicken_breast
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.fat_g
import macrotrack.composeapp.generated.resources.g
import macrotrack.composeapp.generated.resources.ingredient_name
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.macros_per_serving
import macrotrack.composeapp.generated.resources.new_ingredient
import macrotrack.composeapp.generated.resources.one_hundred
import macrotrack.composeapp.generated.resources.placeholder_calories
import macrotrack.composeapp.generated.resources.placeholder_carbs
import macrotrack.composeapp.generated.resources.placeholder_fats
import macrotrack.composeapp.generated.resources.placeholder_protein
import macrotrack.composeapp.generated.resources.preview_per
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.protein_g
import macrotrack.composeapp.generated.resources.save_ingredient
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.addingredient.AddIngredientCallbacks
import org.thelazybattley.macrotrack.features.addingredient.AddIngredientViewModel
import org.thelazybattley.macrotrack.features.addingredient.AddIngredientViewState
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.ui.common.CommonBackButton
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun AddIngredientScreen(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<AddIngredientViewModel>()
    val viewState by viewModel.state.collectAsState()
    Scaffold(
        containerColor = colors.white,
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
    viewState: AddIngredientViewState,
    callbacks: AddIngredientCallbacks
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        TitleBar(modifier = Modifier.fillMaxWidth()) {

        }
        Spacer(modifier = Modifier)
        AddIngredientTextField(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.ingredient_name,
            titleTextColor = colors.gray,
            borderColor = colors.deepBlue,
            placeholder = Res.string.chicken_breast
        ) {
            callbacks.onTextFieldUpdated(
                value = it,
                type = AddIngredientTextFieldType.INGREDIENT_NAME
            )
        }
        Column(modifier = Modifier.fillMaxWidth()) {
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
                            type = AddIngredientTextFieldType.AMOUNT_IN_GRAMS
                        )
                    },
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

        AddIngredientTextField(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.calories_kcal,
            titleTextColor = colors.gray,
            borderColor = colors.deepBlue,
            placeholder = Res.string.placeholder_calories
        ) {
            callbacks.onTextFieldUpdated(value = it, type = AddIngredientTextFieldType.CALORIES)
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AddIngredientTextField(
                modifier = Modifier.weight(weight = 1f),
                title = Res.string.protein_g,
                titleTextColor = colors.gray,
                borderColor = colors.deepBlue,
                placeholder = Res.string.placeholder_protein
            ) {
                callbacks.onTextFieldUpdated(value = it, type = AddIngredientTextFieldType.PROTEIN)
            }
            AddIngredientTextField(
                modifier = Modifier.weight(weight = 1f),
                title = Res.string.carbs_g,
                titleTextColor = colors.green,
                borderColor = colors.green,
                placeholder = Res.string.placeholder_carbs
            ) {
                callbacks.onTextFieldUpdated(value = it, type = AddIngredientTextFieldType.CARBS)
            }
            AddIngredientTextField(
                modifier = Modifier.weight(weight = 1f),
                title = Res.string.fat_g,
                titleTextColor = colors.orange,
                borderColor = colors.orange,
                placeholder = Res.string.placeholder_fats
            ) {
                callbacks.onTextFieldUpdated(value = it, type = AddIngredientTextFieldType.FATS)
            }
        }

        PreviewMacros(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.weight(weight = 1f))
        SaveIngredient(
            modifier = Modifier.fillMaxWidth(),
            isEnabled = viewState.buttonEnabled
        ) {
            callbacks.onSaveIngredient()
        }
    }
}

@Composable
private fun SaveIngredient(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    onButtonClicked: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onButtonClicked,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = colors.skyBlue,
            disabledContentColor = colors.babyBlue,
            containerColor = colors.deepBlue,
            contentColor = colors.white
        ),
        enabled = isEnabled,
        shape = RoundedCornerShape(size = 8.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.save_ingredient),
            style = typography.bold16,
            modifier = Modifier.padding(vertical = 8.dp)
        )
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
        )
    }
}

@Composable
private fun PreviewMacros(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.iceBlue
        ),
        border = BorderStroke(
            width = 1.dp,
            color = colors.skyBlue
        )
    ) {
        Column(
            modifier = Modifier.padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Text(
                text = stringResource(resource = Res.string.preview_per, 100),
                style = typography.bold11,
                color = colors.deepBlue
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroDetail(
                    modifier = Modifier,
                    macro = Res.string.kcal_text,
                    value = "31",
                    textColor = colors.black
                )
                MacroDetail(
                    modifier = Modifier,
                    macro = Res.string.protein,
                    value = "31",
                    textColor = colors.blue
                )
                MacroDetail(
                    modifier = Modifier,
                    macro = Res.string.carbs,
                    value = "31",
                    textColor = colors.green
                )
                MacroDetail(
                    modifier = Modifier,
                    macro = Res.string.fat,
                    value = "31",
                    textColor = colors.orange
                )
            }
        }
    }
}

@Composable
private fun MacroDetail(
    modifier: Modifier = Modifier, macro: StringResource, value: String,
    textColor: Color
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
        Text(text = value, style = typography.bold16, color = textColor)
        Text(
            text = stringResource(resource = macro).lowercase(),
            color = colors.mediumGray,
            style = typography.regular10
        )
    }
}

enum class AddIngredientTextFieldType {
    INGREDIENT_NAME,
    AMOUNT_IN_GRAMS,
    CALORIES,
    FATS,
    PROTEIN,
    CARBS
}

@Composable
private fun TitleBar(modifier: Modifier = Modifier, onBackClicked: () -> Unit) {
    Box(modifier = modifier) {
        CommonBackButton(
            onButtonClicked = onBackClicked,
            modifier = Modifier.align(alignment = Alignment.CenterStart)
        )
        Text(
            text = stringResource(resource = Res.string.new_ingredient),
            style = typography.bold16,
            color = colors.black,
            modifier = Modifier.align(alignment = Alignment.Center)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewAddIngredientScreen() {
    MacroTrackTheme {
        AddIngredientScreen(
            modifier = Modifier.fillMaxSize().padding(all = 16.dp),
            viewState = AddIngredientViewState(),
            callbacks = AddIngredientCallbacks.default()
        )
    }
}
