package org.thelazybattley.macrotrack.features.photos

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
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.CropRect
import org.thelazybattley.macrotrack.domain.model.PhotoAlbum
import org.thelazybattley.macrotrack.domain.model.PhotoPose
import org.thelazybattley.macrotrack.domain.model.ProgressPhoto
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.model.nearestWeighIn
import org.thelazybattley.macrotrack.domain.usecase.photo.GetPhotoAlbumsUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.GetProgressPhotosUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.SavePhotoAlbumUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.SaveProgressPhotosUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** How far back the date picker goes. */
private const val CALENDAR_MONTHS_BACK = 60

private data class SaveChoices(
    val drafts: List<PhotoDraft>,
    val isFromLibrary: Boolean,
    val selected: Int = 0,
    val albumId: Long?,
    val isDatePickerOpen: Boolean = false,
    /** The first day of the month the date picker shows. */
    val calendarMonth: LocalDate,
    val isSaving: Boolean = false
)

data class PhotoSaveViewState(
    val drafts: List<PhotoDraft> = emptyList(),
    val isFromLibrary: Boolean = false,
    val selected: Int = 0,
    val albums: List<AlbumSummary> = emptyList(),
    val albumId: Long? = null,
    val weightsByDate: Map<LocalDate, Double> = emptyMap(),
    val isDatePickerOpen: Boolean = false,
    val calendarMonth: LocalDate = getCurrentDate(),
    val isSaving: Boolean = false
) {
    val draft: PhotoDraft? get() = drafts.getOrNull(index = selected)

    val album: AlbumSummary? get() = albums.find { it.album.id == albumId }

    /** The weigh-in on the photo being looked at: its own day's, or the nearby one picked. */
    val weight: Weight? get() = draft?.let { weightOf(draft = it) }

    /** A weigh-in close by to offer when the photo's day has none. */
    val nearest: Weight? get() = draft?.takeIf { it.weightDate == null }?.let { nearestWeighIn(date = it.date, weightsByDate = weightsByDate) }

    val canGoToPreviousMonth: Boolean
        get() = calendarMonth > getCurrentDate().minus(period = DatePeriod(months = CALENDAR_MONTHS_BACK))

    val canGoToNextMonth: Boolean
        get() = calendarMonth.plus(period = DatePeriod(months = 1)) <= getCurrentDate()

    fun weightOf(draft: PhotoDraft): Weight? {
        val date = draft.weightDate ?: draft.date
        return weightsByDate[date]?.let { Weight(date = date, weight = it) }
    }
}

sealed interface PhotoSaveEvent {
    data class Saved(val albumId: Long) : PhotoSaveEvent
    /** Nothing to save, like after the app was closed on this screen. */
    data object Empty : PhotoSaveEvent
}

