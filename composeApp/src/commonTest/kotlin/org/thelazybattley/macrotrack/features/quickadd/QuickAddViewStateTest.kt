package org.thelazybattley.macrotrack.features.quickadd

import org.thelazybattley.macrotrack.core.KEY_BACKSPACE
import org.thelazybattley.macrotrack.core.KEY_DECIMAL
import org.thelazybattley.macrotrack.domain.model.MacroType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuickAddViewStateTest {

    private fun QuickAddViewState.type(keys: String): QuickAddViewState = keys.fold(this) { state, key -> state.typed(key = key) }

    @Test
    fun caloriesAreWholeNumbersUpToFourDigits() {
        val state = QuickAddViewState().type(keys = "45.0")
        assertEquals("450", state.calories)
        assertEquals("1234", QuickAddViewState().type(keys = "12345").calories)
        assertEquals(450, state.totalCalories)
        assertTrue(state.canAdd)
    }

    @Test
    fun emptyCaloriesCannotBeAdded() {
        assertFalse(QuickAddViewState().canAdd)
        assertFalse(QuickAddViewState().type(keys = "0").canAdd)
    }

    @Test
    fun macrosAddUpToCalories() {
        val state = QuickAddViewState()
            .withMode(mode = QuickAddMode.MACROS).type(keys = "32")
            .withActiveField(field = QuickAddField.CARBS).type(keys = "45")
            .withActiveField(field = QuickAddField.FAT).type(keys = "1.5")
        assertEquals(32 * 4 + 45 * 4 + 14, state.totalCalories)
        assertEquals(MacroType.CARBS, state.dominantMacro)
        // Grams take one decimal place and three whole digits.
        assertEquals("1.5", state.grams[QuickAddField.FAT])
        assertEquals("1.5", state.type(keys = "5").grams[QuickAddField.FAT])
        assertEquals("999", QuickAddViewState(mode = QuickAddMode.MACROS).type(keys = "9999").grams[QuickAddField.PROTEIN])
    }

    @Test
    fun firstKeyAfterSwitchingReplacesTheValue() {
        val typed = QuickAddViewState(mode = QuickAddMode.MACROS).type(keys = "20")
        val switchedBack = typed.withActiveField(field = QuickAddField.PROTEIN)
        assertEquals("5", switchedBack.type(keys = "5").grams[QuickAddField.PROTEIN])
        assertEquals("", switchedBack.typed(key = KEY_BACKSPACE).grams[QuickAddField.PROTEIN])
        assertEquals("0.", switchedBack.typed(key = KEY_DECIMAL).grams[QuickAddField.PROTEIN])
        assertEquals("2", typed.typed(key = KEY_BACKSPACE).grams[QuickAddField.PROTEIN])
    }

    @Test
    fun remainingCaloriesCountTheEntry() {
        val state = QuickAddViewState(remainingCalories = 1050).type(keys = "1200")
        assertEquals(-150, state.remainingAfterAdding)
    }
}
