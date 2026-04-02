package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.ui.FoodFilter
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

interface AddMealCallbacks {

    fun onMealTypeSelected(mealType: MealType)

    fun onFoodFilterSelected(foodFilter: FoodFilter)

    fun onInsertFoodLog(food: Food)

    fun onNavigateScreen(destination: AppDestinations.Root)

    fun resetNavigateScreen()

    fun onRevertLog()

    fun highlightedFood(food: Food)

    fun closeHighlightedFood()

    fun onPortionSizeChanged(portionSize: Double)

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

            override fun onNavigateScreen(destination: AppDestinations.Root) {
                TODO("Not yet implemented")
            }
            override fun resetNavigateScreen() {
                TODO("Not yet implemented")
            }

            override fun onRevertLog() {
                TODO("Not yet implemented")
            }

            override fun highlightedFood(food: Food) {
                TODO("Not yet implemented")
            }

            override fun closeHighlightedFood() {
                TODO("Not yet implemented")
            }

            override fun onPortionSizeChanged(portionSize: Double) {
                TODO("Not yet implemented")
            }

        }
    }
}
