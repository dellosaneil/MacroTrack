package org.thelazybattley.macrotrack.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WaterTest {

    @Test
    fun goalFromWeightIs35MlPerKgRoundedTo100() {
        // The design's sample user: 74.2 kg comes to 2,597 ml.
        assertEquals(expected = 2_600, actual = waterGoalFromWeight(weightKg = 74.2))
        assertEquals(expected = 3_500, actual = waterGoalFromWeight(weightKg = 100.0))
    }

    @Test
    fun goalWithoutWeightIsTheDefault() {
        assertEquals(expected = DEFAULT_WATER_GOAL_ML, actual = waterGoalFromWeight(weightKg = null))
    }

    @Test
    fun goalFromWeightStaysWithinLimits() {
        assertEquals(expected = WATER_GOAL_MAX_ML, actual = waterGoalFromWeight(weightKg = 400.0))
        assertEquals(expected = WATER_GOAL_MIN_ML, actual = waterGoalFromWeight(weightKg = 5.0))
    }

    @Test
    fun ownGoalReplacesTheWeightOne() {
        val settings = WaterSettings(isGoalFromWeight = false, customGoalMl = 3_000)
        assertEquals(expected = 3_000, actual = settings.goalMl(weightKg = 74.2))
    }

    @Test
    fun paceIsEvenFromSevenToNine() {
        assertEquals(expected = 0, actual = waterPaceMl(goalMl = 2_800, minutes = 6 * 60))
        assertEquals(expected = 1_400, actual = waterPaceMl(goalMl = 2_800, minutes = 14 * 60))
        assertEquals(expected = 2_800, actual = waterPaceMl(goalMl = 2_800, minutes = 22 * 60))
    }

    @Test
    fun remindersComeEveryFewHoursInsideTheWindow() {
        val settings = WaterSettings(isReminderOn = true, reminderEveryHours = 2, reminderStartMinutes = 9 * 60, reminderEndMinutes = 21 * 60)
        assertEquals(expected = listOf(11, 13, 15, 17, 19, 21).map { it * 60 }, actual = settings.reminderMinutes())
    }

    @Test
    fun noReminderTimesWhenOff() {
        assertTrue(actual = WaterSettings(isReminderOn = false).reminderMinutes().isEmpty())
    }

    @Test
    fun imperialShowsWholeFluidOunces() {
        assertEquals(expected = 8, actual = UnitSystem.IMPERIAL.waterFromMl(ml = 250))
        assertEquals(expected = 17, actual = UnitSystem.IMPERIAL.waterFromMl(ml = 500))
        assertEquals(expected = 250, actual = UnitSystem.METRIC.waterFromMl(ml = 250))
    }
}
