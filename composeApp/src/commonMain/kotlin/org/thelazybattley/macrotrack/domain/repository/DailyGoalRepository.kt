package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.DailyGoal

interface DailyGoalRepository {

    suspend fun saveDailyGoal(dailyGoal: DailyGoal)

    /** Removes the goals recorded from [start] to [end], inclusive. */
    suspend fun deleteDailyGoals(start: LocalDate, end: LocalDate)

    fun getAllDailyGoals(): Flow<List<DailyGoal>>
}
