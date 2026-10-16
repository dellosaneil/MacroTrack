package org.thelazybattley.macrotrack.features.measurements

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.unit_cm
import macrotrack.composeapp.generated.resources.unit_in
import org.jetbrains.compose.resources.StringResource
import org.thelazybattley.macrotrack.domain.model.Measurement
import org.thelazybattley.macrotrack.domain.model.MeasurementType
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.usecase.units.GetUnitSystemUseCase
import org.thelazybattley.macrotrack.domain.usecase.units.SetUnitSystemUseCase
import kotlin.math.abs
import kotlin.math.round

enum class MeasurementUnit(val label: StringResource, private val perCm: Double) {
    CM(label = Res.string.unit_cm, perCm = 1.0),
    INCH(label = Res.string.unit_in, perCm = 1 / 2.54);

    fun fromCm(cm: Double): Double = cm * perCm

    fun toCm(value: Double): Double = value / perCm
}

/**
 * The measurement unit, which follows the app's unit system: inches for imperial, cm for metric.
 * Picking a unit on a measurement screen switches the whole app's units.
 */
class MeasurementUnitPreference(
    getUnitSystemUseCase: GetUnitSystemUseCase,
    private val setUnitSystemUseCase: SetUnitSystemUseCase
) {
    val unit: Flow<MeasurementUnit> = getUnitSystemUseCase().map { unitSystem ->
        if (unitSystem == UnitSystem.IMPERIAL) MeasurementUnit.INCH else MeasurementUnit.CM
    }

    suspend fun set(unit: MeasurementUnit) {
        setUnitSystemUseCase(unitSystem = if (unit == MeasurementUnit.INCH) UnitSystem.IMPERIAL else UnitSystem.METRIC)
    }
}

data class MeasurementSummary(
    val type: MeasurementType,
    val latest: Measurement,
    val previous: Measurement?
) {
    val changeCm: Double? get() = previous?.let { latest.valueCm - it.valueCm }
}

/** Latest entry per type with the entry logged before it. */
fun List<Measurement>.summaries(): Map<MeasurementType, MeasurementSummary> =
    groupBy { it.type }.mapValues { (type, entries) ->
        val sorted = entries.sortedBy { it.date }
        MeasurementSummary(type = type, latest = sorted.last(), previous = sorted.getOrNull(index = sorted.lastIndex - 1))
    }

/** Distinct days something was measured, newest first. */
fun List<Measurement>.measuredDates(): List<LocalDate> = map { it.date }.distinct().sortedDescending()

/** Average of the left and right bicep, using whichever sides have been measured. */
fun Map<MeasurementType, MeasurementSummary>.bicepAverage(): MeasurementSummary? {
    val sides = listOfNotNull(this[MeasurementType.LEFT_BICEP], this[MeasurementType.RIGHT_BICEP])
    if (sides.isEmpty()) return null
    val latest = sides.first().latest.copy(valueCm = sides.map { it.latest.valueCm }.average())
    val previousSides = sides.mapNotNull { it.previous }
    val previous = if (previousSides.size == sides.size) {
        previousSides.first().copy(valueCm = previousSides.map { it.valueCm }.average())
    } else {
        null
    }
    return MeasurementSummary(type = MeasurementType.LEFT_BICEP, latest = latest, previous = previous)
}

fun Double.formatMeasure(): String {
    val hundredths = round(abs(this) * 100).toInt()
    val sign = if (this < 0 && hundredths != 0) "-" else ""
    return "$sign${hundredths / 100}.${(hundredths % 100).toString().padStart(length = 2, padChar = '0')}"
}

/** "↓ 2.00", "↑ 0.50" or "0.00", already converted to the display unit. */
fun Double.formatChange(): String {
    val hundredths = round(abs(this) * 100).toInt()
    if (hundredths == 0) return "0.00"
    return (if (this < 0) "↓ " else "↑ ") + abs(this).formatMeasure()
}
