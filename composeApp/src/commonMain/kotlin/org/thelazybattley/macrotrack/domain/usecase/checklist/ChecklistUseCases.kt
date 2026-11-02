package org.thelazybattley.macrotrack.domain.usecase.checklist

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.checklist_capsules
import macrotrack.composeapp.generated.resources.checklist_doses
import macrotrack.composeapp.generated.resources.checklist_minutes
import macrotrack.composeapp.generated.resources.checklist_reminder_body
import macrotrack.composeapp.generated.resources.checklist_reminder_body_stock
import macrotrack.composeapp.generated.resources.checklist_reminder_title
import macrotrack.composeapp.generated.resources.checklist_tablets
import macrotrack.composeapp.generated.resources.checklist_times
import macrotrack.composeapp.generated.resources.checklist_unit_g
import macrotrack.composeapp.generated.resources.checklist_unit_iu
import macrotrack.composeapp.generated.resources.checklist_unit_mcg
import macrotrack.composeapp.generated.resources.checklist_unit_capsules
import macrotrack.composeapp.generated.resources.checklist_unit_mg
import macrotrack.composeapp.generated.resources.checklist_unit_tablets
import macrotrack.composeapp.generated.resources.checklist_when_any_time
import macrotrack.composeapp.generated.resources.checklist_when_bedtime
import macrotrack.composeapp.generated.resources.checklist_when_breakfast
import macrotrack.composeapp.generated.resources.checklist_when_dinner
import macrotrack.composeapp.generated.resources.checklist_when_evening
import macrotrack.composeapp.generated.resources.checklist_when_lunch
import macrotrack.composeapp.generated.resources.checklist_when_morning
import macrotrack.composeapp.generated.resources.value_unit
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.thelazybattley.macrotrack.core.platform.ChecklistReminder
import org.thelazybattley.macrotrack.core.platform.ChecklistReminderAction
import org.thelazybattley.macrotrack.core.platform.ChecklistReminderMessage
import org.thelazybattley.macrotrack.core.platform.ChecklistReminderScheduler
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.CHECKLIST_SNOOZE_MINUTES
import org.thelazybattley.macrotrack.domain.model.CHECKLIST_STOCK_MAX
import org.thelazybattley.macrotrack.domain.model.Checklist
import org.thelazybattley.macrotrack.domain.model.ChecklistEntry
import org.thelazybattley.macrotrack.domain.model.ChecklistItem
import org.thelazybattley.macrotrack.domain.model.ChecklistKind
import org.thelazybattley.macrotrack.domain.model.ChecklistSettings
import org.thelazybattley.macrotrack.domain.model.ChecklistStatus
import org.thelazybattley.macrotrack.domain.model.ChecklistTime
import org.thelazybattley.macrotrack.domain.model.DayMark
import org.thelazybattley.macrotrack.domain.model.HabitGoal
import org.thelazybattley.macrotrack.domain.model.SupplementUnit
import org.thelazybattley.macrotrack.domain.model.at
import org.thelazybattley.macrotrack.domain.repository.ChecklistRepository
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** A reminder due this long ago or less still goes out, as the alarm can run a little late. */
private const val DUE_WINDOW_MINUTES = 60

/** Resuming a paused item marks at most this many of the paused days as skipped. */
private const val MAX_PAUSED_DAYS = 366

/** The checklist day [atMillis] falls on: one that starts [dayStartMinutes] after midnight. */
@OptIn(ExperimentalTime::class)
fun checklistDay(atMillis: Long, dayStartMinutes: Int): LocalDate =
    Instant.fromEpochMilliseconds(epochMilliseconds = atMillis - dayStartMinutes * 60_000L)
        .toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
        .date

@OptIn(ExperimentalTime::class)
private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

