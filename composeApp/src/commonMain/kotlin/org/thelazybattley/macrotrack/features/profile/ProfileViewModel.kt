package org.thelazybattley.macrotrack.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.usecase.CalculateBMIUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase

class ProfileViewModel(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateBMIUseCase: CalculateBMIUseCase
): ViewModel(), ProfileCallbacks {

    private val _state = MutableStateFlow(value = ProfileViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getUserDetailsUseCase().let { userDetails ->
                _state.update {  currentState ->
                    currentState.copy(
                        currentGoal = userDetails?.goal,
                    )
                }
            }
            calculateBMIUseCase().let { bmi ->
                _state.update { currentState ->
                    currentState.copy(
                        bmi = bmi
                    )
                }
            }
        }
    }
}
