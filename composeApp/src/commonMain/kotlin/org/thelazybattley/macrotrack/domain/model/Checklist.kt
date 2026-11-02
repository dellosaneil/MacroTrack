package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

enum class ChecklistKind { SUPPLEMENT, HABIT }

/** The parts of the day Today lists items under, in order. */
enum class DayPart { MORNING, AFTERNOON, EVENING, ANY_TIME }

/** When an item is taken or done, the part of the day it's listed under, and the reminder time it starts with. */
enum class ChecklistTime(val part: DayPart, val defaultReminderMinutes: Int) {
    MORNING(part = DayPart.MORNING, defaultReminderMinutes = 8 * 60),
    BREAKFAST(part = DayPart.MORNING, defaultReminderMinutes = 8 * 60),
    LUNCH(part = DayPart.AFTERNOON, defaultReminderMinutes = 12 * 60 + 30),
    DINNER(part = DayPart.EVENING, defaultReminderMinutes = 19 * 60 + 30),
    EVENING(part = DayPart.EVENING, defaultReminderMinutes = 19 * 60),
    BEDTIME(part = DayPart.EVENING, defaultReminderMinutes = 21 * 60 + 30),
    ANY_TIME(part = DayPart.ANY_TIME, defaultReminderMinutes = 12 * 60)
}

/** A supplement's dose unit; capsules and tablets are counted one by one, so a dose takes that many from the stock. */
enum class SupplementUnit(val isCounted: Boolean) {
    MG(isCounted = false), G(isCounted = false), IU(isCounted = false), MCG(isCounted = false),
    CAPSULES(isCounted = true), TABLETS(isCounted = true)
}

/** When a habit counts as done: one tick, a tick after so many minutes, or a tap for each of so many times. */
enum class HabitGoal { TICK, MINUTES, TIMES }

enum class ChecklistIcon { PILL, LEAF, SUN, FISH, DROP, RUN, MOON, BOOK }

/** The day choices; [CUSTOM] is any other set of weekdays. */
enum class ChecklistDays(val days: Set<DayOfWeek>?) {
    EVERY_DAY(days = DayOfWeek.entries.toSet()),
    WEEKDAYS(days = DayOfWeek.entries.take(n = 5).toSet()),
    CUSTOM(days = null);

    companion object {
        fun of(days: Set<DayOfWeek>): ChecklistDays = entries.firstOrNull { it.days == days } ?: CUSTOM
    }
}

const val CHECKLIST_DEFAULT_WARN_AT = 7
const val CHECKLIST_NAME_MAX = 40
const val CHECKLIST_AMOUNT_MAX = 100_000.0
const val CHECKLIST_GOAL_MAX = 999
const val CHECKLIST_STOCK_MAX = 9_999
val CHECKLIST_REFILL_SIZES = listOf(30, 60, 90, 120)

/** Snoozing a reminder brings it back this much later. */
const val CHECKLIST_SNOOZE_MINUTES = 60

