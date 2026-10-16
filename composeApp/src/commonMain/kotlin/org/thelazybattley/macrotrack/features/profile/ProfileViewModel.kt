package org.thelazybattley.macrotrack.features.profile

import androidx.lifecycle.ViewModel
import org.thelazybattley.macrotrack.domain.usecase.backup.GetLastBackupUseCase
import org.thelazybattley.macrotrack.domain.usecase.units.SetUnitSystemUseCase
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.features.profile.ui.ProfileSectionEnum

class ProfileViewModel(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val insertUserDetailsUseCase: InsertUserDetailsUseCase,
    private val getAllWeightUseCase: GetAllWeightUseCase,
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val setUnitSystemUseCase: SetUnitSystemUseCase,
    getLastBackupUseCase: GetLastBackupUseCase
) : ViewModel(), ProfileCallbacks {

    private val _state = MutableStateFlow(value = ProfileViewState())
    val state = _state.asStateFlow()

    private var weights: List<Weight> = emptyList()

    init {
        viewModelScope.launch {
            getLastBackupUseCase().collect { lastBackup -> _state.update { it.copy(lastBackup = lastBackup) } }
        }
        viewModelScope.launch {
            combine(
                flow = getUserDetailsUseCase(),
                flow2 = getAllWeightUseCase(),
                flow3 = getAllFoodLogUseCase()
            ) { userDetails, weights, foodLogs ->
                val today = getCurrentDate()
                this@ProfileViewModel.weights = weights
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
                            calculateGoalProgress(userDetails = it, weights = weights, today = today)
                        },
                        weekSummary = calculateWeekSummary(
                            foodLogs = foodLogs,
                            weights = weights,
                            macroGoals = macroGoals,
                            today = today
                        ),
                        firstWeightDate = weights.minOfOrNull { it.date }
                    )
                }
            }.collect()
        }
    }

    override fun onUnitSystemSelected(unitSystem: UnitSystem) {
        viewModelScope.launch { setUnitSystemUseCase(unitSystem = unitSystem) }
    }

    override fun onSaveTargetWeight(targetKg: Double) {
        val newTarget = targetKg.takeIf { it > 0 } ?: return
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

    override fun onStartDateSelected(date: LocalDate) {
        val userDetails = _state.value.userDetails ?: return
        val startWeight = startWeightOn(date = date, weights = weights)?.weight ?: userDetails.weight
        viewModelScope.launch {
            insertUserDetailsUseCase(
                userDetails = userDetails.copy(
                    targetStartWeight = startWeight,
                    targetSetDate = date
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
