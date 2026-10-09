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
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.Recipe
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.DeleteFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.InsertFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.DeleteRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

class AddMealViewModel(
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val insertFoodLogUseCase: InsertFoodLogUseCase,
    private val calculateAdjustMacrosUseCase: CalculateAdjustMacrosUseCase,
    private val getAllRecipeUseCase: GetAllRecipeUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val deleteFoodUseCase: DeleteFoodUseCase,
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
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
                flow2 = getAllRecipeUseCase()
            ) { foodList, recipeList ->
                _state.update { currentState ->
                    currentState.copy(
                        completeFoodList = foodList,
                        recipeList = recipeToRecipeMeal(recipes = recipeList, completeFoodList = foodList)
                    )
                }
            }.collect()
        }

        viewModelScope.launch {
            combine(
                flow = getAllFoodLogUseCase(),
                flow2 = getUserDetailsUseCase()
            ) { foodLogs, userDetails ->
                val today = getCurrentDate()
                val eatenToday = foodLogs.filter { it.date == today }.sumOf { it.calories }
                val goal = userDetails?.let {
                    calculateMacrosGoalUseCase(
                        height = it.height,
                        weight = it.weight,
                        age = it.age,
                        gender = it.sex,
                        activityLevel = it.activityLevel,
                        goal = it.goal
                    ).calories
                }
                _state.update { currentState ->
                    currentState.copy(
                        remainingCalories = goal?.let { it - eatenToday },
                        lastLoggedOrder = foodLogs
                            .groupBy { it.foodName }
                            .mapValues { (_, logs) -> logs.maxOf { it.id } }
                    )
                }
            }.collect()
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
                val amount = if (item.isRecipe) FULL_RECIPE_PERCENT else item.food.weight
                val selected = SelectedMealItem(
                    key = item.key,
                    isRecipe = item.isRecipe,
                    baseFood = item.food,
                    amount = amount,
                    food = adjustedFood(baseFood = item.food, isRecipe = item.isRecipe, amount = amount)
                )
                currentState.copy(selection = currentState.selection + selected, focusedKey = item.key)
            }
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

    private fun recipeToRecipeMeal(
        recipes: List<Recipe>,
        completeFoodList: List<Food>
    ): List<RecipeMeal> = recipes.map { recipe ->
        val ingredients = recipe.ingredients.mapNotNull { ingredient ->
            val ingredientAsFood = completeFoodList.find { it.name == ingredient.name } ?: return@mapNotNull null
            calculateAdjustMacrosUseCase(portionSize = ingredient.weight, originalFood = ingredientAsFood)
        }
        RecipeMeal(
            food = Food(
                macros = FoodMacros(
                    protein = ingredients.sumOf { it.protein },
                    carbs = ingredients.sumOf { it.carbs },
                    fat = ingredients.sumOf { it.fat },
                    calories = ingredients.sumOf { it.calories }
                ),
                name = recipe.name,
                weight = 0.0,
                dominantMacro = recipe.dominantMacro,
                unit = "serving"
            ),
            ingredients = recipe.ingredients,
            percentage = 1.0,
            name = recipe.name
        )
    }

    companion object {
        const val FULL_RECIPE_PERCENT = 100.0
    }
}
