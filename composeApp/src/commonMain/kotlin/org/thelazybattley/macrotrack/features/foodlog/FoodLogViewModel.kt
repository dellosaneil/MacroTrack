package org.thelazybattley.macrotrack.features.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase

class FoodLogViewModel(
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
) : ViewModel(), FoodLogCallbacks {

    private val _state = MutableStateFlow(value = FoodLogViewState())

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAllFoodLogUseCase().collect { logs ->
                _state.update { currentState ->
                    val breakfastLogs = logs.filter { it.mealType == MealType.BREAKFAST }
                    val lunchLogs = logs.filter { it.mealType == MealType.LUNCH }
                    val dinnerLogs = logs.filter { it.mealType == MealType.DINNER }
                    val snackLogs = logs.filter { it.mealType == MealType.SNACK }
                    currentState.copy(
                        breakfastFood = breakfastLogs,
                        lunchFood = lunchLogs,
                        dinnerFood = dinnerLogs,
                        snackFood = snackLogs
                    )
                }
            }
        }
    }
}
