package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.Ingredient
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter

data class AddMealViewState(
    val selectedMealType: MealType = MealType.BREAKFAST,
    val selectedMealFilter: MealFilter = MealFilter.FOODS,
    val filteredFoodList: List<Food> = emptyList(),
    val completeFoodList: List<Food> = emptyList(),
    val destinationRoute: String? = null,
    val loggedMeals: AddMealLoggedFood = AddMealLoggedFood(),
    val highlightedFood: Food? = null,
    val recipeList: List<RecipeMeal> = emptyList(),
    val filteredRecipeList: List<RecipeMeal> = emptyList(),
    val highlightedRecipe: RecipeMeal? = null
)

data class AddMealLoggedFood(
    val name: String = "",
    val id: Long = 0L,
    val totalCarbs: Int = 0,
    val totalFat: Int = 0,
    val totalProtein: Int = 0,
    val totalCalories: Int = 0,
    val loggedMeals: List<Food> = emptyList()
)

data class RecipeMeal(
    val name: String,
    val food: Food,
    val ingredients: List<Ingredient>,
    val percentage: Double,
)