/** A supplement or habit on the user's checklist. Everything is the user's own; nothing comes pre-filled. */
data class ChecklistItem(
    val id: Long = 0,
    val kind: ChecklistKind,
    val name: String,
    val icon: ChecklistIcon,
    /** A supplement's dose each time, in [unit]. */
    val amount: Double = 1.0,
    val unit: SupplementUnit = SupplementUnit.CAPSULES,
    val goal: HabitGoal = HabitGoal.TICK,
    /** A habit's minutes or times; unused for [HabitGoal.TICK]. */
    val goalValue: Int = 0,
    val time: ChecklistTime,
    val days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    /** Minutes after midnight, or null with no reminder. */
    val reminderMinutes: Int? = null,
    /** What's left, in capsules or tablets for those and doses otherwise; null when not counted. */
    val stock: Int? = null,
    val stockWarnAt: Int = CHECKLIST_DEFAULT_WARN_AT,
    val refills: Int = 0,
    val sortOrder: Int = 0,
    /** Paused from this day on, keeping its history; null while active. */
    val pausedOn: LocalDate? = null,
    val createdOn: LocalDate
) {
    val isPaused: Boolean get() = pausedOn != null

    /** What one tick takes from [stock]: the dose for capsules and tablets, otherwise one. */
    val perDose: Int get() = if (kind == ChecklistKind.SUPPLEMENT && unit.isCounted) amount.roundToInt().coerceAtLeast(minimumValue = 1) else 1

    /** Taps a [HabitGoal.TIMES] habit needs; everything else is done in one. */
    val target: Int get() = if (kind == ChecklistKind.HABIT && goal == HabitGoal.TIMES) goalValue.coerceAtLeast(minimumValue = 1) else 1

    val isLow: Boolean get() = stock != null && stock <= stockWarnAt

    /** About how many days [stock] lasts on this schedule. */
    val daysLeft: Int? get() = stock?.let { daysFor(stock = it) }

    fun daysFor(stock: Int): Int = stock / perDose * 7 / days.size.coerceAtLeast(minimumValue = 1)

    fun isDueOn(date: LocalDate): Boolean =
        date >= createdOn && date.dayOfWeek in days && (pausedOn == null || date < pausedOn)
}

/** [PARTIAL] is a times habit with some taps but not all. */
enum class ChecklistStatus { DONE, SKIPPED, PARTIAL }

/** What happened to an item on a day; a day with nothing done has no entry. */
data class ChecklistEntry(
    val itemId: Long,
    val date: LocalDate,
    val status: ChecklistStatus,
    /** Taps so far for a times habit. */
    val count: Int = 0,
    val doneAtMillis: Long? = null
)

/**
 * Checklist settings. A new day starts [dayStartMinutes] after midnight, so late-night ticks count for the day
 * before. [snoozedUntil] holds "remind me in 1 hour" times by item id, in epoch milliseconds.
 */
@Serializable
data class ChecklistSettings(
    val isShownOnToday: Boolean = true,
    val dayStartMinutes: Int = 4 * 60,
    val snoozedUntil: Map<Long, Long> = emptyMap()
) {
    companion object {
        val DAY_START_OPTIONS = (0..6).map { it * 60 }
    }
}

/** How an item stands on a day. [TO_DO] is today, still open; [NOT_DUE] isn't on its schedule. */
enum class DayMark { DONE, SKIPPED, MISSED, TO_DO, NOT_DUE, FUTURE }

/** How the days in a stretch went for one item. */
data class Completion(val done: Int, val missed: Int, val skipped: Int, val weekendMisses: Int) {
    val counted: Int get() = done + missed
    val share: Float get() = if (counted == 0) 0f else done.toFloat() / counted

    /** Two or more misses with at least two in three on Saturday or Sunday. */
    val isMostlyWeekends: Boolean get() = missed >= 2 && weekendMisses * 3 >= missed * 2
}

/** One day on the history chart: what was done out of what was due. */
data class ChecklistDayTotal(val date: LocalDate, val done: Int, val due: Int)

/** Today's list at a glance, for the hero card and the Today row. */
data class ChecklistDaySummary(val done: Int, val total: Int, val next: ChecklistItem?, val items: List<ChecklistItem>)

