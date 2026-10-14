package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.data.local.dao.AppFlagDao
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository

class AppFlagRepositoryImpl(private val dao: AppFlagDao) : AppFlagRepository {

    override fun isTourCompleted(): Flow<Boolean> = dao.isFlagSet(key = TOUR_COMPLETED)

    override suspend fun setTourCompleted(isCompleted: Boolean) {
        if (isCompleted) {
            dao.insertFlag(flag = AppFlagEntity(key = TOUR_COMPLETED))
        } else {
            dao.deleteFlag(key = TOUR_COMPLETED)
        }
    }

    companion object {
        /** Also seeded by AppDatabase.MIGRATION_8_9 for people who set up the app before the tour existed. */
        const val TOUR_COMPLETED = "tour_completed"
    }
}
