package org.thelazybattley.macrotrack.domain.usecase.checkin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.WeekBalance
import org.thelazybattley.macrotrack.domain.model.weekBalance
import org.thelazybattley.macrotrack.domain.model.weekStart
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase

/**
 * The week balance [weeksAgo] weeks back, 0 being this week; null before onboarding. Each day is measured against
 * the target saved for it, so a target changed mid-week doesn't rewrite the days before.
 */
class GetWeekBalanceUseCase(
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getAllDailyGoalsUseCase: GetAllDailyGoalsUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase
) {
    suspend operator fun invoke(weeksAgo: Int = 0): Flow<WeekBalance?> = combine(
        flow = getAllFoodLogUseCase(),
        flow2 = getAllDailyGoalsUseCase(),
        flow3 = getUserDetailsUseCase()
    ) { foodLogs, dailyGoals, userDetails ->
        userDetails ?: return@combine null
        val today = getCurrentDate()
        val currentTarget = calculateMacrosGoalUseCase(userDetails = userDetails).calories
        val weekStart = today.weekStart().minus(period = DatePeriod(days = 7 * weeksAgo))
        val weekEnd = weekStart.plus(period = DatePeriod(days = 6))
        weekBalance(
            weekStart = weekStart,
            today = today,
            intake = foodLogs.filter { it.date in weekStart..weekEnd }
                .groupBy { it.date }
                .mapValues { (_, logs) -> logs.sumOf { it.calories } },
            targetFor = { date ->
                if (date >= today) {
                    currentTarget
                } else {
                    dailyGoals.filter { it.date <= date }.maxByOrNull { it.date }?.macroGoals?.calories ?: currentTarget
                }
            }
        )
    }
}
