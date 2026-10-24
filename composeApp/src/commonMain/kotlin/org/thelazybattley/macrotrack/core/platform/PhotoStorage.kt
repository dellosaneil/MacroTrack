package org.thelazybattley.macrotrack.core.platform

import kotlinx.datetime.LocalDate

/** Photos are kept at most this many pixels on their long side; plenty for a phone screen and a before & after. */
const val PHOTO_MAX_EDGE = 1600

/** The small copy shown in grids and strips. */
const val PHOTO_THUMBNAIL_EDGE = 360

/** Part of a photo, as fractions of its width and height from the top left. */
data class CropRect(val left: Float, val top: Float, val width: Float, val height: Float)

/** A photo now kept in the app: its [fileName], and the day the image says it was taken, if it says. */
class StoredImage(val fileName: String, val takenOn: LocalDate?)

/**
 * Progress photos as files in the app's own folder, which isn't part of the photo library or the system's cloud
 * backups. Each is kept upright and shrunk to [PHOTO_MAX_EDGE], with a thumbnail beside it.
 */
interface PhotoStorage {

    /** Keeps the image in [bytes]; null when it can't be read as an image. */
    suspend fun store(bytes: ByteArray): StoredImage?

    suspend fun read(fileName: String, isThumbnail: Boolean): ByteArray?

    /**
     * The photo with everything but the person made transparent, as a PNG the size of the photo, for the camera's
     * ghost. Made on the phone the first time it's asked for, then kept. Null when no person is found.
     */
    suspend fun readCutout(fileName: String): ByteArray?

    /** Keeps [crop] of [fileName] as a new photo and returns its name; the original stays until it's deleted. */
    suspend fun crop(fileName: String, crop: CropRect): String?

    suspend fun delete(fileNames: Collection<String>)

    /** Every photo kept, by file name. */
    suspend fun fileNames(): Set<String>
}
