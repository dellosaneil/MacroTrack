package org.thelazybattley.macrotrack.features.addmeal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.features.addmeal.ui.FoodFilter

class AddMealViewModel(private val getAllFoodUseCase: GetAllFoodUseCase) : ViewModel(),
    AddMealCallbacks {

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

}
