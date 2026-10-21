package org.thelazybattley.macrotrack.features.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.WeeklyCheckIn
import org.thelazybattley.macrotrack.domain.model.calorieAdjust
import org.thelazybattley.macrotrack.domain.model.isCheckInDue
import org.thelazybattley.macrotrack.domain.model.nextCheckIn
import org.thelazybattley.macrotrack.domain.usecase.checkin.CheckInSnapshot
import org.thelazybattley.macrotrack.domain.usecase.checkin.CompleteCheckInUseCase
import org.thelazybattley.macrotrack.domain.usecase.checkin.GetWeeklyCheckInUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase

/**
 * The check-in as it was when the screen opened. Applying changes the target the check-in compares against, so the
 * screen keeps showing the suggestion it made rather than recomputing it.
 */
data class CheckInViewState(
    val snapshot: CheckInSnapshot? = null,
    val isApplied: Boolean = false,
    val isClosed: Boolean = false
) {
    val result: WeeklyCheckIn.Result? get() = snapshot?.checkIn as? WeeklyCheckIn.Result

    /** Already checked in this week: the numbers show, but there is nothing to apply until the next one. */
    val isDue: Boolean get() = snapshot?.let { isCheckInDue(lastCheckIn = it.lastCheckIn, today = it.today) } ?: false

    val nextCheckIn: LocalDate?
        get() = snapshot?.let { nextCheckIn(lastCheckIn = it.lastCheckIn, today = it.today) }
}

interface CheckInCallbacks {
    fun onApply()
    fun onUndo()

    /** Keeps the current target, or closes an on-pace check-in; either way this week's check-in is done. */
    fun onKeep()

    companion object {
        fun default() = object : CheckInCallbacks {
            override fun onApply() = Unit
            override fun onUndo() = Unit
            override fun onKeep() = Unit
        }
    }
}

class CheckInViewModel(
    getWeeklyCheckInUseCase: GetWeeklyCheckInUseCase,
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
        }
    }

    override fun onApply() {
        val currentState = _state.value
        val snapshot = currentState.snapshot ?: return
        val result = currentState.result ?: return
        if (currentState.isApplied || !currentState.isDue) return
        val userDetails = snapshot.userDetails
        // Saved so the target comes out at exactly the suggestion: burn plus the goal's pace.
        val maintenance = result.suggestedTarget - userDetails.goal.calorieAdjust(paceKgPerMonth = userDetails.goalPace)
        _state.update { it.copy(isApplied = true) }
        viewModelScope.launch {
            completeCheckInUseCase(date = snapshot.today, userDetails = userDetails, measuredMaintenance = maintenance)
        }
    }

    override fun onUndo() {
        val snapshot = _state.value.snapshot ?: return
        if (!_state.value.isApplied) return
        _state.update { it.copy(isApplied = false) }
        viewModelScope.launch {
            val userDetails = getUserDetailsUseCase().first() ?: return@launch
            insertUserDetailsUseCase(
                userDetails = userDetails.copy(measuredMaintenance = snapshot.userDetails.measuredMaintenance)
            )
        }
    }

    override fun onKeep() {
        val currentState = _state.value
        val snapshot = currentState.snapshot
        if (snapshot == null || currentState.isApplied || !currentState.isDue) {
            _state.update { it.copy(isClosed = true) }
            return
        }
        viewModelScope.launch {
            completeCheckInUseCase(
                date = snapshot.today,
                userDetails = snapshot.userDetails,
                measuredMaintenance = snapshot.userDetails.measuredMaintenance
            )
            _state.update { it.copy(isClosed = true) }
        }
    }
}
