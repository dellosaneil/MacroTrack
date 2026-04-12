package org.thelazybattley.macrotrack.features.addmeal.tabs.recipe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.add_to_value
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.features.addmeal.AddMealCallbacks
import org.thelazybattley.macrotrack.features.addmeal.AddMealLoggedFood
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewState
import org.thelazybattley.macrotrack.features.addmeal.RecipeMeal
import org.thelazybattley.macrotrack.features.addmeal.ui.AddMealItemCard
import org.thelazybattley.macrotrack.features.addmeal.ui.AddMealSelectedItem
import org.thelazybattley.macrotrack.ui.common.CommonSwipeToDelete
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
fun AddRecipeTabScreen(
    modifier: Modifier = Modifier,
    callbacks: AddMealCallbacks.RecipeCallbacks,
    viewState: AddMealViewState,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val lazyState = rememberLazyListState()
    LaunchedEffect(key1 = viewState.highlightedRecipe?.name) {
        val index = viewState.filteredRecipeList.indexOfFirst { recipe ->
            recipe.name == viewState.highlightedRecipe?.name
        }
        if(index == -1) return@LaunchedEffect
        keyboardController?.hide()
        lazyState.animateScrollToItem(index = index)
    }
    LazyColumn(modifier = modifier,
        state = lazyState) {
        items(items = viewState.filteredRecipeList, key = { it.name }) { recipe ->
            when {
                viewState.highlightedRecipe?.name == recipe.name -> {
                    val food = viewState.highlightedRecipe.food
                    AddRecipeInputPercentageEaten(
                        modifier = Modifier.fillMaxWidth(),
                        food = food,
                        onCloseButtonClick = {
                            callbacks.closeSelectedRecipe()
                        },
                        onPercentageEatenValue = { weight ->
                            callbacks.onPercentageEatenValue(value = weight)
                        },
                        onAddMealClick = {
                            callbacks.insertSelectedRecipe()
                        },
                        buttonText = stringResource(
                            resource = Res.string.add_to_value,
                            stringResource(resource = viewState.selectedMealType.title)
                        )
                    )
                }

                viewState.loggedMeals.loggedMeals.any { it.name == recipe.food.name } -> {
                    AddMealSelectedItem(
                        modifier = Modifier.fillMaxWidth(),
                        food = recipe.food
                    )
                }

                else -> {
                    CommonSwipeToDelete(
                        modifier = Modifier,
                        onDelete = {
                            callbacks.deleteRecipe(name = recipe.name)
                        }
                    ) {
                        AddMealItemCard(
                            modifier = Modifier
                                .background(color = colors.white).fillMaxWidth(),
                            food = recipe.food,
                            onButtonClicked = {
                                callbacks.insertRecipe(name = recipe.name, percentage = 1.0)
                            },
                            onMealClicked = {
                                callbacks.onRecipeSelected(name = recipe.name)
                            },
                            onLongPress = {
                                callbacks.updateRecipe(name = recipe.name)
                            }
                        )
                    }
                }
            }

            HorizontalDivider(
                thickness = 1.dp, color = colors.lightGray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAddRecipeTabScreen() {
    MacroTrackTheme {
        AddRecipeTabScreen(
            modifier = Modifier.fillMaxWidth().padding(all = 12.dp),
            callbacks = AddMealCallbacks.defaultRecipeCallbacks(),
            viewState = AddMealViewState(
                filteredRecipeList = listOf(
                    RecipeMeal(
                        name = "Food 1",
                        food = dummyFood,
                        ingredients = listOf(),
                        percentage = 1.0
                    ), RecipeMeal(
                        name = "Food 2",
                        food = dummyFood,
                        ingredients = listOf(),
                        percentage = 1.0
                    ), RecipeMeal(
                        name = "Food 3",
                        food = dummyFood.copy(name = "Food"),
                        ingredients = listOf(),
                        percentage = 1.0
                    )
                ),
                loggedMeals = AddMealLoggedFood(
                    loggedMeals = listOf(dummyFood)
                ),
                highlightedRecipe = RecipeMeal(
                    name = "Food 1",
                    food = dummyFood.copy(name = "Food"),
                    ingredients = listOf(),
                    percentage = 1.0
                )
            )
        )
    }
}
