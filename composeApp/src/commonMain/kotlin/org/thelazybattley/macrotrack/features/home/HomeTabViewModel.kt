package org.thelazybattley.macrotrack.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

class HomeTabViewModel(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase
) : ViewModel(), HomeTabCallbacks {

    private val _state = MutableStateFlow(value = HomeTabViewState())
    val state = _state

    override fun onNavigation(destination: AppDestinations.Root) {
        _state.update { currentState ->
            currentState.copy(
                isNavigationTriggered = true,
                onNavigation = destination
            )
        }
    }

    override fun resetNavigation() {
        _state.update {currentState ->
            currentState.copy(
                isNavigationTriggered = false,
                onNavigation = null
            )
        }
    }

    init {
        viewModelScope.launch {
            _state.update { currentState ->
                val userDetails = getUserDetailsUseCase() ?: return@launch
                currentState.copy(
                    userDetails = userDetails,
                    macroGoals = calculateMacrosGoalUseCase(
                        height = userDetails.height,
                        weight = userDetails.weight,
                        age = userDetails.age,
                        activityLevel = userDetails.activityLevel,
                        gender = userDetails.sex,
                        goal = userDetails.goal
                    )
                )
            }
        }
    }
}
