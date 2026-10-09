package org.thelazybattley.macrotrack.features.addmeal

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.dummyFood
import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter
import kotlin.test.Test
import kotlin.test.assertEquals

class AddMealViewStateTest {

    private fun lastLogged(order: Long) = LastLogged(
        order = order,
        date = LocalDate(year = 2026, month = 10, day = 1),
        amount = 150.0,
        food = dummyFood
    )

    private val state = AddMealViewState(
        completeFoodList = listOf(
            dummyFood.copy(name = "Salmon fillet"),
            dummyFood.copy(name = "Avocado"),
            dummyFood.copy(name = "Chicken breast")
        ),
        recipeList = listOf(
            RecipeMeal(
                name = "Chicken rice bowl",
                food = dummyFood.copy(name = "Chicken rice bowl"),
                ingredients = emptyList(),
                percentage = 1.0
            )
        ),
        lastLogged = mapOf(
            "food:Salmon fillet" to lastLogged(order = 5L),
            "recipe:Chicken rice bowl" to lastLogged(order = 9L)
        ),
        favoriteKeys = setOf("food:Chicken breast", "food:Avocado", "food:Deleted food")
    )

    @Test
    fun opensOnRecentOnceSomethingIsLogged() {
        assertEquals(MealFilter.RECENT, state.activeFilter)
        assertEquals(MealFilter.ALL, state.copy(lastLogged = emptyMap()).activeFilter)
        assertEquals(MealFilter.FAVORITES, state.copy(selectedMealFilter = MealFilter.FAVORITES).activeFilter)
    }

    @Test
    fun recentShowsOnlyLoggedItemsNewestFirst() {
        assertEquals(
            listOf("recipe:Chicken rice bowl", "food:Salmon fillet"),
            state.visibleItems.map { it.key }
        )
        assertEquals(150.0, state.visibleItems.first().lastLogged?.amount)
    }

    @Test
    fun favoritesShowsStarredItemsAlphabetically() {
        val favorites = state.copy(selectedMealFilter = MealFilter.FAVORITES)
        assertEquals(listOf("Avocado", "Chicken breast"), favorites.visibleItems.map { it.food.name })
        assertEquals(true, favorites.visibleItems.all { it.isFavorite })
        // A favorite whose food was deleted is not counted.
        assertEquals(2, state.favoriteCount)
    }

    @Test
    fun allShowsEverythingAlphabetically() {
        val all = state.copy(selectedMealFilter = MealFilter.ALL)
        assertEquals(
            listOf("Avocado", "Chicken breast", "Chicken rice bowl", "Salmon fillet"),
            all.visibleItems.map { it.food.name }
        )
    }

    @Test
    fun searchStaysWithinTheActiveTab() {
        val recent = state.copy(searchQuery = "chick")
        assertEquals(listOf("recipe:Chicken rice bowl"), recent.visibleItems.map { it.key })

        val favorites = state.copy(selectedMealFilter = MealFilter.FAVORITES, searchQuery = "chick")
        assertEquals(listOf("food:Chicken breast"), favorites.visibleItems.map { it.key })

        val all = state.copy(selectedMealFilter = MealFilter.ALL, searchQuery = "chick")
        assertEquals(
            listOf("food:Chicken breast", "recipe:Chicken rice bowl"),
            all.visibleItems.map { it.key }
        )
    }
}
