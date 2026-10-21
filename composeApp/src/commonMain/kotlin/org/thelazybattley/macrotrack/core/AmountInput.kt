package org.thelazybattley.macrotrack.core

import kotlin.math.round

const val KEY_DECIMAL = '.'
const val KEY_BACKSPACE = '⌫'

/**
 * What a keypad accepts: up to [wholeDigits] whole digits and [decimals] decimal places. A [isCode] value,
 * like a barcode, is digits only and keeps a leading zero.
 */
data class KeypadRule(val wholeDigits: Int = 4, val decimals: Int = 2, val isCode: Boolean = false) {
    internal val pattern: Regex = Regex(
        pattern = "^\\d{0,$wholeDigits}" + (if (decimals > 0 && !isCode) "(\\.\\d{0,$decimals})?" else "") + "$"
    )
}

/**
 * An amount typed on the in-app keypad. [text] is what the readout shows and may be empty or end in
 * "." while typing; [replaceOnType] is true until a key is pressed after opening the keypad or
 * picking an amount, so the next key replaces it.
 */
data class AmountInput(
    val text: String,
    val replaceOnType: Boolean = true
) {
    val value: Double get() = text.toDoubleOrNull() ?: 0.0

    /** Applies a keypad key: a digit, [KEY_DECIMAL] or [KEY_BACKSPACE]. Keys that would break [rule] are ignored. */
    fun typed(key: Char, rule: KeypadRule = KeypadRule()): AmountInput {
        if (!key.isDigit() && key != KEY_DECIMAL && key != KEY_BACKSPACE) return this
        val current = if (replaceOnType) "" else text
        val next = when (key) {
            KEY_BACKSPACE -> current.dropLast(n = 1)
            KEY_DECIMAL -> when {
                current.isEmpty() -> "0."
                KEY_DECIMAL in current -> current
                else -> current + key
            }
            else -> if (current == "0" && !rule.isCode) key.toString() else current + key
        }
        if (!rule.pattern.matches(input = next)) return this
        return AmountInput(text = next, replaceOnType = false)
    }

    companion object {
        fun of(value: Double): AmountInput = AmountInput(text = value.roundToHundredths().toCleanString())
    }
}

fun Double.roundToHundredths(): Double = round(this * 100) / 100
