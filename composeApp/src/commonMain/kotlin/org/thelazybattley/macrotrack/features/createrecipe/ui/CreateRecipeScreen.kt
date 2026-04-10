package org.thelazybattley.macrotrack.features.createrecipe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.create_new_recipe
import macrotrack.composeapp.generated.resources.grilled_chicken_breast
import macrotrack.composeapp.generated.resources.save_recipe
import macrotrack.composeapp.generated.resources.this_food_is_already_saved
import macrotrack.composeapp.generated.resources.update_recipe
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.createfood.ui.AddIngredientPreviewCalories
import org.thelazybattley.macrotrack.features.createfood.ui.CreateFoodMacroTracker
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeCallbacks
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeMacros
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeViewModel
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeViewState
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.ui.common.CommonButton
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.common.CommonTopBar
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CreateRecipeScreen(
    modifier: Modifier = Modifier,
    onBackButtonPressed: () -> Unit
) {
    val viewModel = koinViewModel<CreateRecipeViewModel>()
    val viewState by viewModel.state.collectAsState()

    LaunchedEffect(key1 = viewState.recipeSaved) {
        if (viewState.recipeSaved) {
            onBackButtonPressed()
            return@LaunchedEffect
        }
    }
    Scaffold(
        containerColor = colors.white,
        modifier = modifier.fillMaxSize(),
        topBar = {
            val stringResource = if(viewState.isUpdating) {
                Res.string.update_recipe
            } else {
                Res.string.create_new_recipe
            }
            CommonTopBar(
                stringResource = stringResource
            ) {
                onBackButtonPressed()
            }
        }
    ) { innerPadding ->
        CreateRecipeScreen(
            modifier = modifier.padding(paddingValues = innerPadding)
                .padding(paddingValues = AppPadding),
            viewState = viewState, callbacks = viewModel
        )
    }
}

@Composable
private fun CreateRecipeScreen(
    modifier: Modifier = Modifier,
    viewState: CreateRecipeViewState,
    callbacks: CreateRecipeCallbacks
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        CommonTextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = Res.string.grilled_chicken_breast,
            borderColor = colors.deepBlue,
            isError = viewState.isRecipeNameTaken,
            textValue = viewState.recipeName,
            isEnabled = !viewState.isUpdating
        ) { name ->
            callbacks.inputRecipeName(name = name)
        }
        if (viewState.isRecipeNameTaken) {
            Text(
                text = stringResource(resource = Res.string.this_food_is_already_saved),
                style = typography.regular10,
                color = colors.crimsonRed
            )
        }
        Surface(
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(size = 16.dp),
        ) {
            AddIngredientPreviewCalories(
                modifier = Modifier.fillMaxWidth(),
                calories = viewState.macros.calories,
                fats = viewState.macros.fat,
                protein = viewState.macros.protein,
                carbs = viewState.macros.carbs
            )
        }

        CreateFoodMacroTracker(
            modifier = Modifier.fillMaxWidth(),
            proteinPercentage = viewState.macros.proteinPercentage,
            carbsPercentage = viewState.macros.carbsPercentage,
            fatsPercentage = viewState.macros.fatPercentage
        )
        CreateRecipeAddedIngredients(
            modifier = Modifier.fillMaxWidth(),
            selectedIngredients = viewState.selectedIngredients
        ) { food ->
            callbacks.removeIngredient(food = food)
        }
        CreateRecipeAddIngredients(
            modifier = Modifier.fillMaxWidth().weight(weight = 1f),
            viewState = viewState,
            callbacks = callbacks
        )
        if (viewState.highlightedIngredient == null) {
            Spacer(modifier = Modifier.height(8.dp))
            val buttonResource = if(viewState.isUpdating) {
                Res.string.update_recipe
            } else {
                Res.string.save_recipe
            }
            CommonButton(
                buttonText = stringResource(resource = buttonResource),
                isEnabled = viewState.buttonEnabled
            ) {
                callbacks.onSaveRecipe()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCreateRecipeScreen() {
    MacroTrackTheme {
        CreateRecipeScreen(
            modifier = Modifier.padding(paddingValues = AppPadding),
            viewState = CreateRecipeViewState(
                macros = CreateRecipeMacros(
                    protein = 3.0,
                    carbsPercentage = 3.0,
                    fat = 3.0,
                    proteinPercentage = 0.3,
                    fatPercentage = 0.3,
                    carbs = 0.4,
                    calories = 9
                )
            ),
            callbacks = CreateRecipeCallbacks.default()
        )
    }
}
