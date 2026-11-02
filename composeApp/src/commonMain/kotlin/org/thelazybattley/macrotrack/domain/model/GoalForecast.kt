package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.features.profile.weighthistory.movingAverage
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Under this many weigh-ins there's no date yet, only a rough guess from calories. */
const val FORECAST_MIN_WEIGH_INS = 14

/** Trend weight moving less than [FORECAST_FLAT_KG] over this many days reads as flat. */
const val FORECAST_FLAT_DAYS = 21
const val FORECAST_FLAT_KG = 0.2

/** The likely range uses the pace this much faster and slower, so it widens further out. */
const val FORECAST_RANGE_SPREAD = 0.25

/** Past this share of body weight a week, a pace gets the "too fast" warning. */
const val FORECAST_SAFE_WEEKLY_SHARE = 0.01

/** Trend weight is the moving average over this many days. */
const val FORECAST_TREND_DAYS = 7

/** What if moves in steps of this many kcal. */
const val FORECAST_KCAL_STEP = 50

/** What if never suggests eating under this. */
const val FORECAST_MIN_KCAL = 1200

/** How far below (to lose) or above (to gain) maintenance What if reaches. */
const val FORECAST_KCAL_SPAN = 1150

/** From calories needs this many logged days in the last 4 weeks. */
const val FORECAST_MIN_LOGGED_DAYS = 7

private const val KCAL_PER_KG = 7700.0
private const val DAYS_PER_WEEK = 7
private const val CALORIE_WINDOW_DAYS = 28

/** A pace slower than this, in kg a week toward the goal, never gets there. */
private const val MIN_KG_PER_WEEK = 0.01

/** Further out than this the forecast stops giving a date. */
private const val MAX_FORECAST_DAYS = 365 * 5

/** Since your goal changed needs this many days of weigh-ins after the change; until then it uses the last 4 weeks. */
const val FORECAST_GOAL_CHANGE_DAYS = 14

/** What the pace toward the goal is worked out from. */
enum class ForecastBasis(val days: Int?) {
    LAST_2_WEEKS(days = 14),
    LAST_4_WEEKS(days = 28),

    /** Since the goal last changed between deficit, maintain and surplus. */
    SINCE_GOAL_CHANGE(days = null),
    SINCE_START(days = null),

    /** Calories eaten against maintenance, at 7,700 kcal a kg. */
    FROM_CALORIES(days = null)
}

@Serializable
data class ForecastSettings(
    val basis: ForecastBasis = ForecastBasis.LAST_4_WEEKS,
    /** Show the earliest and latest date, not just the most likely one. */
    val isRangeShown: Boolean = true
)

/** A milestone the user added. [byDate] is optional; with one, it shows whether they're on track. */
@Serializable
data class CustomMilestone(
    val id: Long,
    val name: String,
    val weight: Double,
    val byDate: LocalDate? = null
)

sealed interface ForecastStatus {

    /** [date] is the most likely; [earliest] and [latest] come from the pace ±[FORECAST_RANGE_SPREAD]. */
    data class OnTheWay(
        val date: LocalDate,
        val earliest: LocalDate,
        val latest: LocalDate,
        val kgPerWeek: Double
    ) : ForecastStatus

    /** Under [FORECAST_MIN_WEIGH_INS] weigh-ins. [roughDate] comes from calories alone, when there are enough. */
    data class Collecting(val roughDate: LocalDate?) : ForecastStatus

    /** Trend weight has barely moved for [FORECAST_FLAT_DAYS] days. */
    data object Flat : ForecastStatus

    /** The trend is going away from the goal, or so slowly it would take years. */
    data object WrongWay : ForecastStatus

    data class Reached(val date: LocalDate) : ForecastStatus
}

/**
 * When the user reaches their goal weight. Weights are in kg; [direction] is -1 when losing toward the goal and 1
 * when gaining.
 */
