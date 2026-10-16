package org.thelazybattley.macrotrack.features.insights

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.foodlog.resolveDayGoal
import kotlin.math.abs
import kotlin.math.roundToInt

private const val ON_TARGET_TOLERANCE = 0.1
private const val MOST_LOGGED_LIMIT = 5

enum class InsightsRange(val days: Int, val bucketDays: Int) {
    /** Last 7 days, one bar per day. */
    WEEK(days = 7, bucketDays = 1),

    /** Last 4 weeks, one bar per week. */
    MONTH(days = 28, bucketDays = 7)
}

/** One bar: average eaten per logged day against the average goal, both in kcal. */
data class CalorieBar(
    val start: LocalDate,
    val eaten: Int?,
    val goal: Int?
) {
    val isOnTarget: Boolean
        get() = eaten != null && goal != null && goal > 0 && abs(eaten - goal) <= goal * ON_TARGET_TOLERANCE
}

data class MacroAverage(
    val macroType: MacroType,
    val average: Int,
    val target: Int
)

data class LoggedFood(
    val name: String,
    val count: Int,
    val totalAmount: Double,
    val calories: Int,
    val dominantMacro: MacroType
) {
    /** Recipes are logged with an amount of 0, so only their count is meaningful. */
    val isRecipe: Boolean get() = totalAmount == 0.0
}

data class Insights(
    val range: InsightsRange,
    val bars: List<CalorieBar>,
    val averageCalories: Int?,
    val loggedDays: Int,
    val daysOnTarget: Int,
    val macros: List<MacroAverage>,
    val mealShares: Map<MealType, Int>,
    val mostLogged: List<LoggedFood>
)

fun calculateInsights(
    range: InsightsRange,
    today: LocalDate,
    foodLogs: List<FoodLog>,
    dailyGoals: List<DailyGoal>,
    currentGoal: Goal?,
    currentMacroGoals: MacroGoals?
): Insights {
    val start = today.minus(period = DatePeriod(days = range.days - 1))
    val logsInRange = foodLogs.filter { it.date in start..today }
    val logsByDate = logsInRange.groupBy { it.date }
    val loggedDates = logsByDate.keys.sorted()
    val goalFor = { date: LocalDate ->
        resolveDayGoal(
            date = date,
            today = today,
            dailyGoals = dailyGoals,
            currentGoal = currentGoal,
            currentMacroGoals = currentMacroGoals
        )?.macroGoals
    }

    val bars = (0 until range.days / range.bucketDays).map { bucket ->
        val bucketStart = start.plus(period = DatePeriod(days = bucket * range.bucketDays))
        val bucketDates = (0 until range.bucketDays).map { bucketStart.plus(period = DatePeriod(days = it)) }
        val logged = bucketDates.filter { it in logsByDate }
        CalorieBar(
            start = bucketStart,
            eaten = logged.takeIf { it.isNotEmpty() }
                ?.map { date -> logsByDate.getValue(date).sumOf { it.calories } }
                ?.average()?.roundToInt(),
            goal = bucketDates.mapNotNull { goalFor(it)?.calories }.takeIf { it.isNotEmpty() }?.average()?.roundToInt()
        )
    }

    val dailyEaten = loggedDates.associateWith { date -> logsByDate.getValue(date) }
    val daysOnTarget = dailyEaten.count { (date, logs) ->
        val goal = goalFor(date)?.calories ?: return@count false
        goal > 0 && abs(logs.sumOf { it.calories } - goal) <= goal * ON_TARGET_TOLERANCE
    }

    val macros = if (loggedDates.isEmpty()) {
        emptyList()
    } else {
        val targets = loggedDates.mapNotNull { goalFor(it) }
        listOf(
            MacroType.PROTEIN to { log: FoodLog -> log.protein } to { goals: MacroGoals -> goals.protein },
            MacroType.CARBS to { log: FoodLog -> log.carbs } to { goals: MacroGoals -> goals.carbs },
            MacroType.FAT to { log: FoodLog -> log.fat } to { goals: MacroGoals -> goals.fat }
        ).map { (typeAndValue, target) ->
            val (type, value) = typeAndValue
            MacroAverage(
                macroType = type,
                average = dailyEaten.values.map { logs -> logs.sumOf(value) }.average().roundToInt(),
                target = targets.map(target).takeIf { it.isNotEmpty() }?.average()?.roundToInt() ?: 0
            )
        }
    }

    val totalCalories = logsInRange.sumOf { it.calories }
    val mealShares = MealType.entries.associateWith { mealType ->
        if (totalCalories == 0) 0 else logsInRange.filter { it.mealType == mealType }.sumOf { it.calories } * 100 / totalCalories
    }

    // Quick adds are one-off labels, not foods, so they stay out of the most logged foods.
    val mostLogged = logsInRange.filterNot { it.isQuickAdd }.groupBy { it.foodName }
        .map { (name, logs) ->
            LoggedFood(
                name = name,
                count = logs.size,
                totalAmount = logs.sumOf { it.weight },
                calories = logs.sumOf { it.calories },
                dominantMacro = logs.last().dominantMacro
            )
        }
        .sortedWith(compareByDescending<LoggedFood> { it.count }.thenByDescending { it.calories })
        .take(n = MOST_LOGGED_LIMIT)

    return Insights(
        range = range,
        bars = bars,
        averageCalories = dailyEaten.values.takeIf { it.isNotEmpty() }
            ?.map { logs -> logs.sumOf { it.calories } }?.average()?.roundToInt(),
        loggedDays = loggedDates.size,
        daysOnTarget = daysOnTarget,
        macros = macros,
        mealShares = mealShares,
        mostLogged = mostLogged
    )
}
