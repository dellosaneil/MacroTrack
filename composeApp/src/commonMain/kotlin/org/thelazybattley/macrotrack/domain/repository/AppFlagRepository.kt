package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.UnitSystem

interface AppFlagRepository {

    fun isTourCompleted(): Flow<Boolean>

    suspend fun setTourCompleted(isCompleted: Boolean)

    /** The unit system picked by the user, or the one usual for their region until they pick. */
    fun getUnitSystem(): Flow<UnitSystem>

    suspend fun setUnitSystem(unitSystem: UnitSystem)
}
