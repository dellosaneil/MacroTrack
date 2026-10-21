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
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.MacroBasis
import org.thelazybattley.macrotrack.domain.model.MacroTargets
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
                    macroGoals = calculateMacrosGoalUseCase(userDetails = userDetails)
                )
                val history = goalPeriods(
                    records = withCurrentGoal(records = records, today = today, current = current),
                    today = today
                ).reversed()
                _state.update { currentState ->
                    val isFirstLoad = currentState.userDetails == null
                    currentState.copy(
                        userDetails = userDetails,
                        selectedGoal = currentState.selectedGoal ?: userDetails.goal,
                        goalPace = currentState.goalPace ?: userDetails.goalPace,
                        macroBasis = if (isFirstLoad) userDetails.macroBasis else currentState.macroBasis,
                        bodyFat = if (isFirstLoad) userDetails.bodyFat else currentState.bodyFat,
                        isCustom = if (isFirstLoad) userDetails.customMacroGoals != null else currentState.isCustom,
                        customMacroGoals = currentState.customMacroGoals ?: userDetails.customMacroGoals,
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

    override fun onGoalPaceChanged(kgPerMonth: Double) {
        _state.update { currentState ->
            currentState.copy(goalPace = kgPerMonth).withCalculatedGoals()
        }
    }

    override fun onCustomSelected(isCustom: Boolean) {
        _state.update { currentState ->
            currentState.copy(
                isCustom = isCustom,
                customMacroGoals = currentState.customMacroGoals ?: currentState.macroGoals
            )
        }
    }

    override fun onMacroBasisSelected(basis: MacroBasis) {
        _state.update { currentState ->
            if (basis == MacroBasis.LEAN_MASS && currentState.bodyFat == null) return@update currentState
            currentState.copy(macroBasis = basis).withCalculatedGoals()
        }
    }

    override fun onBodyFatChanged(percent: Double) {
        _state.update { currentState ->
            currentState.copy(bodyFat = percent, macroBasis = MacroBasis.LEAN_MASS).withCalculatedGoals()
        }
    }

    override fun onCustomMacroGoalsChanged(macroGoals: MacroGoals) {
        _state.update { it.copy(customMacroGoals = macroGoals) }
    }

    override fun onSave() {
        val currentState = _state.value
        val userDetails = currentState.userDetails ?: return
        val goal = currentState.selectedGoal ?: return
        if (!currentState.canSave) return
        viewModelScope.launch {
            insertUserDetailsUseCase(
                userDetails = userDetails.copy(
                    goal = goal,
                    goalPace = currentState.goalPace ?: userDetails.goalPace,
                    macroBasis = currentState.macroBasis,
                    bodyFat = currentState.bodyFat,
                    customMacroGoals = if (currentState.isCustom) currentState.customMacroGoals else null
                )
            )
            _state.update { it.copy(isSaved = true) }
        }
    }

    private fun NutritionGoalsViewState.withCalculatedGoals(): NutritionGoalsViewState {
        val userDetails = userDetails ?: return this
        val goal = selectedGoal ?: return this
        val goalPace = goalPace ?: return this
        val maintenance = userDetails.measuredMaintenance ?: calculateTDEEUseCase(
            height = userDetails.height,
            weight = userDetails.weight,
            age = userDetails.age,
            gender = userDetails.sex,
            activityLevel = userDetails.activityLevel
        )
        val macroGoals = calculateMacrosGoalUseCase.calculated(
            userDetails = userDetails,
            goal = goal,
            goalPace = goalPace,
            macroBasis = macroBasis,
            bodyFat = bodyFat
        )
        return copy(
            maintenanceCalories = maintenance,
            restingBurn = MacroTargets.restingBurn(
                height = userDetails.height,
                weight = userDetails.weight,
                age = userDetails.age,
                sex = userDetails.sex
            ),
            macroGoals = macroGoals,
            targetBasisWeight = MacroTargets.basisWeight(
                weight = userDetails.weight,
                height = userDetails.height,
                targetWeight = userDetails.targetWeight,
                basis = MacroBasis.TARGET_WEIGHT,
                bodyFat = null
            ),
            leanBasisWeight = bodyFat?.let { MacroTargets.leanMass(weight = userDetails.weight, bodyFat = it) },
            fastestPaceKeepingCarbs = MacroTargets.fastestPaceKeepingCarbs(
                maintenance = maintenance,
                protein = macroGoals.protein,
                fat = macroGoals.fat
            )
        )
    }
}
