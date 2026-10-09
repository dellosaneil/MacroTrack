package org.thelazybattley.macrotrack.features.logweight

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
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.InsertWeightUseCase
import kotlin.math.round

private const val STEP_KG = 0.1

data class LogWeightViewState(
    val date: LocalDate = getCurrentDate(),
    val input: String = "",
    val weights: List<Weight> = emptyList(),
    val userDetails: UserDetails? = null,
    val isLoaded: Boolean = false,
    val isSaved: Boolean = false
) {
    val weight: Double? get() = input.toDoubleOrNull()?.takeIf { it > 0 }

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
    getAllWeightUseCase: GetAllWeightUseCase,
    getUserDetailsUseCase: GetUserDetailsUseCase,
    private val insertWeightUseCase: InsertWeightUseCase,
    private val insertUserDetailsUseCase: InsertUserDetailsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(value = LogWeightViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getAllWeightUseCase(),
                flow2 = flow { emitAll(getUserDetailsUseCase()) }
            ) { weights, userDetails ->
                _state.update { currentState ->
                    val updated = currentState.copy(
                        weights = weights.sortedBy { it.date },
                        userDetails = userDetails
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
            val base = currentState.weight ?: currentState.previous?.weight ?: return@update currentState
            val next = base + if (up) STEP_KG else -STEP_KG
            if (next <= 0) return@update currentState
            currentState.copy(input = next.roundTo2().toCleanString())
        }
    }

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
        return copy(input = default?.roundTo2()?.toCleanString().orEmpty())
    }

    private fun Double.roundTo2(): Double = round(this * 100) / 100
}