/** Every item and entry, with the checklist day it is now. */
class GetChecklistUseCase(private val repository: ChecklistRepository) {
    operator fun invoke(): Flow<Checklist> = combine(
        flow = repository.getItems(),
        flow2 = repository.getEntries(),
        flow3 = repository.getSettings()
    ) { items, entries, settings ->
        Checklist(
            today = checklistDay(atMillis = nowMillis(), dayStartMinutes = settings.dayStartMinutes),
            items = items,
            entries = entries,
            settings = settings
        )
    }
}

/** What a tap or a menu choice does to an item's day. */
enum class ChecklistAction {
    /** The circle: one more for a times habit, otherwise done, or back to not done when it already was. */
    TAP,
    DONE,
    /** Back to nothing: unticks or unskips. */
    CLEAR,
    SKIP
}

/** Marks an item's day, taking a dose from the stock as it's done and putting it back if that's undone. */
class UpdateChecklistDayUseCase(private val repository: ChecklistRepository, private val telemetry: Telemetry) {
    suspend operator fun invoke(itemId: Long, date: LocalDate, action: ChecklistAction) {
        val item = repository.getItem(id = itemId) ?: return
        val entry = repository.getEntry(itemId = itemId, date = date)
        val wasDone = entry?.status == ChecklistStatus.DONE
        val done = ChecklistEntry(itemId = itemId, date = date, status = ChecklistStatus.DONE, count = item.target, doneAtMillis = nowMillis())
        val next = when (action) {
            ChecklistAction.TAP -> {
                val count = if (entry?.status == ChecklistStatus.PARTIAL) entry.count else 0
                when {
                    wasDone -> null
                    count + 1 < item.target -> ChecklistEntry(itemId = itemId, date = date, status = ChecklistStatus.PARTIAL, count = count + 1)
                    else -> done
                }
            }
            ChecklistAction.DONE -> if (wasDone) return else done
            ChecklistAction.CLEAR -> null
            ChecklistAction.SKIP -> ChecklistEntry(itemId = itemId, date = date, status = ChecklistStatus.SKIPPED)
        }
        if (next == null) repository.deleteEntry(itemId = itemId, date = date) else repository.saveEntry(entry = next)
        val isDone = next?.status == ChecklistStatus.DONE
        if (wasDone != isDone && item.stock != null) {
            val stock = item.stock + if (isDone) -item.perDose else item.perDose
            repository.saveItem(item = item.copy(stock = stock.coerceIn(minimumValue = 0, maximumValue = CHECKLIST_STOCK_MAX)))
        }
        if (isDone) telemetry.logEvent(name = TelemetryEvent.CHECKLIST_TICKED, params = mapOf(TelemetryEvent.PARAM_TYPE to item.kind.name.lowercase()))
        // A reminder snoozed for something now done or skipped has nothing left to remind about.
        if (next?.status == ChecklistStatus.DONE || next?.status == ChecklistStatus.SKIPPED) {
            val settings = repository.getSettings().first()
            if (itemId in settings.snoozedUntil) repository.saveSettings(settings = settings.copy(snoozedUntil = settings.snoozedUntil - itemId))
        }
    }
}

/** Adds an item at the end of the list, starting today, or saves changes to one. Returns its id. */
class SaveChecklistItemUseCase(private val repository: ChecklistRepository, private val telemetry: Telemetry) {
    suspend operator fun invoke(item: ChecklistItem): Long {
        if (item.id != 0L) return repository.saveItem(item = item)
        val items = repository.getItems().first()
        val settings = repository.getSettings().first()
        val id = repository.saveItem(
            item = item.copy(
                sortOrder = (items.maxOfOrNull { it.sortOrder } ?: -1) + 1,
                createdOn = checklistDay(atMillis = nowMillis(), dayStartMinutes = settings.dayStartMinutes)
            )
        )
        telemetry.logEvent(name = TelemetryEvent.CHECKLIST_ITEM_ADDED, params = mapOf(TelemetryEvent.PARAM_TYPE to item.kind.name.lowercase()))
        return id
    }
}

