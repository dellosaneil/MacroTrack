package org.thelazybattley.macrotrack.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.InsertWeightUseCase
import org.thelazybattley.macrotrack.features.profile.ui.ProfileSectionEnum

class ProfileViewModel(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val insertWeightUseCase: InsertWeightUseCase,
    private val insertUserDetailsUseCase: InsertUserDetailsUseCase,
    private val getAllWeightUseCase: GetAllWeightUseCase,
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase
) : ViewModel(), ProfileCallbacks {

    private val _state = MutableStateFlow(value = ProfileViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getUserDetailsUseCase(),
                flow2 = getAllWeightUseCase(),
                flow3 = getAllFoodLogUseCase()
            ) { userDetails, weights, foodLogs ->
                val today = getCurrentDate()
                if (userDetails != null && userDetails.targetWeight != null && userDetails.targetStartWeight == null) {
                    // Targets saved before the start point was recorded begin from today's weight.
                    insertUserDetailsUseCase(
                        userDetails = userDetails.copy(
                            targetStartWeight = userDetails.weight,
                            targetSetDate = today
                        )
                    )
                    return@combine
                }
                val macroGoals = userDetails?.let {
                    calculateMacrosGoalUseCase(
                        height = it.height,
                        weight = it.weight,
                        age = it.age,
                        gender = it.sex,
                        activityLevel = it.activityLevel,
                        goal = it.goal
                    )
                }
                _state.update { currentState ->
                    currentState.copy(
                        userDetails = userDetails,
                        macroGoals = macroGoals,
                        goalProgress = userDetails?.let {
                            calculateGoalProgress(userDetails = it, today = today)
                        },
                        weekSummary = calculateWeekSummary(
                            foodLogs = foodLogs,
                            weights = weights,
                            macroGoals = macroGoals,
                            today = today
                        )
                    )
                }
            }.collect()
        }
    }

    override fun onSaveWeight(weight: String) {
        val newWeight = weight.toDoubleOrNull()?.takeIf { it > 0 } ?: return
        val userDetails = _state.value.userDetails ?: return
        viewModelScope.launch {
            insertWeightUseCase(weight = newWeight)
            insertUserDetailsUseCase(userDetails = userDetails.copy(weight = newWeight))
        }
    }

    override fun onSaveTargetWeight(targetWeight: String) {
        val newTarget = targetWeight.toDoubleOrNull()?.takeIf { it > 0 } ?: return
        val userDetails = _state.value.userDetails ?: return
        viewModelScope.launch {
            insertUserDetailsUseCase(
                userDetails = userDetails.copy(
                    targetWeight = newTarget,
                    targetStartWeight = userDetails.weight,
                    targetSetDate = getCurrentDate()
                )
            )
        }
    }

    override fun onNavigate(sectionEnum: ProfileSectionEnum) {
        _state.update { currentState ->
            currentState.copy(route = sectionEnum.name)
        }
    }

    override fun onResetNavigation() {
        _state.update { currentState ->
            currentState.copy(route = null)
        }
    }
}
