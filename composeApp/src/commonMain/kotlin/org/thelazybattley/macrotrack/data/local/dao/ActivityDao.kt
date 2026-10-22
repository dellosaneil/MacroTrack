package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.entity.ActivitySessionEntity
import org.thelazybattley.macrotrack.data.local.entity.StepHourEntity
import org.thelazybattley.macrotrack.domain.model.ActivityType

@Dao
interface ActivityDao {

    @Query("SELECT * FROM StepHourEntity WHERE date >= :fromDate")
    fun getStepHours(fromDate: LocalDate): Flow<List<StepHourEntity>>

    /** Adds [steps] to an hour, starting it at zero when it has none yet. */
    @Query(
        "INSERT INTO StepHourEntity (date, hour, steps) VALUES (:date, :hour, :steps) " +
            "ON CONFLICT(date, hour) DO UPDATE SET steps = steps + excluded.steps"
    )
    suspend fun addSteps(date: LocalDate, hour: Int, steps: Int)

    /** Replaces an hour's count, for phones that keep their own step history. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSteps(stepHour: StepHourEntity)

    @Insert
    suspend fun insertSession(session: ActivitySessionEntity): Long

    @Query("UPDATE ActivitySessionEntity SET name = :name, type = :type WHERE id = :id")
    suspend fun renameSession(id: Long, name: String?, type: ActivityType)

    @Query("DELETE FROM ActivitySessionEntity WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("SELECT * FROM ActivitySessionEntity ORDER BY startMillis DESC")
    fun getSessions(): Flow<List<ActivitySessionEntity>>

    @Query("SELECT * FROM ActivitySessionEntity WHERE id = :id")
    fun getSession(id: Long): Flow<ActivitySessionEntity?>
}
