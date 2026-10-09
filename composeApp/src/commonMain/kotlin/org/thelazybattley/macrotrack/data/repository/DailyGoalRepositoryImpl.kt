package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.dao.DailyGoalDao
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.data.local.entity.toEntity
import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.repository.DailyGoalRepository

class DailyGoalRepositoryImpl(private val dao: DailyGoalDao) : DailyGoalRepository {

    override suspend fun saveDailyGoal(dailyGoal: DailyGoal) {
        dao.upsertDailyGoal(dailyGoal = dailyGoal.toEntity())
    }

    override suspend fun deleteDailyGoals(start: LocalDate, end: LocalDate) {
        dao.deleteDailyGoals(start = start, end = end)
    }

    override fun getAllDailyGoals(): Flow<List<DailyGoal>> =
        dao.getAllDailyGoals().map { entities -> entities.map { it.toDomain() } }
}
