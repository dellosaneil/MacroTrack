package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity
import org.thelazybattley.macrotrack.domain.MacroGoals

data class UserDetails(
    val weight: Double,
    val age: Int,
    val height: Double,
    val sex: UserSex,
    val activityLevel: ActivityLevel,
    val goal: Goal,
    /** How fast to lose or gain, in kg per month. */
    val goalPace: Double = GoalPace.DEFAULT_KG_PER_MONTH,
    val targetWeight: Double? = null,
    val targetStartWeight: Double? = null,
    val targetSetDate: LocalDate? = null,
    /** The daily burn measured by a weekly check-in, used in place of the formula's estimate once applied. */
    val measuredMaintenance: Int? = null,
    /** What protein and fat are planned from in calculated targets. */
    val macroBasis: MacroBasis = MacroBasis.TARGET_WEIGHT,
    /** Body fat in percent, for [MacroBasis.LEAN_MASS]. */
    val bodyFat: Double? = null,
    /** Targets the user typed in; while set they replace the calculated ones. */
    val customMacroGoals: MacroGoals? = null
)

fun UserDetails.toEntity() = UserDetailsEntity(
    weight = weight,
    age = age,
    height = height,
    gender = sex,
    activityLevel = activityLevel,
    goal = goal,
    goalPace = goalPace,
    targetWeight = targetWeight,
    targetStartWeight = targetStartWeight,
    targetSetDate = targetSetDate,
    measuredMaintenance = measuredMaintenance,
    macroBasis = macroBasis,
    bodyFat = bodyFat,
    customCalories = customMacroGoals?.calories,
    customProtein = customMacroGoals?.protein,
    customCarbs = customMacroGoals?.carbs,
    customFat = customMacroGoals?.fat
)
