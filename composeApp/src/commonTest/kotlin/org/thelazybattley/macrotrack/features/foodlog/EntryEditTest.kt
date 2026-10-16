package org.thelazybattley.macrotrack.features.foodlog

import org.thelazybattley.macrotrack.core.KEY_BACKSPACE
import org.thelazybattley.macrotrack.core.KEY_DECIMAL
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.UnitSystem
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
        val edit = EntryEdit(log = yogurt, originalAmount = 200.0, unit = "g").withAmount(value = 150.0)
        assertEquals(143, edit.calories)
        assertEquals(15.0, edit.protein)
        assertEquals(-47, edit.caloriesChange)
        assertEquals(150.0, edit.weight)
        assertTrue(edit.canSave)
    }

    @Test
    fun recipeKeepsZeroWeightAndScalesByPercentage() {
        val edit = EntryEdit(log = bowl, originalAmount = 100.0, unit = "%").withAmount(value = 75.0)
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
        assertEquals(0.5, banana.withAmount(value = 0.5).stepped(up = false))
    }

    @Test
    fun unchangedAmountWithManyDecimalsIsNotAChange() {
        val edit = EntryEdit(log = bowl, originalAmount = 100.0 / 3, unit = "%")
        assertEquals("33.33", edit.amountText)
        assertFalse(edit.hasChanges)
    }

    @Test
    fun recipeBaseCaloriesIsAFullServing() {
        val edit = EntryEdit(log = bowl.copy(calories = 310), originalAmount = 50.0, unit = "%")
        assertEquals(620, edit.baseCalories)
    }

    @Test
    fun firstKeyReplacesTheAmountThenKeysAppend() {
        val edit = EntryEdit(log = yogurt, originalAmount = 200.0, unit = "g")
        val typed = edit.typed(key = '1').typed(key = '5').typed(key = '0')
        assertEquals("150", typed.amountText)
        assertEquals(150.0, typed.amount)
        assertFalse(typed.replaceOnType)
        assertEquals("1505", typed.typed(key = '5').amountText)
        assertEquals("1505", typed.typed(key = '5').typed(key = '5').amountText)
    }

    @Test
    fun decimalAndBackspaceRules() {
        val edit = EntryEdit(log = yogurt, originalAmount = 200.0, unit = "g")
        val half = edit.typed(key = KEY_DECIMAL).typed(key = '5')
        assertEquals("0.5", half.amountText)
        assertEquals("0.5", half.typed(key = KEY_DECIMAL).amountText)
        assertEquals("0.52", half.typed(key = '2').amountText)
        assertEquals("0.52", half.typed(key = '2').typed(key = '9').amountText)
        assertEquals("7", edit.typed(key = '0').typed(key = '7').amountText)
        assertEquals("x", edit.typed(key = 'x').let { if (it == edit) "x" else it.amountText })
    }

    @Test
    fun backspaceCanClearAndBlocksSaving() {
        val cleared = EntryEdit(log = yogurt, originalAmount = 200.0, unit = "g").typed(key = KEY_BACKSPACE)
        assertEquals("", cleared.amountText)
        assertEquals(0.0, cleared.amount)
        assertFalse(cleared.canSave)
        val picked = cleared.withAmount(value = 100.0)
        assertEquals("100", picked.amountText)
        assertTrue(picked.replaceOnType)
        assertTrue(picked.canSave)
    }

    @Test
    fun recipePercentageComesFromCalories() {
        assertEquals(50.0, recipePercentEaten(loggedCalories = 310, fullCalories = 620))
        assertEquals(100.0, recipePercentEaten(loggedCalories = 310, fullCalories = 0))
    }

    @Test
    fun imperialEditsGramsInOunces() {
        val ounces = UnitSystem.IMPERIAL.portionUnit(storedUnit = "g")
        val edit = EntryEdit(log = yogurt, originalAmount = 200.0, unit = "g", portionUnit = ounces)
        assertEquals("7.05", edit.amountText)
        assertEquals("oz", edit.unitLabel)
        assertFalse(edit.hasChanges)
        assertEquals(0.5, edit.step)
        val halved = edit.withAmount(value = 3.5)
        assertEquals(99.22, halved.weight, absoluteTolerance = 0.01)
        assertEquals(94, halved.calories)
    }

    @Test
    fun quickAddIsChangedByPercentage() {
        val quickAdd = yogurt.copy(foodName = "Restaurant lunch", calories = 600, weight = 0.0, isQuickAdd = true)
        assertFalse(quickAdd.isRecipe)
        val edit = EntryEdit(log = quickAdd, originalAmount = 100.0, unit = "%").withAmount(value = 50.0)
        assertTrue(edit.isPercentage)
        assertFalse(edit.isRecipe)
        assertEquals(300, edit.calories)
        assertEquals(600, edit.baseCalories)
        assertEquals(0.0, edit.weight)
    }
}
