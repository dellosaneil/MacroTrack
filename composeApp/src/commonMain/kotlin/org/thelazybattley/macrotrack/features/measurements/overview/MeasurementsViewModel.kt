package org.thelazybattley.macrotrack.features.measurements.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.MeasurementType
import org.thelazybattley.macrotrack.domain.usecase.measurement.GetAllMeasurementsUseCase
import org.thelazybattley.macrotrack.features.measurements.MeasurementSummary
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnit
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnitPreference
import org.thelazybattley.macrotrack.features.measurements.measuredDates
import org.thelazybattley.macrotrack.features.measurements.summaries

data class MeasurementsViewState(
    val unit: MeasurementUnit = MeasurementUnit.CM,
    val summaries: Map<MeasurementType, MeasurementSummary> = emptyMap(),
    val lastMeasured: LocalDate? = null,
    val previousMeasured: LocalDate? = null,
    val entryDays: Int = 0,
    val firstMeasured: LocalDate? = null
)

class MeasurementsViewModel(
    getAllMeasurementsUseCase: GetAllMeasurementsUseCase,
    private val unitPreference: MeasurementUnitPreference
) : ViewModel() {

    val state = combine(
        flow = getAllMeasurementsUseCase(),
        flow2 = unitPreference.unit
    ) { measurements, unit ->
        val dates = measurements.measuredDates()
        MeasurementsViewState(
            unit = unit,
            summaries = measurements.summaries(),
            lastMeasured = dates.firstOrNull(),
            previousMeasured = dates.getOrNull(index = 1),
            entryDays = dates.size,
            firstMeasured = dates.lastOrNull()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = MeasurementsViewState()
    )

    fun onUnitSelected(unit: MeasurementUnit) {
        viewModelScope.launch { unitPreference.set(unit = unit) }
    }
}
