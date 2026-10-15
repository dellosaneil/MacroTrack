package org.thelazybattley.macrotrack.domain.usecase.foodlog

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase

/** Calories left today against the user's goal; null until their details are set up. */
class GetRemainingCaloriesTodayUseCase(
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase
) {
    suspend operator fun invoke(): Flow<Int?> = combine(
        flow = getAllFoodLogUseCase(),
        flow2 = getUserDetailsUseCase()
    ) { foodLogs, userDetails ->
        val today = getCurrentDate()
        val eatenToday = foodLogs.filter { it.date == today }.sumOf { it.calories }
        userDetails?.let {
            calculateMacrosGoalUseCase(
                height = it.height,
                weight = it.weight,
                age = it.age,
                gender = it.sex,
                activityLevel = it.activityLevel,
                goal = it.goal
            ).calories - eatenToday
        }
    }
}
