package org.thelazybattley.macrotrack.features.profile.personalinformation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.UserSex
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.units.GetUnitSystemUseCase
import org.thelazybattley.macrotrack.domain.usecase.units.SetUnitSystemUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase

class PersonalInformationViewModel(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val insertUserDetailsUseCase: InsertUserDetailsUseCase,
    private val getAllWeightUseCase: GetAllWeightUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    getUnitSystemUseCase: GetUnitSystemUseCase,
    private val setUnitSystemUseCase: SetUnitSystemUseCase,
) : ViewModel(), PersonalInformationCallbacks {

    private val _state = MutableStateFlow(value = PersonalInformationViewState())

    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getUserDetailsUseCase(),
                flow2 = getAllWeightUseCase(),
                flow3 = getUnitSystemUseCase()
            ) { userDetails, weights, unitSystem ->
                _state.update { currentState ->
                    val isFirstLoad = currentState.userDetails == null && userDetails != null
                    // Switching units keeps whatever height was typed, shown in the new unit.
                    val heightCm = if (isFirstLoad) userDetails.height else currentState.parsedHeight
                    val updated = currentState.copy(
                        userDetails = userDetails,
                        latestWeight = weights.maxByOrNull { it.date },
                        unitSystem = unitSystem,
                        age = if (isFirstLoad) userDetails.age.toString() else currentState.age,
                        sex = if (isFirstLoad) userDetails.sex else currentState.sex,
                        activityLevel = if (isFirstLoad) userDetails.activityLevel else currentState.activityLevel
                    )
                    val isUnitChanged = unitSystem != currentState.unitSystem
                    when {
                        heightCm == null -> updated
                        isFirstLoad || isUnitChanged -> updated.withHeightFields(cm = heightCm)
                        else -> updated
                    }.withDailyCalories()
                }
            }.collect()
        }
    }

    override fun onAgeChanged(age: String) {
        if (age.length > 3 || age.any { !it.isDigit() }) return
        _state.update { currentState -> currentState.copy(age = age).withDailyCalories() }
    }

    override fun onHeightChanged(height: String) {
        if (height.length > 5 || height.count { it == '.' } > 1 || height.any { !it.isDigit() && it != '.' }) return
        _state.update { currentState -> currentState.copy(height = height).withDailyCalories() }
    }

    override fun onHeightFeetChanged(feet: String) {
        if (feet.length > 1 || feet.any { !it.isDigit() }) return
        _state.update { currentState -> currentState.copy(heightFeet = feet).withDailyCalories() }
    }

    override fun onHeightInchesChanged(inches: String) {
        if (inches.length > 4 || inches.count { it == '.' } > 1 || inches.any { !it.isDigit() && it != '.' }) return
        _state.update { currentState -> currentState.copy(heightInches = inches).withDailyCalories() }
    }

    override fun onUnitSystemSelected(unitSystem: UnitSystem) {
        viewModelScope.launch { setUnitSystemUseCase(unitSystem = unitSystem) }
    }

    override fun onSexSelected(sex: UserSex) {
        _state.update { currentState -> currentState.copy(sex = sex).withDailyCalories() }
    }

    override fun onActivityLevelSelected(activityLevel: ActivityLevel) {
        _state.update { currentState ->
            currentState.copy(activityLevel = activityLevel).withDailyCalories()
        }
    }

    override fun onSave() {
        val currentState = _state.value
        val userDetails = currentState.userDetails ?: return
        val age = currentState.parsedAge ?: return
        val height = currentState.parsedHeight ?: return
        if (!currentState.canSave) return
        viewModelScope.launch {
            insertUserDetailsUseCase(
                userDetails = userDetails.copy(
                    age = age,
                    // Keep the saved height when only feet-and-inches rounding moved it.
                    height = if (currentState.isHeightChanged) height else userDetails.height,
                    sex = currentState.sex ?: userDetails.sex,
                    activityLevel = currentState.activityLevel ?: userDetails.activityLevel,
                    // New details mean a new estimate; the next weekly check-in measures the burn again.
                    measuredMaintenance = null
                )
            )
            _state.update { it.copy(isSaved = true) }
        }
    }

    private fun PersonalInformationViewState.withDailyCalories(): PersonalInformationViewState {
        val userDetails = userDetails ?: return this
        val age = parsedAge ?: return this
        val height = parsedHeight ?: return this
        val calories = calculateMacrosGoalUseCase(
            height = height,
            weight = currentWeight ?: userDetails.weight,
            age = age,
            gender = sex ?: userDetails.sex,
            activityLevel = activityLevel ?: userDetails.activityLevel,
            goal = userDetails.goal,
            goalPace = userDetails.goalPace
        ).calories
        return copy(dailyCalories = calories)
    }
}
