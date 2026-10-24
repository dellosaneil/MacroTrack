package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.PhotoAlbum
import org.thelazybattley.macrotrack.domain.model.ProgressPhoto

interface ProgressPhotoRepository {

    fun getAlbums(): Flow<List<PhotoAlbum>>

    fun getPhotos(): Flow<List<ProgressPhoto>>

    /** Adds [album], or updates it when it has an id. Returns its id. */
    suspend fun saveAlbum(album: PhotoAlbum): Long

    /** Removes the album, its photos and their images. */
    suspend fun deleteAlbum(id: Long)

    suspend fun insertPhotos(photos: List<ProgressPhoto>): List<Long>

    /** Removes the photo and its image. */
    suspend fun deletePhoto(photo: ProgressPhoto)

    /** Every album and photo, with their images. */
    suspend fun deleteAll()

    /** Removes images no photo uses, left behind by a save that never finished; [alsoKeep] are ones still being saved. */
    suspend fun deleteUnusedImages(alsoKeep: Set<String>)
}
