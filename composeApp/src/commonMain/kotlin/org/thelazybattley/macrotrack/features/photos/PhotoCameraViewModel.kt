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
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.PhotoPose
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.usecase.photo.GetPhotoAlbumsUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.GetProgressPhotosUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

/** The self-timer: off, or a countdown in seconds before the photo's taken. */
enum class PhotoTimer(val seconds: Int) {
    OFF(seconds = 0),
    THREE(seconds = 3),
    TEN(seconds = 10);

    fun next(): PhotoTimer = entries[(ordinal + 1) % entries.size]
}

/** What the user has chosen on the camera; [albumId] and [pose] are null until they pick, so the album's own apply. */
private data class CameraChoices(
    val albumId: Long?,
    val pose: PhotoPose? = null,
    val timer: PhotoTimer = PhotoTimer.THREE,
    val isGhostOn: Boolean = true,
    val isSaving: Boolean = false
)

data class PhotoCameraViewState(
    val albums: List<AlbumSummary> = emptyList(),
    val album: AlbumSummary? = null,
    val pose: PhotoPose = PhotoPose.FRONT,
    val timer: PhotoTimer = PhotoTimer.THREE,
    val isGhostOn: Boolean = true,
    val todayWeight: Weight? = null,
    val isSaving: Boolean = false
) {
    /** The album's latest photo, faded over the camera to line up the pose and distance. */
    val ghost: PhotoItem? get() = album?.latest
}

class PhotoCameraViewModel(
    savedStateHandle: SavedStateHandle,
    getPhotoAlbumsUseCase: GetPhotoAlbumsUseCase,
    getProgressPhotosUseCase: GetProgressPhotosUseCase,
    getAllWeightUseCase: GetAllWeightUseCase,
    private val drafts: PhotoDrafts
) : ViewModel() {

    private val choices = MutableStateFlow(
        value = CameraChoices(albumId = savedStateHandle.get<String>(key = AppDestinations.ALBUM_ID)?.toLongOrNull())
    )

    val state = combine(
        flow = getPhotoAlbumsUseCase(),
        flow2 = getProgressPhotosUseCase(),
        flow3 = getAllWeightUseCase(),
        flow4 = choices
    ) { albums, photos, weights, choices ->
        val weightsByDate = weights.byDate()
        val summaries = albumSummaries(albums = albums, photos = photos, weightsByDate = weightsByDate)
        val album = summaries.find { it.album.id == choices.albumId } ?: summaries.mostRecentlyUsed()
        val today = getCurrentDate()
        PhotoCameraViewState(
            albums = summaries,
            album = album,
            pose = choices.pose ?: album?.latest?.photo?.pose ?: PhotoPose.FRONT,
            timer = choices.timer,
            isGhostOn = choices.isGhostOn,
            todayWeight = weightsByDate[today]?.let { Weight(date = today, weight = it) },
            isSaving = choices.isSaving
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = PhotoCameraViewState()
    )

    private val _events = MutableSharedFlow<PhotoCameraEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /** Moves to the next album; its latest photo's pose comes with it. */
    fun onNextAlbum() {
        val current = state.value
        if (current.albums.isEmpty()) return
        val index = current.albums.indexOfFirst { it.album.id == current.album?.album?.id }
        val next = current.albums[(index + 1) % current.albums.size]
        choices.update { it.copy(albumId = next.album.id, pose = null) }
    }

    fun onPoseSelected(pose: PhotoPose) {
        choices.update { it.copy(pose = pose) }
    }

    fun onTimerTapped() {
        choices.update { it.copy(timer = it.timer.next()) }
    }

    fun onGhostToggled() {
        choices.update { it.copy(isGhostOn = !it.isGhostOn) }
    }

    fun onCaptured(bytes: ByteArray?) {
        if (bytes == null) {
            _events.tryEmit(value = PhotoCameraEvent.FAILED)
            return
        }
        choices.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val current = state.value
            val isReady = drafts.fromCamera(bytes = bytes, albumId = current.album?.album?.id, pose = current.pose)
            choices.update { it.copy(isSaving = false) }
            _events.emit(value = if (isReady) PhotoCameraEvent.OPEN_SAVE else PhotoCameraEvent.FAILED)
        }
    }
}

enum class PhotoCameraEvent { OPEN_SAVE, FAILED }