class GetChecklistItemUseCase(private val repository: ChecklistRepository) {
    suspend operator fun invoke(id: Long): ChecklistItem? = repository.getItem(id = id)
}

class DeleteChecklistItemUseCase(private val repository: ChecklistRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteItem(id = id)
}

/**
 * Pauses an item from today, or resumes it. The days it was paused on count as skipped, so they don't show as
 * missed or break its streak.
 */
class SetChecklistItemPausedUseCase(private val repository: ChecklistRepository) {
    suspend operator fun invoke(id: Long, isPaused: Boolean) {
        val item = repository.getItem(id = id) ?: return
        val today = checklistDay(atMillis = nowMillis(), dayStartMinutes = repository.getSettings().first().dayStartMinutes)
        if (isPaused) {
            if (item.pausedOn == null) repository.saveItem(item = item.copy(pausedOn = today))
            return
        }
        val pausedOn = item.pausedOn ?: return
        var date = pausedOn
        var days = 0
        while (date < today && days < MAX_PAUSED_DAYS) {
            if (date.dayOfWeek in item.days && repository.getEntry(itemId = id, date = date) == null) {
                repository.saveEntry(entry = ChecklistEntry(itemId = id, date = date, status = ChecklistStatus.SKIPPED))
            }
            date = date.plus(period = DatePeriod(days = 1))
            days++
        }
        repository.saveItem(item = item.copy(pausedOn = null))
    }
}

/** Puts the items in [orderedIds]' order; any left out keep their place after them. */
class ReorderChecklistUseCase(private val repository: ChecklistRepository) {
    suspend operator fun invoke(orderedIds: List<Long>) {
        val items = repository.getItems().first()
        val ordered = orderedIds.mapNotNull { id -> items.firstOrNull { it.id == id } } + items.filter { it.id !in orderedIds }
        repository.saveItems(items = ordered.mapIndexed { index, item -> item.copy(sortOrder = index) })
    }
}

/** Adds a new bottle of [added] to what's left, and sets when to warn that it's running low. */
class RefillChecklistItemUseCase(private val repository: ChecklistRepository) {
    suspend operator fun invoke(id: Long, added: Int, warnAt: Int) {
        val item = repository.getItem(id = id) ?: return
        repository.saveItem(
            item = item.copy(
                stock = ((item.stock ?: 0) + added).coerceAtMost(maximumValue = CHECKLIST_STOCK_MAX),
                stockWarnAt = warnAt,
                refills = item.refills + 1
            )
        )
    }
}

class SaveChecklistSettingsUseCase(private val repository: ChecklistRepository) {
    suspend operator fun invoke(update: (ChecklistSettings) -> ChecklistSettings) {
        repository.saveSettings(settings = update(repository.getSettings().first()))
    }
}

/** Reminds about the item again in an hour. Returns when, in epoch milliseconds. */
class SnoozeChecklistItemUseCase(private val repository: ChecklistRepository) {
    suspend operator fun invoke(itemId: Long): Long {
        val now = nowMillis()
        val at = now + CHECKLIST_SNOOZE_MINUTES * 60_000L
        val settings = repository.getSettings().first()
        // Snoozes already gone off are dropped as a new one goes in.
        repository.saveSettings(settings = settings.copy(snoozedUntil = settings.snoozedUntil.filterValues { it > now } + (itemId to at)))
        return at
    }
}

/** "2 capsules", "5 g", "10 minutes", "3 times", or empty for a habit that's just ticked; outside a screen. */
suspend fun formatChecklistDose(item: ChecklistItem): String = when (item.kind) {
    ChecklistKind.SUPPLEMENT -> {
        val amount = formatChecklistAmount(amount = item.amount)
        item.unit.countPlural?.let { getPluralString(resource = it, quantity = item.amount.toInt(), amount) }
            ?: getString(resource = Res.string.value_unit, amount, getString(resource = item.unit.label))
    }
    ChecklistKind.HABIT -> when (item.goal) {
        HabitGoal.TICK -> ""
        HabitGoal.MINUTES -> getPluralString(resource = Res.plurals.checklist_minutes, quantity = item.goalValue, item.goalValue)
        HabitGoal.TIMES -> getPluralString(resource = Res.plurals.checklist_times, quantity = item.goalValue, item.goalValue)
    }
}

