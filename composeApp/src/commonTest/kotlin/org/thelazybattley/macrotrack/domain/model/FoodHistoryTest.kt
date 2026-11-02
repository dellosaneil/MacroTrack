package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FoodHistoryTest {

    private val today = LocalDate(year = 2026, month = 10, day = 8)

    private fun log(
        id: Long,
        date: LocalDate,
        weight: Double,
        mealType: MealType = MealType.DINNER,
        name: String = "Chicken breast",
        calories: Int = (weight * 1.65).toInt()
    ) = FoodLog(
        mealType = mealType,
        calories = calories,
        protein = weight * 0.31,
        carbs = 0.0,
        fat = 0.0,
        id = id,
        foodName = name,
        weight = weight,
        dominantMacro = MacroType.PROTEIN,
        date = date
    )

    private val logs = listOf(
        log(id = 1, date = LocalDate(year = 2026, month = 8, day = 17), weight = 150.0),
        log(id = 2, date = LocalDate(year = 2026, month = 9, day = 18), weight = 120.0, mealType = MealType.LUNCH),
        log(id = 3, date = LocalDate(year = 2026, month = 9, day = 29), weight = 150.0),
        log(id = 4, date = LocalDate(year = 2026, month = 10, day = 5), weight = 180.0),
        log(id = 5, date = LocalDate(year = 2026, month = 10, day = 6), weight = 100.0, name = "Rice")
    )

    @Test
    fun entriesAreThatFoodOnlyNewestFirst() {
        val entries = foodHistoryEntries(logs = logs, name = "Chicken breast", isRecipe = false, fullRecipeCalories = null)
        assertEquals(expected = listOf(4L, 3L, 2L, 1L), actual = entries.map { it.log.id })
        assertEquals(expected = 180.0, actual = entries.first().amount)
    }

    @Test
    fun aRecipeAmountIsItsShareOfAServing() {
        val recipeLogs = listOf(log(id = 1, date = today, weight = 0.0, name = "Salmon bowl", calories = 300))
        val entries = foodHistoryEntries(logs = recipeLogs, name = "Salmon bowl", isRecipe = true, fullRecipeCalories = 600)
        assertEquals(expected = 50.0, actual = entries.single().amount)
    }

    @Test
    fun thePeriodKeepsOnlyItsDays() {
        val entries = foodHistoryEntries(logs = logs, name = "Chicken breast", isRecipe = false, fullRecipeCalories = null)
        assertEquals(expected = 3, actual = entries.within(period = FoodHistoryPeriod.DAYS_30, today = today).size)
        assertEquals(expected = 4, actual = entries.within(period = FoodHistoryPeriod.DAYS_60, today = today).size)
    }

    @Test
    fun statsGiveTheUsualAverageRangeAndLast() {
        val stats = foodHistoryEntries(logs = logs, name = "Chicken breast", isRecipe = false, fullRecipeCalories = null).stats()!!
        assertEquals(expected = 4, actual = stats.times)
        assertEquals(expected = 150.0, actual = stats.usual)
        assertEquals(expected = 2, actual = stats.usualCount)
        assertEquals(expected = 150.0, actual = stats.average)
        assertEquals(expected = 120.0, actual = stats.min)
        assertEquals(expected = 180.0, actual = stats.max)
        assertEquals(expected = 4L, actual = stats.last.log.id)
        assertNull(actual = emptyList<FoodHistoryEntry>().stats())
    }

    @Test
    fun mostlyAtNeedsMoreThanHalf() {
        val entries = foodHistoryEntries(logs = logs, name = "Chicken breast", isRecipe = false, fullRecipeCalories = null)
        assertEquals(expected = MealType.DINNER, actual = entries.mostlyAt())
        // One dinner and one lunch: neither is more than half.
        assertNull(actual = entries.filter { it.log.id in setOf(2L, 3L) }.mostlyAt())
    }

    @Test
    fun copiesRememberWhereTheyCameFrom() {
        val source = log(id = 9, date = LocalDate(year = 2026, month = 9, day = 22), weight = 150.0).copy(editedFrom = 120.0)
        val copy = copiedLogs(logs = listOf(source), targetDates = listOf(today), mealType = null).single()
        assertEquals(expected = FoodLogSource.COPIED, actual = copy.source)
        assertEquals(expected = LocalDate(year = 2026, month = 9, day = 22), actual = copy.copiedFrom)
        assertNull(actual = copy.editedFrom)
    }
}
