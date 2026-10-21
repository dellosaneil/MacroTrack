package org.thelazybattley.macrotrack.features.profile.nutritiongoals.ui

import androidx.compose.runtime.Composable
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.body_fat
import macrotrack.composeapp.generated.resources.body_fat_subtitle
import macrotrack.composeapp.generated.resources.g
import macrotrack.composeapp.generated.resources.kcal_unit
import macrotrack.composeapp.generated.resources.keypad_calories
import macrotrack.composeapp.generated.resources.keypad_calories_subtitle
import macrotrack.composeapp.generated.resources.keypad_grams_a_day
import macrotrack.composeapp.generated.resources.keypad_lean_mass
import macrotrack.composeapp.generated.resources.keypad_matches_goal
import macrotrack.composeapp.generated.resources.keypad_over_goal
import macrotrack.composeapp.generated.resources.keypad_percent_of_kcal
import macrotrack.composeapp.generated.resources.keypad_under_goal
import macrotrack.composeapp.generated.resources.value_with_unit
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.KeypadField
import org.thelazybattley.macrotrack.core.text
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.MacroTargets
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.CommonKeypadSheet
import org.thelazybattley.macrotrack.ui.common.KeypadPreview
import org.thelazybattley.macrotrack.ui.common.formatWeightOneDecimalWithUnit
import kotlin.math.abs
import kotlin.math.roundToInt

/** The keypad for the body fat on the calculated side. Typing one switches protein and fat to lean mass. */
@Composable
internal fun BodyFatKeypad(
    bodyFat: Double?,
    weight: Double,
    onCommit: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    CommonKeypadSheet(
        fields = listOf(
            KeypadField(
                id = BODY_FAT,
                label = stringResource(resource = Res.string.body_fat),
                subtitle = stringResource(resource = Res.string.body_fat_subtitle),
                unit = "%",
                value = bodyFat?.toCleanString().orEmpty(),
                wholeDigits = 2,
                decimals = 1,
                min = MacroTargets.MIN_BODY_FAT,
                max = MacroTargets.MAX_BODY_FAT
            )
        ),
        openId = BODY_FAT,
        onOpen = {},
        onCommit = { _, text -> text.toDoubleOrNull()?.let(onCommit) },
        onDismiss = onDismiss,
        preview = { _, live ->
            live(BODY_FAT).toDoubleOrNull()?.takeIf { it < 100 }?.let {
                KeypadPreview(
                    value = MacroTargets.leanMass(weight = weight, bodyFat = it).formatWeightOneDecimalWithUnit(),
                    caption = stringResource(resource = Res.string.keypad_lean_mass)
                )
            }
        }
    )
}

/**
 * Calories, then protein, carbs and fat, in grams or [isPercent] of the calorie goal. The preview adds up what's
 * typed so far against the goal.
 */
@Composable
internal fun CustomTargetsKeypad(
    custom: MacroGoals,
    isPercent: Boolean,
    openId: String,
    onOpen: (String) -> Unit,
    onChanged: (MacroGoals) -> Unit,
    onDismiss: () -> Unit
) {
    val macros = listOf(
        CUSTOM_PROTEIN to MacroType.PROTEIN,
        CUSTOM_CARBS to MacroType.CARBS,
        CUSTOM_FAT to MacroType.FAT
    )
    val kcal = stringResource(resource = Res.string.kcal_unit)
    val fields = listOf(
        KeypadField(
            id = CUSTOM_CALORIES,
            label = stringResource(resource = Res.string.keypad_calories),
            subtitle = stringResource(resource = Res.string.keypad_calories_subtitle),
            unit = kcal,
            value = custom.calories.toString(),
            wholeDigits = 4,
            min = MacroTargets.MIN_CUSTOM_CALORIES,
            max = MacroTargets.MAX_CUSTOM_CALORIES
        )
    ) + macros.map { (id, type) ->
        val grams = custom.gramsOf(type = type)
        KeypadField(
            id = id,
            label = stringResource(resource = type.text()),
            subtitle = if (isPercent) {
                stringResource(resource = Res.string.keypad_percent_of_kcal, custom.calories.withThousands())
            } else {
                stringResource(resource = Res.string.keypad_grams_a_day)
            },
            unit = if (isPercent) "%" else stringResource(resource = Res.string.g),
            value = if (isPercent) percentOf(grams = grams, type = type, calories = custom.calories).toString() else grams.toString(),
            wholeDigits = 3,
            max = if (isPercent) MAX_PERCENT else MAX_GRAMS,
            allowsZero = true
        )
    }
    CommonKeypadSheet(
        fields = fields,
        openId = openId,
        onOpen = onOpen,
        onCommit = { id, text ->
            val number = text.toDoubleOrNull() ?: return@CommonKeypadSheet
            val type = macros.firstOrNull { it.first == id }?.second
            onChanged(
                if (type == null) {
                    custom.copy(calories = number.roundToInt())
                } else {
                    val grams = if (isPercent) gramsOf(percent = number, type = type, calories = custom.calories) else number.roundToInt()
                    custom.withGrams(type = type, grams = grams)
                }
            )
        },
        onDismiss = onDismiss,
        preview = { _, live ->
            val calories = live(CUSTOM_CALORIES).toDoubleOrNull()?.roundToInt() ?: custom.calories
            val sum = macros.sumOf { (id, type) ->
                val typed = live(id).toDoubleOrNull() ?: 0.0
                val grams = if (isPercent) gramsOf(percent = typed, type = type, calories = calories) else typed.roundToInt()
                grams * type.calories
            }
            val diff = sum - calories
            KeypadPreview(
                value = stringResource(resource = Res.string.value_with_unit, sum.withThousands(), kcal),
                caption = when {
                    abs(diff) <= MacroTargets.MATCH_TOLERANCE_KCAL -> stringResource(resource = Res.string.keypad_matches_goal)
                    diff < 0 -> stringResource(resource = Res.string.keypad_under_goal, (-diff).withThousands(), calories.withThousands())
                    else -> stringResource(resource = Res.string.keypad_over_goal, diff.withThousands(), calories.withThousands())
                }
            )
        }
    )
}

private fun MacroGoals.gramsOf(type: MacroType): Int = when (type) {
    MacroType.PROTEIN -> protein
    MacroType.CARBS -> carbs
    MacroType.FAT -> fat
}

private fun MacroGoals.withGrams(type: MacroType, grams: Int): MacroGoals = when (type) {
    MacroType.PROTEIN -> copy(protein = grams)
    MacroType.CARBS -> copy(carbs = grams)
    MacroType.FAT -> copy(fat = grams)
}

private const val MAX_GRAMS = 999.0
private const val MAX_PERCENT = 100.0
