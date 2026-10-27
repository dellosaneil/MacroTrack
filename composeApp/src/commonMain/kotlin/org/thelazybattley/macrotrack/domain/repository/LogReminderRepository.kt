package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.LogReminderSettings
import org.thelazybattley.macrotrack.domain.model.LogReminderState

interface LogReminderRepository {

    /** The user's reminder settings; the defaults, turned off, until they change any. */
    fun getSettings(): Flow<LogReminderSettings>

    suspend fun saveSettings(settings: LogReminderSettings)

    fun getState(): Flow<LogReminderState>

    suspend fun updateState(update: (LogReminderState) -> LogReminderState)
}
