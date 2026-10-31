package org.thelazybattley.macrotrack.features.share

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.MacroType
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.model.waistOn
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.dailygoal.GetAllDailyGoalsUseCase
import org.thelazybattley.macrotrack.domain.usecase.foodlog.GetAllFoodLogUseCase
import org.thelazybattley.macrotrack.domain.usecase.measurement.GetAllMeasurementsUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.GetPhotoAlbumsUseCase
import org.thelazybattley.macrotrack.domain.usecase.photo.GetProgressPhotosUseCase
import org.thelazybattley.macrotrack.domain.usecase.userdetails.GetUserDetailsUseCase
import org.thelazybattley.macrotrack.domain.usecase.weight.GetAllWeightUseCase
import org.thelazybattley.macrotrack.features.insights.CalorieBar
import org.thelazybattley.macrotrack.features.insights.InsightsRange
import org.thelazybattley.macrotrack.features.insights.calculateInsights
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnit
import org.thelazybattley.macrotrack.features.measurements.MeasurementUnitPreference
import org.thelazybattley.macrotrack.features.photos.PhotoItem
import org.thelazybattley.macrotrack.features.photos.albumSummaries
import org.thelazybattley.macrotrack.features.photos.byDate
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

enum class ShareCardKind { WEEK, WEIGHT, PHOTOS }

enum class ShareCardFormat { STORY, SQUARE }

enum class ShareCardStyle { LIGHT, DARK, GREEN }

/** The last 7 days: one bar a day, how many were on target, and the averages over the days logged. */
data class WeekCard(
    val start: LocalDate,
    val end: LocalDate,
    val bars: List<CalorieBar>,
    val daysOnTarget: Int,
    val averageCalories: Int?,
    val averageProtein: Int?,
    /** Latest minus first weigh-in of the week, in kg; null with fewer than two. */
    val weightChangeKg: Double?
)

/** Every weigh-in, oldest first; there are always at least two. */
data class WeightCard(val weights: List<Weight>) {
    val first: Weight get() = weights.first()
    val last: Weight get() = weights.last()
    val changeKg: Double get() = last.weight - first.weight
    val percentChange: Double get() = if (first.weight > 0) changeKg / first.weight * PERCENT else 0.0
    val days: Int get() = (last.date.toEpochDays() - first.date.toEpochDays()).toInt()
    val perWeekKg: Double get() = if (days > 0) changeKg / days * DAYS_PER_WEEK else 0.0
}

data class PhotosCard(
    val albumName: String,
    val before: PhotoItem,
    val after: PhotoItem,
    val photoCount: Int,
    /** After minus before, in cm; null unless both days have a waist measurement by then. */
    val waistChangeCm: Double?
) {
    val days: Int get() = (after.photo.date.toEpochDays() - before.photo.date.toEpochDays()).toInt()

    /** After minus before, in kg; null unless both photos have a weight on them. */
    val weightChangeKg: Double?
        get() {
            val first = before.weight ?: return null
            val last = after.weight ?: return null
            return last.weight - first.weight
        }
}

/** The choices made on the screen; each card opens on the size and color that suit it. */
private data class ShareChoices(
    val kind: ShareCardKind,
    val format: ShareCardFormat = defaultFormat(kind = kind),
    val style: ShareCardStyle = defaultStyle(kind = kind),
    val isWeightShown: Boolean = true
)

data class ShareCardViewState(
    val kind: ShareCardKind = ShareCardKind.WEEK,
    val format: ShareCardFormat = ShareCardFormat.STORY,
    val style: ShareCardStyle = ShareCardStyle.LIGHT,
    val isWeightShown: Boolean = true,
    val today: LocalDate = getCurrentDate(),
    val week: WeekCard? = null,
    /** Null with fewer than two weigh-ins. */
    val weight: WeightCard? = null,
    /** Null until an album has two photos. */
    val photos: PhotosCard? = null,
    val unit: MeasurementUnit = MeasurementUnit.CM,
    val isLoaded: Boolean = false
) {
    /** The cards there's enough to make, in chip order. */
    val kinds: List<ShareCardKind>
        get() = listOfNotNull(
            ShareCardKind.WEEK,
            ShareCardKind.WEIGHT.takeIf { weight != null },
            ShareCardKind.PHOTOS.takeIf { photos != null }
        )
}

interface ShareCardCallbacks {
    fun onKindSelected(kind: ShareCardKind)
    fun onFormatSelected(format: ShareCardFormat)
    fun onStyleSelected(style: ShareCardStyle)
    fun onWeightShownToggled()

    companion object {
        fun default() = object : ShareCardCallbacks {
            override fun onKindSelected(kind: ShareCardKind) = Unit
            override fun onFormatSelected(format: ShareCardFormat) = Unit
            override fun onStyleSelected(style: ShareCardStyle) = Unit
            override fun onWeightShownToggled() = Unit
        }
    }
}

