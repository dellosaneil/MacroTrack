package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity

@Dao
interface AppFlagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlag(flag: AppFlagEntity)

    @Query("DELETE FROM appflagentity WHERE `key` = :key")
    suspend fun deleteFlag(key: String)

    @Query("SELECT EXISTS(SELECT 1 FROM appflagentity WHERE `key` = :key)")
    fun isFlagSet(key: String): Flow<Boolean>
}
