package org.thelazybattley.macrotrack.data.repository

import androidx.compose.ui.text.intl.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.thelazybattley.macrotrack.data.local.dao.AppFlagDao
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.domain.model.UnitSystem
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

    override fun getUnitSystem(): Flow<UnitSystem> = dao.getFlagValue(key = UNIT_SYSTEM).map { value ->
        UnitSystem.entries.firstOrNull { it.name == value } ?: UnitSystem.defaultFor(region = Locale.current.region)
    }

    override suspend fun setUnitSystem(unitSystem: UnitSystem) {
        dao.insertFlag(flag = AppFlagEntity(key = UNIT_SYSTEM, value = unitSystem.name))
    }

    override fun isTelemetryEnabled(): Flow<Boolean> = dao.isFlagSet(key = TELEMETRY_OPTED_OUT).map { !it }

    override suspend fun setTelemetryEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            dao.deleteFlag(key = TELEMETRY_OPTED_OUT)
        } else {
            dao.insertFlag(flag = AppFlagEntity(key = TELEMETRY_OPTED_OUT))
        }
    }

    companion object {
        /** Also seeded by AppDatabase.MIGRATION_8_9 for people who set up the app before the tour existed. */
        const val TOUR_COMPLETED = "tour_completed"
        const val UNIT_SYSTEM = "unit_system"
        const val TELEMETRY_OPTED_OUT = "telemetry_opted_out"
    }
}
