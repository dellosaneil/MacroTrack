package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.thelazybattley.macrotrack.data.local.dao.ActivityDao
import org.thelazybattley.macrotrack.data.local.dao.AppFlagDao
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.data.local.entity.StepHourEntity
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.data.local.entity.toEntity
import org.thelazybattley.macrotrack.domain.model.ActivitySession
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.StepGoal
import org.thelazybattley.macrotrack.domain.model.StepHour
import org.thelazybattley.macrotrack.domain.model.StepNotificationSettings
import org.thelazybattley.macrotrack.domain.repository.ActivityRepository
import org.thelazybattley.macrotrack.domain.repository.StepSensorReading

class ActivityRepositoryImpl(
    private val activityDao: ActivityDao,
    private val appFlagDao: AppFlagDao
) : ActivityRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getStepHours(fromDate: LocalDate): Flow<List<StepHour>> =
        activityDao.getStepHours(fromDate = fromDate).map { hours -> hours.map { it.toDomain() } }

    override suspend fun addSteps(date: LocalDate, hour: Int, steps: Int) {
        if (steps > 0) activityDao.addSteps(date = date, hour = hour, steps = steps)
    }

    override suspend fun setSteps(date: LocalDate, hour: Int, steps: Int) {
        activityDao.setSteps(stepHour = StepHourEntity(date = date, hour = hour, steps = steps))
    }

    override fun getSessions(): Flow<List<ActivitySession>> =
        activityDao.getSessions().map { sessions -> sessions.map { it.toDomain() } }

    override fun getSession(id: Long): Flow<ActivitySession?> = activityDao.getSession(id = id).map { it?.toDomain() }

    override suspend fun insertSession(session: ActivitySession): Long = activityDao.insertSession(session = session.toEntity())

    override suspend fun renameSession(id: Long, name: String?, type: ActivityType) {
        activityDao.renameSession(id = id, name = name, type = type)
    }

    override suspend fun deleteSession(id: Long) {
        activityDao.deleteSession(id = id)
    }

    override fun getStepGoal(): Flow<Int> = appFlagDao.getFlagValue(key = STEP_GOAL).map { value ->
        value?.toIntOrNull()?.takeIf { StepGoal.isValid(goal = it) } ?: StepGoal.DEFAULT
    }

    override suspend fun setStepGoal(goal: Int) {
        appFlagDao.insertFlag(flag = AppFlagEntity(key = STEP_GOAL, value = goal.toString()))
    }

    override fun isStepNudgeEnabled(): Flow<Boolean> = appFlagDao.isFlagSet(key = STEP_NUDGE_OFF).map { !it }

    override suspend fun setStepNudgeEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            appFlagDao.deleteFlag(key = STEP_NUDGE_OFF)
        } else {
            appFlagDao.insertFlag(flag = AppFlagEntity(key = STEP_NUDGE_OFF))
        }
    }

    override fun getStepNotificationSettings(): Flow<StepNotificationSettings> =
        appFlagDao.getFlagValue(key = STEP_NOTIFICATIONS).map { value ->
            value?.let { runCatching { json.decodeFromString<StepNotificationSettings>(string = it) }.getOrNull() }
                ?: StepNotificationSettings()
        }

    override suspend fun setStepNotificationSettings(settings: StepNotificationSettings) {
        appFlagDao.insertFlag(flag = AppFlagEntity(key = STEP_NOTIFICATIONS, value = json.encodeToString(value = settings)))
    }

    override suspend fun getStepNotificationHiddenOn(): LocalDate? = getDateFlag(key = STEP_NOTIFICATION_HIDDEN_ON)

    override suspend fun setStepNotificationHiddenOn(date: LocalDate?) {
        if (date == null) {
            appFlagDao.deleteFlag(key = STEP_NOTIFICATION_HIDDEN_ON)
        } else {
            appFlagDao.insertFlag(flag = AppFlagEntity(key = STEP_NOTIFICATION_HIDDEN_ON, value = date.toString()))
        }
    }

    override suspend fun getGoalAlertSentOn(): LocalDate? = getDateFlag(key = GOAL_ALERT_SENT_ON)

    override suspend fun setGoalAlertSentOn(date: LocalDate) {
        appFlagDao.insertFlag(flag = AppFlagEntity(key = GOAL_ALERT_SENT_ON, value = date.toString()))
    }

    private suspend fun getDateFlag(key: String): LocalDate? =
        appFlagDao.getFlagValue(key = key).first()?.let { runCatching { LocalDate.parse(input = it) }.getOrNull() }

    override fun isBurnAddedToBudget(): Flow<Boolean> = appFlagDao.isFlagSet(key = BURN_ADDED_TO_BUDGET)

    override suspend fun setBurnAddedToBudget(isAdded: Boolean) {
        if (isAdded) {
            appFlagDao.insertFlag(flag = AppFlagEntity(key = BURN_ADDED_TO_BUDGET))
        } else {
            appFlagDao.deleteFlag(key = BURN_ADDED_TO_BUDGET)
        }
    }

    override suspend fun getStepSensorReading(): StepSensorReading? {
        val value = appFlagDao.getFlagValue(key = STEP_SENSOR_READING).first() ?: return null
        val (count, atMillis) = value.split(",").takeIf { it.size == 2 } ?: return null
        return StepSensorReading(count = count.toLongOrNull() ?: return null, atMillis = atMillis.toLongOrNull() ?: return null)
    }

    override suspend fun setStepSensorReading(reading: StepSensorReading) {
        appFlagDao.insertFlag(flag = AppFlagEntity(key = STEP_SENSOR_READING, value = "${reading.count},${reading.atMillis}"))
    }

    override suspend fun getStepsSyncedUntil(): Long? = appFlagDao.getFlagValue(key = STEPS_SYNCED_UNTIL).first()?.toLongOrNull()

    override suspend fun setStepsSyncedUntil(millis: Long) {
        appFlagDao.insertFlag(flag = AppFlagEntity(key = STEPS_SYNCED_UNTIL, value = millis.toString()))
    }

    companion object {
        const val STEP_GOAL = "step_goal"
        const val STEP_NUDGE_OFF = "step_nudge_off"
        const val BURN_ADDED_TO_BUDGET = "burn_added_to_budget"
        const val STEP_NOTIFICATIONS = "step_notifications"

        /** What this phone's notifications already did today: never backed up. */
        const val STEP_NOTIFICATION_HIDDEN_ON = "step_notification_hidden_on"
        const val GOAL_ALERT_SENT_ON = "goal_alert_sent_on"

        /** The phone's own step counter readings: meaningless on another phone, so never backed up. */
        const val STEP_SENSOR_READING = "step_sensor_reading"
        const val STEPS_SYNCED_UNTIL = "steps_synced_until"
    }
}
