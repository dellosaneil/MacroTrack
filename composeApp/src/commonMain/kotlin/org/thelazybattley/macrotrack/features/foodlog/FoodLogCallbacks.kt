package org.thelazybattley.macrotrack.features.foodlog

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.MealType

interface FoodLogCallbacks {

    fun onNavigate(mealType: MealType)
    fun resetNavigateMealTypeParameter()
    fun onDeleteFoodLog(id: Long)
    fun onDateFilterSelected(date: LocalDate)


    companion object {
        fun default() = object : FoodLogCallbacks {
            override fun onNavigate(mealType: MealType) {
                TODO("Not yet implemented")
            }

            override fun resetNavigateMealTypeParameter() {
                TODO("Not yet implemented")
            }

            override fun onDeleteFoodLog(id: Long) {
                TODO("Not yet implemented")
            }

            override fun onDateFilterSelected(date: LocalDate) {
                TODO("Not yet implemented")
            }
        }
    }
}
