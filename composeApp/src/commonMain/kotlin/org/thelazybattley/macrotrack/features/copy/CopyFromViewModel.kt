package org.thelazybattley.macrotrack.features.copy

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.CopyFoodLogsUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetRemainingCaloriesUseCase
import org.thelazybattley.macrotrack.domain.usecase.library.GetFoodLibraryUseCase
import org.thelazybattley.macrotrack.features.foodlog.FoodLogCopyEvents
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.LOG_DATE
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.MEAL_TYPE
import org.thelazybattley.macrotrack.ui.navigation.logDateOf

/** One meal on one earlier day that can be copied in. */
data class PastMeal(val date: LocalDate, val mealType: MealType, val logs: List<FoodLog>) {
    val calories: Int get() = logs.sumOf { it.calories }
}

/** Filling [mealType] on [targetDate] from a meal in the [DAYS_BACK] days before it, the same meal first. */
data class CopyFromViewState(
    val mealType: MealType,
    val targetDate: LocalDate,
    val allLogs: List<FoodLog> = emptyList(),
    val foodUnit: Map<String, String> = emptyMap(),
    val remainingCalories: Int? = null,
    val isAnyMeal: Boolean = false,
    val picked: PastMeal? = null,
    val isDone: Boolean = false
) {
    val pastMeals: List<PastMeal>
        get() = (1..DAYS_BACK).flatMap { back ->
            val date = targetDate.minus(period = DatePeriod(days = back))
            allLogs.filter { it.date == date }
                .groupBy { it.mealType }
                .map { (meal, logs) -> PastMeal(date = date, mealType = meal, logs = logs) }
                .filter { isAnyMeal || it.mealType == mealType }
                .sortedWith(compareBy({ it.mealType != mealType }, { it.mealType.ordinal }))
        }

    /** The picked meal while it's still shown, else the first one. */
    val selected: PastMeal?
        get() = pastMeals.let { meals -> meals.find { it.date == picked?.date && it.mealType == picked.mealType } ?: meals.firstOrNull() }

    val remainingAfter: Int? get() = remainingCalories?.let { it - (selected?.calories ?: 0) }

    companion object {
        const val DAYS_BACK = 7
    }
}

interface CopyFromCallbacks {
    fun onAnyMealSelected(isAnyMeal: Boolean)
    fun onPicked(meal: PastMeal)
    fun onCopy()

    companion object {
        fun default() = object : CopyFromCallbacks {
            override fun onAnyMealSelected(isAnyMeal: Boolean) = Unit
            override fun onPicked(meal: PastMeal) = Unit
            override fun onCopy() = Unit
        }
    }
}

class CopyFromViewModel(
    getAllFoodLogUseCase: GetAllFoodLogUseCase,
    getAllFoodUseCase: GetAllFoodUseCase,
    getFoodLibraryUseCase: GetFoodLibraryUseCase,
    getRemainingCaloriesUseCase: GetRemainingCaloriesUseCase,
    private val copyFoodLogsUseCase: CopyFoodLogsUseCase,
    private val copyEvents: FoodLogCopyEvents,
    savedStateHandle: SavedStateHandle
) : ViewModel(), CopyFromCallbacks {

    private val _state = MutableStateFlow(
        value = CopyFromViewState(
            mealType = MealType.entries.find { it.name == savedStateHandle[MEAL_TYPE] } ?: MealType.BREAKFAST,
            targetDate = logDateOf(argument = savedStateHandle[LOG_DATE])
        )
    )
    val state = _state.asStateFlow()

    private var isCopying = false

    init {
        viewModelScope.launch {
            combine(
                flow = getAllFoodLogUseCase(),
                flow2 = getAllFoodUseCase(),
                flow3 = getFoodLibraryUseCase()
            ) { logs, foods, libraryFoods ->
                logs to (libraryFoods.associate { it.food.name to it.food.unit } + foods.associate { it.name to it.unit })
            }.collect { (logs, foodUnit) ->
                _state.update { it.copy(allLogs = logs, foodUnit = foodUnit) }
            }
        }
        viewModelScope.launch {
            getRemainingCaloriesUseCase(date = _state.value.targetDate).collect { remaining ->
                // Once copied, the new logs already count; keep the figure from before.
                if (!isCopying) _state.update { it.copy(remainingCalories = remaining) }
            }
        }
    }

    override fun onAnyMealSelected(isAnyMeal: Boolean) {
        _state.update { it.copy(isAnyMeal = isAnyMeal) }
    }

    override fun onPicked(meal: PastMeal) {
        _state.update { it.copy(picked = meal) }
    }

    override fun onCopy() {
        val currentState = _state.value
        val meal = currentState.selected ?: return
        if (isCopying) return
        isCopying = true
        viewModelScope.launch {
            copyEvents.post(
                copy = copyFoodLogsUseCase(
                    logs = meal.logs,
                    targetDates = listOf(currentState.targetDate),
                    mealType = currentState.mealType,
                    replace = false
                )
            )
            _state.update { it.copy(isDone = true) }
        }
    }
}
