package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.PlanFood
import org.thelazybattley.macrotrack.domain.model.RepeatingMeal

/** Added by AppDatabase.MIGRATION_15_16. */
@Entity
@Serializable
data class RepeatingMealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mealType: MealType,
    /** ISO day numbers, Monday = 1: "1,2,3,4,5" for weekdays. */
    val days: String,
    val foods: List<PlanFood>,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val maxTimes: Int? = null,
    val isAutoLog: Boolean = false,
    val isActive: Boolean = true,
    val updatedOn: LocalDate
)

fun RepeatingMealEntity.toDomain() = RepeatingMeal(
    id = id,
    name = name,
    mealType = mealType,
    days = days.split(",").mapNotNull { number -> number.trim().toIntOrNull()?.let { DayOfWeek(isoDayNumber = it) } }.toSet(),
    foods = foods,
    startDate = startDate,
    endDate = endDate,
    maxTimes = maxTimes,
    isAutoLog = isAutoLog,
    isActive = isActive,
    updatedOn = updatedOn
)

fun RepeatingMeal.toEntity() = RepeatingMealEntity(
    id = id,
    name = name,
    mealType = mealType,
    days = days.map { it.isoDayNumber }.sorted().joinToString(separator = ","),
    foods = foods,
    startDate = startDate,
    endDate = endDate,
    maxTimes = maxTimes,
    isAutoLog = isAutoLog,
    isActive = isActive,
    updatedOn = updatedOn
)
