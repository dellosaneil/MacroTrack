package org.thelazybattley.macrotrack.features.photos

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.CropRect
import org.thelazybattley.macrotrack.core.platform.PhotoStorage
import org.thelazybattley.macrotrack.core.platform.StoredImage
import org.thelazybattley.macrotrack.domain.model.PhotoAlbum
import org.thelazybattley.macrotrack.domain.model.PhotoPose
import org.thelazybattley.macrotrack.domain.model.ProgressPhoto
import org.thelazybattley.macrotrack.runSync
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PhotoSharedTest {

    private val push = PhotoAlbum(id = 1, name = "Push Day Pump Check", createdAtMillis = 10)
    private val monthly = PhotoAlbum(id = 2, name = "Monthly", createdAtMillis = 20)

    private fun photo(id: Long, albumId: Long, date: LocalDate, pose: PhotoPose = PhotoPose.FLEX, createdAtMillis: Long = id) =
        ProgressPhoto(id = id, albumId = albumId, date = date, pose = pose, fileName = "f$id", createdAtMillis = createdAtMillis)

    @Test
    fun anAlbumMeasuresFromItsFirstToLatestWeighedPhoto() {
        val photos = listOf(
            photo(id = 1, albumId = 1, date = LocalDate(2026, 7, 7)),
            photo(id = 2, albumId = 1, date = LocalDate(2026, 8, 30)),
            photo(id = 3, albumId = 1, date = LocalDate(2026, 10, 20))
        )
        val weights = mapOf(LocalDate(2026, 7, 7) to 78.4, LocalDate(2026, 10, 20) to 73.8)
        val summary = albumSummaries(albums = listOf(push), photos = photos, weightsByDate = weights).single()
        assertEquals(3, summary.photos.size)
        assertNull(summary.photos[1].weight)
        assertEquals(78.4 - 73.8, -summary.changeKg!!, absoluteTolerance = 1e-9)
        assertEquals(1, summary.firstWeighed?.photo?.id)
        assertEquals(3, summary.latestWeighed?.photo?.id)
    }

    @Test
    fun oneWeighedPhotoIsNoChangeYet() {
        val photos = listOf(photo(id = 1, albumId = 1, date = LocalDate(2026, 7, 7)))
        val summary = albumSummaries(albums = listOf(push), photos = photos, weightsByDate = mapOf(LocalDate(2026, 7, 7) to 78.4)).single()
        assertNull(summary.changeKg)
    }

    @Test
    fun newPhotosStartOnTheAlbumsUsualPose() {
        val photos = listOf(
            photo(id = 1, albumId = 1, date = LocalDate(2026, 7, 7), pose = PhotoPose.SIDE),
            photo(id = 2, albumId = 1, date = LocalDate(2026, 7, 8), pose = PhotoPose.BACK),
            photo(id = 3, albumId = 1, date = LocalDate(2026, 7, 9), pose = PhotoPose.BACK)
        )
        assertEquals(PhotoPose.BACK, albumSummaries(albums = listOf(push), photos = photos, weightsByDate = emptyMap()).single().usualPose)
        assertEquals(PhotoPose.FRONT, albumSummaries(albums = listOf(push), photos = emptyList(), weightsByDate = emptyMap()).single().usualPose)
    }

    @Test
    fun plusPhotoAddsToTheAlbumAddedToLast() {
        val photos = listOf(
            photo(id = 1, albumId = 1, date = LocalDate(2026, 7, 7), createdAtMillis = 500),
            photo(id = 2, albumId = 2, date = LocalDate(2026, 9, 1), createdAtMillis = 100)
        )
        assertEquals(push, albumSummaries(albums = listOf(push, monthly), photos = photos, weightsByDate = emptyMap()).mostRecentlyUsed()?.album)
        // With no photos anywhere, the newest album.
        assertEquals(monthly, albumSummaries(albums = listOf(push, monthly), photos = emptyList(), weightsByDate = emptyMap()).mostRecentlyUsed()?.album)
    }

    @Test
    fun justSavedPhotosAreMarkedNew() {
        val photos = listOf(photo(id = 1, albumId = 1, date = LocalDate(2026, 7, 7)), photo(id = 2, albumId = 1, date = LocalDate(2026, 7, 8)))
        val summary = albumSummaries(albums = listOf(push), photos = photos, weightsByDate = emptyMap(), newIds = setOf(2)).single()
        assertEquals(listOf(false, true), summary.photos.map { it.isNew })
    }

    /** Hands out file names in order, with the dates queued for them; records what's deleted. */
    private class FakeStorage(private val dates: List<LocalDate?>) : PhotoStorage {
        val deleted = mutableSetOf<String>()
        private var next = 0

        override suspend fun store(bytes: ByteArray): StoredImage? {
            if (bytes.isEmpty()) return null
            val index = next++
            return StoredImage(fileName = "img$index", takenOn = dates.getOrNull(index = index))
        }

        override suspend fun read(fileName: String, isThumbnail: Boolean): ByteArray? = null
        override suspend fun readCutout(fileName: String): ByteArray? = null
        override suspend fun crop(fileName: String, crop: CropRect): String = "${fileName}_c"
        override suspend fun delete(fileNames: Collection<String>) { deleted += fileNames }
        override suspend fun fileNames(): Set<String> = emptySet()
    }

    @Test
    fun libraryPhotosAreDatedWhenTakenAndSortedOldestFirst() = runSync {
        val today = getCurrentDate()
        val storage = FakeStorage(dates = listOf(LocalDate(2026, 10, 10), null, LocalDate(2026, 9, 2), today.plus(period = DatePeriod(days = 3))))
        val drafts = PhotoDrafts(storage = storage)
        val isReady = drafts.fromLibrary(images = List(size = 5) { if (it == 4) ByteArray(size = 0) else byteArrayOf(1) }, albumId = 1, pose = PhotoPose.SIDE)
        assertTrue(isReady)
        val batch = drafts.batch.value!!
        assertTrue(batch.isFromLibrary)
        // The unreadable one is left out; no date or one in the future means today.
        assertEquals(listOf("img2", "img0", "img1", "img3"), batch.drafts.map { it.fileName })
        assertEquals(listOf(LocalDate(2026, 9, 2), LocalDate(2026, 10, 10), today, today), batch.drafts.map { it.date })
        assertEquals(listOf(LocalDate(2026, 9, 2), LocalDate(2026, 10, 10), null, null), batch.drafts.map { it.takenOn })
        assertTrue(batch.drafts.all { it.pose == PhotoPose.SIDE })
    }

    @Test
    fun aNewBatchDropsTheImagesOfTheOneItReplaces() = runSync {
        val storage = FakeStorage(dates = emptyList())
        val drafts = PhotoDrafts(storage = storage)
        drafts.fromCamera(bytes = byteArrayOf(1), albumId = 1, pose = PhotoPose.FRONT)
        drafts.fromCamera(bytes = byteArrayOf(1), albumId = 1, pose = PhotoPose.FRONT)
        assertEquals(setOf("img0"), storage.deleted)
        assertEquals(setOf("img1"), drafts.pendingFileNames)
    }

    @Test
    fun nothingReadableMeansNothingToSave() = runSync {
        val drafts = PhotoDrafts(storage = FakeStorage(dates = emptyList()))
        assertFalse(drafts.fromLibrary(images = listOf(ByteArray(size = 0)), albumId = null, pose = PhotoPose.FRONT))
        assertNull(drafts.batch.value)
    }
}
