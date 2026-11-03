package org.thelazybattley.macrotrack.domain.usecase.checkin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.CHECK_IN_NO_CHANGE_KCAL
import org.thelazybattley.macrotrack.domain.model.CheckInMethod
import org.thelazybattley.macrotrack.domain.model.CheckInSettings
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.WeeklyCheckIn
import org.thelazybattley.macrotrack.domain.model.weeklyCheckIn
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateTDEEUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import kotlin.math.abs

/** Today's weekly check-in for the saved user, with what it was worked out from; null before onboarding. */
data class CheckInSnapshot(
    val today: LocalDate,
    val userDetails: UserDetails,
    val macroGoals: MacroGoals,
    val checkIn: WeeklyCheckIn,
    /** The suggested target the user last saw on the check-in; null before they first opened it. */
    val seenSuggestion: Int?,
    val settings: CheckInSettings = CheckInSettings(),
    /** The burn each way of measuring weight change gives over the same window; null where it can't be worked out yet. */
    val methodBurns: Map<CheckInMethod, Int?> = emptyMap()
) {
    /** A suggestion worth a look on Today: a change to make, and not the one already seen. */
    val isNewSuggestion: Boolean
        get() {
            val result = checkIn as? WeeklyCheckIn.Result ?: return false
            if (result.isNoChange) return false
            return seenSuggestion == null || abs(result.suggestedTarget - seenSuggestion) >= CHECK_IN_NO_CHANGE_KCAL
        }
}

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
        flow4 = appFlagRepository.getSeenCheckInSuggestion(),
        flow5 = appFlagRepository.getCheckInSettings()
    ) { foodLogs, weights, userDetails, seenSuggestion, settings ->
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
        val checkInFor = { method: CheckInMethod ->
            weeklyCheckIn(
                today = today,
                foodLogs = foodLogs,
                weights = weights,
                goal = userDetails.goal,
                goalPace = userDetails.goalPace,
                currentTarget = macroGoals.calories,
                estimatedBurn = estimatedBurn,
                settings = settings.copy(method = method)
            )
        }
        val checkIns = CheckInMethod.entries.associateWith(checkInFor)
        CheckInSnapshot(
            today = today,
            userDetails = userDetails,
            macroGoals = macroGoals,
            checkIn = checkIns.getValue(key = settings.method),
            seenSuggestion = seenSuggestion,
            settings = settings,
            methodBurns = checkIns.mapValues { (_, checkIn) -> (checkIn as? WeeklyCheckIn.Result)?.measuredBurn }
        )
    }
}
