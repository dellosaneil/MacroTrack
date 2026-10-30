package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.thelazybattley.macrotrack.data.local.dao.AppFlagDao
import org.thelazybattley.macrotrack.data.local.dao.WaterDao
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.data.local.entity.toEntity
import org.thelazybattley.macrotrack.domain.model.WaterEntry
import org.thelazybattley.macrotrack.domain.model.WaterSettings
import org.thelazybattley.macrotrack.domain.repository.WaterRepository

/** Settings are kept as JSON in app flags, so an unreadable value falls back to the defaults. */
class WaterRepositoryImpl(private val waterDao: WaterDao, private val flagDao: AppFlagDao) : WaterRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getWaterSince(from: LocalDate): Flow<List<WaterEntry>> =
        waterDao.getWaterSince(from = from).map { entries -> entries.map { it.toDomain() } }

    override suspend fun addWater(entry: WaterEntry): Long = waterDao.insertWater(water = entry.toEntity())

    override suspend fun deleteWater(id: Long) = waterDao.deleteWater(id = id)

    override fun getSettings(): Flow<WaterSettings> = flagDao.getFlagValue(key = WATER_SETTINGS).map { value ->
        value?.let { runCatching { json.decodeFromString<WaterSettings>(string = it) }.getOrNull() } ?: WaterSettings()
    }

    override suspend fun saveSettings(settings: WaterSettings) {
        flagDao.insertFlag(flag = AppFlagEntity(key = WATER_SETTINGS, value = json.encodeToString(value = settings)))
    }

    companion object {
        const val WATER_SETTINGS = "water_settings"
    }
}
