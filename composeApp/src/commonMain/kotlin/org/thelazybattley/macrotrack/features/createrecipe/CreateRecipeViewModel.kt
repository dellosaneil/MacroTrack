package org.thelazybattley.macrotrack.features.createrecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.Ingredient
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.Recipe
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacroPercentageUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.InsertRecipeUseCase

class CreateRecipeViewModel(
    private val getAllRecipeUseCase: GetAllRecipeUseCase,
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val calculateMacroPercentageUseCase: CalculateMacroPercentageUseCase,
    private val calculateAdjustMacrosUseCase: CalculateAdjustMacrosUseCase,
    private val insertRecipeUseCase: InsertRecipeUseCase
) : ViewModel(), CreateRecipeCallbacks {

    private val _state = MutableStateFlow(value = CreateRecipeViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAllRecipeUseCase().collect { recipes ->
                _state.update { currentState ->
                    currentState.copy(
                        savedRecipesName = recipes.map { it.name }
                    )
                }
            }
        }
        viewModelScope.launch {
            getAllFoodUseCase().collect { foods ->
                _state.update { currentState ->
                    currentState.copy(
                        ingredients = foods,
                        filteredIngredients = foods
                    )
                }
            }
        }
    }

    override fun onSearchKeyword(keyword: String) {
        _state.update { currentState ->
            val filteredIngredients = currentState.ingredients.filter { food ->
                food.name.contains(other = keyword, ignoreCase = true)
            }
            currentState.copy(
                filteredIngredients = filteredIngredients
            )
        }
    }

    override fun onAddIngredient(food: Food) {
        val updatedIngredients = _state.value.selectedIngredients.plus(element = food)
        updateIngredients(updatedIngredients = updatedIngredients)
    }

    override fun customizeIngredientWeight(name: String) {
        _state.update { currentState ->
            currentState.copy(
                highlightedIngredient = currentState.ingredients.firstOrNull { it.name == name }
            )
        }
    }

    override fun closeCustomWeight() {
        _state.update { currentState ->
            currentState.copy(
                highlightedIngredient = null
            )
        }
    }

    override fun addCustomizedIngredient() {
        val currentState = _state.value
        if (currentState.highlightedIngredient == null) return
        onAddIngredient(food = currentState.highlightedIngredient)
        _state.update { currentState ->
            currentState.copy(
                highlightedIngredient = null
            )
        }
    }

    override fun updateWeight(portionSize: Double) {
        _state.update { currentState ->
            val originalFood =
                currentState.ingredients
                    .find { it.name == currentState.highlightedIngredient?.name }
                    ?: return@update currentState
            currentState.copy(
                highlightedIngredient = currentState.highlightedIngredient?.copy(
                    macros = calculateAdjustMacrosUseCase(
                        portionSize = portionSize,
                        originalFood = originalFood
                    ),
                    weight = portionSize
                )
            )
        }
    }

    override fun onSaveRecipe() {
        val currentState = _state.value
        viewModelScope.launch {
            val dominantMacro = listOf(
                MacroType.PROTEIN to currentState.macros.proteinPercentage,
                MacroType.FAT to currentState.macros.fatPercentage,
                MacroType.CARBS to currentState.macros.carbsPercentage
            ).maxBy { it.second }.first
            val recipe = Recipe(
                name = currentState.recipeName,
                ingredients = currentState.selectedIngredients.map { food ->
                    Ingredient(name = food.name, weight = food.weight)
                },
                dominantMacro = dominantMacro
            )
            insertRecipeUseCase(
                recipe = recipe
            )
            _state.update { currentState ->
                currentState.copy(
                    recipeSaved = true
                )
            }
        }
    }

    override fun inputRecipeName(name: String) {
        _state.update { currentState ->
            val isRecipeNameTaken =
                currentState.savedRecipesName.plus(currentState.ingredients.map { it.name })
                    .any { it.equals(other = name, ignoreCase = true) }
            currentState.copy(
                recipeName = name,
                isRecipeNameTaken = isRecipeNameTaken,
                buttonEnabled = isButtonEnabled(
                    selectedIngredients = currentState.selectedIngredients,
                    recipeName = name,
                    isRecipeNameTaken = isRecipeNameTaken
                )
            )
        }
    }

    override fun removeIngredient(food: Food) {
        val updatedIngredients = _state.value.selectedIngredients.minus(element = food)
        updateIngredients(updatedIngredients = updatedIngredients)
    }

    private fun updateIngredients(updatedIngredients: List<Food>) {
        _state.update { currentState ->
            val totalProtein = updatedIngredients.sumOf { it.macros.protein }
            val totalFat = updatedIngredients.sumOf { it.macros.fat }
            val totalCarbs = updatedIngredients.sumOf { it.macros.carbs }
            val totalCalories = updatedIngredients.sumOf { it.macros.calories }
            currentState.copy(
                selectedIngredients = updatedIngredients,
                buttonEnabled = isButtonEnabled(
                    selectedIngredients = updatedIngredients,
                    recipeName = currentState.recipeName,
                    isRecipeNameTaken = currentState.isRecipeNameTaken
                ),
                macros = CreateRecipeMacros(
                    protein = totalProtein,
                    carbs = totalCarbs,
                    fat = totalFat,
                    calories = totalCalories,
                    proteinPercentage = calculateMacroPercentageUseCase(
                        totalCalories = totalCalories,
                        macroValue = totalProtein,
                        macroType = MacroType.PROTEIN
                    ),
                    fatPercentage = calculateMacroPercentageUseCase(
                        totalCalories = totalCalories,
                        macroValue = totalFat,
                        macroType = MacroType.FAT
                    ),
                    carbsPercentage = calculateMacroPercentageUseCase(
                        totalCalories = totalCalories,
                        macroValue = totalCarbs,
                        macroType = MacroType.CARBS
                    )
                )
            )
        }
    }

    private fun isButtonEnabled(
        selectedIngredients: List<Food>,
        recipeName: String,
        isRecipeNameTaken: Boolean
    ): Boolean {
        return selectedIngredients.isNotEmpty() && recipeName.isNotEmpty() && !isRecipeNameTaken
    }
}
