package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.Weight

interface WeightRepository {

    suspend fun insertWeight(
        weight: Weight,
    )

    fun getAllWeight(): Flow<List<Weight>>

}
