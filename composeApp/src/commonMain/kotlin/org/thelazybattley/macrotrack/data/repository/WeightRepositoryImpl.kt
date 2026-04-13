package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.thelazybattley.macrotrack.data.local.dao.WeightDao
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.model.toEntity
import org.thelazybattley.macrotrack.domain.repository.WeightRepository

class WeightRepositoryImpl(private val dao: WeightDao) : WeightRepository {
    override suspend fun insertWeight(weight: Weight) {
        dao.insertWeight(weight = weight.toEntity())
    }

    override fun getAllWeight(): Flow<List<Weight>> {
        return dao.getAllWeight().map { flow ->
            flow.map { entity ->
                entity.toDomain()
            }
        }
    }
}
