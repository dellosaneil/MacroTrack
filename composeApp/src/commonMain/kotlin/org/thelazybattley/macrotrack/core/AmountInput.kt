package org.thelazybattley.macrotrack.core

import kotlin.math.round

const val KEY_DECIMAL = '.'
const val KEY_BACKSPACE = '⌫'

/** Typed amounts: up to 4 whole digits and 2 decimal places. */
private val typedAmountPattern = Regex(pattern = """^\d{0,4}(\.\d{0,2})?$""")

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

    /** Applies a keypad key: a digit, [KEY_DECIMAL] or [KEY_BACKSPACE]. Keys that would make the amount invalid are ignored. */
    fun typed(key: Char): AmountInput {
        if (!key.isDigit() && key != KEY_DECIMAL && key != KEY_BACKSPACE) return this
        val current = if (replaceOnType) "" else text
        val next = when (key) {
            KEY_BACKSPACE -> current.dropLast(n = 1)
            KEY_DECIMAL -> when {
                current.isEmpty() -> "0."
                KEY_DECIMAL in current -> current
                else -> current + key
            }
            else -> if (current == "0") key.toString() else current + key
        }
        if (!typedAmountPattern.matches(input = next)) return this
        return AmountInput(text = next, replaceOnType = false)
    }

    companion object {
        fun of(value: Double): AmountInput = AmountInput(text = value.roundToHundredths().toCleanString())
    }
}

fun Double.roundToHundredths(): Double = round(this * 100) / 100
