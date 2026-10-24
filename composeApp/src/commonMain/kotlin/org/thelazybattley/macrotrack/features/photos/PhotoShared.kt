package org.thelazybattley.macrotrack.features.photos

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.PhotoAlbum
import org.thelazybattley.macrotrack.domain.model.PhotoPose
import org.thelazybattley.macrotrack.domain.model.ProgressPhoto
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.model.inDateOrder
import org.thelazybattley.macrotrack.domain.model.weightOnPhoto

/** A photo with the weigh-in shown on it. [isNew] marks one saved a moment ago. */
data class PhotoItem(
    val photo: ProgressPhoto,
    val weight: Weight?,
    val isNew: Boolean = false
)

/** An album with its photos in date order, and the first and latest that have a weight on them. */
data class AlbumSummary(
    val album: PhotoAlbum,
    val photos: List<PhotoItem>
) {
    val latest: PhotoItem? get() = photos.lastOrNull()

    private val weighed: List<PhotoItem> get() = photos.filter { it.weight != null }

    val firstWeighed: PhotoItem? get() = weighed.takeIf { it.size > 1 }?.first()

    val latestWeighed: PhotoItem? get() = weighed.takeIf { it.size > 1 }?.last()

    /** Latest minus first, in kg; null with fewer than two weighed photos. */
    val changeKg: Double?
        get() {
            val first = firstWeighed?.weight ?: return null
            val latest = latestWeighed?.weight ?: return null
            return latest.weight - first.weight
        }

    /** The pose most of the album is in, which new photos start on. */
    val usualPose: PhotoPose
        get() = photos.groupingBy { it.photo.pose }.eachCount().maxByOrNull { it.value }?.key ?: PhotoPose.FRONT
}

fun List<Weight>.byDate(): Map<LocalDate, Double> = associate { it.date to it.weight }

/** Albums in the order they were made, each with its photos and their weights. */
fun albumSummaries(
    albums: List<PhotoAlbum>,
    photos: List<ProgressPhoto>,
    weightsByDate: Map<LocalDate, Double>,
    newIds: Set<Long> = emptySet()
): List<AlbumSummary> {
    val byAlbum = photos.groupBy { it.albumId }
    return albums.map { album ->
        AlbumSummary(
            album = album,
            photos = byAlbum[album.id].orEmpty().inDateOrder().map { photo ->
                PhotoItem(photo = photo, weight = weightOnPhoto(photo = photo, weightsByDate = weightsByDate), isNew = photo.id in newIds)
            }
        )
    }
}

/** The album a new photo most likely belongs to: the one added to most recently, else the newest. */
fun List<AlbumSummary>.mostRecentlyUsed(): AlbumSummary? =
    maxByOrNull { summary -> summary.photos.maxOfOrNull { it.photo.createdAtMillis } ?: summary.album.createdAtMillis }
