package org.thelazybattley.macrotrack.features.measurements.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.Measurement
import org.thelazybattley.macrotrack.domain.model.MeasurementType
import org.thelazybattley.macrotrack.domain.usecase.measurement.GetAllMeasurementsUseCase
import org.thelazybattley.macrotrack.domain.usecase.measurement.InsertMeasurementsUseCase
import org.thelazybattley.macrotrack.features.measurements.MeasurementSummary
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnit
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnitPreference
import org.thelazybattley.macrotrack.features.measurements.formatMeasure
import org.thelazybattley.macrotrack.features.measurements.summaries

data class MeasurementsLogViewState(
    val date: LocalDate = getCurrentDate(),
    val unit: MeasurementUnit = MeasurementUnit.CM,
    val inputs: Map<MeasurementType, String> = emptyMap(),
    val previous: Map<MeasurementType, MeasurementSummary> = emptyMap(),
    val note: String = "",
    val isSaved: Boolean = false
) {
    val validValues: Map<MeasurementType, Double>
        get() = inputs.mapNotNull { (type, text) ->
            text.toDoubleOrNull()?.takeIf { it > 0 }?.let { type to it }
        }.toMap()
}

class MeasurementsLogViewModel(
    getAllMeasurementsUseCase: GetAllMeasurementsUseCase,
    private val insertMeasurementsUseCase: InsertMeasurementsUseCase,
    private val unitPreference: MeasurementUnitPreference
) : ViewModel() {

    private val _state = MutableStateFlow(value = MeasurementsLogViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getAllMeasurementsUseCase(),
                flow2 = unitPreference.unit
            ) { measurements, unit ->
                _state.update { currentState ->
                    currentState.copy(
                        previous = measurements.summaries(),
                        inputs = currentState.inputs.convert(from = currentState.unit, to = unit),
                        unit = unit
                    )
                }
            }.collect()
        }
    }

    fun onDateSelected(date: LocalDate) {
        if (date > getCurrentDate()) return
        _state.update { it.copy(date = date) }
    }

    fun onUnitSelected(unit: MeasurementUnit) {
        unitPreference.set(unit = unit)
    }

    fun onValueChanged(type: MeasurementType, value: String) {
        if (value.length > 6 || value.count { it == '.' } > 1 || value.any { !it.isDigit() && it != '.' }) return
        _state.update { it.copy(inputs = it.inputs + (type to value)) }
    }

    fun onNoteChanged(note: String) {
        _state.update { it.copy(note = note) }
    }

    fun onSave() {
        val currentState = _state.value
        val values = currentState.validValues
        if (values.isEmpty() || currentState.isSaved) return
        viewModelScope.launch {
            insertMeasurementsUseCase(
                measurements = values.map { (type, value) ->
                    Measurement(
                        date = currentState.date,
                        type = type,
                        valueCm = currentState.unit.toCm(value = value),
                        note = currentState.note.trim().takeIf { it.isNotEmpty() }
                    )
                }
            )
            _state.update { it.copy(isSaved = true) }
        }
    }

    private fun Map<MeasurementType, String>.convert(
        from: MeasurementUnit,
        to: MeasurementUnit
    ): Map<MeasurementType, String> {
        if (from == to) return this
        return mapValues { (_, text) ->
            text.toDoubleOrNull()?.let { to.fromCm(cm = from.toCm(value = it)).formatMeasure() } ?: text
        }
    }
}
