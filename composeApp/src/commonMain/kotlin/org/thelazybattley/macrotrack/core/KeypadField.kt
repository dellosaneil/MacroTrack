package org.thelazybattley.macrotrack.core

/**
 * A number field typed on the shared keypad sheet. [value] is the field's current text. [shortLabel] names
 * the field on the step chips and the Next button when there are several fields to fill.
 *
 * A number must be above zero (or zero itself with [allowsZero]) and within [min]..[max]. An [isCode] field,
 * like a barcode, is digits only and needs at least [minLength] of them.
 */
data class KeypadField(
    val id: String,
    val label: String,
    val shortLabel: String = label,
    val subtitle: String = "",
    val unit: String = "",
    val value: String,
    val placeholder: String = "",
    val wholeDigits: Int = 4,
    val decimals: Int = 0,
    val min: Double? = null,
    val max: Double? = null,
    val allowsZero: Boolean = false,
    val isCode: Boolean = false,
    val minLength: Int = 1,
    /** The button's label while nothing valid is typed; "Enter a number" when null. */
    val emptyLabel: String? = null
) {
    val rule: KeypadRule get() = KeypadRule(wholeDigits = wholeDigits, decimals = decimals, isCode = isCode)

    val isDecimalEnabled: Boolean get() = decimals > 0 && !isCode

    fun check(text: String): KeypadCheck {
        val clean = text.removeSuffix(suffix = ".")
        if (isCode) return if (clean.length >= minLength) KeypadCheck.Valid else KeypadCheck.Empty
        val number = clean.toDoubleOrNull() ?: return KeypadCheck.Empty
        return when {
            min != null && number < min -> KeypadCheck.TooLow(min = min)
            max != null && number > max -> KeypadCheck.TooHigh(max = max)
            number < 0 || number == 0.0 && !allowsZero -> KeypadCheck.Empty
            else -> KeypadCheck.Valid
        }
    }
}

sealed interface KeypadCheck {
    data object Valid : KeypadCheck
    data object Empty : KeypadCheck
    data class TooLow(val min: Double) : KeypadCheck
    data class TooHigh(val max: Double) : KeypadCheck
}
