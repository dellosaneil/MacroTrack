package org.thelazybattley.macrotrack.features.foodhistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodHistoryEntry
import org.thelazybattley.macrotrack.domain.model.FoodHistoryPeriod
import org.thelazybattley.macrotrack.domain.model.FoodHistoryStats
import org.thelazybattley.macrotrack.domain.model.FoodLogSource
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.defaultFitMeal
import org.thelazybattley.macrotrack.domain.model.foodHistoryEntries
import org.thelazybattley.macrotrack.domain.model.mostlyAt
import org.thelazybattley.macrotrack.domain.model.stats
import org.thelazybattley.macrotrack.domain.model.within
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.InsertFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.library.GetFoodLibraryUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.features.addmeal.FULL_RECIPE_PERCENT
import org.thelazybattley.macrotrack.features.addmeal.toRecipeMeals
import org.thelazybattley.macrotrack.features.calendar.LogDateRequest
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.FOOD_NAME
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.IS_RECIPE

/**
 * Every time [name] was logged. [base] is the food as saved (one serving, or a whole recipe), null once it's been
 * deleted; [source] says where it's saved. [meal] filters to one meal; [loggedTo] is set once Log today has added it.
 */
data class FoodHistoryViewState(
    val name: String,
    val isRecipe: Boolean,
    val today: LocalDate = getCurrentDate(),
    val isLoaded: Boolean = false,
    val base: Food? = null,
    val source: FoodLogSource? = null,
    val entries: List<FoodHistoryEntry> = emptyList(),
    val period: FoodHistoryPeriod = FoodHistoryPeriod.DAYS_60,
    val meal: MealType? = null,
    val loggedTo: MealType? = null
) {
    val unit: String get() = if (isRecipe) "" else base?.unit.orEmpty()

    val inPeriod: List<FoodHistoryEntry> get() = entries.within(period = period, today = today)

    val shown: List<FoodHistoryEntry> get() = inPeriod.filter { meal == null || it.log.mealType == meal }

    val stats: FoodHistoryStats? get() = shown.stats()

    /** The meals it was eaten at in the period, with how many times, in the day's order. */
    val mealCounts: List<Pair<MealType, Int>>
        get() = inPeriod.groupBy { it.log.mealType }.map { it.key to it.value.size }.sortedBy { it.first.ordinal }

    /** Where Log today adds it: the meal picked, or where it's mostly eaten, or the next meal of the day. */
    val logMeal: MealType get() = meal ?: inPeriod.mostlyAt() ?: inPeriod.firstOrNull()?.log?.mealType ?: defaultFitMeal(loggedMeals = emptySet())

    /** Its own edit screen; null for a library food, which can't be changed, or one that's been deleted. */
    val editRoute: String?
        get() = when {
            base == null -> null
            source == FoodLogSource.RECIPES -> AppDestinations.Root.CreateRecipe.createRoute(recipeName = name)
            source == FoodLogSource.MY_FOODS -> AppDestinations.Root.CreateFood.createRoute(foodName = name)
            else -> null
        }
}

interface FoodHistoryCallbacks {
    fun onPeriodSelected(period: FoodHistoryPeriod)
    fun onMealSelected(meal: MealType?)

    /** Logs the usual amount to [FoodHistoryViewState.logMeal] today. */
    fun onLogUsual()

    /** Opens [entry] in its day's log. */
    fun onOpenEntry(entry: FoodHistoryEntry)

    companion object {
        fun default() = object : FoodHistoryCallbacks {
            override fun onPeriodSelected(period: FoodHistoryPeriod) = Unit
            override fun onMealSelected(meal: MealType?) = Unit
            override fun onLogUsual() = Unit
            override fun onOpenEntry(entry: FoodHistoryEntry) = Unit
        }
    }
}

class FoodHistoryViewModel(
    getAllFoodLogUseCase: GetAllFoodLogUseCase,
    getAllFoodUseCase: GetAllFoodUseCase,
    getAllRecipeUseCase: GetAllRecipeUseCase,
    getFoodLibraryUseCase: GetFoodLibraryUseCase,
    private val calculateAdjustMacrosUseCase: CalculateAdjustMacrosUseCase,
    private val insertFoodLogUseCase: InsertFoodLogUseCase,
    private val logDateRequest: LogDateRequest,
    savedStateHandle: SavedStateHandle
) : ViewModel(), FoodHistoryCallbacks {

    private val _state = MutableStateFlow(
        value = FoodHistoryViewState(
            name = savedStateHandle.get<String>(key = FOOD_NAME).orEmpty(),
            isRecipe = savedStateHandle.get<String>(key = IS_RECIPE) == "true"
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getAllFoodLogUseCase(),
                flow2 = flow { emitAll(getAllFoodUseCase()) },
                flow3 = flow { emitAll(getAllRecipeUseCase()) },
                flow4 = getFoodLibraryUseCase()
            ) { logs, foods, recipes, libraryFoods ->
                val name = _state.value.name
                val isRecipe = _state.value.isRecipe
                // Logs only keep a name, so a saved food takes precedence over a library food with the same name.
                val (base, source) = if (isRecipe) {
                    recipes.filter { it.name == name }
                        .toRecipeMeals(foods = foods, calculateAdjustMacros = calculateAdjustMacrosUseCase)
                        .firstOrNull()?.food to FoodLogSource.RECIPES
                } else {
                    foods.find { it.name == name }?.let { it to FoodLogSource.MY_FOODS }
                        ?: (libraryFoods.find { it.food.name == name }?.food to FoodLogSource.LIBRARY)
                }
                _state.update { currentState ->
                    currentState.copy(
                        isLoaded = true,
                        base = base,
                        source = source,
                        entries = foodHistoryEntries(
                            logs = logs,
                            name = name,
                            isRecipe = isRecipe,
                            fullRecipeCalories = base?.macros?.calories?.takeIf { isRecipe }
                        )
                    )
                }
            }.collect {}
        }
    }

    override fun onPeriodSelected(period: FoodHistoryPeriod) {
        _state.update { it.copy(period = period) }
    }

    override fun onMealSelected(meal: MealType?) {
        _state.update { it.copy(meal = meal) }
    }

    override fun onLogUsual() {
        val currentState = _state.value
        val base = currentState.base ?: return
        val amount = currentState.stats?.usual ?: return
        if (currentState.loggedTo != null) return
        val mealType = currentState.logMeal
        _state.update { it.copy(loggedTo = mealType) }
        val macros = if (currentState.isRecipe) {
            val fraction = amount / FULL_RECIPE_PERCENT
            FoodMacros(
                protein = base.macros.protein * fraction,
                carbs = base.macros.carbs * fraction,
                fat = base.macros.fat * fraction,
                calories = (base.macros.calories * fraction).toInt()
            )
        } else {
            calculateAdjustMacrosUseCase(portionSize = amount, originalFood = base)
        }
        viewModelScope.launch {
            insertFoodLogUseCase(
                foodName = base.name,
                mealType = mealType,
                calories = macros.calories,
                fat = macros.fat,
                carbs = macros.carbs,
                protein = macros.protein,
                weight = if (currentState.isRecipe) 0.0 else amount,
                dominantMacro = base.dominantMacro,
                date = getCurrentDate(),
                source = currentState.source
            )
        }
    }

    override fun onOpenEntry(entry: FoodHistoryEntry) {
        logDateRequest.request(date = entry.log.date, entryId = entry.log.id)
    }
}
