package org.thelazybattley.macrotrack.features.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.ActivitySession
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.BucketSize
import org.thelazybattley.macrotrack.domain.model.BurnProfile
import org.thelazybattley.macrotrack.domain.model.HistoryBucket
import org.thelazybattley.macrotrack.domain.model.HistoryFilter
import org.thelazybattley.macrotrack.domain.model.HistoryMetric
import org.thelazybattley.macrotrack.domain.model.HistoryPeriod
import org.thelazybattley.macrotrack.domain.model.HistoryRange
import org.thelazybattley.macrotrack.domain.model.HistoryTotals
import org.thelazybattley.macrotrack.domain.model.PersonalBests
import org.thelazybattley.macrotrack.domain.model.averagePace
import org.thelazybattley.macrotrack.domain.model.bucketSize
import org.thelazybattley.macrotrack.domain.model.buckets
import org.thelazybattley.macrotrack.domain.model.longestStreak
import org.thelazybattley.macrotrack.domain.model.period
import org.thelazybattley.macrotrack.domain.model.previous
import org.thelazybattley.macrotrack.domain.usecase.activity.GetActivitySessionsUseCase
import org.thelazybattley.macrotrack.domain.usecase.activity.GetBurnProfileUseCase

/** The list shows this many before "Show all". */
const val HISTORY_LIST_PREVIEW = 8

/** A point on the pace line: one walk or run, or the average over a week or month. */
data class PacePoint(val secondsPerKm: Double, val start: LocalDate, val end: LocalDate, val session: ActivitySession?)

data class ActivityHistoryViewState(
    val today: LocalDate = getCurrentDate(),
    val sessions: List<ActivitySession> = emptyList(),
    val profile: BurnProfile? = null,
    val range: HistoryRange = HistoryRange.MONTH,
    val filter: HistoryFilter = HistoryFilter.ALL,
    val metric: HistoryMetric = HistoryMetric.STEPS,
    /** Which pace the line shows while both walks and runs are listed; their paces don't share a scale. */
    val paceType: ActivityType = ActivityType.WALK,
    val selectedBar: Int? = null,
    val selectedPoint: Int? = null,
    val isListExpanded: Boolean = false
) {
    val period: HistoryPeriod get() = range.period(today = today, firstDate = sessions.minOfOrNull { it.date })

    val previousPeriod: HistoryPeriod? get() = range.previous(period = period)

    private val matching: List<ActivitySession> get() = sessions.filter { filter.matches(type = it.type) }

    /** Newest first. */
    val inRange: List<ActivitySession> get() = matching.filter { it.date in period }

    val totals: HistoryTotals get() = HistoryTotals.of(sessions = inRange, profile = profile)

    val previousTotals: HistoryTotals?
        get() = previousPeriod?.let { previous -> HistoryTotals.of(sessions = matching.filter { it.date in previous }, profile = profile) }

    val activeDays: Int get() = inRange.map { it.date }.toSet().size

    val longestStreak: Int get() = longestStreak(dates = inRange.map { it.date }.toSet(), period = period)

    val bucketSize: BucketSize get() = range.bucketSize

    val buckets: List<HistoryBucket> get() = buckets(sessions = inRange, period = period, size = bucketSize)

    val paceTypeShown: ActivityType
        get() = when (filter) {
            HistoryFilter.ALL -> paceType
            HistoryFilter.WALKS -> ActivityType.WALK
            HistoryFilter.RUNS -> ActivityType.RUN
        }

    /** Oldest first: each walk or run over days, the average per week or month over longer ranges. */
    val pacePoints: List<PacePoint>
        get() {
            val ofType = inRange.filter { it.type == paceTypeShown }
            return if (bucketSize == BucketSize.DAY) {
                ofType.sortedBy { it.startMillis }.mapNotNull { session ->
                    session.paceSecondsPerKm?.let { PacePoint(secondsPerKm = it, start = session.date, end = session.date, session = session) }
                }
            } else {
                buckets(sessions = ofType, period = period, size = bucketSize).mapNotNull { bucket ->
                    averagePace(sessions = bucket.sessions)?.let { PacePoint(secondsPerKm = it, start = bucket.start, end = bucket.end, session = null) }
                }
            }
        }

    val bests: PersonalBests get() = PersonalBests.of(sessions = sessions, profile = profile)
}

interface ActivityHistoryCallbacks {
    fun onRangeSelected(range: HistoryRange)
    fun onFilterSelected(filter: HistoryFilter)
    fun onMetricSelected(metric: HistoryMetric)
    fun onPaceTypeSelected(type: ActivityType)
    fun onBarSelected(index: Int)
    fun onPointSelected(index: Int)
    fun onToggleList()

    companion object {
        fun default() = object : ActivityHistoryCallbacks {
            override fun onRangeSelected(range: HistoryRange) = Unit
            override fun onFilterSelected(filter: HistoryFilter) = Unit
            override fun onMetricSelected(metric: HistoryMetric) = Unit
            override fun onPaceTypeSelected(type: ActivityType) = Unit
            override fun onBarSelected(index: Int) = Unit
            override fun onPointSelected(index: Int) = Unit
            override fun onToggleList() = Unit
        }
    }
}

class ActivityHistoryViewModel(
    getActivitySessionsUseCase: GetActivitySessionsUseCase,
    getBurnProfileUseCase: GetBurnProfileUseCase
) : ViewModel(), ActivityHistoryCallbacks {

    private val _state = MutableStateFlow(value = ActivityHistoryViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(flow = getActivitySessionsUseCase(), flow2 = getBurnProfileUseCase()) { sessions, profile ->
                _state.update { it.copy(sessions = sessions, profile = profile, today = getCurrentDate()) }
            }.collect {}
        }
    }

    override fun onRangeSelected(range: HistoryRange) {
        _state.update { it.copy(range = range, selectedBar = null, selectedPoint = null, isListExpanded = false) }
    }

    override fun onFilterSelected(filter: HistoryFilter) {
        _state.update { it.copy(filter = filter, selectedBar = null, selectedPoint = null, isListExpanded = false) }
    }

    override fun onMetricSelected(metric: HistoryMetric) {
        _state.update { it.copy(metric = metric, selectedBar = null) }
    }

    override fun onPaceTypeSelected(type: ActivityType) {
        _state.update { it.copy(paceType = type, selectedPoint = null) }
    }

    override fun onBarSelected(index: Int) {
        _state.update { it.copy(selectedBar = if (it.selectedBar == index) null else index) }
    }

    override fun onPointSelected(index: Int) {
        _state.update { it.copy(selectedPoint = if (it.selectedPoint == index) null else index) }
    }

    override fun onToggleList() {
        _state.update { it.copy(isListExpanded = !it.isListExpanded) }
    }
}
