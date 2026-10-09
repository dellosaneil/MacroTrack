package org.thelazybattley.macrotrack.features.insights

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InsightsStatsTest {

    private val today = LocalDate(2026, 10, 9)
    private val maintain = MacroGoals(calories = 2650, protein = 148, carbs = 397, fat = 52)
    private val deficit = MacroGoals(calories = 2350, protein = 148, carbs = 322, fat = 52)
    private val dailyGoals = listOf(
        DailyGoal(date = LocalDate(2026, 9, 1), goal = Goal.MAINTAIN_WEIGHT, macroGoals = maintain),
        DailyGoal(date = LocalDate(2026, 10, 5), goal = Goal.LOSE_WEIGHT, macroGoals = deficit)
    )

    private var nextId = 1L
    private fun log(date: LocalDate, calories: Int, name: String = "Food", meal: MealType = MealType.LUNCH, protein: Double = 30.0) =
        FoodLog(
            mealType = meal, calories = calories, protein = protein, carbs = 0.0, fat = 0.0,
            id = nextId++, foodName = name, weight = 100.0, dominantMacro = MacroType.PROTEIN, date = date
        )

    private fun insights(range: InsightsRange, logs: List<FoodLog>) = calculateInsights(
        range = range,
        today = today,
        foodLogs = logs,
        dailyGoals = dailyGoals,
        currentGoal = Goal.LOSE_WEIGHT,
        currentMacroGoals = deficit
    )

    @Test
    fun weekBarsUseEachDaysOwnGoal() {
        val result = insights(
            InsightsRange.WEEK,
            listOf(log(LocalDate(2026, 10, 4), 2600), log(LocalDate(2026, 10, 6), 2300))
        )
        assertEquals(7, result.bars.size)
        assertEquals(LocalDate(2026, 10, 3), result.bars.first().start)
        val oct4 = result.bars.first { it.start == LocalDate(2026, 10, 4) }
        assertEquals(2650, oct4.goal)
        assertTrue(oct4.isOnTarget)
        val oct6 = result.bars.first { it.start == LocalDate(2026, 10, 6) }
        assertEquals(2350, oct6.goal)
        assertNull(result.bars.first { it.start == LocalDate(2026, 10, 5) }.eaten)
        assertEquals(2, result.loggedDays)
        assertEquals(2, result.daysOnTarget)
        assertEquals(2450, result.averageCalories)
    }

    @Test
    fun monthGroupsIntoFourWeeks() {
        val result = insights(InsightsRange.MONTH, listOf(log(today, 2000), log(today, 400)))
        assertEquals(4, result.bars.size)
        assertEquals(2400, result.bars.last().eaten)
    }

    @Test
    fun macroAveragesAndMealSharesAndMostLogged() {
        val result = insights(
            InsightsRange.WEEK,
            listOf(
                log(today, 600, name = "Oats", meal = MealType.BREAKFAST, protein = 20.0),
                log(today, 1400, name = "Chicken", meal = MealType.DINNER, protein = 100.0),
                log(LocalDate(2026, 10, 8), 1000, name = "Chicken", meal = MealType.DINNER, protein = 60.0)
            )
        )
        assertEquals(90, result.macros.first { it.macroType == MacroType.PROTEIN }.average)
        assertEquals(148, result.macros.first { it.macroType == MacroType.PROTEIN }.target)
        assertEquals(20, result.mealShares[MealType.BREAKFAST])
        assertEquals(80, result.mealShares[MealType.DINNER])
        assertEquals("Chicken", result.mostLogged.first().name)
        assertEquals(2, result.mostLogged.first().count)
        assertEquals(200.0, result.mostLogged.first().totalAmount)
    }
}
