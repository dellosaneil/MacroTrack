package org.thelazybattley.macrotrack.features.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.BurnProfile
import org.thelazybattley.macrotrack.domain.model.DayActivity
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetBurnProfileUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetDayActivityUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.IsBurnAddedToBudgetUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.SetBurnAddedToBudgetUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase

data class CaloriesBurnedViewState(
    val date: LocalDate = getCurrentDate(),
    val day: DayActivity = DayActivity(totalSteps = 0, sessions = emptyList()),
    val profile: BurnProfile? = null,
    /** Today's calorie target, before anything burned is added. */
    val calorieGoal: Int? = null,
    val isAddedToBudget: Boolean = false
)

interface CaloriesBurnedCallbacks {
    fun onBudgetToggled()

    companion object {
        fun default() = object : CaloriesBurnedCallbacks {
            override fun onBudgetToggled() = Unit
        }
    }
}

class CaloriesBurnedViewModel(
    getDayActivityUseCase: GetDayActivityUseCase,
    getBurnProfileUseCase: GetBurnProfileUseCase,
    getUserDetailsUseCase: GetUserDetailsUseCase,
    calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    isBurnAddedToBudgetUseCase: IsBurnAddedToBudgetUseCase,
    private val setBurnAddedToBudgetUseCase: SetBurnAddedToBudgetUseCase
) : ViewModel(), CaloriesBurnedCallbacks {

    private val _state = MutableStateFlow(value = CaloriesBurnedViewState())
    val state = _state.asStateFlow()

    init {
        val today = getCurrentDate()
        viewModelScope.launch {
            combine(
                flow = getDayActivityUseCase(date = today),
                flow2 = getBurnProfileUseCase(),
                flow3 = flow { emitAll(getUserDetailsUseCase()) },
                flow4 = isBurnAddedToBudgetUseCase()
            ) { day, profile, userDetails, isAdded ->
                _state.update {
                    it.copy(
                        date = today,
                        day = day,
                        profile = profile,
                        calorieGoal = userDetails?.let { details -> calculateMacrosGoalUseCase(userDetails = details).calories },
                        isAddedToBudget = isAdded
                    )
                }
            }.collect {}
        }
    }

    override fun onBudgetToggled() {
        val isAdded = !_state.value.isAddedToBudget
        _state.update { it.copy(isAddedToBudget = isAdded) }
        viewModelScope.launch { setBurnAddedToBudgetUseCase(isAdded = isAdded) }
    }
}
