package org.thelazybattley.macrotrack.domain.usecase.checkin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.WeeklyCheckIn
import org.thelazybattley.macrotrack.domain.model.weeklyCheckIn
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateTDEEUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase

/** Today's weekly check-in for the saved user, with what it was worked out from; null before onboarding. */
data class CheckInSnapshot(
    val today: LocalDate,
    val userDetails: UserDetails,
    val macroGoals: MacroGoals,
    val checkIn: WeeklyCheckIn,
    val lastCheckIn: LocalDate?
)

class GetWeeklyCheckInUseCase(
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getAllWeightUseCase: GetAllWeightUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val appFlagRepository: AppFlagRepository,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val calculateTDEEUseCase: CalculateTDEEUseCase
) {
    suspend operator fun invoke(): Flow<CheckInSnapshot?> = combine(
        flow = getAllFoodLogUseCase(),
        flow2 = getAllWeightUseCase(),
        flow3 = getUserDetailsUseCase(),
        flow4 = appFlagRepository.getLastCheckIn()
    ) { foodLogs, weights, userDetails, lastCheckIn ->
        userDetails ?: return@combine null
        val today = getCurrentDate()
        val macroGoals = calculateMacrosGoalUseCase(userDetails = userDetails)
        val estimatedBurn = calculateTDEEUseCase(
            height = userDetails.height,
            weight = userDetails.weight,
            age = userDetails.age,
            gender = userDetails.sex,
            activityLevel = userDetails.activityLevel
        )
        CheckInSnapshot(
            today = today,
            userDetails = userDetails,
            macroGoals = macroGoals,
            checkIn = weeklyCheckIn(
                today = today,
                foodLogs = foodLogs,
                weights = weights,
                goal = userDetails.goal,
                goalPace = userDetails.goalPace,
                currentTarget = macroGoals.calories,
                estimatedBurn = estimatedBurn
            ),
            lastCheckIn = lastCheckIn
        )
    }
}
