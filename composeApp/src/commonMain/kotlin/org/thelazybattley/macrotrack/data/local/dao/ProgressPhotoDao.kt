package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.data.local.entity.PhotoAlbumEntity
import org.thelazybattley.macrotrack.data.local.entity.ProgressPhotoEntity

@Dao
abstract class ProgressPhotoDao {

    @Query("SELECT * FROM PhotoAlbumEntity ORDER BY createdAtMillis")
    abstract fun getAlbums(): Flow<List<PhotoAlbumEntity>>

    @Query("SELECT * FROM ProgressPhotoEntity")
    abstract fun getPhotos(): Flow<List<ProgressPhotoEntity>>

    @Query("SELECT fileName FROM ProgressPhotoEntity")
    abstract suspend fun getFileNames(): List<String>

    @Query("SELECT fileName FROM ProgressPhotoEntity WHERE albumId = :albumId")
    abstract suspend fun getFileNames(albumId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAlbum(album: PhotoAlbumEntity): Long

    @Insert
    abstract suspend fun insertPhotos(photos: List<ProgressPhotoEntity>): List<Long>

    @Query("DELETE FROM ProgressPhotoEntity WHERE id = :id")
    abstract suspend fun deletePhoto(id: Long)

    @Query("DELETE FROM ProgressPhotoEntity WHERE albumId = :albumId")
    protected abstract suspend fun deletePhotosOf(albumId: Long)

    @Query("DELETE FROM PhotoAlbumEntity WHERE id = :id")
    protected abstract suspend fun deleteAlbumRow(id: Long)

    @Query("DELETE FROM ProgressPhotoEntity")
    protected abstract suspend fun deletePhotos()

    @Query("DELETE FROM PhotoAlbumEntity")
    protected abstract suspend fun deleteAlbums()

    @Transaction
    open suspend fun deleteAlbum(id: Long) {
        deletePhotosOf(albumId = id)
        deleteAlbumRow(id = id)
    }

    @Transaction
    open suspend fun deleteAll() {
        deletePhotos()
        deleteAlbums()
    }
}
