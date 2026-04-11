package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter

sealed interface AddMealCallbacks {
    interface MainScreenCallbacks : AddMealCallbacks {
        fun onMealFilterSelected(mealFilter: MealFilter)

        fun onNavigateScreen(route: String)

        fun resetNavigateScreen()

        fun onRevertLog()

        fun onSearchQuery(query: String)
    }

    interface FoodCallbacks : AddMealCallbacks {
        fun insertFood(food: Food)

        fun customizeFoodWeight(name: String)

        fun closeCustomizeFoodWeight()

        fun updateWeight(portionSize: Double)

        fun addCustomizedFood()
    }

    interface RecipeCallbacks : AddMealCallbacks {
        fun insertRecipe(name: String, percentage: Double)

        fun onRecipeSelected(name: String)

        fun closeSelectedRecipe()

        fun addCustomizedFood()

        fun onPercentageEatenValue(value: Double)

        fun insertSelectedRecipe()

        fun updateRecipe(name: String)

        fun deleteRecipe(name: String)
    }

    companion object {
        fun defaultRecipeCallbacks() = object : RecipeCallbacks {
            override fun insertRecipe(name: String, percentage: Double) {
                TODO("Not yet implemented")
            }

            override fun onRecipeSelected(name: String) {
                TODO("Not yet implemented")
            }

            override fun closeSelectedRecipe() {
                TODO("Not yet implemented")
            }

            override fun addCustomizedFood() {
                TODO("Not yet implemented")
            }

            override fun onPercentageEatenValue(value: Double) {
                TODO("Not yet implemented")
            }

            override fun insertSelectedRecipe() {
                TODO("Not yet implemented")
            }

            override fun updateRecipe(name: String) {
                TODO("Not yet implemented")
            }

            override fun deleteRecipe(name: String) {
                TODO("Not yet implemented")
            }
        }

        fun defaultMainScreenCallbacks() = object : MainScreenCallbacks {
            override fun onMealFilterSelected(mealFilter: MealFilter) {
                TODO("Not yet implemented")
            }

            override fun onNavigateScreen(route: String) {
                TODO("Not yet implemented")
            }

            override fun resetNavigateScreen() {
                TODO("Not yet implemented")
            }

            override fun onRevertLog() {
                TODO("Not yet implemented")
            }

            override fun onSearchQuery(query: String) {
                TODO("Not yet implemented")
            }
        }

        fun defaultFoodCallbacks() = object : FoodCallbacks {
            override fun insertFood(food: Food) {
                TODO("Not yet implemented")
            }

            override fun customizeFoodWeight(name: String) {
                TODO("Not yet implemented")
            }

            override fun closeCustomizeFoodWeight() {
                TODO("Not yet implemented")
            }

            override fun updateWeight(portionSize: Double) {
                TODO("Not yet implemented")
            }

            override fun addCustomizedFood() {
                TODO("Not yet implemented")
            }
        }
    }
}
