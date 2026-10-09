package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter

interface AddMealCallbacks {
    fun onMealFilterSelected(mealFilter: MealFilter)
    fun onSearchQuery(query: String)
    fun onToggleItem(item: MealListItem)
    fun onToggleFavorite(item: MealListItem)
    fun onFocusItem(key: String)
    fun onAmountChanged(key: String, amount: Double)
    fun onAddSelection()
    fun onEditItem(item: MealListItem)
    fun onDeleteItem(item: MealListItem)
    fun onNavigateScreen(route: String)
    fun resetNavigateScreen()

    companion object {
        fun default() = object : AddMealCallbacks {
            override fun onMealFilterSelected(mealFilter: MealFilter) {}
            override fun onSearchQuery(query: String) {}
            override fun onToggleItem(item: MealListItem) {}
            override fun onToggleFavorite(item: MealListItem) {}
            override fun onFocusItem(key: String) {}
            override fun onAmountChanged(key: String, amount: Double) {}
            override fun onAddSelection() {}
            override fun onEditItem(item: MealListItem) {}
            override fun onDeleteItem(item: MealListItem) {}
            override fun onNavigateScreen(route: String) {}
            override fun resetNavigateScreen() {}
        }
    }
}
