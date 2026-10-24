package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.thelazybattley.macrotrack.core.platform.PhotoStorage
import org.thelazybattley.macrotrack.data.local.dao.ProgressPhotoDao
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.data.local.entity.toEntity
import org.thelazybattley.macrotrack.domain.model.PhotoAlbum
import org.thelazybattley.macrotrack.domain.model.ProgressPhoto
import org.thelazybattley.macrotrack.domain.repository.ProgressPhotoRepository

class ProgressPhotoRepositoryImpl(
    private val dao: ProgressPhotoDao,
    private val storage: PhotoStorage
) : ProgressPhotoRepository {

    override fun getAlbums(): Flow<List<PhotoAlbum>> = dao.getAlbums().map { albums -> albums.map { it.toDomain() } }

    override fun getPhotos(): Flow<List<ProgressPhoto>> = dao.getPhotos().map { photos -> photos.map { it.toDomain() } }

    override suspend fun saveAlbum(album: PhotoAlbum): Long = dao.upsertAlbum(album = album.toEntity())

    override suspend fun deleteAlbum(id: Long) {
        val fileNames = dao.getFileNames(albumId = id)
        dao.deleteAlbum(id = id)
        storage.delete(fileNames = fileNames)
    }

    override suspend fun insertPhotos(photos: List<ProgressPhoto>): List<Long> =
        dao.insertPhotos(photos = photos.map { it.toEntity() })

    override suspend fun deletePhoto(photo: ProgressPhoto) {
        dao.deletePhoto(id = photo.id)
        storage.delete(fileNames = listOf(photo.fileName))
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
        storage.delete(fileNames = storage.fileNames())
    }

    override suspend fun deleteUnusedImages(alsoKeep: Set<String>) {
        val inUse = dao.getFileNames().toSet() + alsoKeep
        storage.delete(fileNames = storage.fileNames() - inUse)
    }
}
