package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.ReviewHistory
import org.thelazybattley.macrotrack.domain.model.UnitSystem

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
}
