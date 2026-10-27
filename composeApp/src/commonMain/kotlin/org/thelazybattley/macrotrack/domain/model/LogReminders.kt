package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** After this many days in a row with nothing logged, reminders pause (when the user lets them ease off). */
const val LOG_REMINDER_BREAK_DAYS = 3

/** How long "Later" puts off the weigh-in reminder. */
const val LOG_REMINDER_LATER_MINUTES = 60

/** Reminder times move in steps this long. */
const val LOG_REMINDER_STEP_MINUTES = 15

/** The usual logging time looks back this many days and needs a meal logged on at least [USUAL_MIN_DAYS] of them. */
private const val USUAL_WINDOW_DAYS = 14
private const val USUAL_MIN_DAYS = 3

/** What a reminder is about: a meal that isn't logged, today's weight, or asking whether to keep reminders after a break. */
enum class LogReminderKind {
    BREAKFAST, LUNCH, DINNER, SNACK, WEIGHT, PAUSED;

    val mealType: MealType? get() = MealType.entries.find { it.name == name }

    companion object {
        fun of(mealType: MealType): LogReminderKind = valueOf(value = mealType.name)
    }
}

@Serializable
data class MealReminder(val isOn: Boolean, val minutes: Int)

/** [days] are ISO day numbers, Monday = 1. */
@Serializable
data class WeightReminder(
    val isOn: Boolean = true,
    val minutes: Int = 9 * 60,
    val days: Set<Int> = DayOfWeek.entries.map { it.isoDayNumber }.toSet()
) {
    fun isOnDay(dayOfWeek: DayOfWeek): Boolean = dayOfWeek.isoDayNumber in days
}

/**
 * Missed-log reminders: at each time the log is checked, and a reminder goes out only when that meal or today's
 * weight isn't there yet. Times are minutes after midnight. [activeSince] is the day reminders were last turned on,
 * changed or kept after a break; a break is counted from there when nothing has been logged since.
 */
@Serializable
data class LogReminderSettings(
    val isOn: Boolean = false,
    val meals: Map<MealType, MealReminder> = DEFAULT_MEAL_REMINDERS,
    val weight: WeightReminder = WeightReminder(),
    val hasQuickActions: Boolean = true,
    val easesOff: Boolean = true,
    val isQuietOnWeekends: Boolean = false,
    val activeSince: LocalDate? = null
) {
    fun meal(mealType: MealType): MealReminder = meals[mealType] ?: DEFAULT_MEAL_REMINDERS.getValue(mealType)

    /** How many reminders would go out on a day nothing is logged; 0 when they're off. */
    val activeCount: Int
        get() = if (!isOn) 0 else MealType.entries.count { meal(mealType = it).isOn } + if (weight.isOn && weight.days.isNotEmpty()) 1 else 0

    companion object {
        val DEFAULT_MEAL_REMINDERS = mapOf(
            MealType.BREAKFAST to MealReminder(isOn = true, minutes = 10 * 60),
            MealType.LUNCH to MealReminder(isOn = true, minutes = 14 * 60),
            MealType.DINNER to MealReminder(isOn = true, minutes = 20 * 60 + 30),
            MealType.SNACK to MealReminder(isOn = false, minutes = 16 * 60)
        )
    }
}

/** What the notifications have done: when the weigh-in was put off to, and up to when reminders have been sent. */
@Serializable
data class LogReminderState(
    val weightLaterAt: LocalDateTime? = null,
    val sentUntilMillis: Long? = null
)

data class LogReminder(val kind: LogReminderKind, val at: LocalDateTime)

/**
 * The reminders due after [from] and up to [until], soonest first. A meal already logged on a day, or a weight
 * already logged, has none that day. With [LogReminderSettings.easesOff], reminders stop [LOG_REMINDER_BREAK_DAYS]
 * days after the last day anything was logged, and one [LogReminderKind.PAUSED] message goes out the day after.
 */
