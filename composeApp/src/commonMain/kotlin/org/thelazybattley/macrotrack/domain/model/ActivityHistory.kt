package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** The time span Activity history looks at. [days] is null for everything since the first recording. */
enum class HistoryRange(val days: Int?) {
    WEEK(days = 7),
    MONTH(days = 30),
    QUARTER(days = 90),
    ALL(days = null)
}

enum class HistoryFilter {
    ALL, WALKS, RUNS;

    fun matches(type: ActivityType): Boolean = when (this) {
        ALL -> true
        WALKS -> type == ActivityType.WALK
        RUNS -> type == ActivityType.RUN
    }
}

enum class HistoryMetric { STEPS, KCAL, TIME, DISTANCE }

/** Days from [start] to [end], both included. */
data class HistoryPeriod(val start: LocalDate, val end: LocalDate) {
    val days: Int get() = start.daysUntil(other = end) + 1

    operator fun contains(date: LocalDate): Boolean = date in start..end
}

/** The period [this] range covers up to [today]; All time starts at [firstDate], or today with nothing recorded. */
fun HistoryRange.period(today: LocalDate, firstDate: LocalDate?): HistoryPeriod = when (val days = days) {
    null -> HistoryPeriod(start = minOf(firstDate ?: today, today), end = today)
    else -> HistoryPeriod(start = today.minus(period = DatePeriod(days = days - 1)), end = today)
}

/** The same number of days just before [period], to compare with; All time has nothing before it. */
fun HistoryRange.previous(period: HistoryPeriod): HistoryPeriod? = if (days == null) {
    null
} else {
    HistoryPeriod(start = period.start.minus(period = DatePeriod(days = period.days)), end = period.start.minus(period = DatePeriod(days = 1)))
}

/** Totals for a set of walks and runs. Calories need the profile, so they're null without one. */
data class HistoryTotals(
    val count: Int,
    val walks: Int,
    val runs: Int,
    val steps: Int,
    val kcal: Int?,
    val movingSeconds: Int,
    val meters: Double,
    val climbMeters: Double
) {
    fun value(metric: HistoryMetric): Double = when (metric) {
        HistoryMetric.STEPS -> steps.toDouble()
        HistoryMetric.KCAL -> (kcal ?: 0).toDouble()
        HistoryMetric.TIME -> movingSeconds.toDouble()
        HistoryMetric.DISTANCE -> meters
    }

    companion object {
        fun of(sessions: List<ActivitySession>, profile: BurnProfile?) = HistoryTotals(
            count = sessions.size,
            walks = sessions.count { it.type == ActivityType.WALK },
            runs = sessions.count { it.type == ActivityType.RUN },
            steps = sessions.sumOf { it.steps },
            kcal = profile?.let { sessions.sumOf { session -> it.kcal(session = session) } },
            movingSeconds = sessions.sumOf { it.movingSeconds },
            meters = sessions.sumOf { it.distanceMeters },
            climbMeters = sessions.sumOf { it.climbMeters }
        )
    }
}

/** Change against the period before, in percent; null when there was nothing before to compare with. */
fun percentChange(current: Double, previous: Double): Int? =
    if (previous <= 0) null else ((current - previous) / previous * 100).let { kotlin.math.round(x = it).toInt() }

/** The longest run of days in a row with at least one walk or run, inside [period]. */
fun longestStreak(dates: Set<LocalDate>, period: HistoryPeriod): Int {
    var longest = 0
    var current = 0
    var date = period.start
    while (date <= period.end) {
        if (date in dates) {
            current++
            longest = maxOf(longest, current)
        } else {
            current = 0
        }
        date = date.plus(period = DatePeriod(days = 1))
    }
    return longest
}

/** Average seconds per km over [sessions], or null with no distance. */
fun averagePace(sessions: List<ActivitySession>): Double? {
    val km = sessions.sumOf { it.distanceKm }
    return if (km <= 0) null else sessions.sumOf { it.movingSeconds } / km
}

/** A bar on the history chart: one day, week or month. */
data class HistoryBucket(
    val start: LocalDate,
    val end: LocalDate,
    val sessions: List<ActivitySession>
)

enum class BucketSize { DAY, WEEK, MONTH }

/** Days for 7 and 30 days, weeks for 90 days, months for all time. */
val HistoryRange.bucketSize: BucketSize
    get() = when (this) {
        HistoryRange.WEEK, HistoryRange.MONTH -> BucketSize.DAY
        HistoryRange.QUARTER -> BucketSize.WEEK
        HistoryRange.ALL -> BucketSize.MONTH
    }

/** [sessions] split into bars across [period], oldest first. Weeks end on the period's last day. */
fun buckets(sessions: List<ActivitySession>, period: HistoryPeriod, size: BucketSize): List<HistoryBucket> = when (size) {
    BucketSize.DAY -> (0 until period.days).map { offset ->
        val day = period.start.plus(period = DatePeriod(days = offset))
        HistoryBucket(start = day, end = day, sessions = sessions.filter { it.date == day })
    }
    BucketSize.WEEK -> {
        val weeks = (period.days + 6) / 7
        (weeks - 1 downTo 0).map { weeksBack ->
            val end = period.end.minus(period = DatePeriod(days = weeksBack * 7))
            val start = maxOf(period.start, end.minus(period = DatePeriod(days = 6)))
            HistoryBucket(start = start, end = end, sessions = sessions.filter { it.date in start..end })
        }
    }
    BucketSize.MONTH -> {
        val months = mutableListOf<HistoryBucket>()
        var start = period.start
        while (start <= period.end) {
            val nextMonth = LocalDate(year = start.year, month = start.month, day = 1).plus(period = DatePeriod(months = 1))
            val end = minOf(period.end, nextMonth.minus(period = DatePeriod(days = 1)))
            months += HistoryBucket(start = start, end = end, sessions = sessions.filter { it.date in start..end })
            start = nextMonth
        }
        months
    }
}

/** Personal bests over everything recorded; each is null until there's something to hold it. */
data class PersonalBests(
    val longestWalk: ActivitySession?,
    val longestRun: ActivitySession?,
    val fastestRun: ActivitySession?,
    val mostBurned: ActivitySession?,
    val mostSteps: ActivitySession?
) {
    companion object {
        /** A run must be at least this long to count as fastest, so a sprint to the bus doesn't. */
        private const val MIN_FASTEST_RUN_METERS = 1_000.0

        fun of(sessions: List<ActivitySession>, profile: BurnProfile?): PersonalBests {
            val walks = sessions.filter { it.type == ActivityType.WALK }
            val runs = sessions.filter { it.type == ActivityType.RUN }
            return PersonalBests(
                longestWalk = walks.maxByOrNull { it.distanceMeters },
                longestRun = runs.maxByOrNull { it.distanceMeters },
                fastestRun = runs.filter { it.distanceMeters >= MIN_FASTEST_RUN_METERS }.minByOrNull { it.paceSecondsPerKm ?: Double.MAX_VALUE },
                mostBurned = profile?.let { sessions.maxByOrNull { session -> it.kcal(session = session) } },
                mostSteps = sessions.maxByOrNull { it.steps }
            )
        }
    }
}

/** Blocks of 7 days counting back from [today]: 0 is this week, 1 last week. */
fun weeksAgo(date: LocalDate, today: LocalDate): Int = date.daysUntil(other = today) / 7
