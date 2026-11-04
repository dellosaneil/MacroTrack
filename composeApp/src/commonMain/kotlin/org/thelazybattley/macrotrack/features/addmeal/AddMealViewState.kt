package org.thelazybattley.macrotrack.features.addmeal

import org.thelazybattley.macrotrack.domain.model.DinnerHeadsUp
import org.thelazybattley.macrotrack.domain.model.DinnerItem
import org.thelazybattley.macrotrack.domain.model.NEXT_DAY_HEADS_UP_KG
import org.thelazybattley.macrotrack.domain.model.NextDayOverview
import org.thelazybattley.macrotrack.domain.model.NextDaySettings
import org.thelazybattley.macrotrack.domain.model.dinnerHeadsUp
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.Ingredient
import org.thelazybattley.macrotrack.domain.model.LibraryFood
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.OnlineFood
import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter

data class AddMealViewState(
    val selectedMealType: MealType = MealType.BREAKFAST,
    /** The day the picked foods are logged to: today, or a past day opened in Food Log. */
    val logDate: LocalDate = getCurrentDate(),
    /** The tab the user picked; null until they pick one, see [activeFilter]. */
    val selectedMealFilter: MealFilter? = null,
    val completeFoodList: List<Food> = emptyList(),
    val recipeList: List<RecipeMeal> = emptyList(),
    /** The built-in food library; its foods show in Recent and Favorites once logged or starred, and in search. */
    val libraryFoods: List<LibraryFood> = emptyList(),
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
    val isSaved: Boolean = false,
    /** Which group of search results shows: all of them, or only one. */
    val searchScope: SearchScope = SearchScope.ALL,
    val online: OnlineResults = OnlineResults(),
    /** The online food whose details are open. */
    val openOnlineFood: OnlineFood? = null,
    /** Online foods saved to My foods while on this screen: barcode to the name they were saved under. */
    val savedOnline: Map<String, String> = emptyMap(),
    val onlineNotice: OnlineNotice? = null,
    /** What each dinner did to the next morning's weigh-in over the year before [logDate]. */
    val nextDay: NextDayOverview? = null,
    val nextDaySettings: NextDaySettings = NextDaySettings(),
    /** What's already logged under Dinner on [logDate]. */
    val loggedDinner: List<DinnerItem> = emptyList(),
    /** Dinners whose heads-up was turned off on this screen, so the card can offer Undo. */
    val headsUpTurnedOff: Set<String> = emptySet()
) {
    /**
     * Tomorrow's weigh-in after tonight's dinner: what's logged plus what's picked, when it's going to Dinner and
     * matches a past dinner that usually adds [NEXT_DAY_HEADS_UP_KG] or more.
     */
    val dinnerHeadsUp: DinnerHeadsUp?
        get() {
            if (selectedMealType != MealType.DINNER || selection.isEmpty()) return null
            val picked = selection.map { item ->
                DinnerItem(
                    name = item.food.name,
                    isRecipe = item.isRecipe,
                    isQuickAdd = false,
                    date = logDate,
                    amount = item.amount,
                    calories = item.food.macros.calories,
                    protein = item.food.macros.protein,
                    carbs = item.food.macros.carbs,
                    fat = item.food.macros.fat
                )
            }
            return dinnerHeadsUp(items = loggedDinner + picked, overview = nextDay, settings = nextDaySettings, keepOff = headsUpTurnedOff)
        }

    /** Opens on Recent once anything has been logged, otherwise on My foods. */
    val activeFilter: MealFilter
        get() = selectedMealFilter ?: if (lastLogged.isEmpty()) MealFilter.MY_FOODS else MealFilter.RECENT

    val isSearching: Boolean get() = searchQuery.isNotBlank()

    /**
     * The list for the active tab: Recent is newest first; Favorites, My foods and Recipes are A to Z. Library
     * foods only appear on Recent and Favorites, once they've been logged or starred.
     */
    val visibleItems: List<MealListItem>
        get() {
            val byName = compareBy<MealListItem> { it.food.name.lowercase() }
            val justScanned = savedItems.find { !it.isRecipe && it.food.name == justScannedName }
            val list = when (activeFilter) {
                MealFilter.RECENT -> allItems
                    .filter { it.lastLogged != null }
                    .sortedByDescending { it.lastLogged?.order }
                    .take(n = RECENT_LIMIT)
                MealFilter.FAVORITES -> allItems.filter { it.isFavorite }.sortedWith(byName)
                MealFilter.MY_FOODS -> savedItems.filter { !it.isRecipe }.sortedWith(byName)
                MealFilter.RECIPES -> savedItems.filter { it.isRecipe }.sortedWith(byName)
            }
            return if (justScanned == null) list else listOf(justScanned) + (list - justScanned)
        }

    /** The user's own foods and recipes that match the search, A to Z. */
    val searchMatches: List<MealListItem>
        get() = savedItems.filter { it.matchesSearch() }.sortedBy { it.food.name.lowercase() }

    /** Library foods that match the search, A to Z; Add meal shows the first [LIBRARY_PREVIEW_LIMIT]. */
    val librarySearchMatches: List<MealListItem>
        get() = libraryItems.filter { it.matchesSearch() }

    val favoriteCount: Int get() = allItems.count { it.isFavorite }

    /** Whether an online food is picked for the meal, through the copy saved in My foods. */
    fun isOnlineSelected(food: OnlineFood): Boolean {
        val name = savedOnline[food.barcode] ?: return false
        return selection.any { it.key == mealItemKey(name = name, kind = MealItemKind.FOOD) }
    }

    val focusedItem: SelectedMealItem? get() = selection.find { it.key == focusedKey }

    val selectionCalories: Int get() = selection.sumOf { it.food.macros.calories }

    private val savedItems: List<MealListItem>
        get() = completeFoodList.map { mealListItem(food = it, kind = MealItemKind.FOOD) } +
            recipeList.map { mealListItem(food = it.food, kind = MealItemKind.RECIPE) }

    private val libraryItems: List<MealListItem>
        get() = libraryFoods.map { mealListItem(food = it.food, kind = MealItemKind.LIBRARY) }

    private val allItems: List<MealListItem> get() = savedItems + libraryItems

    private fun MealListItem.matchesSearch(): Boolean = food.name.contains(other = searchQuery.trim(), ignoreCase = true)

    private fun mealListItem(food: Food, kind: MealItemKind): MealListItem {
        val key = mealItemKey(name = food.name, kind = kind)
        return MealListItem(
            food = food,
            kind = kind,
            isFavorite = key in favoriteKeys,
            lastLogged = lastLogged[key],
            isJustScanned = kind == MealItemKind.FOOD && food.name == justScannedName
        )
    }

    companion object {
        const val RECENT_LIMIT = 20
        const val LIBRARY_PREVIEW_LIMIT = 3

        /** Shorter searches aren't sent online: too many loose matches. */
        const val ONLINE_MIN_QUERY = 3
    }
}