data class GoalForecast(
    val today: LocalDate,
    val startWeight: Double,
    val startDate: LocalDate,
    val targetWeight: Double,
    /** Today's trend weight: the [FORECAST_TREND_DAYS]-day moving average at the latest weigh-in. */
    val trendWeight: Double,
    val direction: Int,
    val weighIns: Int,
    val maintenance: Int,
    /** Signed kg a week by each basis; null when a basis has too little to go on. */
    val paceByBasis: Map<ForecastBasis, Double?>,
    /** The basis asked for, or the next one that has data. */
    val basis: ForecastBasis,
    val status: ForecastStatus,
    /** The trend weight since the start, for the chart. */
    val trend: List<Weight>,
    /** The weigh-ins since the start, for the chart. */
    val weights: List<Weight>,
    /** When the goal last changed between deficit, maintain and surplus, and to what; null when it never has. */
    val goalChange: GoalChange? = null
) {
    /** Since your goal changed has [FORECAST_GOAL_CHANGE_DAYS] days to go on; until then it uses the last 4 weeks. */
    val isGoalChangeReady: Boolean
        get() = goalChange != null && today.toEpochDays() - goalChange.date.toEpochDays() >= FORECAST_GOAL_CHANGE_DAYS

    /** The trend weight on [date]: the moving average there, or the last one before it. */
    fun trendOn(date: LocalDate): Double? = trend.lastOrNull { it.date <= date }?.weight

    /** The forecast at [basis]'s pace [multiplier] times over, never past the goal. */
    fun projectedOn(date: LocalDate, multiplier: Double): Double? {
        val pace = pace ?: return null
        val days = date.toEpochDays() - today.toEpochDays()
        val projected = trendWeight + pace * multiplier * days / DAYS_PER_WEEK
        return if (direction < 0) max(targetWeight, projected) else min(targetWeight, projected)
    }

    /** Signed kg a week by [basis]. */
    val pace: Double? get() = paceByBasis[basis]

    val remainingKg: Double get() = max(0.0, (targetWeight - trendWeight) * direction)

    val changedKg: Double get() = max(0.0, (trendWeight - startWeight) * direction)

    val progress: Float
        get() {
            val total = abs(targetWeight - startWeight)
            return if (total == 0.0) 1f else (changedKg / total).toFloat().coerceIn(0f, 1f)
        }

    /** The most likely date at [basis]'s pace, or null when it never gets there. */
    fun dateFor(basis: ForecastBasis): LocalDate? =
        paceByBasis[basis]?.let { dateAt(kgPerWeekToward = it * direction) }

    /** The day [kgPerWeekToward] reaches the goal, or null when it's too slow or the wrong way. */
    fun dateAt(kgPerWeekToward: Double): LocalDate? = dateFor(remaining = remainingKg, kgPerWeekToward = kgPerWeekToward)

    /** The fastest pace suggested: [FORECAST_SAFE_WEEKLY_SHARE] of trend weight a week. */
    val safeKgPerWeek: Double get() = trendWeight * FORECAST_SAFE_WEEKLY_SHARE

    /** The trend weight expected on [date] at [basis]'s pace, never past the goal. */
    fun projectedOn(date: LocalDate): Double? = projectedOn(date = date, multiplier = 1.0)

    internal fun dateFor(remaining: Double, kgPerWeekToward: Double): LocalDate? {
        if (kgPerWeekToward < MIN_KG_PER_WEEK) return null
        val days = ceil(remaining / kgPerWeekToward * DAYS_PER_WEEK)
        if (days > MAX_FORECAST_DAYS) return null
        return today.plus(period = DatePeriod(days = days.toInt()))
    }
}

/**
 * The forecast to [targetWeight] from [startWeight] on [startDate], or null without a goal weight or any weigh-ins.
 * [maintenance] is the daily burn, for [ForecastBasis.FROM_CALORIES].
 */
