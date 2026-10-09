package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter
import kotlin.test.Test
import kotlin.test.assertEquals

class AddMealViewStateTest {

    private val state = AddMealViewState(
        completeFoodList = listOf(
            dummyFood.copy(name = "Avocado"),
            dummyFood.copy(name = "Chicken breast"),
            dummyFood.copy(name = "Salmon fillet")
        ),
        recipeList = listOf(
            RecipeMeal(
                name = "Chicken rice bowl",
                food = dummyFood.copy(name = "Chicken rice bowl"),
                ingredients = emptyList(),
                percentage = 1.0
            )
        ),
        lastLoggedOrder = mapOf("Salmon fillet" to 5L, "Chicken rice bowl" to 9L)
    )

    @Test
    fun allShowsRecentlyLoggedFirstThenAlphabetical() {
        assertEquals(
            listOf("Chicken rice bowl", "Salmon fillet", "Avocado", "Chicken breast"),
            state.visibleItems.map { it.food.name }
        )
    }

    @Test
    fun filterAndSearchNarrowTheList() {
        val foods = state.copy(selectedMealFilter = MealFilter.FOODS, searchQuery = "chick")
        assertEquals(listOf("Chicken breast"), foods.visibleItems.map { it.food.name })

        val recipes = state.copy(selectedMealFilter = MealFilter.RECIPES)
        assertEquals(listOf("recipe:Chicken rice bowl"), recipes.visibleItems.map { it.key })
    }
}
