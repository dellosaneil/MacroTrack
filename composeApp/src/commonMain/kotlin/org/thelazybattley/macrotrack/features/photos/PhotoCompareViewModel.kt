package org.thelazybattley.macrotrack.features.photos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.thelazybattley.macrotrack.domain.model.Measurement
import org.thelazybattley.macrotrack.domain.model.waistOn
import org.thelazybattley.macrotrack.domain.usecase.measurement.GetAllMeasurementsUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.GetPhotoAlbumsUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.GetProgressPhotosUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnit
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnitPreference
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

enum class CompareMode { SLIDER, SIDE_BY_SIDE }

enum class ComparePick { BEFORE, AFTER }

private data class CompareChoices(
    val beforeId: Long?,
    val afterId: Long?,
    val mode: CompareMode = CompareMode.SLIDER,
    /** How far across the divider sits, 0 to 1; the before photo shows to its left. */
    val split: Float = 0.5f,
    val picking: ComparePick? = null
)

data class PhotoCompareViewState(
    val summary: AlbumSummary? = null,
    val before: PhotoItem? = null,
    val after: PhotoItem? = null,
    val mode: CompareMode = CompareMode.SLIDER,
    val split: Float = 0.5f,
    val picking: ComparePick? = null,
    val waistBefore: Measurement? = null,
    val waistAfter: Measurement? = null,
    val unit: MeasurementUnit = MeasurementUnit.CM,
    val isLoaded: Boolean = false
) {
    /** After minus before, in kg; null unless both have a weight on them. */
    val weightChangeKg: Double?
        get() {
            val first = before?.weight ?: return null
            val last = after?.weight ?: return null
            return last.weight - first.weight
        }

    val days: Int get() = if (before != null && after != null) (after.photo.date.toEpochDays() - before.photo.date.toEpochDays()).toInt() else 0

    /** After minus before, in cm; null unless both days have a waist measurement by then. */
    val waistChangeCm: Double?
        get() {
            val first = waistBefore ?: return null
            val last = waistAfter ?: return null
            return last.valueCm - first.valueCm
        }
}

class PhotoCompareViewModel(
    savedStateHandle: SavedStateHandle,
    getPhotoAlbumsUseCase: GetPhotoAlbumsUseCase,
    getProgressPhotosUseCase: GetProgressPhotosUseCase,
    getAllWeightUseCase: GetAllWeightUseCase,
    getAllMeasurementsUseCase: GetAllMeasurementsUseCase,
    unitPreference: MeasurementUnitPreference
) : ViewModel() {

    private val albumId = savedStateHandle.get<String>(key = AppDestinations.ALBUM_ID)?.toLongOrNull() ?: 0L

    private val choices = MutableStateFlow(
        value = CompareChoices(
            beforeId = savedStateHandle.get<String>(key = AppDestinations.BEFORE_ID)?.toLongOrNull(),
            afterId = savedStateHandle.get<String>(key = AppDestinations.AFTER_ID)?.toLongOrNull(),
            mode = if (savedStateHandle.get<String>(key = AppDestinations.SIDE_BY_SIDE) == "true") CompareMode.SIDE_BY_SIDE else CompareMode.SLIDER
        )
    )

    private val album = combine(
        flow = getPhotoAlbumsUseCase(),
        flow2 = getProgressPhotosUseCase(),
        flow3 = getAllWeightUseCase()
    ) { albums, photos, weights ->
        albumSummaries(albums = albums.filter { it.id == albumId }, photos = photos, weightsByDate = weights.byDate()).firstOrNull()
    }

    val state = combine(
        flow = album,
        flow2 = getAllMeasurementsUseCase(),
        flow3 = unitPreference.unit,
        flow4 = choices
    ) { summary, measurements, unit, choices ->
        val photos = summary?.photos.orEmpty()
        val before = photos.find { it.photo.id == choices.beforeId } ?: photos.firstOrNull()
        // Tapping the first photo in the grid compares it with the latest instead of with itself.
        val after = photos.find { it.photo.id == choices.afterId }?.takeIf { it != before || photos.size == 1 } ?: photos.lastOrNull()
        PhotoCompareViewState(
            summary = summary,
            before = before,
            after = after,
            mode = choices.mode,
            split = choices.split,
            picking = choices.picking,
            waistBefore = before?.let { waistOn(date = it.photo.date, measurements = measurements) },
            waistAfter = after?.let { waistOn(date = it.photo.date, measurements = measurements) },
            unit = unit,
            isLoaded = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = PhotoCompareViewState()
    )

    fun onModeSelected(mode: CompareMode) {
        choices.update { it.copy(mode = mode) }
    }

    fun onSplitChanged(split: Float) {
        choices.update { it.copy(split = split.coerceIn(minimumValue = 0f, maximumValue = 1f)) }
    }

    /** Opens or closes the strip for choosing the before or after photo. */
    fun onPickToggled(pick: ComparePick) {
        choices.update { it.copy(picking = if (it.picking == pick) null else pick) }
    }

    fun onPhotoPicked(id: Long) {
        choices.update { current ->
            when (current.picking) {
                ComparePick.BEFORE -> current.copy(beforeId = id, picking = null)
                ComparePick.AFTER -> current.copy(afterId = id, picking = null)
                null -> current
            }
        }
    }
}
