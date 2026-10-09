package org.thelazybattley.macrotrack.features.measurements

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.Measurement
import org.thelazybattley.macrotrack.domain.model.MeasurementType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MeasurementSharedTest {

    private val sep6 = LocalDate(2026, 9, 6)
    private val oct4 = LocalDate(2026, 10, 4)

    private val measurements = listOf(
        Measurement(date = oct4, type = MeasurementType.WAIST, valueCm = 84.5),
        Measurement(date = sep6, type = MeasurementType.WAIST, valueCm = 86.5),
        Measurement(date = sep6, type = MeasurementType.LEFT_BICEP, valueCm = 35.0),
        Measurement(date = sep6, type = MeasurementType.RIGHT_BICEP, valueCm = 35.5),
        Measurement(date = oct4, type = MeasurementType.LEFT_BICEP, valueCm = 35.5),
        Measurement(date = oct4, type = MeasurementType.RIGHT_BICEP, valueCm = 36.0),
        Measurement(date = oct4, type = MeasurementType.CALVES, valueCm = 38.0)
    )

    @Test
    fun summariesPairLatestWithPrevious() {
        val waist = measurements.summaries().getValue(MeasurementType.WAIST)
        assertEquals(oct4, waist.latest.date)
        assertEquals(sep6, waist.previous?.date)
        assertEquals(-2.0, waist.changeCm!!, absoluteTolerance = 0.0001)

        val calves = measurements.summaries().getValue(MeasurementType.CALVES)
        assertNull(calves.previous)
        assertNull(calves.changeCm)
    }

    @Test
    fun bicepAverageUsesBothSides() {
        val average = measurements.summaries().bicepAverage()!!
        assertEquals(35.75, average.latest.valueCm, absoluteTolerance = 0.0001)
        assertEquals(0.5, average.changeCm!!, absoluteTolerance = 0.0001)
    }

    @Test
    fun measuredDatesAreDistinctNewestFirst() {
        assertEquals(listOf(oct4, sep6), measurements.measuredDates())
    }

    @Test
    fun unitsConvertBothWays() {
        assertEquals(10.0, MeasurementUnit.INCH.fromCm(cm = 25.4), absoluteTolerance = 0.0001)
        assertEquals(25.4, MeasurementUnit.INCH.toCm(value = 10.0), absoluteTolerance = 0.0001)
        assertEquals(84.5, MeasurementUnit.CM.fromCm(cm = 84.5))
    }

    @Test
    fun formattingUsesTwoDecimals() {
        assertEquals("84.50", 84.5.formatMeasure())
        assertEquals("↓ 2.00", (-2.0).formatChange())
        assertEquals("↑ 0.50", 0.5.formatChange())
        assertEquals("0.00", 0.001.formatChange())
    }
}
