package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.features.calendar.currentStreak
import org.thelazybattley.macrotrack.features.profile.weighthistory.MOVING_AVERAGE_COMPARE_DAYS
import org.thelazybattley.macrotrack.features.profile.weighthistory.movingAverageOn

/** The days the weight widget's trend averages, and how many days of it the line shows. */
const val WIDGET_TREND_DAYS = 7

/** How much of something is eaten against its goal for the day. */
data class WidgetAmount(val eaten: Int, val goal: Int) {
    val left: Int get() = goal - eaten
    val progress: Float get() = if (goal <= 0) 0f else (eaten.toFloat() / goal).coerceIn(0f, 1f)
}

/** One meal in the large Today widget. [foodNames] are in the order they were logged. */
data class WidgetMeal(val mealType: MealType, val foodNames: List<String>, val calories: Int) {
    val isLogged: Boolean get() = foodNames.isNotEmpty()
}

data class WidgetWater(val totalMl: Int, val goalMl: Int, val glassMl: Int) {
    val progress: Float get() = if (goalMl <= 0) 0f else (totalMl.toFloat() / goalMl).coerceIn(0f, 1f)
}

/**
 * The weight trend: the average of the last [WIDGET_TREND_DAYS] days of weigh-ins, and how it moved since a week
 * before, in kg. [line] is the trend on each of the last [WIDGET_TREND_DAYS] days that has one, oldest first.
 */
data class WidgetWeight(val trendKg: Double, val weekChangeKg: Double?, val line: List<Double>)

enum class StreakDayState { LOGGED, OPEN, MISSED, LATER }

data class WidgetStreakDay(val dayOfWeek: DayOfWeek, val state: StreakDayState)

/** Days in a row with food logged, and this week from Monday, where today is [StreakDayState.OPEN] until logged. */
data class WidgetStreak(val days: Int, val isTodayLogged: Boolean, val week: List<WidgetStreakDay>)

/**
 * Everything the home screen widgets show, for [date]. [isSetUp] is false until onboarding is done, when the
 * widgets ask to finish setting up instead. [isWeightShown] off hides the weight itself and keeps the change.
 */
data class WidgetSnapshot(
    val date: LocalDate,
    val isSetUp: Boolean,
    val calories: WidgetAmount,
    val protein: WidgetAmount,
    val carbs: WidgetAmount,
    val fat: WidgetAmount,
    val meals: List<WidgetMeal>,
    val water: WidgetWater,
    val weight: WidgetWeight?,
    val streak: WidgetStreak,
    val unitSystem: UnitSystem,
    val isWeightShown: Boolean
)

fun widgetSnapshot(
    today: LocalDate,
    foodLogs: List<FoodLog>,
    macroGoals: MacroGoals?,
    water: WaterSummary,
    weights: List<Weight>,
    unitSystem: UnitSystem,
    isWeightShown: Boolean
): WidgetSnapshot {
    val todayLogs = foodLogs.filter { it.date == today }
    val loggedDates = foodLogs.map { it.date }.toSet()
    return WidgetSnapshot(
        date = today,
        isSetUp = macroGoals != null,
        calories = WidgetAmount(eaten = todayLogs.sumOf { it.calories }, goal = macroGoals?.calories ?: 0),
        protein = WidgetAmount(eaten = todayLogs.sumOf { it.protein }.toInt(), goal = macroGoals?.protein ?: 0),
        carbs = WidgetAmount(eaten = todayLogs.sumOf { it.carbs }.toInt(), goal = macroGoals?.carbs ?: 0),
        fat = WidgetAmount(eaten = todayLogs.sumOf { it.fat }.toInt(), goal = macroGoals?.fat ?: 0),
        meals = MealType.entries.map { mealType ->
            val logs = todayLogs.filter { it.mealType == mealType }.sortedBy { it.loggedAtMillis ?: it.id }
            WidgetMeal(mealType = mealType, foodNames = logs.map { it.foodName }, calories = logs.sumOf { it.calories })
        },
        water = WidgetWater(totalMl = water.totalMl, goalMl = water.goalMl, glassMl = water.settings.glassMl),
        weight = widgetWeight(today = today, weights = weights),
        streak = widgetStreak(today = today, loggedDates = loggedDates),
        unitSystem = unitSystem,
        isWeightShown = isWeightShown
    )
}

internal fun widgetWeight(today: LocalDate, weights: List<Weight>): WidgetWeight? {
    val trend = weights.movingAverageOn(date = today, windowDays = WIDGET_TREND_DAYS) ?: return null
    val weekAgo = today.minus(period = DatePeriod(days = MOVING_AVERAGE_COMPARE_DAYS))
    val line = (WIDGET_TREND_DAYS - 1 downTo 0).mapNotNull { daysAgo ->
        weights.movingAverageOn(date = today.minus(period = DatePeriod(days = daysAgo)), windowDays = WIDGET_TREND_DAYS)
    }
    return WidgetWeight(
        trendKg = trend,
        weekChangeKg = weights.movingAverageOn(date = weekAgo, windowDays = WIDGET_TREND_DAYS)?.let { trend - it },
        line = line
    )
}

internal fun widgetStreak(today: LocalDate, loggedDates: Set<LocalDate>): WidgetStreak {
    val monday = today.minus(period = DatePeriod(days = today.dayOfWeek.ordinal))
    return WidgetStreak(
        days = currentStreak(loggedDates = loggedDates, today = today),
        isTodayLogged = today in loggedDates,
        week = (0 until 7).map { offset ->
            val date = monday.plus(period = DatePeriod(days = offset))
            val state = when {
                date in loggedDates -> StreakDayState.LOGGED
                date == today -> StreakDayState.OPEN
                date < today -> StreakDayState.MISSED
                else -> StreakDayState.LATER
            }
            WidgetStreakDay(dayOfWeek = date.dayOfWeek, state = state)
        }
    )
}
