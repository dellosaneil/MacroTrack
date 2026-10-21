package org.thelazybattley.macrotrack.features.calendar

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
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.features.foodlog.resolveDayGoal

class CalendarViewModel(
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getAllDailyGoalsUseCase: GetAllDailyGoalsUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val logDateRequest: LogDateRequest
) : ViewModel(), CalendarCallbacks {

    private val _state = MutableStateFlow(value = CalendarViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                flow = getAllFoodLogUseCase(),
                flow2 = getAllDailyGoalsUseCase(),
                flow3 = flow { emitAll(getUserDetailsUseCase()) }
            ) { logs, dailyGoals, user ->
                val today = getCurrentDate()
                val currentMacroGoals = user?.let {
                    calculateMacrosGoalUseCase(userDetails = it)
                }
                val days = logs.groupBy { it.date }.mapValues { (date, dayLogs) ->
                    DaySummary(
                        date = date,
                        calories = dayLogs.sumOf { it.calories },
                        protein = dayLogs.sumOf { it.protein },
                        carbs = dayLogs.sumOf { it.carbs },
                        fat = dayLogs.sumOf { it.fat },
                        mealCount = dayLogs.map { it.mealType }.distinct().size,
                        goalCalories = resolveDayGoal(
                            date = date,
                            today = today,
                            dailyGoals = dailyGoals,
                            currentGoal = user?.goal,
                            currentMacroGoals = currentMacroGoals
                        )?.macroGoals?.calories
                    )
                }
                _state.update { it.copy(today = today, days = days) }
            }.collect()
        }
    }

    override fun onPreviousMonth() {
        _state.update { state ->
            if (!state.canGoBack) state else state.copy(visibleMonth = state.visibleMonth.minus(period = DatePeriod(months = 1)))
        }
    }

    override fun onNextMonth() {
        _state.update { state ->
            if (!state.canGoForward) state else state.copy(visibleMonth = state.visibleMonth.plus(period = DatePeriod(months = 1)))
        }
    }

    override fun onSelectDate(date: LocalDate) {
        _state.update { it.copy(selectedDate = date) }
    }

    override fun onOpenInFoodLog(date: LocalDate) {
        logDateRequest.request(date = date)
    }
}
