package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import kotlinx.serialization.Serializable
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.model.Goal

@Entity
@Serializable
data class DailyGoalEntity(
    @PrimaryKey val date: LocalDate,
    val goal: Goal,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

fun DailyGoalEntity.toDomain() = DailyGoal(
    date = date,
    goal = goal,
    macroGoals = MacroGoals(calories = calories, protein = protein, carbs = carbs, fat = fat)
)

fun DailyGoal.toEntity() = DailyGoalEntity(
    date = date,
    goal = goal,
    calories = macroGoals.calories,
    protein = macroGoals.protein,
    carbs = macroGoals.carbs,
    fat = macroGoals.fat
)
