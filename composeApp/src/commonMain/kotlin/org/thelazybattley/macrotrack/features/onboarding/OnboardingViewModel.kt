package org.thelazybattley.macrotrack.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.UserGender
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.features.onboarding.ui.OnboardingStep

class OnboardingViewModel(
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val insertUserDetailsUseCase: InsertUserDetailsUseCase,
) :
    ViewModel(), OnboardingCallbacks {
    private val _state = MutableStateFlow(OnboardingViewState())
    val state = _state.asStateFlow()

    override fun onGoalSelected(goal: Goal) {
        _state.update { currentState ->
            currentState.copy(
                selectedGoal = goal
            )
        }
    }

    override fun onActivityLevelSelected(activityLevel: ActivityLevel) {
        _state.update { currentState ->
            currentState.copy(
                selectedActivityLevel = activityLevel
            )
        }
    }

    override fun onGenderSelected(gender: UserGender) {
        _state.update { currentState ->
            currentState.copy(
                selectedGender = gender
            )
        }
    }

    override fun onContinueClicked() {
        if (_state.value.currentStep == OnboardingStep.SUMMARY) {
            viewModelScope.launch {
                val userDetails = UserDetails(
                    weight = _state.value.weight,
                    age = _state.value.age,
                    height = _state.value.height,
                    gender = _state.value.selectedGender!!,
                    activityLevel = _state.value.selectedActivityLevel!!,
                    goal = _state.value.selectedGoal!!
                )
                insertUserDetailsUseCase(
                    userDetails = userDetails
                )
            }
            return
        }


        _state.update { currentState ->
            val nextStepNumber = currentState.currentStep.ordinal.inc()
            var macroGoals: MacroGoals? = null
            if (nextStepNumber == OnboardingStep.SUMMARY.ordinal && listOf(
                    currentState.selectedGender,
                    currentState.selectedActivityLevel,
                    currentState.selectedGoal
                ).all { it != null }
            ) {
                macroGoals = calculateMacrosGoalUseCase(
                    height = currentState.height,
                    weight = currentState.weight,
                    age = currentState.age,
                    gender = currentState.selectedGender!!,
                    activityLevel = currentState.selectedActivityLevel!!,
                    goal = currentState.selectedGoal!!
                )
            }
            currentState.copy(
                currentStep = OnboardingStep.entries.firstOrNull { it.ordinal == nextStepNumber }
                    ?: OnboardingStep.GOAL_AND_STATS,
                macroGoals = macroGoals
            )

        }
    }

    override fun onAgeUpdated(age: Int) {
        _state.update { currentState ->
            currentState.copy(
                age = age
            )
        }
    }

    override fun onHeightUpdated(height: Double) {
        _state.update { currentState ->
            currentState.copy(
                height = height
            )
        }
    }

    override fun onWeightUpdated(weight: Double) {
        _state.update { currentState ->
            currentState.copy(
                weight = weight
            )
        }
    }

    override fun onBackClicked() {
        _state.update { currentState ->
            val nextStepNumber = currentState.currentStep.ordinal.dec()
            currentState.copy(
                currentStep = OnboardingStep.entries.firstOrNull { it.ordinal == nextStepNumber }
                    ?: OnboardingStep.GOAL_AND_STATS
            )
        }
    }

}
