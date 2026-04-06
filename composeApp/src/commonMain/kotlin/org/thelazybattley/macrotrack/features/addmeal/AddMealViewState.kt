package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

data class AddMealViewState(
    val selectedMealType: MealType = MealType.BREAKFAST,
    val selectedMealFilter: MealFilter = MealFilter.FOODS,
    val filteredFoodList: List<Food> = emptyList(),
    val searchQuery: String = "",
    val completeFoodList: List<Food> = emptyList(),
    val navigateDestination: AppDestinations.Root? = null,
    val loggedMeals : AddMealLoggedFood = AddMealLoggedFood(),
    val highlightedFood: Food? = null
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