/** "6 capsules" or "6 doses" of [item]'s stock; outside a screen. */
suspend fun formatChecklistStock(item: ChecklistItem, count: Int): String =
    getPluralString(resource = item.stockPlural, quantity = count, count.withThousands())

/** "1,000" or "0.5". */
fun formatChecklistAmount(amount: Double): String =
    if (amount == amount.toLong().toDouble()) amount.toInt().withThousands() else amount.toString()

/** The unit's label on its chip; capsules and tablets are written with their count in a dose instead. */
val SupplementUnit.label: StringResource
    get() = when (this) {
        SupplementUnit.MG -> Res.string.checklist_unit_mg
        SupplementUnit.G -> Res.string.checklist_unit_g
        SupplementUnit.IU -> Res.string.checklist_unit_iu
        SupplementUnit.MCG -> Res.string.checklist_unit_mcg
        SupplementUnit.CAPSULES -> Res.string.checklist_unit_capsules
        SupplementUnit.TABLETS -> Res.string.checklist_unit_tablets
    }

/** "%s capsules" for units counted one by one; null for the others. */
val SupplementUnit.countPlural: PluralStringResource?
    get() = when (this) {
        SupplementUnit.CAPSULES -> Res.plurals.checklist_capsules
        SupplementUnit.TABLETS -> Res.plurals.checklist_tablets
        else -> null
    }

/** What the stock is counted in: capsules or tablets, otherwise doses. */
val ChecklistItem.stockPlural: PluralStringResource
    get() = (if (kind == ChecklistKind.SUPPLEMENT) unit.countPlural else null) ?: Res.plurals.checklist_doses

/** "with dinner", "at bedtime": when it's due, in a sentence. */
val ChecklistTime.phrase: StringResource
    get() = when (this) {
        ChecklistTime.MORNING -> Res.string.checklist_when_morning
        ChecklistTime.BREAKFAST -> Res.string.checklist_when_breakfast
        ChecklistTime.LUNCH -> Res.string.checklist_when_lunch
        ChecklistTime.DINNER -> Res.string.checklist_when_dinner
        ChecklistTime.EVENING -> Res.string.checklist_when_evening
        ChecklistTime.BEDTIME -> Res.string.checklist_when_bedtime
        ChecklistTime.ANY_TIME -> Res.string.checklist_when_any_time
    }

private suspend fun reminderMessage(item: ChecklistItem): ChecklistReminderMessage {
    val dose = formatChecklistDose(item = item)
    val phrase = getString(resource = item.time.phrase)
    return ChecklistReminderMessage(
        title = if (dose.isEmpty()) item.name else getString(resource = Res.string.checklist_reminder_title, item.name, dose),
        body = item.stock?.let { stock ->
            getString(resource = Res.string.checklist_reminder_body_stock, phrase, formatChecklistStock(item = item, count = stock))
        } ?: getString(resource = Res.string.checklist_reminder_body, phrase)
    )
}

/**
 * Every reminder for the [days] checklist days from today on, due or not: each active item's reminder time on the
 * days it's due, plus any snoozed one, leaving out those already done or skipped. A reminder time before the day
 * starts falls on the calendar day after.
 */
