package org.thelazybattley.macrotrack.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KeypadFieldTest {

    private val age = KeypadField(id = "age", label = "Age", value = "29", wholeDigits = 3, min = 13.0, max = 120.0)
    private val inches = KeypadField(id = "in", label = "Inches", value = "8.9", wholeDigits = 2, decimals = 1, max = 11.9, allowsZero = true)
    private val barcode = KeypadField(id = "code", label = "Barcode", value = "", wholeDigits = 14, isCode = true, minLength = 8)

    private fun KeypadField.type(keys: String): AmountInput =
        keys.fold(initial = AmountInput(text = value)) { input, key -> input.typed(key = key, rule = rule) }

    @Test
    fun wholeNumberFieldsTakeNoDecimalsAndStopAtTheirDigits() {
        assertFalse(age.isDecimalEnabled)
        assertEquals("34", age.type(keys = "3.4").text)
        assertEquals("123", age.type(keys = "1234").text)
    }

    @Test
    fun decimalFieldsKeepTheirPlaces() {
        assertTrue(inches.isDecimalEnabled)
        assertEquals("11.5", inches.type(keys = "11.55").text)
    }

    @Test
    fun numbersOutsideTheRangeNameTheLimit() {
        assertEquals(KeypadCheck.TooHigh(max = 120.0), age.check(text = "130"))
        assertEquals(KeypadCheck.TooLow(min = 13.0), age.check(text = "9"))
        assertEquals(KeypadCheck.Valid, age.check(text = "29"))
        assertEquals(KeypadCheck.Empty, age.check(text = ""))
    }

    @Test
    fun zeroCountsOnlyWhereTheFieldAllowsIt() {
        assertEquals(KeypadCheck.Valid, inches.check(text = "0"))
        assertEquals(KeypadCheck.Valid, inches.check(text = "4."))
        assertEquals(KeypadCheck.Empty, KeypadField(id = "w", label = "Weight", value = "", decimals = 1).check(text = "0"))
    }

    @Test
    fun codesKeepLeadingZerosAndNeedTheirMinimumLength() {
        assertFalse(barcode.isDecimalEnabled)
        val typed = barcode.type(keys = "0480001")
        assertEquals("0480001", typed.text)
        assertEquals(KeypadCheck.Empty, barcode.check(text = typed.text))
        assertEquals(KeypadCheck.Valid, barcode.check(text = typed.typed(key = '6', rule = barcode.rule).text))
        assertEquals(14, barcode.type(keys = "123456789012345").text.length)
    }
}
