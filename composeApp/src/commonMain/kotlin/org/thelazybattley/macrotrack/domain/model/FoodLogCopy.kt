package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate

/** What a copy did, so it can be undone: the logs it added, and the ones "Replace it" removed. */
data class FoodLogCopy(
    val insertedIds: List<Long>,
    val replaced: List<FoodLog>,
    /** How many logs were copied to each day. */
    val itemCount: Int,
    val targetDates: List<LocalDate>
)

/**
 * [logs] as they land on each of [targetDates]: same portions, in [mealType], or in their own meals when null
 * (a whole day). Ids are left at 0 for the database to fill.
 */
fun copiedLogs(logs: List<FoodLog>, targetDates: List<LocalDate>, mealType: MealType?): List<FoodLog> =
    targetDates.flatMap { date ->
        logs.map { it.copy(id = 0, date = date, mealType = mealType ?: it.mealType) }
    }

/** The logs "Replace it" removes: [mealType] on each target day, or everything on it for a whole day. */
fun logsToReplace(existing: List<FoodLog>, targetDates: List<LocalDate>, mealType: MealType?): List<FoodLog> =
    existing.filter { it.date in targetDates && (mealType == null || it.mealType == mealType) }
