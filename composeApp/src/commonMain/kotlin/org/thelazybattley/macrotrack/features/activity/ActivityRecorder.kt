package org.thelazybattley.macrotrack.features.activity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
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
import org.thelazybattley.macrotrack.core.platform.MotionActivity
import org.thelazybattley.macrotrack.core.platform.MotionSensor
import org.thelazybattley.macrotrack.core.platform.StepSensor
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.MAX_FIX_ACCURACY_METERS
import org.thelazybattley.macrotrack.domain.model.RoutePoint
import org.thelazybattley.macrotrack.domain.model.continuesTo
import org.thelazybattley.macrotrack.domain.model.distanceMeters
import org.thelazybattley.macrotrack.domain.model.routeClimb
import org.thelazybattley.macrotrack.domain.model.routeDistance
import org.thelazybattley.macrotrack.domain.repository.ActivityRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** A fix closer than this to the last one is the phone standing still, not a step along the route. */
private const val MIN_POINT_GAP_METERS = 3.0

/** The recording is saved this often, and at once on Pause and Resume, so closing the app loses little of it. */
private const val SAVE_INTERVAL_MILLIS = 10_000L

/** A fix older than this when it arrives is the phone's last known place, not where the user is now. */
private const val MAX_FIX_AGE_MILLIS = 10_000L

/** The route waits for a fix at least this good to start, so its first point isn't down the street. */
private const val START_FIX_ACCURACY_METERS = 20.0

/** Faster than this between fixes is GPS jumping, not anyone moving. */
private const val MAX_JUMP_SPEED = 70.0

/** After this many jumps in a row the new place is believed, and smoothing starts again from there. */
private const val JUMPS_BEFORE_RESET = 3

/** Without a barometer, GPS altitude is used only when the phone is this sure of it, and smoothed. */
private const val MAX_VERTICAL_ACCURACY_METERS = 12.0
private const val GPS_ALTITUDE_SMOOTHING = 0.2

/** Barometer heights are placed above sea level by averaging this many good GPS altitudes. */
private const val ALTITUDE_OFFSET_SAMPLES = 10

/** While riding, positions are smoothed for vehicle speeds so the line keeps up with the car. */
private const val RIDING_SMOOTHING_SPEED = 30.0

/** A walk or run being recorded. Time spent paused or riding doesn't count toward [movingMillis]. */
@Serializable
data class Recording(
    val type: ActivityType,
    val startMillis: Long,
    val points: List<RoutePoint> = emptyList(),
    val segment: Int = 0,
    val isPaused: Boolean = false,
    /** Moving time before the current stretch began. */
    val movedMillisBefore: Long = 0,
    /** When the current stretch began; null while paused or riding. */
    val stretchStartMillis: Long? = startMillis,
    val steps: Int = 0,
    val distanceMeters: Double = 0.0,
    /** How sure the last fix was, or null before the first. */
    val lastAccuracyMeters: Double? = null,
    /** In a car, bus or on a bike right now: the map shows it, and distance, time and steps don't count. */
    val isRiding: Boolean = false,
    /** Where the phone is now, smoothed, for the live dot; it only joins [points] once it has moved. */
    val livePoint: RoutePoint? = null,
    /** Added to barometer heights to make them metres above sea level, once GPS has given some; null until then. */
    val altitudeOffset: Double? = null,
    val altitudeOffsetSamples: Int = 0
) {
    fun movingMillis(now: Long): Long = movedMillisBefore + (stretchStartMillis?.let { now - it } ?: 0L)

    val climbMeters: Double get() = points.routeClimb()

    val hasGoodFix: Boolean get() = (lastAccuracyMeters ?: Double.MAX_VALUE) <= MAX_FIX_ACCURACY_METERS
}

/** What one recording's sensors have said so far; it lasts as long as the app is open, and isn't saved. */
private class Tracking(type: ActivityType) {
    val detector = RidingDetector(type = type)
    val smoother = LocationSmoother(metersPerSecond = detector.maxFootSpeed)
    var lastFixMillis = 0L
    var jumps = 0
    var gpsAltitude: Double? = null

    /** Null until the barometer is heard from or found missing. */
    var hasBarometer: Boolean? = null

    /** The latest barometer height, carried on from the saved route's last height after the app was closed. */
    var barometerAltitude: Double? = null
    var barometerShift = 0.0
    var lastStepMillis: Long? = null
    var stepsSinceMillis: Long? = null
    var activity: MotionActivity? = null
}

/**
 * Records one walk or run at a time, outliving the screen that started it: leaving the live map keeps recording,
 * and coming back picks it up. Location and step counting run only while something is being recorded.
 *
 * Fixes are smoothed and stale or jumpy ones dropped; height comes from the barometer where there is one; and
 * stretches spent riding a vehicle are marked as such, left out of distance, time and steps.
 *
 * It's saved as it goes, so closing the app doesn't end it: [restore] carries on from the last save, with the time
 * the app was closed counted as moving unless it was paused.
 */
