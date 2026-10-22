package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.activity_run
import macrotrack.composeapp.generated.resources.activity_walk
import org.jetbrains.compose.resources.StringResource
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

enum class ActivityType(val title: StringResource) {
    WALK(title = Res.string.activity_walk),
    RUN(title = Res.string.activity_run)
}

/**
 * One GPS fix on a recorded route. [segment] goes up by one after every pause, so the line isn't drawn across
 * the gap and the time spent paused doesn't count.
 */
@Serializable
data class RoutePoint(
    val lat: Double,
    val lon: Double,
    /** Metres above sea level, when the fix had one. */
    val altitude: Double? = null,
    val timeMillis: Long,
    val segment: Int = 0
)

/** A walk or run recorded with the map. Names are the user's; null shows as the type and date. */
data class ActivitySession(
    val id: Long = 0,
    val type: ActivityType,
    val name: String? = null,
    val startMillis: Long,
    val endMillis: Long,
    /** Time spent moving, pauses left out. */
    val movingSeconds: Int,
    val distanceMeters: Double,
    val steps: Int,
    val climbMeters: Double,
    val route: List<RoutePoint> = emptyList()
) {
    val date: LocalDate get() = startMillis.toLocalDateTime().date

    val startTime: LocalDateTime get() = startMillis.toLocalDateTime()

    val endTime: LocalDateTime get() = endMillis.toLocalDateTime()

    val distanceKm: Double get() = distanceMeters / METERS_PER_KM

    /** Seconds per km, or null before there's enough distance to say. */
    val paceSecondsPerKm: Double?
        get() = if (distanceMeters < MIN_PACE_DISTANCE_METERS) null else movingSeconds / distanceKm

    /** Metres per step, from the distance and the steps the phone counted. */
    val stepLengthMeters: Double? get() = if (steps > 0) distanceMeters / steps else null
}

/** Steps counted in one hour of one day, from the phone's motion sensor. */
data class StepHour(
    val date: LocalDate,
    val hour: Int,
    val steps: Int
)

const val METERS_PER_KM = 1000.0
const val METERS_PER_MILE = 1609.344
const val FEET_PER_METER = 3.28084

/** Names can be this long; the save and rename sheets stop typing there. */
const val ACTIVITY_NAME_MAX_LENGTH = 40

/** A pace needs at least this far to mean anything. */
private const val MIN_PACE_DISTANCE_METERS = 50.0

/** Rises smaller than this are GPS noise, not a hill. */
private const val CLIMB_THRESHOLD_METERS = 3.0

/** Fixes worse than this are dropped while recording. */
const val MAX_FIX_ACCURACY_METERS = 30.0

private const val EARTH_RADIUS_METERS = 6_371_000.0

@OptIn(ExperimentalTime::class)
fun Long.toLocalDateTime(): LocalDateTime =
    Instant.fromEpochMilliseconds(epochMilliseconds = this).toLocalDateTime(timeZone = TimeZone.currentSystemDefault())

/** Great-circle distance in metres. */
fun distanceMeters(from: RoutePoint, to: RoutePoint): Double {
    val lat1 = from.lat.toRadians()
    val lat2 = to.lat.toRadians()
    val dLat = lat2 - lat1
    val dLon = (to.lon - from.lon).toRadians()
    val a = sin(x = dLat / 2) * sin(x = dLat / 2) + cos(x = lat1) * cos(x = lat2) * sin(x = dLon / 2) * sin(x = dLon / 2)
    return 2 * EARTH_RADIUS_METERS * asin(x = sqrt(x = a.coerceIn(minimumValue = 0.0, maximumValue = 1.0)))
}

private fun Double.toRadians(): Double = this * PI / 180

/** The route's length, leaving out the gaps between paused segments. */
fun List<RoutePoint>.routeDistance(): Double = zipWithNext().sumOf { (a, b) -> if (a.segment == b.segment) distanceMeters(from = a, to = b) else 0.0 }

/**
 * Metres climbed. A rise only counts once it passes [CLIMB_THRESHOLD_METERS] above the lowest point since the
 * last one, so GPS altitude wobbling up and down on flat ground adds nothing.
 */
