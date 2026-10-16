package org.thelazybattley.macrotrack.features.createfood

import org.thelazybattley.macrotrack.domain.model.UnitSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateFoodViewStateTest {

    @Test
    fun imperialOuncesAreSavedAsGrams() {
        val (weight, unit) = CreateFoodViewState(weight = 3.5, unit = "oz", unitSystem = UnitSystem.IMPERIAL).servingToSave
        assertEquals(99.22, weight, absoluteTolerance = 0.01)
        assertEquals("g", unit)
        val (millilitres, liquidUnit) = CreateFoodViewState(weight = 8.0, unit = "fl oz", unitSystem = UnitSystem.IMPERIAL).servingToSave
        assertEquals(236.59, millilitres, absoluteTolerance = 0.01)
        assertEquals("ml", liquidUnit)
    }

    @Test
    fun untouchedConvertedServingKeepsTheStoredOne() {
        val state = CreateFoodViewState(
            weight = 3.5,
            unit = "oz",
            unitSystem = UnitSystem.IMPERIAL,
            storedServing = 100.0 to "g",
            shownServing = 3.5 to "oz"
        )
        assertEquals(100.0 to "g", state.servingToSave)
        assertEquals(113.4, state.copy(weight = 4.0).servingToSave.first, absoluteTolerance = 0.01)
    }

    @Test
    fun metricAndOtherUnitsSaveAsTyped() {
        assertEquals(1.0 to "oz", CreateFoodViewState(weight = 1.0, unit = "oz").servingToSave)
        assertEquals(2.0 to "slice", CreateFoodViewState(weight = 2.0, unit = "slice", unitSystem = UnitSystem.IMPERIAL).servingToSave)
    }
}
