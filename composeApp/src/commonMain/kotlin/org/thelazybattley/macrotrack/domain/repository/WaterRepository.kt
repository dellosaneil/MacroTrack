package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.WaterEntry
import org.thelazybattley.macrotrack.domain.model.WaterSettings

interface WaterRepository {

    /** Drinks from [from] on, oldest first. */
    fun getWaterSince(from: LocalDate): Flow<List<WaterEntry>>

    /** Returns the new drink's id. */
    suspend fun addWater(entry: WaterEntry): Long

    suspend fun deleteWater(id: Long)

    fun getSettings(): Flow<WaterSettings>

    suspend fun saveSettings(settings: WaterSettings)
}