fun planLogReminders(
    settings: LogReminderSettings,
    state: LogReminderState,
    from: LocalDateTime,
    until: LocalDateTime,
    foodLogs: List<FoodLog>,
    weights: List<Weight>
): List<LogReminder> {
    if (!settings.isOn || until <= from) return emptyList()
    val lastActive = listOfNotNull(foodLogs.maxOfOrNull { it.date }, weights.maxOfOrNull { it.date }, settings.activeSince).maxOrNull()
    val lastReminderDay = lastActive?.takeIf { settings.easesOff }?.plus(period = DatePeriod(days = LOG_REMINDER_BREAK_DAYS))
    val loggedMeals = foodLogs.filter { it.date >= from.date }.map { it.date to it.mealType }.toSet()
    val weighInDays = weights.filter { it.date >= from.date }.map { it.date }.toSet()
    val reminders = generateSequence(seed = from.date) { it.plus(period = DatePeriod(days = 1)) }
        .takeWhile { it <= until.date }
        .flatMap { date ->
            if (lastReminderDay != null && date > lastReminderDay) {
                // One message the day after the break starts, at the earliest reminder time.
                if (date != lastReminderDay.plus(period = DatePeriod(days = 1))) return@flatMap emptySequence()
                val minutes = settings.earliestMinutes() ?: return@flatMap emptySequence()
                return@flatMap sequenceOf(LogReminder(kind = LogReminderKind.PAUSED, at = date.at(minutes = minutes)))
            }
            val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
            val meals = MealType.entries.asSequence()
                .filter { mealType -> settings.meal(mealType = mealType).isOn && !(settings.isQuietOnWeekends && isWeekend) }
                .filter { mealType -> (date to mealType) !in loggedMeals }
                .map { mealType -> LogReminder(kind = LogReminderKind.of(mealType = mealType), at = date.at(minutes = settings.meal(mealType).minutes)) }
            val weight = if (settings.weight.isOn && settings.weight.isOnDay(dayOfWeek = date.dayOfWeek) && date !in weighInDays) {
                val laterAt = state.weightLaterAt?.takeIf { it.date == date }
                sequenceOf(LogReminder(kind = LogReminderKind.WEIGHT, at = laterAt ?: date.at(minutes = settings.weight.minutes)))
            } else {
                emptySequence()
            }
            meals + weight
        }
    return reminders.filter { it.at > from && it.at <= until }.sortedBy { it.at }.toList()
}

private fun LogReminderSettings.earliestMinutes(): Int? =
    (MealType.entries.map { meal(mealType = it) }.filter { it.isOn }.map { it.minutes } +
        listOfNotNull(weight.minutes.takeIf { weight.isOn && weight.days.isNotEmpty() })).minOrNull()

fun LocalDate.at(minutes: Int): LocalDateTime = LocalDateTime(date = this, time = LocalTime(hour = minutes / 60 % 24, minute = minutes % 60))

/**
 * Around when [mealType] usually gets logged, in minutes after midnight rounded to 5: the middle of the first time
 * it was logged on each of the [USUAL_WINDOW_DAYS] days before [today]. Logs added for another day than the one
 * they were added on don't count. Null without enough days to tell.
 */
@OptIn(ExperimentalTime::class)
fun usualLogMinutes(foodLogs: List<FoodLog>, mealType: MealType, today: LocalDate, timeZone: TimeZone): Int? {
    val start = today.minus(period = DatePeriod(days = USUAL_WINDOW_DAYS))
    val firstPerDay = foodLogs.asSequence()
        .filter { it.mealType == mealType && it.date >= start && it.date < today }
        .mapNotNull { log ->
            val loggedAt = Instant.fromEpochMilliseconds(epochMilliseconds = log.loggedAtMillis ?: return@mapNotNull null)
                .toLocalDateTime(timeZone = timeZone)
            if (loggedAt.date == log.date) log.date to loggedAt.hour * 60 + loggedAt.minute else null
        }
        .groupBy(keySelector = { it.first }, valueTransform = { it.second })
        .values.map { it.min() }
        .sorted()
    if (firstPerDay.size < USUAL_MIN_DAYS) return null
    val middle = firstPerDay.size / 2
    val median = if (firstPerDay.size % 2 == 1) firstPerDay[middle].toDouble() else (firstPerDay[middle - 1] + firstPerDay[middle]) / 2.0
    return (median / 5).roundToInt() * 5
}

/** Reminder times to offer: about an hour after the usual time, else ones that suit [kind]. */
fun suggestedReminderMinutes(kind: LogReminderKind, usualMinutes: Int?): List<Int> {
    if (usualMinutes != null) {
        val base = ((usualMinutes + 60) / LOG_REMINDER_STEP_MINUTES.toDouble()).roundToInt() * LOG_REMINDER_STEP_MINUTES
        return listOf(base - 30, base, base + 30).map { (it + MINUTES_PER_DAY) % MINUTES_PER_DAY }
    }
    return when (kind) {
        LogReminderKind.BREAKFAST -> listOf(570, 600, 660)
        LogReminderKind.LUNCH -> listOf(780, 840, 900)
        LogReminderKind.DINNER -> listOf(1170, 1230, 1290)
        LogReminderKind.SNACK -> listOf(900, 960, 1260)
        LogReminderKind.WEIGHT, LogReminderKind.PAUSED -> listOf(450, 540, 600)
    }
}

const val MINUTES_PER_DAY = 24 * 60
