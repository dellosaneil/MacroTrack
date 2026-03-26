package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.ui.FoodFilter

interface AddMealCallbacks {

    fun onMealTypeSelected(mealType: MealType)

    fun onFoodFilterSelected(foodFilter: FoodFilter)


    companion object {
        fun default() = object : AddMealCallbacks {
            override fun onMealTypeSelected(mealType: MealType) {
                TODO("Not yet implemented")
            }

            override fun onFoodFilterSelected(foodFilter: FoodFilter) {
                TODO("Not yet implemented")
            }

        }
    }
}
