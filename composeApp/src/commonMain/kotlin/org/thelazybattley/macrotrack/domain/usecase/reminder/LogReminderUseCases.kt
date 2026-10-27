package org.thelazybattley.macrotrack.domain.usecase.reminder

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.reminder_yesterday_more
import macrotrack.composeapp.generated.resources.kg
import macrotrack.composeapp.generated.resources.lb
import macrotrack.composeapp.generated.resources.photo_time_am
import macrotrack.composeapp.generated.resources.photo_time_pm
import macrotrack.composeapp.generated.resources.reminder_action_change
import macrotrack.composeapp.generated.resources.reminder_action_keep
import macrotrack.composeapp.generated.resources.reminder_action_later
import macrotrack.composeapp.generated.resources.reminder_action_log
import macrotrack.composeapp.generated.resources.reminder_action_log_weight
import macrotrack.composeapp.generated.resources.reminder_action_quick_add
import macrotrack.composeapp.generated.resources.reminder_action_same
import macrotrack.composeapp.generated.resources.reminder_action_skip
import macrotrack.composeapp.generated.resources.reminder_kcal_left
import macrotrack.composeapp.generated.resources.reminder_log_now
import macrotrack.composeapp.generated.resources.reminder_meal_title
import macrotrack.composeapp.generated.resources.reminder_paused_body
import macrotrack.composeapp.generated.resources.reminder_paused_title
import macrotrack.composeapp.generated.resources.reminder_usual
import macrotrack.composeapp.generated.resources.reminder_weight_body
import macrotrack.composeapp.generated.resources.reminder_weight_body_on
import macrotrack.composeapp.generated.resources.reminder_weight_body_yesterday
import macrotrack.composeapp.generated.resources.reminder_weight_title
import macrotrack.composeapp.generated.resources.reminder_yesterday
import macrotrack.composeapp.generated.resources.value_unit
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.LogReminderAction
import org.thelazybattley.macrotrack.core.platform.LogReminderMessage
import org.thelazybattley.macrotrack.core.platform.LogReminderScheduler
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.core.toAbbreviatedMonthDay
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.FoodLogCopy
import org.thelazybattley.macrotrack.domain.model.LOG_REMINDER_LATER_MINUTES
import org.thelazybattley.macrotrack.domain.model.LogReminder
import org.thelazybattley.macrotrack.domain.model.LogReminderKind
import org.thelazybattley.macrotrack.domain.model.LogReminderSettings
import org.thelazybattley.macrotrack.domain.model.LogReminderState
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.model.at
import org.thelazybattley.macrotrack.domain.model.planLogReminders
import org.thelazybattley.macrotrack.domain.model.usualLogMinutes
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository
import org.thelazybattley.macrotrack.domain.repository.LogReminderRepository
import org.thelazybattley.macrotrack.domain.usecase.foodlog.CopyFoodLogsUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetRemainingCaloriesUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.UndoFoodLogCopyUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.ui.common.formatTwoDecimals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Reminders already sent this long ago or more aren't sent late, after the phone was off, say. */
private const val LATE_LIMIT_MINUTES = 60

class GetLogReminderSettingsUseCase(private val repository: LogReminderRepository) {

    operator fun invoke(): Flow<LogReminderSettings> = repository.getSettings()
}

/**
 * Saves a change on the Reminders screen. Any change counts as being back, so a paused break ends. Reminders whose
 * time has already passed today count as sent, so turning them on doesn't send one straight away.
 */
@OptIn(ExperimentalTime::class)
class SaveLogReminderSettingsUseCase(
    private val repository: LogReminderRepository,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(settings: LogReminderSettings) {
        val wasOn = repository.getSettings().first().isOn
        repository.saveSettings(settings = settings.copy(activeSince = getCurrentDate()))
        repository.updateState { it.copy(sentUntilMillis = Clock.System.now().toEpochMilliseconds()) }
        if (wasOn != settings.isOn) {
            telemetry.logEvent(
                name = TelemetryEvent.LOG_REMINDERS_SET,
                params = mapOf(TelemetryEvent.PARAM_RESULT to if (settings.isOn) "on" else "off")
            )
        }
    }
}

