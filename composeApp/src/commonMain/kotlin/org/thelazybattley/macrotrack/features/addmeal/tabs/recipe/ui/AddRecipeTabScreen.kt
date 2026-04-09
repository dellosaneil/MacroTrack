package org.thelazybattley.macrotrack.features.addmeal.tabs.recipe.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.thelazybattley.macrotrack.features.addmeal.AddMealViewState
import org.thelazybattley.macrotrack.features.addmeal.tabs.recipe.AddRecipeCallbacks
import org.thelazybattley.macrotrack.features.addmeal.ui.AddMealItemCard
import org.thelazybattley.macrotrack.features.addmeal.ui.AddMealSelectedItem
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
fun AddRecipeTabScreen(
    modifier: Modifier = Modifier,
    callbacks: AddRecipeCallbacks,
    viewState: AddMealViewState,
) {
    LazyColumn(modifier = modifier) {
        items(items = viewState.filteredRecipeList, key = { it.name }) { recipe ->
            when {
                viewState.loggedMeals.loggedMeals.any { it.name == recipe.food.name } -> {
                    AddMealSelectedItem(
                        modifier = Modifier.fillMaxWidth(),
                        food = recipe.food
                    )
                }

                else -> {
                    AddMealItemCard(
                        modifier = Modifier.fillMaxWidth(),
                        food = recipe.food,
                        onButtonClicked = {
//                            addFoodCallbacks.insertFood(food = food)
                        },
                        onMealClicked = {
//                            addFoodCallbacks.customizeFoodWeight(name = food.name)
                        }
                    )
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
            callbacks = AddRecipeCallbacks.default(),
            viewState = AddMealViewState()
        )
    }
}
