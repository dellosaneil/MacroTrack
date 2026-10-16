package org.thelazybattley.macrotrack.features.quickadd

import org.thelazybattley.macrotrack.core.KEY_BACKSPACE
import org.thelazybattley.macrotrack.core.KEY_DECIMAL
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import kotlin.math.roundToInt

/** Calories up to 4 digits. */
private val caloriesPattern = Regex(pattern = """^\d{0,4}$""")

/** Grams up to 3 digits and 1 decimal place. */
private val gramsPattern = Regex(pattern = """^\d{0,3}(\.\d?)?$""")

enum class QuickAddMode { CALORIES, MACROS }

enum class QuickAddField(val macro: MacroType) {
    PROTEIN(macro = MacroType.PROTEIN),
    CARBS(macro = MacroType.CARBS),
    FAT(macro = MacroType.FAT)
}

/**
 * Calories, or protein, carbs and fat, typed on the in-app keypad and logged without a food. [replaceOnType] is
 * true until a key is pressed after opening, switching mode or picking a macro, so the next key replaces it.
 */
data class QuickAddViewState(
    val mealType: MealType = MealType.BREAKFAST,
    val mode: QuickAddMode = QuickAddMode.CALORIES,
    val calories: String = "",
    val grams: Map<QuickAddField, String> = QuickAddField.entries.associateWith { "" },
    val activeField: QuickAddField = QuickAddField.PROTEIN,
    val replaceOnType: Boolean = true,
    val label: String = "",
    /** Calories left today before this entry; null until the user's details load. */
    val remainingCalories: Int? = null,
    val isSaved: Boolean = false
) {
    fun gramsOf(field: QuickAddField): Double = grams[field]?.toDoubleOrNull() ?: 0.0

    val macroCalories: Int
        get() = (gramsOf(QuickAddField.PROTEIN) * 4 + gramsOf(QuickAddField.CARBS) * 4 + gramsOf(QuickAddField.FAT) * 9)
            .roundToInt()

    /** What gets logged: the typed calories, or the calories the macros add up to. */
    val totalCalories: Int
        get() = when (mode) {
            QuickAddMode.CALORIES -> calories.toIntOrNull() ?: 0
            QuickAddMode.MACROS -> macroCalories
        }

    val canAdd: Boolean get() = totalCalories > 0

    val remainingAfterAdding: Int? get() = remainingCalories?.let { it - totalCalories }

    /** The macro with the biggest share of the calories, for the entry's color; carbs when only calories are known. */
    val dominantMacro: MacroType
        get() {
            if (mode == QuickAddMode.CALORIES) return MacroType.CARBS
            return listOf(
                MacroType.PROTEIN to gramsOf(QuickAddField.PROTEIN) * 4,
                MacroType.CARBS to gramsOf(QuickAddField.CARBS) * 4,
                MacroType.FAT to gramsOf(QuickAddField.FAT) * 9
            ).maxBy { it.second }.first
        }

    /** Applies a keypad key to the calories or the active macro. Keys that would make the value invalid are ignored. */
    fun typed(key: Char): QuickAddViewState {
        val isCalories = mode == QuickAddMode.CALORIES
        val current = if (isCalories) calories else grams[activeField].orEmpty()
        val next = when (key) {
            KEY_BACKSPACE -> if (replaceOnType) "" else current.dropLast(n = 1)
            KEY_DECIMAL -> when {
                isCalories -> current
                replaceOnType || current.isEmpty() -> "0."
                KEY_DECIMAL in current -> current
                else -> current + key
            }
            else -> if (!key.isDigit()) return this else if (replaceOnType || current == "0") key.toString() else current + key
        }
        if (!(if (isCalories) caloriesPattern else gramsPattern).matches(input = next)) return this
        return if (isCalories) {
            copy(calories = next, replaceOnType = false)
        } else {
            copy(grams = grams + (activeField to next), replaceOnType = false)
        }
    }

    fun withMode(mode: QuickAddMode): QuickAddViewState = copy(mode = mode, replaceOnType = true)

    fun withActiveField(field: QuickAddField): QuickAddViewState = copy(activeField = field, replaceOnType = true)
}
