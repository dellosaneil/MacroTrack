package org.thelazybattley.macrotrack.features.foodlog

import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.dummyFoodLog
import org.thelazybattley.macrotrack.features.addmeal.recipePercentEaten
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EntryEditTest {

    private val yogurt = dummyFoodLog.copy(
        foodName = "Greek yogurt", calories = 190, protein = 20.0, carbs = 8.0, fat = 10.0, weight = 200.0,
        mealType = MealType.BREAKFAST
    )
    private val bowl = dummyFoodLog.copy(
        foodName = "Chicken rice bowl", calories = 620, protein = 45.0, carbs = 70.0, fat = 14.0, weight = 0.0,
        mealType = MealType.LUNCH
    )

    @Test
    fun scalesMacrosFromTheLoggedPortion() {
        val edit = EntryEdit(log = yogurt, originalAmount = 200.0, unit = "g").copy(amount = 150.0)
        assertEquals(143, edit.calories)
        assertEquals(15.0, edit.protein)
        assertEquals(-47, edit.caloriesChange)
        assertEquals(150.0, edit.weight)
        assertTrue(edit.canSave)
    }

    @Test
    fun recipeKeepsZeroWeightAndScalesByPercentage() {
        val edit = EntryEdit(log = bowl, originalAmount = 100.0, unit = "%").copy(amount = 75.0)
        assertTrue(edit.isRecipe)
        assertEquals(465, edit.calories)
        assertEquals(0.0, edit.weight)
        assertEquals(listOf(25.0, 50.0, 75.0, 100.0), edit.presets)
        assertEquals(25.0, edit.step)
    }

    @Test
    fun movingMealAloneIsAChange() {
        val edit = EntryEdit(log = yogurt, originalAmount = 200.0, unit = "g")
        assertFalse(edit.hasChanges)
        val moved = edit.copy(mealType = MealType.SNACK)
        assertTrue(moved.hasChanges)
        assertEquals(190, moved.calories)
    }

    @Test
    fun presetsAndStepsFollowTheLoggedPortion() {
        val edit = EntryEdit(log = yogurt, originalAmount = 200.0, unit = "g")
        assertEquals(listOf(100.0, 200.0, 300.0, 400.0), edit.presets)
        assertEquals(210.0, edit.stepped(up = true))
        val banana = EntryEdit(log = yogurt.copy(weight = 1.0), originalAmount = 1.0, unit = "pc")
        assertEquals(listOf(0.5, 1.0, 1.5, 2.0), banana.presets)
        assertEquals(0.5, banana.copy(amount = 0.5).stepped(up = false))
    }

    @Test
    fun recipePercentageComesFromCalories() {
        assertEquals(50.0, recipePercentEaten(loggedCalories = 310, fullCalories = 620))
        assertEquals(100.0, recipePercentEaten(loggedCalories = 310, fullCalories = 0))
    }
}
