package org.thelazybattley.macrotrack.features.profile.nutritiongoals

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
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.goalPeriods
import org.thelazybattley.macrotrack.domain.model.withCurrentGoal
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateTDEEUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase

class NutritionGoalsViewModel(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val insertUserDetailsUseCase: InsertUserDetailsUseCase,
    private val calculateTDEEUseCase: CalculateTDEEUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val getAllDailyGoalsUseCase: GetAllDailyGoalsUseCase,
) : ViewModel(), NutritionGoalsCallbacks {

    private val _state = MutableStateFlow(value = NutritionGoalsViewState())

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = flow { emitAll(getUserDetailsUseCase()) },
                flow2 = getAllDailyGoalsUseCase()
            ) { userDetails, records ->
                if (userDetails == null) return@combine
                val today = getCurrentDate()
                val current = DailyGoal(
                    date = today,
                    goal = userDetails.goal,
                    macroGoals = calculateMacrosGoalUseCase(
                        height = userDetails.height,
                        weight = userDetails.weight,
                        age = userDetails.age,
                        gender = userDetails.sex,
                        activityLevel = userDetails.activityLevel,
                        goal = userDetails.goal
                    )
                )
                val history = goalPeriods(
                    records = withCurrentGoal(records = records, today = today, current = current),
                    today = today
                ).reversed()
                _state.update { currentState ->
                    currentState.copy(
                        userDetails = userDetails,
                        selectedGoal = currentState.selectedGoal ?: userDetails.goal,
                        history = history
                    ).withCalculatedGoals()
                }
            }.collect()
        }
    }

    override fun onGoalSelected(goal: Goal) {
        _state.update { currentState ->
            currentState.copy(selectedGoal = goal).withCalculatedGoals()
        }
    }

    override fun onSave() {
        val currentState = _state.value
        val userDetails = currentState.userDetails ?: return
        val goal = currentState.selectedGoal ?: return
        if (!currentState.canSave) return
        viewModelScope.launch {
            insertUserDetailsUseCase(userDetails = userDetails.copy(goal = goal))
            _state.update { it.copy(isSaved = true) }
        }
    }

    private fun NutritionGoalsViewState.withCalculatedGoals(): NutritionGoalsViewState {
        val userDetails = userDetails ?: return this
        val goal = selectedGoal ?: return this
        return copy(
            maintenanceCalories = calculateTDEEUseCase(
                height = userDetails.height,
                weight = userDetails.weight,
                age = userDetails.age,
                gender = userDetails.sex,
                activityLevel = userDetails.activityLevel
            ),
            macroGoals = calculateMacrosGoalUseCase(
                height = userDetails.height,
                weight = userDetails.weight,
                age = userDetails.age,
                gender = userDetails.sex,
                activityLevel = userDetails.activityLevel,
                goal = goal
            )
        )
    }
}
