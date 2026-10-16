package org.thelazybattley.macrotrack.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class UnitSystemTest {

    @Test
    fun imperialConvertsPoundsAndInches() {
        assertEquals(165.3466, UnitSystem.IMPERIAL.weightFromKg(kg = 75.0), absoluteTolerance = 0.0001)
        assertEquals(75.0, UnitSystem.IMPERIAL.weightToKg(value = UnitSystem.IMPERIAL.weightFromKg(kg = 75.0)), absoluteTolerance = 1e-9)
        assertEquals(15.0, UnitSystem.IMPERIAL.lengthFromCm(cm = 38.1), absoluteTolerance = 1e-9)
        assertEquals(38.1, UnitSystem.IMPERIAL.lengthToCm(value = 15.0), absoluteTolerance = 1e-9)
    }

    @Test
    fun metricLeavesValuesAsTheyAre() {
        assertEquals(75.0, UnitSystem.METRIC.weightFromKg(kg = 75.0))
        assertEquals(175.0, UnitSystem.METRIC.lengthToCm(value = 175.0))
    }

    @Test
    fun defaultFollowsRegion() {
        assertEquals(UnitSystem.IMPERIAL, UnitSystem.defaultFor(region = "US"))
        assertEquals(UnitSystem.IMPERIAL, UnitSystem.defaultFor(region = "us"))
        assertEquals(UnitSystem.METRIC, UnitSystem.defaultFor(region = "PH"))
        assertEquals(UnitSystem.METRIC, UnitSystem.defaultFor(region = ""))
    }

    @Test
    fun heightSplitsIntoFeetAndInches() {
        assertEquals(FeetInches(feet = 5, inches = 8.9), FeetInches.fromCm(cm = 175.0))
        assertEquals(FeetInches(feet = 6, inches = 0.0), FeetInches.fromCm(cm = 182.88))
        // 11.96 inches rounds up into the next foot rather than showing "5 ft 12 in".
        assertEquals(FeetInches(feet = 6, inches = 0.0), FeetInches.fromCm(cm = 182.8))
        assertEquals(175.006, FeetInches(feet = 5, inches = 8.9).cm, absoluteTolerance = 0.001)
    }

    @Test
    fun imperialShowsGramsAsOuncesAndMlAsFluidOunces() {
        val ounces = UnitSystem.IMPERIAL.portionUnit(storedUnit = "g")
        assertEquals("oz", ounces.label)
        assertEquals("7.1", ounces.format(amount = 200.0))
        assertEquals(28.349523125, ounces.toStored(value = 1.0))
        assertEquals("8.5", UnitSystem.IMPERIAL.portionUnit(storedUnit = "ML").format(amount = 250.0))
        assertEquals("fl oz", UnitSystem.IMPERIAL.portionUnit(storedUnit = "ml").label)
        // Other units, and everything in metric, stay as they are.
        assertEquals(PortionUnit(label = "slice"), UnitSystem.IMPERIAL.portionUnit(storedUnit = "slice"))
        assertEquals("0.25", UnitSystem.METRIC.portionUnit(storedUnit = "g").format(amount = 0.25))
    }
}
