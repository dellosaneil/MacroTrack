package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.data.local.entity.FavoriteEntity

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favoriteentity WHERE `key` = :key")
    suspend fun deleteFavorite(key: String)

    @Query("SELECT * FROM favoriteentity")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>
}
