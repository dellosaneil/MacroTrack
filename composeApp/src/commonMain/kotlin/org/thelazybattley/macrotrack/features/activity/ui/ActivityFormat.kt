package org.thelazybattley.macrotrack.features.activity.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.activity_default_name
import macrotrack.composeapp.generated.resources.duration_hours_minutes
import macrotrack.composeapp.generated.resources.duration_minutes
import macrotrack.composeapp.generated.resources.time_am
import macrotrack.composeapp.generated.resources.time_pm
import macrotrack.composeapp.generated.resources.today
import macrotrack.composeapp.generated.resources.unit_feet
import macrotrack.composeapp.generated.resources.unit_km
import macrotrack.composeapp.generated.resources.unit_km_per_hour
import macrotrack.composeapp.generated.resources.unit_meters
import macrotrack.composeapp.generated.resources.unit_miles
import macrotrack.composeapp.generated.resources.unit_mph
import macrotrack.composeapp.generated.resources.unit_per_km
import macrotrack.composeapp.generated.resources.unit_per_mile
import macrotrack.composeapp.generated.resources.yesterday
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.core.toShortDayMonthDay
import org.thelazybattley.macrotrack.domain.model.ActivitySession
import org.thelazybattley.macrotrack.domain.model.FEET_PER_METER
import org.thelazybattley.macrotrack.domain.model.METERS_PER_KM
import org.thelazybattley.macrotrack.domain.model.METERS_PER_MILE
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** A km or a mile, whichever the user reads distances in. */
@Composable
@ReadOnlyComposable
internal fun distanceUnitMeters(): Double = if (LocalUnitSystem.current == UnitSystem.IMPERIAL) METERS_PER_MILE else METERS_PER_KM

/** "4.15", in km or miles. */
@Composable
@ReadOnlyComposable
internal fun distanceValue(meters: Double, decimals: Int = 2): String = (meters / distanceUnitMeters()).toFixed(decimals = decimals)

@Composable
internal fun distanceUnit(): String =
    stringResource(resource = if (LocalUnitSystem.current == UnitSystem.IMPERIAL) Res.string.unit_miles else Res.string.unit_km)

/** "4.15 km". */
@Composable
internal fun distanceText(meters: Double, decimals: Int = 2): String = distanceValue(meters = meters, decimals = decimals) + " " + distanceUnit()

/** Pace per km or mile as "11:26", from seconds per km. */
@Composable
@ReadOnlyComposable
internal fun paceValue(secondsPerKm: Double): String = minutesSeconds(seconds = secondsPerKm * distanceUnitMeters() / METERS_PER_KM)

@Composable
internal fun paceUnit(): String =
    stringResource(resource = if (LocalUnitSystem.current == UnitSystem.IMPERIAL) Res.string.unit_per_mile else Res.string.unit_per_km)

/** "5.2 km/h" or "3.3 mph". */
@Composable
internal fun speedText(meters: Double, seconds: Int): String {
    val perHour = if (seconds > 0) meters / seconds * 3600 / distanceUnitMeters() else 0.0
    val unit = if (LocalUnitSystem.current == UnitSystem.IMPERIAL) Res.string.unit_mph else Res.string.unit_km_per_hour
    return perHour.toFixed(decimals = 1) + " " + stringResource(resource = unit)
}

/** Metres or feet climbed, rounded. */
@Composable
@ReadOnlyComposable
internal fun climbValue(meters: Double): String =
    (if (LocalUnitSystem.current == UnitSystem.IMPERIAL) meters * FEET_PER_METER else meters).roundToInt().toString()

@Composable
internal fun climbUnit(): String =
    stringResource(resource = if (LocalUnitSystem.current == UnitSystem.IMPERIAL) Res.string.unit_feet else Res.string.unit_meters)

/** "47:30", or "1:02:10" past an hour. */
internal fun clockDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    val mm = minutes.toString().padStart(length = 2, padChar = '0')
    val ss = secs.toString().padStart(length = 2, padChar = '0')
    return if (hours > 0) "$hours:$mm:$ss" else "$minutes:$ss"
}

/** "11:26" from seconds. */
internal fun minutesSeconds(seconds: Double): String {
    val total = seconds.roundToLong()
    return "${total / 60}:" + (total % 60).toString().padStart(length = 2, padChar = '0')
}

/** "1 h 12 m" or "47 min". */
@Composable
internal fun shortDuration(minutes: Int): String = if (minutes >= 60) {
    stringResource(resource = Res.string.duration_hours_minutes, minutes / 60, minutes % 60)
} else {
    stringResource(resource = Res.string.duration_minutes, minutes)
}

/** "7:12 AM". */
@Composable
internal fun clockTime(time: LocalDateTime): String {
    val hour = time.hour % 12
    return stringResource(
        resource = if (time.hour < 12) Res.string.time_am else Res.string.time_pm,
        if (hour == 0) 12 else hour,
        time.minute.toString().padStart(length = 2, padChar = '0')
    )
}

/** The hour of the day it is now, 0 to 23. */
@OptIn(ExperimentalTime::class)
internal fun currentHour(): Int = Clock.System.now().toLocalDateTime(timeZone = TimeZone.currentSystemDefault()).hour

/** "Today", "Yesterday" or "Wed, Oct 7". */
@Composable
internal fun relativeDay(date: LocalDate): String {
    val today = getCurrentDate()
    return when (date) {
        today -> stringResource(resource = Res.string.today)
        today.minus(period = DatePeriod(days = 1)) -> stringResource(resource = Res.string.yesterday)
        else -> date.toShortDayMonthDay()
    }
}

/** The name the user gave it, or the type and date: "Walk · Oct 8". */
@Composable
internal fun ActivitySession.displayName(): String =
    name ?: stringResource(resource = Res.string.activity_default_name, stringResource(resource = type.title), date.toAbbreviatedMonthDay())

/** A fixed number of decimals without a locale, like "4.15". */
internal fun Double.toFixed(decimals: Int): String {
    if (decimals <= 0) return roundToLong().toString()
    var factor = 1L
    repeat(times = decimals) { factor *= 10 }
    val scaled = (abs(x = this) * factor).roundToLong()
    val sign = if (this < 0 && scaled != 0L) "-" else ""
    return sign + (scaled / factor).toString() + "." + (scaled % factor).toString().padStart(length = decimals, padChar = '0')
}
