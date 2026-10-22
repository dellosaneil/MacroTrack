package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.ActivitySession
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.StepHour

interface ActivityRepository {

    /** Hourly step counts from [fromDate] on. */
    fun getStepHours(fromDate: LocalDate): Flow<List<StepHour>>

    /** Adds to an hour's count, for step counters that only say how many steps since the last reading. */
    suspend fun addSteps(date: LocalDate, hour: Int, steps: Int)

    /** Replaces an hour's count, for phones that keep their own step history. */
    suspend fun setSteps(date: LocalDate, hour: Int, steps: Int)

    /** Walks and runs, newest first. */
    fun getSessions(): Flow<List<ActivitySession>>

    fun getSession(id: Long): Flow<ActivitySession?>

    suspend fun insertSession(session: ActivitySession): Long

    suspend fun renameSession(id: Long, name: String?, type: ActivityType)

    suspend fun deleteSession(id: Long)

    fun getStepGoal(): Flow<Int>

    suspend fun setStepGoal(goal: Int)

    /** The 6 PM reminder when the goal is far off. On until turned off. */
    fun isStepNudgeEnabled(): Flow<Boolean>

    suspend fun setStepNudgeEnabled(isEnabled: Boolean)

    /** Adding calories burned to the food budget. Off until turned on. */
    fun isBurnAddedToBudget(): Flow<Boolean>

    suspend fun setBurnAddedToBudget(isAdded: Boolean)

    /** What a step counter that only counts up last read, and when; it belongs to this phone. */
    suspend fun getStepSensorReading(): StepSensorReading?

    suspend fun setStepSensorReading(reading: StepSensorReading)

    /** How far a phone that keeps its own step history has been copied in. */
    suspend fun getStepsSyncedUntil(): Long?

    suspend fun setStepsSyncedUntil(millis: Long)
}

/** A step counter's running total, and when it was read. */
data class StepSensorReading(val count: Long, val atMillis: Long)