class ShareCardViewModel(
    savedStateHandle: SavedStateHandle,
    getAllFoodLogUseCase: GetAllFoodLogUseCase,
    getAllDailyGoalsUseCase: GetAllDailyGoalsUseCase,
    getUserDetailsUseCase: GetUserDetailsUseCase,
    calculateMacrosGoalUseCase: CalculateMacrosGoalUseCase,
    getAllWeightUseCase: GetAllWeightUseCase,
    getPhotoAlbumsUseCase: GetPhotoAlbumsUseCase,
    getProgressPhotosUseCase: GetProgressPhotosUseCase,
    getAllMeasurementsUseCase: GetAllMeasurementsUseCase,
    unitPreference: MeasurementUnitPreference
) : ViewModel(), ShareCardCallbacks {

    private val albumId = savedStateHandle.get<String>(key = AppDestinations.ALBUM_ID)?.toLongOrNull()
    private val beforeId = savedStateHandle.get<String>(key = AppDestinations.BEFORE_ID)?.toLongOrNull()
    private val afterId = savedStateHandle.get<String>(key = AppDestinations.AFTER_ID)?.toLongOrNull()

    private val choices = MutableStateFlow(
        value = ShareChoices(
            kind = ShareCardKind.entries.find { it.name == savedStateHandle.get<String>(key = AppDestinations.SHARE_KIND) }
                ?: ShareCardKind.WEEK
        )
    )

    private val week = combine(
        flow = getAllFoodLogUseCase(),
        flow2 = getAllDailyGoalsUseCase(),
        flow3 = flow { emitAll(getUserDetailsUseCase()) },
        flow4 = getAllWeightUseCase()
    ) { foodLogs, dailyGoals, userDetails, weights ->
        val today = getCurrentDate()
        val insights = calculateInsights(
            range = InsightsRange.WEEK,
            today = today,
            foodLogs = foodLogs,
            dailyGoals = dailyGoals,
            currentGoal = userDetails?.goal,
            currentMacroGoals = userDetails?.let { calculateMacrosGoalUseCase(userDetails = it) }
        )
        val start = insights.bars.first().start
        val weekWeights = weights.filter { it.date in start..today }.sortedBy { it.date }
        WeekCard(
            start = start,
            end = today,
            bars = insights.bars,
            daysOnTarget = insights.daysOnTarget,
            averageCalories = insights.averageCalories,
            averageProtein = insights.macros.find { it.macroType == MacroType.PROTEIN }?.average,
            weightChangeKg = weekWeights.takeIf { it.size > 1 }?.let { it.last().weight - it.first().weight }
        )
    }

    private val photos = combine(
        flow = getPhotoAlbumsUseCase(),
        flow2 = getProgressPhotosUseCase(),
        flow3 = getAllWeightUseCase(),
        flow4 = getAllMeasurementsUseCase()
    ) { albums, photos, weights, measurements ->
        val summaries = albumSummaries(albums = albums, photos = photos, weightsByDate = weights.byDate())
            .filter { it.photos.size > 1 }
        // The album asked for, else the one with the most recent photo.
        val summary = summaries.find { it.album.id == albumId }
            ?: summaries.maxByOrNull { it.photos.last().photo.date }
            ?: return@combine null
        val before = summary.photos.find { it.photo.id == beforeId } ?: summary.photos.first()
        val after = summary.photos.find { it.photo.id == afterId }?.takeIf { it != before } ?: summary.photos.last()
        val waistBefore = waistOn(date = before.photo.date, measurements = measurements)
        val waistAfter = waistOn(date = after.photo.date, measurements = measurements)
        PhotosCard(
            albumName = summary.album.name,
            before = before,
            after = after,
            photoCount = summary.photos.size,
            waistChangeCm = if (waistBefore != null && waistAfter != null) waistAfter.valueCm - waistBefore.valueCm else null
        )
    }

    val state = combine(
        flow = week,
        flow2 = getAllWeightUseCase(),
        flow3 = photos,
        flow4 = unitPreference.unit,
        flow5 = choices
    ) { week, weights, photos, unit, choices ->
        ShareCardViewState(
            kind = choices.kind,
            format = choices.format,
            style = choices.style,
            isWeightShown = choices.isWeightShown,
            week = week,
            weight = weights.sortedBy { it.date }.takeIf { it.size > 1 }?.let { WeightCard(weights = it) },
            photos = photos,
            unit = unit,
            isLoaded = true
        ).let { viewState ->
            // Falls back to the weekly recap when the card asked for has nothing to show.
            if (viewState.kind in viewState.kinds) viewState else viewState.copy(kind = ShareCardKind.WEEK)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = ShareCardViewState()
    )

    override fun onKindSelected(kind: ShareCardKind) {
        choices.update { it.copy(kind = kind) }
    }

    override fun onFormatSelected(format: ShareCardFormat) {
        choices.update { it.copy(format = format) }
    }

    override fun onStyleSelected(style: ShareCardStyle) {
        choices.update { it.copy(style = style) }
    }

    override fun onWeightShownToggled() {
        choices.update { it.copy(isWeightShown = !it.isWeightShown) }
    }
}

private fun defaultFormat(kind: ShareCardKind) = if (kind == ShareCardKind.WEIGHT) ShareCardFormat.SQUARE else ShareCardFormat.STORY

private fun defaultStyle(kind: ShareCardKind) = if (kind == ShareCardKind.WEIGHT) ShareCardStyle.DARK else ShareCardStyle.LIGHT

private const val PERCENT = 100.0
private const val DAYS_PER_WEEK = 7
