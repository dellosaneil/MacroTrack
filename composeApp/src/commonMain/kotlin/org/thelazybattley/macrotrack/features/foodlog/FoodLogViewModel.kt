package org.thelazybattley.macrotrack.features.foodlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.SaveDailyGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.DeleteFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateAdjustMacrosUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.UpdateFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.recipe.GetAllRecipeUseCase
import org.thelazybattley.macrotrack.features.addmeal.FULL_RECIPE_PERCENT
import org.thelazybattley.macrotrack.features.addmeal.recipePercentEaten
import org.thelazybattley.macrotrack.features.addmeal.toRecipeMeals
import org.thelazybattley.macrotrack.features.calendar.LogDateRequest
import org.thelazybattley.macrotrack.features.calendar.currentStreak

class FoodLogViewModel(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val deleteFoodLogUseCase: DeleteFoodLogUseCase,
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getAllFoodUseCase: GetAllFoodUseCase,
    private val getAllDailyGoalsUseCase: GetAllDailyGoalsUseCase,
    private val saveDailyGoalUseCase: SaveDailyGoalUseCase,
    private val logDateRequest: LogDateRequest,
    private val getAllRecipeUseCase: GetAllRecipeUseCase,
    private val calculateAdjustMacrosUseCase: CalculateAdjustMacrosUseCase,
    private val updateFoodLogUseCase: UpdateFoodLogUseCase,
) : ViewModel(), FoodLogCallbacks {

    private val _state = MutableStateFlow(value = FoodLogViewState())

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getAllFoodLogUseCase(),
                flow2 = getAllFoodUseCase(),
                flow3 = getAllRecipeUseCase()
            ) { logs, food, recipes ->
                val dates = logs.map { it.date }.toMutableSet()
                dates.add(element = getCurrentDate())
                _state.update { currentState ->
                    currentState.copy(
                        allFoodLog = logs,
                        availableDates = dates.toList(),
                        currentStreak = currentStreak(loggedDates = logs.map { it.date }.toSet(), today = getCurrentDate()),
                        foodUnit = food.associate {
                            it.name to it.unit
                        },
                        recipeCalories = recipes
                            .toRecipeMeals(foods = food, calculateAdjustMacros = calculateAdjustMacrosUseCase)
                            .associate { it.name to it.food.macros.calories }
                    )
                }.also {
                    val selectedDate = _state.value.selectedDate
                    filterFoodLogsByDate(foodLog = logs.filter { it.date == selectedDate })
                }
            }.collect()
        }
        updateMacroGoals()
    }

    private fun filterFoodLogsByDate(foodLog: List<FoodLog>) {
        val breakfastLogs = foodLog.filter { it.mealType == MealType.BREAKFAST }
        val lunchLogs = foodLog.filter { it.mealType == MealType.LUNCH }
        val dinnerLogs = foodLog.filter { it.mealType == MealType.DINNER }
        val snackLogs = foodLog.filter { it.mealType == MealType.SNACK }
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

    private fun updateMacroGoals() {
        viewModelScope.launch {
            combine(
                flow = flow { emitAll(getUserDetailsUseCase()) },
                flow2 = getAllDailyGoalsUseCase()
            ) { user, dailyGoals ->
                val today = getCurrentDate()
                val currentMacroGoals = user?.let {
                    calculateMacrosGoalUseCase(
                        height = it.height,
                        weight = it.weight,
                        age = it.age,
                        gender = it.sex,
                        activityLevel = it.activityLevel,
                        goal = it.goal
                    )
                }
                if (user != null && currentMacroGoals != null) {
                    // Record today's goal so past days keep the goal they were tracked with.
                    val todayGoal = DailyGoal(date = today, goal = user.goal, macroGoals = currentMacroGoals)
                    if (dailyGoals.find { it.date == today } != todayGoal) {
                        saveDailyGoalUseCase(dailyGoal = todayGoal)
                    }
                }
                _state.update { currentState ->
                    currentState.copy(
                        dailyGoals = dailyGoals,
                        currentGoal = user?.goal,
                        currentMacroGoals = currentMacroGoals
                    ).withDayGoal()
                }
            }.collect()
        }
    }

    private fun FoodLogViewState.withDayGoal(): FoodLogViewState {
        val dayGoal = resolveDayGoal(
            date = selectedDate,
            today = getCurrentDate(),
            dailyGoals = dailyGoals,
            currentGoal = currentGoal,
            currentMacroGoals = currentMacroGoals
        )
        return copy(dayGoal = dayGoal, macroGoals = dayGoal?.macroGoals)
    }

    override fun onNavigate(mealType: MealType) {
        _state.update { currentState ->
            currentState.copy(
                navigateMealTypeParameter = mealType
            )
        }
    }

    override fun resetNavigateMealTypeParameter() {
        _state.update { currentState ->
            currentState.copy(
                navigateMealTypeParameter = null
            )
        }
    }

    override fun onDeleteFoodLog(id: Long) {
        viewModelScope.launch {
            deleteFoodLogUseCase(id = id)
        }
    }

    override fun onScreenShown() {
        onDateFilterSelected(date = logDateRequest.consume() ?: getCurrentDate())
    }

    override fun onOpenEntry(log: FoodLog) {
        _state.update { currentState ->
            val isRecipe = log.weight == 0.0
            val originalAmount = if (isRecipe) {
                currentState.recipeCalories[log.foodName]
                    ?.let { recipePercentEaten(loggedCalories = log.calories, fullCalories = it) }
                    ?: FULL_RECIPE_PERCENT
            } else {
                log.weight
            }
            currentState.copy(
                entryEdit = EntryEdit(
                    log = log,
                    originalAmount = originalAmount,
                    unit = if (isRecipe) "%" else currentState.foodUnit[log.foodName].orEmpty()
                )
            )
        }
    }

    override fun onEntryAmountChanged(amount: Double) {
        if (amount <= 0) return
        _state.update { it.copy(entryEdit = it.entryEdit?.copy(amount = amount)) }
    }

    override fun onEntryMealChanged(mealType: MealType) {
        _state.update { it.copy(entryEdit = it.entryEdit?.copy(mealType = mealType)) }
    }

    override fun onCloseEntry() {
        _state.update { it.copy(entryEdit = null) }
    }

    override fun onSaveEntry() {
        val edit = _state.value.entryEdit ?: return
        if (!edit.canSave) return
        _state.update { it.copy(entryEdit = null) }
        viewModelScope.launch {
            updateFoodLogUseCase(
                id = edit.log.id,
                mealType = edit.mealType,
                calories = edit.calories,
                protein = edit.protein,
                carbs = edit.carbs,
                fat = edit.fat,
                weight = edit.weight
            )
        }
    }

    override fun onDeleteEntry() {
        val edit = _state.value.entryEdit ?: return
        _state.update { it.copy(entryEdit = null) }
        onDeleteFoodLog(id = edit.log.id)
    }

    override fun onDateFilterSelected(date: LocalDate) {
        _state.update { currentState ->
            val filteredLogs = currentState.allFoodLog.filter { it.date == date }
            filterFoodLogsByDate(foodLog = filteredLogs)
            currentState.copy(
                selectedDate = date,
            ).withDayGoal()
        }
    }
}
