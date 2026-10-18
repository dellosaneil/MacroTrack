package org.thelazybattley.macrotrack.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoalPaceTest {

    @Test
    fun defaultPaceKeepsTheOld300Kcal() {
        assertEquals(-300, Goal.LOSE_WEIGHT.calorieAdjust(paceKgPerMonth = GoalPace.DEFAULT_KG_PER_MONTH))
        assertEquals(0, Goal.MAINTAIN_WEIGHT.calorieAdjust(paceKgPerMonth = GoalPace.DEFAULT_KG_PER_MONTH))
        assertEquals(300, Goal.GAIN_WEIGHT.calorieAdjust(paceKgPerMonth = GoalPace.DEFAULT_KG_PER_MONTH))
        assertTrue(GoalPace.isRecommended(kgPerMonth = GoalPace.DEFAULT_KG_PER_MONTH))
    }

    @Test
    fun caloriesRoundToTen() {
        assertEquals(250, GoalPace.dailyCalories(kgPerMonth = 1.0))
        assertEquals(510, GoalPace.dailyCalories(kgPerMonth = 2.0))
        assertFalse(GoalPace.isRecommended(kgPerMonth = 1.0))
    }

    @Test
    fun metricStepsByATenthWithinRange() {
        assertEquals(1.3, GoalPace.step(kgPerMonth = 1.2, unitSystem = UnitSystem.METRIC, faster = true), absoluteTolerance = 1e-9)
        assertEquals(1.1, GoalPace.step(kgPerMonth = 1.2, unitSystem = UnitSystem.METRIC, faster = false), absoluteTolerance = 1e-9)
        assertEquals(4.0, GoalPace.step(kgPerMonth = 4.0, unitSystem = UnitSystem.METRIC, faster = true), absoluteTolerance = 1e-9)
        assertFalse(GoalPace.canStep(kgPerMonth = 0.2, unitSystem = UnitSystem.METRIC, faster = false))
        assertTrue(GoalPace.canStep(kgPerMonth = 0.2, unitSystem = UnitSystem.METRIC, faster = true))
    }

    @Test
    fun imperialShowsPoundsAndStepsByTwoTenths() {
        assertEquals(2.6, GoalPace.shown(kgPerMonth = GoalPace.DEFAULT_KG_PER_MONTH, unitSystem = UnitSystem.IMPERIAL))
        val faster = GoalPace.step(kgPerMonth = GoalPace.DEFAULT_KG_PER_MONTH, unitSystem = UnitSystem.IMPERIAL, faster = true)
        assertEquals(2.8, GoalPace.shown(kgPerMonth = faster, unitSystem = UnitSystem.IMPERIAL))
        assertEquals(2.8 * UnitSystem.KG_PER_LB, faster, absoluteTolerance = 1e-9)
    }
}
