package org.thelazybattley.macrotrack.features.createrecipe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.create_new_recipe
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.features.createfood.ui.AddIngredientPreviewCalories
import org.thelazybattley.macrotrack.features.createfood.ui.CreateFoodMacroTracker
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeCallbacks
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeViewModel
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeViewState
import org.thelazybattley.macrotrack.features.navigation.AppPadding
import org.thelazybattley.macrotrack.ui.common.CommonTopBar
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
fun CreateRecipeScreen(
    modifier: Modifier = Modifier,
    onBackButtonPressed: () -> Unit
) {
    val viewModel = koinViewModel<CreateRecipeViewModel>()
    val viewState by viewModel.state.collectAsState()

    Scaffold(
        containerColor = colors.white,
        modifier = modifier.fillMaxSize(),
        topBar = {
            CommonTopBar(
                stringResource = Res.string.create_new_recipe
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
        Surface(
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(size = 16.dp),
        ) {
            AddIngredientPreviewCalories(
                modifier = Modifier.fillMaxWidth(),
                calories = 90,
                fats = 2.0,
                protein = 4.0,
                carbs = 14.0
            )
        }

        CreateFoodMacroTracker(
            modifier = Modifier.fillMaxWidth(),
            proteinPercentage = 0.33,
            carbsPercentage = 0.33,
            fatsPercentage = 0.34
        )
        CreateRecipeAddedIngredients(modifier = Modifier.fillMaxWidth())
        CreateRecipeAddIngredients(modifier = Modifier.fillMaxWidth().weight(weight = 1f))


    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCreateRecipeScreen() {
    MacroTrackTheme {
        CreateRecipeScreen(
            modifier = Modifier.padding(paddingValues = AppPadding),
            viewState = CreateRecipeViewState(),
            callbacks = CreateRecipeCallbacks.default()
        )
    }
}
