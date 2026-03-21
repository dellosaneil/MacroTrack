package org.thelazybattley.macrotrack.features.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserGender
import org.thelazybattley.macrotrack.features.onboarding.ui.OnboardingStep

class OnboardingViewModel : ViewModel(), OnboardingCallbacks {
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
        TODO("Not yet implemented")
    }

    override fun onGenderSelected(gender: UserGender) {
        _state.update { currentState ->
            currentState.copy(
                selectedGender = gender
            )
        }
    }

    override fun onContinueClicked() {
        _state.update { currentState ->
            val nextStepNumber = currentState.currentStep.ordinal
            currentState.copy(
                currentStep = OnboardingStep.entries.firstOrNull { it.ordinal == nextStepNumber }
                    ?: OnboardingStep.GOAL_AND_STATS
            )
        }
    }

}
