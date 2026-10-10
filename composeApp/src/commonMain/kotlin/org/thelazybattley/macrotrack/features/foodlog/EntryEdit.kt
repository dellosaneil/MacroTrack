package org.thelazybattley.macrotrack.features.foodlog

import org.thelazybattley.macrotrack.core.AmountInput
import org.thelazybattley.macrotrack.core.roundToHundredths
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.addmeal.FULL_RECIPE_PERCENT
import kotlin.math.round
import kotlin.math.roundToInt

private const val RECIPE_STEP_PERCENT = 25.0

/**
 * A logged entry being changed. New macros are scaled from the log itself, so the edit matches
 * what was logged even if the food or recipe has changed since.
 */
data class EntryEdit(
    val log: FoodLog,
    /** The portion that was logged: grams/units, or the percentage eaten for a recipe. */
    val originalAmount: Double,
    val input: AmountInput = AmountInput.of(value = originalAmount),
    val mealType: MealType = log.mealType,
    /** The food's unit, or "%" for a recipe. */
    val unit: String
) {
    val amountText: String get() = input.text
    val replaceOnType: Boolean get() = input.replaceOnType
    val amount: Double get() = input.value

    val isRecipe: Boolean get() = log.weight == 0.0

    /** Calories of a full recipe serving, or of the logged portion for a food. */
    val baseCalories: Int
        get() = if (isRecipe && originalAmount > 0) {
            (log.calories * FULL_RECIPE_PERCENT / originalAmount).roundToInt()
        } else {
            log.calories
        }

    private val scale: Double get() = if (originalAmount > 0) amount / originalAmount else 1.0

    val calories: Int get() = (log.calories * scale).roundToInt()
    val protein: Double get() = log.protein * scale
    val carbs: Double get() = log.carbs * scale
    val fat: Double get() = log.fat * scale

    val caloriesChange: Int get() = calories - log.calories

    /** The readout shows the logged amount to 2 decimal places, so that is what an unchanged amount looks like. */
    val hasChanges: Boolean get() = amount != originalAmount.roundToHundredths() || mealType != log.mealType

    val canSave: Boolean get() = hasChanges && amount > 0

    val step: Double
        get() = when {
            isRecipe -> RECIPE_STEP_PERCENT
            originalAmount > 10 -> 10.0
            else -> 0.5
        }

    /** Quick picks: quarters of a recipe serving, or ½×, 1×, 1.5× and 2× the logged portion. */
    val presets: List<Double>
        get() = if (isRecipe) {
            listOf(25.0, 50.0, 75.0, FULL_RECIPE_PERCENT)
        } else {
            listOf(0.5, 1.0, 1.5, 2.0).map { (originalAmount * it).roundTo(step = step) }.distinct()
        }

    /** Weight to store: the new portion for a food; recipes keep 0.0, which marks them as recipes. */
    val weight: Double get() = if (isRecipe) 0.0 else amount

    fun stepped(up: Boolean): Double = (if (up) amount + step else amount - step).roundTo(step = step)

    fun withAmount(value: Double): EntryEdit = if (value > 0) copy(input = AmountInput.of(value = value)) else this

    fun startTyping(): EntryEdit = copy(input = input.copy(replaceOnType = true))

    fun typed(key: Char): EntryEdit = copy(input = input.typed(key = key))
}

private fun Double.roundTo(step: Double): Double = (round(this / step) * step).let { if (it <= 0) step else it }
