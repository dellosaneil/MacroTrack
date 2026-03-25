package org.thelazybattley.macrotrack.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase

class HomeTabViewModel(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase
) : ViewModel(), HomeTabCallbacks {

    private val _state = MutableStateFlow(value = HomeTabViewState())
    val state = _state

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
                        gender = userDetails.gender,
                        goal = userDetails.goal
                    )
                )
            }
        }
    }
}
