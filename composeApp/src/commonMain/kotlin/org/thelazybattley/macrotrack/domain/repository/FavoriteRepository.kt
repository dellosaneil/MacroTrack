package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {

    fun getFavoriteKeys(): Flow<Set<String>>

    suspend fun setFavorite(key: String, isFavorite: Boolean)
}
