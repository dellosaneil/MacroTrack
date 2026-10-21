package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.data.local.entity.toEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MealPlanTest {

    // Thursday, Oct 8, 2026.
    private val today = LocalDate(year = 2026, month = 10, day = 8)
    private val friday = LocalDate(year = 2026, month = 10, day = 9)

    private val yogurt = PlanFood(
        foodName = "Greek yogurt", calories = 190, protein = 20.0, carbs = 8.0, fat = 10.0, weight = 200.0,
        dominantMacro = MacroType.PROTEIN
    )

    private val weekdayBreakfast = RepeatingMeal(
        id = 1,
        name = "Weekday breakfast",
        mealType = MealType.BREAKFAST,
        days = RepeatingMeal.WEEKDAYS,
        foods = listOf(yogurt),
        startDate = friday,
        updatedOn = today
    )

    private fun day(day: Int) = LocalDate(year = 2026, month = 10, day = day)

    @Test
    fun runsOnItsDaysFromTheStartDate() {
        assertFalse(weekdayBreakfast.occursOn(date = today))
        assertTrue(weekdayBreakfast.occursOn(date = friday))
        assertFalse(weekdayBreakfast.occursOn(date = day(10)))
        assertEquals(listOf(friday, day(12), day(13), day(14)), weekdayBreakfast.nextDates(from = today, count = 4))
    }

    @Test
    fun stopsAfterSoManyTimes() {
        val threeTimes = weekdayBreakfast.copy(maxTimes = 3)
        assertEquals(listOf(friday, day(12), day(13)), threeTimes.nextDates(from = today, count = 10))
        assertEquals(3, threeTimes.totalTimes)
        assertTrue(threeTimes.hasEnded(today = day(14)))
    }

    @Test
    fun stopsAfterTheEndDate() {
        val untilTuesday = weekdayBreakfast.copy(endDate = day(13))
        assertEquals(listOf(friday, day(12), day(13)), untilTuesday.nextDates(from = today, count = 10))
        assertEquals(3, untilTuesday.totalTimes)
        assertEquals(null, weekdayBreakfast.totalTimes)
    }

    @Test
    fun aPausedMealHasNoNextDayAndHasNotEnded() {
        val paused = weekdayBreakfast.copy(isActive = false)
        assertEquals(emptyList(), paused.nextDates(from = today, count = 1))
        assertFalse(paused.hasEnded(today = today))
    }

    @Test
    fun fillsTheComingWeekButLeavesFilledAndSkippedDaysAlone() {
        val skipped = PlannedFood(
            id = 5, date = day(12), mealType = MealType.BREAKFAST, food = yogurt, repeatingMealId = 1, status = PlanStatus.SKIPPED
        )
        val otherMeal = skipped.copy(id = 6, date = day(13), repeatingMealId = 2, status = PlanStatus.PENDING)
        assertEquals(
            listOf(friday, day(13), day(14)),
            weekdayBreakfast.datesToFill(existing = listOf(skipped, otherMeal), today = today)
        )
        assertEquals(emptyList(), weekdayBreakfast.copy(isActive = false).datesToFill(existing = emptyList(), today = today))
    }

    @Test
    fun planningNeverReachesBackButAutoLogCatchesUpSinceItWasLastChanged() {
        val everyDay = weekdayBreakfast.copy(days = RepeatingMeal.EVERY_DAY, startDate = day(1), updatedOn = day(6))
        // Planned only from today on.
        assertEquals(today, everyDay.datesToFill(existing = emptyList(), today = today).first())
        // Logged for the days the app wasn't opened, but not before it was set to log itself.
        assertEquals(day(6), everyDay.copy(isAutoLog = true).datesToFill(existing = emptyList(), today = today).first())
    }

    @Test
    fun repeatingMealsSurviveTheDatabase() {
        val meal = weekdayBreakfast.copy(days = setOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY), maxTimes = 4)
        assertEquals("1,7", meal.toEntity().days)
        assertEquals(meal, meal.toEntity().toDomain())
    }
}
