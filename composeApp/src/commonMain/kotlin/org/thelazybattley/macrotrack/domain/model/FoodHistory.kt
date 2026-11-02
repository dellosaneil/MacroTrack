package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlin.math.roundToInt

/** How far back food history looks. */
enum class FoodHistoryPeriod(val days: Int) {
    DAYS_30(days = 30),
    DAYS_60(days = 60),
    DAYS_90(days = 90)
}

/** One time a food was logged; [amount] is in its stored unit, or the percentage of a recipe serving. */
data class FoodHistoryEntry(
    val log: FoodLog,
    val amount: Double
)

/** Times eaten, the amount logged most often, the average and range, and the latest entry. */
data class FoodHistoryStats(
    val times: Int,
    val usual: Double,
    val usualCount: Int,
    val average: Double,
    val min: Double,
    val max: Double,
    val last: FoodHistoryEntry
)

/**
 * Every log of [name], newest first. A recipe log keeps only its calories, so its percentage of a serving comes from
 * [fullRecipeCalories]; a recipe that's been deleted counts each log as one serving.
 */
fun foodHistoryEntries(
    logs: List<FoodLog>,
    name: String,
    isRecipe: Boolean,
    fullRecipeCalories: Int?
): List<FoodHistoryEntry> = logs
    .filter { it.foodName == name && !it.isQuickAdd && it.isRecipe == isRecipe }
    .sortedWith(compareByDescending<FoodLog> { it.date }.thenByDescending { it.loggedAtMillis ?: 0 }.thenByDescending { it.id })
    .map { log ->
        val amount = when {
            !isRecipe -> log.weight
            fullRecipeCalories != null && fullRecipeCalories > 0 -> (log.calories * 100.0 / fullRecipeCalories).roundToInt().toDouble()
            else -> 100.0
        }
        FoodHistoryEntry(log = log, amount = amount)
    }

/** The entries from the last [period] days, today included. */
fun List<FoodHistoryEntry>.within(period: FoodHistoryPeriod, today: LocalDate): List<FoodHistoryEntry> {
    val from = today.minus(period = DatePeriod(days = period.days))
    return filter { it.log.date > from && it.log.date <= today }
}

/** Stats for these entries, newest first; null when there are none. A tie for usual goes to the newer amount. */
fun List<FoodHistoryEntry>.stats(): FoodHistoryStats? {
    if (isEmpty()) return null
    val amounts = map { it.amount }
    val usual = amounts.groupBy { it }.maxWith(
        comparator = compareBy<Map.Entry<Double, List<Double>>> { it.value.size }.thenByDescending { entry -> amounts.indexOf(entry.key) }
    ).key
    return FoodHistoryStats(
        times = size,
        usual = usual,
        usualCount = amounts.count { it == usual },
        average = amounts.average(),
        min = amounts.min(),
        max = amounts.max(),
        last = first()
    )
}

/** The meal more than half of these were eaten at, if there is one. */
fun List<FoodHistoryEntry>.mostlyAt(): MealType? =
    groupBy { it.log.mealType }.maxByOrNull { it.value.size }?.takeIf { it.value.size * 2 > size }?.key

/** How many times a week [times] in [period] comes to. */
fun timesPerWeek(times: Int, period: FoodHistoryPeriod): Double = times * 7.0 / period.days

/** Whole days from [date] to [today]. */
fun daysAgo(date: LocalDate, today: LocalDate): Int = date.daysUntil(other = today)
