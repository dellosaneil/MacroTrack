package org.thelazybattley.macrotrack.features.profile.pastgoal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.ApplyPastGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.features.calendar.firstOfMonth

class PastGoalViewModel(
    private val getAllDailyGoalsUseCase: GetAllDailyGoalsUseCase,
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getAllWeightUseCase: GetAllWeightUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val applyPastGoalUseCase: ApplyPastGoalUseCase
) : ViewModel(), PastGoalCallbacks {

    private val _state = MutableStateFlow(value = PastGoalViewState())
    val state = _state.asStateFlow()

    private var user: UserDetails? = null
    private var weights: List<Weight> = emptyList()

    init {
        viewModelScope.launch {
            combine(
                flow = getAllDailyGoalsUseCase(),
                flow2 = getAllFoodLogUseCase(),
                flow3 = getAllWeightUseCase(),
                flow4 = flow { emitAll(getUserDetailsUseCase()) }
            ) { records, logs, weightList, userDetails ->
                user = userDetails
                weights = weightList
                val today = getCurrentDate()
                val loggedDates = logs.map { it.date }.toSet()
                val earliest = (loggedDates + records.map { it.date }).minOrNull()
                val current = userDetails?.let {
                    DailyGoal(date = today, goal = it.goal, macroGoals = macrosFor(goal = it.goal, weight = it.weight))
                }
                _state.update { state ->
                    val initialised = state.copy(
                        today = today,
                        records = records,
                        current = current,
                        earliestDate = earliest,
                        loggedDates = loggedDates
                    )
                    val withRange = if (state.start == null && initialised.hasPastDays) {
                        // Opens on the most recent month, ending yesterday.
                        val end = initialised.latestDate
                        val start = maxOf(end.firstOfMonth(), earliest!!)
                        initialised.copy(start = start, end = end, selectedGoal = userDetails?.goal ?: state.selectedGoal)
                    } else {
                        initialised
                    }
                    withRange.withTargets()
                }
            }.collect()
        }
    }

    override fun onMonthTapped(month: LocalDate) {
        _state.update { state ->
            val earliest = state.earliestDate ?: return@update state
            val monthStart = maxOf(month, earliest)
            val monthEnd = minOf(month.plus(period = DatePeriod(months = 1)).minus(period = DatePeriod(days = 1)), state.latestDate)
            val anchor = state.start
            val picked = when {
                !state.isPickingLastMonth || anchor == null ->
                    state.copy(start = monthStart, end = monthEnd, isPickingLastMonth = true)
                monthStart < anchor -> state.copy(start = monthStart, isPickingLastMonth = false)
                else -> state.copy(end = monthEnd, isPickingLastMonth = false)
            }
            picked.withTargets()
        }
    }

    override fun onOpenDateSheet(side: DateSide) {
        _state.update { state ->
            val date = (if (side == DateSide.FROM) state.start else state.end) ?: state.latestDate
            state.copy(sheet = side, sheetMonth = date.firstOfMonth(), isPickingLastMonth = false)
        }
    }

    override fun onSheetMonthChanged(month: LocalDate) {
        _state.update { it.copy(sheetMonth = month) }
    }

    override fun onDatePicked(date: LocalDate) {
        _state.update { state ->
            val side = state.sheet ?: return@update state
            if (!state.isSelectable(date = date, side = side)) return@update state
            when (side) {
                DateSide.FROM -> state.copy(start = date)
                DateSide.UNTIL -> state.copy(end = date)
            }.withTargets()
        }
    }

    override fun onCloseDateSheet() {
        _state.update { it.copy(sheet = null) }
    }

    override fun onGoalSelected(goal: Goal) {
        _state.update { it.copy(selectedGoal = goal) }
    }

    override fun onApply() {
        val change = _state.value.change ?: return
        viewModelScope.launch {
            applyPastGoalUseCase(change = change)
            _state.update { it.copy(isSaved = true) }
        }
    }

    /** Targets come from the weight logged closest to (on or before) the start of the range. */
    private fun PastGoalViewState.withTargets(): PastGoalViewState {
        val user = user ?: return this
        val start = start ?: return this
        val sorted = weights.sortedBy { it.date }
        val weight = sorted.lastOrNull { it.date <= start }?.weight ?: sorted.firstOrNull()?.weight ?: user.weight
        return copy(
            weightAtStart = weight,
            targets = Goal.entries.associateWith { macrosFor(goal = it, weight = weight) }
        )
    }

    private fun macrosFor(goal: Goal, weight: Double) = user!!.let {
        calculateMacrosGoalUseCase(
            height = it.height,
            weight = weight,
            age = it.age,
            gender = it.sex,
            activityLevel = it.activityLevel,
            goal = goal
        )
    }
}
