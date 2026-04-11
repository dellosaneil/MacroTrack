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
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.Recipe
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.DeleteFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.InsertFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.DeleteRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.features.addmeal.ui.MealFilter
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

class AddMealViewModel(
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val insertFoodLogUseCase: InsertFoodLogUseCase,
    private val deleteFoodLogUseCase: DeleteFoodLogUseCase,
    private val calculateAdjustMacrosUseCase: CalculateAdjustMacrosUseCase,
    private val getAllRecipeUseCase: GetAllRecipeUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), AddMealCallbacks.MainScreenCallbacks, AddMealCallbacks.FoodCallbacks,
    AddMealCallbacks.RecipeCallbacks {

    private val _state = MutableStateFlow(value = AddMealViewState())
    val state = _state.asStateFlow()
    val mealType: String? = savedStateHandle["mealType"]

    init {
        _state.update { currentState ->
            currentState.copy(
                selectedMealType = MealType.valueOf(mealType ?: "")
            )
        }

        viewModelScope.launch {
            combine(
                flow = getAllFoodUseCase(),
                flow2 = getAllRecipeUseCase()
            ) { foodList, recipeList ->
                _state.update { currentState ->
                    val recipeMealList =
                        recipeToRecipeMeal(recipes = recipeList, completeFoodList = foodList)
                    currentState.copy(
                        completeFoodList = foodList,
                        filteredFoodList = foodList,
                        filteredRecipeList = recipeMealList,
                        recipeList = recipeMealList
                    )
                }
            }.collect()
        }
    }

    override fun onMealFilterSelected(mealFilter: MealFilter) {
        _state.update { currentState ->
            currentState.copy(
                selectedMealFilter = mealFilter
            )
        }
    }

    override fun onNavigateScreen(route: String) {
        _state.update { currentState ->
            currentState.copy(
                destinationRoute = route
            )
        }
    }

    override fun resetNavigateScreen() {
        _state.update { currentState ->
            currentState.copy(
                destinationRoute = null
            )
        }
    }

    override fun onRevertLog() {
        if (state.value.loggedMeals.id == 0L || state.value.loggedMeals.name.isEmpty()) return
        viewModelScope.launch {
            deleteFoodLogUseCase(id = state.value.loggedMeals.id)
            _state.update { currentState ->
                val updatedList =
                    currentState.loggedMeals.loggedMeals.toMutableList()
                updatedList.remove(element = currentState.loggedMeals.loggedMeals.last())
                currentState.copy(
                    loggedMeals = currentState.loggedMeals.copy(
                        name = "",
                        id = 0L,
                        loggedMeals = updatedList,
                        totalCalories = updatedList.sumOf { it.macros.calories },
                        totalProtein = updatedList.sumOf { it.macros.protein }.toInt(),
                        totalFat = updatedList.sumOf { it.macros.fat }.toInt(),
                        totalCarbs = updatedList.sumOf { it.macros.carbs }.toInt()
                    )
                )
            }
        }
    }

    private suspend fun insertFoodLog(food: Food) {
        val id = insertFoodLogUseCase(
            foodName = food.name,
            carbs = food.macros.carbs,
            fat = food.macros.fat,
            calories = food.macros.calories,
            protein = food.macros.protein,
            mealType = state.value.selectedMealType,
            weight = food.weight,
            dominantMacro = food.dominantMacro
        )
        _state.update { currentState ->
            val updatedList =
                currentState.loggedMeals.loggedMeals.toMutableList()
            updatedList.add(element = food)
            currentState.copy(
                loggedMeals = currentState.loggedMeals.copy(
                    name = food.name,
                    id = id,
                    loggedMeals = updatedList,
                    totalCalories = updatedList.sumOf { it.macros.calories },
                    totalProtein = updatedList.sumOf { it.macros.protein }.toInt(),
                    totalFat = updatedList.sumOf { it.macros.fat }.toInt(),
                    totalCarbs = updatedList.sumOf { it.macros.carbs }.toInt()
                ),
            )
        }
    }

    override fun insertFood(food: Food) {
        viewModelScope.launch {
            insertFoodLog(food = food)
        }
    }

    override fun customizeFoodWeight(name: String) {
        _state.update { currentState ->
            currentState.copy(
                highlightedFood = currentState.completeFoodList.find { it.name == name }
            )
        }
    }

    override fun closeCustomizeFoodWeight() {
        _state.update { currentState ->
            currentState.copy(
                highlightedFood = null
            )
        }
    }

    override fun updateWeight(portionSize: Double) {
        _state.update { currentState ->
            val originalFood =
                currentState.completeFoodList
                    .find { it.name == currentState.highlightedFood?.name }
                    ?: return@update currentState
            currentState.copy(
                highlightedFood = currentState.highlightedFood?.copy(
                    macros = calculateAdjustMacrosUseCase(
                        portionSize = portionSize,
                        originalFood = originalFood
                    ),
                    weight = portionSize
                )
            )
        }
    }

    override fun addCustomizedFood() {
        if (_state.value.highlightedFood == null) {
            return
        }
        viewModelScope.launch {
            insertFoodLog(food = _state.value.highlightedFood!!)
        }
        _state.update { currentState ->
            currentState.copy(
                highlightedFood = null
            )
        }
    }

    override fun onPercentageEatenValue(value: Double) {
        _state.update { currentState ->
            if (currentState.highlightedRecipe == null) return@update currentState
            val recipeMeal =
                currentState.recipeList.find { it.name == currentState.highlightedRecipe.name }
                    ?: return@update currentState
            currentState.copy(
                highlightedRecipe = recipeMeal.copy(
                    food = recipeMeal.food.copy(
                        macros = recipeMeal.food.macros.copy(
                            protein = recipeMeal.food.macros.protein * value / 100,
                            carbs = recipeMeal.food.macros.carbs * value / 100,
                            fat = recipeMeal.food.macros.fat * value / 100,
                            calories = (recipeMeal.food.macros.calories * value / 100).toInt(),
                        )
                    ),
                    percentage = value
                )
            )
        }
    }

    override fun insertSelectedRecipe() {
        viewModelScope.launch {
            if (state.value.highlightedRecipe == null) return@launch
            insertFoodLog(food = state.value.highlightedRecipe!!.food)
        }
        _state.update { currentState ->
            currentState.copy(
                highlightedRecipe = null,
            )
        }
    }

    override fun insertRecipe(name: String, percentage: Double) {
        val recipe = _state.value.recipeList.find { it.name == name } ?: return
        viewModelScope.launch {
            val food = Food(
                macros = FoodMacros(
                    protein = recipe.food.macros.protein * percentage,
                    carbs = recipe.food.macros.carbs * percentage,
                    fat = recipe.food.macros.fat * percentage,
                    calories = (recipe.food.macros.calories * percentage).toInt()
                ),
                name = recipe.food.name,
                weight = 0.0,
                dominantMacro = recipe.food.dominantMacro
            )
            insertFoodLog(food = food)
        }
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(
                    highlightedRecipe = null
                )
            }
        }
    }

    override fun onRecipeSelected(name: String) {
        _state.update { currentState ->
            currentState.copy(
                highlightedRecipe = currentState.recipeList.find { it.name == name }
            )
        }
    }

    override fun closeSelectedRecipe() {
        _state.update { currentState ->
            currentState.copy(
                highlightedRecipe = null
            )
        }
    }

    private fun recipeToRecipeMeal(
        recipes: List<Recipe>,
        completeFoodList: List<Food>
    ): List<RecipeMeal> {
        val recipeMealList = recipes.map { recipe ->
            val foodList = mutableListOf<Food>()
            recipe.ingredients.map { ingredient ->
                val ingredientAsFood =
                    completeFoodList.find { it.name == ingredient.name }
                        ?: return@map null
                val foodMacros = calculateAdjustMacrosUseCase(
                    portionSize = ingredient.weight,
                    originalFood = ingredientAsFood
                )
                foodList.add(
                    element = Food(
                        macros = foodMacros,
                        name = ingredient.name,
                        weight = ingredient.weight,
                        dominantMacro = ingredientAsFood.dominantMacro
                    )
                )
            }
            val food = Food(
                macros = FoodMacros(
                    protein = foodList.sumOf { it.macros.protein },
                    carbs = foodList.sumOf { it.macros.carbs },
                    fat = foodList.sumOf { it.macros.fat },
                    calories = foodList.sumOf { it.macros.calories }
                ),
                name = recipe.name,
                weight = 0.0,
                dominantMacro = recipe.dominantMacro
            )
            RecipeMeal(
                food = food,
                ingredients = recipe.ingredients,
                percentage = 1.0,
                name = recipe.name
            )
        }
        return recipeMealList
    }

    override fun onSearchQuery(query: String) {
        _state.update { currentState ->
            currentState.copy(
                filteredFoodList = currentState.completeFoodList.filter { food ->
                    food.name.contains(
                        other = query,
                        ignoreCase = true
                    )
                },
                filteredRecipeList = currentState.recipeList.filter { recipe ->
                    recipe.name.contains(
                        other = query,
                        ignoreCase = true
                    )
                },
                searchQuery = query
            )
        }
    }

    override fun updateRecipe(name: String) {
        _state.update { currentState ->
            currentState.copy(
                destinationRoute = AppDestinations.Root.CreateRecipe.createRoute(recipeName = name)
            )
        }
    }

    override fun deleteRecipe(name: String) {
        viewModelScope.launch {
            deleteRecipeUseCase(name = name)
        }
    }
}