/** Writes reminders out as they show on the phone, against the log as it is when the first one is written. */
class LogReminderMessages(
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getAllWeightUseCase: GetAllWeightUseCase,
    private val getRemainingCaloriesUseCase: GetRemainingCaloriesUseCase,
    private val appFlagRepository: AppFlagRepository,
    private val repository: LogReminderRepository
) {

    private class Log(
        val foodLogs: List<FoodLog>,
        val weights: List<Weight>,
        val unitSystem: UnitSystem,
        val settings: LogReminderSettings
    )

    /** One writer for a batch of reminders; it reads the log once, the first time it's used. */
    fun writer(): suspend (LogReminder) -> LogReminderMessage {
        var log: Log? = null
        val remaining = mutableMapOf<LocalDate, Int?>()
        return { reminder ->
            val current = log ?: Log(
                foodLogs = getAllFoodLogUseCase().first(),
                weights = getAllWeightUseCase().first(),
                unitSystem = appFlagRepository.getUnitSystem().first(),
                settings = repository.getSettings().first()
            ).also { log = it }
            val date = reminder.at.date
            write(
                kind = reminder.kind,
                date = date,
                log = current,
                remainingCalories = { remaining.getOrPut(key = date) { getRemainingCaloriesUseCase(date = date).first() } }
            )
        }
    }

    private suspend fun write(kind: LogReminderKind, date: LocalDate, log: Log, remainingCalories: suspend () -> Int?): LogReminderMessage {
        val yesterday = date.minus(period = DatePeriod(days = 1))
        val mealType = kind.mealType
        val (title, body, actions) = when {
            mealType != null -> {
                val mealName = getString(resource = mealType.title)
                val yesterdayLogs = log.foodLogs.filter { it.date == yesterday && it.mealType == mealType }
                val usual = usualLogMinutes(foodLogs = log.foodLogs, mealType = mealType, today = date, timeZone = TimeZone.currentSystemDefault())
                val parts = if (mealType == MealType.BREAKFAST || mealType == MealType.LUNCH) {
                    listOfNotNull(
                        usual?.let { getString(resource = Res.string.reminder_usual, formatClockTime(minutes = it)) },
                        yesterdayLogs.takeIf { it.isNotEmpty() }?.let { yesterdaySummary(logs = it) }
                    )
                } else {
                    listOfNotNull(remainingCalories()?.takeIf { it > 0 }?.let { getString(resource = Res.string.reminder_kcal_left, it.withThousands()) })
                }
                val logNow = getString(resource = Res.string.reminder_log_now, mealName.lowercase())
                Triple(
                    getString(resource = Res.string.reminder_meal_title, mealName),
                    (if (parts.isEmpty() || mealType == MealType.DINNER || mealType == MealType.SNACK) parts + logNow else parts).joinToString(separator = " "),
                    listOf(
                        LogReminderAction.LOG to getString(resource = Res.string.reminder_action_log, mealName.lowercase()),
                        if (yesterdayLogs.isNotEmpty()) {
                            LogReminderAction.SAME_AS_YESTERDAY to getString(resource = Res.string.reminder_action_same)
                        } else {
                            LogReminderAction.QUICK_ADD to getString(resource = Res.string.reminder_action_quick_add)
                        },
                        LogReminderAction.SKIP to getString(resource = Res.string.reminder_action_skip)
                    )
                )
            }
            kind == LogReminderKind.WEIGHT -> {
                val last = log.weights.filter { it.date < date }.maxByOrNull { it.date }
                val body = if (last == null) {
                    getString(resource = Res.string.reminder_weight_body)
                } else {
                    val weight = getString(
                        resource = Res.string.value_unit,
                        log.unitSystem.weightFromKg(kg = last.weight).formatTwoDecimals(),
                        getString(resource = if (log.unitSystem == UnitSystem.IMPERIAL) Res.string.lb else Res.string.kg)
                    )
                    if (last.date == yesterday) {
                        getString(resource = Res.string.reminder_weight_body_yesterday, weight)
                    } else {
                        getString(resource = Res.string.reminder_weight_body_on, weight, last.date.toAbbreviatedMonthDay())
                    }
                }
                Triple(
                    getString(resource = Res.string.reminder_weight_title),
                    body,
                    listOf(
                        LogReminderAction.LOG to getString(resource = Res.string.reminder_action_log_weight),
                        LogReminderAction.LATER to getString(resource = Res.string.reminder_action_later),
                        LogReminderAction.SKIP to getString(resource = Res.string.reminder_action_skip)
                    )
                )
            }
            else -> {
                val lastActive = listOfNotNull(log.foodLogs.maxOfOrNull { it.date }, log.weights.maxOfOrNull { it.date }, log.settings.activeSince).maxOrNull()
                Triple(
                    getString(resource = Res.string.reminder_paused_title),
                    getString(resource = Res.string.reminder_paused_body, (lastActive ?: yesterday).dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }),
                    // Keeping them on is what the message asks, so it shows even without quick actions.
                    listOf(
                        LogReminderAction.KEEP_ON to getString(resource = Res.string.reminder_action_keep),
                        LogReminderAction.CHANGE_TIMES to getString(resource = Res.string.reminder_action_change)
                    )
                )
            }
        }
        return LogReminderMessage(
            kind = kind,
            date = date,
            title = title,
            body = body,
            actions = if (log.settings.hasQuickActions || kind == LogReminderKind.PAUSED) actions else emptyList()
        )
    }

    private suspend fun yesterdaySummary(logs: List<FoodLog>): String {
        val calories = logs.sumOf { it.calories }.withThousands()
        val first = logs.first().foodName
        return if (logs.size == 1) {
            getString(resource = Res.string.reminder_yesterday, first, calories)
        } else {
            getPluralString(resource = Res.plurals.reminder_yesterday_more, quantity = logs.size - 1, first, logs.size - 1, calories)
        }
    }
}

