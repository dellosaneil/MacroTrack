package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

interface AddMealCallbacks {

    fun onMealFilterSelected(mealFilter: MealFilter)

    fun onNavigateScreen(destination: AppDestinations.Root)

    fun resetNavigateScreen()

    fun onRevertLog()

    companion object {
        fun default() = object : AddMealCallbacks {

            override fun onMealFilterSelected(mealFilter: MealFilter) {
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
        }
    }
}
