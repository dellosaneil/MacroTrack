package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.DailyGoal

interface DailyGoalRepository {

    suspend fun saveDailyGoal(dailyGoal: DailyGoal)

    fun getAllDailyGoals(): Flow<List<DailyGoal>>
}
