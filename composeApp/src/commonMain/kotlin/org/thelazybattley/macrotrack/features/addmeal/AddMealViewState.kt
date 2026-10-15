package org.thelazybattley.macrotrack.features.addmeal

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.Ingredient
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter

data class AddMealViewState(
    val selectedMealType: MealType = MealType.BREAKFAST,
    /** The tab the user picked; null until they pick one, see [activeFilter]. */
    val selectedMealFilter: MealFilter? = null,
    val completeFoodList: List<Food> = emptyList(),
    val recipeList: List<RecipeMeal> = emptyList(),
    /** The most recent log of each food or recipe, by [mealItemKey]. */
    val lastLogged: Map<String, LastLogged> = emptyMap(),
    val favoriteKeys: Set<String> = emptySet(),
    val searchQuery: String = "",
    val selection: List<SelectedMealItem> = emptyList(),
    val focusedKey: String? = null,
    /** A food the barcode scanner just saved; it's pinned to the top of the list and called out until logged. */
    val justScannedName: String? = null,
    val remainingCalories: Int? = null,
    val destinationRoute: String? = null,
    val isSaved: Boolean = false
) {
    /** Opens on Recent once anything has been logged, otherwise on All. */
    val activeFilter: MealFilter
        get() = selectedMealFilter ?: if (lastLogged.isEmpty()) MealFilter.ALL else MealFilter.RECENT

    val isSearching: Boolean get() = searchQuery.isNotBlank()

    /**
     * The list for the active tab, narrowed by the search: Recent is newest first, Favorites, All
     * and Recipes are A to Z.
     */
    val visibleItems: List<MealListItem>
        get() {
            val foods = completeFoodList.map { mealListItem(food = it, isRecipe = false) }
            val recipes = recipeList.map { mealListItem(food = it.food, isRecipe = true) }
            val items = (foods + recipes).filter { it.food.name.contains(other = searchQuery.trim(), ignoreCase = true) }
            val byName = compareBy<MealListItem> { it.food.name.lowercase() }
            val justScanned = items.find { !it.isRecipe && it.food.name == justScannedName }
            val list = when (activeFilter) {
                MealFilter.RECENT -> items
                    .filter { it.lastLogged != null }
                    .sortedByDescending { it.lastLogged?.order }
                    .take(n = RECENT_LIMIT)
                MealFilter.FAVORITES -> items.filter { it.isFavorite }.sortedWith(byName)
                MealFilter.ALL -> items.sortedWith(byName)
                MealFilter.RECIPES -> items.filter { it.isRecipe }.sortedWith(byName)
            }
            return if (justScanned == null) list else listOf(justScanned) + (list - justScanned)
        }

    val favoriteCount: Int get() = (completeFoodList.map { mealItemKey(name = it.name, isRecipe = false) } +
        recipeList.map { mealItemKey(name = it.name, isRecipe = true) }).count { it in favoriteKeys }

    val focusedItem: SelectedMealItem? get() = selection.find { it.key == focusedKey }

    val selectionCalories: Int get() = selection.sumOf { it.food.macros.calories }

    private fun mealListItem(food: Food, isRecipe: Boolean): MealListItem {
        val key = mealItemKey(name = food.name, isRecipe = isRecipe)
        return MealListItem(
            food = food,
            isRecipe = isRecipe,
            isFavorite = key in favoriteKeys,
            lastLogged = lastLogged[key],
            isJustScanned = !isRecipe && food.name == justScannedName
        )
    }

    companion object {
        const val RECENT_LIMIT = 20
    }
}

data class MealListItem(
    val food: Food,
    val isRecipe: Boolean,
    val isFavorite: Boolean = false,
    val lastLogged: LastLogged? = null,
    val isJustScanned: Boolean = false
) {
    val key: String get() = mealItemKey(name = food.name, isRecipe = isRecipe)
}

/**
 * The last time a food or recipe was logged. [amount] is the portion (grams/units, or the
 * percentage eaten for a recipe) and [food] holds the macros for that portion; [order] sorts
 * logs newest first.
 */
data class LastLogged(
    val order: Long,
    val date: LocalDate,
    val amount: Double,
    val food: Food
)

/**
 * An item picked to be logged. [amount] is grams/units for a food and the percentage eaten for a
 * recipe; [food] holds the macros for that amount.
 */
data class SelectedMealItem(
    val key: String,
    val isRecipe: Boolean,
    val baseFood: Food,
    val amount: Double,
    val food: Food,
    /** The portion logged last time, if this was logged before. */
    val lastAmount: Double? = null
)

data class RecipeMeal(
    val name: String,
    val food: Food,
    val ingredients: List<Ingredient>,
    val percentage: Double,
)

fun mealItemKey(name: String, isRecipe: Boolean): String = (if (isRecipe) "recipe:" else "food:") + name
