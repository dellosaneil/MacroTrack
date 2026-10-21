package org.thelazybattley.macrotrack.data.local.entity

import kotlinx.datetime.LocalDate
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.MacroBasis
import org.thelazybattley.macrotrack.domain.model.GoalPace
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.UserSex

@Entity
@Serializable
data class UserDetailsEntity(
    @PrimaryKey val id: Int = 0,
    val weight: Double,
    val age: Int,
    val height: Double,
    val gender: UserSex,
    val activityLevel: ActivityLevel,
    val goal: Goal,
    /** kg per month; older backups and rows from before AppDatabase.MIGRATION_12_13 get the default. */
    @ColumnInfo(defaultValue = "1.2") val goalPace: Double = GoalPace.DEFAULT_KG_PER_MONTH,
    val targetWeight: Double? = null,
    val targetStartWeight: Double? = null,
    val targetSetDate: LocalDate? = null,
    /** Added by AppDatabase.MIGRATION_13_14; older backups have none and use the formula. */
    val measuredMaintenance: Int? = null,
    /** Added by AppDatabase.MIGRATION_14_15, like the body fat and custom targets after it. */
    @ColumnInfo(defaultValue = "TARGET_WEIGHT") val macroBasis: MacroBasis = MacroBasis.TARGET_WEIGHT,
    val bodyFat: Double? = null,
    val customCalories: Int? = null,
    val customProtein: Int? = null,
    val customCarbs: Int? = null,
    val customFat: Int? = null
)

fun UserDetailsEntity.toDomain() = UserDetails(
    weight = weight,
    age = age,
    height = height,
    sex = gender,
    activityLevel = activityLevel,
    goal = goal,
    goalPace = goalPace,
    targetWeight = targetWeight,
    targetStartWeight = targetStartWeight,
    targetSetDate = targetSetDate,
    measuredMaintenance = measuredMaintenance,
    macroBasis = macroBasis,
    bodyFat = bodyFat,
    customMacroGoals = if (customCalories != null && customProtein != null && customCarbs != null && customFat != null) {
        MacroGoals(calories = customCalories, protein = customProtein, carbs = customCarbs, fat = customFat)
    } else {
        null
    }
)
