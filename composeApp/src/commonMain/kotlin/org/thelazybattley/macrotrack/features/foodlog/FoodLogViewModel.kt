package org.thelazybattley.macrotrack.features.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase

class FoodLogViewModel(
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase
) : ViewModel(), FoodLogCallbacks {

    private val _state = MutableStateFlow(value = FoodLogViewState())

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAllFoodLogUseCase().collect { logs ->
                val breakfastLogs = logs.filter { it.mealType == MealType.BREAKFAST }
                val lunchLogs = logs.filter { it.mealType == MealType.LUNCH }
                val dinnerLogs = logs.filter { it.mealType == MealType.DINNER }
                val snackLogs = logs.filter { it.mealType == MealType.SNACK }
                val breakfast = FoodLogFoodListByMealType(
                    foodList = breakfastLogs,
                    mealType = MealType.BREAKFAST,
                    calories = breakfastLogs.sumOf { it.calories },
                    protein = breakfastLogs.sumOf { it.protein },
                    fat = breakfastLogs.sumOf { it.fat },
                    carbs = breakfastLogs.sumOf { it.carbs }
                )
                val lunch = FoodLogFoodListByMealType(
                    foodList = lunchLogs,
                    mealType = MealType.LUNCH,
                    calories = lunchLogs.sumOf { it.calories },
                    protein = lunchLogs.sumOf { it.protein },
                    fat = lunchLogs.sumOf { it.fat },
                    carbs = lunchLogs.sumOf { it.carbs }
                )
                val dinner = FoodLogFoodListByMealType(
                    foodList = dinnerLogs,
                    mealType = MealType.DINNER,
                    calories = dinnerLogs.sumOf { it.calories },
                    protein = dinnerLogs.sumOf { it.protein },
                    fat = dinnerLogs.sumOf { it.fat },
                    carbs = dinnerLogs.sumOf { it.carbs }
                )
                val snack = FoodLogFoodListByMealType(
                    foodList = snackLogs,
                    mealType = MealType.SNACK,
                    calories = snackLogs.sumOf { it.calories },
                    protein = snackLogs.sumOf { it.protein },
                    fat = snackLogs.sumOf { it.fat },
                    carbs = snackLogs.sumOf { it.carbs }
                )
                _state.update { currentState ->
                    currentState.copy(
                        breakfast = breakfast,
                        lunch = lunch,
                        dinner = dinner,
                        snack = snack,
                        totalFoodMacros = FoodMacros(
                            calories = breakfast.calories + lunch.calories + dinner.calories + snack.calories,
                            protein = breakfast.protein + lunch.protein + dinner.protein + snack.protein,
                            fat = breakfast.fat + lunch.fat + dinner.fat + snack.fat,
                            carbs = breakfast.carbs + lunch.carbs + dinner.carbs + snack.carbs,
                        ),
                    )
                }
            }
        }
        updateMacroGoals()
    }

    private fun updateMacroGoals() {
        viewModelScope.launch {
            getUserDetailsUseCase()?.let { user ->
                val goalMacros = calculateMacrosGoalUseCase(
                    height = user.height,
                    weight = user.weight,
                    age = user.age,
                    gender = user.gender,
                    activityLevel = user.activityLevel,
                    goal = user.goal
                )
                _state.update { currentState ->
                    currentState.copy(
                        macroGoals = goalMacros
                    )
                }
            }
        }
    }

}
