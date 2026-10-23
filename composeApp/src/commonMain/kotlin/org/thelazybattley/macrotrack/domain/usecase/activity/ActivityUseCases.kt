package org.thelazybattley.macrotrack.domain.usecase.activity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.platform.StepNotifier
import org.thelazybattley.macrotrack.core.platform.StepNudgeScheduler
import org.thelazybattley.macrotrack.core.platform.StepSensor
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.ActivitySession
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.BurnProfile
import org.thelazybattley.macrotrack.domain.model.DayActivity
import org.thelazybattley.macrotrack.domain.model.StepHour
import org.thelazybattley.macrotrack.domain.model.StepNotificationSettings
import org.thelazybattley.macrotrack.domain.model.spreadSteps
import org.thelazybattley.macrotrack.domain.model.stepsSince
import org.thelazybattley.macrotrack.domain.repository.ActivityRepository
import org.thelazybattley.macrotrack.domain.repository.StepSensorReading
import org.thelazybattley.macrotrack.domain.usecase.pro.GetProStatusUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Saves a step counter's running total: the steps since the last reading go into the hours they were taken in,
 * and this reading becomes the next one's starting point.
 */
suspend fun ActivityRepository.recordStepCount(count: Long, atMillis: Long) {
    val previous = getStepSensorReading()
    val steps = stepsSince(previous = previous, count = count)
    spreadSteps(steps = steps, fromMillis = previous?.atMillis ?: atMillis, toMillis = atMillis).forEach {
        addSteps(date = it.date, hour = it.hour, steps = it.steps)
    }
    setStepSensorReading(reading = StepSensorReading(count = count, atMillis = atMillis))
}

/**
 * Counts steps into the database for as long as it runs and counting is on; started while the app is open. Turning
 * counting off stops it, and on again picks up from then.
 */
class TrackStepsUseCase(
    private val stepSensor: StepSensor,
    private val repository: ActivityRepository
) {
    suspend operator fun invoke() = repository.isStepCountingOn().collectLatest { isOn ->
        if (isOn) stepSensor.track(repository = repository)
    }
}

class GetStepHoursUseCase(private val repository: ActivityRepository) {
    operator fun invoke(fromDate: LocalDate): Flow<List<StepHour>> = repository.getStepHours(fromDate = fromDate)
}

class GetActivitySessionsUseCase(private val repository: ActivityRepository) {
    operator fun invoke(): Flow<List<ActivitySession>> = repository.getSessions()
}

class GetActivitySessionUseCase(private val repository: ActivityRepository) {
    operator fun invoke(id: Long): Flow<ActivitySession?> = repository.getSession(id = id)
}

class SaveActivitySessionUseCase(
    private val repository: ActivityRepository,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(session: ActivitySession): Long {
        val id = repository.insertSession(session = session)
        telemetry.logEvent(
            name = TelemetryEvent.ACTIVITY_RECORDED,
            params = mapOf(TelemetryEvent.PARAM_TYPE to session.type.name.lowercase(), TelemetryEvent.PARAM_NAMED to (session.name != null))
        )
        return id
    }
}

class RenameActivitySessionUseCase(private val repository: ActivityRepository) {
    suspend operator fun invoke(id: Long, name: String?, type: ActivityType) =
        repository.renameSession(id = id, name = name?.trim()?.takeIf { it.isNotEmpty() }, type = type)
}

class DeleteActivitySessionUseCase(private val repository: ActivityRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteSession(id = id)
}

class GetStepGoalUseCase(private val repository: ActivityRepository) {
    operator fun invoke(): Flow<Int> = repository.getStepGoal()
}

class SetStepGoalUseCase(
    private val repository: ActivityRepository,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(goal: Int) {
        repository.setStepGoal(goal = goal)
        telemetry.logEvent(name = TelemetryEvent.STEP_GOAL_SET)
    }
}

class IsStepNudgeEnabledUseCase(private val repository: ActivityRepository) {
    operator fun invoke(): Flow<Boolean> = repository.isStepNudgeEnabled()
}

/** Saves the choice and sets or clears the evening reminder to match. */
class SetStepNudgeEnabledUseCase(
    private val repository: ActivityRepository,
    private val scheduler: StepNudgeScheduler
) {
    suspend operator fun invoke(isEnabled: Boolean) {
        repository.setStepNudgeEnabled(isEnabled = isEnabled)
        scheduler.schedule(isEnabled = isEnabled, hour = repository.getStepNotificationSettings().first().nudgeHour)
    }
}

/**
 * Sets the next reminder and the steps notification from the saved choices, and again each time they change; run
 * while the app is open, since a restart clears both.
 */
class ScheduleStepNudgeUseCase(
    private val repository: ActivityRepository,
    private val scheduler: StepNudgeScheduler,
    private val notifier: StepNotifier
) {
    suspend operator fun invoke() {
        combine(flow = repository.isStepNudgeEnabled(), flow2 = repository.getStepNotificationSettings()) { isOn, settings ->
            isOn to settings
        }.collect { (isOn, settings) ->
            scheduler.schedule(isEnabled = isOn, hour = settings.nudgeHour)
            notifier.refresh()
        }
    }
}