fun goalForecast(
    today: LocalDate,
    weights: List<Weight>,
    foodLogs: List<FoodLog>,
    targetWeight: Double?,
    startWeight: Double?,
    startDate: LocalDate?,
    maintenance: Int,
    basis: ForecastBasis,
    goalChange: GoalChange? = null
): GoalForecast? {
    targetWeight ?: return null
    val sorted = weights.filter { it.date <= today }.sortedBy { it.date }
    if (sorted.isEmpty()) return null
    val start = startDate ?: sorted.first().date
    val startKg = startWeight ?: sorted.first().weight
    val direction = if (targetWeight < startKg) -1 else 1
    val movingAverage = sorted.movingAverage(windowDays = FORECAST_TREND_DAYS)
    val trendWeight = movingAverage.last().weight

    val paceByBasis = ForecastBasis.entries.associateWith { entry ->
        when (entry) {
            ForecastBasis.LAST_2_WEEKS, ForecastBasis.LAST_4_WEEKS ->
                weeklySlope(weights = sorted, from = today.minus(period = DatePeriod(days = entry.days!!)), minSpanDays = entry.days / 2)
            ForecastBasis.SINCE_GOAL_CHANGE -> null
            ForecastBasis.SINCE_START -> weeklySlope(weights = sorted, from = start, minSpanDays = DAYS_PER_WEEK)
            ForecastBasis.FROM_CALORIES -> caloriePace(today = today, foodLogs = foodLogs, maintenance = maintenance)
        }
    }
    val isGoalChangeReady = goalChange != null &&
        today.toEpochDays() - goalChange.date.toEpochDays() >= FORECAST_GOAL_CHANGE_DAYS
    // Until there are two weeks since the change, this option shows the last 4 weeks.
    val sinceGoalChange = goalChange?.let {
        if (isGoalChangeReady) {
            weeklySlope(weights = sorted, from = it.date, minSpanDays = FORECAST_GOAL_CHANGE_DAYS / 2)
        } else {
            paceByBasis[ForecastBasis.LAST_4_WEEKS]
        }
    }
    val paces = paceByBasis + (ForecastBasis.SINCE_GOAL_CHANGE to sinceGoalChange)
    val effectiveBasis = sequenceOf(basis, ForecastBasis.LAST_4_WEEKS, ForecastBasis.SINCE_START, ForecastBasis.LAST_2_WEEKS)
        .firstOrNull { paces[it] != null } ?: basis

    val base = GoalForecast(
        today = today,
        startWeight = startKg,
        startDate = start,
        targetWeight = targetWeight,
        trendWeight = trendWeight,
        direction = direction,
        weighIns = sorted.size,
        maintenance = maintenance,
        paceByBasis = paces,
        basis = effectiveBasis,
        status = ForecastStatus.WrongWay,
        trend = movingAverage.filter { it.date >= start }.ifEmpty { listOf(movingAverage.last()) },
        weights = sorted.filter { it.date >= start },
        goalChange = goalChange
    )
    val status = when {
        (targetWeight - trendWeight) * direction <= 0 -> ForecastStatus.Reached(
            date = movingAverage.firstOrNull { it.date >= start && (targetWeight - it.weight) * direction <= 0 }?.date ?: today
        )
        sorted.size < FORECAST_MIN_WEIGH_INS -> ForecastStatus.Collecting(
            roughDate = paces[ForecastBasis.FROM_CALORIES]?.let { base.dateAt(kgPerWeekToward = it * direction) }
        )
        isFlat(weights = sorted, movingAverage = movingAverage, today = today) -> ForecastStatus.Flat
        else -> {
            val toward = (paces[effectiveBasis] ?: 0.0) * direction
            val date = base.dateAt(kgPerWeekToward = toward)
            if (date == null) {
                ForecastStatus.WrongWay
            } else {
                ForecastStatus.OnTheWay(
                    date = date,
                    earliest = base.dateAt(kgPerWeekToward = toward * (1 + FORECAST_RANGE_SPREAD)) ?: date,
                    latest = base.dateAt(kgPerWeekToward = toward * (1 - FORECAST_RANGE_SPREAD)) ?: date,
                    kgPerWeek = toward
                )
            }
        }
    }
    return base.copy(status = status)
}

/** Flat when the weigh-ins cover [FORECAST_FLAT_DAYS] days and trend weight moved under [FORECAST_FLAT_KG] in them. */
private fun isFlat(weights: List<Weight>, movingAverage: List<Weight>, today: LocalDate): Boolean {
    val from = today.minus(period = DatePeriod(days = FORECAST_FLAT_DAYS))
    if (weights.first().date > from) return false
    val window = movingAverage.filter { it.date >= from }
    if (window.size < 2) return false
    return window.maxOf { it.weight } - window.minOf { it.weight } < FORECAST_FLAT_KG
}

