package org.thelazybattley.macrotrack.features.addmeal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.DeleteFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.InsertFoodLogUseCase
import org.thelazybattley.macrotrack.features.addmeal.ui.FoodFilter
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

class AddMealViewModel(
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val insertFoodLogUseCase: InsertFoodLogUseCase,
    private val deleteFoodLogUseCase: DeleteFoodLogUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), AddMealCallbacks {

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
            getAllFoodUseCase().collect { foodList ->
                _state.update { currentState ->
                    currentState.copy(
                        completeFoodList = foodList,
                        filteredFoodList = foodList
                    )
                }
            }
        }
    }

    override fun onMealTypeSelected(mealType: MealType) {
        _state.update { currentState ->
            currentState.copy(
                selectedMealType = mealType
            )
        }
    }

    override fun onFoodFilterSelected(foodFilter: FoodFilter) {
        _state.update { currentState ->
            currentState.copy(
                selectedFoodFilter = foodFilter
            )
        }
    }

    override fun onInsertFoodLog(food: Food) {
        viewModelScope.launch {
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
    }

    override fun onNavigateScreen(destination: AppDestinations.Root) {
        _state.update { currentState ->
            currentState.copy(
                navigateDestination = destination
            )
        }
    }

    override fun resetNavigateScreen() {
        _state.update { currentState ->
            currentState.copy(
                navigateDestination = null
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

    override fun highlightedFood(food: Food) {
        _state.update { currentState ->
            currentState.copy(
                highlightedFood = food
            )
        }
    }

    override fun closeHighlightedFood() {
        _state.update { currentState ->
            currentState.copy(
                highlightedFood = null
            )
        }
    }
}
