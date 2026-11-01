package org.thelazybattley.macrotrack.features.widgets

import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.kg
import macrotrack.composeapp.generated.resources.lb
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.value_unit
import macrotrack.composeapp.generated.resources.widget_add_amount
import macrotrack.composeapp.generated.resources.widget_eaten
import macrotrack.composeapp.generated.resources.widget_food
import macrotrack.composeapp.generated.resources.widget_grams
import macrotrack.composeapp.generated.resources.widget_grams_left
import macrotrack.composeapp.generated.resources.widget_kcal_left
import macrotrack.composeapp.generated.resources.widget_kcal_over
import macrotrack.composeapp.generated.resources.widget_log_food
import macrotrack.composeapp.generated.resources.widget_macro_value
import macrotrack.composeapp.generated.resources.widget_meal_empty
import macrotrack.composeapp.generated.resources.widget_meal_items
import macrotrack.composeapp.generated.resources.widget_protein_left
import macrotrack.composeapp.generated.resources.widget_protein_left_short
import macrotrack.composeapp.generated.resources.widget_protein_over
import macrotrack.composeapp.generated.resources.widget_scan
import macrotrack.composeapp.generated.resources.widget_set_up
import macrotrack.composeapp.generated.resources.widget_streak
import macrotrack.composeapp.generated.resources.widget_streak_days
import macrotrack.composeapp.generated.resources.widget_streak_logged
import macrotrack.composeapp.generated.resources.widget_streak_next
import macrotrack.composeapp.generated.resources.widget_streak_start
import macrotrack.composeapp.generated.resources.widget_today
import macrotrack.composeapp.generated.resources.widget_water
import macrotrack.composeapp.generated.resources.widget_water_of
import macrotrack.composeapp.generated.resources.widget_weight
import macrotrack.composeapp.generated.resources.widget_weight_caption
import macrotrack.composeapp.generated.resources.widget_weight_empty
import macrotrack.composeapp.generated.resources.widget_weight_steady
import macrotrack.composeapp.generated.resources.widget_weight_week
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.WidgetAmount
import org.thelazybattley.macrotrack.domain.model.WidgetSnapshot
import org.thelazybattley.macrotrack.domain.model.waterFromMl
import org.thelazybattley.macrotrack.domain.model.waterUnit
import org.thelazybattley.macrotrack.domain.usecase.water.formatWater
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.SAME_WEIGHT_THRESHOLD_KG
import kotlin.math.abs
import kotlin.math.roundToInt

/** A macro row: its name, "74 / 160g", "86g left", and how far along it is. */
data class MacroText(val name: String, val value: String, val left: String, val progress: Float)

/** A meal row in the large Today widget: "Lunch", "2 items · Chicken rice bowl, iced latte", and "640", or no calories when empty. */
data class MealText(val name: String, val detail: String, val calories: String?)

/**
 * A [WidgetSnapshot] with every word and number written out, since widgets can't read the app's strings while
 * they draw. [bigNumber] and [bigLabel] are for the ring, by whether it shows calories or protein.
 */
data class WidgetContent(
    val snapshot: WidgetSnapshot,
    val today: String,
    val date: String,
    val setUp: String,
    val caloriesNumber: String,
    val caloriesLabel: String,
    val proteinNumber: String,
    val proteinLabel: String,
    val proteinLeft: String,
    val eaten: String,
    val macros: List<MacroText>,
    val meals: List<MealText>,
    val logFood: String,
    val food: String,
    val scan: String,
    val addWater: String,
    val waterTitle: String,
    val waterTotal: String,
    val waterOf: String,
    val weightTitle: String,
    val weightNumber: String,
    val weightChange: String,
    val weightCaption: String,
    val weightEmpty: String,
    val streakTitle: String,
    val streakDays: String,
    val streakUnit: String,
    val streakNote: String,
    val dayLetters: List<String>
) {
    fun bigNumber(isProtein: Boolean): String = if (isProtein) proteinNumber else caloriesNumber

    fun bigLabel(isProtein: Boolean): String = if (isProtein) proteinLabel else caloriesLabel

    fun bigProgress(isProtein: Boolean): Float = if (isProtein) snapshot.protein.progress else snapshot.calories.progress
}