/** Least-squares slope of the weigh-ins from [from], in kg a week; null unless they span [minSpanDays]. */
private fun weeklySlope(weights: List<Weight>, from: LocalDate, minSpanDays: Int): Double? {
    val window = weights.filter { it.date >= from }
    if (window.size < 3) return null
    val span = window.last().date.toEpochDays() - window.first().date.toEpochDays()
    if (span < minSpanDays) return null
    val points = window.map { it.date.toEpochDays().toDouble() to it.weight }
    val meanDay = points.sumOf { it.first } / points.size
    val meanWeight = points.sumOf { it.second } / points.size
    val variance = points.sumOf { (day, _) -> (day - meanDay) * (day - meanDay) }
    if (variance == 0.0) return null
    return points.sumOf { (day, weight) -> (day - meanDay) * (weight - meanWeight) } / variance * DAYS_PER_WEEK
}

/** The weight change a week that the average logged intake over the last 4 weeks stands for. */
private fun caloriePace(today: LocalDate, foodLogs: List<FoodLog>, maintenance: Int): Double? {
    val from = today.minus(period = DatePeriod(days = CALORIE_WINDOW_DAYS))
    val daily = foodLogs.filter { it.date >= from && it.date < today }
        .groupBy { it.date }
        .map { (_, logs) -> logs.sumOf { it.calories } }
    if (daily.size < FORECAST_MIN_LOGGED_DAYS) return null
    return weeklyChangeAt(kcal = daily.average(), maintenance = maintenance)
}

/** The signed weight change a week from eating [kcal] a day against [maintenance]. */
fun weeklyChangeAt(kcal: Double, maintenance: Int): Double = (kcal - maintenance) * DAYS_PER_WEEK / KCAL_PER_KG

/** The daily calories that change weight by [kgPerWeek] (signed) against [maintenance]. */
fun kcalForWeeklyChange(kgPerWeek: Double, maintenance: Int): Double = maintenance + kgPerWeek * KCAL_PER_KG / DAYS_PER_WEEK

/** What eating [kcal] a day would do. */
data class WhatIfResult(
    val kcal: Int,
    /** Kg a week toward the goal; 0 or less never gets there. */
    val kgPerWeek: Double,
    val date: LocalDate?,
    val isTooFast: Boolean,
    /** The fastest daily target suggested, on the [FORECAST_KCAL_STEP] grid, and the date it gives. */
    val safeKcal: Int,
    val safeDate: LocalDate?
)

/** The calories What if can be dragged between: from the goal side of maintenance, to maintenance. */
fun GoalForecast.whatIfRange(): IntRange {
    val step = FORECAST_KCAL_STEP
    return if (direction < 0) {
        val low = ceilTo(value = max(FORECAST_MIN_KCAL, maintenance - FORECAST_KCAL_SPAN), step = step)
        low..max(low, floorTo(value = maintenance, step = step))
    } else {
        ceilTo(value = maintenance, step = step)..floorTo(value = maintenance + FORECAST_KCAL_SPAN, step = step)
    }
}

fun GoalForecast.whatIf(kcal: Int): WhatIfResult {
    val toward = weeklyChangeAt(kcal = kcal.toDouble(), maintenance = maintenance) * direction
    val safeRaw = kcalForWeeklyChange(kgPerWeek = safeKgPerWeek * direction, maintenance = maintenance)
    val safeKcal = if (direction < 0) {
        ceilTo(value = ceil(safeRaw).toInt(), step = FORECAST_KCAL_STEP)
    } else {
        floorTo(value = floor(safeRaw).toInt(), step = FORECAST_KCAL_STEP)
    }
    return WhatIfResult(
        kcal = kcal,
        kgPerWeek = toward,
        date = dateAt(kgPerWeekToward = toward),
        isTooFast = toward > safeKgPerWeek + 1e-6,
        safeKcal = safeKcal,
        safeDate = dateAt(kgPerWeekToward = weeklyChangeAt(kcal = safeKcal.toDouble(), maintenance = maintenance) * direction)
    )
}

