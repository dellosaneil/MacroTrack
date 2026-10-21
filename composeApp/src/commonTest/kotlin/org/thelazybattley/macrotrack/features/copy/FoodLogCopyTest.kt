package org.thelazybattley.macrotrack.features.copy

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.copiedLogs
import org.thelazybattley.macrotrack.domain.model.dummyFoodLog
import org.thelazybattley.macrotrack.domain.model.logsToReplace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FoodLogCopyTest {

    private val today = LocalDate(year = 2026, month = 10, day = 8)
    private val yesterday = LocalDate(year = 2026, month = 10, day = 7)
    private val oct5 = LocalDate(year = 2026, month = 10, day = 5)

    private val yogurt = dummyFoodLog.copy(id = 1, foodName = "Greek yogurt", weight = 200.0, calories = 190, date = yesterday)
    private val oats = dummyFoodLog.copy(id = 2, foodName = "Rolled oats", weight = 60.0, calories = 230, date = yesterday)
    private val lunch = dummyFoodLog.copy(id = 3, foodName = "Rice bowl", mealType = MealType.LUNCH, calories = 570, date = yesterday)
    private val todayLunch = dummyFoodLog.copy(id = 4, foodName = "Soup", mealType = MealType.LUNCH, calories = 300, date = today)

    @Test
    fun copiesKeepPortionsAndLandOnEveryDay() {
        val copies = copiedLogs(logs = listOf(yogurt, oats), targetDates = listOf(today, oct5), mealType = MealType.SNACK)
        assertEquals(4, copies.size)
        assertTrue(copies.all { it.mealType == MealType.SNACK && it.id == 0L })
        assertEquals(listOf(200.0, 60.0, 200.0, 60.0), copies.map { it.weight })
        assertEquals(listOf(today, today, oct5, oct5), copies.map { it.date })
    }

    @Test
    fun aWholeDayKeepsEachLogsMeal() {
        val copies = copiedLogs(logs = listOf(yogurt, lunch), targetDates = listOf(today), mealType = null)
        assertEquals(listOf(MealType.BREAKFAST, MealType.LUNCH), copies.map { it.mealType })
    }

    @Test
    fun replaceTakesOnlyTheTargetMealOrTheWholeDay() {
        val all = listOf(yogurt, oats, lunch, todayLunch)
        assertEquals(listOf(todayLunch), logsToReplace(existing = all, targetDates = listOf(today), mealType = MealType.LUNCH))
        assertEquals(emptyList(), logsToReplace(existing = all, targetDates = listOf(today), mealType = MealType.DINNER))
        assertEquals(listOf(yogurt, oats, lunch), logsToReplace(existing = all, targetDates = listOf(yesterday), mealType = null))
    }

    @Test
    fun theDayStripLeavesOutTheSourceAndFutureDays() {
        val state = CopyToViewState(sourceDate = yesterday, mealType = MealType.BREAKFAST, today = today, targetDates = setOf(today))
        assertEquals(today, state.stripDates.first())
        assertFalse(yesterday in state.stripDates)
        assertTrue(state.stripDates.all { it <= today })
    }

    @Test
    fun aConflictNeedsFoodWhereTheCopiesGo() {
        val state = CopyToViewState(
            sourceDate = yesterday,
            mealType = MealType.BREAKFAST,
            today = today,
            sourceLogs = listOf(yogurt, oats),
            targetDates = setOf(today),
            allLogs = listOf(yogurt, oats, lunch, todayLunch)
        )
        assertFalse(state.hasConflict)
        assertTrue(state.copy(targetMeal = MealType.LUNCH).hasConflict)
        assertFalse(state.copy(targetMeal = MealType.LUNCH, unchecked = setOf(1, 2)).hasConflict)
        assertEquals(420, state.selectedCalories)
    }

    @Test
    fun pastMealsShowTheSameMealFirstFromTheLastWeek() {
        val old = dummyFoodLog.copy(id = 9, date = LocalDate(year = 2026, month = 9, day = 30))
        val state = CopyFromViewState(mealType = MealType.LUNCH, targetDate = today, allLogs = listOf(yogurt, oats, lunch, todayLunch, old))
        assertEquals(listOf(MealType.LUNCH), state.pastMeals.map { it.mealType })
        val any = state.copy(isAnyMeal = true).pastMeals
        assertEquals(listOf(MealType.LUNCH, MealType.BREAKFAST), any.map { it.mealType })
        assertEquals(420, any[1].calories)
        assertEquals(any[0], state.copy(isAnyMeal = true).selected)
    }
}
