package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.ui.FoodFilter
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

data class AddMealViewState(
    val selectedMealType: MealType =  MealType.BREAKFAST,
    val selectedFoodFilter: FoodFilter = FoodFilter.ALL,
    val filteredFoodList: List<Food> = emptyList(),
    val searchQuery: String = "",
    val completeFoodList: List<Food> = emptyList(),
    val navigateDestination: AppDestinations.Root? = null,
)
