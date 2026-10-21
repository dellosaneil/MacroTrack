package org.thelazybattley.macrotrack.features.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.domain.usecase.food.GetAllFoodUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase

data class InsightsViewState(
    val insights: Insights,
    val foodUnits: Map<String, String>
)

class InsightsViewModel(
    getAllFoodLogUseCase: GetAllFoodLogUseCase,
    getAllDailyGoalsUseCase: GetAllDailyGoalsUseCase,
    getAllFoodUseCase: GetAllFoodUseCase,
    getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase
) : ViewModel() {

    private val range = MutableStateFlow(value = InsightsRange.WEEK)

    val state = combine(
        getAllFoodLogUseCase(),
        getAllDailyGoalsUseCase(),
        flow { emitAll(getAllFoodUseCase()) },
        flow { emitAll(getUserDetailsUseCase()) },
        range
    ) { foodLogs, dailyGoals, foods, userDetails, selectedRange ->
        val currentMacroGoals = userDetails?.let {
            calculateMacrosGoalUseCase(
                height = it.height,
                weight = it.weight,
                age = it.age,
                gender = it.sex,
                activityLevel = it.activityLevel,
                goal = it.goal,
                goalPace = it.goalPace,
                measuredMaintenance = it.measuredMaintenance
            )
        }
        InsightsViewState(
            insights = calculateInsights(
                range = selectedRange,
                today = getCurrentDate(),
                foodLogs = foodLogs,
                dailyGoals = dailyGoals,
                currentGoal = userDetails?.goal,
                currentMacroGoals = currentMacroGoals
            ),
            foodUnits = foods.associate { it.name to it.unit }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = null
    )

    fun onRangeSelected(range: InsightsRange) {
        this.range.value = range
    }
}
