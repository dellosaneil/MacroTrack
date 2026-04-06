package org.thelazybattley.macrotrack.features.addmeal.tabs.food.ui

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
import org.thelazybattley.macrotrack.features.addmeal.tabs.food.AddFoodCallbacks
import org.thelazybattley.macrotrack.features.addmeal.ui.AddMealItemCard
import org.thelazybattley.macrotrack.features.addmeal.ui.AddMealSelectedItem
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors

@Composable
fun AddFoodTabScreen(
    modifier: Modifier = Modifier,
    viewState: AddMealViewState,
    addFoodCallbacks: AddFoodCallbacks
) {
    LazyColumn(modifier = modifier) {
        items(
            items = viewState.filteredFoodList,
            key = { food -> food.name }
        ) { food ->
            when {
                viewState.highlightedFood?.name == food.name -> {
                    AddFoodCustomizeWeight(
                        modifier = Modifier.fillMaxWidth(),
                        food = viewState.highlightedFood,
                        calories = food.macros.calories,
                        onCloseButtonClick = {
                            addFoodCallbacks.closeCustomizeFoodWeight()
                        },
                        onPortionSizeUpdated = { weight ->
                            addFoodCallbacks.updateWeight(portionSize = weight)
                        },
                        onAddMealClick = {
                            addFoodCallbacks.addCustomizedFood()
                        },
                        originalWeight = food.weight,
                        mealType = viewState.selectedMealType
                    )
                }

                viewState.loggedMeals.loggedMeals.contains(element = food) -> {
                    AddMealSelectedItem(
                        modifier = Modifier.fillMaxWidth(),
                        food = food
                    )
                }

                else -> {
                    AddMealItemCard(
                        modifier = Modifier.fillMaxWidth(),
                        food = food,
                        onAddButtonClicked = {
                            addFoodCallbacks.insertFood(food = food)
                        },
                        onFoodHighlighted = {
                            addFoodCallbacks.customizeFoodWeight(name = food.name)
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


@Preview
@Composable
private fun PreviewAddFoodTabScreen() {
    MacroTrackTheme {
        AddFoodTabScreen(
            modifier = Modifier,
            addFoodCallbacks = AddFoodCallbacks.default(),
            viewState = AddMealViewState()
        )
    }
}
