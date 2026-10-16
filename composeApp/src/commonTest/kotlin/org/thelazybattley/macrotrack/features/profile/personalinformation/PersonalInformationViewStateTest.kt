package org.thelazybattley.macrotrack.features.profile.personalinformation

import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.UserSex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PersonalInformationViewStateTest {

    private val userDetails = UserDetails(
        weight = 74.2,
        age = 29,
        height = 175.0,
        sex = UserSex.MALE,
        activityLevel = ActivityLevel.MODERATELY_ACTIVE,
        goal = Goal.LOSE_WEIGHT
    )

    private fun loaded(unitSystem: UnitSystem) = PersonalInformationViewState(
        userDetails = userDetails,
        unitSystem = unitSystem,
        age = "29",
        sex = userDetails.sex,
        activityLevel = userDetails.activityLevel
    ).withHeightFields(cm = userDetails.height)

    @Test
    fun heightFieldsShowBothUnits() {
        val state = loaded(unitSystem = UnitSystem.IMPERIAL)
        assertEquals("175", state.height)
        assertEquals("5", state.heightFeet)
        assertEquals("8.9", state.heightInches)
    }

    @Test
    fun feetAndInchesRoundingIsNotAChange() {
        val state = loaded(unitSystem = UnitSystem.IMPERIAL)
        assertFalse(state.isHeightChanged)
        assertFalse(state.canSave)
    }

    @Test
    fun editedInchesAreAChange() {
        val state = loaded(unitSystem = UnitSystem.IMPERIAL).copy(heightInches = "9.5")
        assertTrue(state.isHeightChanged)
        assertEquals(176.53, state.parsedHeight!!, absoluteTolerance = 0.001)
        assertTrue(state.canSave)
    }

    @Test
    fun invalidImperialHeightHasNoValue() {
        assertNull(loaded(unitSystem = UnitSystem.IMPERIAL).copy(heightFeet = "").parsedHeight)
        assertNull(loaded(unitSystem = UnitSystem.IMPERIAL).copy(heightInches = "12").parsedHeight)
        // Blank inches mean a whole number of feet.
        assertEquals(152.4, loaded(unitSystem = UnitSystem.IMPERIAL).copy(heightInches = "").parsedHeight!!, absoluteTolerance = 0.001)
    }
}
