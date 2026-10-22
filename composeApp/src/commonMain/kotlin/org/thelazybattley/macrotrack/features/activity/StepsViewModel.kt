package org.thelazybattley.macrotrack.features.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.StepNudgeScheduler
import org.thelazybattley.macrotrack.core.platform.StepSensor
import org.thelazybattley.macrotrack.domain.model.ActivitySession
import org.thelazybattley.macrotrack.domain.model.BurnProfile
import org.thelazybattley.macrotrack.domain.model.DayActivity
import org.thelazybattley.macrotrack.domain.model.StepGoal
import org.thelazybattley.macrotrack.domain.usecase.activity.GetActivitySessionsUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetBurnProfileUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetStepGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetStepHoursUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.IsStepNudgeEnabledUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.SetStepGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.SetStepNudgeEnabledUseCase

/** Steps on one day. */
data class DaySteps(val date: LocalDate, val steps: Int)

enum class StepsRange { DAY, WEEK }

/** The step goal being picked, before it's saved. */
data class GoalDraft(
    val goal: Int,
    val isNudgeOn: Boolean,
    val isTyping: Boolean = false
)

data class StepsViewState(
    val today: LocalDate = getCurrentDate(),
    val goal: Int = StepGoal.DEFAULT,
    val isNudgeOn: Boolean = true,
    /** Today's steps hour by hour, midnight first. */
    val hours: List<Int> = List(size = 24) { 0 },
    /** The last 7 days, today last. */
    val week: List<DaySteps> = emptyList(),
    /** The 14 full days before today, for picking a goal. */
    val lookback: List<DaySteps> = emptyList(),
    val day: DayActivity = DayActivity(totalSteps = 0, sessions = emptyList()),
    val profile: BurnProfile? = null,
    val recentSessions: List<ActivitySession> = emptyList(),
    /** Walks and runs in the last 30 days, for the history card. */
    val sessionsLast30: Int = 0,
    /** Walks and runs per day over the last 7 days, for the history card's mini chart. */
    val sessionsPerDay: List<Int> = List(size = 7) { 0 },
    val range: StepsRange = StepsRange.DAY,
    val selectedHour: Int? = null,
    val selectedDay: Int? = null,
    val goalDraft: GoalDraft? = null,
    val isSensorAvailable: Boolean = true,
    val isNudgeSupported: Boolean = true
) {
    val todaySteps: Int get() = day.totalSteps

    val daysHit: Int get() = week.count { it.steps >= goal }

    val busiestHour: Int? get() = hours.withIndex().filter { it.value > 0 }.maxByOrNull { it.value }?.index

    val weekAverage: Int get() = week.filter { it.steps > 0 }.map { it.steps }.average().takeIf { !it.isNaN() }?.toInt() ?: 0

    val suggestedGoal: Int? get() = StepGoal.suggested(dailySteps = lookback.map { it.steps })

    val lookbackAverage: Int get() = lookback.map { it.steps }.average().takeIf { !it.isNaN() }?.toInt() ?: 0
}

interface StepsCallbacks {
    fun onRangeSelected(range: StepsRange)
    fun onHourSelected(hour: Int)
    fun onDaySelected(index: Int)
    fun onOpenGoal()
    fun onCloseGoal()
    fun onGoalPicked(goal: Int)
    fun onGoalStepDown()
    fun onGoalStepUp()
    fun onGoalTypingChanged(isTyping: Boolean)
    fun onNudgeToggled()
    fun onSaveGoal()

    companion object {
        fun default() = object : StepsCallbacks {
            override fun onRangeSelected(range: StepsRange) = Unit
            override fun onHourSelected(hour: Int) = Unit
            override fun onDaySelected(index: Int) = Unit
            override fun onOpenGoal() = Unit
            override fun onCloseGoal() = Unit
            override fun onGoalPicked(goal: Int) = Unit
            override fun onGoalStepDown() = Unit
            override fun onGoalStepUp() = Unit
            override fun onGoalTypingChanged(isTyping: Boolean) = Unit
            override fun onNudgeToggled() = Unit
            override fun onSaveGoal() = Unit
        }
    }
}