class PhotoSaveViewModel(
    getPhotoAlbumsUseCase: GetPhotoAlbumsUseCase,
    getProgressPhotosUseCase: GetProgressPhotosUseCase,
    getAllWeightUseCase: GetAllWeightUseCase,
    private val saveProgressPhotosUseCase: SaveProgressPhotosUseCase,
    private val savePhotoAlbumUseCase: SavePhotoAlbumUseCase,
    private val drafts: PhotoDrafts
) : ViewModel() {

    private val batch = drafts.batch.value

    private var isSaved = false

    private val choices = MutableStateFlow(
        value = SaveChoices(
            drafts = batch?.drafts.orEmpty(),
            isFromLibrary = batch?.isFromLibrary ?: false,
            albumId = batch?.albumId,
            calendarMonth = (batch?.drafts?.firstOrNull()?.date ?: getCurrentDate()).firstOfMonth()
        )
    )

    val state = combine(
        flow = getPhotoAlbumsUseCase(),
        flow2 = getProgressPhotosUseCase(),
        flow3 = getAllWeightUseCase(),
        flow4 = choices
    ) { albums, photos, weights, choices ->
        val weightsByDate = weights.byDate()
        val summaries = albumSummaries(albums = albums, photos = photos, weightsByDate = weightsByDate)
        PhotoSaveViewState(
            drafts = choices.drafts,
            isFromLibrary = choices.isFromLibrary,
            selected = choices.selected,
            albums = summaries,
            albumId = choices.albumId?.takeIf { id -> summaries.any { it.album.id == id } } ?: summaries.mostRecentlyUsed()?.album?.id,
            weightsByDate = weightsByDate,
            isDatePickerOpen = choices.isDatePickerOpen,
            calendarMonth = choices.calendarMonth,
            isSaving = choices.isSaving
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = PhotoSaveViewState()
    )

    private val _events = MutableSharedFlow<PhotoSaveEvent>(replay = 1)
    val events = _events.asSharedFlow()

    init {
        if (batch == null) _events.tryEmit(value = PhotoSaveEvent.Empty)
    }

    fun onSelect(index: Int) {
        choices.update { it.copy(selected = index, calendarMonth = it.drafts[index].date.firstOfMonth()) }
    }

    fun onDatePickerToggled() {
        choices.update { current ->
            current.copy(
                isDatePickerOpen = !current.isDatePickerOpen,
                calendarMonth = current.drafts.getOrNull(index = current.selected)?.date?.firstOfMonth() ?: current.calendarMonth
            )
        }
    }

    fun onMonthChanged(isNext: Boolean) {
        choices.update { it.copy(calendarMonth = it.calendarMonth.plus(period = DatePeriod(months = if (isNext) 1 else -1))) }
    }

    /** A new day for the photo; a nearby weigh-in picked for the old day no longer applies. */
    fun onDateSelected(date: LocalDate) {
        if (date > getCurrentDate()) return
        updateDraft { it.copy(date = date, weightDate = null) }
    }

    fun onUseNearest() {
        val nearest = state.value.nearest ?: return
        updateDraft { it.copy(weightDate = nearest.date) }
    }

    fun onUndoNearest() {
        updateDraft { it.copy(weightDate = null) }
    }

    fun onPoseSelected(pose: PhotoPose) {
        updateDraft { it.copy(pose = pose) }
    }

    fun onNoteChanged(note: String) {
        updateDraft { it.copy(note = note.take(n = NOTE_MAX)) }
    }

    /** Swaps the photo being looked at for [crop] of it. */
    fun onCropped(crop: CropRect) {
        val draft = state.value.draft ?: return
        viewModelScope.launch {
            val cropped = drafts.crop(fileName = draft.fileName, crop = crop) ?: return@launch
            choices.update { current ->
                current.copy(drafts = current.drafts.map { if (it.fileName == draft.fileName) it.copy(fileName = cropped) else it })
            }
        }
    }

    fun onAlbumSelected(id: Long) {
        choices.update { it.copy(albumId = id) }
    }

    @OptIn(ExperimentalTime::class)
    fun onCreateAlbum(album: PhotoAlbum) {
        viewModelScope.launch {
            val id = savePhotoAlbumUseCase(album = album.copy(createdAtMillis = Clock.System.now().toEpochMilliseconds()))
            choices.update { it.copy(albumId = id) }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun onSave() {
        val current = state.value
        val albumId = current.albumId ?: return
        if (current.drafts.isEmpty() || current.isSaving) return
        choices.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val photos = current.drafts.mapIndexed { index, draft ->
                ProgressPhoto(
                    albumId = albumId,
                    date = draft.date,
                    takenOn = draft.takenOn,
                    pose = draft.pose,
                    note = draft.note.trim().takeIf { it.isNotEmpty() },
                    weightDate = draft.weightDate,
                    fileName = draft.fileName,
                    createdAtMillis = now + index
                )
            }
            val ids = saveProgressPhotosUseCase(photos = photos)
            isSaved = true
            val first = current.drafts.first()
            drafts.onSaved(
                saved = SavedPhotos(
                    albumId = albumId,
                    ids = ids.toSet(),
                    firstDate = first.date,
                    firstWeightKg = current.weightOf(draft = first)?.weight
                )
            )
            _events.emit(value = PhotoSaveEvent.Saved(albumId = albumId))
        }
    }

    /** Leaving without saving drops the photos just taken or picked; a rotation or Log weight on top doesn't. */
    override fun onCleared() {
        if (!isSaved) drafts.discard()
    }

    private fun updateDraft(change: (PhotoDraft) -> PhotoDraft) {
        choices.update { current ->
            current.copy(drafts = current.drafts.mapIndexed { index, draft -> if (index == current.selected) change(draft) else draft })
        }
    }

    companion object {
        const val NOTE_MAX = 120
    }
}

private fun LocalDate.firstOfMonth(): LocalDate = LocalDate(year = year, month = month, day = 1)