/** What reaching the goal by [date] would take. */
data class DeadlinePlan(
    val date: LocalDate,
    val weeks: Double,
    /** Kg a week toward the goal needed. */
    val kgPerWeek: Double,
    /** The daily calories that pace needs, to 10 kcal. */
    val kcal: Int,
    val isTooSoon: Boolean,
    /** The earliest date at the fastest suggested pace. */
    val earliestSafeDate: LocalDate?,
    /** Where the fastest suggested pace gets to by [date]. */
    val realisticWeight: Double
)

fun GoalForecast.deadlinePlan(date: LocalDate): DeadlinePlan {
    val days = max(1L, date.toEpochDays() - today.toEpochDays())
    val weeks = days.toDouble() / DAYS_PER_WEEK
    val needed = remainingKg / weeks
    val kcal = kcalForWeeklyChange(kgPerWeek = needed * direction, maintenance = maintenance)
    return DeadlinePlan(
        date = date,
        weeks = weeks,
        kgPerWeek = needed,
        kcal = (kcal / 10).roundToInt() * 10,
        isTooSoon = needed > safeKgPerWeek,
        earliestSafeDate = dateAt(kgPerWeekToward = safeKgPerWeek),
        realisticWeight = trendWeight + safeKgPerWeek * weeks * direction
    )
}

/** When the goal last changed between deficit, maintain and surplus, and what it changed to. */
data class GoalChange(val date: LocalDate, val goal: Goal)

/** A dated milestone is on track when the trend gets under its weight by its date. */
enum class MilestoneTrack { ON_TRACK, BEHIND }

/** A milestone the user added, with where the trend stands against it. */
data class Milestone(
    val custom: CustomMilestone,
    val reachedOn: LocalDate?,
    /** When the trend gets there; null once reached or with no date to give. */
    val expectedOn: LocalDate?,
    /** Only for milestones with a date that aren't reached yet. */
    val track: MilestoneTrack? = null,
    /** How far the forecast for its date misses its weight, in kg, when [track] is behind. */
    val behindByKg: Double = 0.0
) {
    val weight: Double get() = custom.weight

    val isReached: Boolean get() = reachedOn != null
}

/** The user's milestones in the order the trend reaches them. */
fun GoalForecast.milestones(custom: List<CustomMilestone>): List<Milestone> =
    custom.map { milestone(custom = it) }.sortedBy { (it.weight - startWeight) * direction }

private fun GoalForecast.milestone(custom: CustomMilestone): Milestone {
    val weight = custom.weight
    val reachedOn = trend.firstOrNull { (it.weight - weight) * direction >= 0 }?.date
    val remaining = (trendWeight - weight) * -direction
    val expectedOn = if (reachedOn == null && status is ForecastStatus.OnTheWay) {
        dateFor(remaining = remaining, kgPerWeekToward = status.kgPerWeek)
    } else {
        null
    }
    val byDate = custom.byDate
    val projected = byDate?.let { projectedOn(date = maxOf(it, today)) } ?: trendWeight
    // Positive when the forecast for its date is past the milestone weight.
    val missBy = (weight - projected) * -direction
    val track = when {
        byDate == null || reachedOn != null -> null
        byDate < today -> MilestoneTrack.BEHIND
        projectedOn(date = byDate) == null -> null
        missBy >= -1e-9 -> MilestoneTrack.ON_TRACK
        else -> MilestoneTrack.BEHIND
    }
    return Milestone(
        custom = custom,
        reachedOn = reachedOn,
        expectedOn = expectedOn,
        track = track,
        behindByKg = if (track == MilestoneTrack.BEHIND) max(0.0, -missBy) else 0.0
    )
}

private fun ceilTo(value: Int, step: Int): Int = ceil(value.toDouble() / step).toInt() * step

private fun floorTo(value: Int, step: Int): Int = floor(value.toDouble() / step).toInt() * step