/** The groups of search results; [ALL] shows each in turn. */
enum class SearchScope { ALL, YOURS, LIBRARY, ONLINE }

enum class OnlineStatus {
    /** The search is too short to send. */
    IDLE,
    LOADING,
    LOADING_MORE,
    OFFLINE,
    DONE
}

/** Online results for [query], [page] pages of them so far. */
data class OnlineResults(
    val query: String = "",
    val status: OnlineStatus = OnlineStatus.IDLE,
    val foods: List<OnlineFood> = emptyList(),
    val totalCount: Int = 0,
    val page: Int = 0
) {
    val canShowMore: Boolean get() = status == OnlineStatus.DONE && foods.size < totalCount && foods.isNotEmpty()
}

/**
 * What was just done with an online food, with Undo: saved to My foods and, when [isSelected], picked for the meal.
 * [isNew] is false when it was already in My foods, so Undo leaves it there. [id] tells two in a row apart.
 */
data class OnlineNotice(
    val foodName: String,
    val savedName: String,
    val isSelected: Boolean,
    val isNew: Boolean,
    val id: Long
)

data class MealListItem(
    val food: Food,
    val kind: MealItemKind,
    val isFavorite: Boolean = false,
    val lastLogged: LastLogged? = null,
    val isJustScanned: Boolean = false
) {
    val key: String get() = mealItemKey(name = food.name, kind = kind)
    val isRecipe: Boolean get() = kind == MealItemKind.RECIPE

    /** A built-in library food: read-only, so it can't be edited or deleted. */
    val isLibrary: Boolean get() = kind == MealItemKind.LIBRARY
}

/** Where an Add meal item comes from: the user's foods, their recipes, or the built-in food library. */
enum class MealItemKind(val keyPrefix: String) {
    FOOD(keyPrefix = "food:"),
    RECIPE(keyPrefix = "recipe:"),
    LIBRARY(keyPrefix = "library:")
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
    val isLibrary: Boolean = false,
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

/** Favorites and last-logged portions are stored under this key. */
fun mealItemKey(name: String, kind: MealItemKind): String = kind.keyPrefix + name
