package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.ui.FoodFilter

data class AddMealViewState(
    val selectedMealType: MealType =  MealType.BREAKFAST,
    val selectedFoodFilter: FoodFilter = FoodFilter.ALL
)
