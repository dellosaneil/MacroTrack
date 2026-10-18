package org.thelazybattley.macrotrack.features.onboarding

import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateTDEEUseCase
import org.thelazybattley.macrotrack.domain.usecase.units.SetUnitSystemUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.features.onboarding.ui.OnboardingStep

class OnboardingViewModel(
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val calculateTDEEUseCase: CalculateTDEEUseCase,
    private val insertUserDetailsUseCase: InsertUserDetailsUseCase,
    private val setUnitSystemUseCase: SetUnitSystemUseCase,
    private val telemetry: Telemetry
) : ViewModel(), OnboardingCallbacks {

    private val _state = MutableStateFlow(value = OnboardingViewState())
    val state = _state.asStateFlow()

    override fun onGoalSelected(goal: Goal) {
        _state.update { it.copy(selectedGoal = goal).withPlan() }
    }

    override fun onGoalPaceChanged(kgPerMonth: Double) {
        _state.update { it.copy(goalPace = kgPerMonth).withPlan() }
    }

    override fun onActivityLevelSelected(activityLevel: ActivityLevel) {
        _state.update { it.copy(selectedActivityLevel = activityLevel).withPlan() }
    }

    override fun onGenderSelected(gender: UserSex) {
        _state.update { it.copy(selectedGender = gender).withPlan() }
    }

    override fun onAgeUpdated(age: Int) {
        _state.update { it.copy(age = age).withPlan() }
    }

    override fun onHeightUpdated(height: Double) {
        _state.update { it.copy(height = height).withPlan() }
    }

    override fun onWeightUpdated(weight: Double) {
        _state.update { it.copy(weight = weight).withPlan() }
    }

    override fun onUnitSystemSelected(unitSystem: UnitSystem) {
        viewModelScope.launch { setUnitSystemUseCase(unitSystem = unitSystem) }
    }

    override fun onContinueClicked() {
        val currentState = _state.value
        if (!currentState.canContinue) return
        if (currentState.currentStep == OnboardingStep.SUMMARY) {
            val gender = currentState.selectedGender ?: return
            val activityLevel = currentState.selectedActivityLevel ?: return
            val goal = currentState.selectedGoal ?: return
            viewModelScope.launch {
                insertUserDetailsUseCase(
                    userDetails = UserDetails(
                        weight = currentState.weight,
                        age = currentState.age,
                        height = currentState.height,
                        sex = gender,
                        activityLevel = activityLevel,
                        goal = goal,
                        goalPace = currentState.goalPace
                    )
                )
                telemetry.logEvent(
                    name = TelemetryEvent.ONBOARDING_COMPLETE,
                    params = mapOf(TelemetryEvent.PARAM_GOAL to goal.name.lowercase())
                )
                _state.update { it.copy(isFinished = true) }
            }
            return
        }
        _state.update {
            it.copy(currentStep = OnboardingStep.entries[it.currentStep.ordinal + 1])
        }
    }

    override fun onBackClicked() {
        _state.update {
            it.copy(currentStep = OnboardingStep.entries[(it.currentStep.ordinal - 1).coerceAtLeast(minimumValue = 0)])
        }
    }

    /** Recalculates the daily target as soon as every input is known. */
    private fun OnboardingViewState.withPlan(): OnboardingViewState {
        val gender = selectedGender
        val activityLevel = selectedActivityLevel
        val goal = selectedGoal
        if (gender == null || activityLevel == null || goal == null || age <= 0 || height <= 0 || weight <= 0) {
            return copy(macroGoals = null, maintenanceCalories = null)
        }
        return copy(
            macroGoals = calculateMacrosGoalUseCase(
                height = height,
                weight = weight,
                age = age,
                gender = gender,
                activityLevel = activityLevel,
                goal = goal,
                goalPace = goalPace
            ),
            maintenanceCalories = calculateTDEEUseCase(
                height = height,
                weight = weight,
                age = age,
                gender = gender,
                activityLevel = activityLevel
            )
        )
    }
}
