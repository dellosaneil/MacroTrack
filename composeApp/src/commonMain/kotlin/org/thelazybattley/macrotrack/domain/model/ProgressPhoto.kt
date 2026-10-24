package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/** Album names are kept short enough to fit on an album tile. */
const val PHOTO_ALBUM_NAME_MAX = 40

/** How many photos can be brought in from the photo library at once. */
const val PHOTO_PICK_MAX = 10

/** Album reminders go off at 6 PM on the days picked unless another time is chosen; minutes after midnight. */
const val DEFAULT_PHOTO_REMINDER_MINUTES = 18 * 60

/** A weigh-in up to this many days from a photo's date can stand in when that day has none. */
const val NEAREST_WEIGH_IN_DAYS = 3

/**
 * A set of progress photos the user names, like "Push Day Pump Check" or a monthly front, side and back set.
 * [reminderDays] are the days a reminder nudges for a photo, at [reminderMinutes] after midnight.
 */
data class PhotoAlbum(
    val id: Long = 0,
    val name: String,
    val reminderDays: Set<DayOfWeek> = emptySet(),
    val reminderMinutes: Int = DEFAULT_PHOTO_REMINDER_MINUTES,
    val isDateAndWeightShown: Boolean = true,
    val createdAtMillis: Long = 0
)

enum class PhotoPose { FRONT, SIDE, BACK, FLEX }

/**
 * One photo in an album. Its image is [fileName] in the app's own photo folder. The weight shown on it is the one
 * logged on [date], or on [weightDate] when the user picked a nearby weigh-in instead, so it fills in or changes when
 * weights are logged later. [takenOn] is the day a photo from the library was taken, which may differ from [date].
 */
data class ProgressPhoto(
    val id: Long = 0,
    val albumId: Long,
    val date: LocalDate,
    val takenOn: LocalDate? = null,
    val pose: PhotoPose,
    val note: String? = null,
    val weightDate: LocalDate? = null,
    val fileName: String,
    val createdAtMillis: Long = 0
)

/** The weigh-in shown on [photo]: from [ProgressPhoto.weightDate] when one was picked, otherwise from its own date. */
fun weightOnPhoto(photo: ProgressPhoto, weightsByDate: Map<LocalDate, Double>): Weight? {
    val date = photo.weightDate ?: photo.date
    return weightsByDate[date]?.let { Weight(date = date, weight = it) }
}

/**
 * The closest weigh-in within [NEAREST_WEIGH_IN_DAYS] of [date], the day before winning a tie. Null when [date] has
 * its own weigh-in, or none is close enough.
 */
fun nearestWeighIn(date: LocalDate, weightsByDate: Map<LocalDate, Double>): Weight? {
    if (date in weightsByDate) return null
    for (days in 1..NEAREST_WEIGH_IN_DAYS) {
        listOf(date.minus(period = DatePeriod(days = days)), date.plus(period = DatePeriod(days = days))).forEach { day ->
            weightsByDate[day]?.let { return Weight(date = day, weight = it) }
        }
    }
    return null
}

/** The latest waist measurement on or before [date], or null when there's none that early. */
fun waistOn(date: LocalDate, measurements: List<Measurement>): Measurement? =
    measurements.filter { it.type == MeasurementType.WAIST && it.date <= date }.maxByOrNull { it.date }

/** An album's photos in date order; photos on the same day keep the order they were added in. */
fun List<ProgressPhoto>.inDateOrder(): List<ProgressPhoto> = sortedWith(compareBy({ it.date }, { it.createdAtMillis }, { it.id }))

/** Reminder days kept as text, like "MONDAY,THURSDAY". */
fun Set<DayOfWeek>.toReminderText(): String = sortedBy { it.ordinal }.joinToString(separator = ",") { it.name }

fun reminderDaysOf(text: String): Set<DayOfWeek> =
    text.split(",").mapNotNull { name -> DayOfWeek.entries.find { it.name == name.trim() } }.toSet()
