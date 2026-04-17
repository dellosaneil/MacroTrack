package org.thelazybattley.macrotrack.features.createrecipe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_ingredient
import macrotrack.composeapp.generated.resources.ic_search
import macrotrack.composeapp.generated.resources.search_ingredient
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.features.addmeal.tabs.food.ui.AddFoodCustomizeWeight
import org.thelazybattley.macrotrack.features.addmeal.ui.AddMealItemCard
import org.thelazybattley.macrotrack.features.addmeal.ui.AddMealSelectedItem
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeCallbacks
import org.thelazybattley.macrotrack.features.createrecipe.CreateRecipeViewState
import org.thelazybattley.macrotrack.ui.common.CommonTextField
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun CreateRecipeAddIngredients(
    modifier: Modifier = Modifier,
    viewState: CreateRecipeViewState,
    callbacks: CreateRecipeCallbacks
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val lazyListState = rememberLazyListState()

    LaunchedEffect(key1 = viewState.highlightedIngredient?.name) {
        val index = viewState.filteredIngredients.indexOfFirst { food ->
            food.name == viewState.highlightedIngredient?.name
        }
        if (index == -1) return@LaunchedEffect
        keyboardController?.hide()
        lazyListState.animateScrollToItem(index = index)
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(resource = Res.string.add_ingredient),
            style = typography.regular10,
            color = colors.mediumGray
        )
        var textValue by rememberSaveable { mutableStateOf(value = "") }
        CommonTextField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(resource = Res.string.search_ingredient),
            prefixIcon = Res.drawable.ic_search,
            onValueChanged = { keyword ->
                textValue = keyword
                callbacks.onSearchKeyword(keyword)
            },
            textValue = textValue
        )
        LazyColumn(state = lazyListState) {
            items(
                items = viewState.filteredIngredients,
                key = { it.name }
            ) { food ->
                when {
                    viewState.highlightedIngredient?.name == food.name -> {
                        AddFoodCustomizeWeight(
                            modifier = Modifier.fillMaxWidth(),
                            food = viewState.highlightedIngredient,
                            calories = food.macros.calories,
                            onCloseButtonClick = {
                                callbacks.closeCustomWeight()
                            },
                            onPortionSizeUpdated = { weight ->
                                callbacks.updateWeight(portionSize = weight)
                            },
                            onAddMealClick = {
                                callbacks.addCustomizedIngredient()
                            },
                            originalWeight = food.weight,
                            buttonText = stringResource(resource = Res.string.add_ingredient)
                        )
                    }

                    viewState.selectedIngredients.find { it.name == food.name } != null -> {
                        AddMealSelectedItem(
                            modifier = Modifier.fillMaxWidth(),
                            food = food
                        )
                    }

                    else -> {
                        AddMealItemCard(
                            modifier = Modifier.fillMaxWidth(),
                            food = food,
                            onButtonClicked = {
                                callbacks.onAddIngredient(food = food)
                            },
                            onMealClicked = {
                                callbacks.customizeIngredientWeight(name = food.name)
                            }
                        )
                    }
                }
                HorizontalDivider(
                    thickness = 1.dp, color = colors.lightGray,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    MacroTrackTheme {
        CreateRecipeAddIngredients(
            modifier = Modifier.fillMaxWidth(),
            viewState = CreateRecipeViewState(),
            callbacks = CreateRecipeCallbacks.default()
        )
    }
}
