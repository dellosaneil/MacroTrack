package org.thelazybattley.macrotrack.domain.usecase.forecast

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.CustomMilestone
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.GoalChange
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.features.profile.weighthistory.goalPhases
import org.thelazybattley.macrotrack.domain.model.ForecastSettings
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.GoalForecast
import org.thelazybattley.macrotrack.domain.model.Milestone
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.carbsFor
import org.thelazybattley.macrotrack.domain.model.goalForecast
import org.thelazybattley.macrotrack.domain.model.milestones
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateTDEEUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.InsertUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import kotlin.math.abs

private const val KCAL_PER_KG = 7700.0
private const val DAYS_PER_MONTH = 30.4

/** The goal forecast with what it was worked out from; [forecast] is null without a goal weight or weigh-ins. */
data class ForecastSnapshot(
    val today: LocalDate,
    val userDetails: UserDetails,
    /** Today's targets. */
    val macroGoals: MacroGoals,
    val maintenance: Int,
    val settings: ForecastSettings,
    val unitSystem: UnitSystem,
    val forecast: GoalForecast?,
    val milestones: List<Milestone>
)

class GetGoalForecastUseCase(
    private val getAllWeightUseCase: GetAllWeightUseCase,
    private val getAllFoodLogUseCase: GetAllFoodLogUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val getAllDailyGoalsUseCase: GetAllDailyGoalsUseCase,
    private val appFlagRepository: AppFlagRepository,
    private val calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    private val calculateTDEEUseCase: CalculateTDEEUseCase
) {
    suspend operator fun invoke(): Flow<ForecastSnapshot?> = combine(
        flow = combine(
            flow = getAllWeightUseCase(),
            flow2 = getAllFoodLogUseCase(),
            flow3 = getUserDetailsUseCase(),
            flow4 = getAllDailyGoalsUseCase()
        ) { weights, foodLogs, userDetails, goalRecords -> ForecastInputs(weights, foodLogs, userDetails, goalRecords) },
        flow2 = appFlagRepository.getForecastSettings(),
        flow3 = appFlagRepository.getCustomMilestones(),
        flow4 = appFlagRepository.getUnitSystem()
    ) { (weights, foodLogs, userDetails, goalRecords), settings, customMilestones, unitSystem ->
        userDetails ?: return@combine null
        val today = getCurrentDate()
        val maintenance = userDetails.measuredMaintenance ?: calculateTDEEUseCase(
            height = userDetails.height,
            weight = userDetails.weight,
            age = userDetails.age,
            gender = userDetails.sex,
            activityLevel = userDetails.activityLevel
        )
        val forecast = goalForecast(
            today = today,
            weights = weights,
            foodLogs = foodLogs,
            targetWeight = userDetails.targetWeight,
            startWeight = userDetails.targetStartWeight,
            startDate = userDetails.targetSetDate,
            maintenance = maintenance,
            basis = settings.basis,
            goalChange = goalPhases(records = goalRecords, today = today, currentGoal = userDetails.goal)
                .takeIf { it.size > 1 }
                ?.last()
                ?.let { GoalChange(date = it.start, goal = it.goal) }
        )
        ForecastSnapshot(
            today = today,
            userDetails = userDetails,
            macroGoals = calculateMacrosGoalUseCase(userDetails = userDetails),
            maintenance = maintenance,
            settings = settings,
            unitSystem = unitSystem,
            forecast = forecast,
            milestones = forecast?.milestones(custom = customMilestones).orEmpty()
        )
    }
}

private data class ForecastInputs(
    val weights: List<Weight>,
    val foodLogs: List<FoodLog>,
    val userDetails: UserDetails?,
    val goalRecords: List<DailyGoal>
)

class SetForecastSettingsUseCase(private val repository: AppFlagRepository) {

    suspend operator fun invoke(settings: ForecastSettings) {
        repository.setForecastSettings(settings = settings)
    }
}

class AddCustomMilestoneUseCase(private val repository: AppFlagRepository) {

    suspend operator fun invoke(name: String, weight: Double, byDate: LocalDate?) {
        val milestones = repository.getCustomMilestones().first()
        val id = (milestones.maxOfOrNull { it.id } ?: 0L) + 1
        repository.setCustomMilestones(milestones = milestones + CustomMilestone(id = id, name = name, weight = weight, byDate = byDate))
    }
}

class DeleteCustomMilestoneUseCase(private val repository: AppFlagRepository) {

    suspend operator fun invoke(id: Long) {
        repository.setCustomMilestones(milestones = repository.getCustomMilestones().first().filterNot { it.id == id })
    }
}

/**
 * Sets the daily target to [kcal] from today, keeping protein and fat. Custom targets change calories only and
 * carbs take what's left; calculated ones move the goal and pace so they come out at [kcal] against [maintenance].
 */
class ApplyDailyCaloriesUseCase(private val insertUserDetailsUseCase: InsertUserDetailsUseCase) {

    suspend operator fun invoke(userDetails: UserDetails, kcal: Int, maintenance: Int) {
        val custom = userDetails.customMacroGoals
        val updated = if (custom != null) {
            userDetails.copy(
                customMacroGoals = custom.copy(
                    calories = kcal,
                    carbs = carbsFor(calories = kcal, protein = custom.protein, fat = custom.fat)
                )
            )
        } else {
            val difference = kcal - maintenance
            userDetails.copy(
                goal = when {
                    difference < 0 -> Goal.LOSE_WEIGHT
                    difference > 0 -> Goal.GAIN_WEIGHT
                    else -> Goal.MAINTAIN_WEIGHT
                },
                goalPace = if (difference == 0) userDetails.goalPace else abs(difference) * DAYS_PER_MONTH / KCAL_PER_KG
            )
        }
        insertUserDetailsUseCase(userDetails = updated)
    }
}
