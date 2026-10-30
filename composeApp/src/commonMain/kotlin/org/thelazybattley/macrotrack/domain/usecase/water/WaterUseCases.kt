package org.thelazybattley.macrotrack.domain.usecase.water

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.value_unit
import macrotrack.composeapp.generated.resources.water_reminder_body
import macrotrack.composeapp.generated.resources.water_reminder_title
import org.jetbrains.compose.resources.getString
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.WaterReminder
import org.thelazybattley.macrotrack.core.platform.WaterReminderMessage
import org.thelazybattley.macrotrack.core.platform.WaterReminderScheduler
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.WaterDay
import org.thelazybattley.macrotrack.domain.model.WaterEntry
import org.thelazybattley.macrotrack.domain.model.WaterKind
import org.thelazybattley.macrotrack.domain.model.WaterSettings
import org.thelazybattley.macrotrack.domain.model.WaterSummary
import org.thelazybattley.macrotrack.domain.model.at
import org.thelazybattley.macrotrack.domain.model.reminderMinutes
import org.thelazybattley.macrotrack.domain.model.waterFromMl
import org.thelazybattley.macrotrack.domain.model.waterGoalFromWeight
import org.thelazybattley.macrotrack.domain.model.waterPaceMl
import org.thelazybattley.macrotrack.domain.model.waterUnit
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository
import org.thelazybattley.macrotrack.domain.repository.UserDetailsRepository
import org.thelazybattley.macrotrack.domain.repository.WaterRepository
import org.thelazybattley.macrotrack.domain.repository.WeightRepository
import org.thelazybattley.macrotrack.domain.usecase.reminder.formatClockTime
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** The last 7 days, today included. */
private const val CHART_DAYS = 7

/** A water reminder due this long ago or less still goes out, as the alarm can run a little late. */
private const val DUE_WINDOW_MINUTES = 60

/** The weight a goal from weight works from: the latest weigh-in, else the weight given at sign-up. */
class GetWaterWeightUseCase(
    private val weightRepository: WeightRepository,
    private val userDetailsRepository: UserDetailsRepository
) {
    operator fun invoke(): Flow<Double?> = flow {
        emitAll(
            combine(flow = weightRepository.getAllWeight(), flow2 = userDetailsRepository.getUserDetails()) { weights, details ->
                weights.maxByOrNull { it.date }?.weight ?: details?.weight
            }
        )
    }
}

/** Today's water with its goal and the last 7 days. The day is the one when it's first collected. */
class GetWaterSummaryUseCase(
    private val repository: WaterRepository,
    private val getWaterWeightUseCase: GetWaterWeightUseCase
) {
    operator fun invoke(): Flow<WaterSummary> {
        val today = getCurrentDate()
        val start = today.minus(period = DatePeriod(days = CHART_DAYS - 1))
        return combine(
            flow = repository.getWaterSince(from = start),
            flow2 = repository.getSettings(),
            flow3 = getWaterWeightUseCase()
        ) { entries, settings, weightKg -> summarize(today = today, start = start, entries = entries, settings = settings, weightKg = weightKg) }
    }
}

internal fun summarize(today: LocalDate, start: LocalDate, entries: List<WaterEntry>, settings: WaterSettings, weightKg: Double?): WaterSummary {
    val byDate = entries.groupBy { it.date }
    return WaterSummary(
        date = today,
        entries = byDate[today].orEmpty(),
        goalMl = settings.goalMl(weightKg = weightKg),
        autoGoalMl = waterGoalFromWeight(weightKg = weightKg),
        weightKg = weightKg,
        settings = settings,
        days = (0 until CHART_DAYS).map { offset ->
            val date = start.plus(period = DatePeriod(days = offset))
            WaterDay(date = date, totalMl = byDate[date].orEmpty().sumOf { it.amountMl })
        }
    )
}

class GetWaterSettingsUseCase(private val repository: WaterRepository) {
    operator fun invoke(): Flow<WaterSettings> = repository.getSettings()
}

class SaveWaterSettingsUseCase(private val repository: WaterRepository) {
    suspend operator fun invoke(update: (WaterSettings) -> WaterSettings) {
        repository.saveSettings(settings = update(repository.getSettings().first()))
    }
}

