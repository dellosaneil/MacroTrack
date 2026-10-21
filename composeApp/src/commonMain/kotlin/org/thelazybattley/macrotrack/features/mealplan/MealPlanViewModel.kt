package org.thelazybattley.macrotrack.features.mealplan

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
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PLAN_DAYS_AHEAD
import org.thelazybattley.macrotrack.domain.model.PlannedFood
import org.thelazybattley.macrotrack.domain.model.RepeatingMeal
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.library.GetFoodLibraryUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.GetPlannedFoodsUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.GetRepeatingMealsUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.RemovePlannedFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.mealplan.SyncRepeatingMealsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.LOG_DATE

/** Food planned for [selectedDate], one of the [PLAN_DAYS_AHEAD] days after [today]. */
data class MealPlanViewState(
    val today: LocalDate,
    val selectedDate: LocalDate,
    val planned: List<PlannedFood> = emptyList(),
    val repeatingMeals: List<RepeatingMeal> = emptyList(),
    /** The current calorie goal, which a later day will be tracked with. */
    val calorieGoal: Int? = null,
    val foodUnit: Map<String, String> = emptyMap()
) {
    val days: List<LocalDate> get() = (1..PLAN_DAYS_AHEAD).map { today.plus(period = DatePeriod(days = it)) }

    fun hasPlan(date: LocalDate): Boolean = planned.any { it.date == date && it.isPending }

    /** What's planned on the selected day and still waiting; skipped food is left out. */
    val dayPlan: List<PlannedFood> get() = planned.filter { it.date == selectedDate && it.isPending }

    fun mealPlan(mealType: MealType): List<PlannedFood> = dayPlan.filter { it.mealType == mealType }

    val plannedCalories: Int get() = dayPlan.sumOf { it.food.calories }
    val plannedProtein: Double get() = dayPlan.sumOf { it.food.protein }
    val plannedCarbs: Double get() = dayPlan.sumOf { it.food.carbs }
    val plannedFat: Double get() = dayPlan.sumOf { it.food.fat }

    val activeRepeatingCount: Int get() = repeatingMeals.count { it.isActive && !it.hasEnded(today = today) }

    fun repeatingMealOf(planned: PlannedFood): RepeatingMeal? = repeatingMeals.find { it.id == planned.repeatingMealId }
}

interface MealPlanCallbacks {
    fun onDaySelected(date: LocalDate)

    /** Takes food off the day: deleted when planned by hand, skipped for the day when a repeating meal added it. */
    fun onRemove(planned: PlannedFood)

    companion object {
        fun default() = object : MealPlanCallbacks {
            override fun onDaySelected(date: LocalDate) = Unit
            override fun onRemove(planned: PlannedFood) = Unit
        }
    }
}

class MealPlanViewModel(
    getPlannedFoodsUseCase: GetPlannedFoodsUseCase,
    getRepeatingMealsUseCase: GetRepeatingMealsUseCase,
    getAllFoodUseCase: GetAllFoodUseCase,
    getFoodLibraryUseCase: GetFoodLibraryUseCase,
    getUserDetailsUseCase: GetUserDetailsUseCase,
    calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    syncRepeatingMealsUseCase: SyncRepeatingMealsUseCase,
    private val removePlannedFoodUseCase: RemovePlannedFoodUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), MealPlanCallbacks {

    private val _state = MutableStateFlow(
        value = run {
            val today = getCurrentDate()
            val tomorrow = today.plus(period = DatePeriod(days = 1))
            val requested = savedStateHandle.get<String>(key = LOG_DATE)?.let { runCatching { LocalDate.parse(input = it) }.getOrNull() }
            MealPlanViewState(
                today = today,
                selectedDate = requested?.takeIf { it > today && it <= today.plus(period = DatePeriod(days = PLAN_DAYS_AHEAD)) } ?: tomorrow
            )
        }
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch { syncRepeatingMealsUseCase() }
        viewModelScope.launch {
            combine(flow = getPlannedFoodsUseCase(), flow2 = getRepeatingMealsUseCase()) { planned, repeatingMeals ->
                planned to repeatingMeals
            }.collect { (planned, repeatingMeals) ->
                _state.update { it.copy(planned = planned, repeatingMeals = repeatingMeals) }
            }
        }
        viewModelScope.launch {
            combine(flow = getAllFoodUseCase(), flow2 = getFoodLibraryUseCase()) { foods, libraryFoods ->
                libraryFoods.associate { it.food.name to it.food.unit } + foods.associate { it.name to it.unit }
            }.collect { foodUnit ->
                _state.update { it.copy(foodUnit = foodUnit) }
            }
        }
        viewModelScope.launch {
            getUserDetailsUseCase().collect { user ->
                _state.update { it.copy(calorieGoal = user?.let { details -> calculateMacrosGoalUseCase(userDetails = details).calories }) }
            }
        }
    }

    override fun onDaySelected(date: LocalDate) {
        _state.update { it.copy(selectedDate = date) }
    }

    override fun onRemove(planned: PlannedFood) {
        viewModelScope.launch { removePlannedFoodUseCase(planned = planned) }
    }
}
