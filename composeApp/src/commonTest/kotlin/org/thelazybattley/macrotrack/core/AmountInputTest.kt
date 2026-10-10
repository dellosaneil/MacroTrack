package org.thelazybattley.macrotrack.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AmountInputTest {

    @Test
    fun startsFromTheValueRoundedToTwoDecimals() {
        assertEquals("150", AmountInput.of(value = 150.0).text)
        assertEquals("33.33", AmountInput.of(value = 100.0 / 3).text)
        assertTrue(AmountInput.of(value = 1.0).replaceOnType)
    }

    @Test
    fun allowsTwoDecimalPlacesAndFourWholeDigits() {
        val typed = AmountInput.of(value = 1.0).typed(key = '1').typed(key = KEY_DECIMAL).typed(key = '2').typed(key = '5')
        assertEquals("1.25", typed.text)
        assertEquals(1.25, typed.value)
        assertEquals("1.25", typed.typed(key = '9').text)
        val big = AmountInput(text = "1234", replaceOnType = false)
        assertEquals("1234", big.typed(key = '5').text)
        assertEquals("1234.", big.typed(key = KEY_DECIMAL).text)
    }

    @Test
    fun backspaceAfterOpeningClearsTheAmount() {
        val cleared = AmountInput.of(value = 200.0).typed(key = KEY_BACKSPACE)
        assertEquals("", cleared.text)
        assertEquals(0.0, cleared.value)
        assertFalse(cleared.replaceOnType)
    }

    @Test
    fun ignoresKeysThatAreNotOnTheKeypad() {
        val input = AmountInput.of(value = 5.0)
        assertEquals(input, input.typed(key = '-'))
    }
}
