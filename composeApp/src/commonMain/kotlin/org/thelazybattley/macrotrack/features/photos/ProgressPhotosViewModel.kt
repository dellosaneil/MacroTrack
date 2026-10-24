package org.thelazybattley.macrotrack.features.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.PhotoAlbum
import org.thelazybattley.macrotrack.domain.repository.ProgressPhotoRepository
import org.thelazybattley.macrotrack.domain.usecase.photo.GetPhotoAlbumsUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.GetProgressPhotosUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.SavePhotoAlbumUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class ProgressPhotosViewState(
    val albums: List<AlbumSummary> = emptyList(),
    val isLoaded: Boolean = false
) {
    /** The album shown up top: the most recently added to that has a first and latest weigh-in to compare. */
    val hero: AlbumSummary?
        get() = albums.filter { it.changeKg != null }
            .maxByOrNull { summary -> summary.photos.maxOf { it.photo.createdAtMillis } }

    val photoCount: Int get() = albums.sumOf { it.photos.size }

    /** Where + Photo adds to. */
    val defaultAlbum: AlbumSummary? get() = albums.mostRecentlyUsed()
}

class ProgressPhotosViewModel(
    getPhotoAlbumsUseCase: GetPhotoAlbumsUseCase,
    getProgressPhotosUseCase: GetProgressPhotosUseCase,
    getAllWeightUseCase: GetAllWeightUseCase,
    private val savePhotoAlbumUseCase: SavePhotoAlbumUseCase,
    repository: ProgressPhotoRepository,
    drafts: PhotoDrafts
) : ViewModel() {

    val state = combine(
        flow = getPhotoAlbumsUseCase(),
        flow2 = getProgressPhotosUseCase(),
        flow3 = getAllWeightUseCase()
    ) { albums, photos, weights ->
        ProgressPhotosViewState(
            albums = albumSummaries(albums = albums, photos = photos, weightsByDate = weights.byDate()),
            isLoaded = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = ProgressPhotosViewState()
    )

    private val _createdAlbum = MutableSharedFlow<Long>(extraBufferCapacity = 1)

    /** The id of an album just made, to open it. */
    val createdAlbum = _createdAlbum.asSharedFlow()

    init {
        // Photos kept for a Save that was never finished, like when the app was closed on it.
        viewModelScope.launch { repository.deleteUnusedImages(alsoKeep = drafts.pendingFileNames) }
    }

    @OptIn(ExperimentalTime::class)
    fun onCreateAlbum(album: PhotoAlbum) {
        viewModelScope.launch {
            val id = savePhotoAlbumUseCase(album = album.copy(createdAtMillis = Clock.System.now().toEpochMilliseconds()))
            _createdAlbum.emit(value = id)
        }
    }
}