/** Minutes after midnight as "6:00 PM", outside a screen. */
suspend fun formatClockTime(minutes: Int): String {
    val hour = minutes / 60 % 24
    return getString(
        resource = if (hour < 12) Res.string.photo_time_am else Res.string.photo_time_pm,
        if (hour % 12 == 0) 12 else hour % 12,
        (minutes % 60).toString().padStart(length = 2, padChar = '0')
    )
}

@OptIn(ExperimentalTime::class)
private fun localNow(): LocalDateTime = Clock.System.now().toLocalDateTime(timeZone = TimeZone.currentSystemDefault())

/** Where reminders not yet sent start: after the last one sent, but no more than [LATE_LIMIT_MINUTES] ago. */
@OptIn(ExperimentalTime::class)
private fun LogReminderState.unsentFrom(now: Instant): LocalDateTime {
    val earliest = now - LATE_LIMIT_MINUTES.minutes
    val sentUntil = sentUntilMillis?.let { Instant.fromEpochMilliseconds(epochMilliseconds = it) }
    return maxOf(earliest, sentUntil ?: earliest).toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
}

/**
 * Sets the reminders still to come on the phone, along with any already due but not sent: setting them again just
 * as one is due, while the alarm is still on its way, mustn't lose it.
 */
@OptIn(ExperimentalTime::class)
class ScheduleLogRemindersUseCase(
    private val repository: LogReminderRepository,
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getAllWeightUseCase: GetAllWeightUseCase,
    private val scheduler: LogReminderScheduler,
    private val messages: LogReminderMessages
) {
    suspend operator fun invoke() {
        val now = localNow()
        val state = repository.getState().first()
        val upcoming = planLogReminders(
            settings = repository.getSettings().first(),
            state = state,
            from = state.unsentFrom(now = Clock.System.now()),
            until = now.date.plus(period = DatePeriod(days = scheduler.daysAhead)).at(minutes = 0),
            foodLogs = getAllFoodLogUseCase().first(),
            weights = getAllWeightUseCase().first()
        )
        scheduler.schedule(upcoming = upcoming, message = messages.writer())
    }
}

/**
 * Sets the reminders again whenever the settings or the log change, while the app is open: logging lunch takes back
 * today's lunch reminder where they're set ahead, and a break ends.
 */
class SyncLogRemindersUseCase(
    private val repository: LogReminderRepository,
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getAllWeightUseCase: GetAllWeightUseCase,
    private val scheduleLogRemindersUseCase: ScheduleLogRemindersUseCase
) {
    @OptIn(FlowPreview::class)
    suspend operator fun invoke() {
        combine(
            flow = repository.getSettings(),
            flow2 = repository.getState(),
            flow3 = getAllFoodLogUseCase(),
            flow4 = getAllWeightUseCase()
        ) { settings, state, foodLogs, weights -> listOf(settings, state, foodLogs, weights) }
            .debounce(timeoutMillis = 500)
            .collectLatest { scheduleLogRemindersUseCase() }
    }
}

/** The reminders due now that haven't gone out yet, written out to show; marks them sent. Where an alarm wakes the app. */
@OptIn(ExperimentalTime::class)
class GetDueLogRemindersUseCase(
    private val repository: LogReminderRepository,
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getAllWeightUseCase: GetAllWeightUseCase,
    private val messages: LogReminderMessages
) {
    suspend operator fun invoke(): List<LogReminderMessage> {
        val now = Clock.System.now()
        val state = repository.getState().first()
        val due = planLogReminders(
            settings = repository.getSettings().first(),
            state = state,
            from = state.unsentFrom(now = now),
            until = now.toLocalDateTime(timeZone = TimeZone.currentSystemDefault()),
            foodLogs = getAllFoodLogUseCase().first(),
            weights = getAllWeightUseCase().first()
        )
        repository.updateState { it.copy(sentUntilMillis = now.toEpochMilliseconds()) }
        val write = messages.writer()
        return due.distinctBy { it.kind }.map { write(it) }
    }
}

/** Writes one reminder out again, as it was before an Undo. */
class GetLogReminderMessageUseCase(private val messages: LogReminderMessages) {

    suspend operator fun invoke(kind: LogReminderKind, date: LocalDate): LogReminderMessage =
        messages.writer()(LogReminder(kind = kind, at = date.at(minutes = 0)))
}

