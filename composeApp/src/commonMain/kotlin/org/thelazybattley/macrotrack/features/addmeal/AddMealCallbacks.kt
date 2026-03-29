package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.ui.FoodFilter
import org.thelazybattley.macrotrack.ui.navigation.MacroTrackDestination

interface AddMealCallbacks {

    fun onMealTypeSelected(mealType: MealType)

    fun onFoodFilterSelected(foodFilter: FoodFilter)

    fun onInsertFoodLog(food: Food)

    fun onNavigateScreen(destination: MacroTrackDestination)

    fun resetNavigateScreen()


    companion object {
        fun default() = object : AddMealCallbacks {
            override fun onMealTypeSelected(mealType: MealType) {
                TODO("Not yet implemented")
            }

            override fun onFoodFilterSelected(foodFilter: FoodFilter) {
                TODO("Not yet implemented")
            }

            override fun onInsertFoodLog(food: Food) {
                TODO("Not yet implemented")
            }

            override fun onNavigateScreen(destination: MacroTrackDestination) {
                TODO("Not yet implemented")
            }

            override fun resetNavigateScreen() {
                TODO("Not yet implemented")
            }

        }
    }
}
