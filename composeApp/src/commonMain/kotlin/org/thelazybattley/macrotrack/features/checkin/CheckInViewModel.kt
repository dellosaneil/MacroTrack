package org.thelazybattley.macrotrack.features.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.WEEK_BALANCE_WEEKS
import org.thelazybattley.macrotrack.domain.model.WeekBalance
import org.thelazybattley.macrotrack.domain.model.WeeklyCheckIn
import org.thelazybattley.macrotrack.domain.model.calorieAdjust
import org.thelazybattley.macrotrack.domain.model.carbsFor
import org.thelazybattley.macrotrack.domain.usecase.checkin.CheckInSnapshot
import org.thelazybattley.macrotrack.domain.usecase.checkin.CompleteCheckInUseCase
import org.thelazybattley.macrotrack.domain.usecase.checkin.GetWeekBalanceUseCase
import org.thelazybattley.macrotrack.domain.usecase.checkin.GetWeeklyCheckInUseCase
import org.thelazybattley.macrotrack.domain.usecase.checkin.MarkCheckInSeenUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase

/**
 * The check-in as it was when the screen opened. Applying changes the target the check-in compares against, so the
 * screen keeps showing the suggestion it made rather than recomputing it. The week balance above it stays live.
 */
data class CheckInViewState(
    val snapshot: CheckInSnapshot? = null,
    val weekBalance: WeekBalance? = null,
    /** 0 is this week, 1 the week before. */
    val weeksAgo: Int = 0,
    /** The day tapped on the week balance, by index from Monday. */
    val selectedDay: Int? = null,
    val isApplied: Boolean = false,
    val isClosed: Boolean = false
) {
    val result: WeeklyCheckIn.Result? get() = snapshot?.checkIn as? WeeklyCheckIn.Result
}

interface CheckInCallbacks {
    fun onApply()
    fun onUndo()

    /** Keeps the current target, or leaves after applying or with nothing to change. */
    fun onKeep()
    fun onPreviousWeek()
    fun onNextWeek()
    fun onDayPicked(index: Int)

    companion object {
        fun default() = object : CheckInCallbacks {
            override fun onApply() = Unit
            override fun onUndo() = Unit
            override fun onKeep() = Unit
            override fun onPreviousWeek() = Unit
            override fun onNextWeek() = Unit
            override fun onDayPicked(index: Int) = Unit
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CheckInViewModel(
    getWeeklyCheckInUseCase: GetWeeklyCheckInUseCase,
    getWeekBalanceUseCase: GetWeekBalanceUseCase,
    private val markCheckInSeenUseCase: MarkCheckInSeenUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val completeCheckInUseCase: CompleteCheckInUseCase,
    private val insertUserDetailsUseCase: InsertUserDetailsUseCase
) : ViewModel(), CheckInCallbacks {

    private val _state = MutableStateFlow(value = CheckInViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val snapshot = getWeeklyCheckInUseCase().first()
            _state.update { it.copy(snapshot = snapshot) }
            (snapshot?.checkIn as? WeeklyCheckIn.Result)?.let { markCheckInSeenUseCase(suggestedTarget = it.suggestedTarget) }
        }
        viewModelScope.launch {
            _state.map { it.weeksAgo }
                .distinctUntilChanged()
                .flatMapLatest { weeksAgo -> flow { emitAll(getWeekBalanceUseCase(weeksAgo = weeksAgo)) } }
                .collect { balance -> _state.update { it.copy(weekBalance = balance) } }
        }
    }

    override fun onPreviousWeek() {
        _state.update { it.copy(weeksAgo = (it.weeksAgo + 1).coerceAtMost(maximumValue = WEEK_BALANCE_WEEKS - 1), selectedDay = null) }
    }

    override fun onNextWeek() {
        _state.update { it.copy(weeksAgo = (it.weeksAgo - 1).coerceAtLeast(minimumValue = 0), selectedDay = null) }
    }

    override fun onDayPicked(index: Int) {
        _state.update { it.copy(selectedDay = if (it.selectedDay == index) null else index) }
    }

    override fun onApply() {
        val currentState = _state.value
        val snapshot = currentState.snapshot ?: return
        val result = currentState.result ?: return
        if (currentState.isApplied || result.isNoChange) return
        val userDetails = snapshot.userDetails
        // Saved so the target comes out at exactly the suggestion: burn plus the goal's pace.
        val maintenance = result.suggestedTarget - userDetails.goal.calorieAdjust(paceKgPerMonth = userDetails.goalPace)
        // Custom targets change calories only: protein and fat stay as typed, and carbs take what's left.
        val customMacroGoals = userDetails.customMacroGoals?.let {
            it.copy(
                calories = result.suggestedTarget,
                carbs = carbsFor(calories = result.suggestedTarget, protein = it.protein, fat = it.fat)
            )
        }
        _state.update { it.copy(isApplied = true) }
        viewModelScope.launch {
            completeCheckInUseCase(
                userDetails = userDetails,
                measuredMaintenance = maintenance,
                customMacroGoals = customMacroGoals
            )
        }
    }

    override fun onUndo() {
        val snapshot = _state.value.snapshot ?: return
        if (!_state.value.isApplied) return
        _state.update { it.copy(isApplied = false) }
        viewModelScope.launch {
            val userDetails = getUserDetailsUseCase().first() ?: return@launch
            insertUserDetailsUseCase(
                userDetails = userDetails.copy(
                    measuredMaintenance = snapshot.userDetails.measuredMaintenance,
                    customMacroGoals = snapshot.userDetails.customMacroGoals
                )
            )
        }
    }

    override fun onKeep() {
        val currentState = _state.value
        val snapshot = currentState.snapshot
        val result = currentState.result
        if (snapshot == null || result == null || currentState.isApplied || result.isNoChange) {
            _state.update { it.copy(isClosed = true) }
            return
        }
        viewModelScope.launch {
            completeCheckInUseCase(
                userDetails = snapshot.userDetails,
                measuredMaintenance = snapshot.userDetails.measuredMaintenance
            )
            _state.update { it.copy(isClosed = true) }
        }
    }
}