/** Shows what a reminder looks like: the next meal's, or lunch's, without buttons that would log anything. */
class SendTestLogReminderUseCase(
    private val repository: LogReminderRepository,
    private val scheduler: LogReminderScheduler,
    private val messages: LogReminderMessages
) {
    suspend operator fun invoke() {
        val settings = repository.getSettings().first()
        val nowMinutes = localNow().let { it.hour * 60 + it.minute }
        val mealType = MealType.entries
            .filter { settings.meal(mealType = it).isOn && settings.meal(mealType = it).minutes > nowMinutes }
            .minByOrNull { settings.meal(mealType = it).minutes } ?: MealType.LUNCH
        val message = messages.writer()(LogReminder(kind = LogReminderKind.of(mealType = mealType), at = getCurrentDate().at(minutes = 0)))
        scheduler.sendTest(message = message.copy(actions = emptyList()))
    }
}

/** What "Same as yesterday" logged: yesterday's [logs], and the ids of their copies for Undo. */
data class LoggedAgain(val logs: List<FoodLog>, val insertedIds: List<Long>)

/** What the buttons on a reminder do, each with a way back for Undo. */
@OptIn(ExperimentalTime::class)
class LogReminderActions(
    private val repository: LogReminderRepository,
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val copyFoodLogsUseCase: CopyFoodLogsUseCase,
    private val undoFoodLogCopyUseCase: UndoFoodLogCopyUseCase,
    private val scheduleLogRemindersUseCase: ScheduleLogRemindersUseCase,
    private val telemetry: Telemetry
) {

    /** Logs [mealType] on [date] as it was the day before; null when the day before had none. */
    suspend fun logSameAsYesterday(mealType: MealType, date: LocalDate): LoggedAgain? {
        val yesterday = date.minus(period = DatePeriod(days = 1))
        val logs = getAllFoodLogUseCase().first().filter { it.date == yesterday && it.mealType == mealType }
        if (logs.isEmpty()) return null
        logged(action = LogReminderAction.SAME_AS_YESTERDAY)
        val copy = copyFoodLogsUseCase(logs = logs, targetDates = listOf(date), mealType = mealType, replace = false)
        return LoggedAgain(logs = logs, insertedIds = copy.insertedIds)
    }

    suspend fun undoCopy(insertedIds: List<Long>) {
        undoFoodLogCopyUseCase(copy = FoodLogCopy(insertedIds = insertedIds, replaced = emptyList(), itemCount = insertedIds.size, targetDates = emptyList()))
        scheduleLogRemindersUseCase()
    }

    /** Puts today's weigh-in reminder off by [LOG_REMINDER_LATER_MINUTES]; returns when it'll come, and what it replaced. */
    suspend fun remindLater(): Pair<LocalDateTime, LocalDateTime?> {
        val zone = TimeZone.currentSystemDefault()
        val at = (Clock.System.now() + LOG_REMINDER_LATER_MINUTES.minutes).toLocalDateTime(timeZone = zone)
        val previous = setWeightLater(at = at)
        logged(action = LogReminderAction.LATER)
        return at to previous
    }

    /** No more weigh-in reminder today; returns the put-off one it cancelled, if any. */
    suspend fun skipWeight(): LocalDateTime? = setWeightLater(at = null).also { logged(action = LogReminderAction.SKIP) }

    /** Puts back what [remindLater] or [skipWeight] replaced. */
    suspend fun restoreWeightLater(at: LocalDateTime?) {
        setWeightLater(at = at)
    }

    suspend fun skipMeal() = logged(action = LogReminderAction.SKIP)

    /** Ends a break: reminders count from today again. */
    suspend fun keepOn() {
        repository.saveSettings(settings = repository.getSettings().first().copy(activeSince = getCurrentDate()))
        logged(action = LogReminderAction.KEEP_ON)
        scheduleLogRemindersUseCase()
    }

    fun opened(action: LogReminderAction) = logged(action = action)

    private suspend fun setWeightLater(at: LocalDateTime?): LocalDateTime? {
        var previous: LocalDateTime? = null
        repository.updateState { state ->
            previous = state.weightLaterAt
            state.copy(weightLaterAt = at)
        }
        scheduleLogRemindersUseCase()
        return previous
    }

    private fun logged(action: LogReminderAction) {
        telemetry.logEvent(name = TelemetryEvent.LOG_REMINDER_ACTION, params = mapOf(TelemetryEvent.PARAM_TYPE to action.name.lowercase()))
    }
}

@OptIn(ExperimentalTime::class)
internal fun LocalDateTime.toEpochMillis(): Long = toInstant(timeZone = TimeZone.currentSystemDefault()).toEpochMilliseconds()
