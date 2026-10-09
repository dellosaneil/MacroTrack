package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.Ingredient
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter

data class AddMealViewState(
    val selectedMealType: MealType = MealType.BREAKFAST,
    val selectedMealFilter: MealFilter = MealFilter.ALL,
    val completeFoodList: List<Food> = emptyList(),
    val recipeList: List<RecipeMeal> = emptyList(),
    val lastLoggedOrder: Map<String, Long> = emptyMap(),
    val searchQuery: String = "",
    val selection: List<SelectedMealItem> = emptyList(),
    val focusedKey: String? = null,
    val remainingCalories: Int? = null,
    val destinationRoute: String? = null,
    val isSaved: Boolean = false
) {
    /** Foods and recipes for the selected tab and search, most recently logged first. */
    val visibleItems: List<MealListItem>
        get() {
            val foods = completeFoodList.map { MealListItem(food = it, isRecipe = false) }
            val recipes = recipeList.map { MealListItem(food = it.food, isRecipe = true) }
            val items = when (selectedMealFilter) {
                MealFilter.ALL -> foods + recipes
                MealFilter.FOODS -> foods
                MealFilter.RECIPES -> recipes
            }
            return items
                .filter { it.food.name.contains(other = searchQuery, ignoreCase = true) }
                .sortedWith(
                    compareByDescending<MealListItem> { lastLoggedOrder[it.food.name] ?: Long.MIN_VALUE }
                        .thenBy { it.food.name.lowercase() }
                )
        }

    val focusedItem: SelectedMealItem? get() = selection.find { it.key == focusedKey }

    val selectionCalories: Int get() = selection.sumOf { it.food.macros.calories }
}

data class MealListItem(
    val food: Food,
    val isRecipe: Boolean
) {
    val key: String get() = mealItemKey(name = food.name, isRecipe = isRecipe)
}

/**
 * An item picked to be logged. [amount] is grams/units for a food and the percentage eaten for a
 * recipe; [food] holds the macros for that amount.
 */
data class SelectedMealItem(
    val key: String,
    val isRecipe: Boolean,
    val baseFood: Food,
    val amount: Double,
    val food: Food
)

data class RecipeMeal(
    val name: String,
    val food: Food,
    val ingredients: List<Ingredient>,
    val percentage: Double,
)

fun mealItemKey(name: String, isRecipe: Boolean): String = (if (isRecipe) "recipe:" else "food:") + name
