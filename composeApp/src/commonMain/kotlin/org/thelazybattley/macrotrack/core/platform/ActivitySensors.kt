package org.thelazybattley.macrotrack.core.platform

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.repository.ActivityRepository

/** The phone's motion sensor, counting steps all day without location. */
interface StepSensor {

    /** False on phones without a step counter; steps then stay at zero and walks estimate theirs from distance. */
    val isAvailable: Boolean

    /** Keeps the hourly counts in [repository] up to date for as long as it runs. */
    suspend fun track(repository: ActivityRepository)

    /** A running step count while collected. Only the differences between readings mean anything. */
    fun counts(): Flow<Long>
}

/** A GPS fix. [accuracyMeters] is the radius the phone is fairly sure of. */
data class LocationFix(
    val lat: Double,
    val lon: Double,
    val altitude: Double?,
    val accuracyMeters: Double,
    val timeMillis: Long
)

/** Location while a walk or run is recorded, and never otherwise. */
interface LocationTracker {

    /**
     * Fixes for as long as it's collected. The phone keeps them coming with the screen off, showing that
     * a [type] is being recorded.
     */
    fun fixes(type: ActivityType): Flow<LocationFix>
}

/** The evening reminder when the step goal is far off. */
interface StepNudgeScheduler {

    /** False where the reminder can't be set, so its switch isn't offered. */
    val isSupported: Boolean get() = true

    /** Sets the next reminder at [hour], or clears it when [isEnabled] is false. */
    fun schedule(isEnabled: Boolean, hour: Int)
}

object NoStepNudgeScheduler : StepNudgeScheduler {
    override val isSupported: Boolean = false
    override fun schedule(isEnabled: Boolean, hour: Int) = Unit
}

/**
 * The silent steps notification and the goal-reached alert, kept up to date about every
 * [org.thelazybattley.macrotrack.domain.model.StepNotificationSettings.REFRESH_MINUTES] minutes. Step counting never
 * depends on either.
 */
interface StepNotifier {

    /** False where these notifications can't be shown, so their settings aren't offered. */
    val isSupported: Boolean get() = true

    /**
     * Posts, updates or clears the steps notification to match the saved settings and today's steps, sends the
     * goal-reached alert if it's due, and sets or clears the refresh that keeps doing so.
     */
    suspend fun refresh()

    /** The pop-down shown once, right after choosing to just count steps. */
    fun announceStepsOnly()
}

object NoStepNotifier : StepNotifier {
    override val isSupported: Boolean = false
    override suspend fun refresh() = Unit
    override fun announceStepsOnly() = Unit
}

/**
 * Access to something the system guards, like location or motion. [isPermanentlyDenied] means the system won't
 * ask again, so [request] opens the app's settings.
 */
class SensorPermissionState(
    val isGranted: Boolean,
    val isPermanentlyDenied: Boolean,
    val request: () -> Unit
)

/** Motion access for counting steps. Granted where the platform doesn't ask. */
@Composable
expect fun rememberMotionPermissionState(): SensorPermissionState

/** Location while the app is in use, for drawing a walk or run's route. */
@Composable
expect fun rememberLocationPermissionState(): SensorPermissionState

/** Permission to post the steps notification and step alerts. Granted where the platform doesn't ask. */
@Composable
expect fun rememberNotificationPermissionState(): SensorPermissionState
