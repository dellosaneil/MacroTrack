package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.CustomMilestone
import org.thelazybattley.macrotrack.domain.model.ForecastSettings
import org.thelazybattley.macrotrack.domain.model.ReviewHistory
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.WhatFitsSettings

interface AppFlagRepository {

    fun isTourCompleted(): Flow<Boolean>

    suspend fun setTourCompleted(isCompleted: Boolean)

    /** The unit system picked by the user, or the one usual for their region until they pick. */
    fun getUnitSystem(): Flow<UnitSystem>

    suspend fun setUnitSystem(unitSystem: UnitSystem)

    /** Whether crash reports and usage analytics may be sent. On until the user turns it off. */
    fun isTelemetryEnabled(): Flow<Boolean>

    suspend fun setTelemetryEnabled(isEnabled: Boolean)

    /** When the user was last asked to rate the app, and how many times. */
    fun getReviewHistory(): Flow<ReviewHistory>

    suspend fun recordReviewAsked(date: LocalDate)

    /** The check-in's suggested target when the user last looked at it, so a changed one can show as new. */
    fun getSeenCheckInSuggestion(): Flow<Int?>

    suspend fun setSeenCheckInSuggestion(calories: Int)

    /** Whether the weight widget shows the weight itself; off, it shows only the change. On until turned off. */
    fun isWidgetWeightShown(): Flow<Boolean>

    suspend fun setWidgetWeightShown(isShown: Boolean)

    /** Whether iPhone Lock Screen widgets show numbers while the phone is locked. On until turned off. */
    fun isWidgetLockNumbersShown(): Flow<Boolean>

    suspend fun setWidgetLockNumbersShown(isShown: Boolean)

    fun getForecastSettings(): Flow<ForecastSettings>

    suspend fun setForecastSettings(settings: ForecastSettings)

    /** Milestones the user added to the goal forecast, in the order added. */
    fun getCustomMilestones(): Flow<List<CustomMilestone>>

    suspend fun setCustomMilestones(milestones: List<CustomMilestone>)

    fun getWhatFitsSettings(): Flow<WhatFitsSettings>

    suspend fun setWhatFitsSettings(settings: WhatFitsSettings)
}
