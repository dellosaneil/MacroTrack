package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.data.local.entity.DailyGoalEntity

@Dao
interface DailyGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyGoal(dailyGoal: DailyGoalEntity)

    @Query("SELECT * FROM dailygoalentity ORDER BY date")
    fun getAllDailyGoals(): Flow<List<DailyGoalEntity>>
}