class IsStepCountingOnUseCase(private val repository: ActivityRepository) {
    operator fun invoke(): Flow<Boolean> = repository.isStepCountingOn()
}

/**
 * Turns all-day step counting off or back on. Steps already saved stay; steps taken while it was off are never
 * added. The steps notification goes while it's off, and walks and runs still count their own steps.
 */
class SetStepCountingUseCase(
    private val repository: ActivityRepository,
    private val notifier: StepNotifier
) {
    @OptIn(ExperimentalTime::class)
    suspend operator fun invoke(isOn: Boolean) {
        if (isOn) repository.resetStepBaseline(atMillis = Clock.System.now().toEpochMilliseconds())
        repository.setStepCountingOn(isOn = isOn)
        notifier.refresh()
    }
}

class GetStepNotificationSettingsUseCase(private val repository: ActivityRepository) {
    operator fun invoke(): Flow<StepNotificationSettings> = repository.getStepNotificationSettings()
}

/** Saves a change to the steps notification or alerts; [ScheduleStepNudgeUseCase] applies it. */
class UpdateStepNotificationSettingsUseCase(private val repository: ActivityRepository) {
    suspend operator fun invoke(change: (StepNotificationSettings) -> StepNotificationSettings) {
        val current = repository.getStepNotificationSettings().first()
        val updated = change(current)
        repository.setStepNotificationSettings(settings = updated)
        if (updated.isOn && !current.isOn) repository.setStepNotificationHiddenOn(date = null)
    }
}

class IsBurnAddedToBudgetUseCase(private val repository: ActivityRepository) {
    operator fun invoke(): Flow<Boolean> = repository.isBurnAddedToBudget()
}

class SetBurnAddedToBudgetUseCase(private val repository: ActivityRepository) {
    suspend operator fun invoke(isAdded: Boolean) = repository.setBurnAddedToBudget(isAdded = isAdded)
}

/** The profile burn is worked out from, with the latest weigh-in in place of the profile weight; null until set up. */
class GetBurnProfileUseCase(
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val getAllWeightUseCase: GetAllWeightUseCase
) {
    operator fun invoke(): Flow<BurnProfile?> = combine(
        flow = flow { emitAll(getUserDetailsUseCase()) },
        flow2 = getAllWeightUseCase()
    ) { userDetails, weights ->
        userDetails?.let {
            val latest = weights.maxByOrNull { weight -> weight.date }
            BurnProfile(
                weightKg = latest?.weight ?: it.weight,
                heightCm = it.height,
                age = it.age,
                sex = it.sex,
                weightDate = latest?.date
            )
        }
    }
}

/** One day's steps and recordings. */
class GetDayActivityUseCase(private val repository: ActivityRepository) {
    operator fun invoke(date: LocalDate): Flow<DayActivity> = combine(
        flow = repository.getStepHours(fromDate = date),
        flow2 = repository.getSessions()
    ) { hours, sessions ->
        DayActivity(
            totalSteps = hours.filter { it.date == date }.sumOf { it.steps },
            sessions = sessions.filter { it.date == date }
        )
    }
}

/**
 * Calories burned on [date] that go on top of its food budget: zero unless the user turned that on and has Pro.
 * Only the budget shown changes; the saved goal stays as it was, so the weekly check-in never counts burn twice.
 */
class GetBudgetBurnUseCase(
    private val repository: ActivityRepository,
    private val getDayActivityUseCase: GetDayActivityUseCase,
    private val getBurnProfileUseCase: GetBurnProfileUseCase,
    private val getProStatusUseCase: GetProStatusUseCase
) {
    operator fun invoke(date: LocalDate): Flow<Int> = combine(
        flow = repository.isBurnAddedToBudget(),
        flow2 = getProStatusUseCase().map { it.isActive },
        flow3 = getDayActivityUseCase(date = date),
        flow4 = getBurnProfileUseCase()
    ) { isAdded, isPro, day, profile ->
        if (isAdded && isPro && profile != null) day.activeKcal(profile = profile) else 0
    }
}

/** Today's movement as the food log shows it: steps against the goal, calories burned, and what goes on the budget. */
data class TodayActivity(
    val day: DayActivity,
    val stepGoal: Int,
    /** Null until the profile is set up. */
    val activeKcal: Int?,
    val budgetBurn: Int,
    /** False once step counting is turned off; the steps already saved still show. */
    val isCounting: Boolean = true
)

class GetTodayActivityUseCase(
    private val repository: ActivityRepository,
    private val getDayActivityUseCase: GetDayActivityUseCase,
    private val getBurnProfileUseCase: GetBurnProfileUseCase,
    private val getBudgetBurnUseCase: GetBudgetBurnUseCase
) {
    operator fun invoke(date: LocalDate): Flow<TodayActivity> = combine(
        flow = getDayActivityUseCase(date = date),
        flow2 = repository.getStepGoal(),
        flow3 = getBurnProfileUseCase(),
        flow4 = getBudgetBurnUseCase(date = date),
        flow5 = repository.isStepCountingOn()
    ) { day, goal, profile, budgetBurn, isCounting ->
        TodayActivity(
            day = day,
            stepGoal = goal,
            activeKcal = profile?.let { day.activeKcal(profile = it) },
            budgetBurn = budgetBurn,
            isCounting = isCounting
        )
    }
}
