package org.thelazybattley.macrotrack.features.measurements.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.all
import macrotrack.composeapp.generated.resources.change_since_you_started
import macrotrack.composeapp.generated.resources.six_month_change
import macrotrack.composeapp.generated.resources.six_months
import macrotrack.composeapp.generated.resources.three_month_change
import macrotrack.composeapp.generated.resources.three_months
import org.jetbrains.compose.resources.StringResource
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.Measurement
import org.thelazybattley.macrotrack.domain.model.MeasurementType
import org.thelazybattley.macrotrack.domain.usecase.measurement.GetAllMeasurementsUseCase
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnit
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnitPreference
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

enum class MeasurementRange(val label: StringResource, val changeLabel: StringResource, val months: Int?) {
    THREE_MONTHS(label = Res.string.three_months, changeLabel = Res.string.three_month_change, months = 3),
    SIX_MONTHS(label = Res.string.six_months, changeLabel = Res.string.six_month_change, months = 6),
    ALL(label = Res.string.all, changeLabel = Res.string.change_since_you_started, months = null)
}

data class MeasurementDetailViewState(
    val type: MeasurementType = MeasurementType.WAIST,
    val unit: MeasurementUnit = MeasurementUnit.CM,
    val range: MeasurementRange = MeasurementRange.ALL,
    val start: LocalDate = getCurrentDate(),
    val end: LocalDate = getCurrentDate(),
    val entries: List<Measurement> = emptyList(),
    val allEntries: List<Measurement> = emptyList()
)

class MeasurementDetailViewModel(
    getAllMeasurementsUseCase: GetAllMeasurementsUseCase,
    unitPreference: MeasurementUnitPreference,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val type = MeasurementType.entries.find {
        it.name == savedStateHandle.get<String>(AppDestinations.MEASUREMENT_TYPE)
    } ?: MeasurementType.WAIST

    private val range = MutableStateFlow(value = MeasurementRange.ALL)

    val state = combine(
        flow = getAllMeasurementsUseCase(),
        flow2 = unitPreference.unit,
        flow3 = range
    ) { measurements, unit, selectedRange ->
        val today = getCurrentDate()
        val ofType = measurements.filter { it.type == type }.sortedBy { it.date }
        val start = selectedRange.months?.let { today.minus(period = DatePeriod(months = it)) }
            ?: ofType.firstOrNull()?.date
            ?: today
        MeasurementDetailViewState(
            type = type,
            unit = unit,
            range = selectedRange,
            start = start,
            end = today,
            entries = ofType.filter { it.date in start..today },
            allEntries = ofType
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = MeasurementDetailViewState(type = type)
    )

    fun onRangeSelected(range: MeasurementRange) {
        this.range.value = range
    }
}
