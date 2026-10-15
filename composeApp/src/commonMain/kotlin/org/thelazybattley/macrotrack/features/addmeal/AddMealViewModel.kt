package org.thelazybattley.macrotrack.features.addmeal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.favorite.GetFavoriteKeysUseCase
import org.thelazybattley.macrotrack.domain.usecase.favorite.SetFavoriteUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.DeleteFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetRemainingCaloriesTodayUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.InsertFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.DeleteRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter
import org.thelazybattley.macrotrack.features.scanbarcode.ScannedFoodRequest
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

class AddMealViewModel(
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val insertFoodLogUseCase: InsertFoodLogUseCase,
    private val calculateAdjustMacrosUseCase: CalculateAdjustMacrosUseCase,
    private val getAllRecipeUseCase: GetAllRecipeUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val deleteFoodUseCase: DeleteFoodUseCase,
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getRemainingCaloriesTodayUseCase: GetRemainingCaloriesTodayUseCase,
    private val getFavoriteKeysUseCase: GetFavoriteKeysUseCase,
    private val setFavoriteUseCase: SetFavoriteUseCase,
    private val scannedFoodRequest: ScannedFoodRequest,
    savedStateHandle: SavedStateHandle
) : ViewModel(), AddMealCallbacks {

    private val _state = MutableStateFlow(value = AddMealViewState())
    val state = _state.asStateFlow()
    private val mealType: String? = savedStateHandle["mealType"]

    init {
        _state.update { currentState ->
            currentState.copy(
                selectedMealType = MealType.entries.find { it.name == mealType } ?: MealType.BREAKFAST
            )
        }

        viewModelScope.launch {
            combine(
                flow = getAllFoodUseCase(),
                flow2 = getAllRecipeUseCase(),
                flow3 = getAllFoodLogUseCase(),
                flow4 = getFavoriteKeysUseCase()
            ) { foodList, recipeList, foodLogs, favoriteKeys ->
                val recipeMeals = recipeList.toRecipeMeals(foods = foodList, calculateAdjustMacros = calculateAdjustMacrosUseCase)
                _state.update { currentState ->
                    currentState.copy(
                        completeFoodList = foodList,
                        recipeList = recipeMeals,
                        lastLogged = lastLogged(foodLogs = foodLogs, foods = foodList, recipes = recipeMeals),
                        favoriteKeys = favoriteKeys
                    )
                }
            }.collect()
        }

        viewModelScope.launch {
            scannedFoodRequest.foodName.collect { foodName ->
                if (foodName == null) return@collect
                _state.update { currentState -> currentState.copy(justScannedName = foodName, selectedMealFilter = null, searchQuery = "") }
                scannedFoodRequest.clear()
            }
        }

        viewModelScope.launch {
            getRemainingCaloriesTodayUseCase().collect { remainingCalories ->
                _state.update { currentState -> currentState.copy(remainingCalories = remainingCalories) }
            }
        }
    }

    override fun onMealFilterSelected(mealFilter: MealFilter) {
        _state.update { currentState -> currentState.copy(selectedMealFilter = mealFilter) }
    }

    override fun onSearchQuery(query: String) {
        _state.update { currentState -> currentState.copy(searchQuery = query) }
    }

    override fun onToggleItem(item: MealListItem) {
        _state.update { currentState ->
            if (currentState.selection.any { it.key == item.key }) {
                val remaining = currentState.selection.filterNot { it.key == item.key }
                currentState.copy(
                    selection = remaining,
                    focusedKey = if (currentState.focusedKey == item.key) remaining.lastOrNull()?.key else currentState.focusedKey
                )
            } else {
                // Start from the portion logged last time, if there is one.
                val amount = item.lastLogged?.amount ?: if (item.isRecipe) FULL_RECIPE_PERCENT else item.food.weight
                val selected = SelectedMealItem(
                    key = item.key,
                    isRecipe = item.isRecipe,
                    baseFood = item.food,
                    amount = amount,
                    food = adjustedFood(baseFood = item.food, isRecipe = item.isRecipe, amount = amount),
                    lastAmount = item.lastLogged?.amount
                )
                currentState.copy(selection = currentState.selection + selected, focusedKey = item.key)
            }
        }
    }

    override fun onToggleFavorite(item: MealListItem) {
        viewModelScope.launch {
            setFavoriteUseCase(key = item.key, isFavorite = !item.isFavorite)
        }
    }

    override fun onFocusItem(key: String) {
        _state.update { currentState ->
            if (currentState.selection.none { it.key == key }) return@update currentState
            // Tapping the expanded item collapses it; it stays selected.
            currentState.copy(focusedKey = if (currentState.focusedKey == key) null else key)
        }
    }

    override fun onAmountChanged(key: String, amount: Double) {
        if (amount <= 0) return
        _state.update { currentState ->
            currentState.copy(
                selection = currentState.selection.map { item ->
                    if (item.key != key) return@map item
                    item.copy(
                        amount = amount,
                        food = adjustedFood(baseFood = item.baseFood, isRecipe = item.isRecipe, amount = amount)
                    )
                }
            )
        }
    }

    override fun onAddSelection() {
        val currentState = _state.value
        if (currentState.selection.isEmpty() || currentState.isSaved) return
        viewModelScope.launch {
            currentState.selection.forEach { item ->
                insertFoodLogUseCase(
                    foodName = item.food.name,
                    carbs = item.food.macros.carbs,
                    fat = item.food.macros.fat,
                    calories = item.food.macros.calories,
                    protein = item.food.macros.protein,
                    mealType = currentState.selectedMealType,
                    weight = item.food.weight,
                    dominantMacro = item.food.dominantMacro
                )
            }
            _state.update { it.copy(isSaved = true) }
        }
    }

    override fun onEditItem(item: MealListItem) {
        val route = if (item.isRecipe) {
            AppDestinations.Root.CreateRecipe.createRoute(recipeName = item.food.name)
        } else {
            AppDestinations.Root.CreateFood.createRoute(foodName = item.food.name)
        }
        onNavigateScreen(route = route)
    }

    override fun onDeleteItem(item: MealListItem) {
        _state.update { currentState ->
            currentState.copy(selection = currentState.selection.filterNot { it.key == item.key })
        }
        viewModelScope.launch {
            if (item.isRecipe) {
                deleteRecipeUseCase(name = item.food.name)
            } else {
                deleteFoodUseCase(name = item.food.name)
            }
            setFavoriteUseCase(key = item.key, isFavorite = false)
        }
    }

    override fun onNavigateScreen(route: String) {
        _state.update { currentState -> currentState.copy(destinationRoute = route) }
    }

    override fun resetNavigateScreen() {
        _state.update { currentState -> currentState.copy(destinationRoute = null) }
    }

    /** Recipes are logged with weight 0.0, which the food log uses to tag them as recipes. */
    private fun adjustedFood(baseFood: Food, isRecipe: Boolean, amount: Double): Food = if (isRecipe) {
        val fraction = amount / FULL_RECIPE_PERCENT
        baseFood.copy(
            macros = FoodMacros(
                protein = baseFood.macros.protein * fraction,
                carbs = baseFood.macros.carbs * fraction,
                fat = baseFood.macros.fat * fraction,
                calories = (baseFood.macros.calories * fraction).toInt()
            ),
            weight = 0.0
        )
    } else {
        baseFood.copy(
            macros = calculateAdjustMacrosUseCase(portionSize = amount, originalFood = baseFood),
            weight = amount
        )
    }

    /**
     * The newest log of each saved food and recipe, with its portion. Recipes are logged with
     * weight 0.0, so their portion is worked out from the calories eaten.
     */
    private fun lastLogged(
        foodLogs: List<FoodLog>,
        foods: List<Food>,
        recipes: List<RecipeMeal>
    ): Map<String, LastLogged> {
        val foodsByName = foods.associateBy { it.name }
        val recipesByName = recipes.associateBy { it.name }
        return foodLogs
            .groupBy { mealItemKey(name = it.foodName, isRecipe = it.weight == 0.0) }
            .mapNotNull { (key, logs) ->
                val log = logs.maxBy { it.id }
                val isRecipe = log.weight == 0.0
                val baseFood = if (isRecipe) recipesByName[log.foodName]?.food else foodsByName[log.foodName]
                if (baseFood == null) return@mapNotNull null
                val amount = if (isRecipe) {
                    recipePercentEaten(loggedCalories = log.calories, fullCalories = baseFood.macros.calories)
                } else {
                    log.weight
                }
                if (amount <= 0) return@mapNotNull null
                key to LastLogged(
                    order = log.id,
                    date = log.date,
                    amount = amount,
                    food = adjustedFood(baseFood = baseFood, isRecipe = isRecipe, amount = amount)
                )
            }
            .toMap()
    }
}