/** Adds a drink now, to today; returns it with its id for Undo. */
@OptIn(ExperimentalTime::class)
class AddWaterUseCase(private val repository: WaterRepository, private val telemetry: Telemetry) {
    suspend operator fun invoke(amountMl: Int, kind: WaterKind): WaterEntry {
        val entry = WaterEntry(
            date = getCurrentDate(),
            amountMl = amountMl,
            kind = kind,
            loggedAtMillis = Clock.System.now().toEpochMilliseconds()
        )
        val id = repository.addWater(entry = entry)
        telemetry.logEvent(name = TelemetryEvent.WATER_LOGGED, params = mapOf(TelemetryEvent.PARAM_TYPE to kind.name.lowercase()))
        return entry.copy(id = id)
    }
}

class DeleteWaterUseCase(private val repository: WaterRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteWater(id = id)
}

/** "900 ml" or "30 fl oz", outside a screen. */
suspend fun formatWater(ml: Int, unitSystem: UnitSystem): String =
    getString(resource = Res.string.value_unit, unitSystem.waterFromMl(ml = ml).withThousands(), unitSystem.waterUnit)

/** Writes a reminder for being at [totalMl] when the usual pace by [minutes] is [paceMl]. */
private suspend fun waterMessage(totalMl: Int, goalMl: Int, paceMl: Int, minutes: Int, unitSystem: UnitSystem) = WaterReminderMessage(
    title = getString(resource = Res.string.water_reminder_title),
    body = getString(
        resource = Res.string.water_reminder_body,
        formatWater(ml = totalMl, unitSystem = unitSystem),
        formatWater(ml = goalMl, unitSystem = unitSystem),
        formatClockTime(minutes = minutes),
        formatWater(ml = paceMl, unitSystem = unitSystem)
    )
)

/**
 * Sets the water reminders still to come: today's only where the water so far is behind the pace by then (logging
 * more sets them again, so they drop away as you catch up), and none once the goal is met; on later days every
 * check, as nothing's been drunk yet.
 */
@OptIn(ExperimentalTime::class)
class ScheduleWaterRemindersUseCase(
    private val getWaterSummaryUseCase: GetWaterSummaryUseCase,
    private val appFlagRepository: AppFlagRepository,
    private val scheduler: WaterReminderScheduler
) {
    suspend operator fun invoke() {
        val summary = getWaterSummaryUseCase().first()
        val unitSystem = appFlagRepository.getUnitSystem().first()
        val now = Clock.System.now().toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
        val upcoming = (0 until scheduler.days).flatMap { offset ->
            val date = summary.date.plus(period = DatePeriod(days = offset))
            val total = if (offset == 0) summary.totalMl else 0
            summary.settings.reminderMinutes().mapNotNull { minutes ->
                val at = date.at(minutes = minutes)
                val pace = waterPaceMl(goalMl = summary.goalMl, minutes = minutes)
                if (at <= now || total >= summary.goalMl || pace <= total) return@mapNotNull null
                WaterReminder(at = at, message = waterMessage(totalMl = total, goalMl = summary.goalMl, paceMl = pace, minutes = minutes, unitSystem = unitSystem))
            }
        }
        scheduler.schedule(upcoming = upcoming)
    }
}

/** Sets the water reminders again as drinks, settings or weight change, while the app is open. */
class SyncWaterRemindersUseCase(
    private val getWaterSummaryUseCase: GetWaterSummaryUseCase,
    private val scheduleWaterRemindersUseCase: ScheduleWaterRemindersUseCase
) {
    @OptIn(FlowPreview::class)
    suspend operator fun invoke() {
        getWaterSummaryUseCase()
            .debounce(timeoutMillis = 500)
            .collectLatest { scheduleWaterRemindersUseCase() }
    }
}

/**
 * The reminder to show now, where the phone checks when one is due: null unless a check time came in the last
 * [DUE_WINDOW_MINUTES], the goal isn't met, and today's water is behind the pace.
 */
@OptIn(ExperimentalTime::class)
class GetDueWaterReminderUseCase(
    private val getWaterSummaryUseCase: GetWaterSummaryUseCase,
    private val appFlagRepository: AppFlagRepository
) {
    suspend operator fun invoke(): WaterReminderMessage? {
        val summary = getWaterSummaryUseCase().first()
        val now = Clock.System.now().toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
        val nowMinutes = now.hour * 60 + now.minute
        val minutes = summary.settings.reminderMinutes().lastOrNull { nowMinutes - it in 0 until DUE_WINDOW_MINUTES } ?: return null
        val pace = waterPaceMl(goalMl = summary.goalMl, minutes = minutes)
        if (summary.isGoalMet || summary.totalMl >= pace) return null
        return waterMessage(
            totalMl = summary.totalMl,
            goalMl = summary.goalMl,
            paceMl = pace,
            minutes = minutes,
            unitSystem = appFlagRepository.getUnitSystem().first()
        )
    }
}
