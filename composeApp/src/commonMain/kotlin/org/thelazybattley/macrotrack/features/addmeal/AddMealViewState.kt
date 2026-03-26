package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.MealType

data class AddMealViewState(
    val selectedMealType: MealType =  MealType.BREAKFAST
)
