package org.thelazybattley.macrotrack.domain.usecase.photo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.thelazybattley.macrotrack.core.platform.PhotoReminderScheduler
import org.thelazybattley.macrotrack.domain.model.PhotoAlbum
import org.thelazybattley.macrotrack.domain.model.ProgressPhoto
import org.thelazybattley.macrotrack.domain.repository.ProgressPhotoRepository

class GetPhotoAlbumsUseCase(private val repository: ProgressPhotoRepository) {
    operator fun invoke(): Flow<List<PhotoAlbum>> = repository.getAlbums()
}

class GetProgressPhotosUseCase(private val repository: ProgressPhotoRepository) {
    operator fun invoke(): Flow<List<ProgressPhoto>> = repository.getPhotos()
}

/** Adds or changes an album and sets its reminders again. Returns the album's id. */
class SavePhotoAlbumUseCase(
    private val repository: ProgressPhotoRepository,
    private val scheduler: PhotoReminderScheduler
) {
    suspend operator fun invoke(album: PhotoAlbum): Long {
        val id = repository.saveAlbum(album = album.copy(name = album.name.trim()))
        scheduler.schedule(albums = repository.getAlbums().first())
        return id
    }
}

class DeletePhotoAlbumUseCase(
    private val repository: ProgressPhotoRepository,
    private val scheduler: PhotoReminderScheduler
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteAlbum(id = id)
        scheduler.schedule(albums = repository.getAlbums().first())
    }
}

class SaveProgressPhotosUseCase(private val repository: ProgressPhotoRepository) {
    suspend operator fun invoke(photos: List<ProgressPhoto>): List<Long> =
        if (photos.isEmpty()) emptyList() else repository.insertPhotos(photos = photos)
}

class DeleteProgressPhotoUseCase(private val repository: ProgressPhotoRepository) {
    suspend operator fun invoke(photo: ProgressPhoto) = repository.deletePhoto(photo = photo)
}

/** A restart clears the reminders, so they're set again each time the app opens. */
class SchedulePhotoRemindersUseCase(
    private val repository: ProgressPhotoRepository,
    private val scheduler: PhotoReminderScheduler
) {
    suspend operator fun invoke() = scheduler.schedule(albums = repository.getAlbums().first())
}
