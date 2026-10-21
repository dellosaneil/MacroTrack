package org.thelazybattley.macrotrack.features.copy

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.domain.model.MealType

/**
 * Copying [mealType] from [sourceDate], or the whole day when it's null. Every log starts picked; [unchecked]
 * holds the ones the user left out.
 */
data class CopyToViewState(
    val sourceDate: LocalDate,
    val mealType: MealType?,
    val today: LocalDate,
    val sourceLogs: List<FoodLog> = emptyList(),
    val unchecked: Set<Long> = emptySet(),
    val targetDates: Set<LocalDate> = emptySet(),
    /** The meal the copies go into; a whole day keeps each log's meal. */
    val targetMeal: MealType? = mealType,
    val isReplace: Boolean = false,
    val allLogs: List<FoodLog> = emptyList(),
    val foodUnit: Map<String, String> = emptyMap(),
    val isDone: Boolean = false
) {
    val isMeal: Boolean get() = mealType != null

    val selectedLogs: List<FoodLog> get() = sourceLogs.filter { it.id !in unchecked }

    val selectedCalories: Int get() = selectedLogs.sumOf { it.calories }

    /** The recent days to pick from, newest first, plus any older day picked from the calendar. */
    val stripDates: List<LocalDate>
        get() {
            val recent = (0 until STRIP_DAYS).map { today.minus(period = DatePeriod(days = it)) }
            return (recent + targetDates.filter { it !in recent }.sortedDescending()).filter { it != sourceDate }
        }

    fun isLogged(date: LocalDate): Boolean = allLogs.any { it.date == date }

    /** What a target day already has where the copies would go. */
    fun existingOn(date: LocalDate): List<FoodLog> =
        allLogs.filter { it.date == date && (targetMeal == null || it.mealType == targetMeal) }

    val conflictDates: List<LocalDate>
        get() = targetDates.filter { existingOn(date = it).isNotEmpty() }.sortedDescending()

    val hasConflict: Boolean get() = conflictDates.isNotEmpty() && selectedLogs.isNotEmpty()

    val canCopy: Boolean get() = selectedLogs.isNotEmpty() && targetDates.isNotEmpty()

    companion object {
        const val STRIP_DAYS = 8
    }
}