class ActivityRecorder(
    private val locationTracker: LocationTracker,
    private val stepSensor: StepSensor,
    private val motionSensor: MotionSensor,
    private val repository: ActivityRepository
) {
    // One thread, so the sensors' readings are handled one at a time.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default.limitedParallelism(parallelism = 1))
    private var job: Job? = null
    private var locationJob: Job? = null
    private var tracking: Tracking? = null
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

    /**
     * Starts recording a [type]. With one already going, location starts again if it had stopped, as when the
     * recording was picked up after a restart before location was allowed again.
     */
    fun start(type: ActivityType) {
        if (_state.value != null) {
            collectLocation()
            return
        }
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
        val tracking = Tracking(type = type).also { tracking = it }
        job = scope.launch {
            launch {
                while (true) {
                    _state.value?.let { save(recording = it) }
                    withTimeoutOrNull(timeMillis = SAVE_INTERVAL_MILLIS) { saveRequests.receive() }
                }
            }
            launch {
                var last: Long? = null
                stepSensor.counts().collect { count ->
                    val previous = last
                    last = count
                    if (tracking.stepsSinceMillis == null) tracking.stepsSinceMillis = now()
                    if (previous == null || count <= previous) return@collect
                    tracking.lastStepMillis = now()
                    _state.update { recording ->
                        // Steps while riding are a bumpy road or pedalling, not walking.
                        if (recording == null || recording.isPaused || recording.isRiding) {
                            recording
                        } else {
                            recording.copy(steps = recording.steps + (count - previous).toInt())
                        }
                    }
                }
            }
            launch {
                motionSensor.relativeAltitudes().collect { height ->
                    if (tracking.hasBarometer != true) {
                        tracking.hasBarometer = true
                        // Carries on from the route's last height, so picking up after a restart doesn't jump.
                        val recording = _state.value
                        val lastHeight = recording?.points?.lastOrNull { it.altitude != null }?.altitude
                        tracking.barometerShift = if (lastHeight != null) lastHeight - (recording.altitudeOffset ?: 0.0) - height else -height
                    }
                    tracking.barometerAltitude = height + tracking.barometerShift
                }
                if (tracking.hasBarometer == null) tracking.hasBarometer = false
            }
            launch {
                motionSensor.activities().collect { tracking.activity = it }
            }
        }
        collectLocation()
    }

    private fun collectLocation() {
        val parent = job ?: return
        val type = _state.value?.type ?: return
        if (locationJob?.isActive == true) return
        locationJob = scope.launch(context = parent) { locationTracker.fixes(type = type).collect(::onFix) }
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
                recording.copy(
                    isPaused = false,
                    segment = recording.segment + 1,
                    stretchStartMillis = if (recording.isRiding) null else now()
                )
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
        locationJob = null
        tracking = null
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
        val tracking = tracking ?: return
        val current = _state.value ?: return
        // The phone often hands over where it last was before it knows where it is now.
        if (now() - fix.timeMillis > MAX_FIX_AGE_MILLIS || fix.timeMillis <= tracking.lastFixMillis) return
        val isTooRough = fix.accuracyMeters > MAX_FIX_ACCURACY_METERS ||
            (!tracking.smoother.hasPosition && fix.accuracyMeters > START_FIX_ACCURACY_METERS)
        if (isTooRough || isJump(tracking = tracking, fix = fix)) {
            _state.update { it?.copy(lastAccuracyMeters = fix.accuracyMeters) }
            return
        }
        val smoother = tracking.smoother
        val previous = if (smoother.hasPosition) RoutePoint(lat = smoother.lat, lon = smoother.lon, timeMillis = tracking.lastFixMillis) else null
        smoother.metersPerSecond = if (current.isRiding) RIDING_SMOOTHING_SPEED else tracking.detector.maxFootSpeed
        smoother.add(lat = fix.lat, lon = fix.lon, accuracyMeters = fix.accuracyMeters, timeMillis = fix.timeMillis)
        tracking.lastFixMillis = fix.timeMillis
        val here = RoutePoint(lat = smoother.lat, lon = smoother.lon, timeMillis = fix.timeMillis)
        val speed = fix.speedMetersPerSecond?.takeIf { it >= 0 } ?: previous?.let {
            val seconds = (here.timeMillis - it.timeMillis) / 1000.0
            if (seconds > 0) distanceMeters(from = it, to = here) / seconds else null
        }
        val switchSince = if (current.isPaused) {
            null
        } else {
            tracking.detector.update(
                isRiding = current.isRiding,
                timeMillis = fix.timeMillis,
                speed = speed,
                lastStepMillis = tracking.lastStepMillis,
                stepsSinceMillis = tracking.stepsSinceMillis,
                activity = tracking.activity
            )
        }
        val (offset, offsetSamples) = altitudeOffset(tracking = tracking, recording = current, fix = fix)
        val altitude = altitude(tracking = tracking, fix = fix, offset = offset)
        _state.update { recording ->
            if (recording == null) return@update null
            var next = recording.withAltitudeOffset(offset = offset, samples = offsetSamples)
            if (switchSince != null && next.isRiding == current.isRiding) next = next.switchRiding(isRiding = !next.isRiding, sinceMillis = switchSince)
            val point = here.copy(altitude = altitude, segment = next.segment, isRiding = next.isRiding)
            next = next.copy(lastAccuracyMeters = fix.accuracyMeters, livePoint = point)
            if (next.isPaused) return@update next
            val last = next.points.lastOrNull()
            val gap = if (last != null && last.segment == point.segment) distanceMeters(from = last, to = point) else null
            when {
                gap != null && gap < maxOf(MIN_POINT_GAP_METERS, fix.accuracyMeters / 2) -> next
                else -> next.copy(
                    points = next.points + point,
                    distanceMeters = next.distanceMeters + if (last != null && last.continuesTo(next = point)) gap ?: 0.0 else 0.0
                )
            }
        }
    }

    /** A fix too far from the last to have been reached in time; a few in a row are believed after all. */
    private fun isJump(tracking: Tracking, fix: LocationFix): Boolean {
        val smoother = tracking.smoother
        if (!smoother.hasPosition) return false
        val seconds = (fix.timeMillis - tracking.lastFixMillis) / 1000.0
        val meters = distanceMeters(
            from = RoutePoint(lat = smoother.lat, lon = smoother.lon, timeMillis = 0),
            to = RoutePoint(lat = fix.lat, lon = fix.lon, timeMillis = 0)
        )
        if (seconds <= 0 || (meters - fix.accuracyMeters) / seconds <= MAX_JUMP_SPEED) {
            tracking.jumps = 0
            return false
        }
        tracking.jumps++
        if (tracking.jumps < JUMPS_BEFORE_RESET) return true
        tracking.jumps = 0
        smoother.reset(lat = fix.lat, lon = fix.lon, accuracyMeters = fix.accuracyMeters, timeMillis = tracking.lastFixMillis)
        return false
    }

    /** The barometer's offset to sea level, refined by each good GPS altitude until there are enough. */
    private fun altitudeOffset(tracking: Tracking, recording: Recording, fix: LocationFix): Pair<Double?, Int> {
        val barometer = tracking.barometerAltitude
        val gps = fix.altitude
        val samples = recording.altitudeOffsetSamples
        if (barometer == null || gps == null || samples >= ALTITUDE_OFFSET_SAMPLES) return recording.altitudeOffset to samples
        if ((fix.verticalAccuracyMeters ?: return recording.altitudeOffset to samples) > MAX_VERTICAL_ACCURACY_METERS) {
            return recording.altitudeOffset to samples
        }
        val offset = ((recording.altitudeOffset ?: 0.0) * samples + (gps - barometer)) / (samples + 1)
        return offset to samples + 1
    }

    /**
     * Height for a new point: the barometer's where there is one; otherwise GPS altitude when the phone is sure of
     * it, smoothed. Nothing until it's known whether there's a barometer, so the two are never mixed on one route.
     */
    private fun altitude(tracking: Tracking, fix: LocationFix, offset: Double?): Double? = when (tracking.hasBarometer) {
        true -> tracking.barometerAltitude?.let { it + (offset ?: 0.0) }
        false -> {
            val gps = fix.altitude?.takeIf { (fix.verticalAccuracyMeters ?: 0.0) <= MAX_VERTICAL_ACCURACY_METERS }
            if (gps != null) {
                tracking.gpsAltitude = tracking.gpsAltitude?.let { it + GPS_ALTITUDE_SMOOTHING * (gps - it) } ?: gps
            }
            tracking.gpsAltitude
        }
        null -> null
    }

    @OptIn(ExperimentalTime::class)
    private fun now(): Long = Clock.System.now().toEpochMilliseconds()
}

