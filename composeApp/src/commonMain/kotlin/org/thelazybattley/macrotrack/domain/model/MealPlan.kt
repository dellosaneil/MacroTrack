package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable

/** How many days ahead the meal plan reaches, tomorrow included. */
const val PLAN_DAYS_AHEAD = 6

/** How far back a repeating meal that logs itself catches up, for days the app wasn't opened. */
const val AUTO_LOG_CATCH_UP_DAYS = 7

/** A food with its portion and macros, as planned or repeated; logged as a [FoodLog] once it's eaten. */
@Serializable
data class PlanFood(
    val foodName: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val weight: Double,
    val dominantMacro: MacroType,
    val isQuickAdd: Boolean = false
) {
    fun toFoodLog(date: LocalDate, mealType: MealType) = FoodLog(
        mealType = mealType,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        id = 0,
        foodName = foodName,
        weight = weight,
        dominantMacro = dominantMacro,
        date = date,
        isQuickAdd = isQuickAdd
    )
}

fun FoodLog.toPlanFood() = PlanFood(
    foodName = foodName,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    weight = weight,
    dominantMacro = dominantMacro,
    isQuickAdd = isQuickAdd
)

enum class PlanStatus {
    /** Waiting for Ate it or Skip; not counted. */
    PENDING,

    /** Logged; [PlannedFood.logId] is the log it became. */
    EATEN,

    /** Skipped for that day; for a repeating meal, it also stops the day being filled in again. */
    SKIPPED
}

/** Food planned for one day, by hand or by the repeating meal [repeatingMealId]. It counts only once eaten. */
data class PlannedFood(
    val id: Long,
    val date: LocalDate,
    val mealType: MealType,
    val food: PlanFood,
    val repeatingMealId: Long? = null,
    val status: PlanStatus = PlanStatus.PENDING,
    val logId: Long? = null
) {
    val isPending: Boolean get() = status == PlanStatus.PENDING
}

/**
 * [foods] added to [mealType] on [days] from [startDate], until [endDate] or [maxTimes] days, whichever is set.
 * With [isAutoLog] they're logged on the day; otherwise they're planned and wait for Ate it.
 */
data class RepeatingMeal(
    val id: Long = 0,
    val name: String,
    val mealType: MealType,
    val days: Set<DayOfWeek>,
    val foods: List<PlanFood>,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val maxTimes: Int? = null,
    val isAutoLog: Boolean = false,
    val isActive: Boolean = true,
    /** The last day it was set up or changed; logging for missed days never reaches back before it. */
    val updatedOn: LocalDate = startDate
) {
    val calories: Int get() = foods.sumOf { it.calories }

    /** Whether [date] is one of its days, ignoring [isActive]. */
    fun occursOn(date: LocalDate): Boolean {
        if (date < startDate || date.dayOfWeek !in days) return false
        if (endDate != null && date > endDate) return false
        if (maxTimes != null && occurrencesThrough(date = date) > maxTimes) return false
        return true
    }

    /** The next [count] days it runs on, from [from]; none once it's paused or over. */
    fun nextDates(from: LocalDate, count: Int): List<LocalDate> {
        if (!isActive || days.isEmpty()) return emptyList()
        val last = lastDate
        return generateSequence(seed = maxOf(from, startDate)) { it.plus(period = DatePeriod(days = 1)) }
            .takeWhile { last == null || it <= last }
            .filter { occursOn(date = it) }
            .take(n = count)
            .toList()
    }

    /** True when there are no days left after [today]. */
    fun hasEnded(today: LocalDate): Boolean = nextDates(from = today, count = 1).isEmpty() && isActive

    /** How many times it runs in all; null when it never ends. */
    val totalTimes: Int?
        get() = maxTimes ?: endDate?.let { end -> if (end < startDate) 0 else occurrencesThrough(date = end) }

    /** The day after which nothing more is added, from [endDate] or [maxTimes]; null when it never ends. */
    private val lastDate: LocalDate?
        get() = when {
            endDate != null -> endDate
            // Each week has at least one of its days, so maxTimes weeks always reaches the last one.
            maxTimes != null -> startDate.plus(period = DatePeriod(days = maxTimes * DAYS_PER_WEEK))
            else -> null
        }

    /** Its days from [startDate] through [date], counting [date] when it's one. */
    private fun occurrencesThrough(date: LocalDate): Int {
        val span = startDate.daysUntil(other = date) + 1
        val fullWeeks = span / DAYS_PER_WEEK
        val rest = (0 until span % DAYS_PER_WEEK).count {
            startDate.plus(period = DatePeriod(days = fullWeeks * DAYS_PER_WEEK + it)).dayOfWeek in days
        }
        return fullWeeks * days.size + rest
    }

    companion object {
        private const val DAYS_PER_WEEK = 7
        val EVERY_DAY: Set<DayOfWeek> = DayOfWeek.entries.toSet()
        val WEEKDAYS: Set<DayOfWeek> = setOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
        val WEEKENDS: Set<DayOfWeek> = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }
}

/**
 * The days [meal] should have planned rows for but doesn't yet: the next [PLAN_DAYS_AHEAD] days from [today], and,
 * when it logs itself, up to [AUTO_LOG_CATCH_UP_DAYS] back (never before it was last changed). A day that has any
 * row for it already, skipped or eaten, is left alone, so a skip sticks.
 */
fun RepeatingMeal.datesToFill(existing: List<PlannedFood>, today: LocalDate): List<LocalDate> {
    if (!isActive || foods.isEmpty()) return emptyList()
    val from = if (isAutoLog) {
        maxOf(updatedOn, today.plus(period = DatePeriod(days = -AUTO_LOG_CATCH_UP_DAYS)))
    } else {
        today
    }
    val filled = existing.filter { it.repeatingMealId == id }.map { it.date }.toSet()
    return generateSequence(seed = from) { it.plus(period = DatePeriod(days = 1)) }
        .takeWhile { it <= today.plus(period = DatePeriod(days = PLAN_DAYS_AHEAD)) }
        .filter { it !in filled && occursOn(date = it) }
        .toList()
}
