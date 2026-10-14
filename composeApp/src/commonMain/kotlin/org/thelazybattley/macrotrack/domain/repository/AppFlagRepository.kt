package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow

interface AppFlagRepository {

    fun isTourCompleted(): Flow<Boolean>

    suspend fun setTourCompleted(isCompleted: Boolean)
}
