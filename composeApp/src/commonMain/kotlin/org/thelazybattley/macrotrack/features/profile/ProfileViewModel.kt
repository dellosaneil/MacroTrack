package org.thelazybattley.macrotrack.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.core.to2Decimal
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.usecase.CalculateBMIUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.InsertWeightUseCase
import org.thelazybattley.macrotrack.features.profile.ui.BMI

class ProfileViewModel(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateBMIUseCase: CalculateBMIUseCase,
    private val insertWeightUseCase: InsertWeightUseCase,
    private val insertUserDetailsUseCase: InsertUserDetailsUseCase
) : ViewModel(), ProfileCallbacks {

    private val _state = MutableStateFlow(value = ProfileViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getUserDetailsUseCase().let { userDetails ->
                _state.update { currentState ->
                    currentState.copy(
                        userDetails = userDetails,
                        weightInput = userDetails?.weight?.to2Decimal().toString(),
                    )
                }
            }
            updateBMI()
        }
    }


    override fun onWeightInput(weight: String) {
        _state.update { currentState ->
            currentState.copy(
                weightInput = weight
            )
        }
    }

    override fun onSaveWeight() {
        viewModelScope.launch {
            insertWeightUseCase(weight = _state.value.weightInput.toDouble())
            _state.update { currentState ->
                if(currentState.userDetails == null) return@update currentState
                insertUserDetailsUseCase(
                    userDetails = currentState.userDetails.copy(
                        weight = currentState.weightInput.toDouble()
                    )
                ).also {
                    updateBMI()
                }
                currentState.copy(
                    userDetails = currentState.userDetails.copy(
                        weight = currentState.weightInput.toDouble()
                    )
                )
            }
        }
    }

    override fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            _state.update {currentState ->
                if(currentState.userDetails == null) return@update currentState
                val updatedUserDetails = currentState.userDetails.copy(
                    goal = goal
                )
                insertUserDetailsUseCase(
                    userDetails = updatedUserDetails
                )
                currentState.copy(
                    userDetails = updatedUserDetails
                )
            }
        }
    }

    private suspend fun updateBMI() {
        calculateBMIUseCase().let { bmi ->
            val bmiCategory = when {
                (bmi <= BMI.UNDERWEIGHT.bmiIndex) -> BMI.UNDERWEIGHT
                (bmi <= BMI.NORMAL.bmiIndex) -> BMI.NORMAL
                (bmi <= BMI.OVERWEIGHT.bmiIndex) -> BMI.OVERWEIGHT
                else -> BMI.OBESE
            }
            val progress = when {
                (bmi <= BMI.UNDERWEIGHT.bmiIndex) -> {
                    bmi / BMI.UNDERWEIGHT.bmiIndex
                }

                (bmi <= BMI.NORMAL.bmiIndex) -> {
                    (bmi - BMI.UNDERWEIGHT.bmiIndex) / (BMI.NORMAL.bmiIndex - BMI.UNDERWEIGHT.bmiIndex)
                }

                (bmi <= BMI.OVERWEIGHT.bmiIndex) -> {
                    (bmi - BMI.NORMAL.bmiIndex) / (BMI.OVERWEIGHT.bmiIndex - BMI.NORMAL.bmiIndex)
                }

                else -> {
                    (bmi - BMI.OVERWEIGHT.bmiIndex) / (BMI.OBESE.bmiIndex - BMI.OVERWEIGHT.bmiIndex)
                }
            }
            _state.update { currentState ->
                currentState.copy(
                    profileBMI = ProfileBMI(
                        value = bmi.to2Decimal(),
                        category = bmiCategory,
                        progress = progress
                    )
                )
            }
        }
    }
}
