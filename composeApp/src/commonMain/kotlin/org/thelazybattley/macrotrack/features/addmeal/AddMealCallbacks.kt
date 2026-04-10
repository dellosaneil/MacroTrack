package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter

interface AddMealCallbacks {

    fun onMealFilterSelected(mealFilter: MealFilter)

    fun onNavigateScreen(route: String)

    fun resetNavigateScreen()

    fun onRevertLog()

    fun onSearchQuery(query: String)

    companion object {
        fun default() = object : AddMealCallbacks {

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
    }
}
