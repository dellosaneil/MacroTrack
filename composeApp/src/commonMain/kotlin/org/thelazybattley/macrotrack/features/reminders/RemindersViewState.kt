package org.thelazybattley.macrotrack.features.reminders

import org.thelazybattley.macrotrack.domain.model.LogReminderSettings
import org.thelazybattley.macrotrack.domain.model.MealType

data class RemindersViewState(
    val settings: LogReminderSettings = LogReminderSettings(),
    /** Around when each meal usually gets logged, from the last two weeks; left out without enough days to tell. */
    val usualMinutes: Map<MealType, Int> = emptyMap(),
    val canSendTest: Boolean = false,
    val isTestSent: Boolean = false
)