/** Every item and entry, with the day it is now; the screens all work from this. */
data class Checklist(
    val today: LocalDate,
    val items: List<ChecklistItem>,
    val entries: List<ChecklistEntry>,
    val settings: ChecklistSettings
) {
    private val byItem: Map<Long, Map<LocalDate, ChecklistEntry>> =
        entries.groupBy { it.itemId }.mapValues { (_, list) -> list.associateBy { it.date } }

    val activeItems: List<ChecklistItem> get() = items.filter { !it.isPaused }

    fun entryOf(itemId: Long, date: LocalDate): ChecklistEntry? = byItem[itemId]?.get(date)

    fun item(id: Long): ChecklistItem? = items.firstOrNull { it.id == id }

    fun markOn(item: ChecklistItem, date: LocalDate): DayMark {
        val entry = entryOf(itemId = item.id, date = date)
        return when {
            entry?.status == ChecklistStatus.DONE -> DayMark.DONE
            entry?.status == ChecklistStatus.SKIPPED -> DayMark.SKIPPED
            !item.isDueOn(date = date) -> DayMark.NOT_DUE
            date > today -> DayMark.FUTURE
            date == today -> DayMark.TO_DO
            else -> DayMark.MISSED
        }
    }

    /** The items on [date]'s list, by part of the day then the user's order: those due, and any ticked anyway. */
    fun itemsOn(date: LocalDate): List<ChecklistItem> =
        items.filter { it.isDueOn(date = date) || entryOf(itemId = it.id, date = date) != null }
            .sortedWith(compareBy<ChecklistItem> { it.time.part.ordinal }.thenBy { it.sortOrder }.thenBy { it.id })

    fun summaryOn(date: LocalDate): ChecklistDaySummary {
        val list = itemsOn(date = date)
        val marks = list.associateWith { markOn(item = it, date = date) }
        return ChecklistDaySummary(
            done = marks.count { it.value == DayMark.DONE },
            total = marks.count { it.value != DayMark.SKIPPED },
            next = list.firstOrNull { marks[it] == DayMark.TO_DO || marks[it] == DayMark.FUTURE || marks[it] == DayMark.MISSED },
            items = list
        )
    }

    /** Days in a row done up to today. Skipped and unscheduled days pass over; today still open doesn't break it. */
    fun streakOf(item: ChecklistItem): Int {
        var streak = 0
        var date = today
        while (date >= item.createdOn) {
            when (markOn(item = item, date = date)) {
                DayMark.DONE -> streak++
                DayMark.MISSED -> return streak
                else -> Unit
            }
            date = date.minus(period = DatePeriod(days = 1))
        }
        return streak
    }

    fun bestStreakOf(item: ChecklistItem): Int {
        var best = 0
        var run = 0
        var date = item.createdOn
        while (date <= today) {
            when (markOn(item = item, date = date)) {
                DayMark.DONE -> {
                    run++
                    best = maxOf(best, run)
                }
                DayMark.MISSED -> run = 0
                else -> Unit
            }
            date = date.plus(period = DatePeriod(days = 1))
        }
        return best
    }

    /** How [item] went from [from] to [to], both included and no later than today. */
    fun completionOf(item: ChecklistItem, from: LocalDate, to: LocalDate = today): Completion {
        var done = 0
        var missed = 0
        var skipped = 0
        var weekendMisses = 0
        var date = maxOf(from, item.createdOn)
        val last = minOf(to, today)
        while (date <= last) {
            when (markOn(item = item, date = date)) {
                DayMark.DONE -> done++
                DayMark.SKIPPED -> skipped++
                DayMark.MISSED -> {
                    missed++
                    if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) weekendMisses++
                }
                else -> Unit
            }
            date = date.plus(period = DatePeriod(days = 1))
        }
        return Completion(done = done, missed = missed, skipped = skipped, weekendMisses = weekendMisses)
    }

    /** Done out of due for each of the last [days] days, oldest first; skipped items don't count as due. */
    fun dayTotals(days: Int): List<ChecklistDayTotal> = (days - 1 downTo 0).map { back ->
        val date = today.minus(period = DatePeriod(days = back))
        val marks = itemsOn(date = date).map { markOn(item = it, date = date) }
        ChecklistDayTotal(date = date, done = marks.count { it == DayMark.DONE }, due = marks.count { it != DayMark.SKIPPED })
    }

    /** The first active item running low on stock. */
    val lowItem: ChecklistItem? get() = activeItems.sortedBy { it.sortOrder }.firstOrNull { it.isLow }
}
