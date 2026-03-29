package org.thelazybattley.macrotrack.features.addmeal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.Food
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.InsertFoodLogUseCase
import org.thelazybattley.macrotrack.features.addmeal.ui.FoodFilter
import org.thelazybattley.macrotrack.ui.navigation.MacroTrackDestination

class AddMealViewModel(
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val insertFoodLogUseCase: InsertFoodLogUseCase
) : ViewModel(), AddMealCallbacks {

    private val _state = MutableStateFlow(value = AddMealViewState())
    val state = _state.asStateFlow()

    init {
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
            insertFoodLogUseCase(
                foodName = food.name,
                carbs = food.macros.carbs,
                fat = food.macros.fat,
                calories = food.macros.calories,
                protein = food.macros.protein,
                mealType = state.value.selectedMealType
            )
        }
    }

    override fun onNavigateScreen(destination: MacroTrackDestination) {
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
}
