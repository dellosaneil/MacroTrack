package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.MealType

interface AddMealCallbacks {

    fun onMealTypeSelected(mealType: MealType)

    companion object {
        fun default() = object : AddMealCallbacks {
            override fun onMealTypeSelected(mealType: MealType) {
                TODO("Not yet implemented")
            }

        }
    }
}
