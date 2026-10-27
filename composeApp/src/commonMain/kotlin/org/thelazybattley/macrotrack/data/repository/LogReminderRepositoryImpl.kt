package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.thelazybattley.macrotrack.data.local.dao.AppFlagDao
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.domain.model.LogReminderSettings
import org.thelazybattley.macrotrack.domain.model.LogReminderState
import org.thelazybattley.macrotrack.domain.repository.LogReminderRepository

/** Kept as JSON in app flags, so an unreadable value (from a newer version, say) falls back to the defaults. */
class LogReminderRepositoryImpl(private val dao: AppFlagDao) : LogReminderRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val stateLock = Mutex()

    override fun getSettings(): Flow<LogReminderSettings> = dao.getFlagValue(key = LOG_REMINDERS).map { value ->
        value?.let { runCatching { json.decodeFromString<LogReminderSettings>(string = it) }.getOrNull() } ?: LogReminderSettings()
    }

    override suspend fun saveSettings(settings: LogReminderSettings) {
        dao.insertFlag(flag = AppFlagEntity(key = LOG_REMINDERS, value = json.encodeToString(value = settings)))
    }

    override fun getState(): Flow<LogReminderState> = dao.getFlagValue(key = LOG_REMINDER_STATE).map { value ->
        value?.let { runCatching { json.decodeFromString<LogReminderState>(string = it) }.getOrNull() } ?: LogReminderState()
    }

    override suspend fun updateState(update: (LogReminderState) -> LogReminderState) {
        // A notification action and an alarm can land together; each sees the other's change.
        stateLock.withLock {
            val state = update(getState().first())
            dao.insertFlag(flag = AppFlagEntity(key = LOG_REMINDER_STATE, value = json.encodeToString(value = state)))
        }
    }

    companion object {
        const val LOG_REMINDERS = "log_reminders"
        const val LOG_REMINDER_STATE = "log_reminder_state"
    }
}