suspend fun WidgetSnapshot.toContent(): WidgetContent {
    val weight = weight
    val units = unitSystem
    val weightUnit = getString(resource = if (units == UnitSystem.IMPERIAL) Res.string.lb else Res.string.kg)
    val change = weight?.weekChangeKg
    val changeText = change?.takeIf { abs(it) >= SAME_WEIGHT_THRESHOLD_KG }?.let { getString(resource = Res.string.value_unit, units.signedWeight(kg = it), weightUnit) }
    return WidgetContent(
        snapshot = this,
        today = getString(resource = Res.string.widget_today),
        date = date.toShortDayMonthDay(),
        setUp = getString(resource = Res.string.widget_set_up),
        caloriesNumber = abs(calories.left).withThousands(),
        caloriesLabel = getString(resource = if (calories.left < 0) Res.string.widget_kcal_over else Res.string.widget_kcal_left),
        proteinNumber = getString(resource = Res.string.widget_grams, abs(protein.left).withThousands()),
        proteinLabel = getString(resource = if (protein.left < 0) Res.string.widget_protein_over else Res.string.widget_protein_left),
        proteinLeft = getString(resource = Res.string.widget_protein_left_short, protein.left.coerceAtLeast(minimumValue = 0).withThousands()),
        eaten = getString(resource = Res.string.widget_eaten, calories.eaten.withThousands()),
        macros = listOf(
            macroText(name = getString(resource = Res.string.protein), amount = protein),
            macroText(name = getString(resource = Res.string.carbs), amount = carbs),
            macroText(name = getString(resource = Res.string.fat), amount = fat)
        ),
        meals = meals.map { meal ->
            MealText(
                name = getString(resource = meal.mealType.title),
                detail = if (meal.isLogged) {
                    getPluralString(
                        resource = Res.plurals.widget_meal_items,
                        quantity = meal.foodNames.size,
                        meal.foodNames.joinToString(separator = ", "),
                        meal.foodNames.size
                    )
                } else {
                    getString(resource = Res.string.widget_meal_empty)
                },
                calories = meal.calories.takeIf { meal.isLogged }?.withThousands()
            )
        },
        logFood = getString(resource = Res.string.widget_log_food),
        food = getString(resource = Res.string.widget_food),
        scan = getString(resource = Res.string.widget_scan),
        addWater = getString(resource = Res.string.widget_add_amount, formatWater(ml = water.glassMl, unitSystem = units)),
        waterTitle = getString(resource = Res.string.widget_water),
        waterTotal = units.waterFromMl(ml = water.totalMl).withThousands(),
        waterOf = getString(
            resource = Res.string.widget_water_of,
            getString(resource = Res.string.value_unit, units.waterFromMl(ml = water.goalMl).withThousands(), units.waterUnit)
        ),
        weightTitle = getString(resource = Res.string.widget_weight),
        weightNumber = when {
            weight == null -> ""
            isWeightShown -> units.oneDecimal(kg = weight.trendKg)
            else -> change?.let { units.signedWeight(kg = it) } ?: "–"
        },
        weightChange = when {
            changeText == null -> getString(resource = Res.string.widget_weight_steady)
            isWeightShown -> getString(resource = Res.string.widget_weight_week, changeText)
            else -> getString(resource = Res.string.widget_weight_week, weightUnit)
        },
        weightCaption = getString(resource = Res.string.widget_weight_caption),
        weightEmpty = getString(resource = Res.string.widget_weight_empty),
        streakTitle = getString(resource = Res.string.widget_streak),
        streakDays = streak.days.toString(),
        streakUnit = getPluralString(resource = Res.plurals.widget_streak_days, quantity = streak.days),
        streakNote = when {
            streak.isTodayLogged -> getString(resource = Res.string.widget_streak_logged)
            streak.days > 0 -> getString(resource = Res.string.widget_streak_next, streak.days + 1)
            else -> getString(resource = Res.string.widget_streak_start)
        },
        dayLetters = streak.week.map { it.dayOfWeek.name.take(n = 1) }
    )
}

private suspend fun macroText(name: String, amount: WidgetAmount) = MacroText(
    name = name,
    value = getString(resource = Res.string.widget_macro_value, amount.eaten.withThousands(), amount.goal.withThousands()),
    left = getString(resource = Res.string.widget_grams_left, amount.left.coerceAtLeast(minimumValue = 0).withThousands()),
    progress = amount.progress
)

/** "74.2" in the user's unit. */
private fun UnitSystem.oneDecimal(kg: Double): String {
    val tenths = (abs(weightFromKg(kg = kg)) * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}

/** "+0.4" or "−0.7" in the user's unit. */
private fun UnitSystem.signedWeight(kg: Double): String = (if (kg < 0) "−" else "+") + oneDecimal(kg = kg)
