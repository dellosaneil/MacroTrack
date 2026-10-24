package org.thelazybattley.macrotrack.features.logweight

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.usecase.units.GetUnitSystemUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.InsertWeightUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.navigation.logDateOf
import kotlin.math.round

/** One tap of − or + moves the typed weight by this much, in whichever unit it's typed in. */
private const val STEP = 0.1

data class LogWeightViewState(
    val date: LocalDate = getCurrentDate(),
    /** The typed weight, in [unitSystem]'s unit. */
    val input: String = "",
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val weights: List<Weight> = emptyList(),
    val userDetails: UserDetails? = null,
    val isLoaded: Boolean = false,
    val isSaved: Boolean = false
) {
    /** The typed weight in kg. */
    val weight: Double? get() = inputValue?.let { unitSystem.weightToKg(value = it) }

    private val inputValue: Double? get() = input.toDoubleOrNull()?.takeIf { it > 0 }

    /** The most recent entry before the selected date. */
    val previous: Weight? get() = weights.lastOrNull { it.date < date }

    /** An entry already saved for the selected date; saving replaces it. */
    val existing: Weight? get() = weights.find { it.date == date }

    /** The first entry of the selected date's month, excluding the selected date itself. */
    val monthBaseline: Weight?
        get() = weights.firstOrNull {
            it.date.year == date.year && it.date.month == date.month && it.date != date
        }
}

class LogWeightViewModel(
    savedStateHandle: SavedStateHandle,
    getAllWeightUseCase: GetAllWeightUseCase,
    getUserDetailsUseCase: GetUserDetailsUseCase,
    getUnitSystemUseCase: GetUnitSystemUseCase,
    private val insertWeightUseCase: InsertWeightUseCase,
    private val insertUserDetailsUseCase: InsertUserDetailsUseCase
) : ViewModel() {

    // A progress photo's day without a weigh-in opens here on that day.
    private val _state = MutableStateFlow(
        value = LogWeightViewState(
            date = minOf(logDateOf(argument = savedStateHandle.get<String>(key = AppDestinations.LOG_DATE)), getCurrentDate())
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getAllWeightUseCase(),
                flow2 = flow { emitAll(getUserDetailsUseCase()) },
                flow3 = getUnitSystemUseCase()
            ) { weights, userDetails, unitSystem ->
                _state.update { currentState ->
                    val updated = currentState.copy(
                        weights = weights.sortedBy { it.date },
                        userDetails = userDetails,
                        unitSystem = unitSystem
                    )
                    if (currentState.isLoaded) updated else updated.withDefaultInput().copy(isLoaded = true)
                }
            }.collect()
        }
    }

    fun onDateSelected(date: LocalDate) {
        if (date > getCurrentDate()) return
        _state.update { it.copy(date = date).withDefaultInput() }
    }

    fun onInputChanged(value: String) {
        if (value.length > 6 || value.count { it == '.' } > 1 || value.any { !it.isDigit() && it != '.' }) return
        _state.update { it.copy(input = value) }
    }

    fun onStep(up: Boolean) {
        _state.update { currentState ->
            val base = currentState.input.toDoubleOrNull()?.takeIf { it > 0 }
                ?: currentState.previous?.weight?.let { currentState.unitSystem.weightFromKg(kg = it) }
                ?: return@update currentState
            val next = base + if (up) STEP else -STEP
            if (next <= 0) return@update currentState
            currentState.copy(input = next.roundTo2().toCleanString())
        }
    }

    /** [value] is in the unit the weight is typed in. */
    fun onQuickAdjust(value: Double) {
        if (value <= 0) return
        _state.update { it.copy(input = value.roundTo2().toCleanString()) }
    }

    fun onSave() {
        val currentState = _state.value
        val weight = currentState.weight ?: return
        if (currentState.isSaved) return
        viewModelScope.launch {
            insertWeightUseCase(weight = weight, date = currentState.date)
            val latestDate = currentState.weights.lastOrNull()?.date
            val userDetails = currentState.userDetails
            // Only the newest entry represents the current weight.
            if (userDetails != null && (latestDate == null || currentState.date >= latestDate)) {
                insertUserDetailsUseCase(userDetails = userDetails.copy(weight = weight))
            }
            _state.update { it.copy(isSaved = true) }
        }
    }

    private fun LogWeightViewState.withDefaultInput(): LogWeightViewState {
        val default = existing?.weight ?: previous?.weight ?: weights.lastOrNull()?.weight ?: userDetails?.weight
        return copy(input = default?.let { unitSystem.weightFromKg(kg = it).roundTo2().toCleanString() }.orEmpty())
    }

    private fun Double.roundTo2(): Double = round(this * 100) / 100
}
