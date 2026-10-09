package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.thelazybattley.macrotrack.data.local.dao.FavoriteDao
import org.thelazybattley.macrotrack.data.local.entity.FavoriteEntity
import org.thelazybattley.macrotrack.domain.repository.FavoriteRepository

class FavoriteRepositoryImpl(private val dao: FavoriteDao) : FavoriteRepository {

    override fun getFavoriteKeys(): Flow<Set<String>> =
        dao.getAllFavorites().map { entities -> entities.map { it.key }.toSet() }

    override suspend fun setFavorite(key: String, isFavorite: Boolean) {
        if (isFavorite) {
            dao.insertFavorite(favorite = FavoriteEntity(key = key))
        } else {
            dao.deleteFavorite(key = key)
        }
    }
}