class StepsViewModel(
    getStepHoursUseCase: GetStepHoursUseCase,
    getActivitySessionsUseCase: GetActivitySessionsUseCase,
    getBurnProfileUseCase: GetBurnProfileUseCase,
    getStepGoalUseCase: GetStepGoalUseCase,
    isStepNudgeEnabledUseCase: IsStepNudgeEnabledUseCase,
    stepSensor: StepSensor,
    stepNudgeScheduler: StepNudgeScheduler,
    private val setStepGoalUseCase: SetStepGoalUseCase,
    private val setStepNudgeEnabledUseCase: SetStepNudgeEnabledUseCase
) : ViewModel(), StepsCallbacks {

    private val _state = MutableStateFlow(value = StepsViewState(isSensorAvailable = stepSensor.isAvailable, isNudgeSupported = stepNudgeScheduler.isSupported))
    val state = _state.asStateFlow()

    init {
        val today = getCurrentDate()
        val from = today.minus(period = DatePeriod(days = StepGoal.LOOKBACK_DAYS))
        viewModelScope.launch {
            combine(
                flow = getStepHoursUseCase(fromDate = from),
                flow2 = getActivitySessionsUseCase(),
                flow3 = getBurnProfileUseCase()
            ) { hours, sessions, profile ->
                val byDate = hours.groupBy { it.date }.mapValues { (_, list) -> list.sumOf { it.steps } }
                val stepsOn: (LocalDate) -> Int = { byDate[it] ?: 0 }
                _state.update { state ->
                    state.copy(
                        today = today,
                        hours = List(size = 24) { hour -> hours.filter { it.date == today && it.hour == hour }.sumOf { it.steps } },
                        week = (6 downTo 0).map { back -> today.minus(period = DatePeriod(days = back)) }.map { DaySteps(date = it, steps = stepsOn(it)) },
                        lookback = (StepGoal.LOOKBACK_DAYS downTo 1).map { back -> today.minus(period = DatePeriod(days = back)) }
                            .map { DaySteps(date = it, steps = stepsOn(it)) },
                        day = DayActivity(totalSteps = stepsOn(today), sessions = sessions.filter { it.date == today }),
                        profile = profile,
                        recentSessions = sessions.take(n = RECENT_COUNT),
                        sessionsLast30 = sessions.count { it.date > today.minus(period = DatePeriod(days = 30)) },
                        sessionsPerDay = (6 downTo 0).map { back ->
                            val date = today.minus(period = DatePeriod(days = back))
                            sessions.count { it.date == date }
                        }
                    )
                }
            }.collect {}
        }
        viewModelScope.launch {
            combine(flow = getStepGoalUseCase(), flow2 = isStepNudgeEnabledUseCase()) { goal, isNudgeOn -> goal to isNudgeOn }
                .collect { (goal, isNudgeOn) -> _state.update { it.copy(goal = goal, isNudgeOn = isNudgeOn) } }
        }
    }

    override fun onRangeSelected(range: StepsRange) {
        _state.update { it.copy(range = range, selectedHour = null, selectedDay = null) }
    }

    override fun onHourSelected(hour: Int) {
        _state.update { it.copy(selectedHour = if (it.selectedHour == hour) null else hour) }
    }

    override fun onDaySelected(index: Int) {
        _state.update { it.copy(selectedDay = if (it.selectedDay == index) null else index) }
    }

    override fun onOpenGoal() {
        _state.update { it.copy(goalDraft = GoalDraft(goal = it.goal, isNudgeOn = it.isNudgeOn)) }
    }

    override fun onCloseGoal() {
        _state.update { it.copy(goalDraft = null) }
    }

    override fun onGoalPicked(goal: Int) {
        updateDraft { it.copy(goal = goal.coerceIn(minimumValue = StepGoal.MIN, maximumValue = StepGoal.MAX), isTyping = false) }
    }

    override fun onGoalStepDown() {
        updateDraft { it.copy(goal = StepGoal.stepDown(goal = it.goal)) }
    }

    override fun onGoalStepUp() {
        updateDraft { it.copy(goal = StepGoal.stepUp(goal = it.goal)) }
    }

    override fun onGoalTypingChanged(isTyping: Boolean) {
        updateDraft { it.copy(isTyping = isTyping) }
    }

    override fun onNudgeToggled() {
        updateDraft { it.copy(isNudgeOn = !it.isNudgeOn) }
    }

    override fun onSaveGoal() {
        val current = _state.value
        val draft = current.goalDraft ?: return
        if (!StepGoal.isValid(goal = draft.goal)) return
        _state.update { it.copy(goalDraft = null, selectedDay = null) }
        viewModelScope.launch {
            if (draft.goal != current.goal) setStepGoalUseCase(goal = draft.goal)
            if (draft.isNudgeOn != current.isNudgeOn) setStepNudgeEnabledUseCase(isEnabled = draft.isNudgeOn)
        }
    }

    private fun updateDraft(change: (GoalDraft) -> GoalDraft) {
        _state.update { state -> state.copy(goalDraft = state.goalDraft?.let(change)) }
    }

    private companion object {
        const val RECENT_COUNT = 3
    }
}