fun List<RoutePoint>.routeClimb(): Double {
    var climb = 0.0
    var base: Double? = null
    var segment = -1
    forEach { point ->
        val altitude = point.altitude ?: return@forEach
        if (point.segment != segment || base == null) {
            segment = point.segment
            base = altitude
            return@forEach
        }
        val low = base
        when {
            altitude < low -> base = altitude
            altitude - low >= CLIMB_THRESHOLD_METERS -> {
                climb += altitude - low
                base = altitude
            }
        }
    }
    return climb
}

/** Moving seconds along the route: time between fixes in the same segment. */
fun List<RoutePoint>.movingSeconds(): Int =
    (zipWithNext().sumOf { (a, b) -> if (a.segment == b.segment) b.timeMillis - a.timeMillis else 0L } / 1000).toInt()

/** One split: a full [unitMeters] (km or mile), or the shorter last one, with how long it took. */
data class ActivitySplit(
    val index: Int,
    val meters: Double,
    val seconds: Double
) {
    /** Seconds per unit, so a short last split compares with the full ones. */
    fun pace(unitMeters: Double): Double = if (meters > 0) seconds / (meters / unitMeters) else 0.0
}

/** Splits every [unitMeters], with the leftover as a last, shorter split. */
fun List<RoutePoint>.splits(unitMeters: Double): List<ActivitySplit> {
    val splits = mutableListOf<ActivitySplit>()
    var meters = 0.0
    var seconds = 0.0
    zipWithNext().forEach { (a, b) ->
        if (a.segment != b.segment) return@forEach
        var stepMeters = distanceMeters(from = a, to = b)
        var stepSeconds = (b.timeMillis - a.timeMillis) / 1000.0
        // A long gap between fixes can cross more than one split; share its time out by distance.
        while (meters + stepMeters >= unitMeters && stepMeters > 0) {
            val part = unitMeters - meters
            val partSeconds = stepSeconds * part / stepMeters
            splits += ActivitySplit(index = splits.size + 1, meters = unitMeters, seconds = seconds + partSeconds)
            stepMeters -= part
            stepSeconds -= partSeconds
            meters = 0.0
            seconds = 0.0
        }
        meters += stepMeters
        seconds += stepSeconds
    }
    if (meters >= MIN_PACE_DISTANCE_METERS) {
        splits += ActivitySplit(index = splits.size + 1, meters = meters, seconds = seconds)
    }
    return splits
}

/** A point along the route for the charts: how far in, the pace there and the altitude. */
data class RouteSample(
    val meters: Double,
    /** Seconds per km over the stretch leading here. */
    val paceSecondsPerKm: Double?,
    val altitude: Double?
)

/**
 * About [count] evenly spaced samples along the route. Pace is worked out over each stretch, so one jumpy fix
 * doesn't spike the chart.
 */
fun List<RoutePoint>.samples(count: Int = 24): List<RouteSample> {
    if (size < 2) return emptyList()
    val total = routeDistance()
    if (total <= 0) return emptyList()
    val step = total / count
    val samples = mutableListOf(RouteSample(meters = 0.0, paceSecondsPerKm = null, altitude = first().altitude))
    var meters = 0.0
    var stretchMeters = 0.0
    var stretchSeconds = 0.0
    var next = step
    zipWithNext().forEach { (a, b) ->
        if (a.segment != b.segment) return@forEach
        val d = distanceMeters(from = a, to = b)
        meters += d
        stretchMeters += d
        stretchSeconds += (b.timeMillis - a.timeMillis) / 1000.0
        if (meters >= next) {
            samples += RouteSample(
                meters = meters,
                paceSecondsPerKm = if (stretchMeters > 0) stretchSeconds / (stretchMeters / METERS_PER_KM) else null,
                altitude = b.altitude
            )
            stretchMeters = 0.0
            stretchSeconds = 0.0
            while (next <= meters) next += step
        }
    }
    // The first sample takes the pace of the stretch after it, so the line starts where walking did.
    if (samples.size > 1) samples[0] = samples[0].copy(paceSecondsPerKm = samples[1].paceSecondsPerKm)
    return samples
}
