package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.entity.WaterEntity

@Dao
interface WaterDao {

    @Insert
    suspend fun insertWater(water: WaterEntity): Long

    @Query("DELETE FROM WaterEntity WHERE id = :id")
    suspend fun deleteWater(id: Long)

    /** Drinks from [from] on, oldest first. */
    @Query("SELECT * FROM WaterEntity WHERE date >= :from ORDER BY loggedAt")
    fun getWaterSince(from: LocalDate): Flow<List<WaterEntity>>
}