@OptIn(ExperimentalTime::class)
private suspend fun Checklist.reminders(days: Int): List<ChecklistReminder> {
    val zone = TimeZone.currentSystemDefault()
    val scheduled = (0 until days).flatMap { offset ->
        val date = today.plus(period = DatePeriod(days = offset))
        activeItems.mapNotNull { item ->
            val minutes = item.reminderMinutes ?: return@mapNotNull null
            if (!item.isDueOn(date = date) || markOn(item = item, date = date).isSettled) return@mapNotNull null
            val calendarDate = if (minutes < settings.dayStartMinutes) date.plus(period = DatePeriod(days = 1)) else date
            ChecklistReminder(itemId = item.id, date = date, at = calendarDate.at(minutes = minutes), message = reminderMessage(item = item))
        }
    }
    val snoozed = settings.snoozedUntil.mapNotNull { (itemId, atMillis) ->
        val item = item(id = itemId)?.takeIf { !it.isPaused } ?: return@mapNotNull null
        if (markOn(item = item, date = today).isSettled) return@mapNotNull null
        ChecklistReminder(
            itemId = itemId,
            date = today,
            at = Instant.fromEpochMilliseconds(epochMilliseconds = atMillis).toLocalDateTime(timeZone = zone),
            message = reminderMessage(item = item)
        )
    }
    return (scheduled + snoozed).sortedBy { it.at }
}

private val DayMark.isSettled: Boolean get() = this == DayMark.DONE || this == DayMark.SKIPPED

/** Sets the checklist reminders still to come. */
@OptIn(ExperimentalTime::class)
class ScheduleChecklistRemindersUseCase(
    private val getChecklistUseCase: GetChecklistUseCase,
    private val scheduler: ChecklistReminderScheduler
) {
    suspend operator fun invoke() {
        val now = Clock.System.now().toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
        scheduler.schedule(upcoming = getChecklistUseCase().first().reminders(days = scheduler.days).filter { it.at > now })
    }
}

/** Sets the checklist reminders again as items are ticked or changed, while the app is open. */
class SyncChecklistRemindersUseCase(
    private val getChecklistUseCase: GetChecklistUseCase,
    private val scheduleChecklistRemindersUseCase: ScheduleChecklistRemindersUseCase
) {
    @OptIn(FlowPreview::class)
    suspend operator fun invoke() {
        getChecklistUseCase()
            .debounce(timeoutMillis = 500)
            .collectLatest { scheduleChecklistRemindersUseCase() }
    }
}

/** The reminders to show now, where the phone checks when one is due: those due in the last [DUE_WINDOW_MINUTES]. */
@OptIn(ExperimentalTime::class)
class GetDueChecklistRemindersUseCase(private val getChecklistUseCase: GetChecklistUseCase) {
    suspend operator fun invoke(): List<ChecklistReminder> {
        val zone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val nowTime = now.toLocalDateTime(timeZone = zone)
        val earliest = Instant.fromEpochMilliseconds(epochMilliseconds = now.toEpochMilliseconds() - DUE_WINDOW_MINUTES * 60_000L)
            .toLocalDateTime(timeZone = zone)
        return getChecklistUseCase().first().reminders(days = 2).filter { it.at > earliest && it.at <= nowTime }
    }
}

/** A button on a reminder: done or skipped for [date], or reminded again in an hour. Then sets the reminders again. */
class HandleChecklistReminderActionUseCase(
    private val updateChecklistDayUseCase: UpdateChecklistDayUseCase,
    private val snoozeChecklistItemUseCase: SnoozeChecklistItemUseCase,
    private val scheduleChecklistRemindersUseCase: ScheduleChecklistRemindersUseCase
) {
    suspend operator fun invoke(itemId: Long, date: LocalDate, action: ChecklistReminderAction) {
        when (action) {
            ChecklistReminderAction.DONE -> updateChecklistDayUseCase(itemId = itemId, date = date, action = ChecklistAction.DONE)
            ChecklistReminderAction.SKIP -> updateChecklistDayUseCase(itemId = itemId, date = date, action = ChecklistAction.SKIP)
            ChecklistReminderAction.SNOOZE -> snoozeChecklistItemUseCase(itemId = itemId)
        }
        scheduleChecklistRemindersUseCase()
    }
}
