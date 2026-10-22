package org.thelazybattley.macrotrack.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StepGoalTest {

    // The design's last 14 days, Sep 24 – Oct 7.
    private val lastTwoWeeks = listOf(9_800, 6_400, 7_200, 10_900, 8_800, 5_200, 7_600, 9_900, 8_410, 4_900, 11_230, 9_120, 6_300, 10_450)

    @Test
    fun suggestsTheAverageRoundedUpToTheNext500() {
        assertEquals(8_500, StepGoal.suggested(dailySteps = lastTwoWeeks))
        assertEquals(7, StepGoal.daysHit(dailySteps = lastTwoWeeks, goal = 8_500))
    }

    @Test
    fun daysWithNothingCountedDontPullTheSuggestionDown() {
        assertEquals(8_500, StepGoal.suggested(dailySteps = lastTwoWeeks + listOf(0, 0, 0)))
        assertNull(StepGoal.suggested(dailySteps = listOf(0, 0)))
        assertEquals(StepGoal.MIN, StepGoal.suggested(dailySteps = listOf(300)))
    }

    @Test
    fun plusAndMinusSnapTo500AndStayInRange() {
        assertEquals(7_500, StepGoal.stepDown(goal = 8_000))
        assertEquals(8_000, StepGoal.stepDown(goal = 8_250))
        assertEquals(8_500, StepGoal.stepUp(goal = 8_250))
        assertEquals(StepGoal.MIN, StepGoal.stepDown(goal = StepGoal.MIN))
        assertEquals(StepGoal.MAX, StepGoal.stepUp(goal = StepGoal.MAX))
    }

    @Test
    fun goalsMustBeBetween1000And50000() {
        assertTrue(StepGoal.isValid(goal = 1_000))
        assertTrue(StepGoal.isValid(goal = 50_000))
        assertFalse(StepGoal.isValid(goal = 999))
        assertFalse(StepGoal.isValid(goal = 50_001))
    }
}
