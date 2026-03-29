package org.thelazybattley.macrotrack.features.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.CalculateTDEEUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase

class FoodLogViewModel(
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateTDEEUseCase: CalculateTDEEUseCase
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
                    val breakfast = FoodLogFoodListByMealType(
                        foodList = breakfastLogs,
                        mealType = MealType.BREAKFAST,
                        calories = breakfastLogs.sumOf { it.calories }
                    )
                    val lunch = FoodLogFoodListByMealType(
                        foodList = lunchLogs,
                        mealType = MealType.LUNCH,
                        calories = lunchLogs.sumOf { it.calories }
                    )
                    val dinner = FoodLogFoodListByMealType(
                        foodList = dinnerLogs,
                        mealType = MealType.DINNER,
                        calories = dinnerLogs.sumOf { it.calories }
                    )
                    val snack = FoodLogFoodListByMealType(
                        foodList = snackLogs,
                        mealType = MealType.SNACK,
                        calories = snackLogs.sumOf { it.calories }
                    )

                    currentState.copy(
                        breakfast = breakfast,
                        lunch = lunch,
                        dinner = dinner,
                        snack = snack,
                        totalCalories = breakfast.calories + lunch.calories + dinner.calories + snack.calories
                    )
                }
            }
        }
        viewModelScope.launch {
            getUserDetailsUseCase()?.let { user ->
                println("Test: $user")
                val goalCalories = calculateTDEEUseCase(
                    height = user.height,
                    weight = user.weight,
                    age = user.age,
                    gender = user.gender,
                    activityLevel = user.activityLevel
                )
                _state.update { currentState ->
                    currentState.copy(
                        calorieGoal = goalCalories
                    )
                }
            }
        }
    }
}
