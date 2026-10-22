package org.thelazybattley.macrotrack.data.backup

import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.data.local.dao.DatabaseSnapshot
import org.thelazybattley.macrotrack.data.local.entity.ActivitySessionEntity
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.data.local.entity.DailyGoalEntity
import org.thelazybattley.macrotrack.data.local.entity.FavoriteEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodEntity
import org.thelazybattley.macrotrack.data.local.entity.FoodLogEntity
import org.thelazybattley.macrotrack.data.local.entity.MeasurementEntity
import org.thelazybattley.macrotrack.data.local.entity.PlannedFoodEntity
import org.thelazybattley.macrotrack.data.local.entity.RecipeEntity
import org.thelazybattley.macrotrack.data.local.entity.RepeatingMealEntity
import org.thelazybattley.macrotrack.data.local.entity.StepHourEntity
import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity
import org.thelazybattley.macrotrack.data.local.entity.WeightEntity

/** Marks a JSON file as a MacroTrack backup. */
const val BACKUP_FORMAT = "macrotrack-backup"

/** Bump when the file layout changes in a way older versions can't read. */
const val BACKUP_VERSION = 1

/** A backup as saved to a file: every table, plus when and where it was made. */
@Serializable
data class BackupFile(
    val format: String = BACKUP_FORMAT,
    val version: Int = BACKUP_VERSION,
    val createdAtMillis: Long,
    val device: String,
    val foods: List<FoodEntity> = emptyList(),
    val recipes: List<RecipeEntity> = emptyList(),
    val userDetails: List<UserDetailsEntity> = emptyList(),
    val foodLogs: List<FoodLogEntity> = emptyList(),
    val weights: List<WeightEntity> = emptyList(),
    val measurements: List<MeasurementEntity> = emptyList(),
    val dailyGoals: List<DailyGoalEntity> = emptyList(),
    val favorites: List<FavoriteEntity> = emptyList(),
    val appFlags: List<AppFlagEntity> = emptyList(),
    /** Missing from backups made before the meal plan; they restore with an empty plan. */
    val plannedFoods: List<PlannedFoodEntity> = emptyList(),
    val repeatingMeals: List<RepeatingMealEntity> = emptyList(),
    /** Missing from backups made before steps and walks; they restore with no activity. */
    val stepHours: List<StepHourEntity> = emptyList(),
    val activitySessions: List<ActivitySessionEntity> = emptyList()
) {
    fun toSnapshot() = DatabaseSnapshot(
        foods = foods,
        recipes = recipes,
        userDetails = userDetails,
        foodLogs = foodLogs,
        weights = weights,
        measurements = measurements,
        dailyGoals = dailyGoals,
        favorites = favorites,
        appFlags = appFlags,
        plannedFoods = plannedFoods,
        repeatingMeals = repeatingMeals,
        stepHours = stepHours,
        activitySessions = activitySessions
    )
}
