package org.thelazybattley.macrotrack.features.photos

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.CropRect
import org.thelazybattley.macrotrack.core.platform.PhotoStorage
import org.thelazybattley.macrotrack.domain.model.PHOTO_PICK_MAX
import org.thelazybattley.macrotrack.domain.model.PhotoPose

/** A photo kept in the app but not saved to an album yet. [date] starts on the day it was taken, or today. */
data class PhotoDraft(
    val fileName: String,
    val takenOn: LocalDate?,
    val date: LocalDate,
    val pose: PhotoPose,
    val note: String = "",
    /** A nearby weigh-in the user chose for a day without one. */
    val weightDate: LocalDate? = null
)

/** Photos on their way to the Save screen, from the camera or the photo library, for [albumId] unless changed there. */
data class PhotoDraftBatch(
    val albumId: Long?,
    val drafts: List<PhotoDraft>,
    val isFromLibrary: Boolean
)

/** What was just saved, for the album's confirmation and the New tags. */
data class SavedPhotos(
    val albumId: Long,
    val ids: Set<Long>,
    val firstDate: LocalDate,
    val firstWeightKg: Double?
)

/**
 * Hands photos from the camera and the photo library to the Save screen. The images are kept in the app as soon as
 * they arrive, so leaving Save without saving has to [discard] them.
 */
class PhotoDrafts(private val storage: PhotoStorage) {

    private val scope = CoroutineScope(context = SupervisorJob() + Dispatchers.Default)

    private val _batch = MutableStateFlow<PhotoDraftBatch?>(value = null)
    val batch = _batch.asStateFlow()

    private val _saved = MutableStateFlow<SavedPhotos?>(value = null)
    val saved = _saved.asStateFlow()

    /** Images still waiting on Save, which tidying up mustn't remove. */
    val pendingFileNames: Set<String> get() = _batch.value?.drafts?.map { it.fileName }?.toSet().orEmpty()

    /** Keeps a photo just taken; false when the camera's image couldn't be read. */
    suspend fun fromCamera(bytes: ByteArray, albumId: Long?, pose: PhotoPose): Boolean {
        val stored = storage.store(bytes = bytes) ?: return false
        replace(
            batch = PhotoDraftBatch(
                albumId = albumId,
                drafts = listOf(PhotoDraft(fileName = stored.fileName, takenOn = null, date = getCurrentDate(), pose = pose)),
                isFromLibrary = false
            )
        )
        return true
    }

    /**
     * Keeps photos picked from the library, oldest first, each dated the day it was taken (never later than today).
     * False when none could be read.
     */
    suspend fun fromLibrary(images: List<ByteArray>, albumId: Long?, pose: PhotoPose): Boolean {
        val today = getCurrentDate()
        val drafts = images.take(n = PHOTO_PICK_MAX).mapNotNull { bytes ->
            storage.store(bytes = bytes)?.let { stored ->
                val takenOn = stored.takenOn?.takeIf { it <= today }
                PhotoDraft(fileName = stored.fileName, takenOn = takenOn, date = takenOn ?: today, pose = pose)
            }
        }.sortedBy { it.date }
        if (drafts.isEmpty()) return false
        replace(batch = PhotoDraftBatch(albumId = albumId, drafts = drafts, isFromLibrary = true))
        return true
    }

    /**
     * Crops a waiting photo, which then takes the original's place in the batch; the original's image goes. Returns
     * the cropped photo's name, or null if it couldn't be cropped.
     */
    suspend fun crop(fileName: String, crop: CropRect): String? {
        val cropped = storage.crop(fileName = fileName, crop = crop) ?: return null
        _batch.value = _batch.value?.let { batch ->
            batch.copy(drafts = batch.drafts.map { if (it.fileName == fileName) it.copy(fileName = cropped) else it })
        }
        storage.delete(fileNames = listOf(fileName))
        return cropped
    }

    /** Drops the waiting photos; their images go in the background, so leaving the screen doesn't cut it short. */
    fun discard() {
        val fileNames = pendingFileNames
        _batch.value = null
        scope.launch { storage.delete(fileNames = fileNames) }
    }

    /** The waiting photos are in an album now, so their images stay. */
    fun onSaved(saved: SavedPhotos) {
        _batch.value = null
        _saved.value = saved
    }

    fun clearSaved() {
        _saved.value = null
    }

    private suspend fun replace(batch: PhotoDraftBatch) {
        val previous = pendingFileNames
        _batch.value = batch
        storage.delete(fileNames = previous)
    }
}
