package org.thelazybattley.macrotrack.features.activity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.thelazybattley.macrotrack.core.platform.LocationFix
import org.thelazybattley.macrotrack.core.platform.LocationTracker
import org.thelazybattley.macrotrack.core.platform.StepSensor
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.MAX_FIX_ACCURACY_METERS
import org.thelazybattley.macrotrack.domain.model.RoutePoint
import org.thelazybattley.macrotrack.domain.model.distanceMeters
import org.thelazybattley.macrotrack.domain.model.routeClimb
import org.thelazybattley.macrotrack.domain.repository.ActivityRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** A fix closer than this to the last one is the phone standing still, not a step along the route. */
private const val MIN_POINT_GAP_METERS = 3.0

/** The recording is saved this often, and at once on Pause and Resume, so closing the app loses little of it. */
private const val SAVE_INTERVAL_MILLIS = 10_000L

/** A walk or run being recorded. Time spent paused doesn't count toward [movingMillis]. */
@Serializable
data class Recording(
    val type: ActivityType,
    val startMillis: Long,
    val points: List<RoutePoint> = emptyList(),
    val segment: Int = 0,
    val isPaused: Boolean = false,
    /** Moving time before the current stretch began. */
    val movedMillisBefore: Long = 0,
    /** When the current stretch began; null while paused. */
    val stretchStartMillis: Long? = startMillis,
    val steps: Int = 0,
    val distanceMeters: Double = 0.0,
    /** How sure the last fix was, or null before the first. */
    val lastAccuracyMeters: Double? = null
) {
    fun movingMillis(now: Long): Long = movedMillisBefore + (stretchStartMillis?.let { now - it } ?: 0L)

    val climbMeters: Double get() = points.routeClimb()

    val hasGoodFix: Boolean get() = (lastAccuracyMeters ?: Double.MAX_VALUE) <= MAX_FIX_ACCURACY_METERS
}

/**
 * Records one walk or run at a time, outliving the screen that started it: leaving the live map keeps recording,
 * and coming back picks it up. Location and step counting run only while something is being recorded.
 *
 * It's saved as it goes, so closing the app doesn't end it: [restore] carries on from the last save, with the time
 * the app was closed counted as moving unless it was paused.
 */
class ActivityRecorder(
    private val locationTracker: LocationTracker,
    private val stepSensor: StepSensor,
    private val repository: ActivityRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    private val json = Json { ignoreUnknownKeys = true }
    private val saveLock = Mutex()
    private val saveRequests = Channel<Unit>(capacity = Channel.CONFLATED)
    private var isRestored = false
    private val _state = MutableStateFlow<Recording?>(value = null)

    /** The recording in progress, or null. */
    val state: StateFlow<Recording?> = _state.asStateFlow()

    private val _openRequests = Channel<Unit>(capacity = Channel.CONFLATED)

    /** Asks for the live map, from the Log card, the recording bar or the notification. */
    val openRequests: Flow<Unit> = _openRequests.receiveAsFlow()

    private val _isFinishRequested = MutableStateFlow(value = false)

    /** Set when Finish was tapped away from the live map, so it opens with the save sheet up. */
    val isFinishRequested: StateFlow<Boolean> = _isFinishRequested.asStateFlow()

    /** Shows the live map, with its save sheet open when [finish]. Does nothing when nothing is being recorded. */
    fun requestOpen(finish: Boolean = false) {
        if (_state.value == null) return
        if (finish) _isFinishRequested.value = true
        _openRequests.trySend(element = Unit)
    }

    fun onFinishRequestHandled() {
        _isFinishRequested.value = false
    }

    fun togglePause() {
        if (_state.value?.isPaused == true) resume() else pause()
    }

    fun start(type: ActivityType) {
        if (_state.value != null) return
        _state.value = Recording(type = type, startMillis = now())
        track(type = type)
    }

    /**
     * Picks up a recording that was going when the app was last closed. Called once the app is on screen, since
     * Android can start recording's foreground service only from there.
     */
    fun restore() {
        if (isRestored) return
        isRestored = true
        scope.launch {
            val saved = saveLock.withLock { repository.getActiveRecording() } ?: return@launch
            val recording = runCatching { json.decodeFromString<Recording>(string = saved) }.getOrNull()
            if (recording == null) {
                saveLock.withLock { repository.setActiveRecording(value = null) }
                return@launch
            }
            // Started again while this was loading; that one wins.
            if (!_state.compareAndSet(expect = null, update = recording)) return@launch
            track(type = recording.type)
        }
    }

    private fun track(type: ActivityType) {
        job = scope.launch {
            launch {
                while (true) {
                    _state.value?.let { save(recording = it) }
                    withTimeoutOrNull(timeMillis = SAVE_INTERVAL_MILLIS) { saveRequests.receive() }
                }
            }
            launch { locationTracker.fixes(type = type).collect(::onFix) }
            launch {
                var last: Long? = null
                stepSensor.counts().collect { count ->
                    val previous = last
                    last = count
                    if (previous == null || count <= previous) return@collect
                    _state.update { recording ->
                        if (recording == null || recording.isPaused) recording else recording.copy(steps = recording.steps + (count - previous).toInt())
                    }
                }
            }
        }
    }

    fun pause() {
        val now = now()
        _state.update { recording ->
            if (recording == null || recording.isPaused) {
                recording
            } else {
                recording.copy(isPaused = true, movedMillisBefore = recording.movingMillis(now = now), stretchStartMillis = null)
            }
        }
        saveRequests.trySend(element = Unit)
    }

    fun resume() {
        _state.update { recording ->
            if (recording == null || !recording.isPaused) {
                recording
            } else {
                // A new segment, so the route isn't drawn across wherever the phone went while paused.
                recording.copy(isPaused = false, segment = recording.segment + 1, stretchStartMillis = now())
            }
        }
        saveRequests.trySend(element = Unit)
    }

    /** Stops recording and hands back what was recorded, paused at this moment. */
    fun finish(): Recording? {
        pause()
        val recording = _state.value
        stop()
        return recording
    }

    fun discard() = stop()

    private fun stop() {
        val stopped = job
        job = null
        _state.value = null
        _isFinishRequested.value = false
        // Waits out a save in flight, so it can't bring the recording back after it's cleared.
        scope.launch {
            stopped?.cancelAndJoin()
            saveLock.withLock { repository.setActiveRecording(value = null) }
        }
    }

    private suspend fun save(recording: Recording) {
        val value = json.encodeToString(value = recording)
        saveLock.withLock { repository.setActiveRecording(value = value) }
    }

    private fun onFix(fix: LocationFix) {
        _state.update { recording ->
            if (recording == null) return@update null
            val withAccuracy = recording.copy(lastAccuracyMeters = fix.accuracyMeters)
            if (recording.isPaused || fix.accuracyMeters > MAX_FIX_ACCURACY_METERS) return@update withAccuracy
            val point = RoutePoint(lat = fix.lat, lon = fix.lon, altitude = fix.altitude, timeMillis = fix.timeMillis, segment = recording.segment)
            val last = recording.points.lastOrNull()
            val gap = if (last != null && last.segment == point.segment) distanceMeters(from = last, to = point) else null
            when {
                gap != null && gap < maxOf(MIN_POINT_GAP_METERS, fix.accuracyMeters / 2) -> withAccuracy
                else -> withAccuracy.copy(points = recording.points + point, distanceMeters = recording.distanceMeters + (gap ?: 0.0))
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun now(): Long = Clock.System.now().toEpochMilliseconds()
}
