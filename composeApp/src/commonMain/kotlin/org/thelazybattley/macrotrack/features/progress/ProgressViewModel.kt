package org.thelazybattley.macrotrack.features.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.MeasurementType
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.usecase.measurement.GetAllMeasurementsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.InsertWeightUseCase
import org.thelazybattley.macrotrack.features.measurements.MeasurementSummary
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnit
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnitPreference
import org.thelazybattley.macrotrack.features.measurements.bicepAverage
import org.thelazybattley.macrotrack.features.measurements.measuredDates
import org.thelazybattley.macrotrack.features.measurements.summaries
import org.thelazybattley.macrotrack.features.profile.weighthistory.inPeriod
import org.thelazybattley.macrotrack.features.profile.weighthistory.periodContaining
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryTimeRangeEnum

private const val SPARKLINE_DAYS = 90

data class ProgressViewState(
    val userDetails: UserDetails? = null,
    val latestWeight: Weight? = null,
    val monthChange: Double? = null,
    val monthAverage: Double? = null,
    val sparkline: List<Weight> = emptyList(),
    val unit: MeasurementUnit = MeasurementUnit.CM,
    val lastMeasured: LocalDate? = null,
    val previousMeasured: LocalDate? = null,
    val highlights: List<Pair<MeasurementType, MeasurementSummary>> = emptyList(),
    val measurementCount: Int = 0
)

class ProgressViewModel(
    getAllWeightUseCase: GetAllWeightUseCase,
    getUserDetailsUseCase: GetUserDetailsUseCase,
    getAllMeasurementsUseCase: GetAllMeasurementsUseCase,
    unitPreference: MeasurementUnitPreference,
    private val insertWeightUseCase: InsertWeightUseCase,
    private val insertUserDetailsUseCase: InsertUserDetailsUseCase
) : ViewModel() {

    val state = combine(
        flow = getAllWeightUseCase(),
        flow2 = flow { emitAll(getUserDetailsUseCase()) },
        flow3 = getAllMeasurementsUseCase(),
        flow4 = unitPreference.unit
    ) { weights, userDetails, measurements, unit ->
        val today = getCurrentDate()
        val sorted = weights.sortedBy { it.date }
        val month = sorted.inPeriod(
            period = periodContaining(
                range = WeightHistoryTimeRangeEnum.MONTH,
                date = today,
                earliestEntry = sorted.firstOrNull()?.date
            )
        )
        val summaries = measurements.summaries()
        val dates = measurements.measuredDates()
        ProgressViewState(
            userDetails = userDetails,
            latestWeight = sorted.lastOrNull(),
            monthChange = month.takeIf { it.size > 1 }?.let { it.last().weight - it.first().weight },
            monthAverage = month.takeIf { it.isNotEmpty() }?.map { it.weight }?.average(),
            sparkline = sorted.filter { it.date > today.minus(period = DatePeriod(days = SPARKLINE_DAYS)) },
            unit = unit,
            lastMeasured = dates.firstOrNull(),
            previousMeasured = dates.getOrNull(index = 1),
            highlights = listOfNotNull(
                summaries[MeasurementType.WAIST]?.let { MeasurementType.WAIST to it },
                summaries[MeasurementType.CHEST]?.let { MeasurementType.CHEST to it },
                summaries.bicepAverage()?.let { MeasurementType.LEFT_BICEP to it }
            ),
            measurementCount = summaries.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = ProgressViewState()
    )

    fun onSaveWeight(weight: String) {
        val newWeight = weight.toDoubleOrNull()?.takeIf { it > 0 } ?: return
        val userDetails = state.value.userDetails
        viewModelScope.launch {
            insertWeightUseCase(weight = newWeight)
            if (userDetails != null) {
                insertUserDetailsUseCase(userDetails = userDetails.copy(weight = newWeight))
            }
        }
    }
}
