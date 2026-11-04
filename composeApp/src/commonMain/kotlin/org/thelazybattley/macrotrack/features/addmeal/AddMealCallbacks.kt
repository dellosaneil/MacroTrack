package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.OnlineFood
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
    fun onSearchScopeSelected(scope: SearchScope)
    fun onAddOnlineFood(food: OnlineFood)
    fun onOpenOnlineFood(food: OnlineFood)
    fun onCloseOnlineFood()
    fun onAddOnlineFoodPortion(food: OnlineFood, amount: Double)
    fun onSaveOnlineFood(food: OnlineFood)
    fun onFixOnlineFood(food: OnlineFood)
    fun onRetryOnline()
    fun onShowMoreOnline()
    fun onUndoOnlineNotice()
    fun onDismissOnlineNotice()

    /** Stops the next-day heads-up for the food with [key], with Undo. */
    fun onHideHeadsUp(key: String)
    fun onUndoHeadsUp(key: String)

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
            override fun onSearchScopeSelected(scope: SearchScope) {}
            override fun onAddOnlineFood(food: OnlineFood) {}
            override fun onOpenOnlineFood(food: OnlineFood) {}
            override fun onCloseOnlineFood() {}
            override fun onAddOnlineFoodPortion(food: OnlineFood, amount: Double) {}
            override fun onSaveOnlineFood(food: OnlineFood) {}
            override fun onFixOnlineFood(food: OnlineFood) {}
            override fun onRetryOnline() {}
            override fun onShowMoreOnline() {}
            override fun onUndoOnlineNotice() {}
            override fun onDismissOnlineNotice() {}
            override fun onHideHeadsUp(key: String) {}
            override fun onUndoHeadsUp(key: String) {}
        }
    }
}
