package org.thelazybattley.macrotrack.features.photos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.thelazybattley.macrotrack.domain.model.PhotoAlbum
import org.thelazybattley.macrotrack.domain.model.ProgressPhoto
import org.thelazybattley.macrotrack.domain.usecase.photo.DeletePhotoAlbumUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.DeleteProgressPhotoUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.GetPhotoAlbumsUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.GetProgressPhotosUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.SavePhotoAlbumUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

enum class AlbumView { GRID, TIMELINE }

data class PhotoAlbumViewState(
    val summary: AlbumSummary? = null,
    val view: AlbumView = AlbumView.GRID,
    /** What was just saved here, for the confirmation up top. */
    val saved: SavedPhotos? = null,
    val isLoaded: Boolean = false
)

class PhotoAlbumViewModel(
    savedStateHandle: SavedStateHandle,
    getPhotoAlbumsUseCase: GetPhotoAlbumsUseCase,
    getProgressPhotosUseCase: GetProgressPhotosUseCase,
    getAllWeightUseCase: GetAllWeightUseCase,
    private val savePhotoAlbumUseCase: SavePhotoAlbumUseCase,
    private val deletePhotoAlbumUseCase: DeletePhotoAlbumUseCase,
    private val deleteProgressPhotoUseCase: DeleteProgressPhotoUseCase,
    private val drafts: PhotoDrafts
) : ViewModel() {

    val albumId = savedStateHandle.get<String>(key = AppDestinations.ALBUM_ID)?.toLongOrNull() ?: 0L

    private val view = MutableStateFlow(value = AlbumView.GRID)

    val state = combine(
        flow = getPhotoAlbumsUseCase(),
        flow2 = getProgressPhotosUseCase(),
        flow3 = getAllWeightUseCase(),
        flow4 = view,
        flow5 = drafts.saved
    ) { albums, photos, weights, view, saved ->
        val savedHere = saved?.takeIf { it.albumId == albumId }
        PhotoAlbumViewState(
            summary = albumSummaries(
                albums = albums.filter { it.id == albumId },
                photos = photos,
                weightsByDate = weights.byDate(),
                newIds = savedHere?.ids.orEmpty()
            ).firstOrNull(),
            view = view,
            saved = savedHere,
            isLoaded = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = PhotoAlbumViewState()
    )

    private val _closed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** The album was deleted, so its screen should close. */
    val closed = _closed.asSharedFlow()

    fun onViewSelected(view: AlbumView) {
        this.view.update { view }
    }

    fun onSaveAlbum(album: PhotoAlbum) {
        viewModelScope.launch { savePhotoAlbumUseCase(album = album) }
    }

    fun onDeleteAlbum() {
        viewModelScope.launch {
            deletePhotoAlbumUseCase(id = albumId)
            _closed.emit(value = Unit)
        }
    }

    fun onDeletePhoto(photo: ProgressPhoto) {
        viewModelScope.launch { deleteProgressPhotoUseCase(photo = photo) }
    }

    /** The New tags and confirmation are for the visit right after saving; leaving the album clears them. */
    fun onLeave() {
        if (drafts.saved.value?.albumId == albumId) drafts.clearSaved()
    }
}
