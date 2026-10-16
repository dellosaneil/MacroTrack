package org.thelazybattley.macrotrack.features.addmeal

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.LibraryCategory
import org.thelazybattley.macrotrack.domain.model.LibraryFood
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
        libraryFoods = listOf(
            libraryFood(name = "Apple, medium"),
            libraryFood(name = "Chicken thigh, roasted"),
            libraryFood(name = "Chicken noodle soup"),
            libraryFood(name = "Chicken breast, roasted"),
            libraryFood(name = "Egg, large")
        ),
        lastLogged = mapOf(
            "food:Salmon fillet" to lastLogged(order = 5L),
            "recipe:Chicken rice bowl" to lastLogged(order = 9L),
            "library:Egg, large" to lastLogged(order = 7L)
        ),
        favoriteKeys = setOf("food:Chicken breast", "food:Avocado", "food:Deleted food", "library:Apple, medium")
    )

    private fun libraryFood(name: String) = LibraryFood(
        food = dummyFood.copy(name = name),
        category = LibraryCategory.PROTEIN,
        isPopular = false,
        fiber = null,
        sugar = null,
        sodium = null
    )

    @Test
    fun opensOnRecentOnceSomethingIsLogged() {
        assertEquals(MealFilter.RECENT, state.activeFilter)
        assertEquals(MealFilter.MY_FOODS, state.copy(lastLogged = emptyMap()).activeFilter)
        assertEquals(MealFilter.FAVORITES, state.copy(selectedMealFilter = MealFilter.FAVORITES).activeFilter)
    }

    @Test
    fun recentShowsOnlyLoggedItemsNewestFirst() {
        assertEquals(
            listOf("recipe:Chicken rice bowl", "library:Egg, large", "food:Salmon fillet"),
            state.visibleItems.map { it.key }
        )
        assertEquals(150.0, state.visibleItems.first().lastLogged?.amount)
    }

    @Test
    fun favoritesShowsStarredItemsAlphabetically() {
        val favorites = state.copy(selectedMealFilter = MealFilter.FAVORITES)
        assertEquals(listOf("Apple, medium", "Avocado", "Chicken breast"), favorites.visibleItems.map { it.food.name })
        assertEquals(true, favorites.visibleItems.all { it.isFavorite })
        // A favorite whose food was deleted is not counted; a starred library food is.
        assertEquals(3, state.favoriteCount)
    }

    @Test
    fun myFoodsShowsOnlySavedFoodsAlphabetically() {
        val myFoods = state.copy(selectedMealFilter = MealFilter.MY_FOODS)
        assertEquals(listOf("Avocado", "Chicken breast", "Salmon fillet"), myFoods.visibleItems.map { it.food.name })
    }

    @Test
    fun searchGroupsSavedItemsAndTheLibrary() {
        val searching = state.copy(searchQuery = " chick ")
        assertEquals(
            listOf("food:Chicken breast", "recipe:Chicken rice bowl"),
            searching.searchMatches.map { it.key }
        )
        assertEquals(
            listOf("library:Chicken breast, roasted", "library:Chicken noodle soup", "library:Chicken thigh, roasted"),
            searching.librarySearchMatches.map { it.key }.sorted()
        )
        assertEquals(true, searching.librarySearchMatches.all { it.isLibrary })
    }

    @Test
    fun searchWithNoSavedMatchStillFindsLibraryFoods() {
        val searching = state.copy(searchQuery = "apple")
        assertEquals(emptyList(), searching.searchMatches)
        assertEquals(listOf("Apple, medium"), searching.librarySearchMatches.map { it.food.name })
    }
}
