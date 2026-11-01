package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomeWidgetTest {

    // A Thursday.
    private val today = LocalDate(year = 2026, monthNumber = 10, dayOfMonth = 8)

    private fun log(id: Long, mealType: MealType, calories: Int, date: LocalDate = today, name: String = "Food $id") = FoodLog(
        mealType = mealType,
        calories = calories,
        protein = 10.0,
        carbs = 20.0,
        fat = 5.0,
        id = id,
        foodName = name,
        weight = 100.0,
        dominantMacro = MacroType.CARBS,
        date = date,
        loggedAtMillis = id
    )

    private fun water(totalMl: Int) = WaterSummary(
        date = today,
        entries = listOf(WaterEntry(date = today, amountMl = totalMl, kind = WaterKind.OTHER, loggedAtMillis = 0)),
        goalMl = 2_600,
        autoGoalMl = 2_600,
        weightKg = 74.2,
        settings = WaterSettings(glassMl = 300),
        days = emptyList()
    )

    private fun snapshot(
        foodLogs: List<FoodLog> = emptyList(),
        macroGoals: MacroGoals? = MacroGoals(calories = 2_150, protein = 160, carbs = 220, fat = 70),
        weights: List<Weight> = emptyList()
    ) = widgetSnapshot(
        today = today,
        foodLogs = foodLogs,
        macroGoals = macroGoals,
        water = water(totalMl = 1_250),
        weights = weights,
        unitSystem = UnitSystem.METRIC,
        isWeightShown = true
    )

    @Test
    fun countsOnlyTodaysFoodAgainstTodaysGoal() {
        val result = snapshot(
            foodLogs = listOf(
                log(id = 1, mealType = MealType.BREAKFAST, calories = 460),
                log(id = 2, mealType = MealType.LUNCH, calories = 640),
                log(id = 3, mealType = MealType.DINNER, calories = 900, date = LocalDate(year = 2026, monthNumber = 10, dayOfMonth = 7))
            )
        )
        assertEquals(expected = WidgetAmount(eaten = 1_100, goal = 2_150), actual = result.calories)
        assertEquals(expected = 1_050, actual = result.calories.left)
        assertEquals(expected = WidgetAmount(eaten = 20, goal = 160), actual = result.protein)
    }

    @Test
    fun goingOverLeavesANegativeLeftAndAFullRing() {
        val result = snapshot(foodLogs = listOf(log(id = 1, mealType = MealType.DINNER, calories = 2_400)))
        assertEquals(expected = -250, actual = result.calories.left)
        assertEquals(expected = 1f, actual = result.calories.progress)
    }

    @Test
    fun notSetUpWithoutGoals() {
        val result = snapshot(macroGoals = null)
        assertFalse(actual = result.isSetUp)
        assertEquals(expected = 0f, actual = result.calories.progress)
    }

    @Test
    fun mealsListEveryMealInOrderWithFoodsAsLogged() {
        val result = snapshot(
            foodLogs = listOf(
                log(id = 2, mealType = MealType.LUNCH, calories = 140, name = "Iced latte"),
                log(id = 1, mealType = MealType.LUNCH, calories = 500, name = "Chicken rice bowl")
            )
        )
        assertEquals(expected = MealType.entries, actual = result.meals.map { it.mealType })
        val lunch = result.meals.first { it.mealType == MealType.LUNCH }
        assertEquals(expected = listOf("Chicken rice bowl", "Iced latte"), actual = lunch.foodNames)
        assertEquals(expected = 640, actual = lunch.calories)
        assertFalse(actual = result.meals.first { it.mealType == MealType.DINNER }.isLogged)
    }

    @Test
    fun waterUsesTheGlassSizeFromSettings() {
        assertEquals(expected = WidgetWater(totalMl = 1_250, goalMl = 2_600, glassMl = 300), actual = snapshot().water)
    }

    @Test
    fun noWeightTrendWithoutARecentWeighIn() {
        val longAgo = listOf(Weight(date = LocalDate(year = 2026, monthNumber = 9, dayOfMonth = 1), weight = 80.0))
        assertNull(actual = snapshot(weights = longAgo).weight)
    }

    @Test
    fun weightTrendComparesWithAWeekBefore() {
        val weights = (0..13).map { daysAgo ->
            Weight(date = LocalDate.fromEpochDays(today.toEpochDays() - daysAgo), weight = 74.0 + daysAgo * 0.1)
        }
        val weight = snapshot(weights = weights).weight!!
        // Today's 7-day average covers 0–6 days ago: 74.3. A week before covers 7–13: 75.0.
        assertEquals(expected = 74.3, actual = weight.trendKg, absoluteTolerance = 0.001)
        assertEquals(expected = -0.7, actual = weight.weekChangeKg!!, absoluteTolerance = 0.001)
        assertEquals(expected = WIDGET_TREND_DAYS, actual = weight.line.size)
        assertTrue(actual = weight.line.first() > weight.line.last())
    }

    @Test
    fun streakWeekStartsMondayAndLeavesTodayOpenUntilLogged() {
        // Oct 5 is the Monday; logged Monday to Wednesday, nothing yet on Thursday the 8th.
        val logs = listOf(5, 6, 7).map { day ->
            log(id = day.toLong(), mealType = MealType.LUNCH, calories = 500, date = LocalDate(year = 2026, monthNumber = 10, dayOfMonth = day))
        }
        val streak = snapshot(foodLogs = logs).streak
        assertEquals(expected = 3, actual = streak.days)
        assertFalse(actual = streak.isTodayLogged)
        assertEquals(expected = DayOfWeek.MONDAY, actual = streak.week.first().dayOfWeek)
        assertEquals(
            expected = List(size = 3) { StreakDayState.LOGGED } + StreakDayState.OPEN + List(size = 3) { StreakDayState.LATER },
            actual = streak.week.map { it.state }
        )
    }

    @Test
    fun aGapThisWeekShowsAsMissed() {
        val monday = log(id = 1, mealType = MealType.LUNCH, calories = 500, date = LocalDate(year = 2026, monthNumber = 10, dayOfMonth = 5))
        val streak = snapshot(foodLogs = listOf(monday, log(id = 2, mealType = MealType.LUNCH, calories = 500))).streak
        assertEquals(expected = 1, actual = streak.days)
        assertTrue(actual = streak.isTodayLogged)
        assertEquals(
            expected = listOf(StreakDayState.LOGGED, StreakDayState.MISSED, StreakDayState.MISSED, StreakDayState.LOGGED),
            actual = streak.week.take(n = 4).map { it.state }
        )
    }
}