/** Moves the heights recorded so far along with a changed sea-level [offset]. */
private fun Recording.withAltitudeOffset(offset: Double?, samples: Int): Recording {
    if (offset == altitudeOffset) return this
    val shift = (offset ?: 0.0) - (altitudeOffset ?: 0.0)
    return copy(
        points = points.map { point -> point.altitude?.let { point.copy(altitude = it + shift) } ?: point },
        altitudeOffset = offset,
        altitudeOffsetSamples = samples
    )
}

/**
 * Starts or stops riding from [sinceMillis], when the evidence began: the points since then move into a new
 * segment marked [isRiding], and distance and moving time are worked out again without the riding.
 */
private fun Recording.switchRiding(isRiding: Boolean, sinceMillis: Long): Recording {
    val nextSegment = segment + 1
    val moved = points.map { point ->
        if (point.segment == segment && point.timeMillis >= sinceMillis) point.copy(segment = nextSegment, isRiding = isRiding) else point
    }
    val movedBefore = if (isRiding) {
        movedMillisBefore + (stretchStartMillis?.let { (sinceMillis - it).coerceAtLeast(minimumValue = 0) } ?: 0L)
    } else {
        movedMillisBefore
    }
    return copy(
        isRiding = isRiding,
        segment = nextSegment,
        points = moved,
        distanceMeters = moved.routeDistance(),
        movedMillisBefore = movedBefore,
        stretchStartMillis = if (isRiding || isPaused) null else sinceMillis
    )
}
