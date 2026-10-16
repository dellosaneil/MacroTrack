package org.thelazybattley.macrotrack.features.foodlibrary

import org.thelazybattley.macrotrack.domain.model.LibraryCategory
import org.thelazybattley.macrotrack.domain.model.LibraryFood
import org.thelazybattley.macrotrack.domain.model.MealType

data class FoodLibraryViewState(
    val mealType: MealType = MealType.BREAKFAST,
    val foods: List<LibraryFood> = emptyList(),
    val query: String = "",
    /** The picked category chip; null is Popular. A search ignores it. */
    val category: LibraryCategory? = null,
    /** Names of the foods picked to log, in the order they were picked. */
    val selection: List<String> = emptyList(),
    /** The food whose nutrition sheet is open. */
    val openFoodName: String? = null,
    val destinationRoute: String? = null,
    val isSaved: Boolean = false
) {
    val isSearching: Boolean get() = query.isNotBlank()

    val visibleFoods: List<LibraryFood>
        get() = when {
            isSearching -> foods.filter { it.food.name.contains(other = query.trim(), ignoreCase = true) }
            category == null -> foods.filter { it.isPopular }
            else -> foods.filter { it.category == category }
        }

    val openFood: LibraryFood? get() = foods.find { it.food.name == openFoodName }
}
